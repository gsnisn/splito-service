-- 1) Mark a group as "direct" (auto-created 1:1 group)
ALTER TABLE splito_group
ADD COLUMN IF NOT EXISTS is_direct BOOLEAN NOT NULL DEFAULT FALSE;

-- 2) Ensure only one direct group per user pair using a pair table
CREATE TABLE IF NOT EXISTS splito_direct_group_pair (
    group_id   BIGINT PRIMARY KEY,
    user_low   BIGINT NOT NULL,
    user_high  BIGINT NOT NULL,

    CONSTRAINT fk_dgp_group
        FOREIGN KEY (group_id) REFERENCES splito_group(id) ON DELETE CASCADE,

    CONSTRAINT fk_dgp_user_low
        FOREIGN KEY (user_low) REFERENCES splito_user(id) ON DELETE CASCADE,

    CONSTRAINT fk_dgp_user_high
        FOREIGN KEY (user_high) REFERENCES splito_user(id) ON DELETE CASCADE,

    CONSTRAINT chk_user_order CHECK (user_low < user_high),
    CONSTRAINT uq_direct_pair UNIQUE (user_low, user_high)
);

CREATE INDEX IF NOT EXISTS idx_dgp_pair ON splito_direct_group_pair(user_low, user_high);

-- 3) Settlement ledger for settle-up payments in a group
CREATE TABLE IF NOT EXISTS settlement (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_settlement_group
        FOREIGN KEY (group_id) REFERENCES splito_group(id) ON DELETE CASCADE,

    CONSTRAINT fk_settlement_from_user
        FOREIGN KEY (from_user_id) REFERENCES splito_user(id) ON DELETE CASCADE,

    CONSTRAINT fk_settlement_to_user
        FOREIGN KEY (to_user_id) REFERENCES splito_user(id) ON DELETE CASCADE,

    CONSTRAINT chk_settlement_amount CHECK (amount > 0),
    CONSTRAINT chk_settlement_users CHECK (from_user_id <> to_user_id)
);

CREATE INDEX IF NOT EXISTS idx_settlement_group_id ON settlement(group_id);
CREATE INDEX IF NOT EXISTS idx_settlement_from_user_id ON settlement(from_user_id);
CREATE INDEX IF NOT EXISTS idx_settlement_to_user_id ON settlement(to_user_id);

