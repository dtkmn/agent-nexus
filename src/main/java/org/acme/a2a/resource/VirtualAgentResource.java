package org.acme.a2a.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.a2a.model.AgentConfig;
import org.acme.a2a.service.DynamicAgentService;
import java.util.Map;

/**
 * Represents a single, hydrated Virtual Agent.
 * Note: NO class-level @Path annotation!
 */
public class VirtualAgentResource {

    private final AgentConfig config;
    private final DynamicAgentService agentService;

    public VirtualAgentResource(AgentConfig config, DynamicAgentService agentService) {
        System.err.println("!!! VirtualAgentResource INSTANTIATED for " + config.id);
        this.config = config;
        this.agentService = agentService;
    }

    // Endpoint: /agents/{id}/.well-known/agent-card.json
    @GET
    @Path(".well-known/agent-card.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentCard() {
        // Return metadata required by the A2A protocol
        var card = Map.of(
                "id", config.id,
                "name", config.name,
                "description", "I am a virtual agent powered by Quarkus.",
                "homepage", "/agents/" + config.id);
        return Response.ok(card).build();
    }

    // Endpoint: /agents/{id}/message
    @POST
    @Path("message")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleMessage(Map<String, String> request) {
        String userMessage = request.get("message");
        System.err.println("!!! Handling message: " + userMessage);

        try {
            System.err.println("!!! Using agentService: " + agentService);

            String reply = agentService.processRequest(config, userMessage);
            return Response.ok(Map.of("reply", reply)).build();
        } catch (Exception e) {
            System.err.println("!!! ERROR in handleMessage: " + e.getMessage());
            e.printStackTrace();
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }
}