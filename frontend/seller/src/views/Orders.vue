<template>
  <div class="orders-page" v-loading="loading">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>Orders</el-breadcrumb-item>
    </el-breadcrumb>
    <div class="page-header">
      <h3>订单管理</h3>
    </div>

    <el-table v-if="orders.length" :data="orders" border stripe class="orders-table">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="orderNo" label="订单号" width="200">
        <template #default="{ row }">
          <code class="order-no">{{ row.orderNo }}</code>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">
          <span class="amount">{{ row.currency || 'USD' }} {{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small" effect="plain">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.paidAt" type="success" size="small">已支付</el-tag>
          <el-tag v-else type="warning" size="small">未支付</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="buyerName" label="买家" width="120" />
      <el-table-column prop="createdAt" label="下单时间" width="180" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="viewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else-if="!loading" description="暂无订单" />

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetail" title="订单详情" width="700px" top="5vh">
      <template v-if="detail">
        <div class="detail-header">
          <div>
            <strong>订单号：</strong><code>{{ detail.orderNo }}</code>
          </div>
          <el-tag :type="statusType(detail.status)" size="small">{{ detail.status }}</el-tag>
        </div>

        <!-- Order Items -->
        <h4 class="section-label">商品信息</h4>
        <el-table :data="detail.items || []" size="small" border>
          <el-table-column prop="productTitle" label="商品名称" min-width="180" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="unitPrice" label="单价" width="100" align="right" />
          <el-table-column prop="subtotal" label="小计" width="100" align="right" />
        </el-table>

        <!-- Order Info -->
        <h4 class="section-label">订单信息</h4>
        <div class="info-grid">
          <div><span class="info-label">买家</span><span>{{ detail.buyerName }}</span></div>
          <div><span class="info-label">支付方式</span><span>{{ detail.paymentMethod }}</span></div>
          <div><span class="info-label">配送方式</span><span>{{ detail.shippingMethod }}</span></div>
          <div><span class="info-label">收货地址</span><span>{{ detail.shippingAddress }}</span></div>
          <div><span class="info-label">总金额</span><span class="total-amount">{{ detail.currency || 'USD' }} {{ detail.totalAmount }}</span></div>
          <div v-if="detail.paidAt"><span class="info-label">支付时间</span><span>{{ detail.paidAt }}</span></div>
        </div>

        <!-- Timeline -->
        <h4 class="section-label">操作记录</h4>
        <el-steps v-if="detail.logs && detail.logs.length" :active="detail.logs.length" direction="vertical" space="60">
          <el-step
            v-for="(log, i) in detail.logs"
            :key="i"
            :title="log.toStatus"
            :description="`${log.note || ''} — ${log.operator || '系统'} · ${log.createdAt}`"
          />
        </el-steps>
        <el-empty v-else description="暂无操作记录" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const orders = ref([])
const loading = ref(true)
const showDetail = ref(false)
const detail = ref(null)

async function loadOrders() {
  try {
    const res = await api.get('/orders/seller')
    orders.value = res.data || []
  } catch (e) { orders.value = [] }
  loading.value = false
}

async function viewDetail(row) {
  try {
    const res = await api.get(`/orders/${row.id}`)
    detail.value = res.data
    showDetail.value = true
  } catch (e) {}
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

onMounted(loadOrders)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.orders-table { border-radius: 8px; overflow: hidden; }
.orders-table :deep(.el-table__header th) { background: #f8fafc; font-weight: 600; }
.order-no { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 12px; }
.amount { font-weight: 600; color: #059669; }

.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid #eee; }
.section-label { font-size: 14px; font-weight: 600; color: #374151; margin: 20px 0 12px; }
.section-label:first-of-type { margin-top: 0; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; }
.info-grid div { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: .03em; }
.total-amount { font-size: 16px; font-weight: 700; color: #dc2626; }
</style>
