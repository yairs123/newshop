-- ============================================================
-- V17: Audit trail, inventory management, barcode coding tables
-- ============================================================

CREATE SCHEMA IF NOT EXISTS coin_admin;
CREATE SCHEMA IF NOT EXISTS coin_finance;

-- ============================================================
-- 1. Global audit log for all entity changes
-- ============================================================
CREATE TABLE coin_admin.audit_log (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50)   NOT NULL,   -- 'PRODUCT','USER','ORDER','SELLER'
    entity_id       BIGINT        NOT NULL,
    operation       VARCHAR(20)   NOT NULL,   -- 'CREATE','UPDATE','DELETE','STATUS_CHANGE'
    field_name      VARCHAR(100),              -- NULL for CREATE/DELETE
    old_value       TEXT,
    new_value       TEXT,
    operator_id     BIGINT,
    operator_name   VARCHAR(100),
    change_summary  VARCHAR(500),              -- "Changed price from 50 to 80"
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_entity     ON coin_admin.audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_operator   ON coin_admin.audit_log(operator_id);
CREATE INDEX idx_audit_created_at ON coin_admin.audit_log(created_at);

-- ============================================================
-- 2. Barcode coding system (user-maintainable code tables)
-- ============================================================
CREATE TABLE coin_admin.barcode_codes (
    id              BIGSERIAL PRIMARY KEY,
    code_type       VARCHAR(20)   NOT NULL,   -- 'COUNTRY','CATEGORY','DENOM','ERA','GRADE'
    code_value      VARCHAR(10)   NOT NULL,   -- e.g. '840', '1', '001'
    label_en        VARCHAR(200),
    label_zh        VARCHAR(200),
    parent_type     VARCHAR(20),               -- parent hierarchy
    parent_value    VARCHAR(10),
    sort_order      INT           DEFAULT 0,
    is_active       BOOLEAN       DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_barcode_type ON coin_admin.barcode_codes(code_type, code_value);
CREATE INDEX idx_barcode_parent ON coin_admin.barcode_codes(parent_type, parent_value);

-- Seed initial country codes (ISO 3166-1 numeric)
INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, sort_order) VALUES
('COUNTRY', '840', 'USA', '美国', 1),
('COUNTRY', '156', 'China', '中国', 2),
('COUNTRY', '826', 'United Kingdom', '英国', 3),
('COUNTRY', '392', 'Japan', '日本', 4),
('COUNTRY', '124', 'Canada', '加拿大', 5),
('COUNTRY', '250', 'France', '法国', 6),
('COUNTRY', '276', 'Germany', '德国', 7),
('COUNTRY', '380', 'Italy', '意大利', 8),
('COUNTRY', '724', 'Spain', '西班牙', 9),
('COUNTRY', '036', 'Australia', '澳大利亚', 10),
('COUNTRY', '756', 'Switzerland', '瑞士', 11),
('COUNTRY', '203', 'Czech Republic', '捷克', 12),
('COUNTRY', '528', 'Netherlands', '荷兰', 13),
('COUNTRY', '344', 'Hong Kong', '香港', 14),
('COUNTRY', '158', 'Taiwan', '台湾', 15),
('COUNTRY', '410', 'South Korea', '韩国', 16),
('COUNTRY', '764', 'Thailand', '泰国', 17),
('COUNTRY', '704', 'Vietnam', '越南', 18),
('COUNTRY', '702', 'Singapore', '新加坡', 19),
('COUNTRY', '458', 'Malaysia', '马来西亚', 20),
('COUNTRY', '360', 'Indonesia', '印度尼西亚', 21),
('COUNTRY', '608', 'Philippines', '菲律宾', 22),
('COUNTRY', '784', 'UAE', '阿联酋', 23),
('COUNTRY', '682', 'Saudi Arabia', '沙特阿拉伯', 24),
('COUNTRY', '376', 'Israel', '以色列', 25),
('COUNTRY', '643', 'Russia', '俄罗斯', 26),
('COUNTRY', '040', 'Austria', '奥地利', 27),
('COUNTRY', '056', 'Belgium', '比利时', 28),
('COUNTRY', '100', 'Bulgaria', '保加利亚', 29),
('COUNTRY', '191', 'Croatia', '克罗地亚', 30),
('COUNTRY', '208', 'Denmark', '丹麦', 31),
('COUNTRY', '233', 'Estonia', '爱沙尼亚', 32),
('COUNTRY', '246', 'Finland', '芬兰', 33),
('COUNTRY', '300', 'Greece', '希腊', 34),
('COUNTRY', '348', 'Hungary', '匈牙利', 35),
('COUNTRY', '372', 'Ireland', '爱尔兰', 36),
('COUNTRY', '428', 'Latvia', '拉脱维亚', 37),
('COUNTRY', '440', 'Lithuania', '立陶宛', 38),
('COUNTRY', '484', 'Mexico', '墨西哥', 39),
('COUNTRY', '554', 'New Zealand', '新西兰', 40),
('COUNTRY', '578', 'Norway', '挪威', 41),
('COUNTRY', '586', 'Pakistan', '巴基斯坦', 42),
('COUNTRY', '604', 'Peru', '秘鲁', 43),
('COUNTRY', '616', 'Poland', '波兰', 44),
('COUNTRY', '620', 'Portugal', '葡萄牙', 45),
('COUNTRY', '642', 'Romania', '罗马尼亚', 46),
('COUNTRY', '688', 'Serbia', '塞尔维亚', 47),
('COUNTRY', '703', 'Slovakia', '斯洛伐克', 48),
('COUNTRY', '705', 'Slovenia', '斯洛文尼亚', 49),
('COUNTRY', '710', 'South Africa', '南非', 50),
('COUNTRY', '752', 'Sweden', '瑞典', 51),
('COUNTRY', '792', 'Turkey', '土耳其', 52),
('COUNTRY', '804', 'Ukraine', '乌克兰', 53),
('COUNTRY', '858', 'Uruguay', '乌拉圭', 54),
('COUNTRY', '032', 'Argentina', '阿根廷', 55),
('COUNTRY', '076', 'Brazil', '巴西', 56),
('COUNTRY', '152', 'Chile', '智利', 57),
('COUNTRY', '170', 'Colombia', '哥伦比亚', 58),
('COUNTRY', '818', 'Egypt', '埃及', 59),
('COUNTRY', '051', 'Armenia', '亚美尼亚', 60),
('COUNTRY', '268', 'Georgia', '格鲁吉亚', 61),
('COUNTRY', '398', 'Kazakhstan', '哈萨克斯坦', 62),
('COUNTRY', '496', 'Mongolia', '蒙古', 63);

