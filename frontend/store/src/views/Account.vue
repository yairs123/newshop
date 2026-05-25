<template>
  <div class="account-page">
    <div class="page-inner">
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('account.myAccount') }}</el-breadcrumb-item>
      </el-breadcrumb>
      <h1 class="page-title">{{ $t('account.myAccount') }}</h1>
      <p class="page-subtitle">{{ $t('account.orders') }}</p>

      <div class="account-grid">
        <div v-for="item in menuItems" :key="item.route" class="account-card" @click="$router.push(item.route)">
          <div class="card-icon" :style="{ background: item.bg }">
            <el-icon :size="32" color="#fff">{{ item.icon }}</el-icon>
          </div>
          <div class="card-body">
            <h3>{{ $t(item.title) }}</h3>
            <p>{{ $t(item.desc) }}</p>
          </div>
          <el-icon class="card-arrow" color="#9ca3af"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ShoppingBag, Location, CreditCard, Message, Shop, ChatDotSquare, ArrowRight } from '@element-plus/icons-vue'

const menuItems = [
  { icon: ShoppingBag, title: 'account.orders', desc: 'account.myOrders', route: '/orders', bg: '#3b82f6' },
  { icon: Location, title: 'account.addresses', desc: 'account.defaultAddress', route: '/addresses', bg: '#10b981' },
  { icon: CreditCard, title: 'account.paymentMethods', desc: 'account.defaultPayment', route: '/payment-methods', bg: '#8b5cf6' },
  { icon: Message, title: 'account.messages', desc: 'account.messageCenter', route: '/messages', bg: '#f59e0b' },
  { icon: Shop, title: 'account.openStore', desc: 'account.openStoreDesc', route: '/seller/apply', bg: '#ef4444' },
  { icon: ChatDotSquare, title: 'account.contactUs', desc: 'account.needHelp', route: '/contact', bg: '#06b6d4' },
]
</script>

<style scoped>
.account-page { background: #f5f5f7; min-height: 60vh; }
.page-inner { max-width: 900px; margin: 0 auto; padding: 48px 24px; }
.page-title { font-size: 28px; font-weight: 700; color: #111827; }
.page-subtitle { color: #6b7280; margin: 8px 0 32px; }

.account-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }

.account-card {
  display: flex; align-items: center; gap: 16px;
  background: #fff; border-radius: 14px; padding: 24px;
  cursor: pointer; transition: all 0.2s;
  border: 1px solid #f0f0f0;
}
.account-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: #e5e7eb;
}

.card-icon {
  width: 56px; height: 56px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.card-body { flex: 1; min-width: 0; }
.card-body h3 { font-size: 16px; font-weight: 600; color: #111827; margin-bottom: 4px; }
.card-body p { font-size: 13px; color: #6b7280; }
.card-arrow { flex-shrink: 0; }

@media (max-width: 600px) {
  .account-grid { grid-template-columns: 1fr; }
  .page-inner { padding: 24px 16px; }
}
</style>
