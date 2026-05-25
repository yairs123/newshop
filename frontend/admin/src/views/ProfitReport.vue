<template>
  <div v-loading="loading" class="report-page">
    <h2 style="margin-bottom:16px">{{ $t('financeReports.profitReport.title') }}</h2>

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
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.profitReport.totalRevenue') }}</div>
            <div class="stat-value" style="color:#10b981">${{ summary.totalRevenue }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.profitReport.totalCost') }}</div>
            <div class="stat-value" style="color:#f59e0b">${{ summary.totalCost }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.profitReport.netProfit') }}</div>
            <div class="stat-value" :style="{ color: netProfitColor }">${{ summary.netProfit }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">{{ $t('financeReports.profitReport.profitMargin') }}</div>
            <div class="stat-value" :style="{ color: netProfitColor }">{{ summary.profitMargin }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="monthlyData" stripe>
      <el-table-column prop="month" :label="$t('financeReports.profitReport.month')" width="120" />
      <el-table-column :label="$t('financeReports.profitReport.revenue')" align="right">
        <template #default="{ row }">${{ formatNum(row.revenue) }}</template>
      </el-table-column>
      <el-table-column :label="$t('financeReports.profitReport.cost')" align="right">
        <template #default="{ row }">${{ formatNum(row.cost) }}</template>
      </el-table-column>
      <el-table-column :label="$t('financeReports.profitReport.profit')" align="right">
        <template #default="{ row }">
          <span :style="{ color: Number(row.profit) >= 0 ? '#10b981' : '#ef4444' }">
            ${{ formatNum(row.profit) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('financeReports.profitReport.margin')" align="right">
        <template #default="{ row }">
          <span :style="{ color: Number(row.margin) >= 0 ? '#10b981' : '#ef4444' }">
            {{ row.margin }}
          </span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && monthlyData.length === 0" :description="$t('common.noData')" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { api } from '../api'

const loading = ref(false)
const dateRange = ref([])
const monthlyData = ref([])
const summary = reactive({ totalRevenue: '0.00', totalCost: '0.00', netProfit: '0.00', profitMargin: '0%' })

const netProfitColor = computed(() => {
  const v = Number(summary.netProfit)
  return v >= 0 ? '#10b981' : '#ef4444'
})

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
    const res = await api.get('/admin/finance/profit-report', { params: { startDate, endDate } })
    const data = res.data
    summary.totalRevenue = data.totalRevenue ?? '0.00'
    summary.totalCost = data.totalCost ?? '0.00'
    summary.netProfit = data.netProfit ?? '0.00'
    summary.profitMargin = data.profitMargin ?? '0%'
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
