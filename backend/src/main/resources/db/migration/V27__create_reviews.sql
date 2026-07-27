CREATE SCHEMA IF NOT EXISTS coin_product;

CREATE TABLE coin_product.reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES coin_product.products(id),
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_product_id ON coin_product.reviews(product_id);
CREATE INDEX idx_reviews_user_id ON coin_product.reviews(user_id);
