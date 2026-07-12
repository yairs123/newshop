<template>
  <div class="checkout-page">
    <div class="checkout-inner">
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cart' }">{{ $t('cart.pageTitle') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('cart.checkout') }}</el-breadcrumb-item>
      </el-breadcrumb>
      <h2 class="checkout-title">{{ $t('cart.checkout') }}</h2>

      <div v-loading="loading" style="min-height: 200px;">
      <!-- Saved Addresses -->
      <section class="checkout-section" v-if="savedAddresses.length > 0">
        <h3 class="section-title">{{ $t('checkout.shippingAddress') }}</h3>
        <div class="saved-addresses">
          <div
            v-for="addr in savedAddresses"
            :key="addr.id"
            :class="['saved-addr-card', { active: selectedAddrId === addr.id }]"
            @click="selectAddress(addr)"
          >
            <el-radio :value="addr.id" v-model="selectedAddrId" size="large">
              <div class="addr-card-body">
                <strong>{{ addr.fullName }}</strong>
                <span v-if="addr.phone"> · {{ addr.phone }}</span>
                <p>{{ addr.address }}, {{ addr.city }} {{ addr.zipCode }}, {{ $t('countries.' + addr.country) }}</p>
              </div>
            </el-radio>
            <el-tag v-if="addr.isDefault" type="success" size="small">{{ $t('account.defaultAddress') }}</el-tag>
          </div>
          <div
            :class="['saved-addr-card', 'new-addr-card', { active: selectedAddrId === 'new' }]"
            @click="selectNewAddress"
          >
            <el-radio value="new" v-model="selectedAddrId" size="large">
              <span class="new-addr-label">+ {{ $t('account.addAddress') }}</span>
            </el-radio>
          </div>
        </div>
      </section>

      <!-- Manual Address Form -->
      <section class="checkout-section" v-show="selectedAddrId === 'new' || savedAddresses.length === 0">
        <h3 class="section-title">{{ $t('checkout.shippingAddress') }}</h3>
        <div class="address-form">
          <div class="form-row">
            <el-input v-model="form.fullName" :placeholder="$t('common.fullName')" size="large" />
          </div>
          <div class="form-row">
            <el-input v-model="form.phone" :placeholder="$t('common.phone')" size="large" />
          </div>
          <div class="form-row">
            <el-input v-model="form.address" type="textarea" :rows="2" :placeholder="$t('common.address')" size="large" />
          </div>
          <div class="form-row form-row-split">
            <el-input v-model="form.city" :placeholder="$t('common.city')" size="large" />
            <el-input v-model="form.zipCode" :placeholder="$t('common.zipCode')" size="large" @input="onZipInput" />
            <small v-if="zipLoading" class="zip-loading">{{ $t('checkout.searchingZip') }}</small>
          </div>
          <div class="form-row">
            <el-select v-model="form.country" :placeholder="$t('common.country')" size="large" style="width:100%">
              <el-option v-for="c in countries" :key="c" :label="$t('countries.' + c)" :value="c" />
            </el-select>
          </div>
        </div>
      </section>

      <!-- Order Summary -->
      <section class="checkout-section">
        <h3 class="section-title">{{ $t('checkout.orderSummary') }}</h3>
        <div class="order-items">
          <div v-for="item in cartStore.items" :key="item.id" class="order-item">
            <div class="oi-image">{{ item.title.charAt(0) }}</div>
            <div class="oi-info">
              <p class="oi-title">{{ item.title }}</p>
              <div class="oi-meta" v-if="item.country || item.year">
                <span v-if="item.country">{{ $t('countries.' + item.country) }}</span>
                <span v-if="item.year">{{ item.year }}</span>
              </div>
            </div>
            <div class="oi-qty">{{ $t('cart.colQuantity') }}: {{ item.quantity }}</div>
            <div class="oi-price">{{ formatPrice(item.price * item.quantity) }}</div>
          </div>
        </div>
      </section>

      <!-- Shipping Method -->
      <section class="checkout-section">
        <h3 class="section-title">{{ $t('checkout.shippingMethod') }}</h3>
        <div class="method-list">
          <label
            v-for="s in shippingMethods"
            :key="s.value"
            :class="['method-option', { active: form.shippingMethod === s.value }]"
          >
            <el-radio v-model="form.shippingMethod" :value="s.value" size="large">
              <div class="method-content">
                <div class="method-left">
                  <span class="method-icon" v-html="s.icon"></span>
                  <div>
                    <span class="method-label">{{ $t(s.label) }}</span>
                    <span class="method-desc" v-if="s.desc">{{ $t(s.desc) }}</span>
                  </div>
                </div>
                <span v-if="s.fee === 0" class="method-fee free">{{ $t('checkout.freeShipping') }}</span>
                <span v-else class="method-fee">{{ formatPrice(s.fee) }}</span>
              </div>
            </el-radio>
          </label>
        </div>
      </section>

      <!-- Payment Method -->
      <section class="checkout-section">
        <h3 class="section-title">{{ $t('checkout.paymentMethod') }}</h3>
        <div class="method-list payment-list">
          <label
            v-for="p in paymentMethods"
            :key="p.value"
            :class="['method-option', { active: form.paymentMethod === p.value }]"
          >
            <el-radio v-model="form.paymentMethod" :value="p.value" size="large">
              <div class="method-content">
                <span class="method-icon" v-html="p.icon"></span>
                <span class="method-label">{{ $t(p.label) }}</span>
              </div>
            </el-radio>
          </label>
        </div>
      </section>

      <!-- Buyer Note -->
      <section class="checkout-section">
        <h3 class="section-title">{{ $t('checkout.buyerNote') }}</h3>
        <el-input v-model="form.buyerNote" type="textarea" :rows="2" :placeholder="$t('checkout.buyerNote')" />
      </section>

      <!-- Totals & Submit -->
      <section class="checkout-footer">
        <div class="total-row">
          <span>{{ $t('cartDrawer.subtotal') }}</span>
          <span>{{ formatPrice(cartStore.totalAmount) }}</span>
        </div>
        <div class="total-row">
          <span>{{ $t('checkout.shippingFee') }}</span>
          <span v-if="shippingFee === 0" class="free-ship">{{ $t('checkout.freeShipping') }}</span>
          <span v-else>{{ formatPrice(shippingFee) }}</span>
        </div>
        <div class="total-divider"></div>
        <div class="total-row total-final">
          <span>{{ $t('checkout.orderTotal') }}</span>
          <span class="final-amount">{{ formatPrice(cartStore.totalAmount + shippingFee) }}</span>
        </div>
        <el-button
          type="primary"
          size="large"
          class="place-order-btn"
          :loading="submitting"
          :disabled="cartStore.items.length === 0"
          @click="placeOrder"
        >
          {{ $t('checkout.placeOrder') }}
        </el-button>
      </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { useCartStore } from '../store/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const { t, locale } = useI18n()
