<template>
  <div class="order-list">
    <el-breadcrumb separator="/" style="margin-bottom: 12px;">
      <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ $t('account.orders') }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="page-title">
      <h1>{{ $t('account.myOrders') }}</h1>
      <p class="page-subtitle">{{ $t('account.ordersSubtitle', '查看和管理您的订单') }}</p>
    </div>

    <el-tabs v-model="activeTab" class="order-tabs">
      <!-- My Orders Tab -->
      <el-tab-pane :label="$t('account.myOrders')" name="orders">
        <div v-loading="ordersLoading">
          <!-- 状态筛选提示 -->
          <div v-if="statusFilter" class="filter-indicator">
            <span>{{ statusLabel(statusFilter) }}</span>
            <button @click="clearStatusFilter">✕ {{ $t('common.clear', '清除筛选') }}</button>
          </div>
          <el-empty v-if="filteredOrders.length === 0" :description="$t('account.noOrders')" />
          <div v-for="order in filteredOrders" :key="order.id" class="order-card-wrapper">
            <el-card shadow="hover" class="order-card">
              <!-- Order Header -->
              <div class="order-header">
                <div class="order-header-left">
                  <span class="order-no">#{{ order.orderNo }}</span>
                  <el-tag :type="statusType(order.status)" size="small" effect="dark" class="status-tag">
                    {{ statusLabel(order.status) }}
                  </el-tag>
                </div>
                <div class="order-header-right">
                  <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                </div>
              </div>

              <!-- Order Items -->
              <div class="order-body">
                <div v-for="item in order.items" :key="item.id" class="order-item">
                  <div class="item-main">
                    <div class="item-avatar">
                      <span class="item-avatar-letter">{{ getFirstLetter(item.productTitle) }}</span>
                    </div>
                    <div class="item-info">
                      <span class="item-title">{{ item.productTitle }}</span>
                      <span class="item-meta">
                        <span class="item-qty">x{{ item.quantity }}</span>
                        <span class="item-sep">|</span>
                        <span class="item-price">${{ item.subtotal }}</span>
                      </span>
                    </div>
                  </div>
                  <div class="item-actions">
                    <el-button
                      v-if="order.status === 'COMPLETED' && !isReviewed(order.id, item.productId)"
                      size="small"
                      text
                      type="primary"
                      @click="openReviewDialog(order, item)"
                    >
                      {{ $t('account.writeReview') }}
                    </el-button>
                    <el-button
                      v-if="isReviewed(order.id, item.productId)"
                      size="small"
                      text
                      disabled
                    >
                      {{ $t('account.reviewed') }}
                    </el-button>
                    <el-button
                      v-if="order.trackingNumber"
                      size="small"
                      text
                      type="primary"
                      @click="openTrackDialog(order)"
                    >
                      {{ $t('account.trackPackage') }}
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- Order Footer -->
              <div class="order-footer">
                <div class="footer-summary">
                  <span class="footer-total">{{ $t('account.total') }}: <strong>${{ order.totalAmount }}</strong></span>
                  <span v-if="order.paymentMethod" class="footer-meta">
                    <el-icon style="margin-right: 2px; vertical-align: middle;"><CreditCard /></el-icon>
                    {{ paymentMethodLabel(order.paymentMethod) }}
                  </span>
                  <span v-if="order.shippingAddress" class="footer-meta">
                    <el-icon style="margin-right: 2px; vertical-align: middle;"><Location /></el-icon>
                    {{ order.shippingAddress }}
                  </span>
                </div>
                <div class="footer-actions">
                  <!-- 主操作：立即支付（金色） -->
                  <el-button
                    v-if="order.status === 'PENDING_PAYMENT'"
                    size="small"
                    class="btn-pay"
                    @click="payOrder(order.id)"
                  >
                    💳 {{ $t('checkout.payNow') }}
                  </el-button>
                  <!-- 次要操作：文字链接风格 -->
                  <el-button
                    v-if="order.status === 'PENDING_PAYMENT'"
                    size="small"
                    text
                    class="btn-secondary"
                    @click="cancelOrder(order.id)"
                  >
                    {{ $t('common.cancel') }}
                  </el-button>
                  <el-button size="small" text class="btn-secondary" @click="$router.push(`/orders/${order.id}`)">
                    {{ $t('account.viewDetails') }}
                  </el-button>
                  <el-button size="small" text class="btn-secondary" @click="$router.push(`/orders/${order.id}/invoice`)">
                    {{ $t('account.invoice') }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <!-- Buy Again Tab -->
      <el-tab-pane :label="$t('account.buyAgain')" name="buyAgain">
        <div v-loading="buyAgainLoading">
          <el-empty v-if="buyAgainItems.length === 0" :description="$t('account.noBuyAgain')" />
          <div class="buy-again-grid">
            <div v-for="item in buyAgainItems" :key="item.productId" class="buy-again-card">
              <div class="buy-again-image">
                <img
                  v-if="item.productImage"
                  :src="item.productImage"
                  :alt="item.productTitle"
                  loading="lazy"
                />
                <div v-else class="buy-again-placeholder">
                  <span>{{ getFirstLetter(item.productTitle) }}</span>
                </div>
              </div>
              <div class="buy-again-title">{{ item.productTitle }}</div>
              <div class="buy-again-price">${{ item.unitPrice }}</div>
              <el-button type="primary" size="small" @click="addToCart(item)">
                {{ $t('account.addToCart') }}
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- Not Yet Shipped Tab -->
      <el-tab-pane :label="$t('account.notYetShipped')" name="unshipped">
        <div v-loading="unshippedLoading">
          <el-empty v-if="unshippedOrders.length === 0" :description="$t('account.noOrders')" />
          <div v-for="order in unshippedOrders" :key="order.id" class="order-card-wrapper">
            <el-card shadow="hover" class="order-card">
              <div class="order-header">
                <div class="order-header-left">
                  <span class="order-no">#{{ order.orderNo }}</span>
                  <el-tag :type="statusType(order.status)" size="small" effect="dark" class="status-tag">
                    {{ statusLabel(order.status) }}
                  </el-tag>
                </div>
                <div class="order-header-right">
                  <span class="order-date">{{ formatDate(order.createdAt) }}</span>
                </div>
              </div>
              <div class="order-body">
                <div v-for="item in order.items" :key="item.id" class="order-item">
                  <div class="item-main">
                    <div class="item-avatar">
                      <span class="item-avatar-letter">{{ getFirstLetter(item.productTitle) }}</span>
                    </div>
                    <div class="item-info">
                      <span class="item-title">{{ item.productTitle }}</span>
                      <span class="item-meta">
                        <span class="item-qty">x{{ item.quantity }}</span>
                        <span class="item-sep">|</span>
                        <span class="item-price">${{ item.subtotal }}</span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="order-footer">
                <div class="footer-summary">
                  <span class="footer-total">{{ $t('account.total') }}: <strong>${{ order.totalAmount }}</strong></span>
                </div>
                <div class="footer-actions">
                  <el-button size="small" @click="$router.push(`/orders/${order.id}`)">
                    {{ $t('account.viewDetails') }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Track Package Dialog -->
    <el-dialog
      v-model="trackDialogVisible"
      :title="$t('account.trackPackage')"
      width="420px"
      :close-on-click-modal="false"
    >
      <div v-if="trackOrder" class="track-info">
        <div class="track-row">
          <span class="track-label">{{ $t('account.carrier') }}</span>
          <span class="track-value">{{ trackOrder.trackingCompany || '-' }}</span>
        </div>
        <div class="track-row">
          <span class="track-label">{{ $t('account.trackingNumber') }}</span>
          <span class="track-value track-number">{{ trackOrder.trackingNumber || '-' }}</span>
        </div>
        <el-empty v-if="!trackOrder.trackingNumber" :description="$t('account.noTrackingInfo')" />
      </div>
      <template #footer>
        <el-button @click="trackDialogVisible = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Write Review Dialog -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="$t('account.writeReview')"
      width="480px"
      :close-on-click-modal="false"
    >
      <div class="review-form">
        <div class="review-product">
          {{ $t('account.reviewingProduct') }}: <strong>{{ reviewProductTitle }}</strong>
        </div>
        <div class="review-field">
          <label class="review-label">{{ $t('account.rating') }}</label>
          <el-rate
            v-model="reviewRating"
            :max="5"
            show-score
            :score-template="$t('account.starsTemplate')"
          />
        </div>
        <div class="review-field">
          <label class="review-label">{{ $t('common.review') }}</label>
          <el-input
            v-model="reviewContent"
            type="textarea"
            :rows="4"
            :placeholder="$t('account.reviewPlaceholder')"
            maxlength="500"
            show-word-limit
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button
          type="primary"
          :loading="reviewSubmitting"
          :disabled="!reviewContent.trim()"
          @click="submitReview"
        >
          {{ $t('account.submitReview') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { useCartStore } from '../store/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CreditCard, Location } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

const activeTab = ref('orders')

// 状态筛选（来自 URL ?status=xxx）
const statusFilter = ref(route.query.status || '')

// 筛选后的订单
const filteredOrders = computed(() => {
  if (!statusFilter.value) return orders.value
  return orders.value.filter(o => o.status === statusFilter.value)
})

// My Orders
const orders = ref([])
const ordersLoading = ref(true)

// Buy Again
const buyAgainItems = ref([])
const buyAgainLoading = ref(true)

// Not Yet Shipped
const unshippedOrders = ref([])
const unshippedLoading = ref(true)

// Track Dialog
const trackDialogVisible = ref(false)
const trackOrder = ref(null)

// Review Dialog
const reviewDialogVisible = ref(false)
const reviewOrderId = ref(null)
const reviewProductId = ref(null)
const reviewProductTitle = ref('')
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewSubmitting = ref(false)

// Reviewed items tracker: key = "orderId-productId"
const reviewedMap = reactive({})

function clearStatusFilter() {
  statusFilter.value = ''
  router.push({ path: '/orders', query: {} })
}

onMounted(() => {
  fetchOrders()
  fetchBuyAgain()
  fetchUnshipped()
})

// --- Data Fetching ---

async function fetchOrders() {
  ordersLoading.value = true
  try {
    const res = await api.get('/orders/buyer')
    orders.value = res.data || []
    checkReviewsForOrders(orders.value)
  } catch {
    orders.value = []
  } finally {
    ordersLoading.value = false
  }
}

async function fetchBuyAgain() {
  buyAgainLoading.value = true
  try {
    const res = await api.get('/orders/buyer/items')
    buyAgainItems.value = res.data || []
  } catch {
    buyAgainItems.value = []
  } finally {
    buyAgainLoading.value = false
  }
}

async function fetchUnshipped() {
  unshippedLoading.value = true
  try {
    const res = await api.get('/orders/buyer/unshipped')
    unshippedOrders.value = res.data || []
  } catch {
    unshippedOrders.value = []
  } finally {
    unshippedLoading.value = false
  }
}

async function checkReviewsForOrders(ordersList) {
  const checks = []
  for (const order of ordersList) {
    if (order.status === 'COMPLETED' && order.items) {
      for (const item of order.items) {
        const key = `${order.id}-${item.productId}`
        if (!(key in reviewedMap)) {
          checks.push(checkReviewed(order.id, item.productId).then(r => {
            reviewedMap[key] = r
          }))
        }
      }
    }
  }
  if (checks.length > 0) {
    await Promise.all(checks)
  }
}

async function checkReviewed(orderId, productId) {
  try {
    const res = await api.get('/reviews/check', {
      params: { orderId, productId }
    })
    if (typeof res.data === 'boolean') return res.data
    if (res.data && typeof res.data.reviewed === 'boolean') return res.data.reviewed
    return false
  } catch {
    return false
  }
}

function isReviewed(orderId, productId) {
  return reviewedMap[`${orderId}-${productId}`] === true
}

// --- Order Actions ---

async function payOrder(orderId) {
  try {
    const res = await api.post(`/orders/${orderId}/pay`, {
      returnUrl: window.location.href
    })
    if (res.data?.paymentUrl) {
      window.location.href = res.data.paymentUrl
    } else {
      ElMessage.success(t('account.paymentInitiated'))
      await fetchOrders()
    }
  } catch {
    // Error message already shown by API interceptor
  }
}

async function cancelOrder(orderId) {
  try {
    await ElMessageBox.confirm(
      t('account.confirmCancel'),
      t('account.confirmCancelTitle'),
      {
        confirmButtonText: t('account.confirmYesCancel'),
        cancelButtonText: t('account.confirmKeepOrder'),
        type: 'warning'
      }
    )
    await api.post(`/orders/${orderId}/cancel`)
    ElMessage.success(t('account.orderCancelled'))
    await fetchOrders()
    await fetchUnshipped()
  } catch {
    // User cancelled the dialog or API error (message already shown by interceptor)
  }
}

// --- Track Dialog ---

function openTrackDialog(order) {
  trackOrder.value = order
  trackDialogVisible.value = true
}

// --- Review Dialog ---

function openReviewDialog(order, item) {
  reviewOrderId.value = order.id
  reviewProductId.value = item.productId
  reviewProductTitle.value = item.productTitle
  reviewRating.value = 5
  reviewContent.value = ''
  reviewDialogVisible.value = true
}

async function submitReview() {
  if (!reviewContent.value.trim()) {
    ElMessage.warning(t('account.pleaseWriteReview'))
    return
  }
  reviewSubmitting.value = true
  try {
    await api.post('/reviews', {
      productId: reviewProductId.value,
      orderId: reviewOrderId.value,
      rating: reviewRating.value,
      content: reviewContent.value.trim()
    })
    ElMessage.success(t('account.reviewSubmitted'))
    reviewDialogVisible.value = false
    reviewedMap[`${reviewOrderId.value}-${reviewProductId.value}`] = true
  } catch {
    // Error message already shown by API interceptor
  } finally {
    reviewSubmitting.value = false
  }
}

// --- Helpers ---

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString()
}

function getFirstLetter(title) {
  if (!title) return '?'
  return title.charAt(0).toUpperCase()
}

function statusType(status) {
  switch (status) {
    case 'PENDING_PAYMENT': return 'danger'
    case 'PAID': return 'success'
    case 'SHIPPED': return 'warning'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'info'
    default: return 'info'
  }
}

function statusLabel(status) {
  const key = `order.${status}`
  const translated = t(key)
  return translated !== key ? translated : status || t('order.UNKNOWN')
}

// 支付方式代码 → 本地化文案
function paymentMethodLabel(method) {
  if (!method) return t('checkout.noPaymentMethod')
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
  if (!key) return method
  return t(key)
}

function addToCart(item) {
  cartStore.addItem({
    id: item.productId,
    title: item.productTitle,
    price: item.unitPrice,
    primaryImage: item.productImage,
    quantity: 1
  })
  ElMessage.success(t('account.addedToCart'))
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@500;600;700&family=DM+Sans:wght@400;500;600&display=swap');

.order-list {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'DM Sans', -apple-system, sans-serif;
}

.order-tabs {
  margin-bottom: 24px;
}
.order-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}
.order-tabs :deep(.el-tabs__active-bar) {
  background-color: #b8860b;
}

/* Page title */
.page-title {
  margin-bottom: 20px;
}
.page-title h1 {
  font-family: 'Cormorant Garamond', Georgia, serif;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}
.page-subtitle {
  font-size: 14px;
  color: #8a8a93;
  margin: 2px 0 0;
}

/* 状态筛选提示 */
.filter-indicator {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  color: #b8860b;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
}
.filter-indicator button {
  border: none;
  background: transparent;
  color: #b8860b;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
}
.filter-indicator button:hover {
  text-decoration: underline;
}

/* Order Card — Numismatic Catalog style */
.order-card-wrapper {
  margin-bottom: 16px;
}

.order-card {
  border-radius: 12px !important;
  border: 1px solid #ece7e0 !important;
  box-shadow: 0 1px 3px rgba(26, 26, 46, 0.04) !important;
  overflow: hidden;
  transition: box-shadow 0.2s, transform 0.2s;
}
.order-card:hover {
  box-shadow: 0 6px 20px rgba(26, 26, 46, 0.08) !important;
  transform: translateY(-1px);
}
.order-card::before {
  content: '';
  display: block;
  height: 3px;
  background: linear-gradient(90deg, #b8860b, #d4a843, #b8860b);
}

.order-card :deep(.el-card__body) {
  padding: 0;
}

/* Order Header */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 22px;
  background: #faf8f5;
  border-bottom: 1px solid #ece7e0;
  border-radius: 12px 12px 0 0;
  flex-wrap: wrap;
  gap: 8px;
}

.order-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-header-right {
  display: flex;
  align-items: center;
}

.order-no {
  font-family: 'SF Mono', 'Courier New', monospace;
  font-size: 13px;
  color: #1a1a2e;
  font-weight: 600;
  background: #f0ece6;
  padding: 2px 8px;
  border-radius: 5px;
  letter-spacing: 0.02em;
  word-break: break-all;
  display: inline-block;
  max-width: 100%;
}

.status-tag {
  font-weight: 600;
  border-radius: 20px;
  padding: 0 12px;
  border: none;
}

.order-date {
  font-size: 13px;
  color: #8a8a93;
  font-weight: 500;
}

/* Order Body */
.order-body {
  padding: 4px 20px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  gap: 12px;
  flex-wrap: wrap;
}

.order-item:last-child {
  border-bottom: none;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.item-avatar {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  background: linear-gradient(135deg, #d4a843, #b8860b);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2);
}

.item-avatar-letter {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.item-title {
  font-weight: 500;
  color: #111827;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.item-sep {
  color: #d1d5db;
}

.item-price {
  color: #111827;
  font-weight: 600;
}

.item-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

/* Order Footer */
.order-footer {
  padding: 14px 22px;
  background: #faf8f5;
  border-top: 1px solid #ece7e0;
  border-radius: 0 0 12px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.footer-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-total {
  font-size: 14px;
  color: #374151;
}

.footer-total strong {
  color: #111827;
}

.footer-meta {
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}
.footer-actions .btn-pay {
  margin-right: 8px;
  padding: 0 16px;
  height: 30px;
  background: linear-gradient(135deg, #b8860b, #d4a843);
  border: none;
  color: #fff;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(184, 134, 11, 0.3);
}
.footer-actions .btn-pay:hover {
  opacity: 0.92;
  box-shadow: 0 3px 10px rgba(184, 134, 11, 0.4);
  transform: translateY(-1px);
}
.footer-actions .btn-secondary {
  color: #6b7280;
  font-size: 13px;
  border: 1px solid #e5e0d6;
  border-radius: 8px;
  height: 30px;
  padding: 0 12px;
  background: #fff;
}
.footer-actions .btn-secondary:hover {
  color: #b8860b;
  border-color: #b8860b;
  background: #faf8f5;
}

/* Buy Again Grid */
.buy-again-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.buy-again-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  background: #fff;
  transition: box-shadow 0.15s;
}

.buy-again-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.buy-again-image {
  width: 100%;
  height: 150px;
  overflow: hidden;
  margin-bottom: 8px;
  border-radius: 4px;
}

.buy-again-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.buy-again-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
  color: #6366f1;
}

.buy-again-title {
  font-size: 13px;
  color: #111827;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.buy-again-price {
  font-size: 16px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 8px;
}

/* Track Dialog */
.track-info {
  padding: 8px 0;
}

.track-row {
  display: flex;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.track-row:last-child {
  border-bottom: none;
}

.track-label {
  width: 140px;
  font-size: 14px;
  color: #6b7280;
  flex-shrink: 0;
}

.track-value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
  word-break: break-all;
}

.track-number {
  font-family: 'SFMono-Regular', Consolas, monospace;
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

/* Review Dialog */
.review-form {
  padding: 8px 0;
}

.review-product {
  font-size: 14px;
  color: #374151;
  margin-bottom: 20px;
  padding: 10px 14px;
  background: #f9fafb;
  border-radius: 6px;
}

.review-field {
  margin-bottom: 18px;
}

.review-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

/* Responsive */
@media (max-width: 640px) {
  .order-list {
    padding: 16px 12px;
  }
  .order-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .order-footer {
    flex-direction: column;
    align-items: flex-start;
  }
  .footer-actions {
    width: 100%;
    justify-content: flex-start;
  }
  .item-actions {
    width: 100%;
    justify-content: flex-start;
  }
  .track-label {
    width: 100px;
  }
}
</style>
