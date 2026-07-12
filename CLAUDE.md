# Coin Marketplace Platform

多商家硬币/收藏品交易平台。

## 项目结构

```
coin-marketplace/
├── backend/           # Spring Boot 3.3.0 + Java 21 + MyBatis-Plus
│   └── src/main/java/com/coinmarket/
│       ├── admin/     # 后台管理
│       ├── auth/      # 认证授权
│       ├── cart/      # 购物车
│       ├── common/    # 公共工具
│       ├── finance/   # 财务
│       ├── order/     # 订单
│       ├── payment/   # 支付
│       ├── product/   # 商品
│       ├── search/    # 搜索
│       ├── seller/    # 卖家
│       └── user/      # 用户
├── frontend/
│   ├── store/         # 买家商城 (Vue 3, port 3003)
│   ├── seller/        # 卖家端 (Vue 3)
│   └── admin/         # 管理后台 (Vue 3, ECharts)
└── docker-compose.yml # PostgreSQL
```

## 启动方式

```bash
# 数据库
docker compose up -d

# 后端
cd backend && ./mvnw spring-boot:run

# 前端 (store)
cd frontend/store && npm run dev   # port 3003
```

## 技术栈

| 层 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3.0, Java 21, MyBatis-Plus, PostgreSQL |
| 前端 | Vue 3.4, Vite 5, Element Plus 2.7, Pinia, Vue I18n |
| 数据库 | PostgreSQL (Docker) |

## 沟通方式

- 可以讲中文
- 喜欢直接给代码/配置，附带简短说明
- 倾向于主动执行操作而非只给建议
- 请用 Markdown 文件链接 `[文件名](路径)` 引用代码
