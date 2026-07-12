# 财务管理模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构增强 Coin Marketplace 管理后台的财务管理模块，新增银行管理、费用管理、人员开支、图表总览和打印功能。

**Architecture:** 后端按子模块拆分控制器和服务（Bank/Expense/Personnel），前端按子模块拆分为独立页面文件，总览页使用 ECharts 图表，所有子页面支持打印。

**Tech Stack:** Java 21 + Spring Boot 3.3.0 + JPA + Flyway + PostgreSQL | Vue 3 + Element Plus + ECharts + print-js

---

## File Structure

### Backend — New Files

```
com.coinmarket.finance/
├── controller/
│   ├── BankController.java              ← 银行管理 API
│   ├── ExpenseController.java           ← 费用管理 API
│   └── PersonnelController.java         ← 人员开支 API
├── entity/
│   ├── BankAccount.java                 ← 银行账户实体
│   ├── BankTransfer.java                ← 转账记录实体
│   ├── ExpenseCategory.java             ← 费用类别实体
│   ├── OtherExpense.java                ← 其他费用实体
│   └── PersonnelExpense.java            ← 人员开支实体
├── repository/
│   ├── BankAccountRepository.java
│   ├── BankTransferRepository.java
│   ├── ExpenseCategoryRepository.java
│   ├── OtherExpenseRepository.java
│   └── PersonnelExpenseRepository.java
├── service/
│   ├── BankService.java                 ← 银行管理业务逻辑
│   ├── ExpenseService.java              ← 费用管理业务逻辑
│   └── PersonnelService.java            ← 人员开支业务逻辑
└── dto/
    ├── BankAccountRequest.java          ← 创建/编辑银行账户
    ├── BankAccountResponse.java         ← 银行账户响应
    ├── BankTransferRequest.java         ← 转账请求
    ├── BankTransferResponse.java        ← 转账响应
    ├── ExpenseCategoryRequest.java      ← 创建/编辑费用类别
    ├── ExpenseCategoryResponse.java     ← 费用类别响应
    ├── OtherExpenseRequest.java         ← 创建/编辑费用记录
    ├── OtherExpenseResponse.java        ← 费用记录响应
    ├── PersonnelExpenseRequest.java     ← 创建/编辑人员开支
    ├── PersonnelExpenseResponse.java    ← 人员开支响应
    └── FinanceOverviewResponse.java     ← 总览数据响应
```

### Backend — Modified Files

```
├── FinanceController.java               ← 保留总览和报表接口，移除/拆分子模块接口
├── FinanceService.java                   ← 保留总览和报表逻辑，移除/拆分子模块逻辑
└── Reimbursement.java                    ← 增加 expense_category_id, bank_account_id 字段
```

### Frontend — New Files

```
frontend/admin/src/
├── views/finance/
│   ├── Overview.vue                      ← 总览仪表盘（含 ECharts 图表）
│   ├── Banks.vue                         ← 银行管理
│   ├── Income.vue                        ← 收款/销售收入
│   ├── Purchases.vue                     ← 进货支出（从 PurchaseReport.vue 迁移增强）
│   ├── Personnel.vue                     ← 人员开支
│   ├── Reimbursements.vue               ← 报销管理（从 Finance.vue 抽取增强）
│   ├── Expenses.vue                      ← 其他费用
│   └── Profit.vue                        ← 利润报表
├── utils/print.js                        ← 打印工具函数
└── components/finance/
    ├── StatCard.vue                       ← 统计卡片组件
    ├── BankCard.vue                       ← 银行余额卡片组件
    └── TransferDialog.vue                ← 银行转账弹窗组件
```

### Frontend — Modified Files

```
├── router/index.js                        ← 新增 7 个财务子路由
├── package.json                           ← 新增 echarts, vue-echarts, print-js
├── i18n/index.js                          ← 新增财务模块翻译
└── App.vue                                ← 新增左侧菜单财务子项
```

### Database Migration

```
backend/src/main/resources/db/migration/
└── V25__create_finance_module.sql         ← 新迁移文件
```

---

## Task Breakdown

### Phase 1: 数据库迁移

#### Task 1: 创建数据库迁移文件 V25

**Files:**
- Create: `backend/src/main/resources/db/migration/V25__create_finance_module.sql`

- [ ] **Step 1: Write the migration SQL**

Create `V25__create_finance_module.sql` with the full schema:

```sql
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
```

- [ ] **Step 2: 运行迁移验证**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn flyway:migrate`
Expected: `Successfully applied 1 migration` (existing V1-V24 + new V25)

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/resources/db/migration/V25__create_finance_module.sql
git commit -m "feat: add finance module database tables (banks, expenses, personnel)"
```

---

### Phase 2: 后端 Entity + Repository

#### Task 2: 创建 BankAccount 实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/entity/BankAccount.java`
- Create: `backend/src/main/java/com/coinmarket/finance/repository/BankAccountRepository.java`

- [ ] **Step 1: Create BankAccount entity**

```java
package com.coinmarket.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts", schema = "coin_finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(name = "current_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Create BankAccountRepository**

```java
package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByIsActiveTrueOrderBySortOrder();
}
```

- [ ] **Step 3: Compile to verify**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/entity/BankAccount.java
git add backend/src/main/java/com/coinmarket/finance/repository/BankAccountRepository.java
git commit -m "feat: add BankAccount entity and repository"
```

#### Task 3: 创建 BankTransfer 实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/entity/BankTransfer.java`
- Create: `backend/src/main/java/com/coinmarket/finance/repository/BankTransferRepository.java`

- [ ] **Step 1: Create BankTransfer entity**

```java
package com.coinmarket.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_transfers", schema = "coin_finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankTransfer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Create BankTransferRepository**

```java
package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.BankTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransferRepository extends JpaRepository<BankTransfer, Long> {
    Page<BankTransfer> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(
            Long fromAccountId, Long toAccountId, Pageable pageable);
}
```

- [ ] **Step 3: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/entity/BankTransfer.java
git add backend/src/main/java/com/coinmarket/finance/repository/BankTransferRepository.java
git commit -m "feat: add BankTransfer entity and repository"
```

#### Task 4: 创建 ExpenseCategory 实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/entity/ExpenseCategory.java`
- Create: `backend/src/main/java/com/coinmarket/finance/repository/ExpenseCategoryRepository.java`

- [ ] **Step 1: Create ExpenseCategory entity**

```java
package com.coinmarket.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_categories", schema = "coin_finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    private String color;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Create ExpenseCategoryRepository**

```java
package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findByIsActiveTrueOrderBySortOrder();
    boolean existsByName(String name);
}
```

- [ ] **Step 3: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/entity/ExpenseCategory.java
git add backend/src/main/java/com/coinmarket/finance/repository/ExpenseCategoryRepository.java
git commit -m "feat: add ExpenseCategory entity and repository"
```

#### Task 5: 创建 OtherExpense 实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/entity/OtherExpense.java`
- Create: `backend/src/main/java/com/coinmarket/finance/repository/OtherExpenseRepository.java`

- [ ] **Step 1: Create OtherExpense entity**

```java
package com.coinmarket.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "other_expenses", schema = "coin_finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtherExpense {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 200)
    private String vendor;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Create OtherExpenseRepository**

```java
package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.OtherExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OtherExpenseRepository extends JpaRepository<OtherExpense, Long> {
    Page<OtherExpense> findByCategoryIdOrderByExpenseDateDesc(Long categoryId, Pageable pageable);
    Page<OtherExpense> findAllByOrderByExpenseDateDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM OtherExpense e WHERE e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e.categoryId, COALESCE(SUM(e.amount), 0) FROM OtherExpense e " +
           "WHERE e.expenseDate BETWEEN :start AND :end GROUP BY e.categoryId")
    List<Object[]> sumGroupByCategory(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
```

- [ ] **Step 3: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/entity/OtherExpense.java
git add backend/src/main/java/com/coinmarket/finance/repository/OtherExpenseRepository.java
git commit -m "feat: add OtherExpense entity and repository"
```

#### Task 6: 创建 PersonnelExpense 实体和 Repository

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/entity/PersonnelExpense.java`
- Create: `backend/src/main/java/com/coinmarket/finance/repository/PersonnelExpenseRepository.java`

- [ ] **Step 1: Create PersonnelExpense entity** (follow OtherExpense pattern, adapting fields from schema)

```java
package com.coinmarket.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "personnel_expenses", schema = "coin_finance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonnelExpense {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(length = 100)
    private String position;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "pay_date", nullable = false)
    private LocalDate payDate;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: Create PersonnelExpenseRepository**

