# Deployment Guide

## Development Setup

参见 [README.md](../README.md) 快速开始章节。

## Production Build

### Backend

```bash
cd backend

# 编译并打包（跳过测试）
./mvnw clean package -DskipTests

# 编译并运行测试
./mvnw clean package

# 编译产物
# target/coin-marketplace-1.0.0-SNAPSHOT.jar
```

### Frontend

```bash
# 管理后台
cd frontend/admin
npm install
npm run build     # 输出到 dist/

# 卖家中心
cd frontend/seller
npm install
npm run build     # 输出到 dist/

# 买家商城
cd frontend/store
npm install
npm run build     # 输出到 dist/
```

前端构建产物可以直接部署到 Nginx / CDN。

---

## Docker Deployment

### 使用 Docker Compose（含基础设施）

```bash
# 构建并启动所有服务
docker compose up -d --build

# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 停止并删除数据卷
docker compose down -v
```

### 单独启动后端容器

```bash
# 构建镜像
cd backend
docker build -t coinmarket-backend .

# 运行容器
docker run -d \
  --name coinmarket-backend \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/coin_marketplace \
  -e DB_USERNAME=coinuser \
  -e DB_PASSWORD=coinpass123 \
  -e REDIS_HOST=host.docker.internal \
  -e MQ_HOST=host.docker.internal \
  -e JWT_SECRET=your-256-bit-secret \
  coinmarket-backend
```

---

## 环境变量参考

### 基础配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/coin_marketplace` | 数据库连接 URL |
| `DB_USERNAME` | `coinuser` | 数据库用户名 |
| `DB_PASSWORD` | `coinpass123` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `MQ_HOST` | `localhost` | RabbitMQ 主机 |
| `MQ_PORT` | `5672` | RabbitMQ 端口 |
| `MQ_USERNAME` | `coinmq` | RabbitMQ 用户名 |
| `MQ_PASSWORD` | `coinmq123` | RabbitMQ 密码 |
| `ES_URIS` | `http://localhost:9200` | Elasticsearch URL |
| `JWT_SECRET` | (无默认值) | JWT 签名密钥，生产环境需设置为强密钥 |

### 存储配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `STORAGE_TYPE` | `local` | 存储类型 (`local` 或 `s3`) |
| `UPLOAD_DIR` | `./uploads` (dev) / `/data/uploads` (prod) | 本地文件上传目录 |
| `S3_ENDPOINT` | `http://localhost:9000` | S3 兼容存储端点 |
| `S3_REGION` | `us-east-1` | S3 区域 |
| `S3_ACCESS_KEY` | `minioadmin` | S3 Access Key |
| `S3_SECRET_KEY` | `minioadmin` | S3 Secret Key |
| `S3_BUCKET` | `coinmarket` | S3 Bucket 名称 |

### 评级 API 配置

| 变量名 | 说明 |
|--------|------|
| `NGC_API_KEY` | NGC 评级 API 密钥 |
| `PCGS_API_KEY` | PCGS 评级 API 密钥 |
| `PMG_API_KEY` | PMG 评级 API 密钥 |

### Stripe 支付 (可选)

| 变量名 | 说明 |
|--------|------|
| `STRIPE_SECRET_KEY` | Stripe Secret Key |
| `STRIPE_WEBHOOK_SECRET` | Stripe Webhook Secret |

---

## 数据库迁移

使用 Flyway 进行数据库版本管理：

```
backend/src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_orders.sql
├── V3__add_cart.sql
├── V4__add_news_ads.sql
├── V5__add_audit_log.sql
├── V6__add_barcode_codes.sql
├── V7__add_seller_applications.sql
├── V8__add_support_tickets.sql
└── V9__add_inventory.sql
```

Flyway 在应用启动时自动执行待迁移脚本。

手动执行迁移：

```bash
# 使用 Maven 插件
cd backend
./mvnw flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/coin_marketplace \
  -Dflyway.user=coinuser \
  -Dflyway.password=coinpass123

# 查看迁移状态
./mvnw flyway:info

# 回滚（如支持）
./mvnw flyway:undo
```

---

## 健康检查

后端提供了 Spring Boot Actuator 端点：

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 服务健康状态 |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 应用指标 |

---

## Nginx 配置示例

```nginx
# 前端路由
server {
    listen 80;
    server_name shop.example.com;

    # 买家商城
    location / {
        root /var/www/store;
        try_files $uri $uri/ /index.html;
    }
}

server {
    listen 80;
    server_name seller.example.com;

    # 卖家中心
    location / {
        root /var/www/seller;
        try_files $uri $uri/ /index.html;
    }
}

server {
    listen 80;
    server_name admin.example.com;

    # 管理后台
    location / {
        root /var/www/admin;
        try_files $uri $uri/ /index.html;
    }
}

server {
    listen 80;
    server_name api.example.com;

    # API 反向代理
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 配置文件

生产环境推荐使用 `prod` profile 启动：

```bash
java -jar target/coin-marketplace-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

或通过环境变量：

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar target/coin-marketplace-1.0.0-SNAPSHOT.jar
```
