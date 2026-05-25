<template>
  <div v-loading="loading">
    <h3>{{ $t('dashboard.title') }}</h3>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-statistic :title="$t('dashboard.totalProducts')" :value="stats.productCount" />
      </el-col>
      <el-col :span="6">
        <el-statistic :title="$t('dashboard.totalOrders')" :value="stats.orderCount" />
      </el-col>
      <el-col :span="6">
        <el-statistic :title="$t('dashboard.totalUsers')" :value="stats.userCount" />
      </el-col>
      <el-col :span="6">
        <el-statistic :title="$t('dashboard.pendingSellers')" :value="stats.pendingSellerCount" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const stats = ref({ productCount: 0, orderCount: 0, userCount: 0, pendingSellerCount: 0 })
const loading = ref(true)

onMounted(async () => {
  try {
    const [products, orders, users, sellers] = await Promise.all([
      api.get('/admin/products?size=1'),
      api.get('/admin/orders?size=1'),
      api.get('/admin/users?size=1'),
      api.get('/admin/sellers/applications?status=PENDING')
    ])
    stats.value = {
      productCount: products.data.totalElements,
      orderCount: orders.data.totalElements,
      userCount: users.data.totalElements,
      pendingSellerCount: Array.isArray(sellers.data) ? sellers.data.length : 0
    }
  } catch (e) {
    // ignore
  }
  loading.value = false
})
</script>