const loading = ref(true)
const submitting = ref(false)
const savedAddresses = ref([])
const selectedAddrId = ref('new')
const zipLoading = ref(false)
let zipTimer = null

const form = reactive({
  fullName: '',
  phone: '',
  address: '',
  city: '',
  zipCode: '',
  country: '',
  shippingMethod: 'STANDARD',
  paymentMethod: 'CREDIT_CARD',
  buyerNote: '',
})

const shippingFee = computed(() => {
  switch (form.shippingMethod) {
    case 'STANDARD': return 0
    case 'REGISTERED': return 12
    case 'EXPRESS': return 15
    case 'PRIORITY': return 25
    default: return 0
  }
})

const countries = ['USA','China','Canada','UK','Germany','France','Japan','Spain','Italy','Greece','Roman Empire','Byzantine Empire','Luxembourg','Vatican','Vietnam']

const shippingMethods = [
  { value: 'STANDARD', label: 'checkout.shipStandard', fee: 0, icon: '📦' },
  { value: 'EXPRESS', label: 'checkout.shipExpress', fee: 15, icon: '🚚' },
  { value: 'PRIORITY', label: 'checkout.shipPriority', fee: 25, icon: '✈️' },
  { value: 'REGISTERED', label: 'checkout.shipRegistered', fee: 12, icon: '📬' },
]