```java
package com.coinmarket.finance.repository;

import com.coinmarket.finance.entity.PersonnelExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PersonnelExpenseRepository extends JpaRepository<PersonnelExpense, Long> {
    Page<PersonnelExpense> findAllByOrderByPayDateDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PersonnelExpense p WHERE p.payDate BETWEEN :start AND :end")
    BigDecimal sumByPayDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
```

- [ ] **Step 3: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/entity/PersonnelExpense.java
git add backend/src/main/java/com/coinmarket/finance/repository/PersonnelExpenseRepository.java
git commit -m "feat: add PersonnelExpense entity and repository"
```

---

### Phase 3: 后端 DTO

#### Task 7: 创建银行管理 DTO

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/dto/BankAccountRequest.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/BankAccountResponse.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/BankTransferRequest.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/BankTransferResponse.java`

- [ ] **Step 1: Create BankAccountRequest**

```java
package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建/编辑银行账户请求")
public class BankAccountRequest {
    @NotBlank @Schema(description = "银行名称")
    private String bankName;

    @NotBlank @Schema(description = "账户名称")
    private String accountName;

    @NotBlank @Schema(description = "账号（仅后4位）")
    private String accountNumber;

    @NotBlank @Schema(description = "币种 SGD/MYR/USD")
    private String currency;

    @NotBlank @Schema(description = "国家 SG/MY")
    private String country;

    @NotNull @Schema(description = "当前余额")
    private BigDecimal currentBalance;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "排序")
    private Integer sortOrder;
}
```

- [ ] **Step 2: Create BankAccountResponse**

```java
package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.BankAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "银行账户响应数据")
public class BankAccountResponse {
    private Long id;
    private String bankName;
    private String accountName;
    private String accountNumber;
    private String currency;
    private String country;
    private BigDecimal currentBalance;
    private Boolean isActive;
    private Integer sortOrder;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BankAccountResponse from(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .accountName(account.getAccountName())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .country(account.getCountry())
                .currentBalance(account.getCurrentBalance())
                .isActive(account.getIsActive())
                .sortOrder(account.getSortOrder())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
```

- [ ] **Step 3: Create BankTransferRequest**

```java
package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "银行转账请求")
public class BankTransferRequest {
    @NotNull @Schema(description = "转出账户ID")
    private Long fromAccountId;

    @NotNull @Schema(description = "转入账户ID")
    private Long toAccountId;

    @NotNull @Positive @Schema(description = "转账金额")
    private BigDecimal amount;

    @NotNull @Schema(description = "币种")
    private String currency;

    @Schema(description = "手续费", defaultValue = "0.00")
    private BigDecimal fee;

    @NotNull @Schema(description = "转账日期")
    private LocalDate transferDate;

    @Schema(description = "参考号")
    private String referenceNo;

    @Schema(description = "备注")
    private String description;
}
```

- [ ] **Step 4: Create BankTransferResponse**

```java
package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.BankTransfer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "银行转账响应数据")
public class BankTransferResponse {
    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String currency;
    private BigDecimal fee;
    private LocalDate transferDate;
    private String referenceNo;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String fromBankName;
    private String toBankName;

    public static BankTransferResponse from(BankTransfer transfer) {
        return BankTransferResponse.builder()
                .id(transfer.getId())
                .fromAccountId(transfer.getFromAccountId())
                .toAccountId(transfer.getToAccountId())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .fee(transfer.getFee())
                .transferDate(transfer.getTransferDate())
                .referenceNo(transfer.getReferenceNo())
                .description(transfer.getDescription())
                .createdBy(transfer.getCreatedBy())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 5: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/dto/BankAccountRequest.java
git add backend/src/main/java/com/coinmarket/finance/dto/BankAccountResponse.java
git add backend/src/main/java/com/coinmarket/finance/dto/BankTransferRequest.java
git add backend/src/main/java/com/coinmarket/finance/dto/BankTransferResponse.java
git commit -m "feat: add bank DTOs"
```

#### Task 8: 创建费用管理 DTO

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/dto/ExpenseCategoryRequest.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/ExpenseCategoryResponse.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/OtherExpenseRequest.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/OtherExpenseResponse.java`

- [ ] **Step 1: Create ExpenseCategoryRequest**

```java
package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建/编辑费用类别请求")
public class ExpenseCategoryRequest {
    @NotBlank @Schema(description = "类别名称")
    private String name;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "显示颜色")
    private String color;

    @Schema(description = "排序")
    private Integer sortOrder;
}
```

- [ ] **Step 2: Create ExpenseCategoryResponse**

```java
package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "费用类别响应数据")
public class ExpenseCategoryResponse {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static ExpenseCategoryResponse from(ExpenseCategory c) {
        return ExpenseCategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .icon(c.getIcon())
                .color(c.getColor())
                .sortOrder(c.getSortOrder())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 3: Create OtherExpenseRequest**

```java
package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建/编辑其他费用请求")
public class OtherExpenseRequest {
    @NotNull @Schema(description = "费用类别ID")
    private Long categoryId;

    @NotNull @Positive @Schema(description = "金额")
    private BigDecimal amount;

    @NotNull @Schema(description = "币种")
    private String currency;

    @NotNull @Schema(description = "费用日期")
    private LocalDate expenseDate;

    @NotBlank @Schema(description = "费用说明")
    private String description;

    @Schema(description = "供应商/收款方")
    private String vendor;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "关联银行账户ID")
    private Long bankAccountId;

    @Schema(description = "票据URL")
    private String receiptUrl;
}
```

- [ ] **Step 4: Create OtherExpenseResponse**

```java
package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.OtherExpense;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "其他费用响应数据")
public class OtherExpenseResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private String description;
    private String vendor;
    private String paymentMethod;
    private Long bankAccountId;
    private String receiptUrl;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static OtherExpenseResponse from(OtherExpense expense) {
        return OtherExpenseResponse.builder()
                .id(expense.getId())
                .categoryId(expense.getCategoryId())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription())
                .vendor(expense.getVendor())
                .paymentMethod(expense.getPaymentMethod())
                .bankAccountId(expense.getBankAccountId())
                .receiptUrl(expense.getReceiptUrl())
                .createdBy(expense.getCreatedBy())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    public OtherExpenseResponse withCategoryName(String categoryName) {
        this.categoryName = categoryName;
        return this;
    }
}
```

- [ ] **Step 5: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/dto/ExpenseCategoryRequest.java
git add backend/src/main/java/com/coinmarket/finance/dto/ExpenseCategoryResponse.java
git add backend/src/main/java/com/coinmarket/finance/dto/OtherExpenseRequest.java
git add backend/src/main/java/com/coinmarket/finance/dto/OtherExpenseResponse.java
git commit -m "feat: add expense DTOs"
```

#### Task 9: 创建人员开支和总览 DTO

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/dto/PersonnelExpenseRequest.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/PersonnelExpenseResponse.java`
- Create: `backend/src/main/java/com/coinmarket/finance/dto/FinanceOverviewResponse.java`

- [ ] **Step 1: Create PersonnelExpenseRequest**

```java
package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建/编辑人员开支请求")
public class PersonnelExpenseRequest {
    @NotBlank @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "岗位")
    private String position;

    @NotNull @Positive @Schema(description = "实发金额")
    private BigDecimal amount;

    @NotNull @Schema(description = "币种")
    private String currency;

    @NotNull @Schema(description = "发薪日")
    private LocalDate payDate;

    @NotNull @Schema(description = "薪资周期开始")
    private LocalDate periodStart;

    @NotNull @Schema(description = "薪资周期结束")
    private LocalDate periodEnd;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "关联银行账户ID")
    private Long bankAccountId;
}
```

- [ ] **Step 2: Create PersonnelExpenseResponse**

```java
package com.coinmarket.finance.dto;

import com.coinmarket.finance.entity.PersonnelExpense;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "人员开支响应数据")
public class PersonnelExpenseResponse {
    private Long id;
    private String employeeName;
    private String position;
    private BigDecimal amount;
    private String currency;
    private LocalDate payDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String notes;
    private String paymentMethod;
    private Long bankAccountId;
    private Long createdBy;
    private LocalDateTime createdAt;

    public static PersonnelExpenseResponse from(PersonnelExpense p) {
        return PersonnelExpenseResponse.builder()
                .id(p.getId())
                .employeeName(p.getEmployeeName())
                .position(p.getPosition())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .payDate(p.getPayDate())
                .periodStart(p.getPeriodStart())
                .periodEnd(p.getPeriodEnd())
                .notes(p.getNotes())
                .paymentMethod(p.getPaymentMethod())
                .bankAccountId(p.getBankAccountId())
                .createdBy(p.getCreatedBy())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 3: Create FinanceOverviewResponse**

```java
package com.coinmarket.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "财务总览响应数据")
public class FinanceOverviewResponse {

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "总支出")
    private BigDecimal totalExpenses;

