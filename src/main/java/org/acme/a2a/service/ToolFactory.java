package org.acme.a2a.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Factory for creating LangChain4j Tool definitions.
 * Handles both standard MCP tools and A2A peer delegation tools.
 */
@ApplicationScoped
public class ToolFactory {

    private static final String GATEWAY_BASE_URL = "http://localhost:8080";

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
                tools.add(new PeerDelegationTool(peerId, GATEWAY_BASE_URL));
            }
        }

        return tools;
    }

    // Standard weather tool example
    public static class WeatherTool {
        @dev.langchain4j.agent.tool.Tool("Get the current weather for a location")
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

        @dev.langchain4j.agent.tool.Tool("Delegate a question or task to a specialized agent")
        public String callAgent(String question) {
            try {
                LOG.info("Delegating to agent '" + peerId + "': " + question);
                
                // Build REST client
                AgentClient client = RestClientBuilder.newBuilder()
                    .baseUri(URI.create(gatewayBaseUrl))
                    .build(AgentClient.class);
                
                // Call the peer agent
                Map<String, String> request = Map.of("message", question);
                Map<String, String> response = client.sendMessage(peerId, request);
                
                String reply = response.get("reply");
                LOG.info("Agent '" + peerId + "' responded: " + reply);
                
                return reply;
            } catch (Exception e) {
                LOG.error("Failed to call agent '" + peerId + "'", e);
                return "Error: Unable to reach the " + peerId + " agent. " + e.getMessage();
            }
        }
    }

    /**
     * REST client interface for calling peer agents
     */
    @jakarta.ws.rs.Path("/agents")
    public interface AgentClient {
        
        @jakarta.ws.rs.POST
        @jakarta.ws.rs.Path("/{agentId}/message")
        @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
        @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
        Map<String, String> sendMessage(
            @jakarta.ws.rs.PathParam("agentId") String agentId,
            Map<String, String> request
        );
    }
}