-- Admin order edit history — tracks every admin modification with reason
-- Allows future auditing of who changed what and why
CREATE TABLE coin_order.order_edit_logs (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES coin_order.orders(id),
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    reason TEXT NOT NULL DEFAULT '',
    operator VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_edit_logs_order ON coin_order.order_edit_logs(order_id);
