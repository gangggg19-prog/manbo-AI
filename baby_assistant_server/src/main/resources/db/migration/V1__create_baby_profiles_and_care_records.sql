CREATE TABLE baby_profiles (
    id UUID PRIMARY KEY,
    display_name VARCHAR(80) NOT NULL,
    birth_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE care_records (
    id UUID PRIMARY KEY,
    baby_id UUID NOT NULL REFERENCES baby_profiles(id) ON DELETE CASCADE,
    record_type VARCHAR(20) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    amount_ml INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT care_records_type_check
        CHECK (record_type IN ('FEEDING', 'DIAPER')),
    CONSTRAINT care_records_amount_check
        CHECK (
            (record_type = 'FEEDING' AND amount_ml IS NOT NULL AND amount_ml > 0)
            OR (record_type = 'DIAPER' AND amount_ml IS NULL)
        )
);

CREATE INDEX care_records_baby_time_idx
    ON care_records (baby_id, recorded_at DESC);