#!/bin/bash

# Test script for the A2A Gateway

BASE_URL="http://localhost:8080"

echo "========================================="
echo "Testing A2A Gateway"
echo "========================================="
echo ""

# Step 1: Create an agent
echo "Step 1: Creating a new agent..."
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/agents" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-agent-01",
    "name": "Test Assistant",
    "systemPrompt": "You are a helpful AI assistant. Be concise and friendly.",
    "enabledTools": ["weather"],
    "downstreamPeers": []
  }')

echo "Response: HTTP 201 (expected)"
echo ""

# Step 2: Get agent card
echo "Step 2: Fetching agent card..."
CARD_RESPONSE=$(curl -s "$BASE_URL/agents/test-agent-01/.well-known/agent-card.json")
echo "Agent Card Response:"
echo "$CARD_RESPONSE" | jq '.' 2>/dev/null || echo "$CARD_RESPONSE"
echo ""

# Step 3: Send a message to the agent
echo "Step 3: Sending a test message to the agent..."
MESSAGE_RESPONSE=$(curl -s -X POST "$BASE_URL/agents/test-agent-01/message" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello! Can you tell me a short joke?"
  }')

echo "Message Response:"
echo "$MESSAGE_RESPONSE" | jq '.' 2>/dev/null || echo "$MESSAGE_RESPONSE"
echo ""

# Step 4: Test non-existent agent
echo "Step 4: Testing non-existent agent (should return 404)..."
NOT_FOUND=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE_URL/agents/non-existent/.well-known/agent-card.json")
echo "$NOT_FOUND"
echo ""

echo "========================================="
echo "Test Complete!"
echo "========================================="
