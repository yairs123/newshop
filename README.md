# Coin Marketplace Platform

多商家硬币/收藏品交易平台。支持买家商城、卖家中心、管理后台三端分离。

## 技术栈
- **后端**: Spring Boot 3.3.0, Java 21, MyBatis-Plus, PostgreSQL, Redis, RabbitMQ, Elasticsearch
- **前端(Admin)**: Vue 3.4 + Element Plus 2.7, port 3002
- **前端(Seller)**: Vue 3.4 + Element Plus 2.7, port 3001
- **前端(Store)**: Vue 3.4 + Element Plus 2.7, port 3000

## 快速开始

### 前置条件
- JDK 21+
- Node.js 18+
- Docker & Docker Compose

### 启动

```bash
# 1. 启动基础设施 (PostgreSQL, Redis, RabbitMQ, Elasticsearch)
docker compose up -d

# 2. 启动后端
cd backend && ./mvnw spring-boot:run

# 3. 启动前端（新开终端）
cd frontend/admin && npm install && npm run dev   # 管理后台 :3002
cd frontend/seller && npm install && npm run dev  # 卖家中心 :3001
cd frontend/store && npm install && npm run dev   # 买家商城 :3000
```

### 一键启动

```bash
bash dev-start.sh
```

### 测试账号

| 用户名 | 密码 | 角色 | 入口 |
|--------|------|------|------|
| admin | 123 | 管理员 | http://localhost:3002 |
| seller | 123 | 卖家 | http://localhost:3001 |
| buyer | 123 | 买家 | http://localhost:3000 |

## 测试

```bash
cd backend && mvn test
# 当前 167+ 测试
```

## 项目结构

```
coin-marketplace/
├── backend/              # Spring Boot 后端
│   └── src/main/java/com/coinmarket/
│       ├── admin/        # 后台管理
│       ├── auth/         # 认证授权
│       ├── cart/         # 购物车
│       ├── common/       # 公共工具
│       ├── finance/      # 财务
│       ├── order/        # 订单
│       ├── payment/      # 支付
│       ├── product/      # 商品
│       ├── search/       # 搜索
│       ├── seller/       # 卖家
│       └── user/         # 用户
├── frontend/
│   ├── store/            # 买家商城 (Vue 3, port 3000)
│   ├── seller/           # 卖家端 (Vue 3, port 3001)
│   └── admin/            # 管理后台 (Vue 3, port 3002)
└── docker-compose.yml    # PostgreSQL + Redis + RabbitMQ + ES
```

## 环境变量

参见 `.env.example`
