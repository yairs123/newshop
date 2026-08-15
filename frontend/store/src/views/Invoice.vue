<template>
  <div class="invoice-page">
    <!-- 工具栏（打印时隐藏） -->
    <div class="page-toolbar no-print">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">{{ $t('account.orders') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('account.invoice') }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="toolbar-actions">
        <el-button type="primary" @click="handlePrint">
          <el-icon style="margin-right:6px"><Printer /></el-icon>
          {{ $t('account.print') }}
        </el-button>
        <el-button @click="$router.push('/orders/' + orderId)">
          {{ $t('account.viewDetails') }}
        </el-button>
      </div>
    </div>

    <!-- 账单内容 -->
    <div v-loading="loading" class="invoice-sheet">
      <div v-if="order" ref="invoiceRef" class="invoice">
        <!-- 公司抬头 -->
        <div class="invoice-header">
          <div class="company">
            <div class="company-logo">
              <svg viewBox="0 0 40 40" width="40" height="40">
                <circle cx="20" cy="20" r="18" fill="#f59e0b" stroke="#b45309" stroke-width="2"/>
                <text x="20" y="20" text-anchor="middle" dominant-baseline="central"
                      font-size="16" font-weight="bold" fill="#fff" font-family="Arial">$</text>
              </svg>
              <div>
                <h1>CoinMarket</h1>
                <p class="company-sub">{{ $t('invoice.companySubtitle') }}</p>
              </div>
            </div>
            <div class="company-address">
              <p>{{ $t('invoice.companyAddress') }}</p>
              <p>{{ $t('invoice.contact') }}</p>
            </div>
          </div>
        </div>

        <!-- 账单标题 -->
        <div class="invoice-title-row">
          <div class="invoice-title">
            <h2>{{ $t('account.invoice').toUpperCase() }}</h2>
            <p class="invoice-no">{{ $t('account.orderNo') }}: {{ order.orderNo }}</p>
          </div>
          <div class="invoice-date">
            <p>{{ $t('account.invoiceDate') }}: {{ formatDate(order.createdAt) }}</p>
          </div>
        </div>

        <!-- 客户信息 -->
        <div class="customer-row">
          <div class="customer-info">
            <h4>{{ $t('account.buyer') }}</h4>
            <p class="customer-name">{{ order.buyerName || '-' }}</p>
            <p>{{ order.shippingAddress || '' }}</p>
          </div>
          <div class="payment-info">
            <h4>{{ $t('account.paymentMethod') }}</h4>
            <p>{{ paymentMethodLabel(order.paymentMethod) }}</p>
            <p v-if="order.paidAt">{{ $t('order.paidAt') }}: {{ formatDate(order.paidAt) }}</p>
          </div>
        </div>

        <!-- 商品明细 -->
        <table class="items-table">
          <thead>
            <tr>
              <th class="col-num">#</th>
              <th class="col-desc">{{ $t('cart.colProduct') }}</th>
              <th class="col-qty">{{ $t('cart.colQuantity') }}</th>
              <th class="col-price">{{ $t('cart.colPrice') }}</th>
              <th class="col-total">{{ $t('account.itemTotal') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, i) in order.items" :key="item.id">
              <td class="col-num">{{ i + 1 }}</td>
              <td class="col-desc">{{ item.productTitle }}</td>
              <td class="col-qty">{{ item.quantity }}</td>
              <td class="col-price">{{ formatPrice(item.unitPrice) }}</td>
              <td class="col-total">{{ formatPrice(item.subtotal) }}</td>
            </tr>
          </tbody>
        </table>

        <!-- 合计 -->
        <div class="totals-row">
          <div class="totals-left">
            <p class="notes-label">{{ $t('checkout.buyerNote') }}:</p>
            <p class="notes-text">{{ order.buyerNote || '—' }}</p>
          </div>
          <div class="totals-right">
            <div class="total-line">
              <span>{{ $t('order.subtotal') }}</span>
              <span>{{ formatPrice(subtotal) }}</span>
            </div>
            <div class="total-line" v-if="order.shippingFee && order.shippingFee > 0">
              <span>{{ $t('order.shipping') }}</span>
              <span>{{ formatPrice(order.shippingFee) }}</span>
            </div>
            <div class="total-line grand-total">
              <span>{{ $t('account.total') }}</span>
              <span>{{ order.currency || 'USD' }} {{ formatPrice(order.totalAmount) }}</span>
            </div>
          </div>
        </div>

        <!-- 页脚 -->
        <div class="invoice-footer">
          <p>{{ $t('footer.copyright') }}</p>
          <p class="footer-small">{{ $t('footer.email') }} | {{ $t('invoice.website') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Printer } from '@element-plus/icons-vue'
import { api } from '../api'

const { t } = useI18n()
const route = useRoute()
const orderId = route.params.id
const order = ref(null)
const loading = ref(true)
const invoiceRef = ref(null)

const subtotal = computed(() => {
  if (!order.value?.items) return 0
  return order.value.items.reduce((sum, item) => sum + (item.subtotal || 0), 0)
})

onMounted(async () => {
  try {
    const res = await api.get('/orders/' + orderId)
    order.value = res.data
  } catch (e) { /* ignore */ }
  loading.value = false
})

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
  return key ? t(key) : method
}

function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString(undefined, { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function formatPrice(val) {
  return val != null ? Number(val).toFixed(2) : '0.00'
}

function handlePrint() {
  window.print()
}
</script>

<style scoped>
.invoice-page {
  min-height: 100dvh;
  background: #f3f4f6;
  padding: 20px;
}

.page-toolbar {
  max-width: 800px;
  margin: 0 auto 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.invoice-sheet {
  max-width: 800px;
  margin: 0 auto;
}

.invoice {
  background: #fff;
  padding: 48px 40px;
  box-shadow: 0 1px 4px rgba(0,0,0,.1);
  border-radius: 4px;
}

/* Header */
.invoice-header { margin-bottom: 32px; }
.company { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 16px; }
.company-logo { display: flex; align-items: center; gap: 10px; }
.company-logo h1 { font-size: 24px; font-weight: 800; color: #111; margin: 0; }
.company-sub { font-size: 11px; color: #6b7280; text-transform: uppercase; letter-spacing: .05em; }
.company-address { text-align: right; font-size: 11px; color: #6b7280; line-height: 1.6; }

/* Title row */
.invoice-title-row {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 28px; padding-bottom: 16px; border-bottom: 2px solid #111;
  flex-wrap: wrap; gap: 12px;
}
.invoice-title h2 { font-size: 28px; font-weight: 800; color: #111; margin: 0; letter-spacing: .02em; }
.invoice-no { font-size: 13px; color: #6b7280; margin-top: 4px; }
.invoice-date { text-align: right; font-size: 12px; color: #6b7280; }

/* Customer */
.customer-row {
  display: flex; justify-content: space-between;
  margin-bottom: 28px; padding-bottom: 20px; border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap; gap: 16px;
}
.customer-info h4, .payment-info h4 { font-size: 11px; text-transform: uppercase; color: #9ca3af; margin: 0 0 8px; letter-spacing: .04em; }
.customer-name { font-size: 15px; font-weight: 700; color: #111; }
.customer-info p, .payment-info p { font-size: 12px; color: #374151; margin: 2px 0; line-height: 1.5; }

/* Items table */
.items-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; }
.items-table th {
  font-size: 11px; text-transform: uppercase; color: #6b7280;
  padding: 8px 6px; border-bottom: 1px solid #d1d5db;
  text-align: right; letter-spacing: .03em;
}
.items-table th.col-num { text-align: center; width: 40px; }
.items-table th.col-desc { text-align: left; }
.items-table th.col-qty { text-align: center; width: 60px; }
.items-table th.col-price { width: 110px; }
.items-table th.col-total { width: 120px; }

.items-table td {
  font-size: 13px; padding: 6px; border-bottom: 1px solid #f3f4f6;
  text-align: right; color: #374151;
}
.items-table td.col-num { text-align: center; color: #9ca3af; }
.items-table td.col-desc { text-align: left; font-weight: 500; color: #111; }
.items-table td.col-qty { text-align: center; }

/* Totals */
.totals-row { display: flex; justify-content: space-between; margin-bottom: 32px; flex-wrap: wrap; gap: 16px; }
.totals-left { flex: 1; min-width: 200px; }
.notes-label { font-size: 11px; font-weight: 600; color: #6b7280; text-transform: uppercase; margin: 0 0 4px; }
.notes-text { font-size: 12px; color: #6b7280; margin: 0; }
.totals-right { width: 280px; }
.total-line {
  display: flex; justify-content: space-between;
  padding: 4px 0; font-size: 13px; color: #374151;
}
.grand-total {
  border-top: 2px solid #111;
  margin-top: 4px; padding-top: 8px;
  font-size: 16px; font-weight: 800; color: #111;
}

/* Footer */
.invoice-footer { text-align: center; padding-top: 20px; border-top: 1px solid #e5e7eb; }
.invoice-footer p { font-size: 13px; color: #374151; }
.footer-small { font-size: 11px; color: #9ca3af; margin-top: 4px; }

/* Print */
@media print {
  .invoice-page { padding: 0; background: #fff; min-height: auto; }
  .invoice { box-shadow: none; padding: 20px 30px; border-radius: 0; }
  .no-print { display: none !important; }
}

@media (max-width: 640px) {
  .invoice-page { padding: 12px; }
  .invoice { padding: 24px 20px; }
  .company { flex-direction: column; }
  .company-address { text-align: left; }
  .totals-right { width: 100%; }
}
</style>

<!-- 全局打印样式 -->
<style>
@media print {
  body { background: #fff !important; }
  .top-bar,
  .main-header,
  .main-nav,
  .main-footer,
  .cart-drawer,
  .el-dialog__wrapper,
  .el-overlay,
  #cart-drawer,
  .page-toolbar {
    display: none !important;
  }
  main {
    padding: 0 !important;
    margin: 0 !important;
  }
  #app {
    background: #fff !important;
  }
  .invoice-sheet {
    max-width: 100% !important;
  }
}
@page { size: A4; margin: 15mm; }
</style>
