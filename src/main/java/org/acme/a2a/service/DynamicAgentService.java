package org.acme.a2a.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.a2a.model.AgentConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core runtime engine that executes agent prompts and delegation decisions.
 *
 * It supports direct chat responses, routing to downstream peers, and a
 * bounded delegation depth to prevent orchestration loops.
 */
@ApplicationScoped
public class DynamicAgentService {

    private static final Logger LOG = Logger.getLogger(DynamicAgentService.class);
    private static final Pattern TARGET_JSON_PATTERN =
            Pattern.compile("\"target\"\\s*:\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    @Inject
    ChatModel chatModel;

    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String apiKey;

    @ConfigProperty(name = "agent.orchestration.max-depth", defaultValue = "3")
    int maxDelegationDepth;

    public String processRequest(AgentConfig config, String userMessage) {
        return processRequest(config, userMessage, 0);
    }

    public String processRequest(AgentConfig config, String userMessage, int delegationDepth) {
        LOG.debug("Processing request for agent: " + config.id);

        // Check if OpenAI is properly configured
        if (apiKey == null || "demo-key-not-set".equals(apiKey)) {
            LOG.warn("OpenAI API key is not configured. Please set OPENAI_API_KEY environment variable.");
            return String.format(
                "I am %s (Agent ID: %s). However, I cannot process your request because " +
                "the OpenAI API key is not configured. Please set the OPENAI_API_KEY environment variable. " +
                "Your message was: '%s'", 
                config.name, config.id, userMessage
            );
        }

        if (userMessage == null || userMessage.isBlank()) {
            return "Please provide a non-empty 'message' field.";
        }

        if (delegationDepth > maxDelegationDepth) {
            LOG.warnf("Delegation depth limit reached for agent %s at depth %d", config.id, delegationDepth);
            return "Delegation depth limit reached. Please refine your request.";
        }

        try {
            if (chatModel == null) {
                LOG.error("ChatModel is null! This should not happen if API key is set.");
                return "Error: AI model not initialized despite having API key configured.";
            }

            if (hasDownstreamPeers(config)) {
                return orchestrateWithDelegation(config, userMessage, delegationDepth);
            }

            // If agent has no tools, use simple chat
            if (config.tools == null || config.tools.isEmpty()) {
                return simpleChat(config, userMessage);
            }

            // Agent has tools - use tool execution loop
            LOG.debug("Agent has " + config.tools.size() + " tools available, executing with tool support");
            return chatWithTools(config, userMessage);

        } catch (Exception e) {
            LOG.error("Error processing request for agent " + config.id, e);
            return "Error processing request: " + e.getMessage() + 
                   ". Please check the server logs for more details.";
        }
    }

    /**
     * Simple chat without tools
     */
    private String simpleChat(AgentConfig config, String userMessage) {
        SystemMessage systemMessage = new SystemMessage(config.systemPrompt);
        UserMessage userMsg = new UserMessage(userMessage);

        ChatResponse response = chatModel.chat(systemMessage, userMsg);
        AiMessage aiMessage = response.aiMessage();
        return aiMessage == null ? "" : aiMessage.text();
    }

    private boolean hasDownstreamPeers(AgentConfig config) {
        return config.downstreamPeers != null && !config.downstreamPeers.isEmpty();
    }

    private String orchestrateWithDelegation(AgentConfig config, String userMessage, int delegationDepth) {
        String selectedPeer = selectTargetPeer(config, userMessage);

        // SELF means no delegation needed.
        if (selectedPeer == null) {
            LOG.debugf("Orchestrator %s chose to answer directly", config.id);
            return simpleChat(config, userMessage);
        }

        ToolFactory.PeerDelegationTool delegationTool = findDelegationTool(config.tools, selectedPeer);
        if (delegationTool == null) {
            LOG.warnf("No delegation tool configured for peer '%s' on orchestrator '%s'", selectedPeer, config.id);
            return "Unable to delegate to " + selectedPeer + " because delegation is not configured.";
        }

        LOG.infof("Orchestrator %s delegating request to %s", config.id, selectedPeer);
        String delegatedReply = delegationTool.callAgent(userMessage, delegationDepth + 1);

        // Keep attribution explicit for demo transparency.
        return "Delegated to " + selectedPeer + ":\n" + delegatedReply;
    }

    private String selectTargetPeer(AgentConfig config, String userMessage) {
        String peers = String.join(", ", config.downstreamPeers);
        String routingInstructions = """
                You are a strict routing engine for a multi-agent orchestrator.
                Pick exactly one target from [%s] or SELF.
                Return JSON only, exactly this schema:
                {"target":"<agent-id-or-SELF>","reason":"<short reason>"}
                Rules:
                - Use SELF for greetings, small talk, or unclear/ambiguous requests.
                - Otherwise select the single best specialist agent.
                - Never invent agent IDs.
                """.formatted(peers);

        ChatResponse routingResponse =
                chatModel.chat(new SystemMessage(routingInstructions), new UserMessage(userMessage));
        AiMessage routingMessage = routingResponse.aiMessage();
        String raw = routingMessage == null ? "" : routingMessage.text();
        String target = parseTarget(raw, config.downstreamPeers);

        if (target == null) {
            LOG.debugf("Routing decision: SELF (raw response: %s)", raw);
        } else {
            LOG.debugf("Routing decision: %s (raw response: %s)", target, raw);
        }

        return target;
    }

    private String parseTarget(String raw, List<String> peers) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        Matcher matcher = TARGET_JSON_PATTERN.matcher(raw);
        if (matcher.find()) {
            String candidate = normalizeTarget(matcher.group(1), peers);
            if (candidate != null || isSelfToken(matcher.group(1))) {
                return candidate;
            }
        }

        // Fallback: if JSON is malformed, still recover using simple matching.
        String normalizedRaw = raw.toLowerCase(Locale.ROOT);
        if (normalizedRaw.contains("self")) {
            return null;
        }

        for (String peer : peers) {
            if (normalizedRaw.contains(peer.toLowerCase(Locale.ROOT))) {
                return peer;
            }
        }
        return null;
    }

