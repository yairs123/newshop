CREATE SCHEMA IF NOT EXISTS coin_auction;

CREATE TABLE IF NOT EXISTS coin_auction.auction_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    start_price DECIMAL(12, 2) NOT NULL,
    reserve_price DECIMAL(12, 2),
    current_bid DECIMAL(12, 2),
    bidder_id BIGINT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    extended_times INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS coin_auction.bids (
    id BIGSERIAL PRIMARY KEY,
    auction_item_id BIGINT NOT NULL REFERENCES coin_auction.auction_items(id),
    bidder_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    is_auto_bid BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_auction.auction_items IS '拍卖品表—预留，二期实现';
COMMENT ON TABLE coin_auction.bids IS '出价记录—预留，二期实现';
