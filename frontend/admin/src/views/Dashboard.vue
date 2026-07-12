<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.key">
        <el-card class="stat-card" shadow="hover" @click="$router.push(card.link)">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ card.value }}</span>
              <span class="stat-label">{{ card.label }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="action-card" shadow="hover">
      <template #header>
        <span style="font-weight:600;font-size:16px">{{ $t('dashboard.quickActions', '快捷操作') }}</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/products')">{{ $t('admin.menuSub.productList') }}</el-button>
        <el-button type="success" @click="$router.push('/orders')">{{ $t('admin.menuSub.orderList') }}</el-button>
        <el-button type="warning" @click="$router.push('/inventory')">{{ $t('admin.menuSub.inventory') }}</el-button>
        <el-button @click="$router.push('/users')">{{ $t('admin.menuSub.buyerMgmt') }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { Goods, List, UserFilled, Avatar } from '@element-plus/icons-vue'

const { t } = useI18n()
const loading = ref(true)
const stats = ref({ productCount: 0, orderCount: 0, userCount: 0, pendingSellerCount: 0 })

const statCards = computed(() => [
  { key: 'products', icon: Goods, label: t('dashboard.totalProducts', '商品总数'), value: stats.value.productCount, link: '/products', bg: 'linear-gradient(135deg, #409eff, #337ecc)' },
  { key: 'orders', icon: List, label: t('dashboard.totalOrders', '订单总数'), value: stats.value.orderCount, link: '/orders', bg: 'linear-gradient(135deg, #67c23a, #529b2e)' },
  { key: 'users', icon: UserFilled, label: t('dashboard.totalUsers', '用户总数'), value: stats.value.userCount, link: '/users', bg: 'linear-gradient(135deg, #e6a23c, #cf9236)' },
  { key: 'sellers', icon: Avatar, label: t('dashboard.pendingSellers', '待审卖家'), value: stats.value.pendingSellerCount, link: '/sellers', bg: 'linear-gradient(135deg, #f56c6c, #d9534f)' },
])

onMounted(async () => {
  try {
    const [products, orders, users, sellers] = await Promise.all([
      api.get('/admin/products?size=1'),
      api.get('/admin/orders?size=1'),
      api.get('/admin/users?size=1'),
      api.get('/admin/sellers/applications?status=PENDING')
    ])
    stats.value = {
      productCount: products.data?.totalElements || 0,
      orderCount: orders.data?.totalElements || 0,
      userCount: users.data?.totalElements || 0,
      pendingSellerCount: Array.isArray(sellers.data) ? sellers.data.length : 0
    }
  } catch (e) { /* ignore */ }
  loading.value = false
})
</script>

<style scoped>
.dashboard { max-width: 1200px; }
.stat-row { margin-bottom: 24px; }
.stat-card { cursor: pointer; border-radius: 12px; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-4px); }
.stat-inner { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.action-card { border-radius: 12px; margin-bottom: 24px; }
.quick-actions { display: flex; gap: 12px; flex-wrap: wrap; }
</style>
