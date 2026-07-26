<template>
  <div class="dashboard">
    <!-- Page Header -->
    <div class="page-head">
      <div>
        <h2>控制台</h2>
        <p class="page-desc">欢迎回来，这是今天的运营概览</p>
      </div>
      <button class="refresh-btn" @click="load">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
        刷新
      </button>
    </div>

    <!-- Alert Banner -->
    <div v-if="alerts.length > 0" class="alert-strip">
      <div v-for="a in alerts" :key="a.key" class="alert-chip" :class="'alert-' + a.type">
        <span class="alert-emoji">{{ a.icon }}</span>
        <span class="alert-text">{{ a.text }}</span>
        <button class="alert-action" @click="a.action">{{ a.actionText }} →</button>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="stats-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.key" @click="$router.push(card.link)">
        <div class="stat-top">
          <span class="stat-icon" :style="{ background: card.bg }">{{ card.icon }}</span>
          <span :style="{ color: card.color }" class="stat-trend">{{ card.trend }}</span>
        </div>
        <div class="stat-value">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>
    </div>

    <!-- Two-column layout -->
    <div class="content-grid">
      <!-- Low Stock -->
      <div class="content-card">
        <div class="card-head">
          <h3>⚠️ 低库存商品</h3>
          <button v-if="lowStockProducts.length" class="card-action" @click="$router.push('/inventory')">去入库 →</button>
        </div>
        <div v-if="lowStockProducts.length" class="mini-list">
          <div v-for="p in lowStockProducts.slice(0, 8)" :key="p.id" class="mini-item">
            <div class="mini-info">
              <span class="mini-title">{{ p.title }}</span>
              <span class="mini-meta">{{ p.barcode }}</span>
            </div>
            <span class="mini-badge" :class="p.stock <= 1 ? 'badge-danger' : 'badge-warn'">{{ p.stock }}</span>
          </div>
        </div>
        <div v-else class="card-empty">✅ 暂无低库存商品</div>
      </div>

      <!-- Recent Orders -->
      <div class="content-card">
        <div class="card-head">
          <h3>📋 近期订单</h3>
          <button class="card-action" @click="$router.push('/orders')">查看全部 →</button>
        </div>
        <div v-if="recentOrders.length" class="mini-list">
          <div v-for="o in recentOrders.slice(0, 8)" :key="o.id" class="mini-item" @click="$router.push('/orders')">
            <div class="mini-info">
              <span class="mini-title">
                <code class="order-code">{{ o.orderNo }}</code>
                <span class="mini-buyer">{{ o.buyerName }}</span>
              </span>
              <span class="mini-meta">{{ formatDate(o.createdAt) }}</span>
            </div>
            <div class="mini-right">
              <span class="mini-price">{{ o.currency }} {{ formatPrice(o.totalAmount) }}</span>
              <span class="status-dot" :class="'dot-' + o.status"></span>
            </div>
          </div>
        </div>
        <div v-else class="card-empty">暂无订单</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const stats = ref({ productCount: 0, orderCount: 0, userCount: 0, pendingSellerCount: 0 })
const lowStockProducts = ref([])
const recentOrders = ref([])
const allProducts = ref([])

const statCards = computed(() => [
  { key: 'products', icon: '📦', label: '商品总数', value: stats.value.productCount, link: '/products', color: '#3b82f6', bg: 'radial-gradient(circle at 30% 30%, #dbeafe, #bfdbfe)', trend: '' },
  { key: 'orders', icon: '📋', label: '订单总数', value: stats.value.orderCount, link: '/orders', color: '#059669', bg: 'radial-gradient(circle at 30% 30%, #d1fae5, #a7f3d0)', trend: '' },
  { key: 'users', icon: '👤', label: '用户总数', value: stats.value.userCount, link: '/users', color: '#d97706', bg: 'radial-gradient(circle at 30% 30%, #fef3c7, #fde68a)', trend: '' },
  { key: 'sellers', icon: '🤝', label: '待审卖家', value: stats.value.pendingSellerCount, link: '/sellers', color: '#dc2626', bg: 'radial-gradient(circle at 30% 30%, #fee2e2, #fecaca)', trend: '' },
])

const alerts = computed(() => {
  const items = []
  if (stats.value.pendingSellerCount > 0)
    items.push({ key: 'seller', type: 'info', icon: '🤝', text: `${stats.value.pendingSellerCount} 个卖家入驻申请待审核`, action: () => router.push('/sellers'), actionText: '去审核' })
  const paid = recentOrders.value.filter(o => o.status === 'PAID').length
  if (paid > 0)
    items.push({ key: 'ship', type: 'warn', icon: '📦', text: `${paid} 个订单待发货`, action: () => router.push('/orders'), actionText: '去发货' })
  if (lowStockProducts.value.length > 0)
    items.push({ key: 'stock', type: 'danger', icon: '⚠️', text: `${lowStockProducts.value.length} 个商品库存不足（≤3）`, action: () => router.push('/inventory'), actionText: '去入库' })
  return items
})

function formatPrice(val) { return val != null ? Number(val).toFixed(2) : '0.00' }
function formatDate(d) { return d ? d.slice(0, 10) : '-' }

