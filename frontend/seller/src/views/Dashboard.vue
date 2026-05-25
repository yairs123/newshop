<template>
  <div class="dashboard" v-loading="loading">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>Dashboard</el-breadcrumb-item>
    </el-breadcrumb>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-label">商品总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-value">{{ stats.pendingOrders }}</div>
          <div class="stat-label">待处理订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-value">${{ formatPrice(stats.monthlySales) }}</div>
          <div class="stat-label">本月销售额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-value">{{ stats.averageRating }}</div>
          <div class="stat-label">评价</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const loading = ref(true)
const stats = ref({ productCount: 0, pendingOrders: 0, monthlySales: 0, averageRating: 0 })

onMounted(async () => {
  try {
    const res = await api.get('/seller/dashboard')
    stats.value = res.data || stats.value
  } catch (e) { /* ignore */ }
  loading.value = false
})

function formatPrice(p) { return Number(p).toLocaleString() }
</script>

<style>
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }
</style>
