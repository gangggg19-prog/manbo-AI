-- 睡眠与喂养、尿布的字段结构不同：它需要开始与结束两个时间点，独立建表更利于跨天统计。
CREATE TABLE sleep_sessions (
    id UUID PRIMARY KEY,
    baby_id UUID NOT NULL REFERENCES baby_profiles(id) ON DELETE CASCADE,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sleep_sessions_time_check
        CHECK (ended_at IS NULL OR ended_at > started_at)
);

-- 查询某天的睡眠时，需要按宝宝和开始时间快速定位。
CREATE INDEX sleep_sessions_baby_started_idx
    ON sleep_sessions (baby_id, started_at DESC);

-- 同一个宝宝同一时刻只能有一段进行中的睡眠，数据库层也会兜底并发重复点击。
CREATE UNIQUE INDEX sleep_sessions_one_active_per_baby_idx
    ON sleep_sessions (baby_id)
    WHERE ended_at IS NULL;