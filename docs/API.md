# API Documentation - Agent Nexus

## Base URL

```
http://localhost:8080
```

## Endpoints

### 1. Create Agent

Create a new AI agent with custom configuration.

**Endpoint:** `POST /agents`

**Request Body:**
```json
{
  "name": "agent-name",
  "systemPrompt": "You are a helpful assistant specialized in...",
  "modelName": "gpt-4o",
  "enabledTools": ["PeerDelegationTool"],
  "downstreamPeers": ["agent-id-1", "agent-id-2"]
}
```

**Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Unique identifier for the agent |
| `systemPrompt` | string | Yes | System prompt defining agent behavior |
| `modelName` | string | No | LLM model to use (default: gpt-4o) |
| `enabledTools` | array | No | List of tools agent can use |
| `downstreamPeers` | array | No | List of agent IDs this agent can delegate to |

**Response:**
```json
{
  "id": "agent-name",
  "name": "agent-name",
  "systemPrompt": "You are a helpful assistant...",
  "modelName": "gpt-4o",
  "enabledTools": ["PeerDelegationTool"],
  "downstreamPeers": ["agent-id-1", "agent-id-2"]
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "sales-bot",
    "systemPrompt": "You are a friendly sales assistant.",
    "modelName": "gpt-4o",
    "enabledTools": []
  }'
```

---

### 2. Get Agent

Retrieve details of a specific agent.

**Endpoint:** `GET /agents/{agentId}`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `agentId` | string | Unique agent identifier |

**Response:**
```json
{
  "id": "sales-bot",
  "name": "sales-bot",
  "systemPrompt": "You are a friendly sales assistant.",
  "modelName": "gpt-4o",
  "enabledTools": [],
  "downstreamPeers": []
}
```

**Example:**
```bash
curl http://localhost:8080/agents/sales-bot
```

---

### 3. List All Agents

Get a list of all agents with their relationships.

**Endpoint:** `GET /api/agents`

**Response:**
```json
[
  {
    "id": "support-orchestrator",
    "name": "Support Orchestrator",
    "promptPreview": "You are a support routing agent that...",
    "enabledTools": ["PeerDelegationTool"],
    "downstreamPeers": ["billing-agent", "technical-agent", "sales-agent"]
  },
  {
    "id": "billing-agent",
    "name": "Billing Agent",
    "promptPreview": "You are a billing specialist...",
    "enabledTools": [],
    "downstreamPeers": []
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/agents
```

---

### 4. Send Message to Agent

Send a message to an agent and get a response.

**Endpoint:** `POST /agents/{agentId}/message`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `agentId` | string | Target agent identifier |

**Request Body:**
```json
{
  "userMessage": "What are your pricing plans?"
}
```

**Response:**
```json
{
  "response": "We offer three pricing tiers: Basic ($10/month), Pro ($50/month), and Enterprise (custom pricing)..."
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/agents/billing-agent/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "What are your pricing plans?"}'
```

---

### 5. Delete Agent

Delete an agent by ID.

**Endpoint:** `DELETE /agents/{agentId}`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `agentId` | string | Agent to delete |

**Response:** `204 No Content`

**Example:**
```bash
curl -X DELETE http://localhost:8080/agents/sales-bot
```

---

## Agent Tools

### PeerDelegationTool

Allows an agent to delegate requests to downstream agents.

**Usage:** Add `"PeerDelegationTool"` to `enabledTools` array when creating an orchestrator agent.

**Requirements:**
- Agent must have `downstreamPeers` configured
- Downstream agents must exist

**Example Orchestrator:**
```json
{
  "name": "orchestrator",
  "systemPrompt": "Route requests to appropriate specialists. Billing -> billing-agent, Technical -> technical-agent",
  "enabledTools": ["PeerDelegationTool"],
  "downstreamPeers": ["billing-agent", "technical-agent"]
}
```

---

## Error Responses

### 404 Not Found
```json
{
  "error": "Agent not found",
  "agentId": "non-existent-agent"
}
```

### 400 Bad Request
```json
{
  "error": "Invalid request",
  "message": "systemPrompt is required"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "Failed to process LLM request"
}
```

---

## Rate Limits

Currently no rate limits are enforced. Consider implementing rate limiting for production deployments.

## Authentication

Currently no authentication required. Add authentication middleware for production use.

## Versioning

API versioning not yet implemented. Future versions will use URL path versioning (e.g., `/v1/agents`).
