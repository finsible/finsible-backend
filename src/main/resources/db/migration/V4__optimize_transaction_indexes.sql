-- =====================================================
-- V4: Optimize indexes for transaction queries
-- =====================================================

-- Remove unused indexes
DROP INDEX IF EXISTS idx_transactions_paid_by;      -- Not used in queries
DROP INDEX IF EXISTS idx_transactions_space;        -- Not filtered, only fetched
DROP INDEX IF EXISTS idx_transactions_created_at;   -- We filter on transaction_date, not created_at
DROP INDEX IF EXISTS idx_transactions_user_type;
DROP index if exists idx_transactions_created_by;

-- Rename old index (account_id was renamed to to_account_id in V3)
DROP INDEX IF EXISTS idx_transactions_account;
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);

-- Add missing indexes for query filters

-- from_account_id - Used in account filter (added in V3)
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);

-- transaction_date - Used for date range filter and ORDER BY
CREATE INDEX idx_transactions_date ON transactions(transaction_date);

-- total_amount - Used for amount range filter
CREATE INDEX idx_transactions_amount ON transactions(total_amount);

-- COMPOSITE INDEX - Most important for pagination performance!
-- Covers: WHERE created_by = ? ORDER BY transaction_date DESC LIMIT ?
-- This allows the database to use index for both filtering AND sorting
CREATE INDEX idx_transactions_user_date ON transactions(created_by, transaction_date DESC);

-- COMPOSITE INDEX for daily summary aggregation
-- Covers: WHERE created_by = ? AND type IN ('INCOME','EXPENSE') GROUP BY transaction_date
CREATE INDEX idx_transactions_user_type_date ON transactions(created_by, type, transaction_date);