-- Category codes (major categories)
INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, sort_order) VALUES
('CATEGORY', '1', 'Minted Coin / 机制币', '机制币', 1),
('CATEGORY', '2', 'Ancient Coin / 古钱币', '古钱币', 2),
('CATEGORY', '3', 'Gold/Silver Ingot / 金银锭', '金银锭', 3),
('CATEGORY', '4', 'Modern Coin / 现代币章', '现代币章', 4),
('CATEGORY', '5', 'Banknote / 纸币', '纸币', 5),
('CATEGORY', '6', 'Other / 其他', '其他', 6);

-- Era codes for ancient coins
INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, parent_type, parent_value, sort_order) VALUES
('ERA', '101', 'Qing - Kangxi', '清·康熙', 'CATEGORY', '2', 1),
('ERA', '102', 'Qing - Yongzheng', '清·雍正', 'CATEGORY', '2', 2),
('ERA', '103', 'Qing - Qianlong', '清·乾隆', 'CATEGORY', '2', 3),
('ERA', '104', 'Qing - Jiaqing', '清·嘉庆', 'CATEGORY', '2', 4),
('ERA', '105', 'Qing - Xianfeng', '清·咸丰', 'CATEGORY', '2', 5),
('ERA', '106', 'Qing - Guangxu', '清·光绪', 'CATEGORY', '2', 6),
('ERA', '107', 'Qing - Xuantong', '清·宣统', 'CATEGORY', '2', 7),
('ERA', '201', 'Ming - Hongwu', '明·洪武', 'CATEGORY', '2', 8),
('ERA', '202', 'Ming - Yongle', '明·永乐', 'CATEGORY', '2', 9),
('ERA', '203', 'Ming - Wanli', '明·万历', 'CATEGORY', '2', 10),
('ERA', '301', 'Song - Huizong', '宋·徽宗', 'CATEGORY', '2', 11),
('ERA', '302', 'Song - Zhenzong', '宋·真宗', 'CATEGORY', '2', 12),
('ERA', '303', 'Song - Northern Song', '北宋·其他', 'CATEGORY', '2', 13),
('ERA', '304', 'Song - Southern Song', '南宋', 'CATEGORY', '2', 14),
('ERA', '401', 'Tang - Kaiyuan', '唐·开元', 'CATEGORY', '2', 15),
('ERA', '501', 'Han - Wuzhu', '汉·五铢', 'CATEGORY', '2', 16),
('ERA', '601', 'Pre-Qin / 先秦', '先秦', 'CATEGORY', '2', 17),
('ERA', '701', 'Three Kingdoms', '三国', 'CATEGORY', '2', 18),
('ERA', '801', 'Sui', '隋', 'CATEGORY', '2', 19),
('ERA', '901', 'Yuan', '元', 'CATEGORY', '2', 20),
('ERA', '100', 'Qing (general)', '清代（通用）', 'CATEGORY', '2', 21),
('ERA', '200', 'Ming (general)', '明代（通用）', 'CATEGORY', '2', 22),
('ERA', '300', 'Song (general)', '宋代（通用）', 'CATEGORY', '2', 23);

