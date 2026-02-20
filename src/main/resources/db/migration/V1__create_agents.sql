CREATE TABLE IF NOT EXISTS agents (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    systemPrompt VARCHAR(4096),
    enabledTools JSONB NOT NULL DEFAULT '[]'::jsonb,
    downstreamPeers JSONB NOT NULL DEFAULT '[]'::jsonb
);
