-- Indexes for frequently queried product columns (WHERE clauses).
-- IF NOT EXISTS keeps this idempotent for indexes already created in V3.
CREATE INDEX IF NOT EXISTS idx_products_status ON coin_product.products(status);
CREATE INDEX IF NOT EXISTS idx_products_category ON coin_product.products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_seller ON coin_product.products(seller_id);
CREATE INDEX IF NOT EXISTS idx_products_title ON coin_product.products(lower(title));
