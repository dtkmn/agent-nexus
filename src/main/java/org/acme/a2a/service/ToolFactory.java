package org.acme.a2a.service;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Factory for creating LangChain4j Tool definitions.
 * Handles both standard MCP tools and A2A peer delegation tools.
 */
@ApplicationScoped
public class ToolFactory {

    @ConfigProperty(name = "agent.gateway.base-url", defaultValue = "http://localhost:8080")
    String gatewayBaseUrl;

    public List<Object> createToolsFor(List<String> toolNames, List<String> peers) {
        List<Object> tools = new ArrayList<>();

        if (toolNames != null) {
            // 1. Add Standard Tools (Simulated MCP)
            for (String toolName : toolNames) {
                if ("weather".equals(toolName)) {
                    tools.add(new WeatherTool());
                }
                // Add more tool types as needed
            }
        }

        if (peers != null) {
            // 2. Add A2A Tools (Dynamic delegation)
            for (String peerId : peers) {
                tools.add(new PeerDelegationTool(peerId, gatewayBaseUrl));
            }
        }

        return tools;
    }

    // Standard weather tool example
    public static class WeatherTool {
        @Tool("Get the current weather for a location")
        public String getWeather(String location) {
            return "The weather in " + location + " is sunny and 25 degrees Celsius.";
        }
    }

    /**
     * Tool that delegates to another agent via HTTP
     */
    public static class PeerDelegationTool {
        private static final Logger LOG = Logger.getLogger(PeerDelegationTool.class);
        private final String peerId;
        private final String gatewayBaseUrl;

        public PeerDelegationTool(String peerId, String gatewayBaseUrl) {
            this.peerId = peerId;
            this.gatewayBaseUrl = gatewayBaseUrl;
        }

        @Tool("Delegate a question or task to a specialized agent")
        public String callAgent(String question) {
            return callAgent(question, 0);
        }

        public String callAgent(String question, int delegationDepth) {
            try {
                LOG.info("Delegating to agent '" + peerId + "': " + question);

                // Build REST client
                AgentClient client = RestClientBuilder.newBuilder()
                    .baseUri(URI.create(gatewayBaseUrl))
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build(AgentClient.class);

                // Call the peer agent
                Map<String, String> request = Map.of(
                        "message", question == null ? "" : question,
                        "_delegationDepth", String.valueOf(delegationDepth)
                );
                Map<String, String> response = client.sendMessage(peerId, request);

                String reply = response == null ? null : response.get("reply");
                LOG.info("Agent '" + peerId + "' responded: " + reply);

                if (reply == null || reply.isBlank()) {
                    return "Delegation to " + peerId + " returned an empty response.";
                }

                return reply;
            } catch (Exception e) {
                LOG.error("Failed to call agent '" + peerId + "'", e);
                return "Error: Unable to reach the " + peerId + " agent. " + e.getMessage();
            }
        }

        public String getPeerId() {
            return peerId;
        }
    }

    /**
     * REST client interface for calling peer agents
     */
    @Path("/agents")
    public interface AgentClient {
        
        @POST
        @Path("/{agentId}/message")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        Map<String, String> sendMessage(
            @PathParam("agentId") String agentId,
            Map<String, String> request
        );
    }
}
