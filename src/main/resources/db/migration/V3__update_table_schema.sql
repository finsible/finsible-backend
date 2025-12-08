ALTER TABLE transactions
    ADD COLUMN transaction_date BIGINT NOT NULL default 1735689600; -- Default to Jan 1, 2025

-- Rename existing 'account' column to 'to_account_id'
ALTER TABLE transactions
    RENAME COLUMN account_id TO to_account_id;

-- Add 'from_account_id' column for transfer transactions
ALTER TABLE transactions
    ADD COLUMN from_account_id BIGINT,
    ADD CONSTRAINT fk_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id);

ALTER TABLE categories
DROP CONSTRAINT categories_type_check,
ADD CONSTRAINT categories_type_check
    CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER'));

ALTER TABLE account_groups ADD COLUMN color VARCHAR(25);