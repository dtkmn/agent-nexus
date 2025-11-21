#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "Setting up Multi-Agent System"
echo "========================================="
echo ""

# Create Billing Agent
echo "1. Creating Billing Agent..."
curl -s -X POST "$BASE_URL/agents" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "billing-agent",
    "name": "Billing Specialist",
    "systemPrompt": "You are a billing and payments specialist. You help customers with invoices, subscriptions, payment issues, refunds, and pricing questions. Be professional and precise with financial information. Keep responses concise.",
    "enabledTools": [],
    "downstreamPeers": []
  }' && echo "✓ Created"
echo ""

# Create Technical Agent
echo "2. Creating Technical Agent..."
curl -s -X POST "$BASE_URL/agents" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "technical-agent",
    "name": "Technical Support",
    "systemPrompt": "You are a technical support specialist. You help customers with bugs, technical issues, API integration, feature requests, and troubleshooting. Provide clear technical guidance. Keep responses concise.",
    "enabledTools": [],
    "downstreamPeers": []
  }' && echo "✓ Created"
echo ""

# Create Orchestrator Agent (knows about the other two)
echo "3. Creating Support Orchestrator..."
curl -s -X POST "$BASE_URL/agents" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "support-orchestrator",
    "name": "Support Orchestrator",
    "systemPrompt": "You are a customer support orchestrator. Your job is to understand customer queries and delegate them to the right specialist:\n\n- For billing, payments, invoices, subscriptions, refunds, or pricing questions → delegate to billing-agent\n- For bugs, technical issues, API questions, features, or troubleshooting → delegate to technical-agent\n- For general greetings or unclear questions → respond directly with a friendly message\n\nWhen delegating, explain briefly why you are routing them to that specialist.",
    "enabledTools": [],
    "downstreamPeers": ["billing-agent", "technical-agent"]
  }' && echo "✓ Created"
echo ""

echo "========================================="
echo "✅ Multi-Agent System Ready!"
echo "========================================="
echo ""
echo "Agents Created:"
echo "  • support-orchestrator (main entry point)"
echo "  • technical-agent (handles technical issues)"
echo "  • billing-agent (handles billing/payments)"
echo ""
echo "Try these test queries:"
echo ""
echo "# General greeting"
echo 'curl -X POST "'$BASE_URL'/agents/support-orchestrator/message" \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"message": "Hello, I need some help"}'"'"
echo ""
echo "# Billing question"
echo 'curl -X POST "'$BASE_URL'/agents/support-orchestrator/message" \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"message": "I was charged twice this month, can you help?"}'"'"
echo ""
echo "# Technical question"
echo 'curl -X POST "'$BASE_URL'/agents/support-orchestrator/message" \'
echo '  -H "Content-Type: application/json" \'
echo '  -d '"'"'{"message": "The API is returning 500 errors, how do I debug this?"}'"'"
echo ""
