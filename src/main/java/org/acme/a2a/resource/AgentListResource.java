package org.acme.a2a.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.a2a.entity.AgentEntity;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a lightweight registry view of configured agents for the UI.
 *
 * The endpoint returns agent identity, prompt preview, enabled tools, and
 * downstream relationships used to render orchestration topology.
 */
@Path("/api/agents")
public class AgentListResource {

    private static final Logger LOG = Logger.getLogger(AgentListResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AgentInfo> listAgents() {
        LOG.info("Fetching all agents with relationships");
        
        List<AgentEntity> agents = AgentEntity.listAll();
        
        return agents.stream()
            .map(agent -> new AgentInfo(
                agent.id,
                agent.name,
                agent.systemPrompt != null ? agent.systemPrompt.substring(0, Math.min(100, agent.systemPrompt.length())) + "..." : "",
                agent.enabledTools != null ? agent.enabledTools : List.of(),
                agent.downstreamPeers != null ? agent.downstreamPeers : List.of()
            ))
            .collect(Collectors.toList());
    }

    /**
     * DTO returned by {@link #listAgents()} for dashboard rendering.
     */
    public static record AgentInfo(
        String id,
        String name,
        String promptPreview,
        List<String> enabledTools,
        List<String> downstreamPeers
    ) {}
}
