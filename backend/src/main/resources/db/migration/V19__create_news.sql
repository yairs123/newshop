CREATE TABLE coin_admin.news (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(200)  NOT NULL,
    summary       VARCHAR(500),
    content       TEXT          NOT NULL,
    image_url     VARCHAR(500),
    is_published  BOOLEAN       NOT NULL DEFAULT FALSE,
    published_at  TIMESTAMP,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_news_published ON coin_admin.news(is_published, published_at DESC);
