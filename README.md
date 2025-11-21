# 🤖 Agent Nexus

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Quarkus](https://img.shields.io/badge/Quarkus-3.29.4-blue?logo=quarkus)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.19.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

*Central hub for multi-agent AI orchestration with real-time visualization*

[Features](#-features) • [Quick Start](#-quick-start) • [Architecture](#-architecture) • [Demo](#-demo) • [API](#-api-reference)

</div>

---

## 🌟 Features

- **🔄 Dynamic Agent Creation** - Create AI agents on-the-fly with custom prompts and tools
- **🎭 Multi-Agent Orchestration** - Build orchestrator agents that delegate to specialized sub-agents
- **🧠 LLM Integration** - Powered by OpenAI GPT-4o via LangChain4j
- **📊 Real-Time Visualization** - Interactive D3.js force-directed graph showing agent relationships
- **🗄️ Persistent Storage** - PostgreSQL for reliable agent configuration management
- **🐳 Docker Ready** - Full containerization with Docker Compose
- **🎨 Modern UI** - Elegant minimalist design with glassmorphism effects

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- OpenAI API key
- Java 21+ (for local development only)
- Maven 3.9+ (for local development only)

### Running with Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/agent-nexus.git
   cd agent-nexus
   ```

2. **Set up environment variables**
   ```bash
   echo "OPENAI_API_KEY=your-api-key-here" > .env
   ```

3. **Start the application**
   ```bash
   docker-compose up -d
   ```

4. **Provision demo agents** (optional)
   ```bash
   ./setup-multi-agent.sh
   ```

5. **Open the UI**
   - Navigate to: http://localhost:8080
   - View the interactive agent graph!

### Testing the System

```bash
# Run comprehensive test suite
./test-multi-agent.sh

# Test orchestrator delegation
curl -X POST http://localhost:8080/agents/support-orchestrator/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "I need help with my billing"}'
```

## 🏗️ Architecture

### System Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────────────────────────────┐
│     A2A Gateway (Quarkus)           │
│  ┌─────────────┐  ┌──────────────┐ │
│  │ Gateway API │  │  Web UI      │ │
│  │  (REST)     │  │  (D3.js)     │ │
│  └──────┬──────┘  └──────┬───────┘ │
│         │                │          │
│  ┌──────▼────────────────▼───────┐ │
│  │    Agent Service               │ │
│  │  - Dynamic routing             │ │
│  │  - Orchestration               │ │
│  │  - Tool execution              │ │
│  └──────┬─────────────────────────┘ │
└─────────┼─────────────────────────┬─┘
          │                         │
    ┌─────▼──────┐            ┌────▼─────┐
    │ PostgreSQL │            │ OpenAI   │
    │   (Agents) │            │  GPT-4o  │
    └────────────┘            └──────────┘
```

### Key Components

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Gateway API** | Quarkus REST | Agent CRUD operations, message routing |
| **Agent Service** | LangChain4j | LLM integration, tool execution |
| **Web UI** | D3.js, HTML/CSS | Real-time agent graph visualization |
| **Database** | PostgreSQL 17 | Agent configuration persistence |
| **LLM** | OpenAI GPT-4o | Natural language understanding |

### Agent Types

1. **Simple Agents** - Direct LLM interaction without tools
2. **Specialist Agents** - Equipped with specific tools (e.g., `PeerDelegationTool`)
3. **Orchestrator Agents** - Route requests to downstream specialist agents

## 🎭 Demo Scenarios

### Scenario 1: Multi-Agent Support System

The included demo creates a customer support ecosystem:

```
                ┌──────────────────┐
                │  Support         │
                │  Orchestrator    │
                └────────┬─────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
  ┌──────────┐    ┌──────────┐    ┌──────────┐
  │ Billing  │    │Technical │    │  Sales   │
  │  Agent   │    │  Agent   │    │  Agent   │
  └──────────┘    └──────────┘    └──────────┘
```

**Try it:**
```bash
# Billing question (routes to billing-agent)
curl -X POST http://localhost:8080/agents/support-orchestrator/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "What are your pricing plans?"}'

# Technical question (routes to technical-agent)
curl -X POST http://localhost:8080/agents/support-orchestrator/message \
  -H "Content-Type: application/json" \
  -d '{"userMessage": "How do I integrate the API?"}'
```

## 📡 API Reference

### Create Agent

```bash
POST /agents
Content-Type: application/json

{
  "name": "my-agent",
  "systemPrompt": "You are a helpful assistant...",
  "modelName": "gpt-4o",
  "enabledTools": ["PeerDelegationTool"],
  "downstreamPeers": ["agent-1", "agent-2"]
}
```

### Send Message to Agent

```bash
POST /agents/{agentId}/message
Content-Type: application/json

{
  "userMessage": "Hello, can you help me?"
}
```

### List All Agents

```bash
GET /api/agents
```

**Response:**
```json
[
  {
    "id": "support-orchestrator",
    "name": "Support Orchestrator",
    "promptPreview": "You are a support routing agent...",
    "enabledTools": ["PeerDelegationTool"],
    "downstreamPeers": ["billing-agent", "technical-agent"]
  }
]
```

### Get Agent Details

```bash
GET /agents/{agentId}
```

## 🛠️ Development

### Local Development (without Docker)

```bash
# Start PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_USER=quarkus \
  -e POSTGRES_PASSWORD=quarkus \
  -e POSTGRES_DB=a2a_gateway \
  -p 5432:5432 postgres:17

# Run in dev mode with hot reload
./mvnw quarkus:dev
```

### Project Structure

```
agent-nexus/
├── src/main/java/org/acme/a2a/
│   ├── config/          # Database configuration
│   ├── entity/          # JPA entities
│   ├── model/           # DTOs and models
│   ├── resource/        # REST endpoints
│   └── service/         # Business logic
├── src/main/resources/
│   ├── META-INF/resources/
│   │   └── index.html   # Interactive UI
│   └── application.properties
├── docker-compose.yml
├── Dockerfile
└── setup-multi-agent.sh # Demo provisioning
```

### Running Tests

```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Test multi-agent system
./test-multi-agent.sh
```

## 🎨 UI Features

- **Force-Directed Graph**: Visualizes agent relationships with physics simulation
- **Interactive Nodes**: Click to view agent details, drag to reposition
- **Real-Time Updates**: Auto-refresh every 5 seconds (toggleable)
- **Agent Types**: Color-coded by capability (orchestrator/specialist/simple)
- **Peer Badges**: Shows delegation relationships at a glance

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key | *Required* |
| `QUARKUS_DATASOURCE_JDBC_URL` | PostgreSQL connection | `jdbc:postgresql://postgres:5432/a2a_gateway` |
| `QUARKUS_DATASOURCE_USERNAME` | Database user | `quarkus` |
| `QUARKUS_DATASOURCE_PASSWORD` | Database password | `quarkus` |

### Customizing Agents

Edit `setup-multi-agent.sh` to create your own agent hierarchy:

```bash
# Create a custom agent
curl -X POST http://localhost:8080/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "code-reviewer",
    "systemPrompt": "You are an expert code reviewer...",
    "modelName": "gpt-4o",
    "enabledTools": []
  }'
```

## 🤝 Contributing

Contributions welcome! Areas for improvement:

- [ ] Add WebSocket support for real-time agent updates
- [ ] Implement more sophisticated tool execution
- [ ] Add agent conversation history
- [ ] Create agent templates/presets
- [ ] Add metrics and monitoring
- [ ] Support for other LLM providers (Anthropic, Azure OpenAI)

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

- Built with [Quarkus](https://quarkus.io/) - Supersonic Subatomic Java
- AI powered by [LangChain4j](https://github.com/langchain4j/langchain4j)
- Visualization with [D3.js](https://d3js.org/)
- Inspired by the Agent-to-Agent communication paradigm

---

<div align="center">

**Made with ❤️ by [Your Name]**

⭐ Star this repo if you find it useful!

</div>
