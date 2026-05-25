ALTER TABLE coin_order.orders
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30),
    ADD COLUMN IF NOT EXISTS shipping_method VARCHAR(30);
