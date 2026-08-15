<template>
  <div class="messages-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/account' }">{{ $t('account.myAccount') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ $t('account.messages') }}</el-breadcrumb-item>
    </el-breadcrumb>
    <h2>{{ $t('account.messageCenter') }}</h2>
    <div class="message-tabs">
      <el-radio-group v-model="filter" style="margin-bottom:16px">
        <el-radio-button value="">{{ $t('common.all') }}</el-radio-button>
        <el-radio-button value="BUYER">{{ $t('account.buyerMessages') }}</el-radio-button>
        <el-radio-button value="SELLER">{{ $t('account.sellerMessages') }}</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading">
      <el-empty v-if="tickets.length === 0" :description="$t('account.noMessages')" />
      <el-table v-else :data="tickets" border stripe>
        <el-table-column prop="subject" :label="$t('account.subject')" min-width="200" />
        <el-table-column prop="message" :label="$t('account.describeIssue')" min-width="300" show-overflow-tooltip />
        <el-table-column :label="$t('account.ticketType')" width="120">
          <template #default="{ row }">{{ ticketTypeLabel(row.ticketType) }}</template>
        </el-table-column>
        <el-table-column :label="$t('account.status')" width="100">
          <template #default="{ row }">{{ ticketStatusLabel(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('account.createdAt')" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'

const { t } = useI18n()
const tickets = ref([])
const loading = ref(true)
const filter = ref('')

onMounted(fetchTickets)
watch(filter, fetchTickets)

async function fetchTickets() {
  loading.value = true
  try {
    const params = filter.value ? { type: filter.value } : {}
    const res = await api.get('/messages', { params })
    tickets.value = res.data || []
  } catch (e) { tickets.value = [] }
  loading.value = false
}

function ticketTypeLabel(type) {
  if (!type) return '-'
  const key = `ticketType.${type}`
  const translated = t(key)
  return translated !== key ? translated : type
}

function ticketStatusLabel(status) {
  if (!status) return '-'
  const key = `ticketStatus.${status}`
  const translated = t(key)
  return translated !== key ? translated : status
}
</script>

<style scoped>
.messages-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
h2 { margin-bottom: 16px; }
.empty { text-align: center; padding: 60px; color: #9ca3af; }
</style>
