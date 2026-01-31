# Finsible Database Documentation

This document provides comprehensive documentation of the Finsible database schema, including table structures, relationships, and design decisions.

## Table of Contents

- [Overview](#overview)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Core Tables](#core-tables)
- [Account Tables](#account-tables)
- [Transaction Tables](#transaction-tables)
- [Reference Tables](#reference-tables)
- [Space/Group Tables](#spacegroup-tables)
- [Database Migrations](#database-migrations)
- [Indexes](#indexes)
- [Constraints](#constraints)

---

## Overview

### Database Engine
- **Database:** PostgreSQL 15+
- **Migration Tool:** Flyway
- **ORM:** Hibernate/JPA

### Key Design Decisions

1. **Audit Fields:** All tables include `created_at`, `updated_at`, `created_by`, `updated_by` for audit trail
2. **Soft Deletes:** `is_active` flags used instead of hard deletes where appropriate
3. **Currency Handling:** Amounts stored as `DECIMAL(19, 4)` for precision
4. **Timestamps:** All timestamps stored as `TIMESTAMPTZ` (with timezone)
5. **Foreign Keys:** Referential integrity enforced at database level

---

## Entity Relationship Diagram

```
                           ┌──────────────────────────┐
                           │   supported_languages    │
                           │──────────────────────────│
                           │ id (PK)                  │
                           │ code (UNIQUE)            │
                           │ name                     │
                           └───────────┬──────────────┘
                                       │
                                       │ default_language
                                       ▼
┌──────────────────────────┐    ┌──────────────────────────┐    ┌──────────────────────────┐
│   supported_currencies   │    │         users            │    │         admins           │
│──────────────────────────│    │──────────────────────────│    │──────────────────────────│
│ id (PK)                  │◄───│ id (PK)                  │    │ id (PK)                  │
│ code (UNIQUE)            │    │ email (UNIQUE)           │    │ email (UNIQUE)           │
│ name                     │    │ name                     │    │ created_at               │
│ symbol                   │    │ picture                  │    └──────────────────────────┘
└───────────┬──────────────┘    │ last_logged_in           │
            │                   │ account_created          │
            │ default_currency  │ categories_edited        │
            │                   │ default_language (FK)    │
            │                   │ default_currency (FK)    │
            │                   └──────────────┬───────────┘
            │                                  │
            │                                  │ user_id
            ▼                                  ▼
┌───────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ┌──────────────────────────┐    ┌──────────────────────────┐                      │
│  │      account_groups      │    │       categories         │                      │
│  │──────────────────────────│    │──────────────────────────│                      │
│  │ id (PK)                  │    │ id (PK)                  │                      │
│  │ name                     │    │ type (ENUM)              │                      │
│  │ description              │    │ name                     │                      │
│  │ icon                     │    │ icon                     │                      │
│  │ color                    │    │ user_id (FK) ◄───────────┼──────────────────────┤
│  │ is_system_default        │    │ is_sub_category          │                      │
│  │ display_order            │    │ parent_category_id (FK)  │─┐ self-reference     │
│  │ user_id (FK) ◄───────────┼────│                          │ │                    │
│  └───────────┬──────────────┘    └──────────────┬───────────┘◄┘                    │
│              │                                  │                                   │
│              │ account_group_id                 │ category_id                       │
│              ▼                                  │                                   │
│  ┌──────────────────────────┐                   │                                   │
│  │        accounts          │                   │                                   │
│  │──────────────────────────│                   │                                   │
│  │ id (PK)                  │                   │                                   │
│  │ user_id (FK) ◄───────────┼───────────────────┼───────────────────────────────────┤
│  │ account_group_id (FK)    │                   │                                   │
│  │ name                     │                   │                                   │
│  │ description              │                   │                                   │
│  │ balance                  │                   │                                   │
│  │ currency_code (FK)       │◄──────────────────┼───────────────────────────────────┤
│  │ is_active                │                   │          currency_code            │
│  │ is_system_default        │                   │                                   │
│  └───────────┬──────────────┘                   │                                   │
│              │                                  │                                   │
│              │ account_id                       │                                   │
│              ▼                                  ▼                                   │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌────────────────────────────┐ │
│  │ credit_card_details   │ │ debit_card_details    │ │      transactions          │ │
│  │───────────────────────│ │───────────────────────│ │────────────────────────────│ │
│  │ account_id (PK/FK)    │ │ account_id (PK/FK)    │ │ id (PK)                    │ │
│  │ credit_limit          │ │ linked_bank_account_id│ │ type (ENUM)                │ │
│  │ available_credit      │ └───────────────────────┘ │ total_amount               │ │
│  │ billing_date          │                           │ transaction_date           │ │
│  │ due_date              │ ┌───────────────────────┐ │ user_share                 │ │
│  │ auto_pay_enabled      │ │    loan_details       │ │ is_split                   │ │
│  │ auto_pay_from_account │ │───────────────────────│ │ paid_by_id (FK)            │ │
│  └───────────────────────┘ │ account_id (PK/FK)    │ │ category_id (FK)           │ │
│                            │ loan_type             │ │ to_account_id (FK)         │ │
│                            │ principal_amount      │ │ from_account_id (FK)       │ │
│                            │ interest_rate         │ │ space_id (FK)              │ │
│                            │ emi_amount            │ │ currency_code (FK)         │ │
│                            │ emi_date              │ └────────────────────────────┘ │
│                            │ tenure_months         │                                │
│                            │ start_date            │                                │
│                            └───────────────────────┘                                │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Core Tables

### users

Stores user account information authenticated via Google OAuth.

| Column              | Type         | Nullable | Description                            |
|---------------------|--------------|----------|----------------------------------------|
| `id`                | VARCHAR(255) | NO       | Primary key (Google user ID)           |
| `email`             | VARCHAR(255) | NO       | Unique email address                   |
| `name`              | VARCHAR(255) | NO       | User's display name                    |
| `picture`           | VARCHAR(500) | YES      | Profile picture URL                    |
| `last_logged_in`    | TIMESTAMPTZ  | NO       | Last login timestamp                   |
| `account_created`   | TIMESTAMPTZ  | NO       | Account creation timestamp             |
| `categories_edited` | BOOLEAN      | NO       | Whether user has customized categories |
| `default_language`  | VARCHAR(10)  | NO       | FK to supported_languages(code)        |
| `default_currency`  | VARCHAR(3)   | NO       | FK to supported_currencies(code)       |

**Indexes:**
- `idx_users_email` on `email`

**Notes:**
- ID is not auto-generated; it uses the Google user ID
- Users are created on first Google sign-in

---

### admins

Stores admin user email addresses for role-based access control.

| Column       | Type         | Nullable | Description             |
|--------------|--------------|----------|-------------------------|
| `id`         | BIGSERIAL    | NO       | Primary key             |
| `email`      | VARCHAR(255) | NO       | Unique admin email      |
| `created_at` | TIMESTAMPTZ  | NO       | Creation timestamp      |
| `updated_at` | TIMESTAMPTZ  | NO       | Last update timestamp   |
| `created_by` | VARCHAR(255) | YES      | Creator identifier      |
| `updated_by` | VARCHAR(255) | YES      | Last updater identifier |

**Notes:**
- Admin email is seeded via Flyway migration
- Admin role is checked by email lookup during JWT generation

---

## Account Tables

### account_groups

Defines groupings/types for accounts (Cash, Bank, Credit Card, etc.).

| Column              | Type         | Nullable | Description                             |
|---------------------|--------------|----------|-----------------------------------------|
| `id`                | BIGSERIAL    | NO       | Primary key                             |
| `name`              | VARCHAR(255) | NO       | Group name                              |
| `description`       | VARCHAR(255) | YES      | Group description                       |
| `icon`              | VARCHAR(255) | YES      | Icon identifier                         |
| `color`             | VARCHAR(25)  | YES      | Display color                           |
| `is_system_default` | BOOLEAN      | NO       | System-provided group flag              |
| `display_order`     | INT          | YES      | Display ordering                        |
| `user_id`           | VARCHAR(255) | YES      | FK to users(id), NULL for system groups |
| `created_at`        | TIMESTAMPTZ  | NO       | Creation timestamp                      |
| `updated_at`        | TIMESTAMPTZ  | NO       | Last update timestamp                   |
| `updated_by`        | VARCHAR(255) | YES      | Last updater identifier                 |

**Indexes:**
- `idx_system_default_account_groups` on `is_system_default`
- `idx_account_groups_user_id` on `user_id`

**Constraints:**
- `uk_user_account_group_name` UNIQUE(`user_id`, `name`)

**Default Groups:**
| ID | Name |
|----|------|
| 1 | Cash |
| 2 | Bank Account |
| 3 | Credit Card |
| 4 | Debit Card |
| 5 | Loan |
| 6 | Others |

---

### accounts

Stores individual financial accounts.

| Column              | Type          | Nullable | Description                      |
|---------------------|---------------|----------|----------------------------------|
| `id`                | BIGSERIAL     | NO       | Primary key                      |
| `user_id`           | VARCHAR(255)  | YES      | FK to users(id)                  |
| `account_group_id`  | BIGINT        | NO       | FK to account_groups(id)         |
| `name`              | VARCHAR(255)  | NO       | Account name                     |
| `description`       | VARCHAR(255)  | YES      | Account description              |
| `icon`              | VARCHAR(255)  | YES      | Icon identifier                  |
| `balance`           | DECIMAL(19,4) | YES      | Current balance                  |
| `currency_code`     | VARCHAR(3)    | NO       | FK to supported_currencies(code) |
| `is_active`         | BOOLEAN       | NO       | Active status flag               |
| `is_system_default` | BOOLEAN       | NO       | System default flag              |
| `created_at`        | TIMESTAMPTZ   | NO       | Creation timestamp               |
| `updated_at`        | TIMESTAMPTZ   | NO       | Last update timestamp            |
| `created_by`        | VARCHAR(255)  | YES      | Creator identifier               |
| `updated_by`        | VARCHAR(255)  | YES      | Last updater identifier          |

**Indexes:**
- `idx_account_user_id` on `user_id`
- `idx_account_group_id` on `account_group_id`

**Constraints:**
- `uk_user_account_name` UNIQUE(`user_id`, `name`)

---

### credit_card_details

Extended details for credit card accounts.

| Column                     | Type          | Nullable | Description               |
|----------------------------|---------------|----------|---------------------------|
| `account_id`               | BIGINT        | NO       | PK and FK to accounts(id) |
| `credit_limit`             | DECIMAL(19,4) | NO       | Credit limit              |
| `available_credit`         | DECIMAL(19,4) | NO       | Available credit          |
| `billing_date`             | INT           | NO       | Day of month (1-31)       |
| `due_date`                 | INT           | NO       | Day of month (1-31)       |
| `auto_pay_enabled`         | BOOLEAN       | YES      | Auto-pay flag             |
| `auto_pay_from_account_id` | BIGINT        | YES      | FK to accounts(id)        |
| `created_at`               | TIMESTAMPTZ   | NO       | Creation timestamp        |
| `updated_at`               | TIMESTAMPTZ   | NO       | Last update timestamp     |
| `created_by`               | VARCHAR(255)  | YES      | Creator identifier        |
| `updated_by`               | VARCHAR(255)  | YES      | Last updater identifier   |

**Constraints:**
- `chk_billing_date` CHECK(`billing_date` BETWEEN 1 AND 31)
- `chk_due_date` CHECK(`due_date` BETWEEN 1 AND 31)
- CASCADE delete on `account_id`

---

### debit_card_details

Extended details for debit card accounts.

| Column                   | Type         | Nullable | Description               |
|--------------------------|--------------|----------|---------------------------|
| `account_id`             | BIGINT       | NO       | PK and FK to accounts(id) |
| `linked_bank_account_id` | BIGINT       | NO       | FK to accounts(id)        |
| `created_at`             | TIMESTAMPTZ  | NO       | Creation timestamp        |
| `updated_at`             | TIMESTAMPTZ  | NO       | Last update timestamp     |
| `created_by`             | VARCHAR(255) | YES      | Creator identifier        |
| `updated_by`             | VARCHAR(255) | YES      | Last updater identifier   |

---

### loan_details

Extended details for loan accounts.

| Column             | Type          | Nullable | Description                        |
|--------------------|---------------|----------|------------------------------------|
| `account_id`       | BIGINT        | NO       | PK and FK to accounts(id)          |
| `loan_type`        | VARCHAR(50)   | YES      | HOME_LOAN, PERSONAL_LOAN, CAR_LOAN |
| `principal_amount` | DECIMAL(19,4) | NO       | Principal amount                   |
| `interest_rate`    | DECIMAL(5,2)  | YES      | Interest rate percentage           |
| `emi_amount`       | DECIMAL(19,4) | YES      | Monthly EMI                        |
| `emi_date`         | INT           | YES      | EMI due date (1-31)                |
| `tenure_months`    | INT           | YES      | Loan tenure in months              |
| `start_date`       | DATE          | YES      | Loan start date                    |
| `created_at`       | TIMESTAMPTZ   | NO       | Creation timestamp                 |
| `updated_at`       | TIMESTAMPTZ   | NO       | Last update timestamp              |
| `created_by`       | VARCHAR(255)  | YES      | Creator identifier                 |
| `updated_by`       | VARCHAR(255)  | YES      | Last updater identifier            |

---

## Transaction Tables

### transactions

Stores financial transactions (income, expense, transfer).

| Column             | Type          | Nullable | Description                               |
|--------------------|---------------|----------|-------------------------------------------|
| `id`               | BIGSERIAL     | NO       | Primary key                               |
| `type`             | VARCHAR(50)   | NO       | INCOME, EXPENSE, TRANSFER                 |
| `total_amount`     | DECIMAL(19,4) | YES      | Transaction amount                        |
| `transaction_date` | BIGINT        | NO       | Epoch milliseconds                        |
| `user_share`       | DECIMAL(19,4) | YES      | User's share (for splits)                 |
| `is_split`         | BOOLEAN       | YES      | Split transaction flag                    |
| `paid_by_id`       | VARCHAR(255)  | YES      | FK to users(id)                           |
| `category_id`      | BIGINT        | YES      | FK to categories(id)                      |
| `to_account_id`    | BIGINT        | YES      | FK to accounts(id) - for INCOME/TRANSFER  |
| `from_account_id`  | BIGINT        | YES      | FK to accounts(id) - for EXPENSE/TRANSFER |
| `description`      | TEXT          | YES      | Transaction description                   |
| `space_id`         | BIGINT        | YES      | FK to spaces(id)                          |
| `currency_code`    | VARCHAR(3)    | NO       | FK to supported_currencies(code)          |
| `created_at`       | TIMESTAMPTZ   | NO       | Creation timestamp                        |
| `updated_at`       | TIMESTAMPTZ   | NO       | Last update timestamp                     |
| `created_by`       | VARCHAR(255)  | YES      | Creator identifier                        |
| `updated_by`       | VARCHAR(255)  | YES      | Last updater identifier                   |

**Indexes:**
- `idx_transactions_paid_by` on `paid_by_id`
- `idx_transactions_category` on `category_id`
- `idx_transactions_account` on `to_account_id`
- `idx_transactions_space` on `space_id`
- `idx_transactions_type` on `type`
- `idx_transactions_created_at` on `created_at`

**Constraints:**
- `chk_transaction_type` CHECK(`type` IN ('INCOME', 'EXPENSE', 'TRANSFER'))

**Transaction Type Logic:**
| Type | from_account_id | to_account_id |
|------|-----------------|---------------|
| INCOME | NULL | Required |
| EXPENSE | Required | NULL |
| TRANSFER | Required | Required |

---

### categories

Stores transaction categories.

| Column               | Type         | Nullable | Description                        |
|----------------------|--------------|----------|------------------------------------|
| `id`                 | BIGSERIAL    | NO       | Primary key                        |
| `type`               | VARCHAR(255) | YES      | INCOME, EXPENSE, TRANSFER          |
| `name`               | VARCHAR(255) | NO       | Category name                      |
| `icon`               | VARCHAR(255) | YES      | Icon identifier                    |
| `user_id`            | VARCHAR(255) | YES      | FK to users(id), NULL for defaults |
| `is_sub_category`    | BOOLEAN      | NO       | Subcategory flag                   |
| `parent_category_id` | BIGINT       | YES      | FK to categories(id)               |
| `created_at`         | TIMESTAMPTZ  | NO       | Creation timestamp                 |
| `updated_at`         | TIMESTAMPTZ  | NO       | Last update timestamp              |
| `updated_by`         | VARCHAR(255) | YES      | Last updater identifier            |

**Indexes:**
- `idx_categories_user_id` on `user_id`
- `idx_categories_type` on `type`

**Constraints:**
- `uk_user_category_name` UNIQUE(`user_id`, `name`, `type`)
- `categories_type_check` CHECK(`type` IN ('INCOME', 'EXPENSE', 'TRANSFER'))

**Default Categories:**

*Income:*
- Salary, Business, Interest, Gift

*Expense:*
- Food & Dining, Groceries, Transport, Shopping, Health, Utilities, Rent, Entertainment

---

## Reference Tables

### supported_currencies

Stores supported currency codes.

| Column       | Type         | Nullable | Description             |
|--------------|--------------|----------|-------------------------|
| `id`         | BIGSERIAL    | NO       | Primary key             |
| `code`       | VARCHAR(3)   | NO       | ISO 4217 currency code  |
| `name`       | VARCHAR(100) | NO       | Currency name           |
| `symbol`     | VARCHAR(10)  | NO       | Currency symbol         |
| `created_at` | TIMESTAMPTZ  | NO       | Creation timestamp      |
| `updated_at` | TIMESTAMPTZ  | NO       | Last update timestamp   |
| `created_by` | VARCHAR(255) | YES      | Creator identifier      |
| `updated_by` | VARCHAR(255) | YES      | Last updater identifier |

**Default Currencies:**
| Code | Name | Symbol |
|------|------|--------|
| INR | Indian Rupee | ₹ |
| USD | US Dollar | $ |

---

### supported_languages

Stores supported language codes.

| Column       | Type         | Nullable | Description             |
|--------------|--------------|----------|-------------------------|
| `id`         | BIGSERIAL    | NO       | Primary key             |
| `code`       | VARCHAR(10)  | NO       | ISO language code       |
| `name`       | VARCHAR(100) | NO       | Language name           |
| `created_at` | TIMESTAMPTZ  | NO       | Creation timestamp      |
| `updated_at` | TIMESTAMPTZ  | NO       | Last update timestamp   |
| `created_by` | VARCHAR(255) | YES      | Creator identifier      |
| `updated_by` | VARCHAR(255) | YES      | Last updater identifier |

**Default Languages:**
| Code | Name |
|------|------|
| en | English |

---

## Space/Group Tables

### spaces

Stores shared expense spaces/groups.

| Column              | Type          | Nullable | Description                      |
|---------------------|---------------|----------|----------------------------------|
| `id`                | BIGSERIAL     | NO       | Primary key                      |
| `space_name`        | VARCHAR(255)  | NO       | Space name                       |
| `description`       | TEXT          | YES      | Space description                |
| `icon`              | VARCHAR(255)  | YES      | Icon identifier                  |
| `total_spends`      | DECIMAL(19,4) | YES      | Total spending amount            |
| `currency_code`     | VARCHAR(3)    | NO       | FK to supported_currencies(code) |
| `is_active`         | BOOLEAN       | YES      | Active status                    |
| `is_system_default` | BOOLEAN       | NO       | System default flag              |
| `note`              | TEXT          | YES      | Additional notes                 |
| `created_at`        | TIMESTAMPTZ   | NO       | Creation timestamp               |
| `updated_at`        | TIMESTAMPTZ   | NO       | Last update timestamp            |
| `created_by`        | VARCHAR(255)  | YES      | Creator identifier               |
| `updated_by`        | VARCHAR(255)  | YES      | Last updater identifier          |

---

### space_user_details

Links users to spaces with their spending details.

| Column          | Type          | Nullable | Description                      |
|-----------------|---------------|----------|----------------------------------|
| `id`            | BIGSERIAL     | NO       | Primary key                      |
| `space_id`      | BIGINT        | NO       | FK to spaces(id)                 |
| `user_id`       | VARCHAR(255)  | YES      | FK to users(id)                  |
| `user_email`    | VARCHAR(255)  | NO       | User email                       |
| `user_name`     | VARCHAR(255)  | YES      | User display name                |
| `user_spends`   | DECIMAL(19,4) | YES      | User's spending                  |
| `role`          | VARCHAR(50)   | YES      | User role in space               |
| `is_active`     | BOOLEAN       | YES      | Active status                    |
| `currency_code` | VARCHAR(3)    | NO       | FK to supported_currencies(code) |
| `created_at`    | TIMESTAMPTZ   | NO       | Creation timestamp               |
| `updated_at`    | TIMESTAMPTZ   | NO       | Last update timestamp            |

**Constraints:**
- `uk_space_user` UNIQUE(`space_id`, `user_email`)

---

### split_transactions

Stores individual splits of a split transaction.

| Column            | Type          | Nullable | Description             |
|-------------------|---------------|----------|-------------------------|
| `id`              | BIGSERIAL     | NO       | Primary key             |
| `transaction_id`  | BIGINT        | YES      | FK to transactions(id)  |
| `user_id`         | VARCHAR(255)  | YES      | FK to users(id)         |
| `user_email`      | VARCHAR(255)  | NO       | User email              |
| `user_name`       | VARCHAR(255)  | YES      | User display name       |
| `space_id`        | BIGINT        | YES      | FK to spaces(id)        |
| `share_amount`    | DECIMAL(19,4) | YES      | User's share amount     |
| `is_settled`      | BOOLEAN       | YES      | Settlement status       |
| `settlement_date` | TIMESTAMPTZ   | YES      | When settled            |
| `is_active`       | BOOLEAN       | YES      | Active status           |
| `version`         | VARCHAR(255)  | YES      | Version tracking        |
| `superseded_at`   | TIMESTAMPTZ   | YES      | When superseded         |
| `created_at`      | TIMESTAMPTZ   | NO       | Creation timestamp      |
| `updated_at`      | TIMESTAMPTZ   | NO       | Last update timestamp   |
| `created_by`      | VARCHAR(255)  | YES      | Creator identifier      |
| `updated_by`      | VARCHAR(255)  | YES      | Last updater identifier |

---

### split_balances

Tracks balances between users from split transactions.

| Column             | Type          | Nullable | Description             |
|--------------------|---------------|----------|-------------------------|
| `id`               | BIGSERIAL     | NO       | Primary key             |
| `transaction_id`   | BIGINT        | YES      | FK to transactions(id)  |
| `user1_id`         | VARCHAR(255)  | YES      | FK to users(id)         |
| `user2_id`         | VARCHAR(255)  | YES      | FK to users(id)         |
| `user1_email`      | VARCHAR(255)  | NO       | User 1 email            |
| `user2_email`      | VARCHAR(255)  | NO       | User 2 email            |
| `user1_name`       | VARCHAR(255)  | YES      | User 1 name             |
| `user2_name`       | VARCHAR(255)  | YES      | User 2 name             |
| `space_id`         | BIGINT        | YES      | FK to spaces(id)        |
| `original_amount`  | DECIMAL(19,4) | YES      | Original balance        |
| `remaining_amount` | DECIMAL(19,4) | YES      | Remaining balance       |
| `is_fully_settled` | BOOLEAN       | YES      | Fully settled flag      |
| `settlement_date`  | TIMESTAMPTZ   | YES      | Settlement date         |
| `created_at`       | TIMESTAMPTZ   | NO       | Creation timestamp      |
| `updated_at`       | TIMESTAMPTZ   | NO       | Last update timestamp   |
| `created_by`       | VARCHAR(255)  | YES      | Creator identifier      |
| `updated_by`       | VARCHAR(255)  | YES      | Last updater identifier |

**Constraints:**
- `uk_transaction_users` UNIQUE(`transaction_id`, `user1_email`, `user2_email`)

---

### settlement_transactions

Records settlement payments between users.

| Column                | Type          | Nullable | Description              |
|-----------------------|---------------|----------|--------------------------|
| `id`                  | BIGSERIAL     | NO       | Primary key              |
| `split_balance_id`    | BIGINT        | YES      | FK to split_balances(id) |
| `payer_user_id`       | VARCHAR(255)  | YES      | FK to users(id)          |
| `payer_user_email`    | VARCHAR(255)  | NO       | Payer email              |
| `payer_user_name`     | VARCHAR(255)  | YES      | Payer name               |
| `receiver_user_id`    | VARCHAR(255)  | YES      | FK to users(id)          |
| `receiver_user_email` | VARCHAR(255)  | NO       | Receiver email           |
| `receiver_user_name`  | VARCHAR(255)  | YES      | Receiver name            |
| `amount`              | DECIMAL(19,4) | YES      | Settlement amount        |
| `payment_method`      | VARCHAR(255)  | YES      | Payment method used      |
| `created_at`          | TIMESTAMPTZ   | NO       | Creation timestamp       |
| `updated_at`          | TIMESTAMPTZ   | NO       | Last update timestamp    |
| `created_by`          | VARCHAR(255)  | YES      | Creator identifier       |
| `updated_by`          | VARCHAR(255)  | YES      | Last updater identifier  |

---

## Database Migrations

Migrations are managed by Flyway and located in:
```
src/main/resources/db/migration/
```

### Migration Files

| Version | File                            | Description                            |
|---------|---------------------------------|----------------------------------------|
| V1      | `V1__create_initial_tables.sql` | Creates all initial tables             |
| V2      | `V2__initial_data_seed.sql`     | Seeds default data                     |
| V3      | `V3__update_table_schema.sql`   | Adds transaction_date, from_account_id |

### Running Migrations

```bash
# Via Gradle
./gradlew flywayMigrate

# Via Spring Boot (automatic on startup)
# Set in application.properties:
spring.flyway.enabled=true
```

### Creating New Migrations

```bash
# Create a new migration file
touch src/main/resources/db/migration/V4__description.sql
```

**Naming Convention:** `V{version}__{description}.sql`

---

## Indexes

### Performance Indexes

| Table          | Index                             | Columns           |
|----------------|-----------------------------------|-------------------|
| users          | idx_users_email                   | email             |
| account_groups | idx_system_default_account_groups | is_system_default |
| account_groups | idx_account_groups_user_id        | user_id           |
| accounts       | idx_account_user_id               | user_id           |
| accounts       | idx_account_group_id              | account_group_id  |
| categories     | idx_categories_user_id            | user_id           |
| categories     | idx_categories_type               | type              |
| transactions   | idx_transactions_paid_by          | paid_by_id        |
| transactions   | idx_transactions_category         | category_id       |
| transactions   | idx_transactions_account          | to_account_id     |
| transactions   | idx_transactions_space            | space_id          |
| transactions   | idx_transactions_type             | type              |
| transactions   | idx_transactions_created_at       | created_at        |

---

## Constraints

### Unique Constraints

| Table                | Constraint                 | Columns              |
|----------------------|----------------------------|----------------------|
| users                | pk_users                   | id                   |
| users                | uk_users_email             | email                |
| account_groups       | uk_user_account_group_name | user_id, name        |
| accounts             | uk_user_account_name       | user_id, name        |
| categories           | uk_user_category_name      | user_id, name, type  |
| supported_currencies | uk_currency_code           | code                 |
| supported_languages  | uk_language_code           | code                 |
| space_user_details   | uk_space_user              | space_id, user_email |

### Check Constraints

| Table               | Constraint            | Condition                                 |
|---------------------|-----------------------|-------------------------------------------|
| transactions        | chk_transaction_type  | type IN ('INCOME', 'EXPENSE', 'TRANSFER') |
| categories          | categories_type_check | type IN ('INCOME', 'EXPENSE', 'TRANSFER') |
| credit_card_details | chk_billing_date      | billing_date BETWEEN 1 AND 31             |
| credit_card_details | chk_due_date          | due_date BETWEEN 1 AND 31                 |
| loan_details        | chk_emi_date          | emi_date BETWEEN 1 AND 31                 |

### Foreign Key Behavior

| Table               | FK Column  | On Delete |
|---------------------|------------|-----------|
| credit_card_details | account_id | CASCADE   |
| debit_card_details  | account_id | CASCADE   |
| loan_details        | account_id | CASCADE   |
| transactions        | *          | RESTRICT  |
| space_user_details  | *          | RESTRICT  |

---

*Last updated: January 31, 2025*
