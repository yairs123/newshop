<template>
  <div class="invoice-page">
    <div class="no-print" style="margin-bottom:16px">
      <el-button @click="$router.push('/orders')">← 返回</el-button>
      <el-button type="primary" @click="windowPrint" style="margin-left:8px">打印 / PDF</el-button>
    </div>

    <div v-loading="loading" class="invoice-sheet" ref="invoiceSheet">
      <div v-if="order" class="invoice">
        <!-- Header -->
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
                <p class="company-sub">Premium Numismatics</p>
              </div>
            </div>
            <div class="company-address">
              <p>123 Coin Street, Numismatic City</p>
              <p>Tel: +1-555-0123 | Email: info@coinmarket.com</p>
              <p>Registration: T5020001111665</p>
            </div>
          </div>
        </div>

        <!-- Invoice Title -->
        <div class="invoice-title-row">
          <div class="invoice-title">
            <h2>INVOICE</h2>
            <p class="invoice-no">No. {{ order.orderNo }}</p>
          </div>
          <div class="invoice-date">
            <p>Date: {{ formatDate(order.createdAt) }}</p>
            <p class="status-tag">{{ order.status }}</p>
          </div>
        </div>

        <!-- Customer -->
        <div class="customer-row">
          <div class="customer-info">
            <h4>Bill To</h4>
            <p class="customer-name">{{ order.buyerName || 'N/A' }}</p>
            <p>{{ order.shippingAddress || '' }}</p>
            <p>Buyer ID: {{ order.buyerId }}</p>
          </div>
          <div class="payment-info">
            <h4>Payment</h4>
            <p>Method: {{ order.paymentMethod || 'N/A' }}</p>
            <p>Shipping: {{ order.shippingMethod || 'N/A' }}</p>
          </div>
        </div>

        <!-- Items Table -->
        <table class="items-table">
          <thead>
            <tr>
              <th class="col-num">#</th>
              <th class="col-desc">Description</th>
              <th class="col-qty">Qty</th>
              <th class="col-price">Unit Price</th>
              <th class="col-total">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, i) in order.items" :key="item.id">
              <td class="col-num">{{ i + 1 }}</td>
              <td class="col-desc">{{ item.productTitle }}</td>
              <td class="col-qty">{{ item.quantity }}</td>
              <td class="col-price">{{ formatMoney(item.unitPrice) }}</td>
              <td class="col-total">{{ formatMoney(item.subtotal) }}</td>
            </tr>
          </tbody>
        </table>

        <!-- Totals -->
        <div class="totals-row">
          <div class="totals-left">
            <p class="notes-label">Notes:</p>
            <p class="notes-text">{{ order.buyerNote || '—' }}</p>
          </div>
          <div class="totals-right">
            <div class="total-line">
              <span>Subtotal</span>
              <span>{{ formatMoney(order.totalAmount) }}</span>
            </div>
            <div class="total-line">
              <span>Shipping</span>
              <span>—</span>
            </div>
            <div class="total-line grand-total">
              <span>Grand Total</span>
              <span>{{ order.currency || 'USD' }} {{ formatMoney(order.totalAmount) }}</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="invoice-footer">
          <p>Thank you for your business!</p>
          <p class="footer-small">CoinMarket - Premium Numismatics | www.coinmarket.com</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)
const invoiceSheet = ref(null)

onMounted(async () => {
  try {
    const res = await api.get(`/admin/orders/${route.params.id}`)
    order.value = res.data
  } catch (e) {}
  loading.value = false
})

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function formatMoney(n) {
  return (Number(n) || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function windowPrint() {
  window.print()
}
</script>

<style scoped>
.invoice-page { background: #f3f4f6; min-height: 100vh; padding: 20px; }

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
.company { display: flex; justify-content: space-between; align-items: flex-start; }
.company-logo { display: flex; align-items: center; gap: 10px; }
.company-logo h1 { font-size: 24px; font-weight: 800; color: #111; margin: 0; }
.company-sub { font-size: 11px; color: #6b7280; text-transform: uppercase; letter-spacing: .05em; }
.company-address { text-align: right; font-size: 11px; color: #6b7280; line-height: 1.6; }

/* Title row */
.invoice-title-row {
  display: flex; justify-content: space-between; align-items: flex-end;
  margin-bottom: 28px; padding-bottom: 16px; border-bottom: 2px solid #111;
}
.invoice-title h2 { font-size: 28px; font-weight: 800; color: #111; margin: 0; letter-spacing: .02em; }
.invoice-no { font-size: 13px; color: #6b7280; margin-top: 4px; }
.invoice-date { text-align: right; font-size: 12px; color: #6b7280; }
.status-tag { font-weight: 600; color: #059669; text-transform: uppercase; }

/* Customer */
.customer-row {
  display: flex; justify-content: space-between;
  margin-bottom: 28px; padding-bottom: 20px; border-bottom: 1px solid #e5e7eb;
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
.totals-row { display: flex; justify-content: space-between; margin-bottom: 32px; }
.totals-left { flex: 1; }
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
  .no-print { display: none !important; }
  .invoice { box-shadow: none; padding: 20px 30px; border-radius: 0; }
}

@page { size: A4; margin: 15mm; }
</style>

<style>
@media print {
  .app-aside,
  .app-header {
    display: none !important;
  }
  .app-main {
    padding: 0 !important;
    margin-left: 0 !important;
  }
}
</style>