    private String normalizeTarget(String candidate, List<String> peers) {
        if (candidate == null) {
            return null;
        }
        String cleaned = candidate.trim().replace("`", "");
        if (isSelfToken(cleaned)) {
            return null;
        }

        for (String peer : peers) {
            if (peer.equalsIgnoreCase(cleaned)) {
                return peer;
            }
        }
        return null;
    }

    private boolean isSelfToken(String token) {
        if (token == null) {
            return false;
        }
        String normalized = token.trim().toUpperCase(Locale.ROOT);
        return Objects.equals(normalized, "SELF") || Objects.equals(normalized, "NONE");
    }

    private ToolFactory.PeerDelegationTool findDelegationTool(List<Object> tools, String targetPeer) {
        if (tools == null || tools.isEmpty()) {
            return null;
        }
        for (Object tool : tools) {
            if (tool instanceof ToolFactory.PeerDelegationTool peerTool
                    && peerTool.getPeerId().equalsIgnoreCase(targetPeer)) {
                return peerTool;
            }
        }
        return null;
    }

    /**
     * Chat with tool execution loop
     * For now, we'll use a simplified approach - just mention tools in system prompt
     * Full tool execution requires AiServices API which needs different setup
     */
    private String chatWithTools(AgentConfig config, String userMessage) {
        // Build a system message that mentions available tools
        StringBuilder enhancedPrompt = new StringBuilder(config.systemPrompt);
        enhancedPrompt.append("\n\nYou have access to the following tools:\n");
        
        for (Object tool : config.tools) {
            if (tool instanceof ToolFactory.PeerDelegationTool) {
                enhancedPrompt.append("- callAgent(peerId, query): Delegate to specialist agents: ");
                enhancedPrompt.append(String.join(", ", config.downstreamPeers));
                enhancedPrompt.append("\n");
            }
        }
        
        enhancedPrompt.append("\nFor now, please indicate which agent you would delegate to and what you would ask them.");
        
        SystemMessage systemMessage = new SystemMessage(enhancedPrompt.toString());
        UserMessage userMsg = new UserMessage(userMessage);

        ChatResponse response = chatModel.chat(systemMessage, userMsg);
        AiMessage aiMessage = response.aiMessage();

        // TODO: Parse response for delegation intent and actually call the tool
        // For now, just return the LLM's response
        return aiMessage == null ? "" : aiMessage.text();
    }
}
