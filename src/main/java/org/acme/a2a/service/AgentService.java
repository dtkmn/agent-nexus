package org.acme.a2a.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import org.acme.a2a.entity.AgentEntity;
import org.acme.a2a.model.AgentConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AgentService {

    @Inject
    ToolFactory toolFactory;

    // In-memory cache for "hot" agents to avoid hitting DB on every request
    private final Map<String, AgentConfig> configCache = new ConcurrentHashMap<>();

    /**
     * Provisioning: Saves a new agent definition to the database.
     */
    @Transactional
    public void provision(AgentEntity entity) {
        if (AgentEntity.findById(entity.id) != null) {
            throw new IllegalArgumentException("Agent ID '" + entity.id + "' is already taken.");
        }
        entity.persist();
    }

    /**
     * Hydration: Loads agent config and prepares it for execution.
     * This is called by the Gateway for every request.
     */
    public AgentConfig loadConfig(String agentId) {
        io.quarkus.logging.Log.info("Loading config for agent: " + agentId);
        // 1. Check Cache
        if (configCache.containsKey(agentId)) {
            return configCache.get(agentId);
        }

        // 2. Check Database
        AgentEntity entity = AgentEntity.findById(agentId);
        if (entity == null) {
            return null; // Agent not found
        }

        // 3. Transform Entity -> Runtime Config
        AgentConfig config = new AgentConfig();
        config.id = entity.id;
        config.name = entity.name;
        config.systemPrompt = entity.systemPrompt;
        config.enabledTools = entity.enabledTools;
        config.downstreamPeers = entity.downstreamPeers;

        // 4. Initialize MCP tools based on entity configuration
        config.tools = toolFactory.createToolsFor(entity.enabledTools, entity.downstreamPeers);

        // 5. Cache and Return
        configCache.put(agentId, config);
        return config;
    }

    /**
     * Invalidates the cache for a specific agent.
     * Useful when an agent's configuration is updated.
     */
    public void invalidateCache(String agentId) {
        configCache.remove(agentId);
    }

    /**
     * Clears the entire agent configuration cache.
     */
    public void clearCache() {
        configCache.clear();
    }
}