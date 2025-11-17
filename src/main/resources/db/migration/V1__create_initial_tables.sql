-- supported languages for user
CREATE TABLE supported_languages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL ,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- supported currencies by application
CREATE TABLE supported_currencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(3) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    picture VARCHAR(500),
    last_logged_in TIMESTAMPTZ NOT NULL,
    account_created TIMESTAMPTZ NOT NULL,
    categories_edited BOOLEAN NOT NULL DEFAULT FALSE,
    default_language VARCHAR(10) NOT NULL,
    default_currency VARCHAR(3) NOT NULL,

    CONSTRAINT fk_user_language foreign key (default_language) REFERENCES supported_languages(code) ON DELETE SET NULL,
    CONSTRAINT fk_user_default_currency foreign key (default_currency) REFERENCES supported_currencies(code) ON DELETE RESTRICT
);

-- Add index for performance
CREATE INDEX idx_users_email ON Users(email);

CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE account_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(255),
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT,
    user_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(255),

    CONSTRAINT fk_account_group_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_user_account_group_name UNIQUE (user_id, name)
);

CREATE INDEX idx_system_default_account_groups ON account_groups(is_system_default);
CREATE INDEX idx_account_groups_user_id ON account_groups(user_id);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) CHECK (type IN ('INCOME', 'EXPENSE')),
    name VARCHAR(255) NOT NULL,
    icon VARCHAR(255),
    user_id VARCHAR(255),
    is_sub_category BOOLEAN NOT NULL DEFAULT false,
    parent_category_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by VARCHAR(255),

    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id) REFERENCES categories(id),

    CONSTRAINT uk_user_category_name UNIQUE (user_id, name, type)
);

-- Add index for performance
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255),
    account_group_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(255),
    balance DECIMAL(19, 4),
    currency_code VARCHAR(3) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_system_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_account_user FOREIGN KEY (user_id)
     REFERENCES users(id),
    CONSTRAINT fk_account_group FOREIGN KEY (account_group_id)
     REFERENCES account_groups (id),
    CONSTRAINT fk_account_currency FOREIGN KEY (currency_code)
     REFERENCES supported_currencies (code),

    CONSTRAINT uk_user_account_name UNIQUE (user_id, name)
);

-- Indexes for foreign keys and common queries
CREATE INDEX idx_account_user_id ON accounts(user_id);
CREATE INDEX idx_account_group_id ON accounts(account_group_id);

-- Credit Card Configuration (important for tracking)
CREATE TABLE credit_card_details (
    account_id BIGINT PRIMARY KEY,
    credit_limit DECIMAL(19, 4) NOT NULL,
    available_credit DECIMAL(19, 4) NOT NULL,
    billing_date INT NOT NULL CHECK (billing_date BETWEEN 1 AND 31),
    due_date INT NOT NULL CHECK (due_date BETWEEN 1 AND 31),
    auto_pay_enabled BOOLEAN DEFAULT FALSE,
    auto_pay_from_account_id BIGINT,  -- Reference to bank account
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (auto_pay_from_account_id) REFERENCES accounts(id)
);

