ALTER TABLE expense
    ALTER COLUMN amount TYPE NUMERIC(12,2)
    USING ROUND(amount::numeric, 2);

ALTER TABLE expense_split
    ALTER COLUMN amount TYPE NUMERIC(12,2)
    USING ROUND(amount::numeric, 2);
