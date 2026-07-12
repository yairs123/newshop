<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>📈 利润报表 / Profit Report</h2>
      <el-button @click="printPage('利润报表')">🖨️ 打印</el-button>
    </div>
    <el-card style="margin-bottom:16px">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="~"
        value-format="YYYY-MM-DD" @change="loadData" />
    </el-card>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6"><StatCard label="总收入" :value="fmt(summary.totalRevenue)" color="#10b981" /></el-col>
      <el-col :span="6"><StatCard label="总成本" :value="fmt(summary.totalCost)" color="#f59e0b" /></el-col>
      <el-col :span="6"><StatCard label="净利润" :value="fmt(summary.netProfit)" :color="Number(summary.netProfit) >= 0 ? '#10b981' : '#ef4444'" /></el-col>
      <el-col :span="6"><StatCard label="利润率" :value="summary.profitMargin + '%'" color="#3b82f6" /></el-col>
    </el-row>
    <el-table :data="monthlyData" stripe v-loading="loading">
      <el-table-column prop="month" label="月份" width="120" />
      <el-table-column label="收入" align="right"><template #default="{ row }">${{ fmt(row.revenue) }}</template></el-table-column>
      <el-table-column label="成本" align="right"><template #default="{ row }">${{ fmt(row.cost) }}</template></el-table-column>
      <el-table-column label="利润" align="right"><template #default="{ row }">${{ fmt(row.profit) }}</template></el-table-column>
      <el-table-column label="利润率" align="right"><template #default="{ row }">{{ row.margin }}%</template></el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../../api'
import { printPage } from '../../utils/print'
import StatCard from '../../components/finance/StatCard.vue'

const loading = ref(false)
const dateRange = ref([])
const monthlyData = ref([])
const summary = reactive({ totalRevenue: '0.00', totalCost: '0.00', netProfit: '0.00', profitMargin: '0.00' })

onMounted(() => {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  dateRange.value = [start.toISOString().split('T')[0], now.toISOString().split('T')[0]]
  loadData()
})

async function loadData() {
  if (!dateRange.value?.length) return
  loading.value = true
  try {
    const [s, e] = dateRange.value
    const res = await api.get('/admin/finance/profit-report', { params: { startDate: s, endDate: e } })
    Object.assign(summary, res.data)
    monthlyData.value = res.data?.monthlyBreakdown || []
  } catch (_) { monthlyData.value = [] }
  finally { loading.value = false }
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.finance-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
