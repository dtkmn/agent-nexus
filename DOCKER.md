# A2A Gateway - Docker Setup

## Quick Start

```bash
# Start all services (PostgreSQL + A2A Gateway)
./docker-start.sh

# Setup the multi-agent system
./setup-multi-agent.sh

# Test the orchestrator
./test-multi-agent.sh
```

## Services

- **A2A Gateway API**: http://localhost:8080
- **PostgreSQL**: localhost:5432 (quarkus/quarkus)

## Docker Commands

```bash
# View logs
docker-compose logs -f agent-nexus

# Stop services
docker-compose down

# Restart services
docker-compose restart

# Rebuild after code changes
docker-compose up --build -d
```

## Architecture

The system uses a **multi-agent orchestration** pattern:

1. **Support Orchestrator** - Main entry point, routes queries to specialists
2. **Billing Agent** - Handles billing, payments, refunds
3. **Technical Agent** - Handles API issues, troubleshooting, features

## Example Requests

### Direct to orchestrator (will delegate automatically)
```bash
curl -X POST "http://localhost:8080/agents/support-orchestrator/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "I was charged twice this month"}'
```

### Direct to specialist agent
```bash
curl -X POST "http://localhost:8080/agents/billing-agent/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "Can you check my recent charges?"}'
```

### Get agent metadata
```bash
curl http://localhost:8080/agents/support-orchestrator/.well-known/agent-card.json | jq .
```

## Configuration

- OpenAI API key is configured in `src/main/resources/application.properties`
- Database connection configured via environment variables in `docker-compose.yml`
- To use a different OpenAI key, update the properties file and rebuild

## Development

The Docker setup uses a multi-stage build:
1. **Build stage**: Maven builds the Quarkus app
2. **Runtime stage**: Minimal JRE image runs the application

This ensures fast builds with layer caching and small production images.
