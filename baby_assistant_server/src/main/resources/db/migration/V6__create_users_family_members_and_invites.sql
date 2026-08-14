CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    display_name VARCHAR(48) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uk_app_users_username_lower
    ON app_users (LOWER(username));

ALTER TABLE family_chat_messages
    ADD COLUMN sender_user_id UUID REFERENCES app_users(id) ON DELETE SET NULL;

CREATE INDEX idx_family_chat_messages_sender
    ON family_chat_messages (sender_user_id, sent_at DESC);

CREATE TABLE family_members (
    id UUID PRIMARY KEY,
    baby_id UUID NOT NULL REFERENCES baby_profiles(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    member_role VARCHAR(16) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_family_members_baby_user UNIQUE (baby_id, user_id),
    CONSTRAINT ck_family_members_role CHECK (member_role IN ('OWNER', 'MEMBER'))
);

CREATE UNIQUE INDEX uk_family_members_single_owner
    ON family_members (baby_id)
    WHERE member_role = 'OWNER';

CREATE INDEX idx_family_members_user
    ON family_members (user_id, joined_at ASC);

CREATE TABLE family_invites (
    id UUID PRIMARY KEY,
    baby_id UUID NOT NULL REFERENCES baby_profiles(id) ON DELETE CASCADE,
    invite_code VARCHAR(12) NOT NULL UNIQUE,
    created_by UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_by UUID REFERENCES app_users(id) ON DELETE SET NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_family_invites_baby
    ON family_invites (baby_id, created_at DESC);