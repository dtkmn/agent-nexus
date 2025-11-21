package org.acme.a2a.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.a2a.model.AgentConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DynamicAgentService {

    private static final Logger LOG = Logger.getLogger(DynamicAgentService.class);

    @Inject
    ChatLanguageModel chatModel;

    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String apiKey;

    public String processRequest(AgentConfig config, String userMessage) {
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

        try {
            if (chatModel == null) {
                LOG.error("ChatLanguageModel is null! This should not happen if API key is set.");
                return "Error: AI model not initialized despite having API key configured.";
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
        
        Response<AiMessage> response = chatModel.generate(systemMessage, userMsg);
        return response.content().text();
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
        
        Response<AiMessage> response = chatModel.generate(systemMessage, userMsg);
        
        // TODO: Parse response for delegation intent and actually call the tool
        // For now, just return the LLM's response
        return response.content().text();
    }
}
