<template>
  <div class="order-detail" v-loading="loading">
    <div class="detail-container">
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">{{ $t('nav.myOrders') }}</el-breadcrumb-item>
        <el-breadcrumb-item v-if="order">#{{ order.orderNo }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="!loading && !order" style="text-align: center; padding: 60px 0;">
        <el-empty :description="$t('order.notFound')" />
        <el-button type="primary" style="margin-top: 16px;" @click="$router.push('/orders')">
          {{ $t('order.backToOrders') }}
        </el-button>
      </div>

      <template v-if="order">
        <!-- Header Card -->
        <el-card shadow="never" class="detail-card header-card">
          <div class="header-row">
            <div class="header-left">
              <h2 class="order-title">{{ $t('order.orderTitle') }} #{{ order.orderNo }}</h2>
              <p class="order-date">{{ $t('order.placedOn') }} {{ formatDate(order.createdAt) }}</p>
            </div>
            <div class="header-right">
              <el-tag :type="statusType(order.status)" size="large" effect="dark" class="status-tag">
                {{ statusLabel(order.status) }}
              </el-tag>
              <div class="action-buttons" v-if="order.status === 'PENDING_PAYMENT'">
                <el-button type="warning" :loading="paying" @click="handlePay" size="large" class="btn-pay">
                  {{ $t('checkout.payNow') }}
                </el-button>
                <el-button :loading="cancelling" @click="handleCancel" size="small" text class="btn-cancel">
                  {{ $t('common.cancel') }}
                </el-button>
              </div>
              <el-button size="small" text class="btn-invoice" @click="$router.push('/orders/' + order.id + '/invoice')">
                {{ $t('account.invoice') }}
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- Order Items Card -->
        <el-card shadow="never" class="detail-card">
          <h3 class="section-title">{{ $t('order.items') }}</h3>
          <el-table :data="order.items || []" style="width: 100%;" stripe>
            <el-table-column :label="$t('cart.colProduct')" min-width="200">
              <template #default="{ row }">
                <span class="product-title">{{ row.productTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('cart.colQuantity')" width="100" align="center">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column :label="$t('cart.colPrice')" width="130" align="right">
              <template #default="{ row }">{{ formatPrice(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column :label="$t('order.subtotal')" width="130" align="right">
              <template #default="{ row }">
                <span class="subtotal-price">{{ formatPrice(row.subtotal) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="order-total-line">
            <span class="total-label">{{ $t('checkout.orderTotal') }}</span>
            <span class="total-amount">{{ formatPrice(order.totalAmount) }}</span>
          </div>
        </el-card>

        <!-- Payment Info Card -->
        <el-card shadow="never" class="detail-card" v-if="order.paymentMethod">
          <h3 class="section-title">{{ $t('order.payment') }}</h3>
          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">{{ $t('checkout.paymentMethod') }}</span>
              <span class="info-value">{{ paymentMethodLabel(order.paymentMethod) }}</span>
            </div>
            <div class="info-row" v-if="order.paidAt">
              <span class="info-label">{{ $t('order.paidAt') }}</span>
              <span class="info-value">{{ formatDate(order.paidAt) }}</span>
            </div>
          </div>
        </el-card>

        <!-- Shipping Card -->
        <el-card shadow="never" class="detail-card" v-if="order.shippingAddress">
          <h3 class="section-title">{{ $t('order.shipping') }}</h3>
          <div class="info-grid">
            <div class="info-row">
              <span class="info-label">{{ $t('checkout.shippingAddress') }}</span>
              <span class="info-value">{{ order.shippingAddress }}</span>
            </div>
            <div class="info-row" v-if="order.shippingMethod">
              <span class="info-label">{{ $t('checkout.shippingMethod') }}</span>
              <span class="info-value">{{ order.shippingMethod }}</span>
            </div>
            <div class="info-row" v-if="order.trackingNumber">
              <span class="info-label">{{ $t('account.trackingNumber') }}</span>
              <span class="info-value">
                {{ order.trackingNumber }}
                <el-tag size="small" type="warning" style="margin-left: 8px;" v-if="order.trackingCompany">
                  {{ order.trackingCompany }}
                </el-tag>
              </span>
            </div>
          </div>
        </el-card>

        <!-- Buyer Note Card -->
        <el-card shadow="never" class="detail-card" v-if="order.buyerNote">
          <h3 class="section-title">{{ $t('checkout.buyerNote') }}</h3>
          <p class="buyer-note">{{ order.buyerNote }}</p>
        </el-card>

        <!-- Order Timeline Card -->
        <el-card shadow="never" class="detail-card" v-if="order.logs && order.logs.length">
          <h3 class="section-title">{{ $t('order.history') }}</h3>
          <div class="timeline">
            <div
              v-for="(log, index) in sortedLogs"
              :key="index"
              class="timeline-item"
              :class="{ 'timeline-item-last': index === sortedLogs.length - 1 }"
            >
              <div class="timeline-dot-wrapper">
                <div class="timeline-dot" :class="'dot-' + statusType(log.toStatus)"></div>
                <div class="timeline-line" v-if="index < sortedLogs.length - 1"></div>
              </div>
              <div class="timeline-content">
                <div class="timeline-header">
                  <span class="timeline-status">{{ statusLabel(log.toStatus) }}</span>
                  <span class="timeline-time">{{ formatDate(log.createdAt) }}</span>
                </div>
                <div class="timeline-meta" v-if="log.operator || log.note">
                  <span v-if="log.operator" class="timeline-operator">{{ $t('order.by') }} {{ log.operator }}</span>
                  <span v-if="log.note" class="timeline-note">{{ log.note }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const order = ref(null)
const loading = ref(true)
const paying = ref(false)
const cancelling = ref(false)

const statusTypeMap = {
  PENDING_PAYMENT: 'danger',
  PAID: 'success',
  SHIPPED: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'info',
}

const sortedLogs = computed(() => {
  if (!order.value || !order.value.logs) return []
  const logs = [...order.value.logs]
  logs.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
  return logs
})

function statusType(status) {
  return statusTypeMap[status] || 'info'
}

function statusLabel(status) {
  return t('order.' + status)
}

// 支付方式代码 → 本地化文案
function paymentMethodLabel(method) {
  if (!method) return ''
  const keyMap = {
    CREDIT_CARD: 'checkout.payCreditCard',
    PAYPAL: 'checkout.payPaypal',
    ALIPAY: 'checkout.payAlipay',
    WECHAT_PAY: 'checkout.payWechat',
    GRABPAY: 'checkout.payGrabPay',
    PAYNOW: 'checkout.payPayNow',
    BANK_TRANSFER: 'checkout.payBankTransfer'
  }
  const key = keyMap[method]
  return key ? t(key) : method
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return d.toLocaleDateString(locale.value, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatPrice(price) {
  if (price == null) return '$0.00'
  return '$' + Number(price).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

async function fetchOrder() {
  loading.value = true
  try {
    const res = await api.get('/orders/' + route.params.id)
    order.value = res.data
  } catch (e) {
    order.value = null
  }
  loading.value = false
}

async function handlePay() {
  paying.value = true
  try {
    const id = route.params.id
    const returnUrl = window.location.origin + '/payment/return?orderId=' + id
    const cancelUrl = window.location.origin + '/orders/' + id
    const res = await api.post('/orders/' + id + '/pay', {
      returnUrl,
      cancelUrl,
    })
    const pay = res.data || {}
    // 支付失败：显示错误，不误报成功
    if (pay.status === 'FAILED') {
      ElMessage.error(pay.errorMessage || t('account.paymentFailed', '支付失败，请重试'))
      await fetchOrder()
      return
    }
    // 有支付链接：跳转支付
    if (pay.paymentUrl) {
      window.location.href = pay.paymentUrl
    } else {
      ElMessage.success(t('account.paymentInitiated'))
      await fetchOrder()
    }
  } catch (e) {
    // Error message handled by API interceptor
  }
  paying.value = false
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm(
      t('account.confirmCancel'),
      t('account.confirmCancelTitle'),
      {
        confirmButtonText: t('account.confirmYesCancel'),
        cancelButtonText: t('account.confirmKeepOrder'),
        type: 'warning',
      }
    )
  } catch (e) {
    return
  }
  cancelling.value = true
  try {
    await api.post('/orders/' + route.params.id + '/cancel')
    ElMessage.success(t('account.orderCancelled'))
    await fetchOrder()
  } catch (e) {
    // Error message handled by API interceptor
  }
  cancelling.value = false
}

onMounted(fetchOrder)
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@500;600;700&family=DM+Sans:wght@400;500;600&display=swap');

.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'DM Sans', -apple-system, sans-serif;
}

.detail-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid #ece7e0;
  box-shadow: 0 1px 3px rgba(26, 26, 46, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* Header Card */
.header-card {
  background: linear-gradient(135deg, #faf8f5 0%, #ffffff 100%);
  border-top: 3px solid #b8860b;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.order-title {
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.order-date {
  font-size: 13px;
  color: #8a8a93;
  margin: 0;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.status-tag {
  font-size: 14px;
  padding: 6px 14px;
  border-radius: 20px;
  border: none;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
}
.action-buttons .btn-pay {
  margin-right: 8px;
}
.action-buttons .btn-cancel,
.btn-invoice {
  color: #6b7280;
}
.action-buttons .btn-cancel:hover,
.btn-invoice:hover {
  color: #f59e0b;
}

/* Items Table */
.product-title {
  font-weight: 500;
  color: #1d1d1f;
}

.subtotal-price {
  font-weight: 600;
  color: #1d1d1f;
}

.order-total-line {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 24px;
  padding: 16px 20px 0 0;
  margin-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.total-label {
  font-size: 15px;
  font-weight: 500;
  color: #6b7280;
}

.total-amount {
  font-size: 22px;
  font-weight: 700;
  color: #dc2626;
}

/* Info Grid */
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.info-label {
  font-size: 13px;
  font-weight: 500;
  color: #86868b;
  min-width: 140px;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #1d1d1f;
  word-break: break-word;
}

/* Buyer Note */
.buyer-note {
  font-size: 14px;
  color: #6b7280;
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 0;
  line-height: 1.6;
}

/* Timeline */
.timeline {
  padding: 4px 0;
}

.timeline-item {
  display: flex;
  gap: 16px;
  position: relative;
}

.timeline-item + .timeline-item {
  margin-top: 0;
}

.timeline-dot-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 16px;
  flex-shrink: 0;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d9d9d9;
  border: 2px solid #bfbfbf;
  flex-shrink: 0;
  margin-top: 4px;
}

.timeline-dot.dot-danger {
  background: #fef0f0;
  border-color: #f56c6c;
}

.timeline-dot.dot-success {
  background: #f0f9eb;
  border-color: #67c23a;
}

.timeline-dot.dot-warning {
  background: #fdf6ec;
  border-color: #e6a23c;
}

.timeline-dot.dot-info {
  background: #f4f4f5;
  border-color: #909399;
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #e8e8e8;
  min-height: 24px;
}

.timeline-item-last .timeline-line {
  display: none;
}

.timeline-content {
  flex: 1;
  padding-bottom: 20px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.timeline-status {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.timeline-time {
  font-size: 12px;
  color: #86868b;
  white-space: nowrap;
}

.timeline-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.timeline-operator {
  font-size: 12px;
  color: #86868b;
}

.timeline-note {
  font-size: 12px;
  color: #909399;
  font-style: italic;
}

/* Responsive */
@media (max-width: 640px) {
  .detail-container {
    padding: 12px;
  }

  .header-row {
    flex-direction: column;
  }

  .header-right {
    align-items: flex-start;
    width: 100%;
  }

  .action-buttons {
    width: 100%;
  }

  .action-buttons .el-button {
    flex: 1;
  }

  .info-row {
    flex-direction: column;
    gap: 2px;
  }

  .info-label {
    min-width: auto;
  }

  .order-total-line {
    padding-right: 0;
  }
}
</style>
