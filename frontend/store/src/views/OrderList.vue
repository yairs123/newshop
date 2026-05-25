<template>
  <div class="order-list">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ $t('account.orders') }}</el-breadcrumb-item>
    </el-breadcrumb>
    <el-tabs v-model="activeTab" class="order-tabs">
      <el-tab-pane :label="$t('account.myOrders')" name="orders">
        <div v-loading="ordersLoading">
          <el-empty v-if="orders.length === 0" :description="$t('account.noOrders')" />
          <div v-for="order in orders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-header-left">
                <span class="order-meta">{{ $t('order.createdAt') }}: {{ formatDate(order.createdAt) }}</span>
                <span class="order-meta">{{ $t('account.total') }}: ${{ order.totalAmount }}</span>
                <span class="order-meta">{{ $t('account.shipTo') }}: {{ order.buyerName || '-' }}</span>
              </div>
              <div class="order-header-right">
                <span class="order-no">#{{ order.orderNo }}</span>
                <el-button size="small" @click="$router.push(`/orders/${order.id}`)">{{ $t('account.viewDetails') }}</el-button>
                <el-button size="small">{{ $t('account.invoice') }}</el-button>
              </div>
            </div>
            <div class="order-body">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <div class="item-info">
                  <span class="item-title">{{ item.productTitle }}</span>
                  <span class="item-qty">x{{ item.quantity }}</span>
                  <span class="item-price">${{ item.subtotal }}</span>
                </div>
                <div class="item-actions">
                  <el-button size="small" text>{{ $t('account.trackPackage') }}</el-button>
                  <el-button size="small" text>{{ $t('account.returnItem') }}</el-button>
                  <el-button size="small" text>{{ $t('account.share') }}</el-button>
                  <el-button size="small" text>{{ $t('account.askProduct') }}</el-button>
                  <el-button size="small" text>{{ $t('account.rateSeller') }}</el-button>
                  <el-button size="small" text>{{ $t('account.writeReview') }}</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('account.buyAgain')" name="buyAgain">
        <div v-loading="buyAgainLoading">
          <el-empty v-if="buyAgainItems.length === 0" :description="$t('account.noBuyAgain')" />
          <div class="buy-again-grid">
            <div v-for="item in buyAgainItems" :key="item.productId" class="buy-again-card">
              <div class="buy-again-image">
                <img :src="item.productImage || 'https://placehold.co/200x200?text=No+Image'" :alt="item.productTitle" />
              </div>
              <div class="buy-again-title">{{ item.productTitle }}</div>
              <div class="buy-again-price">${{ item.unitPrice }}</div>
              <el-button type="primary" size="small" @click="addToCart(item)">{{ $t('account.addToCart') }}</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('account.notYetShipped')" name="unshipped">
        <div v-loading="unshippedLoading">
          <el-empty v-if="unshippedOrders.length === 0" :description="$t('account.noOrders')" />
          <div v-for="order in unshippedOrders" :key="order.id" class="order-card">
            <div class="order-header">
              <div class="order-header-left">
                <span class="order-meta">{{ $t('order.createdAt') }}: {{ formatDate(order.createdAt) }}</span>
                <span class="order-meta">{{ $t('account.total') }}: ${{ order.totalAmount }}</span>
                <span class="order-meta">{{ $t('account.shipTo') }}: {{ order.buyerName || '-' }}</span>
              </div>
              <div class="order-header-right">
                <span class="order-no">#{{ order.orderNo }}</span>
                <el-button size="small" @click="$router.push(`/orders/${order.id}`)">{{ $t('account.viewDetails') }}</el-button>
              </div>
            </div>
            <div class="order-body">
              <div v-for="item in order.items" :key="item.id" class="order-item">
                <div class="item-info">
                  <span class="item-title">{{ item.productTitle }}</span>
                  <span class="item-qty">x{{ item.quantity }}</span>
                  <span class="item-price">${{ item.subtotal }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { useCartStore } from '../store/cart'
import { ElMessage } from 'element-plus'

const cartStore = useCartStore()
const activeTab = ref('orders')

const orders = ref([])
const ordersLoading = ref(true)

const buyAgainItems = ref([])
const buyAgainLoading = ref(true)

const unshippedOrders = ref([])
const unshippedLoading = ref(true)

onMounted(() => {
  fetchOrders()
  fetchBuyAgain()
  fetchUnshipped()
})

async function fetchOrders() {
  try {
    const res = await api.get('/orders/buyer')
    orders.value = res.data || []
  } catch (e) { orders.value = [] }
  ordersLoading.value = false
}

async function fetchBuyAgain() {
  try {
    const res = await api.get('/orders/buyer/items')
    buyAgainItems.value = res.data || []
  } catch (e) { buyAgainItems.value = [] }
  buyAgainLoading.value = false
}

async function fetchUnshipped() {
  try {
    const res = await api.get('/orders/buyer/unshipped')
    unshippedOrders.value = res.data || []
  } catch (e) { unshippedOrders.value = [] }
  unshippedLoading.value = false
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return d.toLocaleDateString()
}

function addToCart(item) {
  cartStore.addItem({
    productId: item.productId,
    title: item.productTitle,
    price: item.unitPrice,
    image: item.productImage,
    quantity: 1
  })
  ElMessage.success('Added to cart')
}
</script>

<style scoped>
.order-list { max-width: 1000px; margin: 0 auto; padding: 24px; }
.order-tabs { margin-bottom: 24px; }
.empty-state { text-align: center; padding: 60px 20px; color: #9ca3af; font-size: 16px; }
.order-card { border: 1px solid #e5e7eb; border-radius: 8px; margin-bottom: 16px; background: #fff; }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; border-radius: 8px 8px 0 0; flex-wrap: wrap; gap: 8px; }
.order-header-left { display: flex; gap: 16px; flex-wrap: wrap; }
.order-meta { font-size: 13px; color: #6b7280; }
.order-header-right { display: flex; align-items: center; gap: 8px; }
.order-no { font-size: 13px; color: #3b82f6; font-weight: 600; }
.order-body { padding: 12px 16px; }
.order-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #f3f4f6; flex-wrap: wrap; gap: 8px; }
.order-item:last-child { border-bottom: none; }
.item-info { display: flex; gap: 12px; align-items: center; }
.item-title { font-weight: 500; color: #111827; font-size: 14px; }
.item-qty { color: #9ca3af; font-size: 13px; }
.item-price { color: #111827; font-size: 14px; font-weight: 600; }
.item-actions { display: flex; gap: 4px; flex-wrap: wrap; }
.buy-again-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.buy-again-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 12px; text-align: center; background: #fff; }
.buy-again-image { width: 100%; height: 150px; overflow: hidden; margin-bottom: 8px; }
.buy-again-image img { width: 100%; height: 100%; object-fit: contain; }
.buy-again-title { font-size: 13px; color: #111827; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.buy-again-price { font-size: 16px; font-weight: 700; color: #3b82f6; margin-bottom: 8px; }
@media (max-width: 640px) {
  .order-header { flex-direction: column; align-items: flex-start; }
  .item-actions { margin-top: 4px; }
}
</style>
