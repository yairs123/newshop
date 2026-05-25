CREATE SCHEMA IF NOT EXISTS coin_users;

-- User Addresses
CREATE TABLE coin_users.user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20),
    address TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User Payment Methods
CREATE TABLE coin_users.user_payment_methods (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    method_type VARCHAR(30) NOT NULL,
    provider VARCHAR(50),
    account_last_four VARCHAR(4),
    expiry_date VARCHAR(7),
    cardholder_name VARCHAR(100),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Support Tickets (Contact / Messages)
CREATE TABLE coin_users.support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    ticket_type VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_addresses_user ON coin_users.user_addresses(user_id);
CREATE INDEX idx_payment_methods_user ON coin_users.user_payment_methods(user_id);
CREATE INDEX idx_tickets_user ON coin_users.support_tickets(user_id);
CREATE INDEX idx_tickets_status ON coin_users.support_tickets(status);
