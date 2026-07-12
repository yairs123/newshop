<template>
  <el-card shadow="hover" :class="{ 'bank-card': true, 'inactive': !account.isActive }">
    <div class="bank-header">
      <span class="bank-name">{{ account.bankName }}</span>
      <el-tag size="small" :type="account.isActive ? 'success' : 'info'">
        {{ account.currency }}
      </el-tag>
    </div>
    <div class="bank-balance" :style="{ color: balanceColor }">
      {{ formatMoney(account.currentBalance) }}
    </div>
    <div class="bank-meta">
      <span>{{ account.accountName }}</span>
      <span>****{{ account.accountNumber }}</span>
    </div>
    <div class="bank-actions">
      <el-button size="small" text @click="$emit('transfer', account)">📤 转账</el-button>
      <el-button size="small" text @click="$emit('adjust', account)">📥 调整</el-button>
      <el-button size="small" text @click="$emit('detail', account)">📋 流水</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  account: { type: Object, required: true },
})

defineEmits(['transfer', 'adjust', 'detail'])

const balanceColor = computed(() => {
  const v = Number(props.account.currentBalance)
  return v > 0 ? '#10b981' : v < 0 ? '#ef4444' : '#6b7280'
})

function formatMoney(v) {
  const num = Number(v || 0)
  return props.account.currency === 'MYR'
    ? `RM ${num.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
    : `$ ${num.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
}
</script>

<style scoped>
.bank-card { margin-bottom: 12px; }
.bank-card.inactive { opacity: 0.6; }
.bank-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.bank-name { font-size: 16px; font-weight: 600; }
.bank-balance { font-size: 26px; font-weight: 700; margin-bottom: 6px; }
.bank-meta { font-size: 12px; color: #9ca3af; display: flex; justify-content: space-between; margin-bottom: 10px; }
.bank-actions { display: flex; gap: 4px; }
</style>
