<template>
  <div class="account-page">
    <div class="page-inner">
      <!-- Breadcrumb -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('account.myAccount') }}</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- Profile header -->
      <div class="profile-header">
        <div class="profile-avatar">
          <div class="avatar-circle">{{ initials }}</div>
          <div class="online-dot"></div>
        </div>
        <div class="profile-info">
          <h1>{{ username || $t('account.myAccount') }}</h1>
          <p class="profile-email">{{ email || '-' }}</p>
          <div class="profile-badges">
            <span class="badge badge-member">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
              {{ $t('account.member', '会员') }}
            </span>
            <span class="badge badge-orders">{{ $t('account.myOrders', '我的订单') }} · {{ orderCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- Order stats -->
      <div class="stats-row">
        <div class="stat-card stat-total" @click="goOrders('')">
          <span class="stat-icon">&#x1F4CB;</span>
          <div class="stat-body">
            <span class="stat-value">{{ orderCount || 0 }}</span>
            <span class="stat-label">{{ $t('account.allOrders', '全部订单') }}</span>
          </div>
        </div>
        <div class="stat-card stat-pending" @click="goOrders('PENDING_PAYMENT')">
          <span class="stat-icon">&#x23F3;</span>
          <div class="stat-body">
            <span class="stat-value">{{ pendingCount || 0 }}</span>
            <span class="stat-label">{{ $t('account.unpaidOrders', '待支付') }}</span>
          </div>
        </div>
        <div class="stat-card stat-paid" @click="goOrders('PAID')">
          <span class="stat-icon">&#x2714;&#xFE0F;</span>
          <div class="stat-body">
            <span class="stat-value">{{ paidCount || 0 }}</span>
            <span class="stat-label">{{ $t('account.paidOrders', '已支付') }}</span>
          </div>
        </div>
        <div class="stat-card stat-completed" @click="goOrders('COMPLETED')">
          <span class="stat-icon">&#x1F3C1;</span>
          <div class="stat-body">
            <span class="stat-value">{{ completedCount || 0 }}</span>
            <span class="stat-label">{{ $t('account.completedOrders', '已完成') }}</span>
          </div>
        </div>
      </div>

      <!-- Quick actions with icon -->
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
        {{ $t('account.quickActions', '快捷操作') }}
      </h2>
      <div class="actions-grid">
        <div v-for="item in menuItems" :key="item.route" class="action-card" @click="$router.push(item.route)">
          <div class="action-icon" v-html="item.iconHtml"></div>
          <div class="action-info">
            <span class="action-title">{{ $t(item.title) }}</span>
            <span class="action-desc">{{ $t(item.desc) }}</span>
          </div>
          <svg class="action-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
        </div>
      </div>

      <!-- Recent Orders (if any) -->
      <div v-if="recentOrders.length" class="recent-section">
        <h2 class="section-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
          {{ $t('account.recentOrders', '最近订单') }}
        </h2>
        <div class="recent-list">
          <div v-for="order in recentOrders" :key="order.id" class="recent-item" @click="$router.push('/orders/' + order.id)">
            <div class="recent-left">
              <span class="recent-no">{{ order.orderNo || '#' + order.id }}</span>
              <span :class="['recent-status', statusClass(order.status)]">{{ statusLabel(order.status) }}</span>
            </div>
            <div class="recent-right">
              <span class="recent-amount">{{ order.currency || 'USD' }} {{ order.totalAmount || '-' }}</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'

const router = useRouter()
const { t } = useI18n()

const username = ref('')
const email = ref('')
const orderCount = ref(0)
const pendingCount = ref(0)
const paidCount = ref(0)
const completedCount = ref(0)
const favCount = ref(0)
const msgCount = ref(0)
const recentOrders = ref([])

const initials = computed(() => {
  const u = username.value || 'U'
  return u.substring(0, 2).toUpperCase()
})

const menuItems = [
  { iconHtml: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 3v18M3 9h18"/></svg>', title: 'account.orders', desc: 'account.myOrders', route: '/orders' },
  { iconHtml: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="1.5"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>', title: 'account.addresses', desc: 'account.defaultAddress', route: '/addresses' },
  { iconHtml: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#8b5cf6" stroke-width="1.5"><rect x="1" y="4" width="22" height="16" rx="2"/><path d="M1 10h22"/></svg>', title: 'account.paymentMethods', desc: 'account.defaultPayment', route: '/payment-methods' },
  { iconHtml: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>', title: 'account.messages', desc: 'account.messageCenter', route: '/messages' },
  { iconHtml: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="1.5"><path d="M3 9h18v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9Z"/><path d="M3 9V5a2 2 0 0 1 2-2h4l2 3h6l2-3h4a2 2 0 0 1 2 2v4"/></svg>', title: 'account.openStore', desc: 'account.openStoreDesc', route: '/seller/apply' },
  { iconHtml: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#06b6d4" stroke-width="1.5"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>', title: 'account.contactUs', desc: 'account.needHelp', route: '/contact' }
]

const statusLabel = (status) => {
  if (!status) return ''
  const key = `order.${status}`
  const translated = t(key)
  return translated !== key ? translated : status
}

const statusClass = (status) => {
  const map = { PENDING_PAYMENT: 'status-pending', PAID: 'status-paid', SHIPPED: 'status-shipped', COMPLETED: 'status-done', CANCELLED: 'status-cancel' }
  return map[status] || ''
}

// 跳转到订单页并按状态筛选
function goOrders(status) {
  router.push({ path: '/orders', query: status ? { status } : {} })
}

onMounted(async () => {
  try {
    const meRes = await api.get('/users/me')
    const user = meRes.data || {}
    username.value = user.username || ''
    email.value = user.email || ''
  } catch (e) { /* not logged in */ }

  try {
    const ordersRes = await api.get('/orders/buyer')
    const orders = Array.isArray(ordersRes.data) ? ordersRes.data : []
    recentOrders.value = orders.slice(0, 3)
    orderCount.value = orders.length
    pendingCount.value = orders.filter(o => o.status === 'PENDING_PAYMENT').length
    paidCount.value = orders.filter(o => o.status === 'PAID' || o.status === 'SHIPPED').length
    completedCount.value = orders.filter(o => o.status === 'COMPLETED').length
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.account-page { background: #f5f5f7; min-height: 60vh; }
.page-inner { max-width: 900px; margin: 0 auto; padding: 32px 24px 64px; }
.breadcrumb { margin-bottom: 16px; }
.breadcrumb :deep(.el-breadcrumb__inner) { font-size: 13px; }

/* === Profile Header === */
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, #111827 0%, #1f2937 100%);
  border-radius: 16px;
  padding: 28px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.profile-header::after {
  content: '';
  position: absolute;
  top: -60%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(245,158,11,0.08) 0%, transparent 70%);
  pointer-events: none;
}

.avatar-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  position: relative;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  background: #10b981;
  border: 2px solid #1f2937;
  border-radius: 50%;
}

.profile-info { position: relative; z-index: 1; }

.profile-info h1 {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 2px;
}

.profile-email {
  font-size: 13px;
  color: #9ca3af;
  margin: 0 0 10px;
}

.profile-badges {
  display: flex;
  gap: 8px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.badge-member {
  background: rgba(245,158,11,0.15);
  color: #fbbf24;
}

.badge-orders {
  background: rgba(255,255,255,0.08);
  color: #d1d5db;
}

/* === Stats === */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  border-top: 3px solid #e5e7eb;
  transition: all 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

/* 各状态卡片配色 */
.stat-total { border-top-color: #b8860b; }
.stat-pending { border-top-color: #d97706; }
.stat-paid { border-top-color: #059669; }
.stat-completed { border-top-color: #2563eb; }

.stat-total .stat-icon { background: #fef3c7; color: #b8860b; }
.stat-pending .stat-icon { background: #fff7ed; color: #d97706; }
.stat-paid .stat-icon { background: #f0fdf4; color: #059669; }
.stat-completed .stat-icon { background: #eff6ff; color: #2563eb; }

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-body { display: flex; flex-direction: column; }

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

/* === Section Title === */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 16px;
}

/* === Actions Grid === */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 32px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border-color: #e5e7eb;
}

.action-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  border-radius: 10px;
  flex-shrink: 0;
  transition: all 0.2s;
}

.action-card:hover .action-icon {
  background: #fffbeb;
  transform: scale(1.05);
}

.action-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.action-desc {
  font-size: 12px;
  color: #9ca3af;
}

.action-arrow { flex-shrink: 0; }
.action-card:hover .action-arrow { stroke: #f59e0b; }

/* === Recent Orders === */
.recent-section {
  margin-top: 8px;
}

.recent-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f9fafb;
}

.recent-item:last-child { border-bottom: none; }
.recent-item:hover { background: #f9fafb; }

.recent-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.recent-no {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.recent-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.status-pending { background: #fef3c7; color: #d97706; }
.status-paid { background: #d1fae5; color: #059669; }
.status-shipped { background: #dbeafe; color: #2563eb; }
.status-done { background: #e0e7ff; color: #4f46e5; }
.status-cancel { background: #f3f4f6; color: #6b7280; }

.recent-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.recent-amount {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

/* Responsive */
@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .actions-grid { grid-template-columns: 1fr; }
}

@media (max-width: 480px) {
  .profile-header { flex-direction: column; text-align: center; }
  .profile-badges { justify-content: center; }
}
</style>
