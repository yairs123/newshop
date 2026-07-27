<template>
  <div class="orders-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>{{ $t('orders.title', '订单管理') }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- Status filter tabs -->
    <div class="filter-bar">
      <el-tag
        v-for="s in statusFilters" :key="s.value"
        :type="statusFilter === s.value ? 'primary' : 'info'"
        effect="plain" style="cursor:pointer"
        @click="statusFilter = s.value"
      >{{ s.label }}</el-tag>
    </div>

    <el-table v-loading="loading" :data="filteredOrders" border stripe class="orders-table">
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
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small" effect="plain">{{ STATUS_LABELS[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.paidAt" type="success" size="small">✓</el-tag>
          <el-tag v-else type="warning" size="small">—</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="buyerName" label="买家" width="100" />
      <el-table-column label="快递" width="140">
        <template #default="{ row }">
          <span v-if="row.trackingCompany" class="tracking-info">{{ row.trackingCompany }}<br><small>{{ row.trackingNumber }}</small></span>
          <span v-else style="color:#999">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="160" />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-space size="small">
            <el-button size="small" plain @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PAID'" type="primary" size="small" @click="openShip(row)">发货</el-button>
          </el-space>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单" />

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetail" title="订单详情" width="700px" top="5vh">
      <template v-if="detail">
        <div class="detail-header">
          <div><strong>订单号：</strong><code>{{ detail.orderNo }}</code></div>
          <el-tag :type="statusType(detail.status)" size="small">{{ detail.status }}</el-tag>
        </div>

        <h4 class="section-label">商品</h4>
        <el-table :data="detail.items || []" size="small" border>
          <el-table-column prop="productTitle" label="商品名称" min-width="180" />
          <el-table-column prop="quantity" label="数量" width="60" align="center" />
          <el-table-column prop="unitPrice" label="单价" width="100" align="right" />
          <el-table-column prop="subtotal" label="小计" width="100" align="right" />
        </el-table>

        <h4 class="section-label">信息</h4>
        <div class="info-grid">
          <div><span class="info-label">买家</span><span>{{ detail.buyerName }}</span></div>
          <div><span class="info-label">支付方式</span><span>{{ detail.paymentMethod }}</span></div>
          <div><span class="info-label">配送方式</span><span>{{ detail.shippingMethod }}</span></div>
          <div><span class="info-label">地址</span><span>{{ detail.shippingAddress }}</span></div>
          <div><span class="info-label">金额</span><span class="total-amount">{{ detail.currency || 'USD' }} {{ formatPrice(detail.totalAmount) }}</span></div>
          <div v-if="detail.paidAt"><span class="info-label">支付时间</span><span>{{ detail.paidAt }}</span></div>
          <div v-if="detail.trackingCompany"><span class="info-label">快递</span><span>{{ detail.trackingCompany }} · {{ detail.trackingNumber }}</span></div>
        </div>

        <h4 class="section-label">操作记录</h4>
        <el-steps v-if="detail.logs?.length" :active="detail.logs.length" direction="vertical" :space="50">
          <el-step v-for="(log, i) in detail.logs" :key="i"
            :title="log.toStatus"
            :description="`${log.note || ''} — ${log.operator || '系统'} ${log.createdAt ? '@ ' + log.createdAt.slice(0,16) : ''}`"
          />
        </el-steps>
        <el-empty v-else description="暂无记录" :image-size="50" />
      </template>
    </el-dialog>

    <!-- Ship Dialog -->
    <el-dialog v-model="showShip" title="发货" width="420px" destroy-on-close>
      <template v-if="shipOrder">
        <div class="ship-info">
          <p><strong>订单：</strong><code>{{ shipOrder.orderNo }}</code></p>
          <p><strong>金额：</strong>{{ shipOrder.currency || 'USD' }} {{ formatPrice(shipOrder.totalAmount) }}</p>
        </div>
        <el-form label-position="top">
          <el-form-item label="快递公司">
            <el-select v-model="shipForm.company" placeholder="选择快递公司" style="width:100%">
              <el-option label="USPS" value="USPS" />
              <el-option label="UPS" value="UPS" />
              <el-option label="FedEx" value="FedEx" />
              <el-option label="DHL" value="DHL" />
              <el-option label="EMS" value="EMS" />
              <el-option label="顺丰速运" value="顺丰速运" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="shipForm.number" placeholder="输入快递单号" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="showShip = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const STATUS_LABELS = {
  PENDING_PAYMENT: '待付款', PAID: '已支付', SHIPPED: '已发货',
  COMPLETED: '已完成', CANCELLED: '已取消'
}
const statusFilters = [
  { value: '', label: '全部' },
  { value: 'PAID', label: '待发货' },
  { value: 'SHIPPED', label: '已发货' },
  { value: 'COMPLETED', label: '已完成' },
]

