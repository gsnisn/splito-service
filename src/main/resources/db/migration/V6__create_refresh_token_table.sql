-- =========================
-- REFRESH TOKEN TABLE
-- =========================

CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGSERIAL PRIMARY KEY,

    -- token fields
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    device VARCHAR(128),

    -- BaseEntity fields
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    created_by BIGINT,
    updated_by BIGINT,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES splito_user(id) ON DELETE CASCADE
);

-- Helpful indexes for cleanup + lookups
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_token(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires ON refresh_token(expires_at);

-- Optional: if you frequently query "active" tokens
CREATE INDEX IF NOT EXISTS idx_refresh_token_active
    ON refresh_token(user_id)
    WHERE revoked_at IS NULL AND deleted_at IS NULL;
