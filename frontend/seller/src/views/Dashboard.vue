<template>
  <div class="dashboard">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>{{ $t('dashboard.title', '控制台') }}</el-breadcrumb-item>
    </el-breadcrumb>

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

    <el-row :gutter="16" class="charts-row">
      <!-- Weekly Sales Chart -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:600">{{ $t('dashboard.weeklySales', '近7日销售额') }}</span>
          </template>
          <div class="chart-container" v-if="weeklySales.length">
            <div class="bar-chart">
              <div v-for="(sale, i) in weeklySales" :key="i" class="bar-item">
                <div class="bar-value">${{ formatPrice(sale) }}</div>
                <div class="bar" :style="{ height: barHeight(sale) + 'px' }"></div>
                <div class="bar-label">{{ dayLabels[i] }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无销售数据" :image-size="60" />
        </el-card>
      </el-col>

      <!-- Recent Orders -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:600">{{ $t('dashboard.recentOrders', '近期订单') }}</span>
          </template>
          <div v-if="recentOrders.length" class="recent-orders">
            <div v-for="o in recentOrders" :key="o.id" class="order-item" @click="$router.push('/orders')">
              <div class="order-top">
                <code class="order-no">{{ o.orderNo }}</code>
                <el-tag :type="statusType(o.status)" size="small" effect="plain">{{ o.status }}</el-tag>
              </div>
              <div class="order-meta">
                <span class="order-amount">{{ o.currency || 'USD' }} {{ formatPrice(o.totalAmount) }}</span>
                <span class="order-date">{{ formatDate(o.createdAt) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无订单" :image-size="50" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'

const { t } = useI18n()
const loading = ref(true)
const stats = ref({ productCount: 0, pendingOrders: 0, monthlySales: 0, lowStockCount: 0, toShipCount: 0 })
const weeklySales = ref([])
const recentOrders = ref([])

const DAY_NAMES = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat']

function dayLabel(i) {
  const d = new Date()
  d.setDate(d.getDate() - (6 - i))
  return DAY_NAMES[d.getDay()]
}

const dayLabels = computed(() => weeklySales.value.map((_, i) => dayLabel(i)))

const statCards = computed(() => [
  { key: 'products', icon: '📦', label: t('dashboard.totalProducts', '商品总数'), value: stats.value.productCount, color: '#409eff' },
  { key: 'pending', icon: '⏳', label: t('dashboard.pendingOrders', '待处理'), value: stats.value.pendingOrders, color: '#e6a23c' },
  { key: 'sales', icon: '💰', label: t('dashboard.monthlySales', '月销售额'), value: '$' + formatPrice(stats.value.monthlySales), color: '#67c23a' },
  { key: 'lowstock', icon: '⚠️', label: t('dashboard.lowStock', '低库存'), value: stats.value.lowStockCount + ' / 待发' + stats.value.toShipCount, color: '#f56c6c' },
])

const maxSale = computed(() => Math.max(...weeklySales.value, 1))

function barHeight(sale) {
  return Math.max(4, (sale / maxSale.value) * 120)
}

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

function formatPrice(p) { return Number(p || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

function formatDate(d) {
  if (!d) return ''
  return d.slice(0, 10)
}

onMounted(async () => {
  try {
    const res = await api.get('/seller/dashboard')
    const data = res.data || {}
    stats.value = data
    weeklySales.value = data.weeklySales || []
    recentOrders.value = (data.recentOrders || []).slice(0, 5)
  } catch (e) { /* ignore */ }
  loading.value = false
})
</script>

<style scoped>
.dashboard { max-width: 1200px; }
.stats-row { margin-bottom: 16px; }
.charts-row { margin-bottom: 16px; }

.stat-card { border-radius: 10px; cursor: default; margin-bottom: 16px; }
.stat-body { display: flex; align-items: center; gap: 12px; }
.stat-icon { font-size: 28px; line-height: 1; }
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 2px; }

.chart-container { padding: 8px 0; }
.bar-chart { display: flex; align-items: flex-end; justify-content: space-around; height: 180px; padding-top: 20px; }
.bar-item { display: flex; flex-direction: column; align-items: center; gap: 4px; flex: 1; }
.bar-value { font-size: 11px; color: #606266; font-weight: 600; }
.bar { width: 36px; background: linear-gradient(180deg, #409eff, #79bbff); border-radius: 4px 4px 0 0; transition: height 0.3s; min-height: 4px; }
.bar-label { font-size: 11px; color: #909399; }

.recent-orders { display: flex; flex-direction: column; gap: 8px; }
.order-item { padding: 10px; border-radius: 8px; border: 1px solid #f0f0f0; cursor: pointer; transition: all 0.15s; }
.order-item:hover { border-color: #409eff; background: #f0f7ff; }
.order-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.order-no { font-size: 11px; font-family: 'SF Mono', monospace; color: #606266; }
.order-meta { display: flex; justify-content: space-between; font-size: 12px; }
.order-amount { font-weight: 600; color: #059669; }
.order-date { color: #909399; }
</style>
