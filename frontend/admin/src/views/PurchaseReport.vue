<template>
  <div v-loading="loading" class="report-page">
    <h2 style="margin-bottom:16px">{{ $t('financeReports.purchaseReport.title') }}</h2>

    <el-card style="margin-bottom:16px">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="~"
        :start-placeholder="$t('common.startDate')"
        :end-placeholder="$t('common.endDate')"
        value-format="YYYY-MM-DD"
        @change="onDateChange"
      />
    </el-card>

    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.purchaseReport.totalCost') }}</div>
            <div class="stat-value" style="color:#f59e0b">${{ summary.totalPurchaseCost }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.purchaseReport.batchCount') }}</div>
            <div class="stat-value" style="color:#3b82f6">{{ summary.batchCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.purchaseReport.avgCostPerBatch') }}</div>
            <div class="stat-value" style="color:#8b5cf6">${{ summary.averageCostPerBatch }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="monthlyData" stripe>
      <el-table-column prop="month" :label="$t('financeReports.purchaseReport.month')" width="160" />
      <el-table-column :label="$t('financeReports.purchaseReport.cost')" align="right">
        <template #default="{ row }">${{ formatNum(row.totalCost) }}</template>
      </el-table-column>
      <el-table-column prop="batchCount" :label="$t('financeReports.purchaseReport.batchCount')" align="center" />
    </el-table>
    <el-empty v-if="!loading && monthlyData.length === 0" :description="$t('common.noData')" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'

const loading = ref(false)
const dateRange = ref([])
const monthlyData = ref([])
const summary = reactive({ totalPurchaseCost: '0.00', batchCount: 0, averageCostPerBatch: '0.00' })

function getDefaultRange() {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  return [
    start.toISOString().split('T')[0],
    now.toISOString().split('T')[0]
  ]
}

onMounted(() => {
  dateRange.value = getDefaultRange()
  loadData()
})

function onDateChange() {
  loadData()
}

async function loadData() {
  if (!dateRange.value || dateRange.value.length < 2) return
  loading.value = true
  try {
    const [startDate, endDate] = dateRange.value
    const res = await api.get('/admin/finance/purchase-report', { params: { startDate, endDate } })
    const data = res.data
    summary.totalPurchaseCost = data.totalPurchaseCost ?? '0.00'
    summary.batchCount = data.batchCount ?? 0
    summary.averageCostPerBatch = data.averageCostPerBatch ?? '0.00'
    monthlyData.value = data.monthlyBreakdown || []
  } catch (_) {
    monthlyData.value = []
  } finally { loading.value = false }
}

function formatNum(n) {
  return Number(n || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
</script>

<style scoped>
.report-page { padding: 4px; }
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #6b7280; margin-bottom: 4px; }
.stat-value { font-size: 28px; font-weight: 700; }
</style>
