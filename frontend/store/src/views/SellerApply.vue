<template>
  <div class="seller-apply">
    <div style="max-width: 900px; margin: 0 auto; padding: 48px 24px;">
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/account' }">{{ $t('account.myAccount') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('account.openStore') }}</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="apply-card" style="text-align: center; background: #fff; padding: 48px; border-radius: 12px; border: 1px solid #e5e7eb; max-width: 400px;">
        <el-icon :size="48" color="#3b82f6"><Shop /></el-icon>
        <h2>{{ $t('account.openStoreTitle') }}</h2>
        <p>{{ $t('account.openStoreDesc') }}</p>
        <el-button type="primary" size="large" @click="apply">{{ $t('account.applyNow') }}</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { Shop } from '@element-plus/icons-vue'

function apply() {
  ElMessage.info('Seller application feature coming soon')
}
</script>

<style scoped>
.seller-apply { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
</style>
