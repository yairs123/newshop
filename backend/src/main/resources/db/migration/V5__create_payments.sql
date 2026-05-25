CREATE SCHEMA IF NOT EXISTS coin_payment;

CREATE TABLE coin_payment.payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_no VARCHAR(50) NOT NULL UNIQUE,
    order_no VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    gateway_transaction_id VARCHAR(200),
    gateway_response TEXT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_order ON coin_payment.payment_transactions(order_no);
CREATE INDEX idx_payment_transaction_no ON coin_payment.payment_transactions(transaction_no);
