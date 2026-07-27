<template>
  <div class="sales-report">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>{{ $t('salesReport.title', '销售报表') }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- Summary Cards -->
    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card" style="borderTop: 3px solid #409eff">
          <div class="summary-body">
            <span class="summary-icon">💰</span>
            <div class="summary-info">
              <span class="summary-value">${{ formatPrice(totalRevenue) }}</span>
              <span class="summary-label">{{ $t('salesReport.totalRevenue', '本月总收入') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card" style="borderTop: 3px solid #67c23a">
          <div class="summary-body">
            <span class="summary-icon">📋</span>
            <div class="summary-info">
              <span class="summary-value">{{ completedOrders.length }}</span>
              <span class="summary-label">{{ $t('salesReport.completedOrders', '已完成订单') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card" style="borderTop: 3px solid #e6a23c">
          <div class="summary-body">
            <span class="summary-icon">🕐</span>
            <div class="summary-info">
              <span class="summary-value">{{ pendingOrders.length }}</span>
              <span class="summary-label">{{ $t('salesReport.pendingOrders', '待处理订单') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="summary-card" style="borderTop: 3px solid #f56c6c">
          <div class="summary-body">
            <span class="summary-icon">📊</span>
            <div class="summary-info">
              <span class="summary-value">{{ averageOrderValue }}</span>
              <span class="summary-label">{{ $t('salesReport.avgOrderValue', '平均订单金额') }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Status Breakdown -->
    <el-row :gutter="16" class="breakdown-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:600">{{ $t('salesReport.statusBreakdown', '订单状态分布') }}</span>
          </template>
          <div class="breakdown-chart" v-if="statusBreakdown.length">
            <div v-for="item in statusBreakdown" :key="item.status" class="breakdown-item">
              <div class="breakdown-header">
                <el-tag :type="statusType(item.status)" size="small" effect="plain">
                  {{ STATUS_LABELS[item.status] || item.status }}
                </el-tag>
                <span class="breakdown-count">{{ item.count }} 单</span>
                <span class="breakdown-amount">${{ formatPrice(item.amount) }}</span>
              </div>
              <el-progress
                :percentage="item.percentage"
                :color="statusColor(item.status)"
                :stroke-width="16"
                :format="() => item.percentage + '%'"
              />
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Monthly Completed Orders Table -->
    <el-card shadow="hover" class="orders-card">
      <template #header>
        <div class="card-header">
          <span style="font-weight:600">{{ $t('salesReport.monthlyOrders', '本月已完成订单') }}</span>
          <el-tag type="success" effect="plain">
            {{ $t('salesReport.total', '合计') }}: ${{ formatPrice(totalRevenue) }}
          </el-tag>
        </div>
      </template>
      <el-table v-loading="loading" :data="completedOrders" border stripe class="orders-table">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="orderNo" label="订单号" width="190">
          <template #default="{ row }">
            <code class="order-no">{{ row.orderNo }}</code>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">{{ row.currency || 'USD' }} {{ formatPrice(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="paidAt" label="支付时间" width="160" />
        <el-table-column prop="createdAt" label="下单时间" width="160" />
      </el-table>
      <el-empty v-if="!loading && completedOrders.length === 0" description="本月暂无已完成订单" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'

const { t } = useI18n()
const loading = ref(true)
const orders = ref([])

const STATUS_LABELS = {
  PENDING_PAYMENT: '待付款', PAID: '已支付', SHIPPED: '已发货',
  COMPLETED: '已完成', CANCELLED: '已取消'
}

const completedOrders = computed(() =>
  orders.value.filter(o => o.status === 'COMPLETED')
)

const pendingOrders = computed(() =>
  orders.value.filter(o => o.status === 'PAID' || o.status === 'SHIPPED')
)

const totalRevenue = computed(() =>
  completedOrders.value.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0)
)

const averageOrderValue = computed(() => {
  if (completedOrders.value.length === 0) return '$0.00'
  const avg = totalRevenue.value / completedOrders.value.length
  return '$' + formatPrice(avg)
})

const statusBreakdown = computed(() => {
  const groups = {}
  orders.value.forEach(o => {
    if (!groups[o.status]) groups[o.status] = { status: o.status, count: 0, amount: 0 }
    groups[o.status].count++
    groups[o.status].amount += Number(o.totalAmount || 0)
  })
  const total = orders.value.length || 1
  return Object.values(groups).map(g => ({
    ...g,
    percentage: Math.round((g.count / total) * 100)
  }))
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

function statusColor(status) {
  switch (status) {
    case 'PENDING_PAYMENT': return '#e6a23c'
    case 'PAID': return '#67c23a'
    case 'SHIPPED': return '#409eff'
    case 'COMPLETED': return '#67c23a'
    case 'CANCELLED': return '#909399'
    default: return '#909399'
  }
}

function formatPrice(p) {
  return Number(p || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

onMounted(async () => {
  try {
    const res = await api.get('/orders/seller')
    orders.value = res.data || []
  } catch (e) {
    orders.value = []
  }
  loading.value = false
})
</script>

<style scoped>
.sales-report { max-width: 1200px; }
.summary-row { margin-bottom: 16px; }
.breakdown-row { margin-bottom: 16px; }

.summary-card { border-radius: 10px; cursor: default; margin-bottom: 16px; }
.summary-body { display: flex; align-items: center; gap: 12px; }
.summary-icon { font-size: 28px; line-height: 1; }
.summary-info { display: flex; flex-direction: column; }
.summary-value { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.2; }
.summary-label { font-size: 12px; color: #909399; margin-top: 2px; }

.breakdown-chart { display: flex; flex-direction: column; gap: 16px; padding: 8px 0; }
.breakdown-item { display: flex; flex-direction: column; gap: 6px; }
.breakdown-header { display: flex; align-items: center; gap: 12px; }
.breakdown-count { font-size: 13px; color: #606266; font-weight: 600; margin-left: auto; }
.breakdown-amount { font-size: 13px; color: #059669; font-weight: 600; min-width: 100px; text-align: right; }

.orders-card { border-radius: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.orders-table { border-radius: 8px; overflow: hidden; }
.orders-table :deep(.el-table__header th) { background: #f8fafc; font-weight: 600; }
.order-no { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 12px; }
.amount { font-weight: 600; color: #059669; }
</style>
