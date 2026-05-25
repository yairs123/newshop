CREATE SCHEMA IF NOT EXISTS coin_cart;

CREATE TABLE coin_cart.cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES coin_product.products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_cart_items_user_id ON coin_cart.cart_items(user_id);