const paymentMethods = [
  { value: 'CREDIT_CARD', label: 'checkout.payCreditCard', icon: '💳' },
  { value: 'PAYPAL', label: 'checkout.payPaypal', icon: '<span class="pay-icon paypal">P</span>' },
  { value: 'ALIPAY', label: 'checkout.payAlipay', icon: '<span class="pay-icon alipay">A</span>' },
  { value: 'WECHAT_PAY', label: 'checkout.payWechat', icon: '<span class="pay-icon wechat">W</span>' },
  { value: 'GRABPAY', label: 'checkout.payGrabPay', icon: '<span class="pay-icon grabpay">G</span>' },
  { value: 'PAYNOW', label: 'checkout.payPayNow', icon: '<span class="pay-icon paynow">N</span>' },
  { value: 'BANK_TRANSFER', label: 'checkout.payBankTransfer', icon: '🏦' },
]

onMounted(async () => {
  try {
    const res = await api.get('/addresses')
    savedAddresses.value = res.data || []
    const def = savedAddresses.value.find(a => a.isDefault)
    if (def) {
      selectAddress(def)
    }
  } catch (e) { /* ignore */ }
  loading.value = false
})

function selectAddress(addr) {
  selectedAddrId.value = addr.id
  form.fullName = addr.fullName || ''
  form.phone = addr.phone || ''
  form.address = addr.address || ''
  form.city = addr.city || ''
  form.zipCode = addr.zipCode || ''
  form.country = addr.country || ''
}

function selectNewAddress() {
  selectedAddrId.value = 'new'
  form.fullName = ''
  form.phone = ''
  form.address = ''
  form.city = ''
  form.zipCode = ''
  form.country = ''
}

function onZipInput() {
  if (zipTimer) clearTimeout(zipTimer)
  const zip = form.zipCode.replace(/\D/g, '')
  if (zip.length >= 7 && form.country === 'Japan') {
    zipLoading.value = true
    zipTimer = setTimeout(async () => {
      try {
        const res = await fetch(`https://zipcloud.ibsnet.co.jp/api/search?zipcode=${zip}`)
        const data = await res.json()
        if (data.results && data.results[0]) {
          const r = data.results[0]
          form.city = `${r.address1} ${r.address2} ${r.address3}`.trim()
        }
      } catch (e) { /* silent */ }
      zipLoading.value = false
    }, 500)
  }
}

async function placeOrder() {
  if (!form.fullName || !form.address) {
    ElMessage.warning(t('checkout.fillRequiredFields'))
    return
  }
  submitting.value = true
  const fullAddress = `${form.fullName}, ${form.phone ? form.phone + ', ' : ''}${form.address}, ${form.city} ${form.zipCode}, ${form.country}`
  try {
    // Create order with all cart items
    const items = cartStore.items.map(i => ({
      productId: i.id,
      quantity: i.quantity,
    }))
    const orderRes = await api.post('/orders/batch', {
      items,
      shippingAddress: fullAddress,
      paymentMethod: form.paymentMethod,
      shippingMethod: form.shippingMethod,
      buyerNote: form.buyerNote,
    })
    const order = orderRes.data

    // Clear local cart
    cartStore.clear()

    // Process payment with return/cancel URLs
    const returnUrl = window.location.origin + '/payment/return?orderId=' + order.id
    const cancelUrl = window.location.origin + '/orders/' + order.id
    const payRes = await api.post('/orders/' + order.id + '/pay', {
      returnUrl,
      cancelUrl,
    })

    if (payRes.data && payRes.data.paymentUrl) {
      // Redirect to external payment gateway
      window.location.href = payRes.data.paymentUrl
    } else {
      // Payment completed inline
      ElMessage.success(t('checkout.orderPlaced'))
      router.push('/orders')
    }
  } catch (e) {
    // Error message handled by API interceptor
  }
  submitting.value = false
}

