-- Add new product categories
INSERT INTO coin_product.categories (id, name, slug, parent_id, sort_order) VALUES
    (7, 'Spanish Coins', 'spanish-coins', NULL, 7),
    (8, 'Germany Coins', 'germany-coins', NULL, 8),
    (9, 'France Coins', 'france-coins', NULL, 9),
    (10, 'Britain Coins', 'britain-coins', NULL, 10),
    (11, 'USA Coins', 'usa-coins', NULL, 11),
    (12, 'Bullion Coin', 'bullion-coin', NULL, 12),
    (13, 'Euros Coins', 'euros-coins', NULL, 13),
    (14, 'Medal', 'medal', NULL, 14),
    (15, 'Banknotes', 'banknotes', NULL, 15),
    (16, 'Accessories Numismatic', 'accessories-numismatic', NULL, 16)
ON CONFLICT (slug) DO NOTHING;

SELECT setval('coin_product.categories_id_seq', (SELECT MAX(id) FROM coin_product.categories));