    @Schema(description = "净利润")
    private BigDecimal netProfit;

    @Schema(description = "利润率(%)")
    private BigDecimal profitMargin;

    @Schema(description = "银行总余额")
    private BigDecimal totalBankBalance;

    @Schema(description = "月度趋势")
    private List<MonthlyTrend> monthlyTrend;

    @Schema(description = "费用构成")
    private List<ExpenseBreakdown> expenseBreakdown;

    @Schema(description = "银行余额分布")
    private List<BankBalanceInfo> bankBreakdown;

    @Schema(description = "月度数据")
    public record MonthlyTrend(String month, BigDecimal income, BigDecimal expenses, BigDecimal profit) {}

    @Schema(description = "费用构成数据")
    public record ExpenseBreakdown(String category, BigDecimal amount, Double percentage) {}

    @Schema(description = "银行余额信息")
    public record BankBalanceInfo(String bankName, BigDecimal balance, String currency) {}
}
```

- [ ] **Step 4: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/dto/PersonnelExpenseRequest.java
git add backend/src/main/java/com/coinmarket/finance/dto/PersonnelExpenseResponse.java
git add backend/src/main/java/com/coinmarket/finance/dto/FinanceOverviewResponse.java
git commit -m "feat: add personnel and overview DTOs"
```

---

### Phase 4: 后端 Service

#### Task 10: 创建 BankService

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/service/BankService.java`

- [ ] **Step 1: Create BankService**

```java
package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.BankAccount;
import com.coinmarket.finance.entity.BankTransfer;
import com.coinmarket.finance.repository.BankAccountRepository;
import com.coinmarket.finance.repository.BankTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankAccountRepository bankAccountRepository;
    private final BankTransferRepository bankTransferRepository;

    @Transactional(readOnly = true)
    public List<BankAccountResponse> listAccounts() {
        return bankAccountRepository.findByIsActiveTrueOrderBySortOrder().stream()
                .map(BankAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse getAccount(Long id) {
        return bankAccountRepository.findById(id)
                .map(BankAccountResponse::from)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
    }

    @Transactional
    public BankAccountResponse createAccount(BankAccountRequest request) {
        BankAccount account = BankAccount.builder()
                .bankName(request.getBankName())
                .accountName(request.getAccountName())
                .accountNumber(request.getAccountNumber())
                .currency(request.getCurrency())
                .country(request.getCountry())
                .currentBalance(request.getCurrentBalance() != null ? request.getCurrentBalance() : BigDecimal.ZERO)
                .isActive(true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .notes(request.getNotes())
                .build();
        account = bankAccountRepository.save(account);
        return BankAccountResponse.from(account);
    }

    @Transactional
    public BankAccountResponse updateAccount(Long id, BankAccountRequest request) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
        account.setBankName(request.getBankName());
        account.setAccountName(request.getAccountName());
        account.setAccountNumber(request.getAccountNumber());
        account.setCurrency(request.getCurrency());
        account.setCountry(request.getCountry());
        account.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        account.setNotes(request.getNotes());
        account = bankAccountRepository.save(account);
        return BankAccountResponse.from(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
        account.setIsActive(false);
        bankAccountRepository.save(account);
    }

    @Transactional
    public BankAccountResponse adjustBalance(Long id, BigDecimal amount, String notes) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("银行账户不存在"));
        account.setCurrentBalance(account.getCurrentBalance().add(amount));
        if (notes != null) account.setNotes(notes);
        account = bankAccountRepository.save(account);
        return BankAccountResponse.from(account);
    }

    @Transactional
    public BankTransferResponse transfer(BankTransferRequest request, Long userId) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BusinessException("转出和转入账户不能相同");
        }

        BankAccount from = bankAccountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new BusinessException("转出账户不存在"));
        BankAccount to = bankAccountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new BusinessException("转入账户不存在"));

        BigDecimal totalDeduction = request.getAmount().add(
                request.getFee() != null ? request.getFee() : BigDecimal.ZERO);

        if (from.getCurrentBalance().compareTo(totalDeduction) < 0) {
            throw new BusinessException("余额不足：账户余额 " + from.getCurrentBalance()
                    + "，需要 " + totalDeduction);
        }

        // 扣减转出账户
        from.setCurrentBalance(from.getCurrentBalance().subtract(totalDeduction));
        bankAccountRepository.save(from);

        // 增加转入账户
        to.setCurrentBalance(to.getCurrentBalance().add(request.getAmount()));
        bankAccountRepository.save(to);

        // 记录转账
        BankTransfer transfer = BankTransfer.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                .transferDate(request.getTransferDate())
                .referenceNo(request.getReferenceNo())
                .description(request.getDescription())
                .createdBy(userId)
                .build();
        transfer = bankTransferRepository.save(transfer);

        BankTransferResponse response = BankTransferResponse.from(transfer);
        response.setFromBankName(from.getBankName());
        response.setToBankName(to.getBankName());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<BankTransferResponse> listTransfers(Long accountId, Pageable pageable) {
        if (accountId != null) {
            return bankTransferRepository
                    .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId, pageable)
                    .map(t -> {
                        BankTransferResponse r = BankTransferResponse.from(t);
                        r.setFromBankName(
                            bankAccountRepository.findById(t.getFromAccountId())
                                .map(BankAccount::getBankName).orElse(""));
                        r.setToBankName(
                            bankAccountRepository.findById(t.getToAccountId())
                                .map(BankAccount::getBankName).orElse(""));
                        return r;
                    });
        }
        return bankTransferRepository.findAll(pageable).map(t -> {
            BankTransferResponse r = BankTransferResponse.from(t);
            r.setFromBankName(
                bankAccountRepository.findById(t.getFromAccountId())
                    .map(BankAccount::getBankName).orElse(""));
            r.setToBankName(
                bankAccountRepository.findById(t.getToAccountId())
                    .map(BankAccount::getBankName).orElse(""));
            return r;
        });
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/service/BankService.java
git commit -m "feat: add BankService"
```

#### Task 11: 创建 ExpenseService

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/service/ExpenseService.java`

- [ ] **Step 1: Create ExpenseService**

```java
package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.ExpenseCategory;
import com.coinmarket.finance.entity.OtherExpense;
import com.coinmarket.finance.repository.ExpenseCategoryRepository;
import com.coinmarket.finance.repository.OtherExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final OtherExpenseRepository otherExpenseRepository;

    // --- Categories ---

    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> listCategories() {
        return expenseCategoryRepository.findByIsActiveTrueOrderBySortOrder().stream()
                .map(ExpenseCategoryResponse::from)
                .toList();
    }

    @Transactional
    public ExpenseCategoryResponse createCategory(ExpenseCategoryRequest request) {
        if (expenseCategoryRepository.existsByName(request.getName())) {
            throw new BusinessException("该费用类别已存在");
        }

        ExpenseCategory category = ExpenseCategory.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor() != null ? request.getColor() : "#6b7280")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(true)
                .build();
        category = expenseCategoryRepository.save(category);
        return ExpenseCategoryResponse.from(category);
    }

    @Transactional
    public ExpenseCategoryResponse updateCategory(Long id, ExpenseCategoryRequest request) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用类别不存在"));
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor() != null ? request.getColor() : "#6b7280");
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category = expenseCategoryRepository.save(category);
        return ExpenseCategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用类别不存在"));
        category.setIsActive(false);
        expenseCategoryRepository.save(category);
    }

    // --- Expenses ---

    @Transactional(readOnly = true)
    public Page<OtherExpenseResponse> listExpenses(Long categoryId, Pageable pageable) {
        Page<OtherExpense> page;
        if (categoryId != null) {
            page = otherExpenseRepository.findByCategoryIdOrderByExpenseDateDesc(categoryId, pageable);
        } else {
            page = otherExpenseRepository.findAllByOrderByExpenseDateDesc(pageable);
        }
        return page.map(e -> {
            String catName = expenseCategoryRepository.findById(e.getCategoryId())
                    .map(ExpenseCategory::getName).orElse("未知");
            return OtherExpenseResponse.from(e).withCategoryName(catName);
        });
    }

    @Transactional
    public OtherExpenseResponse createExpense(OtherExpenseRequest request, Long userId) {
        if (!expenseCategoryRepository.existsById(request.getCategoryId())) {
            throw new BusinessException("费用类别不存在");
        }

        OtherExpense expense = OtherExpense.builder()
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "SGD")
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .vendor(request.getVendor())
                .paymentMethod(request.getPaymentMethod())
                .bankAccountId(request.getBankAccountId())
                .receiptUrl(request.getReceiptUrl())
                .createdBy(userId)
                .build();
        expense = otherExpenseRepository.save(expense);

        String catName = expenseCategoryRepository.findById(expense.getCategoryId())
                .map(ExpenseCategory::getName).orElse("未知");
        return OtherExpenseResponse.from(expense).withCategoryName(catName);
    }

    @Transactional
    public OtherExpenseResponse updateExpense(Long id, OtherExpenseRequest request) {
        OtherExpense expense = otherExpenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用记录不存在"));

        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SGD");
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());
        expense.setVendor(request.getVendor());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setBankAccountId(request.getBankAccountId());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense = otherExpenseRepository.save(expense);

        String catName = expenseCategoryRepository.findById(expense.getCategoryId())
                .map(ExpenseCategory::getName).orElse("未知");
        return OtherExpenseResponse.from(expense).withCategoryName(catName);
    }

    @Transactional
    public void deleteExpense(Long id) {
        if (!otherExpenseRepository.existsById(id)) {
            throw new BusinessException("费用记录不存在");
        }
        otherExpenseRepository.deleteById(id);
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/service/ExpenseService.java
git commit -m "feat: add ExpenseService"
```

