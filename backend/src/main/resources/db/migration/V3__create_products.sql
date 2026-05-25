CREATE SCHEMA IF NOT EXISTS coin_product;

CREATE TABLE coin_product.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES coin_product.categories(id),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_product.products (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    stock INT NOT NULL DEFAULT 0,
    category_id BIGINT REFERENCES coin_product.categories(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    rating_company VARCHAR(20),
    rating_number VARCHAR(50),
    rating_grade VARCHAR(50),
    country VARCHAR(100),
    year INT,
    material VARCHAR(50),
    denomination VARCHAR(50),
    weight DECIMAL(10, 2),
    view_count INT NOT NULL DEFAULT 0,
    sales_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_product.product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES coin_product.products(id) ON DELETE CASCADE,
    url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_products_seller ON coin_product.products(seller_id);
CREATE INDEX idx_products_category ON coin_product.products(category_id);
CREATE INDEX idx_products_rating_number ON coin_product.products(rating_number);
CREATE INDEX idx_products_status ON coin_product.products(status);
