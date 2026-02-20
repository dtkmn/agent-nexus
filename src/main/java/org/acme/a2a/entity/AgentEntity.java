package org.acme.a2a.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import java.util.ArrayList;

/**
 * Database entity representing a persisted virtual agent definition.
 *
 * JSON columns keep tool identifiers and downstream peer IDs used for runtime
 * orchestration.
 */
@Entity
@Table(name = "agents")
public class AgentEntity extends PanacheEntityBase {

    @Id
    public String id; // e.g., "support-bot-01"

    public String name;

    @Column(length = 4096)
    public String systemPrompt;

    // Stores JSON list of tool names or MCP connection strings
    @JdbcTypeCode(SqlTypes.JSON)
    public List<String> enabledTools = new ArrayList<>();

    // Stores JSON list of other Agent IDs this one can call (A2A)
    @JdbcTypeCode(SqlTypes.JSON)
    public List<String> downstreamPeers = new ArrayList<>();
}
