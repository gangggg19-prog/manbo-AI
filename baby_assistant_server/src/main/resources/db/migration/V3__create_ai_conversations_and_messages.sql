CREATE TABLE ai_conversations (
    id UUID PRIMARY KEY,
    baby_id UUID NOT NULL REFERENCES baby_profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ai_conversations_baby_created
    ON ai_conversations (baby_id, created_at DESC);

CREATE TABLE ai_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    message_role VARCHAR(16) NOT NULL CHECK (message_role IN ('USER', 'ASSISTANT')),
    content TEXT NOT NULL,
    source VARCHAR(48),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ai_messages_conversation_created
    ON ai_messages (conversation_id, created_at ASC);