CREATE TABLE coin_admin.ads (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL,
    image_url   VARCHAR(500)  NOT NULL,
    link_url    VARCHAR(500),
    sort_order  INT           NOT NULL DEFAULT 0,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    start_date  DATE,
    end_date    DATE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ads_active_sort ON coin_admin.ads(is_active, sort_order);
