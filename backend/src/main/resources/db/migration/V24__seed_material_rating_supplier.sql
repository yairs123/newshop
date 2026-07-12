-- Seed data for MATERIAL, RATING_COMPANY, SUPPLIER barcode code types

INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, sort_order, is_active) VALUES
('MATERIAL', 'GOLD', 'Gold', '金', 1, true),
('MATERIAL', 'SILVER', 'Silver', '银', 2, true),
('MATERIAL', 'COPPER', 'Copper', '铜', 3, true),
('MATERIAL', 'BRONZE', 'Bronze', '青铜', 4, true),
('MATERIAL', 'NICKEL', 'Nickel', '镍', 5, true),
('MATERIAL', 'PLATINUM', 'Platinum', '铂金', 6, true),
('MATERIAL', 'PALLADIUM', 'Palladium', '钯金', 7, true),
('MATERIAL', 'IRON', 'Iron', '铁', 8, true),
('MATERIAL', 'LEAD', 'Lead', '铅', 9, true),
('MATERIAL', 'BIMETALLIC', 'Bimetallic', '双金属', 10, true),
('MATERIAL', 'ALUMINUM', 'Aluminum', '铝', 11, true),
('MATERIAL', 'TIN', 'Tin', '锡', 12, true);

INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, sort_order, is_active) VALUES
('RATING_COMPANY', 'NGC', 'NGC', 'NGC', 1, true),
('RATING_COMPANY', 'PCGS', 'PCGS', 'PCGS', 2, true),
('RATING_COMPANY', 'PMG', 'PMG', 'PMG', 3, true),
('RATING_COMPANY', 'ANACS', 'ANACS', 'ANACS', 4, true),
('RATING_COMPANY', 'ICG', 'ICG', 'ICG', 5, true),
('RATING_COMPANY', 'CAC', 'CAC', 'CAC', 6, true),
('RATING_COMPANY', 'CNCS', 'CNCS', 'CNCS', 7, true),
('RATING_COMPANY', 'GBCA', 'GBCA / 公博', '公博', 8, true),
('RATING_COMPANY', 'PCGS_CHINA', 'PCGS China', 'PCGS中国', 9, true),
('RATING_COMPANY', 'CNGC', 'CNGC / 中金国衡', '中金国衡', 10, true),
('RATING_COMPANY', 'HXCA', 'HXCA / 华夏评级', '华厦评级', 11, true);
