-- Seed data for development and testing
-- All passwords = "123" (bcrypt hash: $2a$04$sxgDeY0CJjwgIg.gxY8fQeTAGhXQwAmA8hZSJqQ2cC5kcf1o53IfO)

-- Test Users
INSERT INTO coin_users.users (id, username, email, phone, display_name, password_hash, preferred_language, enabled)
VALUES
    (1, 'admin', 'admin@coinmarket.com', '+1-555-0100', 'Admin', '$2a$04$sxgDeY0CJjwgIg.gxY8fQeTAGhXQwAmA8hZSJqQ2cC5kcf1o53IfO', 'en', TRUE),
    (2, 'seller', 'seller@coinmarket.com', '+1-555-0101', 'Coin Seller', '$2a$04$sxgDeY0CJjwgIg.gxY8fQeTAGhXQwAmA8hZSJqQ2cC5kcf1o53IfO', 'en', TRUE),
    (3, 'buyer', 'buyer@coinmarket.com', '+1-555-0102', 'Coin Collector', '$2a$04$sxgDeY0CJjwgIg.gxY8fQeTAGhXQwAmA8hZSJqQ2cC5kcf1o53IfO', 'en', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Role assignments
-- Roles from V1: ROLE_BUYER(1), ROLE_SELLER(2), ROLE_ADMIN(3)
INSERT INTO coin_users.user_roles (user_id, role_id)
SELECT u.id, r.id FROM (VALUES ('admin', 'ROLE_ADMIN'), ('seller', 'ROLE_SELLER'), ('seller', 'ROLE_BUYER'), ('buyer', 'ROLE_BUYER')) AS ur(uname, rname)
JOIN coin_users.users u ON u.username = ur.uname
JOIN coin_users.roles r ON r.name = ur.rname
ON CONFLICT DO NOTHING;

-- Seller Profile (for seller user)
INSERT INTO coin_seller.seller_profiles (user_id, shop_name, shop_description, contact_phone, contact_email, status, locked)
SELECT id, 'Numismatic Treasures', 'Premium ancient and world coins, certified by NGC and PCGS', '+1-555-0101', 'seller@coinmarket.com', 'ACTIVE', FALSE
FROM coin_users.users WHERE username = 'seller'
ON CONFLICT DO NOTHING;

-- Product Categories
INSERT INTO coin_product.categories (id, name, slug, parent_id, sort_order) VALUES
    (1, 'Ancient Coins', 'ancient-coins', NULL, 1),
    (2, 'Gold Coins', 'gold-coins', NULL, 2),
    (3, 'Silver Coins', 'silver-coins', NULL, 3),
    (4, 'Chinese Coins', 'chinese-coins', NULL, 4),
    (5, 'World Coins', 'world-coins', NULL, 5),
    (6, 'Commemorative Coins', 'commemorative-coins', NULL, 6)
ON CONFLICT (slug) DO NOTHING;

-- Sample Products (seller determined by subquery)
INSERT INTO coin_product.products (id, seller_id, title, description, price, currency, stock, category_id, status, rating_company, rating_number, rating_grade, country, year, material, denomination, weight, view_count, sales_count)
SELECT v.id, u.id, v.title, v.prod_desc, v.price, v.currency, v.stock, v.cat_id, v.status, v.rating_company, v.rating_number, v.rating_grade, v.country, v.year, v.material, v.denomination, v.weight, v.view_count, v.sales_count
FROM coin_users.users u
CROSS JOIN (VALUES
    (1, '1895 Morgan Silver Dollar PCGS MS63', 'Stunning 1895 Morgan Silver Dollar certified by PCGS. Beautiful luster with sharp details.', 1299.99, 'USD', 2, 3, 'ACTIVE', 'PCGS', 'MS63', 'MS63', 'USA', 1895, 'Silver', '$1', 26.73, 142, 5),
    (2, '1916-D Mercury Dime NGC AU55', 'Key date Mercury Dime from Denver mint. Well-struck with attractive toning.', 849.50, 'USD', 3, 3, 'ACTIVE', 'NGC', 'AU55', 'AU55', 'USA', 1916, 'Silver', '10¢', 2.50, 89, 3),
    (3, '1909-S VDB Lincoln Cent NGC XF40', 'One of the most sought-after Lincoln cents. Clear VDB initials on reverse.', 675.00, 'USD', 1, 3, 'ACTIVE', 'NGC', 'XF40', 'XF40', 'USA', 1909, 'Copper', '1¢', 3.11, 210, 8),
    (4, '1933 Saint-Gaudens Gold Double Eagle MS65', 'Rare pre-1933 gold coin. Exceptional mint state condition with original mint luster.', 24999.99, 'USD', 1, 2, 'ACTIVE', 'PCGS', 'MS65', 'MS65', 'USA', 1933, 'Gold', '$20', 33.44, 56, 1),
    (5, '1794 Flowing Hair Silver Dollar PCGS F12', 'Early American silver dollar with bold details. Historic piece from the first year of issue.', 15999.00, 'USD', 1, 1, 'ACTIVE', 'PCGS', 'F12', 'F12', 'USA', 1794, 'Silver', '$1', 26.96, 34, 0),
    (6, '2008 China Panda Gold Coin NGC MS70', 'Modern Chinese gold bullion coin. Perfect MS70 grade with beautiful panda design.', 2899.00, 'USD', 5, 2, 'ACTIVE', 'NGC', 'MS70', 'MS70', 'China', 2008, 'Gold', '100元', 31.10, 178, 12),
    (7, '1986 Statue of Liberty Commemorative Silver Dollar PR69', 'Proof silver dollar commemorating the centennial of the Statue of Liberty. Deep Cameo contrast.', 125.00, 'USD', 10, 6, 'ACTIVE', 'NGC', 'PR69DCAM', 'PR69', 'USA', 1986, 'Silver', '$1', 26.73, 67, 4),
    (8, '1921 Peace Silver Dollar NGC MS64', 'First year of issue Peace Dollar with full cartwheel luster. Popular design by Anthony de Francisci.', 349.99, 'USD', 4, 3, 'ACTIVE', 'NGC', 'MS64', 'MS64', 'USA', 1921, 'Silver', '$1', 26.73, 112, 7),
    (9, '1878-CC Morgan Silver Dollar PCGS AU53', 'Carson City Mint Morgan Dollar with visible CC mintmark. Scarce branch mint issue.', 899.00, 'USD', 2, 3, 'ACTIVE', 'PCGS', 'AU53', 'AU53', 'USA', 1878, 'Silver', '$1', 26.73, 45, 2),
    (10, '1911-D Indian Head Quarter Eagle NGC AU58', 'Scarce Denver mint gold quarter eagle. Rich natural gold color with minimal wear.', 2499.00, 'USD', 1, 2, 'ACTIVE', 'NGC', 'AU58', 'AU58', 'USA', 1911, 'Gold', '$2.50', 4.18, 28, 0),
    (11, 'Ancient Roman Denarius - Emperor Trajan NGC Ch F', 'Silver denarius from the reign of Emperor Trajan (AD 98-117). Well-centered strike with clear portrait.', 450.00, 'USD', 3, 1, 'ACTIVE', 'NGC', 'ChF', 'ChF', 'Roman Empire', 107, 'Silver', 'Denarius', 3.40, 91, 6),
    (12, '1995-W Proof Silver Eagle PCGS PR70DCAM', 'Perfect PR70 Deep Cameo proof Silver Eagle from West Point. Flawless mirror surfaces.', 599.99, 'USD', 3, 3, 'ACTIVE', 'PCGS', 'PR70DCAM', 'PR70', 'USA', 1995, 'Silver', '$1', 31.10, 234, 15)
) AS v(id, title, prod_desc, price, currency, stock, cat_id, status, rating_company, rating_number, rating_grade, country, year, material, denomination, weight, view_count, sales_count)
WHERE u.username = 'seller'
ON CONFLICT DO NOTHING;

-- Sample Orders
-- Order 1: Completed order from buyer to seller
INSERT INTO coin_order.orders (id, order_no, buyer_id, seller_id, status, total_amount, currency, shipping_address, paid_at, completed_at, created_at, updated_at)
SELECT 1, 'ORD20260501000001', bu.id, su.id, 'COMPLETED', 1299.99, 'USD', '123 Main St, New York, NY 10001, USA', '2026-05-01 10:30:00', '2026-05-05 14:00:00', '2026-05-01 10:00:00', '2026-05-05 14:00:00'
FROM coin_users.users bu, coin_users.users su
WHERE bu.username = 'buyer' AND su.username = 'seller'
ON CONFLICT DO NOTHING;

INSERT INTO coin_order.order_items (order_id, product_id, product_title, quantity, unit_price, subtotal)
SELECT 1, id, title, 1, price, price FROM coin_product.products WHERE title = '1895 Morgan Silver Dollar PCGS MS63'
AND NOT EXISTS (SELECT 1 FROM coin_order.order_items WHERE order_id = 1);

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 1, NULL, 'PENDING_PAYMENT', '系统', '订单创建', '2026-05-01 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 1);

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 1, 'PENDING_PAYMENT', 'PAID', '系统', '支付成功', '2026-05-01 10:30:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 1 AND to_status = 'PAID');

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 1, 'PAID', 'SHIPPED', '卖家', '卖家已发货', '2026-05-02 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 1 AND to_status = 'SHIPPED');

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 1, 'SHIPPED', 'COMPLETED', '买家', '买家确认收货', '2026-05-05 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 1 AND to_status = 'COMPLETED');

-- Order 2: Pending payment order
INSERT INTO coin_order.orders (id, order_no, buyer_id, seller_id, status, total_amount, currency, shipping_address, created_at, updated_at)
SELECT 2, 'ORD20260507000002', bu.id, su.id, 'PENDING_PAYMENT', 675.00, 'USD', '123 Main St, New York, NY 10001, USA', '2026-05-07 15:00:00', '2026-05-07 15:00:00'
FROM coin_users.users bu, coin_users.users su
WHERE bu.username = 'buyer' AND su.username = 'seller'
ON CONFLICT DO NOTHING;

INSERT INTO coin_order.order_items (order_id, product_id, product_title, quantity, unit_price, subtotal)
SELECT 2, id, title, 1, price, price FROM coin_product.products WHERE title = '1909-S VDB Lincoln Cent NGC XF40'
AND NOT EXISTS (SELECT 1 FROM coin_order.order_items WHERE order_id = 2);

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 2, NULL, 'PENDING_PAYMENT', '系统', '订单创建', '2026-05-07 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 2);

-- Order 3: Paid but not shipped
INSERT INTO coin_order.orders (id, order_no, buyer_id, seller_id, status, total_amount, currency, shipping_address, paid_at, created_at, updated_at)
SELECT 3, 'ORD20260508000003', bu.id, su.id, 'PAID', 2899.00, 'USD', '123 Main St, New York, NY 10001, USA', '2026-05-08 12:00:00', '2026-05-08 11:30:00', '2026-05-08 12:00:00'
FROM coin_users.users bu, coin_users.users su
WHERE bu.username = 'buyer' AND su.username = 'seller'
ON CONFLICT DO NOTHING;

INSERT INTO coin_order.order_items (order_id, product_id, product_title, quantity, unit_price, subtotal)
SELECT 3, id, title, 1, price, price FROM coin_product.products WHERE title = '2008 China Panda Gold Coin NGC MS70'
AND NOT EXISTS (SELECT 1 FROM coin_order.order_items WHERE order_id = 3);

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 3, NULL, 'PENDING_PAYMENT', '系统', '订单创建', '2026-05-08 11:30:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 3);

INSERT INTO coin_order.order_logs (order_id, from_status, to_status, operator, note, created_at)
SELECT 3, 'PENDING_PAYMENT', 'PAID', '系统', '支付成功', '2026-05-08 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM coin_order.order_logs WHERE order_id = 3 AND to_status = 'PAID');

-- Reset sequence values
SELECT setval('coin_users.users_id_seq', (SELECT MAX(id) FROM coin_users.users));
SELECT setval('coin_product.categories_id_seq', (SELECT MAX(id) FROM coin_product.categories));
SELECT setval('coin_product.products_id_seq', (SELECT MAX(id) FROM coin_product.products));
SELECT setval('coin_order.orders_id_seq', (SELECT MAX(id) FROM coin_order.orders));