-- Debit Card Configuration
CREATE TABLE debit_card_details (
    account_id BIGINT PRIMARY KEY,
    linked_bank_account_id BIGINT NOT NULL,  -- Which bank account?
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (linked_bank_account_id) REFERENCES accounts(id)
);
-- Loan Configuration
CREATE TABLE loan_details (
    account_id BIGINT PRIMARY KEY,
    loan_type VARCHAR(50),              -- HOME_LOAN, PERSONAL_LOAN, CAR_LOAN
    principal_amount DECIMAL(19, 4) NOT NULL,
    interest_rate DECIMAL(5,2),
    emi_amount DECIMAL(19, 4),
    emi_date INT CHECK (emi_date BETWEEN 1 AND 31),
    tenure_months INT,
    start_date DATE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE spaces (
    id BIGSERIAL PRIMARY KEY,
    space_name VARCHAR(255) NOT NULL,
    description TEXT,
    icon VARCHAR(255),
    total_spends DECIMAL(19, 4),
    currency_code VARCHAR(3) NOT NULL,
    is_active BOOLEAN,
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_spaces_currency FOREIGN KEY (currency_code)
        REFERENCES supported_currencies(code)
);

CREATE TABLE space_user_details (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGINT NOT NULL,
    user_id VARCHAR(255),
    user_email VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    user_spends DECIMAL(19, 4),
    role VARCHAR(50),
    is_active BOOLEAN,
    currency_code VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_space_user_details_space
        FOREIGN KEY (space_id)
        REFERENCES spaces(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_space_user_details_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_space_user_details_currency
        FOREIGN KEY (currency_code)
        REFERENCES supported_currencies(code)
        ON DELETE RESTRICT,

    -- Prevent duplicate user-space combinations
    CONSTRAINT uk_space_user UNIQUE (space_id, user_email)
);

-- Performance indexes
CREATE INDEX idx_space_user_details_space_id ON space_user_details(space_id);
CREATE INDEX idx_space_user_details_user_id ON space_user_details(user_email);
CREATE INDEX idx_space_user_details_is_active ON space_user_details(is_active);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19, 4),
    user_share DECIMAL(19, 4),
    is_split BOOLEAN DEFAULT FALSE,
    paid_by_id VARCHAR(255),
    category_id BIGINT,
    account_id BIGINT,
    description TEXT,
    space_id BIGINT,
    currency_code VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_transactions_paid_by
        FOREIGN KEY (paid_by_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_transactions_space
        FOREIGN KEY (space_id)
        REFERENCES spaces(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_transactions_currency
        FOREIGN KEY (currency_code)
        REFERENCES supported_currencies(code)
        ON DELETE RESTRICT,

    CONSTRAINT chk_transaction_type
        CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER'))
);

-- Performance indexes
CREATE INDEX idx_transactions_paid_by ON transactions(paid_by_id);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_space ON transactions(space_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

CREATE TABLE split_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT,
    user_id VARCHAR(255),
    user_email VARCHAR(255) NOT NULL,
    user_name VARCHAR(255),
    space_id BIGINT,
    share_amount DECIMAL(19, 4),
    is_settled BOOLEAN DEFAULT FALSE,
    settlement_date TIMESTAMPTZ,
    is_active BOOLEAN,
    version VARCHAR(255),
    superseded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_split_transactions_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_split_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_split_transactions_space
        FOREIGN KEY (space_id)
        REFERENCES spaces(id)
        ON DELETE RESTRICT
);

-- Performance indexes
CREATE INDEX idx_split_transactions_transaction ON split_transactions(transaction_id);
CREATE INDEX idx_split_transactions_user ON split_transactions(user_email);
CREATE INDEX idx_split_transactions_space ON split_transactions(space_id);
CREATE INDEX idx_split_transactions_is_settled ON split_transactions(is_settled);
CREATE INDEX idx_split_transactions_is_active ON split_transactions(is_active);

CREATE TABLE split_balances (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT,
    user1_id VARCHAR(255),
    user2_id VARCHAR(255),
    user1_email VARCHAR(255) NOT NULL,
    user2_email VARCHAR(255) NOT NULL,
    user1_name VARCHAR(255),
    user2_name VARCHAR(255),
    space_id BIGINT,
    original_amount DECIMAL(19, 4),
    remaining_amount DECIMAL(19, 4),
    is_fully_settled BOOLEAN DEFAULT FALSE,
    settlement_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_split_balances_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_split_balances_user1
        FOREIGN KEY (user1_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_split_balances_user2
        FOREIGN KEY (user2_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_split_balances_space
        FOREIGN KEY (space_id)
        REFERENCES spaces(id)
        ON DELETE RESTRICT,

    -- Ensure user1_id is always less than user2_id to prevent duplicate reverse entries
    --CONSTRAINT chk_user_order CHECK (user1_id < user2_id),

    -- Prevent duplicate balance records between same users for same transaction
    CONSTRAINT uk_transaction_users UNIQUE (transaction_id, user1_email, user2_email)
);

-- Performance indexes
CREATE INDEX idx_split_balances_transaction ON split_balances(transaction_id);
CREATE INDEX idx_split_balances_user1 ON split_balances(user1_email);
CREATE INDEX idx_split_balances_user2 ON split_balances(user2_email);
CREATE INDEX idx_split_balances_space ON split_balances(space_id);
CREATE INDEX idx_split_balances_is_settled ON split_balances(is_fully_settled);

CREATE TABLE settlement_transactions (
    id BIGSERIAL PRIMARY KEY,
    split_balance_id BIGINT,
    payer_user_id VARCHAR(255),
    payer_user_email VARCHAR(255) NOT NULL,
    payer_user_name VARCHAR(255),
    receiver_user_id VARCHAR(255),
    receiver_user_email VARCHAR(255) NOT NULL,
    receiver_user_name VARCHAR(255),
    amount DECIMAL(19,4),
    payment_method VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_split_balance FOREIGN KEY (split_balance_id) REFERENCES split_balances(id),
    CONSTRAINT fk_payer_user FOREIGN KEY (payer_user_id) REFERENCES users(id),
    CONSTRAINT fk_receiver_user FOREIGN KEY (receiver_user_id) REFERENCES users(id)
);

-- Performance indexes
CREATE INDEX idx_settlement_split_balance ON settlement_transactions(split_balance_id);
CREATE INDEX idx_settlement_payer_user ON settlement_transactions(payer_user_email);
CREATE INDEX idx_settlement_receiver_user ON settlement_transactions(receiver_user_email);