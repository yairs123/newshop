# 财务管理模块设计文档

> **日期:** 2026-06-07
> **版本:** v1.0
> **状态:** 待实现

## 1. 概述

### 1.1 目标

重构并增强 Coin Marketplace 管理后台的财务管理模块，解决现有模块功能分散、界面简陋、缺乏可视化的问题。

### 1.2 核心需求

| 模块 | 说明 | 优先级 |
|------|------|--------|
| 财务总览仪表盘 | 图表化展示收入/支出/利润趋势 | P0 |
| 银行账户管理 | 多银行余额、转账记录 | P0 |
| 收款明细（销售收入） | 已完成订单的收入流水 | P0 |
| 进货支出 | 采购成本明细（已有，增强） | P0 |
| 人员开支 | 简单工资发放记录 | P1 |
| 报销管理 | 已有功能增强 | P1 |
| 其他费用 | 分类汇总 + 明细记录 | P1 |
| 打印/PDF导出 | 每个子页面支持打印 | P0 |

### 1.3 设计原则

- **打印友好**: 每个子报表页面可以独立打印/导出 PDF
- **可视化**: 关键指标用图表呈现，不用盯着数字看
- **可扩展**: 费用类别、银行账户支持用户自定义增删
- **一致性**: 保持与现有 Element Plus 后台风格统一

## 2. 系统架构

### 2.1 导航结构

```
财务管理 (左侧菜单)
├── 📊 财务总览        → /finance              ← 仪表盘（主入口）
├── 💳 银行管理        → /finance/banks        ← 银行账户 + 转账
├── 📥 收款明细        → /finance/income       ← 销售收入流水
├── 📤 进货支出        → /finance/purchases    ← 采购成本
├── 👥 人员开支        → /finance/personnel    ← 工资发放
├── 🧾 报销管理        → /finance/reimbursements ← 员工报销
├── 📋 其他费用        → /finance/expenses     ← 分类费用
└── 📈 利润报表        → /finance/profit       ← 收入/支出对比
```

### 2.2 页面局部布局

每个子页面右上角有操作工具栏:

```
[页面标题]                               [📅 日期筛选] [🖨️ 打印/PDF] [➕ 新增]
```

打印时隐藏侧边栏和顶部导航，仅保留内容区域。

### 2.3 前端技术选型

- **图表库**: ECharts（通过 `echarts` + `vue-echarts`）
  - 柱状图: 月度收入/支出/利润趋势
  - 饼图: 费用构成分析
  - 折线图: 利润趋势
- **打印方案**: 使用 `print-js` + 自定义 `@media print` CSS
- **UI 框架**: Element Plus（维持现有统一风格）

## 3. 数据库设计

### 3.1 新增表: 银行账户 (bank_accounts)

```sql
CREATE SCHEMA IF NOT EXISTS coin_finance;

CREATE TABLE coin_finance.bank_accounts (
    id              BIGSERIAL PRIMARY KEY,
    bank_name       VARCHAR(100) NOT NULL,       -- 银行名称: DBS, OCBC, Maybank etc
    account_name    VARCHAR(200) NOT NULL,        -- 账户名称/户名
    account_number  VARCHAR(50) NOT NULL,         -- 账号（脱敏存储后4位）
    currency        VARCHAR(3) NOT NULL DEFAULT 'SGD',  -- SGD/MYR/USD
    country         VARCHAR(50) NOT NULL,         -- SG/MY
    current_balance DECIMAL(14,2) NOT NULL DEFAULT 0.00, -- 当前余额
    is_active       BOOLEAN NOT NULL DEFAULT true,
    sort_order      INT NOT NULL DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.bank_accounts IS '银行账户管理';
COMMENT ON COLUMN coin_finance.bank_accounts.country IS 'SG=新加坡, MY=马来西亚';
```

### 3.2 新增表: 银行转账记录 (bank_transfers)

```sql
CREATE TABLE coin_finance.bank_transfers (
    id              BIGSERIAL PRIMARY KEY,
    from_account_id BIGINT NOT NULL REFERENCES coin_finance.bank_accounts(id),
    to_account_id   BIGINT NOT NULL REFERENCES coin_finance.bank_accounts(id),
    amount          DECIMAL(14,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    fee             DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- 转账手续费
    transfer_date   DATE NOT NULL,
    reference_no    VARCHAR(100),            -- 交易参考号
    description     TEXT,
    created_by      BIGINT,                  -- 操作人
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coin_finance.bank_transfers IS '银行间转账记录';
```

