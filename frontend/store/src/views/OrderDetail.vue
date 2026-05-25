<template>
  <div class="order-detail" v-loading="loading">
    <div class="detail-container" style="max-width: 900px; margin: 0 auto; padding: 24px;">
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">{{ $t('account.orders') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('order.orderNo') }}{{ order?.orderNo ? ': ' + order.orderNo : '' }}</el-breadcrumb-item>
      </el-breadcrumb>
      <h2>Order Detail</h2>
      <el-card v-if="order">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Order No">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="order.status === 'COMPLETED' ? 'success' : 'warning'">{{ order.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Total">${{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="Created">{{ order.createdAt }}</el-descriptions-item>
      </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const order = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get(`/orders/${route.params.id}`)
    order.value = res.data
  } catch (e) {}
  loading.value = false
})
</script>
