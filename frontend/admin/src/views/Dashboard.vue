<template>
  <div class="dashboard">
    <!-- Smart Alert Banner -->
    <el-card v-if="alerts.length > 0" shadow="hover" class="alert-card">
      <div v-for="a in alerts" :key="a.key" class="alert-item" :class="'alert-' + a.type">
        <span class="alert-icon">{{ a.icon }}</span>
        <span class="alert-text">{{ a.text }}</span>
        <el-button size="small" @click="a.action">{{ a.actionText }}</el-button>
      </div>
    </el-card>

    <!-- Stats Cards -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: '3px solid ' + card.color }">
          <div class="stat-body">
            <span class="stat-icon">{{ card.icon }}</span>
            <div class="stat-info">
              <span class="stat-value">{{ card.value }}</span>
              <span class="stat-label">{{ card.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-card shadow="hover" class="action-card">
      <template #header><span style="font-weight:600">⚡ 快捷操作</span></template>
      <div class="quick-actions">
        <el-button type="warning" @click="$router.push('/inventory')">📥 入库</el-button>
        <el-button type="primary" @click="$router.push('/products')">📦 商品管理</el-button>
        <el-button type="success" @click="$router.push('/orders')">📋 待处理订单</el-button>
        <el-button @click="$router.push('/print-labels')">🏷️ 打印条码</el-button>
      </div>
    </el-card>

    <!-- Low Stock Products -->
    <el-card v-if="lowStockProducts.length" shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span style="font-weight:600">⚠️ 低库存商品 ({{ lowStockProducts.length }})</span>
          <el-button size="small" @click="$router.push('/inventory')">去入库</el-button>
        </div>
      </template>
      <el-table :data="lowStockProducts" size="small" @row-click="row => $router.push('/products')">
        <el-table-column prop="barcode" label="条码" width="130" />
        <el-table-column prop="title" label="名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="库存" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stock <= 1 ? 'danger' : 'warning'" size="small">{{ row.stock }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="100" align="right">
          <template #default="{ row }">{{ row.currency || 'USD' }} {{ row.price }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Recent Orders -->
    <el-card v-if="recentOrders.length" shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span style="font-weight:600">📋 近期订单</span>
          <el-button size="small" @click="$router.push('/orders')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentOrders" size="small" @row-click="row => $router.push('/orders')">
        <el-table-column prop="orderNo" label="订单号" width="180">
          <template #default="{ row }"><code style="font-size:12px">{{ row.orderNo }}</code></template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="100" />
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">{{ row.currency || 'USD' }} {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="plain">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'

const router = useRouter()
const { t } = useI18n()

const stats = ref({ productCount: 0, orderCount: 0, userCount: 0, pendingSellerCount: 0 })
const lowStockProducts = ref([])
const recentOrders = ref([])
const allProducts = ref([])

const statCards = computed(() => [
  { key: 'products', icon: '📦', label: '商品总数', value: stats.value.productCount, color: '#409eff' },
  { key: 'orders', icon: '📋', label: '订单总数', value: stats.value.orderCount, color: '#67c23a' },
  { key: 'users', icon: '👤', label: '用户总数', value: stats.value.userCount, color: '#e6a23c' },
  { key: 'sellers', icon: '🤝', label: '待审卖家', value: stats.value.pendingSellerCount, color: '#f56c6c' },
])

const alerts = computed(() => {
  const items = []
  if (stats.value.pendingSellerCount > 0) {
    items.push({ key: 'seller', type: 'info', icon: '🤝', text: `${stats.value.pendingSellerCount} 个卖家入驻申请待审核`, action: () => router.push('/sellers'), actionText: '去审核' })
  }
  const paid = recentOrders.value.filter(o => o.status === 'PAID').length
  if (paid > 0) {
    items.push({ key: 'ship', type: 'warning', icon: '📦', text: `${paid} 个订单待发货`, action: () => router.push('/orders'), actionText: '去发货' })
  }
  if (lowStockProducts.value.length > 0) {
    items.push({ key: 'stock', type: 'danger', icon: '⚠️', text: `${lowStockProducts.value.length} 个商品库存不足（≤3）`, action: () => router.push('/inventory'), actionText: '去入库' })
  }
  return items
})

function statusType(status) {
  switch (status) {
    case 'PENDING_PAYMENT': return 'warning'
    case 'PAID': return 'success'
    case 'SHIPPED': return 'primary'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'info'
    default: return 'info'
  }
}

onMounted(async () => {
  try {
    const [productsRes, ordersRes, usersRes, sellersRes] = await Promise.all([
      api.get('/admin/products', { params: { page: 0, size: 200 } }),
      api.get('/admin/orders', { params: { page: 0, size: 10 } }),
      api.get('/admin/users', { params: { page: 0, size: 1 } }),
      api.get('/admin/sellers/applications', { params: { status: 'PENDING' } }),
    ])
    const pData = productsRes.data || {}
    allProducts.value = pData.content || []
    stats.value.productCount = pData.totalElements || allProducts.value.length
    stats.value.orderCount = ordersRes.data?.totalElements || 0
    stats.value.userCount = usersRes.data?.totalElements || 0
    stats.value.pendingSellerCount = Array.isArray(sellersRes.data) ? sellersRes.data.length : 0
    recentOrders.value = (ordersRes.data?.content || []).slice(0, 8)
    lowStockProducts.value = allProducts.value.filter(p => p.stock !== undefined && p.stock <= 3 && p.status !== 'INACTIVE').slice(0, 20)
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.dashboard { max-width: 1200px; }

/* Alert banner */
.alert-card { margin-bottom: 16px; padding: 0; }
.alert-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; border-radius: 8px; margin-bottom: 6px;
}
.alert-item:last-child { margin-bottom: 0; }
.alert-info { background: #f0f9ff; }
.alert-warning { background: #fffbeb; }
.alert-danger { background: #fef2f2; }
.alert-icon { font-size: 20px; }
.alert-text { flex: 1; font-size: 14px; font-weight: 500; }

/* Stats */
.stats-row { margin-bottom: 16px; }
.stat-card { border-radius: 10px; margin-bottom: 16px; }
.stat-body { display: flex; align-items: center; gap: 12px; }
.stat-icon { font-size: 28px; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 2px; }

.action-card { margin-bottom: 16px; border-radius: 10px; }
.quick-actions { display: flex; gap: 12px; flex-wrap: wrap; }

.list-card { margin-bottom: 16px; border-radius: 10px; }
.list-card :deep(.el-table__row) { cursor: pointer; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