#### Task 12: 创建 PersonnelService 和更新 FinanceService

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/service/PersonnelService.java`
- Modify: `backend/src/main/java/com/coinmarket/finance/service/FinanceService.java` (add getOverview method)

- [ ] **Step 1: Create PersonnelService**

```java
package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.PersonnelExpense;
import com.coinmarket.finance.repository.PersonnelExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonnelService {

    private final PersonnelExpenseRepository personnelExpenseRepository;

    @Transactional(readOnly = true)
    public Page<PersonnelExpenseResponse> listPersonnel(Pageable pageable) {
        return personnelExpenseRepository.findAllByOrderByPayDateDesc(pageable)
                .map(PersonnelExpenseResponse::from);
    }

    @Transactional
    public PersonnelExpenseResponse createPersonnel(PersonnelExpenseRequest request, Long userId) {
        PersonnelExpense p = PersonnelExpense.builder()
                .employeeName(request.getEmployeeName())
                .position(request.getPosition())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "SGD")
                .payDate(request.getPayDate())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod())
                .bankAccountId(request.getBankAccountId())
                .createdBy(userId)
                .build();
        p = personnelExpenseRepository.save(p);
        return PersonnelExpenseResponse.from(p);
    }

    @Transactional
    public PersonnelExpenseResponse updatePersonnel(Long id, PersonnelExpenseRequest request) {
        PersonnelExpense p = personnelExpenseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("人员开支记录不存在"));
        p.setEmployeeName(request.getEmployeeName());
        p.setPosition(request.getPosition());
        p.setAmount(request.getAmount());
        p.setCurrency(request.getCurrency() != null ? request.getCurrency() : "SGD");
        p.setPayDate(request.getPayDate());
        p.setPeriodStart(request.getPeriodStart());
        p.setPeriodEnd(request.getPeriodEnd());
        p.setNotes(request.getNotes());
        p.setPaymentMethod(request.getPaymentMethod());
        p.setBankAccountId(request.getBankAccountId());
        p = personnelExpenseRepository.save(p);
        return PersonnelExpenseResponse.from(p);
    }

    @Transactional
    public void deletePersonnel(Long id) {
        if (!personnelExpenseRepository.existsById(id)) {
            throw new BusinessException("人员开支记录不存在");
        }
        personnelExpenseRepository.deleteById(id);
    }
}
```

- [ ] **Step 2: Add getOverview method to FinanceService.java**

Open `FinanceService.java` and add the new method after getDashboard():

```java
    // ========== 新增: 财务总览 ==========

    private final BankAccountRepository bankAccountRepository;
    private final OtherExpenseRepository otherExpenseRepository;
    private final PersonnelExpenseRepository personnelExpenseRepository;

    public FinanceOverviewResponse getOverview() {
        LocalDate now = LocalDate.now();
        LocalDate yearStart = now.with(java.time.temporal.TemporalAdjusters.firstDayOfYear());
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate yearEnd = now.plusDays(1);

        // 收入：已完成订单
        BigDecimal totalIncome = orderRepository.sumCompletedSalesBetween(
                yearStart.atStartOfDay(), yearEnd.atStartOfDay());

        // 各月收入
        List<Object[]> salesRows = orderRepository.monthlySalesBetween(
                yearStart.atStartOfDay(), yearEnd.atStartOfDay());

        // 进货成本
        BigDecimal purchaseCost = inventoryBatchRepository.sumPurchaseCostBetween(yearStart, now);

        // 各月进货
        List<Object[]> purchaseRows = inventoryBatchRepository.monthlyPurchaseBetween(yearStart, now);

        // 其他费用
        BigDecimal otherExpenses = otherExpenseRepository.sumByDateBetween(yearStart, now);

        // 人员开支
        BigDecimal personnelCost = personnelExpenseRepository.sumByPayDateBetween(yearStart, now);

        // 报销（已支付的）
        BigDecimal reimbursed = reimbursementRepository.sumPaidAmountBetween(yearStart.atStartOfDay(),
                now.plusDays(1).atStartOfDay());

        BigDecimal totalExpenses = purchaseCost.add(otherExpenses).add(personnelCost).add(reimbursed);
        BigDecimal netProfit = totalIncome.subtract(totalExpenses);
        BigDecimal profitMargin = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.multiply(BigDecimal.valueOf(100)).divide(totalIncome, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 银行总余额
        BigDecimal totalBankBalance = bankAccountRepository.findByIsActiveTrueOrderBySortOrder().stream()
                .map(BankAccount::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 月度趋势（合并收入和各项支出）
        Set<String> allMonths = new TreeSet<>();
        Map<String, BigDecimal> incomeByMonth = new HashMap<>();
        for (Object[] r : salesRows) { String m = (String)r[0]; allMonths.add(m); incomeByMonth.put(m, (BigDecimal)r[1]); }
        Map<String, BigDecimal> costByMonth = new HashMap<>();
        for (Object[] r : purchaseRows) { String m = (String)r[0]; allMonths.add(m); costByMonth.put(m, (BigDecimal)r[1]); }

        // 实际的月度费用和人员开支需要按月份聚合 — 简化：均摊到已有月份
        BigDecimal monthlyOtherExp = allMonths.isEmpty() ? otherExpenses : otherExpenses.divide(
                BigDecimal.valueOf(allMonths.size()), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyPersonnel = allMonths.isEmpty() ? personnelCost : personnelCost.divide(
                BigDecimal.valueOf(allMonths.size()), 2, RoundingMode.HALF_UP);

        List<FinanceOverviewResponse.MonthlyTrend> monthlyTrend = allMonths.stream().map(m -> {
            BigDecimal inc = incomeByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal cost = costByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal other = monthlyOtherExp;
            BigDecimal person = monthlyPersonnel;
            BigDecimal totalExp = cost.add(other).add(person);
            return new FinanceOverviewResponse.MonthlyTrend(m, inc, totalExp, inc.subtract(totalExp));
        }).toList();

        // 费用构成
        List<FinanceOverviewResponse.ExpenseBreakdown> expenseBreakdown = new ArrayList<>();
        expenseBreakdown.add(new FinanceOverviewResponse.ExpenseBreakdown(
                "进货成本", purchaseCost, purchaseCost.doubleValue() / totalExpenses.doubleValue() * 100));
        expenseBreakdown.add(new FinanceOverviewResponse.ExpenseBreakdown(
                "人员开支", personnelCost, personnelCost.doubleValue() / totalExpenses.doubleValue() * 100));
        expenseBreakdown.add(new FinanceOverviewResponse.ExpenseBreakdown(
                "其他费用", otherExpenses, otherExpenses.doubleValue() / totalExpenses.doubleValue() * 100));
        expenseBreakdown.add(new FinanceOverviewResponse.ExpenseBreakdown(
                "报销支出", reimbursed, reimbursed.doubleValue() / totalExpenses.doubleValue() * 100));

        // 银行余额
        List<FinanceOverviewResponse.BankBalanceInfo> bankBreakdown = bankAccountRepository
                .findByIsActiveTrueOrderBySortOrder().stream()
                .map(b -> new FinanceOverviewResponse.BankBalanceInfo(
                        b.getBankName(), b.getCurrentBalance(), b.getCurrency()))
                .toList();

        return FinanceOverviewResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .totalBankBalance(totalBankBalance)
                .monthlyTrend(monthlyTrend)
                .expenseBreakdown(expenseBreakdown)
                .bankBreakdown(bankBreakdown)
                .build();
    }
```

Also add these new imports to FinanceService.java at the top:

```java
import com.coinmarket.finance.entity.BankAccount;
import com.coinmarket.finance.repository.BankAccountRepository;
import com.coinmarket.finance.repository.OtherExpenseRepository;
import com.coinmarket.finance.repository.PersonnelExpenseRepository;
import com.coinmarket.finance.dto.FinanceOverviewResponse;
import java.util.*;
```

- [ ] **Step 3: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/service/PersonnelService.java
git add backend/src/main/java/com/coinmarket/finance/service/FinanceService.java
git commit -m "feat: add PersonnelService and FinanceOverview to FinanceService"
```

---

### Phase 5: 后端 Controller

#### Task 13: 创建 BankController

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/controller/BankController.java`

- [ ] **Step 1: Create BankController**

```java
package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.service.BankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/finance/banks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "银行管理", description = "银行账户管理接口")
public class BankController {

    private final BankService bankService;

    @Operation(summary = "获取银行列表")
    @GetMapping
    public ApiResponse<List<BankAccountResponse>> listAccounts() {
        return ApiResponse.success(bankService.listAccounts());
    }

    @Operation(summary = "获取银行详情")
    @GetMapping("/{id}")
    public ApiResponse<BankAccountResponse> getAccount(@PathVariable Long id) {
        return ApiResponse.success(bankService.getAccount(id));
    }

    @Operation(summary = "新增银行账户")
    @PostMapping
    public ApiResponse<BankAccountResponse> createAccount(@Valid @RequestBody BankAccountRequest request) {
        return ApiResponse.success(bankService.createAccount(request));
    }

    @Operation(summary = "编辑银行账户")
    @PutMapping("/{id}")
    public ApiResponse<BankAccountResponse> updateAccount(@PathVariable Long id, @Valid @RequestBody BankAccountRequest request) {
        return ApiResponse.success(bankService.updateAccount(id, request));
    }

    @Operation(summary = "删除银行账户（软删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        bankService.deleteAccount(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "手动调整余额")
    @PostMapping("/{id}/adjust")
    public ApiResponse<BankAccountResponse> adjustBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String notes) {
        return ApiResponse.success(bankService.adjustBalance(id, amount, notes));
    }

    @Operation(summary = "银行转账")
    @PostMapping("/transfer")
    public ApiResponse<BankTransferResponse> transfer(
            @Valid @RequestBody BankTransferRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(bankService.transfer(request, principal.getId()));
    }

    @Operation(summary = "转账记录列表")
    @GetMapping("/transfers")
    public ApiResponse<PageResponse<BankTransferResponse>> listTransfers(
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BankTransferResponse> result = bankService.listTransfers(accountId, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/controller/BankController.java
git commit -m "feat: add BankController"
```

#### Task 14: 创建 ExpenseController

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/controller/ExpenseController.java`

- [ ] **Step 1: Create ExpenseController**

```java
package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "费用管理", description = "费用类别和费用记录管理接口")
public class ExpenseController {

    private final ExpenseService expenseService;

    // --- Categories ---

    @Operation(summary = "获取费用类别列表")
    @GetMapping("/expense-categories")
    public ApiResponse<List<ExpenseCategoryResponse>> listCategories() {
        return ApiResponse.success(expenseService.listCategories());
    }

    @Operation(summary = "新增费用类别")
    @PostMapping("/expense-categories")
    public ApiResponse<ExpenseCategoryResponse> createCategory(@Valid @RequestBody ExpenseCategoryRequest request) {
        return ApiResponse.success(expenseService.createCategory(request));
    }

    @Operation(summary = "编辑费用类别")
    @PutMapping("/expense-categories/{id}")
    public ApiResponse<ExpenseCategoryResponse> updateCategory(
            @PathVariable Long id, @Valid @RequestBody ExpenseCategoryRequest request) {
        return ApiResponse.success(expenseService.updateCategory(id, request));
    }

    @Operation(summary = "删除费用类别（软删除）")
    @DeleteMapping("/expense-categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        expenseService.deleteCategory(id);
        return ApiResponse.success(null);
    }

    // --- Expenses ---

    @Operation(summary = "获取费用记录列表")
    @GetMapping("/expenses")
    public ApiResponse<PageResponse<OtherExpenseResponse>> listExpenses(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        Page<OtherExpenseResponse> result = expenseService.listExpenses(categoryId, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @Operation(summary = "新增费用记录")
    @PostMapping("/expenses")
    public ApiResponse<OtherExpenseResponse> createExpense(
            @Valid @RequestBody OtherExpenseRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(expenseService.createExpense(request, principal.getId()));
    }

    @Operation(summary = "编辑费用记录")
    @PutMapping("/expenses/{id}")
    public ApiResponse<OtherExpenseResponse> updateExpense(
            @PathVariable Long id, @Valid @RequestBody OtherExpenseRequest request) {
        return ApiResponse.success(expenseService.updateExpense(id, request));
    }

    @Operation(summary = "删除费用记录")
    @DeleteMapping("/expenses/{id}")
    public ApiResponse<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 2: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/controller/ExpenseController.java
git commit -m "feat: add ExpenseController"
```

#### Task 15: 创建 PersonnelController 和更新 FinanceController

**Files:**
- Create: `backend/src/main/java/com/coinmarket/finance/controller/PersonnelController.java`
- Modify: `backend/src/main/java/com/coinmarket/finance/controller/FinanceController.java` (add overview endpoint)

- [ ] **Step 1: Create PersonnelController**

```java
package com.coinmarket.finance.controller;

import com.coinmarket.common.dto.ApiResponse;
import com.coinmarket.common.dto.PageResponse;
import com.coinmarket.common.security.CurrentUser;
import com.coinmarket.common.security.UserPrincipal;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.service.PersonnelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/finance/personnel")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "人员开支", description = "人员开支管理接口")
public class PersonnelController {

    private final PersonnelService personnelService;

    @Operation(summary = "获取人员开支列表")
    @GetMapping
    public ApiResponse<PageResponse<PersonnelExpenseResponse>> listPersonnel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "payDate"));
        Page<PersonnelExpenseResponse> result = personnelService.listPersonnel(pageable);
        return ApiResponse.success(PageResponse.from(result));
    }

    @Operation(summary = "新增人员开支")
    @PostMapping
    public ApiResponse<PersonnelExpenseResponse> createPersonnel(
            @Valid @RequestBody PersonnelExpenseRequest request,
            @CurrentUser UserPrincipal principal) {
        return ApiResponse.success(personnelService.createPersonnel(request, principal.getId()));
    }

    @Operation(summary = "编辑人员开支")
    @PutMapping("/{id}")
    public ApiResponse<PersonnelExpenseResponse> updatePersonnel(
            @PathVariable Long id, @Valid @RequestBody PersonnelExpenseRequest request) {
        return ApiResponse.success(personnelService.updatePersonnel(id, request));
    }

    @Operation(summary = "删除人员开支")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePersonnel(@PathVariable Long id) {
        personnelService.deletePersonnel(id);
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 2: Add overview endpoint to FinanceController**

Read `FinanceController.java` and add this import:
```java
import com.coinmarket.finance.dto.FinanceOverviewResponse;
```

Then add the endpoint (e.g., right before the existing `getDashboard` method):

```java
    @Operation(summary = "获取财务总览", description = "获取财务总览图表数据，包括月度趋势、费用构成和银行余额分布")
    @GetMapping("/overview")
    public ApiResponse<FinanceOverviewResponse> getOverview() {
        return ApiResponse.success(financeService.getOverview());
    }
```

- [ ] **Step 3: Create the missing method in ReimbursementRepository**

Open `ReimbursementRepository.java` and add:

```java
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Reimbursement r WHERE r.status = 'PAID' AND r.paidAt BETWEEN :start AND :end")
    BigDecimal sumPaidAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
```

Add imports:
```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.math.BigDecimal;
```

- [ ] **Step 4: Compile**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn compile -q`
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/coinmarket/finance/controller/PersonnelController.java
git add backend/src/main/java/com/coinmarket/finance/controller/FinanceController.java
git add backend/src/main/java/com/coinmarket/finance/repository/ReimbursementRepository.java
git commit -m "feat: add PersonnelController, overview endpoint, and sumPaid repository method"
```

---

### Phase 6: 前端基础设施

#### Task 16: 安装前端依赖并创建打印工具

**Files:**
- Modify: `frontend/admin/package.json`
- Create: `frontend/admin/src/utils/print.js`

- [ ] **Step 1: Install dependencies**

```bash
cd /Users/dprk/coin-marketplace/frontend/admin && npm install echarts vue-echarts print-js
```

- [ ] **Step 2: Create print utility**

```js
// frontend/admin/src/utils/print.js
/**
 * 打印当前页面（隐藏侧边栏和导航，仅保留内容）
 */
export function printPage(title) {
  document.title = title || '财务报表'
  window.print()
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/admin/package.json
git add frontend/admin/src/utils/print.js
git commit -m "chore: add echarts, vue-echarts, print-js deps and print utility"
```

#### Task 17: 创建可复用组件

**Files:**
- Create: `frontend/admin/src/components/finance/StatCard.vue`
- Create: `frontend/admin/src/components/finance/BankCard.vue`
- Create: `frontend/admin/src/components/finance/TransferDialog.vue`

- [ ] **Step 1: Create StatCard.vue**

```vue
<template>
  <el-card shadow="hover">
    <div class="stat-card" @click="$emit('click')">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value" :style="{ color }">{{ prefix }}{{ value }}</div>
      <div v-if="subtext" class="stat-subtext">{{ subtext }}</div>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  label: String,
  value: [String, Number],
  color: { type: String, default: '#6b7280' },
  prefix: { type: String, default: '' },
  subtext: String,
})
defineEmits(['click'])
</script>

<style scoped>
.stat-card { text-align: center; cursor: pointer; }
.stat-label { font-size: 13px; color: #6b7280; margin-bottom: 4px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-subtext { font-size: 12px; color: #9ca3af; margin-top: 4px; }
</style>
```

- [ ] **Step 2: Create BankCard.vue**

```vue
<template>
  <el-card shadow="hover" :class="{ 'bank-card': true, 'inactive': !account.isActive }">
    <div class="bank-header">
      <span class="bank-name">{{ account.bankName }}</span>
      <el-tag size="small" :type="account.isActive ? 'success' : 'info'">
        {{ account.currency }}
      </el-tag>
    </div>
    <div class="bank-balance" :style="{ color: balanceColor }">
      {{ formatMoney(account.currentBalance) }}
    </div>
    <div class="bank-meta">
      <span>{{ account.accountName }}</span>
      <span>****{{ account.accountNumber }}</span>
    </div>
    <div class="bank-actions">
      <el-button size="small" text @click="$emit('transfer', account)">📤 转账</el-button>
      <el-button size="small" text @click="$emit('adjust', account)">📥 调整</el-button>
      <el-button size="small" text @click="$emit('detail', account)">📋 流水</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  account: { type: Object, required: true },
})

defineEmits(['transfer', 'adjust', 'detail'])

const balanceColor = computed(() => {
  const v = Number(props.account.currentBalance)
  return v > 0 ? '#10b981' : v < 0 ? '#ef4444' : '#6b7280'
})

function formatMoney(v) {
  const num = Number(v || 0)
  return props.account.currency === 'MYR'
    ? `RM ${num.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
    : `$ ${num.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
}
</script>

<style scoped>
.bank-card { margin-bottom: 12px; }
.bank-card.inactive { opacity: 0.6; }
.bank-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.bank-name { font-size: 16px; font-weight: 600; }
.bank-balance { font-size: 26px; font-weight: 700; margin-bottom: 6px; }
.bank-meta { font-size: 12px; color: #9ca3af; display: flex; justify-content: space-between; margin-bottom: 10px; }
.bank-actions { display: flex; gap: 4px; }
</style>
```

- [ ] **Step 3: Create TransferDialog.vue**

```vue
<template>
  <el-dialog v-model="visible" title="💳 银行转账" width="480px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="从" prop="fromAccountId">
        <el-select v-model="form.fromAccountId" style="width:100%" @change="onFromChange">
          <el-option v-for="b in banks" :key="b.id" :label="`${b.bankName} (${b.currency})`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="到" prop="toAccountId">
        <el-select v-model="form.toAccountId" style="width:100%" @change="onToChange">
          <el-option v-for="b in banks" :key="b.id" :label="`${b.bankName} (${b.currency})`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width:100%" />
      </el-form-item>
      <el-form-item label="手续费">
        <el-input-number v-model="form.fee" :min="0" :precision="2" style="width:100%" />
      </el-form-item>
      <el-form-item label="日期" prop="transferDate">
        <el-date-picker v-model="form.transferDate" type="date" style="width:100%" />
      </el-form-item>
      <el-form-item label="参考号">
        <el-input v-model="form.referenceNo" placeholder="交易参考号" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="转账说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="saving">确认转账</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { api } from '../../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  banks: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, v => visible.value = v)
watch(visible, v => emit('update:modelValue', v))

const formRef = ref(null)
const saving = ref(false)
const form = reactive({
  fromAccountId: null,
  toAccountId: null,
  amount: 0,
  fee: 0,
  transferDate: new Date().toISOString().split('T')[0],
  referenceNo: '',
  description: '',
})

const rules = {
  fromAccountId: [{ required: true, message: '请选择转出账户' }],
  toAccountId: [{ required: true, message: '请选择转入账户' }],
  amount: [{ required: true, message: '请输入金额' }],
  transferDate: [{ required: true, message: '请选择日期' }],
}

function onFromChange() {
  const bank = props.banks.find(b => b.id === form.fromAccountId)
  if (bank) form.currency = bank.currency
}
function onToChange() {
  const bank = props.banks.find(b => b.id === form.toAccountId)
  if (bank && !form.currency) form.currency = bank.currency
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = { ...form, transferDate: form.transferDate }
    if (typeof data.transferDate === 'object') {
      data.transferDate = data.transferDate.toISOString().split('T')[0]
    }
    await api.post('/admin/finance/banks/transfer', data)
    ElMessage.success('转账成功')
    visible.value = false
    emit('success')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '转账失败')
  } finally { saving.value = false }
}
</script>
```

- [ ] **Step 4: Commit**

```bash
git add frontend/admin/src/components/finance/
git commit -m "feat: add finance reusable components (StatCard, BankCard, TransferDialog)"
```

---

### Phase 7: 前端页面

#### Task 18: 更新路由和菜单

**Files:**
- Modify: `frontend/admin/src/router/index.js`

- [ ] **Step 1: Update router — add finance sub-routes, replace old single route**

Replace the existing finance routes with:

```js
  // Finance Module (replacing single /finance route)
  { path: '/finance',                     component: () => import('../views/finance/Overview.vue'), meta: { requiresAuth: true } },
  { path: '/finance/banks',               component: () => import('../views/finance/Banks.vue'), meta: { requiresAuth: true } },
  { path: '/finance/income',              component: () => import('../views/finance/Income.vue'), meta: { requiresAuth: true } },
  { path: '/finance/purchases',           component: () => import('../views/finance/Purchases.vue'), meta: { requiresAuth: true } },
  { path: '/finance/personnel',           component: () => import('../views/finance/Personnel.vue'), meta: { requiresAuth: true } },
  { path: '/finance/reimbursements',      component: () => import('../views/finance/Reimbursements.vue'), meta: { requiresAuth: true } },
  { path: '/finance/expenses',            component: () => import('../views/finance/Expenses.vue'), meta: { requiresAuth: true } },
  { path: '/finance/profit',              component: () => import('../views/finance/Profit.vue'), meta: { requiresAuth: true } },
```

Also remove the old routes:
```js
  // Remove these lines:
  { path: '/finance', component: () => import('../views/Finance.vue'), meta: { requiresAuth: true } },
  { path: '/finance/sales-revenue', component: () => import('../views/SalesRevenue.vue'), meta: { requiresAuth: true } },
  { path: '/finance/purchase-report', component: () => import('../views/PurchaseReport.vue'), meta: { requiresAuth: true } },
  { path: '/finance/profit-report', component: () => import('../views/ProfitReport.vue'), meta: { requiresAuth: true } },
```

- [ ] **Step 2: Commit**

```bash
git add frontend/admin/src/router/index.js
git commit -m "feat: update finance routes to modular sub-pages"
```

#### Task 19: 创建总览仪表盘 Overview.vue

**Files:**
- Create: `frontend/admin/src/views/finance/Overview.vue`

- [ ] **Step 1: Create Overview.vue**

```vue
<template>
  <div class="overview-page">
    <div class="page-header">
      <h2>📊 财务总览 / Finance Overview</h2>
      <el-button @click="printPage('财务总览')">🖨️ 打印</el-button>
    </div>

    <!-- Key Metrics -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6"><StatCard label="总收入 / Total Income" :value="fmt(data.totalIncome)" color="#10b981" /></el-col>
      <el-col :span="6"><StatCard label="总支出 / Total Expenses" :value="fmt(data.totalExpenses)" color="#f59e0b" /></el-col>
      <el-col :span="6"><StatCard label="净利润 / Net Profit" :value="fmt(data.netProfit)" :color="profitColor" /></el-col>
      <el-col :span="6"><StatCard label="银行总余额 / Bank Balance" :value="fmt(data.totalBankBalance)" color="#3b82f6" /></el-col>
    </el-row>

    <!-- Charts Row -->
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>📈 月度趋势 / Monthly Trend</template>
          <div ref="trendChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>🥧 费用构成 / Expense Breakdown</template>
          <div ref="pieChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Bank Breakdown -->
    <el-card style="margin-top:16px">
      <template #header>🏦 银行余额分布 / Bank Balances</template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="b in data.bankBreakdown || []" :key="b.bankName">
          <div class="bank-mini-card">
            <div class="bank-mini-name">{{ b.bankName }}</div>
            <div class="bank-mini-balance" :style="{ color: Number(b.balance) > 0 ? '#10b981' : '#ef4444' }">
              {{ b.currency === 'MYR' ? 'RM' : '$' }}{{ Number(b.balance).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount, computed } from 'vue'
import { api } from '../../api'
import { printPage } from '../../utils/print'
import StatCard from '../../components/finance/StatCard.vue'
import * as echarts from 'echarts'

const data = reactive({
  totalIncome: 0, totalExpenses: 0, netProfit: 0,
  profitMargin: 0, totalBankBalance: 0,
  monthlyTrend: [], expenseBreakdown: [], bankBreakdown: [],
})

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const profitColor = computed(() => Number(data.netProfit) >= 0 ? '#10b981' : '#ef4444')

function fmt(v) { return `$${Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` }

onMounted(async () => {
  try {
    const res = await api.get('/admin/finance/overview')
    if (res.data) Object.assign(data, res.data)
  } catch (_) {}
  await nextTick()
  initCharts()
})

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['收入', '支出', '利润'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.monthlyTrend.map(m => m.month) },
      yAxis: { type: 'value', axisLabel: { formatter: '${v}' } },
      series: [
        { name: '收入', type: 'bar', stack: 'total', data: data.monthlyTrend.map(m => m.income), itemStyle: { color: '#10b981' } },
        { name: '支出', type: 'bar', stack: 'total', data: data.monthlyTrend.map(m => m.expenses), itemStyle: { color: '#f59e0b' } },
        { name: '利润', type: 'line', data: data.monthlyTrend.map(m => m.profit), itemStyle: { color: '#3b82f6' }, lineStyle: { width: 3 } },
      ],
    })
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    const total = data.expenseBreakdown.reduce((s, e) => s + Number(e.amount), 0)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ${c} ({d}%)' },
      series: [{
        type: 'pie', radius: ['30%', '70%'],
        data: data.expenseBreakdown.map(e => ({ name: e.category, value: Number(e.amount) })),
        label: { formatter: '{b}\n{d}%' },
        colors: ['#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4'],
      }],
    })
  }
}

