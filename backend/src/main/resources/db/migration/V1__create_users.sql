CREATE SCHEMA IF NOT EXISTS coin_users;

CREATE TABLE coin_users.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(30),
    phone_country_code VARCHAR(5),
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    avatar_url VARCHAR(500),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_users.roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE coin_users.user_roles (
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    role_id BIGINT NOT NULL REFERENCES coin_users.roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE coin_users.user_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id),
    login_ip VARCHAR(45),
    login_time TIMESTAMP NOT NULL DEFAULT NOW(),
    success BOOLEAN NOT NULL
);

INSERT INTO coin_users.roles (name, description) VALUES
    ('ROLE_BUYER', '买家'),
    ('ROLE_SELLER', '卖家'),
    ('ROLE_ADMIN', '管理员');
