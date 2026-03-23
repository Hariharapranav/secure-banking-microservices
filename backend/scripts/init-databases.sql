-- ============================================
-- Secure Banking System - PostgreSQL Database Setup
-- ============================================
-- Run this script in psql or pgAdmin to create all required databases
-- Connect as the 'postgres' superuser first

CREATE DATABASE banking_auth_db;
CREATE DATABASE banking_user_db;
CREATE DATABASE banking_account_db;
CREATE DATABASE banking_transaction_db;
CREATE DATABASE banking_notification_db;

-- If using a custom user instead of 'postgres', grant access:
-- GRANT ALL PRIVILEGES ON DATABASE banking_auth_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE banking_user_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE banking_account_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE banking_transaction_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE banking_notification_db TO your_user;

-- Verify databases were created
\l