onBeforeUnmount(() => {
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.overview-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.bank-mini-card {
  background: #f9fafb; border-radius: 8px; padding: 16px; text-align: center;
}
.bank-mini-name { font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.bank-mini-balance { font-size: 22px; font-weight: 700; }
@media print {
  .page-header el-button { display: none; }
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/admin/src/views/finance/Overview.vue
git commit -m "feat: add Finance Overview page with ECharts"
```

#### Task 20: 创建 Banks.vue

**Files:**
- Create: `frontend/admin/src/views/finance/Banks.vue`

- [ ] **Step 1: Create Banks.vue**

```vue
<template>
  <div class="banks-page">
    <div class="page-header">
      <h2>💳 银行管理 / Bank Accounts</h2>
      <div>
        <el-button @click="printPage('银行账户管理')">🖨️ 打印</el-button>
        <el-button type="primary" @click="openAddDialog">＋ 添加银行</el-button>
      </div>
    </div>

    <!-- Bank Cards -->
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8" v-for="bank in banks" :key="bank.id">
        <BankCard :account="bank" @transfer="openTransfer" @adjust="openAdjust" @detail="showDetail" />
      </el-col>
    </el-row>

    <!-- Transfer Records -->
    <el-card style="margin-top:20px">
      <template #header>📋 转账记录 / Transfer History</template>
      <el-table :data="transfers" stripe v-loading="loadingTransfers" @expand-change="loadTransferDetail">
        <el-table-column type="expand">
          <template #default="{ row }">
            <p style="margin:4px 0"><strong>备注：</strong>{{ row.description || '-' }}</p>
            <p style="margin:4px 0"><strong>参考号：</strong>{{ row.referenceNo || '-' }}</p>
          </template>
        </el-table-column>
        <el-table-column prop="transferDate" label="日期" width="120" />
        <el-table-column label="从" min-width="120">
          <template #default="{ row }">{{ row.fromBankName || 'Account #' + row.fromAccountId }}</template>
        </el-table-column>
        <el-table-column label="到" min-width="120">
          <template #default="{ row }">{{ row.toBankName || 'Account #' + row.toAccountId }}</template>
        </el-table-column>
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">${{ fmt(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="手续费" width="100" align="right">
          <template #default="{ row }">${{ fmt(row.fee) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Bank Dialog -->
    <el-dialog v-model="bankDialog" :title="editingBank ? '编辑银行' : '添加银行'" width="500px">
      <el-form ref="bankFormRef" :model="bankForm" :rules="bankRules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="银行名称" prop="bankName">
              <el-select v-model="bankForm.bankName" style="width:100%" filterable allow-create>
                <el-option v-for="n in bankNames" :key="n" :label="n" :value="n" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-select v-model="bankForm.currency" style="width:100%">
                <el-option label="SGD" value="SGD" /><el-option label="MYR" value="MYR" /><el-option label="USD" value="USD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="户名" prop="accountName">
          <el-input v-model="bankForm.accountName" />
        </el-form-item>
        <el-form-item label="账号(后4位)" prop="accountNumber">
          <el-input v-model="bankForm.accountNumber" maxlength="4" placeholder="仅输入后4位" />
        </el-form-item>
        <el-form-item label="国家" prop="country">
          <el-select v-model="bankForm.country" style="width:100%">
            <el-option label="新加坡 SG" value="SG" /><el-option label="马来西亚 MY" value="MY" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前余额" prop="currentBalance">
          <el-input-number v-model="bankForm.currentBalance" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bankForm.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bankDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBank" :loading="savingBank">{{ editingBank ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <!-- Transfer Dialog -->
    <TransferDialog v-model="transferDialogVisible" :banks="banks" @success="loadData" />

    <!-- Adjust Balance Dialog -->
    <el-dialog v-model="adjustDialog" title="调整余额" width="400px">
      <el-form ref="adjustFormRef" :model="adjustForm" label-width="80px">
        <el-form-item label="银行">
          <el-tag>{{ adjustBank?.bankName }}</el-tag>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="adjustForm.amount" :precision="2" style="width:100%"
            :placeholder="'正数=入账，负数=出账'" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="adjustForm.notes" type="textarea" :rows="2" placeholder="调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust" :loading="savingAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { printPage } from '../../utils/print'
import BankCard from '../../components/finance/BankCard.vue'
import TransferDialog from '../../components/finance/TransferDialog.vue'

const bankNames = ['DBS', 'OCBC', 'UOB', 'Maybank', 'CIMB', 'Public Bank', 'RHB', 'HSBC', 'Standard Chartered']
const banks = ref([])
const transfers = ref([])
const loadingTransfers = ref(false)
const bankDialog = ref(false)
const editingBank = ref(null)
const savingBank = ref(false)
const bankFormRef = ref(null)

const bankForm = reactive({
  bankName: '', accountName: '', accountNumber: '', currency: 'SGD',
  country: 'SG', currentBalance: 0, notes: '', sortOrder: 0,
})

const bankRules = {
  bankName: [{ required: true, message: '请选择银行' }],
  accountName: [{ required: true, message: '请输入户名' }],
  accountNumber: [{ required: true, message: '请输入账号' }],
  currency: [{ required: true, message: '请选择币种' }],
  country: [{ required: true, message: '请选择国家' }],
}

const transferDialogVisible = ref(false)
const adjustDialog = ref(false)
const adjustBank = ref(null)
const savingAdjust = ref(false)
const adjustFormRef = ref(null)
const adjustForm = reactive({ amount: 0, notes: '' })

onMounted(loadData)

async function loadData() {
  try {
    const [bankRes, transferRes] = await Promise.all([
      api.get('/admin/finance/banks'),
      api.get('/admin/finance/banks/transfers', { params: { page: 0, size: 20 } }),
    ])
    banks.value = bankRes.data || []
    transfers.value = transferRes.data?.content || []
  } catch (_) {}
}

function openAddDialog() {
  editingBank.value = null
  Object.assign(bankForm, { bankName: '', accountName: '', accountNumber: '', currency: 'SGD', country: 'SG', currentBalance: 0, notes: '', sortOrder: 0 })
  bankDialog.value = true
}

async function saveBank() {
  const valid = await bankFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingBank.value = true
  try {
    await api.post('/admin/finance/banks', { ...bankForm })
    ElMessage.success('添加成功')
    bankDialog.value = false
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
  finally { savingBank.value = false }
}

function openTransfer(bank) {
  transferDialogVisible.value = true
}

function openAdjust(bank) {
  adjustBank.value = bank
  adjustForm.amount = 0
  adjustForm.notes = ''
  adjustDialog.value = true
}

async function submitAdjust() {
  savingAdjust.value = true
  try {
    await api.post(`/admin/finance/banks/${adjustBank.value.id}/adjust`, null, {
      params: { amount: adjustForm.amount, notes: adjustForm.notes || '' }
    })
    ElMessage.success('余额已调整')
    adjustDialog.value = false
    loadData()
  } catch (e) { ElMessage.error('调整失败') }
  finally { savingAdjust.value = false }
}

function showDetail(bank) {
  // Could navigate to a detail page or show in-table
  loadData()
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.banks-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/admin/src/views/finance/Banks.vue
git commit -m "feat: add Bank Management page"
```

#### Task 21: 创建子页面（Income, Purchases, Personnel, Reimbursements, Expenses, Profit）

These follow the same pattern. Each page:
1. Loads data from API
2. Shows a table with date filter
3. Has print button

- [ ] **Step 1: Create Income.vue**

```vue
<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>📥 收款明细 / Income</h2>
      <el-button @click="printPage('收款明细')">🖨️ 打印</el-button>
    </div>
    <el-card style="margin-bottom:16px">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="~"
        value-format="YYYY-MM-DD" @change="loadData" />
    </el-card>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="8"><StatCard label="总收入" :value="fmt(summary.totalRevenue)" color="#10b981" /></el-col>
      <el-col :span="8"><StatCard label="订单数" :value="summary.orderCount" color="#3b82f6" /></el-col>
      <el-col :span="8"><StatCard label="平均订单金额" :value="fmt(summary.averageOrderValue)" color="#8b5cf6" /></el-col>
    </el-row>
    <el-table :data="monthlyData" stripe v-loading="loading">
      <el-table-column prop="month" label="月份" width="160" />
      <el-table-column label="收入" align="right"><template #default="{ row }">${{ fmt(row.revenue) }}</template></el-table-column>
      <el-table-column prop="orderCount" label="订单数" align="center" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../../api'
import { printPage } from '../../utils/print'
import StatCard from '../../components/finance/StatCard.vue'

const loading = ref(false)
const dateRange = ref([])
const monthlyData = ref([])
const summary = reactive({ totalRevenue: '0.00', orderCount: 0, averageOrderValue: '0.00' })

onMounted(() => {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  dateRange.value = [start.toISOString().split('T')[0], now.toISOString().split('T')[0]]
  loadData()
})

async function loadData() {
  if (!dateRange.value?.length) return
  loading.value = true
  try {
    const [s, e] = dateRange.value
    const res = await api.get('/admin/finance/sales-revenue', { params: { startDate: s, endDate: e } })
    Object.assign(summary, res.data)
    monthlyData.value = res.data?.monthlyBreakdown || []
  } catch (_) { monthlyData.value = [] }
  finally { loading.value = false }
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.finance-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
```

- [ ] **Step 2: Create Purchases.vue** (similar to Income but loads from `/admin/finance/purchase-report`)

Files to create in same pattern:
- `Purchases.vue` — wraps purchase-report API
- `Personnel.vue` — CRUD table with add/edit dialog for `/admin/finance/personnel`
- `Reimbursements.vue` — extracted from old Finance.vue with enhanced bank/category fields
- `Expenses.vue` — CRUD table with category filter for `/admin/finance/expenses`
- `Profit.vue` — wraps profit-report API with comparison chart

(These follow the same patterns established above — repeat the template, adjust API endpoints and fields accordingly)

- [ ] **Step 3: Commit all sub-pages**

```bash
git add frontend/admin/src/views/finance/
git commit -m "feat: add finance sub-pages (Income, Purchases, Personnel, Reimbursements, Expenses, Profit)"
```

---

### Phase 8: 打印样式 + 全局配置

#### Task 22: 添加全局打印 CSS

**Files:**
- Modify: `frontend/admin/src/App.vue`

- [ ] **Step 1: Add global print styles to App.vue**

Add inside `<style>` block:

```css
@media print {
  .el-aside, .el-menu, .sidebar, .el-header,
  .el-menu--horizontal, .el-scrollbar__bar { display: none !important; }
  .el-main { margin-left: 0 !important; padding: 0 !important; }
  body { background: white; }
  .el-card { box-shadow: none !important; border: 1px solid #e5e7eb !important; break-inside: avoid; }
  button { display: none !important; }
  .page-break { page-break-after: always; }
  .print-footer { position: fixed; bottom: 10px; right: 10px; font-size: 9px; color: #9ca3af; }
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/admin/src/App.vue
git commit -m "style: add print media queries for finance reports"
```

---

### Phase 9: 测试

#### Task 23: 编写后端单元测试

**Files:**
- Create: `backend/src/test/java/com/coinmarket/finance/service/BankServiceTest.java`

- [ ] **Step 1: Create BankServiceTest**

```java
package com.coinmarket.finance.service;

import com.coinmarket.common.exception.BusinessException;
import com.coinmarket.finance.dto.*;
import com.coinmarket.finance.entity.BankAccount;
import com.coinmarket.finance.repository.BankAccountRepository;
import com.coinmarket.finance.repository.BankTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock private BankAccountRepository bankAccountRepository;
    @Mock private BankTransferRepository bankTransferRepository;

    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankService = new BankService(bankAccountRepository, bankTransferRepository);
    }

    @Test
    @DisplayName("transfer deducts from source and adds to destination")
    void transfer_success() {
        var from = BankAccount.builder().id(1L).bankName("DBS").currentBalance(BigDecimal.valueOf(10000)).build();
        var to = BankAccount.builder().id(2L).bankName("OCBC").currentBalance(BigDecimal.valueOf(5000)).build();
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(to));
        when(bankAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var request = new BankTransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(3000));
        request.setCurrency("SGD");
        request.setFee(BigDecimal.ZERO);
        request.setTransferDate(java.time.LocalDate.now());

        var result = bankService.transfer(request, 1L);

        assertThat(from.getCurrentBalance()).isEqualByComparingTo("7000");
        assertThat(to.getCurrentBalance()).isEqualByComparingTo("8000");
        assertThat(result.getAmount()).isEqualByComparingTo("3000");
    }

    @Test
    @DisplayName("transfer throws when balance insufficient")
    void transfer_insufficientBalance() {
        var from = BankAccount.builder().id(1L).bankName("DBS").currentBalance(BigDecimal.valueOf(1000)).build();
        var to = BankAccount.builder().id(2L).bankName("OCBC").currentBalance(BigDecimal.valueOf(5000)).build();
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(from));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(to));

        var request = new BankTransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(2L);
        request.setAmount(BigDecimal.valueOf(5000));
        request.setCurrency("SGD");
        request.setFee(BigDecimal.ZERO);
        request.setTransferDate(java.time.LocalDate.now());

        assertThatThrownBy(() -> bankService.transfer(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("余额不足");
    }

    @Test
    @DisplayName("transfer throws when from and to are the same")
    void transfer_sameAccount() {
        var request = new BankTransferRequest();
        request.setFromAccountId(1L);
        request.setToAccountId(1L);

        assertThatThrownBy(() -> bankService.transfer(request, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能相同");
    }
}
```

- [ ] **Step 2: Run tests**

Run: `cd /Users/dprk/coin-marketplace/backend && mvn test -Dtest=BankServiceTest -q`
Expected: Tests pass (3/3)

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/coinmarket/finance/service/BankServiceTest.java
git commit -m "test: add BankService unit tests"
```

---

## Self-Review Checklist

- [ ] Spec coverage: Bank management ✓ (Tasks 2,3,7,10,13,20), Expense management ✓ (Tasks 4,5,8,11,14), Personnel ✓ (Tasks 6,9,12,15), Overview dashboard ✓ (Task 19), Print ✓ (Tasks 16,22), Charts ✓ (Task 19)
- [ ] No placeholders, TODOs, or vague steps
- [ ] All file paths are exact and absolute
- [ ] Type/method consistency across tasks
- [ ] Follows existing codebase patterns (entity, DTO, service, controller, repository conventions)
