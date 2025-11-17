INSERT INTO supported_languages (code, name, created_at, updated_at)
VALUES ('en', 'English', NOW(), NOW());

INSERT INTO supported_currencies (code, name, symbol, created_at, updated_at)
VALUES
  ('INR', 'Indian Rupee', '₹', NOW(), NOW()),
  ('USD', 'US Dollar', '$', NOW(), NOW());

INSERT INTO admins (email, created_at, updated_at, created_by, updated_by)
VALUES ('${admin.email}', NOW(), NOW(), 'system', 'system');

INSERT INTO account_groups (name, description, icon, is_system_default, display_order, created_at, updated_at)
VALUES
  ('Cash', 'Cash in hand', 'cash_icon', TRUE, 1, NOW(), NOW()),
  ('Bank Account', 'Bank savings/current account', 'bank_icon', TRUE, 2, NOW(), NOW()),
  ('Credit Card', 'Credit card account', 'credit_card_icon', TRUE, 3, NOW(), NOW()),
  ('Debit Card', 'Debit card account', 'debit_card_icon', TRUE, 4, NOW(), NOW()),
  ('Loan', 'Loan account', 'loan_icon', TRUE, 5, NOW(), NOW()),
  ('Others', 'Other types of accounts', 'others_icon', TRUE, 6, NOW(), NOW());

-- Default INCOME categories
INSERT INTO categories (type, name, icon, is_sub_category, created_at, updated_at)
VALUES
  ('INCOME', 'Salary', 'salary_icon', FALSE, NOW(), NOW()),
  ('INCOME', 'Business', 'business_icon', FALSE, NOW(), NOW()),
  ('INCOME', 'Interest', 'interest_icon', FALSE, NOW(), NOW()),
  ('INCOME', 'Gift', 'gift_icon', FALSE, NOW(), NOW());

-- Default EXPENSE categories
INSERT INTO categories (type, name, icon, is_sub_category, created_at, updated_at)
VALUES
  ('EXPENSE', 'Food & Dining', 'food_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Groceries', 'groceries_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Transport', 'transport_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Shopping', 'shopping_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Health', 'health_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Utilities', 'utilities_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Rent', 'rent_icon', FALSE, NOW(), NOW()),
  ('EXPENSE', 'Entertainment', 'entertainment_icon', FALSE, NOW(), NOW());