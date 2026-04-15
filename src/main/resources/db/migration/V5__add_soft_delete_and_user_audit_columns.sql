-- =========================
-- SOFT DELETE + USER AUDIT
-- =========================

-- Users
ALTER TABLE splito_user
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- Groups
ALTER TABLE splito_group
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- Expenses
ALTER TABLE expense
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- Expense Split
ALTER TABLE expense_split
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- Settlement
ALTER TABLE settlement
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- ==================================================
-- ADD FK CONSTRAINTS SAFELY (idempotent)
-- ==================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_group_created_by'
    ) THEN
        ALTER TABLE splito_group
            ADD CONSTRAINT fk_group_created_by
            FOREIGN KEY (created_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_group_updated_by'
    ) THEN
        ALTER TABLE splito_group
            ADD CONSTRAINT fk_group_updated_by
            FOREIGN KEY (updated_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_expense_created_by'
    ) THEN
        ALTER TABLE expense
            ADD CONSTRAINT fk_expense_created_by
            FOREIGN KEY (created_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_expense_updated_by'
    ) THEN
        ALTER TABLE expense
            ADD CONSTRAINT fk_expense_updated_by
            FOREIGN KEY (updated_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_expense_split_created_by'
    ) THEN
        ALTER TABLE expense_split
            ADD CONSTRAINT fk_expense_split_created_by
            FOREIGN KEY (created_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_expense_split_updated_by'
    ) THEN
        ALTER TABLE expense_split
            ADD CONSTRAINT fk_expense_split_updated_by
            FOREIGN KEY (updated_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_settlement_created_by'
    ) THEN
        ALTER TABLE settlement
            ADD CONSTRAINT fk_settlement_created_by
            FOREIGN KEY (created_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_settlement_updated_by'
    ) THEN
        ALTER TABLE settlement
            ADD CONSTRAINT fk_settlement_updated_by
            FOREIGN KEY (updated_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_created_by'
    ) THEN
        ALTER TABLE splito_user
            ADD CONSTRAINT fk_user_created_by
            FOREIGN KEY (created_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_updated_by'
    ) THEN
        ALTER TABLE splito_user
            ADD CONSTRAINT fk_user_updated_by
            FOREIGN KEY (updated_by) REFERENCES splito_user(id) ON DELETE SET NULL;
    END IF;
END $$;

-- =========================
-- INDEXES FOR ANALYTICS
-- =========================

CREATE INDEX IF NOT EXISTS idx_user_created_at ON splito_user(created_at);
CREATE INDEX IF NOT EXISTS idx_group_created_at ON splito_group(created_at);

CREATE INDEX IF NOT EXISTS idx_expense_created_at ON expense(created_at);
CREATE INDEX IF NOT EXISTS idx_expense_group_created_at ON expense(group_id, created_at);

CREATE INDEX IF NOT EXISTS idx_settlement_created_at ON settlement(created_at);
CREATE INDEX IF NOT EXISTS idx_settlement_group_created_at ON settlement(group_id, created_at);

CREATE INDEX IF NOT EXISTS idx_expense_split_created_at ON expense_split(created_at);
CREATE INDEX IF NOT EXISTS idx_expense_split_expense_created_at ON expense_split(expense_id, created_at);

-- Partial indexes (soft delete friendly)
CREATE INDEX IF NOT EXISTS idx_expense_not_deleted_created_at
    ON expense(created_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_settlement_not_deleted_created_at
    ON settlement(created_at)
    WHERE deleted_at IS NULL;