const orders = ref([])
const loading = ref(true)
const statusFilter = ref('')
const showDetail = ref(false)
const detail = ref(null)

// Ship
const showShip = ref(false)
const shipOrder = ref(null)
const shipping = ref(false)
const shipForm = ref({ company: '', number: '' })

const filteredOrders = computed(() => {
  if (!statusFilter.value) return orders.value
  return orders.value.filter(o => o.status === statusFilter.value)
})

async function loadOrders() {
  loading.value = true
  try {
    const res = await api.get('/orders/seller')
    orders.value = res.data || []
  } catch (e) { orders.value = [] }
  loading.value = false
}

async function viewDetail(row) {
  try {
    const res = await api.get('/orders/' + row.id)
    detail.value = res.data
    showDetail.value = true
  } catch (e) { /* ignore */ }
}

function openShip(row) {
  shipOrder.value = row
  shipForm.value = { company: '', number: '' }
  showShip.value = true
}

async function confirmShip() {
  if (!shipForm.value.company || !shipForm.value.number) {
    ElMessage.warning('请填写快递公司和单号')
    return
  }
  shipping.value = true
  try {
    await api.post('/orders/' + shipOrder.value.id + '/ship', {
      trackingCompany: shipForm.value.company,
      trackingNumber: shipForm.value.number,
    })
    ElMessage.success('发货成功！')
    showShip.value = false
    loadOrders()
  } catch (e) { /* handled */ }
  finally { shipping.value = false }
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

onMounted(loadOrders)
</script>

<style scoped>
.filter-bar { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.orders-table { border-radius: 8px; overflow: hidden; }
.orders-table :deep(.el-table__header th) { background: #f8fafc; font-weight: 600; }
.order-no { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 12px; }
.amount { font-weight: 600; color: #059669; }
.tracking-info { font-size: 12px; line-height: 1.4; }
.tracking-info small { color: #909399; }

.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #eee; }
.section-label { font-size: 14px; font-weight: 600; color: #374151; margin: 20px 0 12px; }
.section-label:first-of-type { margin-top: 0; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 24px; }
.info-grid div { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: .03em; }
.total-amount { font-size: 16px; font-weight: 700; color: #dc2626; }
.ship-info { margin-bottom: 16px; padding: 12px; background: #f8fafc; border-radius: 8px; }
.ship-info p { margin: 4px 0; font-size: 14px; }

/* ====== Mobile Responsive ====== */
@media (max-width: 768px) {
  .filter-bar { overflow-x: auto; flex-wrap: nowrap; -webkit-overflow-scrolling: touch; padding-bottom: 4px; }
  .el-table { overflow-x: auto; }
  .el-dialog { width: 95% !important; max-width: 95vw !important; }
  .info-grid { grid-template-columns: 1fr; }
  .detail-header { flex-direction: column; gap: 8px; align-items: flex-start; }
}
@media (max-width: 480px) {
  .orders-page { padding: 0; }
  .el-table :deep(.el-table__body-wrapper) { overflow-x: auto; }
}
</style>
