# Architecture Documentation

## System Architecture

### High-Level Overview

Agent Nexus is a containerized multi-agent orchestration platform built on Quarkus with LangChain4j for AI integration. It enables dynamic creation and coordination of AI agents with different specializations.

```
┌──────────────────────────────────────────────────────────┐
│                        Client Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │
│  │  Web UI     │  │  REST API   │  │  CLI Tools  │      │
│  │  (Browser)  │  │  Clients    │  │  (curl)     │      │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘      │
└─────────┼────────────────┼────────────────┼──────────────┘
          │                │                │
          └────────────────┴────────────────┘
                           │ HTTP/REST
          ┌────────────────▼────────────────┐
          │   Agent Nexus Application       │
          │  ┌──────────────────────────┐  │
          │  │  Quarkus REST Endpoints  │  │
          │  │  - GatewayResource       │  │
          │  │  - AgentListResource     │  │
          │  │  - VirtualAgentResource  │  │
          │  └───────────┬──────────────┘  │
          │              │                  │
          │  ┌───────────▼──────────────┐  │
          │  │    Service Layer         │  │
          │  │  - DynamicAgentService   │  │
          │  │  - AgentService          │  │
          │  │  - ToolFactory           │  │
          │  └───────────┬──────────────┘  │
          │              │                  │
          │  ┌───────────▼──────────────┐  │
          │  │  Persistence Layer       │  │
          │  │  - JPA/Panache           │  │
          │  │  - AgentEntity           │  │
          │  └───────────┬──────────────┘  │
          └──────────────┼──────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │                                 │
   ┌────▼─────┐                    ┌─────▼──────┐
   │PostgreSQL│                    │  OpenAI    │
   │ Database │                    │  GPT-4o    │
   └──────────┘                    └────────────┘
```

---

## Component Details

### 1. REST Layer

#### GatewayResource
- **Path**: `/agents`
- **Responsibilities**:
  - Agent CRUD operations
  - Sub-resource locator pattern for dynamic routing
  - Delegates to VirtualAgentResource for per-agent endpoints

#### AgentListResource
- **Path**: `/api/agents`
- **Responsibilities**:
  - List all agents with metadata
  - Provides data for UI visualization
  - Returns agent relationships (downstream peers)

#### VirtualAgentResource
- **Path**: `/agents/{agentId}/*`
- **Responsibilities**:
  - Per-agent message handling
  - Dynamic endpoint creation for each agent
  - Routes messages to DynamicAgentService

---

### 2. Service Layer

#### DynamicAgentService
**Core LLM integration service**

```java
@ApplicationScoped
public class DynamicAgentService {
    // Builds ChatLanguageModel for each agent
    // Processes requests with tool awareness
    // Manages LLM interactions
}
```

**Key Methods**:
- `processRequest()`: Main entry point for agent interactions
- `chatWithTools()`: Handles tool-enabled agents
- `simpleChat()`: Handles simple agents without tools

**Design Pattern**: Factory pattern for LLM creation

#### AgentService
**Agent lifecycle management**

```java
@ApplicationScoped
public class AgentService {
    // CRUD operations on AgentEntity
    // Validation and persistence
}
```

#### ToolFactory
**Tool instantiation for agents**

Currently supports:
- `PeerDelegationTool`: Enables agent-to-agent delegation

---

### 3. Data Layer

#### AgentEntity
**JPA entity for agent persistence**

```java
@Entity
public class AgentEntity extends PanacheEntity {
    public String id;              // Unique identifier
    public String name;            // Display name
    public String systemPrompt;    // LLM system prompt
    public String modelName;       // LLM model (e.g., gpt-4o)
    
    @Convert(converter = StringListConverter.class)
    public List<String> enabledTools;
    
    @Convert(converter = StringListConverter.class)
    public List<String> downstreamPeers;
}
```

**Database Schema**:
```sql
CREATE TABLE agententity (
    id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    systemprompt TEXT,
    modelname VARCHAR(100),
    enabledtools TEXT,        -- JSON array
    downstreampeers TEXT      -- JSON array
);
```

---

### 4. Frontend Layer

#### Web UI (`index.html`)
**Single-page application with D3.js visualization**

**Components**:
1. **Header**: Title and branding
2. **Controls**: Refresh button, auto-refresh toggle, status indicator
3. **Graph Container**: D3.js force-directed graph
4. **Agent Details Panel**: Selected agent information
5. **Legend**: Agent type color coding

**Technologies**:
- D3.js v7: Force simulation and graph rendering
- Vanilla JavaScript: No framework dependencies
- CSS3: Glassmorphism effects, animations

**Data Flow**:
```
User Action → fetchAgents() → /api/agents
                            ↓
                   visualizeAgents(data)
                            ↓
              D3.js Force Simulation
                            ↓
          Interactive Graph Rendering
```

---

## Agent Types & Behavior

### Simple Agent
```json
{
  "name": "simple-agent",
  "systemPrompt": "You are a helpful assistant",
  "enabledTools": [],
  "downstreamPeers": []
}
```
**Behavior**: Direct LLM chat, no tools

### Specialist Agent
```json
{
  "name": "billing-agent",
  "systemPrompt": "You are a billing specialist",
  "enabledTools": ["SomeSpecializedTool"],
  "downstreamPeers": []
}
```
**Behavior**: LLM with access to specific tools

