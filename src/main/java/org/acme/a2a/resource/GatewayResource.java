package org.acme.a2a.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.a2a.service.AgentService;
import org.acme.a2a.entity.AgentEntity;
import org.acme.a2a.model.AgentConfig;
import org.acme.a2a.service.DynamicAgentService;

/**
 * Entry-point resource for agent lifecycle and per-agent request dispatch.
 *
 * It provisions agent definitions and routes `/agents/{agentId}` requests to a
 * dedicated {@link VirtualAgentResource} instance.
 */
@Path("/agents")
public class GatewayResource {

    public GatewayResource() {
        System.err.println("!!! GATEWAY RESOURCE INSTANTIATED !!!");
    }

    @Inject
    AgentService service;

    @Inject
    DynamicAgentService agentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAgent(AgentEntity entity) {
        System.err.println("!!! Creating agent: " + entity.name);
        service.provision(entity);
        return Response.status(201).build();
    }

    @Path("{agentId}")
    public VirtualAgentResource dispatch(@PathParam("agentId") String agentId) {
        System.err.println("!!! Dispatching for agent: " + agentId);
        // 1. Load the configuration for the requested ID
        AgentConfig config = service.loadConfig(agentId);

        // 2. If not found, 404 immediately
        if (config == null) {
            throw new NotFoundException("Virtual Agent '" + agentId + "' does not exist.");
        }

        // 3. Create and return the virtual handler
        // The JAX-RS runtime will then call the matching @GET/@POST
        // method on THIS returned object.
        return new VirtualAgentResource(config, agentService);
    }
}