### 3.3 新增表: 费用类别 (expense_categories)

```sql
CREATE TABLE coin_finance.expense_categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,         -- 类别名称
    icon            VARCHAR(50),                   -- 图标标识
    color           VARCHAR(7) DEFAULT '#6b7280',  -- 显示颜色
    sort_order      INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

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
```

### 3.4 新增表: 其他费用记录 (other_expenses)

```sql
CREATE TABLE coin_finance.other_expenses (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT NOT NULL REFERENCES coin_finance.expense_categories(id),
    amount          DECIMAL(14,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'SGD',
    expense_date    DATE NOT NULL,
    description     VARCHAR(500) NOT NULL,
    vendor          VARCHAR(200),                  -- 供应商/收款方
    receipt_url     VARCHAR(500),                  -- 票据图片
    payment_method  VARCHAR(50),                   -- 支付方式
    bank_account_id BIGINT REFERENCES coin_finance.bank_accounts(id), -- 从哪个银行支付
    created_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 3.5 新增表: 人员开支记录 (personnel_expenses)

```sql
CREATE TABLE coin_finance.personnel_expenses (
    id              BIGSERIAL PRIMARY KEY,
    employee_name   VARCHAR(100) NOT NULL,         -- 员工姓名
    position        VARCHAR(100),                  -- 岗位
    amount          DECIMAL(14,2) NOT NULL,        -- 实发金额
    currency        VARCHAR(3) NOT NULL DEFAULT 'SGD',
    pay_date        DATE NOT NULL,                 -- 发薪日
    period_start    DATE NOT NULL,                 -- 薪资周期开始
    period_end      DATE NOT NULL,                 -- 薪资周期结束
    notes           TEXT,
    payment_method  VARCHAR(50),
    bank_account_id BIGINT REFERENCES coin_finance.bank_accounts(id),
    created_by      BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### 3.6 已有表增强: 报销表新增银行关联

在 `coin_finance.reimbursements` 表新增字段（ALTER TABLE）:

```sql
ALTER TABLE coin_finance.reimbursements
    ADD COLUMN IF NOT EXISTS expense_category_id BIGINT REFERENCES coin_finance.expense_categories(id),
    ADD COLUMN IF NOT EXISTS bank_account_id BIGINT REFERENCES coin_finance.bank_accounts(id);
```

## 4. 后端 API 设计

### 4.1 银行管理

```
GET    /api/admin/finance/banks                    ← 获取银行列表（含余额）
POST   /api/admin/finance/banks                    ← 新增银行账户
PUT    /api/admin/finance/banks/{id}               ← 编辑银行账户
DELETE /api/admin/finance/banks/{id}               ← 删除银行账户（有交易记录不能删）
POST   /api/admin/finance/banks/{id}/adjust        ← 手动调整余额（入账/出账）

GET    /api/admin/finance/banks/{id}/transactions  ← 银行流水（分页）
POST   /api/admin/finance/banks/transfer           ← 银行间转账
GET    /api/admin/finance/banks/transfers          ← 转账记录列表
```

### 4.2 财务总览

```
GET    /api/admin/finance/overview                 ← 总览仪表盘数据
```

**返回示例:**
```json
{
  "totalIncome": 128450.00,
  "totalExpenses": 89200.00,
  "netProfit": 39250.00,
  "profitMargin": 30.56,
  "totalBankBalance": 215000.00,
  "monthlyTrend": [
    { "month": "2026-01", "income": 21000, "expenses": 14000, "profit": 7000 },
    { "month": "2026-02", "income": 18500, "expenses": 15200, "profit": 3300 }
  ],
  "expenseBreakdown": [
    { "category": "进货成本", "amount": 52000, "percentage": 58.3 },
    { "category": "人员开支", "amount": 18000, "percentage": 20.2 },
    { "category": "其他费用", "amount": 19200, "percentage": 21.5 }
  ],
  "bankBreakdown": [
    { "bankName": "DBS", "balance": 85000, "currency": "SGD" },
    { "bankName": "OCBC", "balance": 62000, "currency": "SGD" },
    { "bankName": "Maybank", "balance": 68000, "currency": "MYR" }
  ]
}
```

### 4.3 费用管理

```
GET    /api/admin/finance/expense-categories       ← 获取费用类别列表
POST   /api/admin/finance/expense-categories       ← 新增费用类别
PUT    /api/admin/finance/expense-categories/{id}  ← 编辑费用类别
DELETE /api/admin/finance/expense-categories/{id}  ← 删除费用类别（有关联不能删）

GET    /api/admin/finance/expenses                 ← 费用列表（分页 + 按类别筛选）
POST   /api/admin/finance/expenses                 ← 新增费用记录
PUT    /api/admin/finance/expenses/{id}            ← 编辑费用记录
DELETE /api/admin/finance/expenses/{id}            ← 删除费用记录
```

### 4.4 人员开支

```
GET    /api/admin/finance/personnel                ← 人员开支列表（分页）
POST   /api/admin/finance/personnel                ← 新增工资记录
PUT    /api/admin/finance/personnel/{id}           ← 编辑
DELETE /api/admin/finance/personnel/{id}           ← 删除
```

### 4.5 已有接口增强

现有报销接口增强:
```
POST   /api/admin/finance/reimbursements/{id}/pay  ← 增强：支持选择支付银行
```

现有报表接口增加:
```
GET    /api/admin/finance/sales-revenue   ← 增强：增加图表数据维度
GET    /api/admin/finance/purchase-report ← 增强：增加图表数据维度
GET    /api/admin/finance/profit-report   ← 增强：增加图表数据维度
```

## 5. 前端组件设计

### 5.1 路由配置

```js
{ path: '/finance',                     component: () => import('../views/finance/Overview.vue') }
{ path: '/finance/banks',               component: () => import('../views/finance/Banks.vue') }
{ path: '/finance/income',              component: () => import('../views/finance/Income.vue') }
{ path: '/finance/purchases',           component: () => import('../views/finance/Purchases.vue') }
{ path: '/finance/personnel',           component: () => import('../views/finance/Personnel.vue') }
{ path: '/finance/reimbursements',      component: () => import('../views/finance/Reimbursements.vue') }
{ path: '/finance/expenses',            component: () => import('../views/finance/Expenses.vue') }
{ path: '/finance/profit',              component: () => import('../views/finance/Profit.vue') }
```

将原 `views/Finance.vue` 拆分为 `views/finance/` 目录下的独立组件：
- `Overview.vue` — 总览仪表盘（含 ECharts 图表）
- `Banks.vue` — 银行管理（含转账弹窗）
- `Income.vue` — 收款明细（销售收入流水）
- `Purchases.vue` — 进货支出（采购成本）
- `Personnel.vue` — 人员开支
- `Reimbursements.vue` — 报销管理（从原 Finance.vue 抽取）
- `Expenses.vue` — 其他费用
- `Profit.vue` — 利润报表

### 5.2 需要安装的前端依赖

```bash
npm install echarts vue-echarts print-js
```

### 5.3 图表使用

在 `Overview.vue` 中:
- **月度趋势图**: 堆叠柱状图（收入绿色、支出橙色、利润蓝色）
- **费用构成饼图**: 进货/人员/其他费用的比例
- **银行余额卡片**: 每个银行独立显示余额

在 `Profit.vue` 中:
- **收入 vs 支出对比**: 分组柱状图
- **利润率折线**: 叠加在柱状图上的折线

在 `Expenses.vue` 中:
- **分类汇总玫瑰图**: 各类费用占比

### 5.4 打印功能

每个子页面使用统一的打印工具函数:

```js
// src/utils/print.js
export function printPage(title) {
  window.print()  // 结合 @media print CSS
}
```

关键 CSS:
```css
@media print {
  .sidebar, .navbar, .el-header, .el-menu { display: none !important; }
  .main-content { width: 100% !important; margin: 0 !important; }
  .page-break { page-break-after: always; }
  .print-date { position: fixed; bottom: 10px; right: 10px; font-size: 10px; }
  canvas { max-width: 100% !important; }  /* ECharts */
}
```

### 5.5 银行管理 UI 设计

银行管理页面布局:

```
┌─────────────────────────────────────────────────────────┐
│  💳 银行管理                              [+ 添加银行]  │
├─────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────┐ │
│ │  [DBS]    SGD $85,000  ┃  [OCBC]    SGD $62,000    │ │
│ │  ⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜⬜    ┃  ⬜⬜⬜⬜⬜⬜⬜⬜⬜      │ │
│ │  📤转账 📥入账 📋流水    ┃  📤转账 📥入账 📋流水    │ │
│ ├─────────────────────────────────────────────────────┤ │
│ │  [Maybank] MYR RM68,000 ┃  [CIMB]    MYR RM35,000  │ │
│ │  ⬜⬜⬜⬜⬜⬜⬜⬜⬜       ┃  ⬜⬜⬜⬜⬜             │ │
│ │  📤转账 📥入账 📋流水    ┃  📤转账 📥入账 📋流水    │ │
│ └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│  最近转账记录                                             │
│  2026-06-01  DBS → OCBC     SGD 10,000  转账到运营账户  │
│  2026-05-28  Maybank → CIMB MYR 5,000   调拨资金       │
│  2026-05-20  OCBC → DBS     SGD 3,000   还款           │
└─────────────────────────────────────────────────────────┘
```

**转账弹窗:**
```
┌─ 银行转账 ──────────────────────────┐
│  从: [DBS SGD ▼]  到: [OCBC SGD ▼]  │
│  金额: [_________]  SGD              │
│  手续费: [0.00]                       │
│  日期: [2026-06-07]                   │
│  备注: [________________]             │
│                                      │
│  [取消]                    [确认转账] │
└──────────────────────────────────────┘
```

### 5.6 费用管理 UI 设计

```
┌─ 📋 其他费用 ──────────────────────────────── [+ 新增] ──┐
│  [📅 日期筛选]  [全部分类 ▼]                              │
│                                                          │
│  ┌──────────────── 分类汇总卡片 ────────────────────┐     │
│  │ 🏢 办公租金 $3,200  │ 📦 物流运费 $5,800         │     │
│  │ 📢 市场推广 $2,500  │ 🔧 维修保养 $1,200         │     │
│  │ 📄 税费 $8,900      │ 📱 通讯网络 $800          │     │
│  │ ...                 │                           │     │
│  └──────────────────────────────────────────────────┘     │
│                                                          │
│  费用明细列表（分页表格）                                  │
│  日期      类别      金额    说明         支付方式  操作   │
│  06-01  物流运费  $1,200  空运到SG   DBS ****   ✏️ 🗑️ │
│  06-03  办公租金  $3,200  6月租金    OCBC ****  ✏️ 🗑️ │
│  ...                                                    │
└──────────────────────────────────────────────────────────┘
```

## 6. 数据库迁移计划

创建一个新的迁移文件: `V25__create_finance_module.sql`

包含:
1. 创建 `bank_accounts` 表
2. 创建 `bank_transfers` 表
3. 创建 `expense_categories` 表 + 默认数据
4. 创建 `other_expenses` 表
5. 创建 `personnel_expenses` 表
6. ALTER TABLE reimbursements 增加字段

## 7. 后端 Java 结构

```
com.coinmarket.finance/
├── controller/
│   ├── FinanceController.java      ← 重构，拆分到以下控制器
│   ├── BankController.java         ← 银行管理 API
│   ├── ExpenseController.java      ← 费用管理 API
│   └── PersonnelController.java    ← 人员开支 API
├── entity/
│   ├── Reimbursement.java          ← 已有，增强
│   ├── BankAccount.java            ← 新增
│   ├── BankTransfer.java           ← 新增
│   ├── ExpenseCategory.java        ← 新增
│   ├── OtherExpense.java           ← 新增
│   └── PersonnelExpense.java       ← 新增
├── repository/ (对应 JPA Repository 每个实体一个)
├── service/
│   ├── FinanceService.java         ← 重构（保留总览和报表逻辑）
│   ├── BankService.java            ← 新增
│   ├── ExpenseService.java         ← 新增
│   └── PersonnelService.java       ← 新增
└── dto/ (每个实体对应的 Request/Response DTO)
```

## 8. 安全考虑

### 8.1 权限控制

- 所有财务接口需要 `hasRole('ADMIN')` 或 `hasRole('FINANCE')` 权限
- 银行转账需要二次确认（前端弹窗 + 后端操作审计日志）

### 8.2 审计日志

- 所有财务操作（新增、修改、删除、转账）记录到审计日志
- 银行余额调整需要记录调整原因和操作人

### 8.3 数据脱敏

- 银行账号仅显示后4位
- 导出报表时自动添加"内部资料"水印

## 9. 测试要点

### 9.1 单元测试

| 测试项 | 说明 |
|--------|------|
| BankService.createAccount | 创建银行账户、重复校验 |
| BankService.transfer | 转账余额扣减、手续费计算、余额不足异常 |
| BankService.deleteAccount | 有交易记录的账户不可删除 |
| ExpenseService.crud | 费用类别和记录的增删改查 |
| PersonnelService.crud | 人员开支的增删改查 |
| FinanceService.getOverview | 总览数据汇总计算 |

### 9.2 集成测试

| 测试项 | 说明 |
|--------|------|
| 银行转账后双方余额正确 | 从A转$1000到B，A-1000，B+1000 |
| 费用记录统计正确 | 按类别、日期范围筛选汇总 |
| 总览数据计算 | 收入 - (进货+人员+其他费用+报销) = 净利润 |

### 9.3 E2E 测试

| 测试项 | 说明 |
|--------|------|
| 银行管理完整流程 | 添加账户 → 查看余额 → 转账 → 查看流水 |
| 费用管理完整流程 | 新增类别 → 添加费用 → 按类别筛选 → 导出打印 |
| 总览仪表盘 | 图表渲染正常、数据正确、响应式适配 |

### 9.4 打印测试

| 测试项 | 说明 |
|--------|------|
| 打印预览 | 侧边栏和导航自动隐藏 |
| 分页 | 长表格自动分页 |
| 图表打印 | ECharts 转为高清静态图 |

## 10. 实施步骤

### Phase 1: 基础设施 (预计 1 天)
1. 创建数据库迁移 `V25__create_finance_module.sql`
2. 创建后端 Entity + Repository
3. 创建后端 DTO
4. 安装前端依赖: echarts, vue-echarts, print-js

### Phase 2: 银行管理 (预计 1.5 天)
1. BankController + BankService 实现
2. Banks.vue 前端页面
3. 转账功能弹窗

### Phase 3: 费用管理 + 人员开支 (预计 1.5 天)
1. ExpenseController + ExpenseService
2. PersonnelController + PersonnelService
3. Expenses.vue + Personnel.vue

### Phase 4: 总览仪表盘 (预计 1 天)
1. 总览 API 实现
2. Overview.vue 图表设计
3. 月度趋势 + 费用构成可视化

### Phase 5: 打印 + 增强 (预计 0.5 天)
1. 每个页面增加打印功能
2. print-js 集成
3. 报销页面增强（选择银行、关联费用类别）

### Phase 6: 测试 (预计 1 天)
1. 单元测试
2. 集成测试
3. E2E 测试
4. 打印功能验证

---

## 11. 附录: 现有文件修改清单

### 需修改的文件

| 文件 | 变更 |
|------|------|
| `frontend/admin/src/router/index.js` | 新增 7 个财务子路由 |
| `frontend/admin/src/views/Finance.vue` | 废弃，拆分为目录组件 |
| `frontend/admin/src/api/index.js` | 无需修改（已有 axios 实例） |
| `frontend/admin/package.json` | 新增 echarts, vue-echarts, print-js |
| `frontend/admin/src/i18n/index.js` | 新增财务模块的 i18n 翻译 |
| `backend/.../FinanceController.java` | 重构按模块拆分 |
| `backend/.../FinanceService.java` | 保留总览逻辑，拆分子模块 |
| `backend/.../Reimbursement.java` | 增加 expense_category_id, bank_account_id 字段 |
| `backend/.../ReimbursementRepository.java` | 按新字段查询 |
