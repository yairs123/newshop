#!/bin/bash
# =============================================================================
# Coin Marketplace - Development Startup Script
# =============================================================================
# Starts all services for local development:
#   - Docker services (PostgreSQL, Redis, RabbitMQ, Elasticsearch)
#   - Backend (Spring Boot)
#   - Frontends (Admin, Seller, Store)
# =============================================================================

set -e

echo "=== Starting Docker services (PostgreSQL, Redis, RabbitMQ, Elasticsearch) ==="
docker compose up -d

echo "=== Waiting for database to be ready ==="
sleep 5

echo "=== Starting backend (Spring Boot on port 8080) ==="
cd "$(dirname "$0")/backend" && mvn spring-boot:run &
BACKEND_PID=$!

echo "=== Waiting for backend to initialize ==="
sleep 20

echo "=== Starting frontends ==="
cd "$(dirname "$0")/frontend/admin" && npm run dev &
cd "$(dirname "$0")/frontend/seller" && npm run dev &
cd "$(dirname "$0")/frontend/store" && npm run dev &

echo ""
echo "============================================================================="
echo " All services starting..."
echo "   Admin UI:   http://localhost:<admin-port>"
echo "   Seller UI:  http://localhost:<seller-port>"
echo "   Store UI:   http://localhost:3003"
echo "   Backend:    http://localhost:8080"
echo "============================================================================="
echo ""
echo "Press Ctrl+C to stop all services."

# Wait for all background processes
wait
