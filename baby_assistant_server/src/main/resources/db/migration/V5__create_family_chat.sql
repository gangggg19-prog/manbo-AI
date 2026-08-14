CREATE TABLE family_chat_rooms (
    id UUID PRIMARY KEY,
    baby_id UUID NOT NULL UNIQUE REFERENCES baby_profiles(id) ON DELETE CASCADE,
    title VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE family_chat_messages (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES family_chat_rooms(id) ON DELETE CASCADE,
    sender_name VARCHAR(48) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_family_chat_messages_room_sent
    ON family_chat_messages (room_id, sent_at ASC);