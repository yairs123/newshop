#!/bin/bash

# ============================================
# CoinMarket Platform - 一键启动脚本
# 双击此文件即可启动所有服务
# ============================================

# 获取脚本所在目录
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

echo "=========================================="
echo "  CoinMarket Platform 启动中..."
echo "=========================================="
echo ""

# 1. 启动 PostgreSQL（Docker）
echo "[1/4] 启动数据库 PostgreSQL + Redis..."
docker compose up -d postgres redis 2>/dev/null
if [ $? -eq 0 ]; then
  echo "  ✅ PostgreSQL 启动成功"
else
  echo "  ⚠️  请确保 Docker Desktop 已运行"
  echo "  打开 Docker Desktop 后重新双击此脚本"
  read -p "按回车键退出..."
  exit 1
fi

# 等待数据库就绪
echo "  等待数据库就绪..."
for i in 1 2 3 4 5 6 7 8; do
  sleep 2
  python3 -c "import socket; s=socket.socket(); s.settimeout(2); s.connect(('localhost',5432)); s.close(); print('ready')" 2>/dev/null && break
done

# 2. 启动后端
echo "[2/4] 启动后端服务 (port 8080)..."
cd "$DIR/backend"
mvn spring-boot:run > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
echo "  后端 PID: $BACKEND_PID"

# 等待后端就绪
echo "  等待后端就绪（约30秒）..."
for i in 1 2 3 4 5 6 7 8 9 10 11 12; do
  sleep 3
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/api/products 2>/dev/null)
  if [ "$STATUS" = "200" ] || [ "$STATUS" = "404" ]; then
    echo "  ✅ 后端启动成功"
    break
  fi
  echo "  等待中... ($i)"
done

# 3. 启动管理后台
echo "[3/4] 启动管理后台 (port 3002)..."
cd "$DIR/frontend/admin"
npx vite --port 3002 > /tmp/frontend-admin.log 2>&1 &
echo "  管理后台 PID: $!"
sleep 3

# 4. 启动商城前台
echo "[4/4] 启动商城前台 (port 3000)..."
cd "$DIR/frontend/store"
npx vite --port 3000 > /tmp/frontend-store.log 2>&1 &
echo "  商城前台 PID: $!"
sleep 3

echo ""
echo "=========================================="
echo "  🎉 所有服务已启动！"
echo "=========================================="
echo ""
echo "  📍 管理后台: http://localhost:3002"
echo "  📍 商城前台: http://localhost:3000"
echo "  📍 后端 API: http://localhost:8080"
echo ""
echo "  👤 管理员账号"
echo "    用户名: admin"
echo "    密码:   admin123"
echo ""
echo "  ℹ️  关闭所有服务请按 Ctrl+C"
echo "=========================================="

# 保存账号信息到同级目录
cat > "$DIR/账号信息.txt" << EOF
========================================
CoinMarket Platform - 账号信息
========================================

管理后台: http://localhost:3002
商城前台: http://localhost:3000

管理员账号
  用户名: admin
  密码:   admin123

========================================
EOF

# 等待用户按 Ctrl+C
wait