-- Denomination/variety codes for minted coins (CATEGORY=1)
INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, parent_type, parent_value, sort_order) VALUES
('DENOM', '001', 'Morgan $1', '摩根1元', 'COUNTRY', '840', 1),
('DENOM', '002', 'Peace $1', '和平1元', 'COUNTRY', '840', 2),
('DENOM', '003', '$20 Double Eagle', '双鹰金币', 'COUNTRY', '840', 3),
('DENOM', '004', '$10 Eagle', '鹰金币', 'COUNTRY', '840', 4),
('DENOM', '005', '$5 Half Eagle', '半鹰金币', 'COUNTRY', '840', 5),
('DENOM', '006', '$2.50 Quarter Eagle', '四分之一鹰', 'COUNTRY', '840', 6),
('DENOM', '007', 'Washington Quarter', '华盛顿25¢', 'COUNTRY', '840', 7),
('DENOM', '008', 'Mercury Dime', '墨丘利10¢', 'COUNTRY', '840', 8),
('DENOM', '009', 'Lincoln Cent', '林肯1¢', 'COUNTRY', '840', 9),
('DENOM', '010', 'Jefferson Nickel', '杰斐逊5¢', 'COUNTRY', '840', 10),
('DENOM', '011', 'Roosevelt Dime', '罗斯福10¢', 'COUNTRY', '840', 11),
('DENOM', '012', 'Kennedy Half', '肯尼迪50¢', 'COUNTRY', '840', 12),
('DENOM', '013', 'Walking Liberty Half', '行走自由女神50¢', 'COUNTRY', '840', 13),
('DENOM', '020', 'Trade Dollar', '贸易银元', 'COUNTRY', '840', 14),
-- China varieties
('DENOM', '001', 'Yuan Shih-kai Dollar / 袁大头', '袁大头壹圆', 'COUNTRY', '156', 1),
('DENOM', '002', 'Dragon Dollar / 龙洋', '龙洋壹圆', 'COUNTRY', '156', 2),
('DENOM', '003', 'Sun Yat-sen Dollar / 孙小头', '孙像壹圆', 'COUNTRY', '156', 3),
('DENOM', '004', '20 Cents / 贰角', '贰角', 'COUNTRY', '156', 4),
('DENOM', '005', '10 Cents / 壹角', '壹角', 'COUNTRY', '156', 5),
('DENOM', '006', '50 Cents / 半圆', '半圆', 'COUNTRY', '156', 6),
-- UK
('DENOM', '001', 'Sovereign', ' sovereign金币', 'COUNTRY', '826', 1),
('DENOM', '002', 'Half Sovereign', '半 sovereign', 'COUNTRY', '826', 2),
('DENOM', '003', 'Crown', '克朗', 'COUNTRY', '826', 3),
('DENOM', '010', 'Penny', '便士', 'COUNTRY', '826', 4),
-- Japan
('DENOM', '001', 'Trade Dollar / 貿易銀', '贸易银', 'COUNTRY', '392', 1),
('DENOM', '002', '1 Yen / 壹円', '壹円银币', 'COUNTRY', '392', 2),
('DENOM', '003', '50 Sen / 五十銭', '五十銭', 'COUNTRY', '392', 3),
('DENOM', '004', '20 Yen / 二十円', '二十円金币', 'COUNTRY', '392', 4);

