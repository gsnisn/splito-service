-- Users
CREATE TABLE IF NOT EXISTS splito_user (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255)
);

-- Groups
CREATE TABLE IF NOT EXISTS splito_group (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Group members (Many-to-Many)
CREATE TABLE IF NOT EXISTS splito_group_members (
    splito_group_id BIGINT NOT NULL,
    members_id BIGINT NOT NULL,
    PRIMARY KEY (splito_group_id, members_id),
    CONSTRAINT fk_group_members_group
        FOREIGN KEY (splito_group_id) REFERENCES splito_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_user
        FOREIGN KEY (members_id) REFERENCES splito_user(id) ON DELETE CASCADE
);

-- Expenses
CREATE TABLE IF NOT EXISTS expense (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255),
    amount DOUBLE PRECISION NOT NULL,
    paid_by_id BIGINT,
    group_id BIGINT,
    CONSTRAINT fk_expense_paidby FOREIGN KEY (paid_by_id) REFERENCES splito_user(id),
    CONSTRAINT fk_expense_group FOREIGN KEY (group_id) REFERENCES splito_group(id) ON DELETE CASCADE
);

-- Expense splitBetween (Many-to-Many)
CREATE TABLE IF NOT EXISTS expense_split_between (
    expense_id BIGINT NOT NULL,
    split_between_id BIGINT NOT NULL,
    PRIMARY KEY (expense_id, split_between_id),
    CONSTRAINT fk_esb_expense FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE,
    CONSTRAINT fk_esb_user FOREIGN KEY (split_between_id) REFERENCES splito_user(id) ON DELETE CASCADE
);

-- ExpenseSplit (for unequal/exact splits)
CREATE TABLE IF NOT EXISTS expense_split (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_expensesplit_expense FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE,
    CONSTRAINT fk_expensesplit_user FOREIGN KEY (user_id) REFERENCES splito_user(id) ON DELETE CASCADE
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_expense_group_id ON expense(group_id);
CREATE INDEX IF NOT EXISTS idx_expense_paid_by_id ON expense(paid_by_id);
CREATE INDEX IF NOT EXISTS idx_expensesplit_expense_id ON expense_split(expense_id);
