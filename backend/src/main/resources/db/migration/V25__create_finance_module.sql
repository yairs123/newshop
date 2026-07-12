-- ============================================================
-- V25: 财务模块 - 银行账户、费用管理、人员开支
-- ============================================================

-- 1. 银行账户表
CREATE TABLE coin_finance.bank_accounts (
    id              BIGSERIAL PRIMARY KEY,
    bank_name       VARCHAR(100) NOT NULL,
    account_name    VARCHAR(200) NOT NULL,
    account_number  VARCHAR(50) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'SGD',
    country         VARCHAR(50) NOT NULL,
    current_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    sort_order      INT NOT NULL DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.bank_accounts IS '银行账户管理';
COMMENT ON COLUMN coin_finance.bank_accounts.country IS 'SG=新加坡, MY=马来西亚';
COMMENT ON COLUMN coin_finance.bank_accounts.current_balance IS '当前余额';
COMMENT ON COLUMN coin_finance.bank_accounts.account_number IS '账号（脱敏存储后4位）';

-- 2. 银行转账记录表
CREATE TABLE coin_finance.bank_transfers (
    id              BIGSERIAL PRIMARY KEY,
    from_account_id BIGINT NOT NULL REFERENCES coin_finance.bank_accounts(id),
    to_account_id   BIGINT NOT NULL REFERENCES coin_finance.bank_accounts(id),
    amount          DECIMAL(14,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    fee             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    transfer_date   DATE NOT NULL,
    reference_no    VARCHAR(100),
    description     TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.bank_transfers IS '银行间转账记录';

-- 3. 费用类别表
CREATE TABLE coin_finance.expense_categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    icon            VARCHAR(50),
    color           VARCHAR(7) DEFAULT '#6b7280',
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.expense_categories IS '费用类别（可自定义）';

-- 默认费用类别
INSERT INTO coin_finance.expense_categories (name, icon, color, sort_order) VALUES
('办公用品',     'office',   '#3b82f6', 1),
('办公租金',     'building', '#8b5cf6', 2),
('水电物业',     'bolt',     '#f59e0b', 3),
('物流运费',     'truck',    '#10b981', 4),
('市场推广',     'megaphone','#ef4444', 5),
('税费',         'document', '#6b7280', 6),
('维修保养',     'wrench',   '#f97316', 7),
('通讯网络',     'wifi',     '#06b6d4', 8),
('差旅交通',     'plane',    '#ec4899', 9),
('餐饮招待',     'food',     '#84cc16', 10),
('其他',         'more',     '#9ca3af', 99);

-- 4. 其他费用记录表
CREATE TABLE coin_finance.other_expenses (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT NOT NULL REFERENCES coin_finance.expense_categories(id),
    amount          DECIMAL(14,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'SGD',
    expense_date    DATE NOT NULL,
    description     VARCHAR(500) NOT NULL,
    vendor          VARCHAR(200),
    receipt_url     VARCHAR(500),
    payment_method  VARCHAR(50),
    bank_account_id BIGINT REFERENCES coin_finance.bank_accounts(id),
    created_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.other_expenses IS '其他费用记录';

-- 5. 人员开支记录表
CREATE TABLE coin_finance.personnel_expenses (
    id              BIGSERIAL PRIMARY KEY,
    employee_name   VARCHAR(100) NOT NULL,
    position        VARCHAR(100),
    amount          DECIMAL(14,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'SGD',
    pay_date        DATE NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    notes           TEXT,
    payment_method  VARCHAR(50),
    bank_account_id BIGINT REFERENCES coin_finance.bank_accounts(id),
    created_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.personnel_expenses IS '人员开支/工资发放记录';

-- 6. 报销表新增字段
ALTER TABLE coin_finance.reimbursements
    ADD COLUMN IF NOT EXISTS expense_category_id BIGINT REFERENCES coin_finance.expense_categories(id),
    ADD COLUMN IF NOT EXISTS bank_account_id BIGINT REFERENCES coin_finance.bank_accounts(id);
