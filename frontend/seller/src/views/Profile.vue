<template>
  <div class="profile-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>Profile</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card>
      <template #header><h2>卖家资料</h2></template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="店铺名称">{{ profile.shopName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="店铺描述">{{ profile.shopDescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ profile.status || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资料锁定">{{ profile.locked ? '已锁定' : '未锁定' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const profile = ref({})

onMounted(async () => {
  try {
    const res = await api.get('/seller/status')
    profile.value = res.data || {}
  } catch (e) {}
})
</script>
