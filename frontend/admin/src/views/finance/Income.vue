<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>📥 收款明细 / Income</h2>
      <el-button @click="printPage('收款明细')">🖨️ 打印</el-button>
    </div>
    <el-card style="margin-bottom:16px">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="~"
        value-format="YYYY-MM-DD" @change="loadData" />
    </el-card>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="8"><StatCard label="总收入" :value="fmt(summary.totalRevenue)" color="#10b981" /></el-col>
      <el-col :span="8"><StatCard label="订单数" :value="summary.orderCount" color="#3b82f6" /></el-col>
      <el-col :span="8"><StatCard label="平均订单金额" :value="fmt(summary.averageOrderValue)" color="#8b5cf6" /></el-col>
    </el-row>
    <el-table :data="monthlyData" stripe v-loading="loading">
      <el-table-column prop="month" label="月份" width="160" />
      <el-table-column label="收入" align="right"><template #default="{ row }">${{ fmt(row.revenue) }}</template></el-table-column>
      <el-table-column prop="orderCount" label="订单数" align="center" />
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
const summary = reactive({ totalRevenue: '0.00', orderCount: 0, averageOrderValue: '0.00' })

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
    const res = await api.get('/admin/finance/sales-revenue', { params: { startDate: s, endDate: e } })
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
