ALTER TABLE coin_order.orders
    ADD COLUMN IF NOT EXISTS tracking_number VARCHAR(100),
    ADD COLUMN IF NOT EXISTS tracking_company VARCHAR(50);
