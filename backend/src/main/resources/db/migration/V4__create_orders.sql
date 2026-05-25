CREATE SCHEMA IF NOT EXISTS coin_order;

CREATE TABLE coin_order.orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(30) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    seller_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    shipping_address TEXT,
    shipping_country VARCHAR(100),
    buyer_note TEXT,
    paid_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_order.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES coin_order.orders(id),
    product_id BIGINT NOT NULL,
    product_title VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL
);

CREATE TABLE coin_order.order_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES coin_order.orders(id),
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    operator VARCHAR(100),
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer ON coin_order.orders(buyer_id);
CREATE INDEX idx_orders_seller ON coin_order.orders(seller_id);
CREATE INDEX idx_orders_no ON coin_order.orders(order_no);
