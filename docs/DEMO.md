# Demo Script - Agent Nexus

This script demonstrates the key features of the Agent Nexus system.

## Prerequisites

```bash
# Ensure Docker is running
docker --version

# Ensure containers are up
docker-compose ps

# You should see:
# - agent-nexus-app (running)
# - agent-nexus-postgres (healthy)
```

---

## Demo Flow

### Part 1: System Setup (2 minutes)

**1. Show the clean slate**
```bash
# List agents (should be empty initially)
curl http://localhost:8080/api/agents | jq
```

**2. Provision the multi-agent system**
```bash
# Run the setup script
./setup-multi-agent.sh

# Expected output:
# ✓ Created billing-agent
# ✓ Created technical-agent
# ✓ Created sales-agent
# ✓ Created support-orchestrator
```

**3. Open the UI**
```bash
# Open in browser
open http://localhost:8080

# Or navigate manually to: http://localhost:8080
```

**What to show:**
- Modern, elegant UI with dark aesthetic
- 4 agents displayed in force-directed graph
- Support orchestrator at center with 3 downstream agents
- Try clicking on nodes to see agent details
- Try dragging nodes to reposition them

---

### Part 2: Simple Agent Interaction (3 minutes)

**1. Talk to the Billing Agent directly**
```bash
curl -X POST http://localhost:8080/agents/billing-agent/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "What payment methods do you accept?"}' \
  | jq -r '.response'
```

**Expected behavior:**
- Billing agent responds with payment information
- Direct LLM interaction (no delegation)

**2. Talk to the Technical Agent**
```bash
curl -X POST http://localhost:8080/agents/technical-agent/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "How do I authenticate API requests?"}' \
  | jq -r '.response'
```

**Expected behavior:**
- Technical agent provides API authentication guidance
- Shows specialist knowledge

---

### Part 3: Orchestrator Magic (5 minutes)

**1. Send a billing question to the orchestrator**
```bash
curl -X POST http://localhost:8080/agents/support-orchestrator/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "I need help with my billing, what are your pricing tiers?"}' \
  | jq -r '.response'
```

**What's happening:**
- Orchestrator analyzes the question
- Identifies "billing" keyword
- Routes to billing-agent
- Returns billing-agent's response

**2. Send a technical question to the orchestrator**
```bash
curl -X POST http://localhost:8080/agents/support-orchestrator/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "How do I integrate the REST API?"}' \
  | jq -r '.response'
```

**What's happening:**
- Orchestrator identifies technical nature
- Routes to technical-agent
- Demonstrates intelligent delegation

**3. Send a sales question**
```bash
curl -X POST http://localhost:8080/agents/support-orchestrator/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "I want to learn about your enterprise features"}' \
  | jq -r '.response'
```

**What's happening:**
- Orchestrator routes to sales-agent
- Shows multi-agent coordination

---

### Part 4: Dynamic Agent Creation (4 minutes)

**1. Create a new specialist agent**
```bash
curl -X POST http://localhost:8080/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "security-agent",
    "systemPrompt": "You are a cybersecurity expert. Help users with security best practices, vulnerability assessments, and secure architecture design.",
    "modelName": "gpt-4o",
    "enabledTools": []
  }' | jq
```

**2. Refresh the UI to see the new agent**
```bash
# Click "Refresh" button in the UI
# New security-agent node should appear!
```

**3. Test the new agent**
```bash
curl -X POST http://localhost:8080/agents/security-agent/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "What are best practices for API key management?"}' \
  | jq -r '.response'
```

**4. Add it to the orchestrator's network**
```bash
# Note: This would require updating the orchestrator's downstreamPeers
# Future feature: PUT /agents/{id} endpoint for updates
```

---

### Part 5: UI Exploration (3 minutes)

**Demonstrate UI features:**

1. **Real-time refresh**
   - Toggle "Auto-refresh" on/off
   - Show 5-second updates

2. **Interactive graph**
   - Drag nodes around
   - Click nodes to view details
   - Hover to see enhanced shadows

3. **Agent details panel**
   - Click on "support-orchestrator"
   - Show: ID, prompt preview, tools, downstream peers
   - Click on "billing-agent"
   - Show: No downstream peers (leaf node)

4. **Legend**
   - Point out color coding:
     - Orchestrators (gray)
     - Specialists (warm gray)
     - Simple agents (cool gray)

---

### Part 6: Monitoring & Logs (2 minutes)

**1. View container logs**
```bash
# See application logs
docker-compose logs -f agent-nexus-app --tail=50

# Look for:
# - Agent creation events
# - LLM request/response logs
# - Delegation decisions
```

**2. Check database**
```bash
# Connect to PostgreSQL
docker exec -it agent-nexus-postgres psql -U quarkus -d a2a_gateway

# Query agents
SELECT id, name FROM agententity;

# Exit
\q
```

---

### Part 7: Testing Suite (2 minutes)

**Run the comprehensive test script**
```bash
./test-multi-agent.sh
```

**What it tests:**
1. Agent creation
2. Direct agent messaging
3. Orchestrator routing
4. Agent listing
5. Agent deletion

**Expected output:**
```
✓ Test 1: Create test agent
✓ Test 2: Send message to test agent
✓ Test 3: Test orchestrator with billing question
✓ Test 4: Test orchestrator with technical question
✓ Test 5: List all agents
✓ Test 6: Delete test agent

All tests passed! 🎉
```

---

## Advanced Demos

### Multi-level Orchestration

Create a hierarchy with multiple levels:

```bash
# Create domain orchestrators
curl -X POST http://localhost:8080/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "tech-orchestrator",
    "systemPrompt": "Route technical questions to appropriate specialists.",
    "enabledTools": ["PeerDelegationTool"],
    "downstreamPeers": ["technical-agent", "security-agent"]
  }'

# Create master orchestrator
curl -X POST http://localhost:8080/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "master-orchestrator",
    "systemPrompt": "Route to domain orchestrators: tech-orchestrator (technical/security), support-orchestrator (billing/sales).",
    "enabledTools": ["PeerDelegationTool"],
    "downstreamPeers": ["tech-orchestrator", "support-orchestrator"]
  }'
```

---

## Cleanup

```bash
# Stop containers
docker-compose down

# Remove volumes (clears database)
docker-compose down -v
```

---

## Key Talking Points

1. **Dynamic Nature**: Agents created on-the-fly, no code deployment needed
2. **Visualization**: Real-time graph shows system architecture
3. **Intelligence**: LLM-powered routing decisions
4. **Scalability**: Add agents without restart
5. **Persistence**: All configuration stored in PostgreSQL
6. **Modern Stack**: Quarkus (fast startup), LangChain4j (AI integration), D3.js (visualization)

---

## Audience Questions - Prepared Answers

**Q: How does the orchestrator decide which agent to use?**
A: The LLM analyzes the user message against the system prompt, which describes each downstream agent's specialty. It uses semantic understanding to route appropriately.

**Q: Can agents call each other recursively?**
A: Currently no cycle detection. This is a great enhancement opportunity - add graph traversal to prevent infinite loops.

**Q: What happens if the downstream agent is offline?**
A: Currently no health checks. Future: implement agent health monitoring and fallback strategies.

**Q: How do you handle conversation context?**
A: Each request is stateless currently. Future: add conversation history management per session.

**Q: Can you use models other than OpenAI?**
A: Yes! LangChain4j supports multiple providers (Anthropic, Azure OpenAI, local models). Just change the configuration.

**Q: Production readiness?**
A: This is a prototype/demo. For production: add auth, rate limiting, monitoring, conversation history, error handling, and testing.
