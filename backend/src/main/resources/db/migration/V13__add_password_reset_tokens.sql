CREATE SCHEMA IF NOT EXISTS coin_users;

CREATE TABLE coin_users.password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES coin_users.users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_tokens_token ON coin_users.password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_user_id ON coin_users.password_reset_tokens(user_id);
