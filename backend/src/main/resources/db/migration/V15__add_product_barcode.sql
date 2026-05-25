ALTER TABLE coin_product.products ADD COLUMN barcode VARCHAR(50) UNIQUE;
CREATE INDEX idx_products_barcode ON coin_product.products(barcode);