### Orchestrator Agent
```json
{
  "name": "orchestrator",
  "systemPrompt": "Route billing→billing-agent, tech→technical-agent",
  "enabledTools": ["PeerDelegationTool"],
  "downstreamPeers": ["billing-agent", "technical-agent"]
}
```
**Behavior**: Analyzes requests and delegates to downstream agents

---

## Request Flow

### Direct Agent Message Flow

```
1. Client sends POST /agents/billing-agent/message
          ↓
2. VirtualAgentResource.sendMessage() invoked
          ↓
3. DynamicAgentService.processRequest() called
          ↓
4. ChatLanguageModel created for agent
          ↓
5. LLM request sent to OpenAI GPT-4o
          ↓
6. Response returned through chain
          ↓
7. Client receives JSON response
```

### Orchestrator Delegation Flow

```
1. Client sends POST /agents/orchestrator/message
          ↓
2. VirtualAgentResource routes to DynamicAgentService
          ↓
3. Orchestrator analyzes request with LLM
          ↓
4. LLM identifies downstream agent: "billing-agent"
          ↓
5. PeerDelegationTool mentioned in response
          ↓
6. Response indicates delegation intent
          ↓
7. Client receives orchestrator's routing decision
```

**Note**: Current implementation shows delegation intent but doesn't execute HTTP calls to downstream agents automatically.

---

## Database Design

### Schema

```sql
-- Agent storage
CREATE TABLE agententity (
    id BIGSERIAL PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    systemprompt TEXT,
    modelname VARCHAR(100) DEFAULT 'gpt-4o',
    enabledtools TEXT,        -- Serialized JSON array
    downstreampeers TEXT      -- Serialized JSON array
);

-- Indexes for performance
CREATE INDEX idx_agententity_id ON agententity(id);
CREATE INDEX idx_agententity_name ON agententity(name);
```

### Data Storage Strategy

**Agent Configuration**: Stored as structured data in PostgreSQL
**Conversation History**: Not persisted (future enhancement)
**Tool Results**: Ephemeral (in-memory during request)

---

## Deployment Architecture

### Docker Compose Setup

```yaml
services:
  postgres:
    - Persistent volume for data
    - Health check configured
    - Isolated network
    
  agent-nexus:
    - Multi-stage Docker build
    - Dependency caching
    - Waits for postgres health
    - Environment-based configuration
```

### Container Communication

```
┌─────────────────────────────────────┐
│   Docker Network: a2a-network       │
│                                     │
│  ┌──────────────┐  ┌─────────────┐ │
│  │  agent-nexus │  │  postgres   │ │
│  │  :8080       │◄─┤  :5432      │ │
│  └──────┬───────┘  └─────────────┘ │
│         │                           │
└─────────┼───────────────────────────┘
          │
          │ Outbound to Internet
          ▼
   ┌──────────────┐
   │ OpenAI API   │
   │ (external)   │
   └──────────────┘
```

---

## Security Considerations

### Current State
⚠️ **This is a demo/prototype**

**Missing for Production**:
- No authentication/authorization
- No rate limiting
- No input sanitization
- No conversation encryption
- No audit logging
- API keys in environment variables (use secrets manager)

### Recommendations for Production

1. **Authentication**: Add JWT or OAuth2
2. **Authorization**: Role-based access control (RBAC)
3. **Rate Limiting**: Prevent abuse
4. **Input Validation**: Sanitize all user inputs
5. **Secrets Management**: Use Vault or AWS Secrets Manager
6. **HTTPS**: TLS/SSL certificates
7. **Network Isolation**: Private subnets for database
8. **Audit Logging**: Track all agent operations

---

## Performance Characteristics

### Quarkus Startup Time
- **Dev Mode**: ~1-2 seconds
- **Production Mode**: ~0.5-1 second
- **Native Mode**: ~0.01 seconds (if built with GraalVM)

### Request Latency
- **Simple Agent**: 1-3 seconds (LLM latency)
- **Orchestrator**: 1-3 seconds (single LLM call)
- **Database Query**: <50ms

### Scalability Considerations

**Current Limitations**:
- Single instance (no horizontal scaling)
- Synchronous LLM calls
- No request queuing
- No caching

**Future Improvements**:
- Add Redis for response caching
- Implement async LLM calls
- Add load balancer for multi-instance
- Implement request queuing (RabbitMQ/Kafka)

---

## Technology Stack Details

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Runtime | Java | 21 | Application runtime |
| Framework | Quarkus | 3.29.4 | REST framework |
| AI Library | LangChain4j | 0.19.0 | LLM integration |
| ORM | Hibernate Panache | - | Database access |
| Database | PostgreSQL | 17 | Data persistence |
| Frontend | D3.js | 7 | Graph visualization |
| Containerization | Docker | - | Deployment |
| Orchestration | Docker Compose | - | Multi-container management |
| LLM Provider | OpenAI | GPT-4o | AI capabilities |

---

## Extension Points

### Adding New Tools

1. Create tool class implementing LangChain4j tool interface
2. Register in `ToolFactory`
3. Add tool name to agent's `enabledTools`

### Adding New Agent Types

1. Create specialized service class
2. Add routing logic in `DynamicAgentService`
3. Update UI visualization (optional)

### Adding Conversation History

1. Create ConversationEntity JPA entity
2. Store messages in database
3. Load context in DynamicAgentService
4. Update UI to show history

### Multi-LLM Support

1. Add provider configuration to AgentEntity
2. Update DynamicAgentService to build different model types
3. Support Azure OpenAI, Anthropic, local models, etc.