-- Grade codes
INSERT INTO coin_admin.barcode_codes (code_type, code_value, label_en, label_zh, sort_order) VALUES
('GRADE', '70', 'MS70 / PF70', 'MS70 / PF70', 1),
('GRADE', '69', 'MS69 / PF69', 'MS69 / PF69', 2),
('GRADE', '68', 'MS68 / PF68', 'MS68 / PF68', 3),
('GRADE', '67', 'MS67 / PF67', 'MS67 / PF67', 4),
('GRADE', '66', 'MS66 / PF66', 'MS66 / PF66', 5),
('GRADE', '65', 'MS65', 'MS65', 6),
('GRADE', '64', 'MS64', 'MS64', 7),
('GRADE', '63', 'MS63', 'MS63', 8),
('GRADE', '62', 'MS62', 'MS62', 9),
('GRADE', '61', 'MS61', 'MS61', 10),
('GRADE', '60', 'MS60', 'MS60', 11),
('GRADE', '58', 'AU58', 'AU58', 12),
('GRADE', '55', 'AU55', 'AU55', 13),
('GRADE', '53', 'AU53', 'AU53', 14),
('GRADE', '50', 'AU50', 'AU50', 15),
('GRADE', '45', 'XF45', 'XF45', 16),
('GRADE', '40', 'XF40', 'XF40', 17),
('GRADE', '35', 'VF35', 'VF35', 18),
('GRADE', '30', 'VF30', 'VF30', 19),
('GRADE', '25', 'VF25', 'VF25', 20),
('GRADE', '20', 'VF20', 'VF20', 21),
('GRADE', '12', 'F12', 'F12', 22),
('GRADE', '08', 'VG8', 'VG8', 23),
('GRADE', '03', 'AG3', 'AG3', 24),
('GRADE', '01', 'PO1', 'PO1', 25),
('GRADE', '00', 'Uncertified / 无评级', '无评级', 26),
-- Ancient coin grades
('GRADE', '01', 'Extremely Fine / 极美品', '极美品', 27),
('GRADE', '02', 'Very Fine / 上美品', '上美品', 28),
('GRADE', '03', 'Fine / 美品', '美品', 29),
('GRADE', '04', 'Good / 普品', '普品', 30),
('GRADE', '05', 'Poor / 差品', '差品', 31);

-- ============================================================
-- 3. Inventory batches (track each purchase batch)
-- ============================================================
CREATE TABLE coin_admin.inventory_batches (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT        NOT NULL REFERENCES coin_product.products(id),
    quantity        INT           NOT NULL DEFAULT 1,
    purchase_price  DECIMAL(12,2) NOT NULL,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    supplier        VARCHAR(200),
    invoice_no      VARCHAR(100),
    batch_date      DATE          NOT NULL,
    notes           TEXT,
    receipt_image   VARCHAR(500),
    operator_id     BIGINT,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_inv_batch_product ON coin_admin.inventory_batches(product_id);
CREATE INDEX idx_inv_batch_date    ON coin_admin.inventory_batches(batch_date);

-- ============================================================
-- 4. Alter products table for inventory + cost tracking
-- ============================================================
ALTER TABLE coin_product.products ADD COLUMN IF NOT EXISTS purchase_price  DECIMAL(12,2);
ALTER TABLE coin_product.products ADD COLUMN IF NOT EXISTS purchase_currency VARCHAR(3) DEFAULT 'USD';
ALTER TABLE coin_product.products ADD COLUMN IF NOT EXISTS supplier        VARCHAR(200);
ALTER TABLE coin_product.products ADD COLUMN IF NOT EXISTS source_invoice  VARCHAR(100);
ALTER TABLE coin_product.products ADD COLUMN IF NOT EXISTS sale_qty        INT DEFAULT 0;

-- ============================================================
-- 5. Reimbursements table
-- ============================================================
CREATE TABLE coin_finance.reimbursements (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200)  NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    category        VARCHAR(50)   NOT NULL,
    description     TEXT,
    receipt_url     VARCHAR(500),
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    submitter_id    BIGINT,
    approver_id     BIGINT,
    approved_at     TIMESTAMP,
    reject_reason   TEXT,
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_reimb_status    ON coin_finance.reimbursements(status);
CREATE INDEX idx_reimb_submitter ON coin_finance.reimbursements(submitter_id);
