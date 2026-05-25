-- Add more test products across all categories
INSERT INTO coin_product.products (id, seller_id, title, description, price, currency, stock, category_id, status, rating_company, rating_number, rating_grade, country, year, material, denomination, weight, view_count, sales_count)
SELECT v.id, u.id, v.title, v.prod_desc, v.price, v.currency, v.stock, v.cat_id, v.status, v.rating_company, v.rating_number, v.rating_grade, v.country, v.year, v.material, v.denomination, v.weight, v.view_count, v.sales_count
FROM coin_users.users u
CROSS JOIN (VALUES
    -- Ancient Coins (cat 1) - more variety
    (13, 'Athenian Owl Tetradrachm NGC AU', 'Classical Greek silver tetradrachm from Athens, c. 454-404 BC. Iconic owl reverse design.', 1850.00, 'USD', 2, 1, 'ACTIVE', 'NGC', 'AU', 'AU', 'Greece', -450, 'Silver', 'Tetradrachm', 17.20, 67, 3),
    (14, 'Alexander the Great Drachm NGC XF', 'Silver drachm of Alexander III, struck at Lampsacus mint. Heracles/Zeus type.', 675.00, 'USD', 4, 1, 'ACTIVE', 'NGC', 'XF', 'XF', 'Greece', -325, 'Silver', 'Drachm', 4.20, 88, 5),
    (15, 'Roman Denarius - Julius Caesar NGC VF', 'Silver denarius of Julius Caesar with elephant reverse, struck 49-48 BC.', 1200.00, 'USD', 1, 1, 'ACTIVE', 'NGC', 'VF', 'VF', 'Roman Empire', -48, 'Silver', 'Denarius', 3.90, 42, 2),
    (16, 'Byzantine Gold Solidus - Justinian I NGC AU', 'Gold solidus of Emperor Justinian I (AD 527-565). Well-struck with full legends.', 2450.00, 'USD', 2, 1, 'ACTIVE', 'NGC', 'AU', 'AU', 'Byzantine', 540, 'Gold', 'Solidus', 4.50, 35, 1),
    (17, 'Ancient Chinese Ban Liang Coin', 'Early Chinese bronze ban liang coin from the Qin Dynasty (221-206 BC). Large heavy cash type.', 195.00, 'USD', 6, 4, 'ACTIVE', NULL, NULL, NULL, 'China', -221, 'Bronze', 'Ban Liang', 8.50, 25, 1),

    -- Gold Coins (cat 2)
    (18, '1915 Austrian 100 Corona Gold NGC MS63', 'Large gold coin featuring Emperor Franz Joseph. Highly sought-after European gold.', 3299.00, 'USD', 2, 2, 'ACTIVE', 'NGC', 'MS63', 'MS63', 'Austria', 1915, 'Gold', '100 Corona', 33.88, 56, 2),
    (19, '1908 Indian Head $10 Gold Eagle PCGS AU55', 'Classic Indian Head design by Augustus Saint-Gaudens. No motto variety.', 1899.00, 'USD', 3, 2, 'ACTIVE', 'PCGS', 'AU55', 'AU55', 'USA', 1908, 'Gold', '$10', 16.72, 73, 4),
    (20, '2012 Canadian Gold Maple Leaf NGC MS70', 'Pure gold bullion coin from the Royal Canadian Mint. Perfect MS70 grade.', 2599.00, 'USD', 8, 2, 'ACTIVE', 'NGC', 'MS70', 'MS70', 'Canada', 2012, 'Gold', '$100', 31.10, 145, 9),
    (21, '1861 Confederate Half Dollar Restrike', 'Silver restrike of the famous Confederate half dollar. Only 500 minted.', 3450.00, 'USD', 1, 3, 'ACTIVE', 'PCGS', 'PR63', 'PR63', 'USA', 1861, 'Silver', '50¢', 12.44, 29, 0),

    -- Silver Coins (cat 3) - more variety
    (22, '1922 Peace Silver Dollar NGC MS65', 'High grade Peace Dollar with exceptional luster and strike.', 599.00, 'USD', 5, 3, 'ACTIVE', 'NGC', 'MS65', 'MS65', 'USA', 1922, 'Silver', '$1', 26.73, 167, 8),
    (23, '1944 Walking Liberty Half Dollar PCGS MS66', 'Gorgeous Walking Liberty half dollar in superb gem condition.', 449.00, 'USD', 3, 3, 'ACTIVE', 'PCGS', 'MS66', 'MS66', 'USA', 1944, 'Silver', '50¢', 12.50, 93, 6),
    (24, '1937 Buffalo Nickel NGC MS65', 'Popular Buffalo nickel with sharp details on horn and tail.', 275.00, 'USD', 4, 3, 'ACTIVE', 'NGC', 'MS65', 'MS65', 'USA', 1937, 'Nickel', '5¢', 5.00, 134, 10),
    (25, '1964 Kennedy Half Dollar PCGS MS67', 'First year Kennedy half dollar in super gem condition.', 225.00, 'USD', 6, 3, 'ACTIVE', 'PCGS', 'MS67', 'MS67', 'USA', 1964, 'Silver', '50¢', 12.50, 188, 14),

    -- Spanish Coins (cat 7)
    (26, '1792 Spanish 8 Reales NGC AU55', 'Silver 8 reales "Piece of Eight" from Charles IV reign. Famous colonial coin.', 895.00, 'USD', 3, 7, 'ACTIVE', 'NGC', 'AU55', 'AU55', 'Spain', 1792, 'Silver', '8 Reales', 27.07, 56, 3),
    (27, '1715 Fleet Spanish Cob 8 Reales NGC VF', 'Silver cob from the legendary 1715 Treasure Fleet. Attractive cross reverse.', 1450.00, 'USD', 1, 7, 'ACTIVE', 'NGC', 'VF', 'VF', 'Spain', 1715, 'Silver', '8 Reales', 27.00, 89, 2),
    (28, '1808 Spanish 2 Reales Ferdinand VII NGC AU', 'Silver 2 reales from the reign of Ferdinand VII. Barcelona mint.', 245.00, 'USD', 5, 7, 'ACTIVE', 'NGC', 'AU', 'AU', 'Spain', 1808, 'Silver', '2 Reales', 6.77, 42, 4),

    -- Germany Coins (cat 8)
    (29, '1913 German 20 Mark Wilhelm II NGC MS62', 'Gold 20 mark coin of Kaiser Wilhelm II. Prussia issue.', 1895.00, 'USD', 2, 8, 'ACTIVE', 'NGC', 'MS62', 'MS62', 'Germany', 1913, 'Gold', '20 Mark', 7.96, 45, 2),
    (30, '1876 German 5 Mark NGC AU58', 'Silver 5 mark of the German Empire. Beautiful old toning.', 399.00, 'USD', 3, 8, 'ACTIVE', 'NGC', 'AU58', 'AU58', 'Germany', 1876, 'Silver', '5 Mark', 27.78, 38, 1),
    (31, '2002 German 10 Euro NGC MS69', 'Modern German 10 euro commemorative coin. First year of euro coinage.', 49.00, 'USD', 15, 8, 'ACTIVE', 'NGC', 'MS69', 'MS69', 'Germany', 2002, 'Silver', '10 Euro', 18.00, 22, 0),

    -- France Coins (cat 9)
    (32, '1815 Napoleon 5 Francs NGC AU', 'Silver 5 francs from Napoleon I. La Guilloche design. Waterloo year issue.', 675.00, 'USD', 2, 9, 'ACTIVE', 'NGC', 'AU', 'AU', 'France', 1815, 'Silver', '5 Francs', 25.00, 67, 3),
    (33, '1858 Napoleon III 100 Franc Gold NGC AU55', 'Large gold 100 franc coin of Napoleon III. Impressive 32mm diameter.', 3299.00, 'USD', 1, 9, 'ACTIVE', 'NGC', 'AU55', 'AU55', 'France', 1858, 'Gold', '100 Francs', 32.26, 31, 1),
    (34, '1902 French 2 Franc NGC MS64', 'Silver 2 franc with classic Cérès head design. Nice lustrous example.', 89.00, 'USD', 8, 9, 'ACTIVE', 'NGC', 'MS64', 'MS64', 'France', 1902, 'Silver', '2 Francs', 10.00, 18, 0),

    -- Britain Coins (cat 10)
    (35, '1911 British Gold Sovereign NGC MS64', 'Gold sovereign of George V. Struck at London mint. Saint George reverse.', 895.00, 'USD', 4, 10, 'ACTIVE', 'NGC', 'MS64', 'MS64', 'United Kingdom', 1911, 'Gold', '1 Sovereign', 7.99, 112, 7),
    (36, '1894 British Trade Dollar NGC AU55', 'Silver trade dollar used in the Far East. Britannia reverse design.', 549.00, 'USD', 2, 10, 'ACTIVE', 'NGC', 'AU55', 'AU55', 'United Kingdom', 1894, 'Silver', '1 Trade Dollar', 26.96, 45, 2),
    (37, '1953 British Coronation Crown NGC MS65', 'Silver crown of Queen Elizabeth II coronation year. Regal crown reverse.', 299.00, 'USD', 6, 10, 'ACTIVE', 'NGC', 'MS65', 'MS65', 'United Kingdom', 1953, 'Silver', '5 Shillings', 28.28, 78, 5),

    -- USA Coins (cat 11)
    (38, '1881-CC Morgan Silver Dollar NGC MS63', 'Carson City mint Morgan dollar. Low mintage issue with strong collector demand.', 1299.00, 'USD', 3, 11, 'ACTIVE', 'NGC', 'MS63', 'MS63', 'USA', 1881, 'Silver', '$1', 26.73, 156, 6),
    (39, '1915-S Pan-Pac Gold $50 Round NGC AU55', 'Commemorative gold $50 from the 1915 Panama-Pacific Exposition. Round type.', 12999.00, 'USD', 1, 11, 'ACTIVE', 'NGC', 'AU55', 'AU55', 'USA', 1915, 'Gold', '$50', 83.59, 23, 0),
    (40, '1799 Draped Bust Silver Dollar PCGS VF30', 'Early US silver dollar with flowing hair. Bold Eagle reverse.', 8950.00, 'USD', 1, 11, 'ACTIVE', 'PCGS', 'VF30', 'VF30', 'USA', 1799, 'Silver', '$1', 26.96, 41, 1),
    (41, '1917 Standing Liberty Quarter PCGS MS65', 'Full head Standing Liberty Quarter. First year of type 2 design.', 1249.00, 'USD', 2, 11, 'ACTIVE', 'PCGS', 'MS65', 'MS65', 'USA', 1917, 'Silver', '25¢', 6.25, 89, 4),

    -- Bullion Coin (cat 12)
    (42, '2024 American Silver Eagle BU', 'Modern silver bullion coin from US Mint. In original mint tube of 20.', 45.00, 'USD', 100, 12, 'ACTIVE', NULL, NULL, NULL, 'USA', 2024, 'Silver', '$1', 31.10, 567, 45),
    (43, '2024 Austrian Silver Philharmonic BU', 'Popular silver bullion coin with Vienna Philharmonic Hall organ design.', 42.00, 'USD', 80, 12, 'ACTIVE', NULL, NULL, NULL, 'Austria', 2024, 'Silver', '€1.50', 31.10, 345, 28),
    (44, '2024 Gold Maple Leaf 1 oz BU', 'Pure 9999 gold bullion coin from Royal Canadian Mint.', 2699.00, 'USD', 25, 12, 'ACTIVE', NULL, NULL, NULL, 'Canada', 2024, 'Gold', '$100', 31.10, 234, 12),
    (45, '2024 Silver Britannia 1 oz BU', 'Silver bullion coin from Royal Mint. New security features.', 44.00, 'USD', 90, 12, 'ACTIVE', NULL, NULL, NULL, 'United Kingdom', 2024, 'Silver', '£2', 31.21, 289, 21),

    -- Euros Coins (cat 13)
    (46, '2002 France 2 Euro Commemorative UNC', 'First year French 2 euro coin in uncirculated condition.', 12.00, 'USD', 30, 13, 'ACTIVE', NULL, NULL, NULL, 'France', 2002, 'Bimetallic', '2 Euro', 8.50, 56, 5),
    (47, '2004 Greece Olympic 2 Euro UNC', 'Greek 2 euro commemorative celebrating the Athens 2004 Olympic Games.', 18.00, 'USD', 20, 13, 'ACTIVE', NULL, NULL, NULL, 'Greece', 2004, 'Bimetallic', '2 Euro', 8.50, 45, 3),
    (48, '2012 Vatican Euro Set BU', 'Complete Vatican euro coin set in original card. Scarce Vatican issue.', 89.00, 'USD', 5, 13, 'ACTIVE', NULL, NULL, NULL, 'Vatican', 2012, 'Various', 'Various', 0, 34, 2),
    (49, '2015 Luxembourg Grand Duke Henri 2 Euro', 'Luxembourg 2 euro coin featuring Grand Duke Henri. Low mintage.', 15.00, 'USD', 25, 13, 'ACTIVE', NULL, NULL, NULL, 'Luxembourg', 2015, 'Bimetallic', '2 Euro', 8.50, 22, 1),

    -- Medal (cat 14)
    (50, 'US Army Distinguished Service Cross Medal', 'Bronze medal with eagle design. Full size with ribbon.', 295.00, 'USD', 3, 14, 'ACTIVE', NULL, NULL, NULL, 'USA', 1945, 'Bronze', NULL, 0, 12, 0),
    (51, 'Papal Medal - Pope John Paul II Silver', 'Silver papal medal commemorating the 1979 visit to the United States.', 125.00, 'USD', 8, 14, 'ACTIVE', 'NGC', 'MS66', 'MS66', 'Vatican', 1979, 'Silver', NULL, 18.00, 28, 1),
    (52, '1904 St. Louis World Fair Bronze Medal', 'Large bronze exposition medal. 60mm diameter with allegorical designs.', 175.00, 'USD', 2, 14, 'ACTIVE', NULL, NULL, NULL, 'USA', 1904, 'Bronze', NULL, 0, 15, 0),
    (53, 'Coronation Medal - Queen Elizabeth II 1953', 'Silver coronation medal issued by the Royal Mint. Original case.', 89.00, 'USD', 10, 14, 'ACTIVE', NULL, NULL, NULL, 'United Kingdom', 1953, 'Silver', NULL, 15.00, 45, 3),

    -- Banknotes (cat 15)
    (54, '1928 US $1 Silver Certificate PMG 65', 'Silver certificate with blue seal. Popular horse blanket note.', 89.00, 'USD', 5, 15, 'ACTIVE', 'PMG', '65', '65', 'USA', 1928, 'Paper', '$1', 0, 78, 6),
    (55, '1914 US $50 Federal Reserve Note PMG 30', 'Large size $50 note from the first series of Federal Reserve notes.', 899.00, 'USD', 2, 15, 'ACTIVE', 'PMG', '30', '30', 'USA', 1914, 'Paper', '$50', 0, 34, 1),
    (56, '1990 100-Deutsche Mark Note PMG 67', 'German 100 Mark note featuring composer Carl Maria von Weber.', 149.00, 'USD', 8, 15, 'ACTIVE', 'PMG', '67', '67', 'Germany', 1990, 'Paper', '100 Mark', 0, 23, 0),
    (57, '1961 100 Franc Note PMG 64', 'French 100 franc note featuring Napoleon Bonaparte. Pre-euro design.', 89.00, 'USD', 6, 15, 'ACTIVE', 'PMG', '64', '64', 'France', 1961, 'Paper', '100 Francs', 0, 19, 0),

    -- Accessories Numismatic (cat 16)
    (58, 'NGC Standard Coin Holder Box (25-pack)', 'Official NGC plastic holders for encapsulated coins. Fits standard slabs.', 24.99, 'USD', 50, 16, 'ACTIVE', NULL, NULL, NULL, 'USA', 2024, 'Plastic', NULL, 0, 89, 12),
    (59, 'Lighthouse 60-Coin Album Pages', 'Premium acid-free coin album pages by Lighthouse. Holds 60 coins per page.', 19.95, 'USD', 30, 16, 'ACTIVE', NULL, NULL, NULL, 'Germany', 2024, 'Paper', NULL, 0, 56, 8),
    (60, 'Professional Coin Loupe 10x', 'High-quality 10x magnification loupe with LED light for coin examination.', 34.99, 'USD', 40, 16, 'ACTIVE', NULL, NULL, NULL, 'Japan', 2024, 'Metal', NULL, 0, 112, 15),
    (61, 'Coin Grading Gloves (Cotton, 12-pack)', 'Soft white cotton gloves for safe coin handling. Lint-free.', 9.99, 'USD', 100, 16, 'ACTIVE', NULL, NULL, NULL, 'Vietnam', 2024, 'Cotton', NULL, 0, 67, 23)
) AS v(id, title, prod_desc, price, currency, stock, cat_id, status, rating_company, rating_number, rating_grade, country, year, material, denomination, weight, view_count, sales_count)
WHERE u.username = 'seller'
ON CONFLICT DO NOTHING;

-- Reset sequence
SELECT setval('coin_product.products_id_seq', (SELECT MAX(id) FROM coin_product.products));