onMounted(async () => {
  try {
    const [pRes, oRes, uRes, sRes] = await Promise.all([
      api.get('/admin/products', { params: { page: 0, size: 200 } }),
      api.get('/admin/orders', { params: { page: 0, size: 10 } }),
      api.get('/admin/users', { params: { page: 0, size: 1 } }),
      api.get('/admin/sellers/applications', { params: { status: 'PENDING' } }),
    ])
    const pd = pRes.data || {}
    allProducts.value = pd.content || []
    stats.value.productCount = pd.totalElements || allProducts.value.length
    stats.value.orderCount = oRes.data?.totalElements || 0
    stats.value.userCount = uRes.data?.totalElements || 0
    stats.value.pendingSellerCount = Array.isArray(sRes.data) ? sRes.data.length : 0
    recentOrders.value = (oRes.data?.content || []).slice(0, 8)
    lowStockProducts.value = allProducts.value.filter(p => p.stock !== undefined && p.stock <= 3 && p.status !== 'INACTIVE').slice(0, 20)
  } catch (e) { /* ignore */ }
})

async function load() { onMounted() }
</script>

<style scoped>
.dashboard { max-width: 1200px; }

/* Page head */
.page-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-head h2 { font-family: 'DM Serif Display', Georgia, serif; font-size: 24px; font-weight: 700; color: var(--ink, #0f172a); margin: 0; }
.page-desc { font-size: 13px; color: var(--text-muted, #94a3b8); margin-top: 2px; }
.refresh-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px; border: 1px solid var(--border, #e5e7eb);
  background: var(--surface, #fff); font-size: 13px; color: var(--text-muted, #94a3b8);
  cursor: pointer; transition: all 0.15s; font-family: inherit;
}
.refresh-btn:hover { border-color: var(--gold, #d4a843); color: var(--gold-dark, #b8932a); }

/* Alert strip */
.alert-strip { display: flex; flex-direction: column; gap: 6px; margin-bottom: 20px; }
.alert-chip {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 16px; border-radius: 10px;
  font-size: 13px; font-weight: 500;
}
.alert-info { background: #eff6ff; color: #1d4ed8; }
.alert-warn { background: #fffbeb; color: #92400e; }
.alert-danger { background: #fef2f2; color: #991b1b; }
.alert-emoji { font-size: 18px; }
.alert-text { flex: 1; }
.alert-action {
  border: none; background: transparent; font-size: 13px; font-weight: 600;
  cursor: pointer; font-family: inherit; padding: 4px 8px; border-radius: 6px;
  color: inherit; transition: background 0.15s;
}
.alert-action:hover { background: rgba(0,0,0,.06); }

/* Stats grid */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card {
  background: var(--surface, #fff); border-radius: var(--radius, 10px);
  padding: 18px 20px; cursor: pointer; border: 1px solid var(--border, #e5e7eb);
  transition: all 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,.06)); }
.stat-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.stat-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.stat-trend { font-size: 12px; font-weight: 600; }
.stat-value { font-size: 28px; font-weight: 800; color: var(--ink, #0f172a); line-height: 1.1; margin-bottom: 4px; }
.stat-label { font-size: 13px; color: var(--text-muted, #94a3b8); }

/* Content grid */
.content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.content-card {
  background: var(--surface, #fff); border-radius: var(--radius, 10px);
  padding: 20px; border: 1px solid var(--border, #e5e7eb);
}
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-head h3 { font-size: 15px; font-weight: 600; color: var(--ink, #0f172a); margin: 0; }
.card-action {
  border: none; background: transparent; font-size: 12px; font-weight: 500;
  color: var(--gold, #d4a843); cursor: pointer; font-family: inherit;
}
.card-action:hover { text-decoration: underline; }
.card-empty { text-align: center; padding: 24px; color: var(--text-muted, #94a3b8); font-size: 13px; }

/* Mini list */
.mini-list { display: flex; flex-direction: column; gap: 4px; }
.mini-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: background 0.15s;
}
.mini-item:hover { background: var(--ivory, #f5f2ed); }
.mini-info { display: flex; flex-direction: column; gap: 2px; overflow: hidden; }
.mini-title { font-size: 13px; font-weight: 500; color: var(--text, #1e293b); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: flex; align-items: center; gap: 8px; }
.mini-meta { font-size: 11px; color: var(--text-muted, #94a3b8); }
.mini-buyer { font-size: 11px; color: var(--text-muted, #94a3b8); }
.mini-badge { display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 8px; font-size: 12px; font-weight: 700; color: #fff; }
.badge-danger { background: var(--danger, #dc2626); }
.badge-warn { background: var(--warning, #d97706); }
.mini-right { display: flex; align-items: center; gap: 8px; }
.mini-price { font-size: 13px; font-weight: 600; color: var(--text, #1e293b); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.dot-PENDING_PAYMENT { background: var(--danger, #dc2626); }
.dot-PAID { background: var(--success, #059669); }
.dot-SHIPPED { background: var(--warning, #d97706); }
.dot-COMPLETED { background: var(--success, #059669); }
.dot-CANCELLED { background: var(--text-muted, #94a3b8); }
.order-code { font-family: 'SF Mono', monospace; font-size: 11px; color: var(--gold-dark, #b8932a); }

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .content-grid { grid-template-columns: 1fr; } }
</style>
