#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========================================="
echo "Testing Multi-Agent Orchestration"
echo "========================================="
echo ""

# Test 1: General greeting (Orchestrator handles directly)
echo "Test 1: General Greeting"
echo "----------------------------------------"
echo "Query: 'Hello, I need some help'"
echo ""
RESPONSE=$(curl -s -X POST "$BASE_URL/agents/support-orchestrator/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, I need some help"}')
echo "Response:"
echo "$RESPONSE" | jq -r '.reply' 2>/dev/null || echo "$RESPONSE"
echo ""
echo "========================================"
echo ""

# Test 2: Billing question (Should delegate to billing-agent)
echo "Test 2: Billing Issue"
echo "----------------------------------------"
echo "Query: 'I was charged twice this month'"
echo ""
RESPONSE=$(curl -s -X POST "$BASE_URL/agents/support-orchestrator/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "I was charged twice this month, can you help me get a refund?"}')
echo "Response:"
echo "$RESPONSE" | jq -r '.reply' 2>/dev/null || echo "$RESPONSE"
echo ""
echo "========================================"
echo ""

# Test 3: Technical question (Should delegate to technical-agent)
echo "Test 3: Technical Issue"
echo "----------------------------------------"
echo "Query: 'API returning 500 errors'"
echo ""
RESPONSE=$(curl -s -X POST "$BASE_URL/agents/support-orchestrator/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "The API is returning 500 errors when I try to create a user. How do I debug this?"}')
echo "Response:"
echo "$RESPONSE" | jq -r '.reply' 2>/dev/null || echo "$RESPONSE"
echo ""
echo "========================================"
echo ""

# Test 4: Another billing question
echo "Test 4: Subscription Question"
echo "----------------------------------------"
echo "Query: 'How much does the premium plan cost?'"
echo ""
RESPONSE=$(curl -s -X POST "$BASE_URL/agents/support-orchestrator/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "How much does the premium plan cost per month?"}')
echo "Response:"
echo "$RESPONSE" | jq -r '.reply' 2>/dev/null || echo "$RESPONSE"
echo ""
echo "========================================"
echo ""

# Test 5: Another technical question
echo "Test 5: Feature Request"
echo "----------------------------------------"
echo "Query: 'Does the API support webhooks?'"
echo ""
RESPONSE=$(curl -s -X POST "$BASE_URL/agents/support-orchestrator/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "Does your API support webhooks for real-time notifications?"}')
echo "Response:"
echo "$RESPONSE" | jq -r '.reply' 2>/dev/null || echo "$RESPONSE"
echo ""
echo "========================================"
echo ""

echo "✅ Test Complete!"
echo ""
echo "Note: The orchestrator should:"
echo "  • Handle greetings directly"
echo "  • Delegate billing questions to billing-agent"
echo "  • Delegate technical questions to technical-agent"
echo ""
