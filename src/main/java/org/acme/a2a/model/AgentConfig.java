package org.acme.a2a.model;

import java.util.List;

/**
 * Runtime configuration for a Virtual Agent.
 * This is a lightweight POJO created from AgentEntity for execution.
 */
public class AgentConfig {

    public String id;
    public String name;
    public String systemPrompt;
    public List<String> enabledTools;
    public List<String> downstreamPeers;

    // Tools will be initialized by ToolFactory
    public List<Object> tools;

    public AgentConfig() {
    }

    public AgentConfig(String id, String name, String systemPrompt) {
        this.id = id;
        this.name = name;
        this.systemPrompt = systemPrompt;
    }
}

