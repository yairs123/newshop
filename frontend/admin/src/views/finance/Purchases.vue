<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>📦 进货支出 / Purchases</h2>
      <el-button @click="printPage('进货支出')">🖨️ 打印</el-button>
    </div>
    <el-card style="margin-bottom:16px">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="~"
        value-format="YYYY-MM-DD" @change="loadData" />
    </el-card>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="8"><StatCard label="总采购成本" :value="fmt(summary.totalCost)" color="#f59e0b" /></el-col>
      <el-col :span="8"><StatCard label="批次数量" :value="summary.batchCount" color="#3b82f6" /></el-col>
      <el-col :span="8"><StatCard label="平均成本/批" :value="fmt(summary.averageCostPerBatch)" color="#8b5cf6" /></el-col>
    </el-row>
    <el-table :data="monthlyData" stripe v-loading="loading">
      <el-table-column prop="month" label="月份" width="160" />
      <el-table-column label="成本" align="right"><template #default="{ row }">${{ fmt(row.cost) }}</template></el-table-column>
      <el-table-column prop="batchCount" label="批次" align="center" />
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
const summary = reactive({ totalCost: '0.00', batchCount: 0, averageCostPerBatch: '0.00' })

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
    const res = await api.get('/admin/finance/purchase-report', { params: { startDate: s, endDate: e } })
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
