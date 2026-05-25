CREATE SCHEMA IF NOT EXISTS coin_seller;

CREATE TABLE coin_seller.seller_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    id_document_url VARCHAR(500),
    id_document_type VARCHAR(50),
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    shop_name VARCHAR(100),
    shop_description TEXT,
    reject_reason TEXT,
    reviewed_by BIGINT REFERENCES coin_users.users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_seller.seller_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES coin_users.users(id),
    shop_name VARCHAR(100) NOT NULL,
    shop_description TEXT,
    contact_phone VARCHAR(30),
    contact_email VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    locked BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