function formatPrice(p) {
  const loc = locale.value || 'en'
  const prefix = loc.startsWith('zh') || loc === 'ja' ? '¥' : '$'
  return prefix + Number(p).toLocaleString(loc, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style>
.checkout-page { background: #f5f5f7; min-height: 100vh; }
.checkout-inner { max-width: 800px; margin: 0 auto; padding: 32px 20px 64px; }
.checkout-title { font-size: 26px; font-weight: 700; color: #1d1d1f; margin-bottom: 24px; }

.checkout-section {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* Saved Addresses */
.saved-addresses { display: flex; flex-direction: column; gap: 8px; }
.saved-addr-card {
  display: flex; align-items: flex-start;
  border: 1.5px solid #e5e5e7;
  border-radius: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all .15s;
  gap: 8px;
}
.saved-addr-card:hover { border-color: #c0c0c0; }
.saved-addr-card.active { border-color: #0071e3; background: #f5f9ff; }
.saved-addr-card .el-radio { align-items: flex-start; }
.addr-card-body p { font-size: 13px; color: #6b7280; margin-top: 2px; }
.new-addr-card { justify-content: center; border-style: dashed; }
.new-addr-label { color: #0071e3; font-weight: 500; }

/* Order Items */
.order-items { display: flex; flex-direction: column; gap: 10px; }
.order-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; }
.order-item + .order-item { border-top: 1px solid #f5f5f7; }
.oi-image { width: 40px; height: 40px; background: #f0f0f0; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #999; font-size: 14px; flex-shrink: 0; }
.oi-info { flex: 1; min-width: 0; }
.oi-title { font-size: 13px; font-weight: 500; color: #1d1d1f; line-height: 1.3; }
.oi-meta { display: flex; gap: 4px; margin-top: 2px; }
.oi-meta span { font-size: 11px; color: #86868b; }
.oi-qty { font-size: 13px; color: #86868b; white-space: nowrap; }
.oi-price { font-size: 14px; font-weight: 600; color: #1d1d1f; min-width: 80px; text-align: right; }

/* Address Form */
.address-form { display: flex; flex-direction: column; gap: 12px; }
.form-row-split { display: flex; gap: 12px; align-items: flex-start; }
.form-row-split .el-input { flex: 1; }
.zip-loading { font-size: 12px; color: #6b7280; margin-top: 8px; white-space: nowrap; }

/* Method List */
.method-list { display: flex; flex-direction: column; gap: 8px; }
.method-option {
  display: block;
  border: 1.5px solid #e5e5e7;
  border-radius: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: all .15s;
}
.method-option:hover { border-color: #c0c0c0; }
.method-option.active { border-color: #0071e3; background: #f5f9ff; }
.method-option .el-radio { display: flex; align-items: center; width: 100%; }
.method-option .el-radio__label { flex: 1; }
.method-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 12px;
}
.method-left { display: flex; align-items: center; gap: 10px; }
.method-icon { font-size: 22px; line-height: 1; }
.pay-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
}
.pay-icon.paypal { background: #003087; }
.pay-icon.alipay { background: #1677ff; }
.pay-icon.wechat { background: #07c160; }
.pay-icon.grabpay { background: #00b14f; }
.pay-icon.paynow { background: #ed1c24; }
.method-label { font-size: 14px; font-weight: 500; color: #1d1d1f; }
.method-desc { font-size: 12px; color: #86868b; display: block; margin-top: 2px; }
.method-fee { font-size: 14px; font-weight: 600; color: #1d1d1f; white-space: nowrap; }
.method-fee.free { color: #34c759; font-weight: 500; }
.payment-list .method-content { justify-content: flex-start; gap: 14px; }
.payment-list .method-label { font-size: 14px; }

/* Footer */
.checkout-footer {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}
.total-row { display: flex; justify-content: space-between; align-items: center; font-size: 14px; color: #1d1d1f; padding: 4px 0; }
.total-divider { height: 1px; background: #e5e5e7; margin: 12px 0; }
.total-final { font-size: 16px; font-weight: 700; margin-bottom: 20px; }
.final-amount { font-size: 22px; color: #dc2626; }
.free-ship { color: #34c759; font-weight: 500; }
.place-order-btn { width: 100%; height: 52px; font-size: 16px; border-radius: 12px; }

/* Responsive */
@media (max-width: 600px) {
  .checkout-inner { padding: 16px 12px 40px; }
  .checkout-section { padding: 16px; }
}
</style>
