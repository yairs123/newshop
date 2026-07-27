<template>
  <div class="overview-page">
    <div class="page-header">
      <h2>📊 财务总览 / Finance Overview</h2>
      <el-button @click="printPage('财务总览')">🖨️ 打印</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6"><StatCard label="总收入 / Total Income" :value="fmt(data.totalIncome)" color="#10b981" /></el-col>
      <el-col :span="6"><StatCard label="总支出 / Total Expenses" :value="fmt(data.totalExpenses)" color="#f59e0b" /></el-col>
      <el-col :span="6"><StatCard label="净利润 / Net Profit" :value="fmt(data.netProfit)" :color="profitColor" /></el-col>
      <el-col :span="6"><StatCard label="银行总余额 / Bank Balance" :value="fmt(data.totalBankBalance)" color="#3b82f6" /></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>📈 月度趋势 / Monthly Trend</template>
          <div ref="trendChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>🥧 费用构成 / Expense Breakdown</template>
          <div ref="pieChartRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:16px">
      <template #header>🏦 银行余额分布 / Bank Balances</template>
      <el-row :gutter="16">
        <el-col :span="6" v-for="b in data.bankBreakdown || []" :key="b.bankName">
          <div class="bank-mini-card">
            <div class="bank-mini-name">{{ b.bankName }}</div>
            <div class="bank-mini-balance" :style="{ color: Number(b.balance) > 0 ? '#10b981' : '#ef4444' }">
              {{ b.currency === 'MYR' ? 'RM' : '$' }}{{ Number(b.balance).toLocaleString('en-US', { minimumFractionDigits: 2 }) }}
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount, computed } from 'vue'
import { api } from '../../api'
import { printPage } from '../../utils/print'
import StatCard from '../../components/finance/StatCard.vue'
import * as echarts from 'echarts'

const data = reactive({
  totalIncome: 0, totalExpenses: 0, netProfit: 0,
  profitMargin: 0, totalBankBalance: 0,
  monthlyTrend: [], expenseBreakdown: [], bankBreakdown: [],
})

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const profitColor = computed(() => Number(data.netProfit) >= 0 ? '#10b981' : '#ef4444')

function fmt(v) { return `$${Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}` }

onMounted(async () => {
  try {
    const res = await api.get('/admin/finance/overview')
    if (res.data) Object.assign(data, res.data)
  } catch (_) {}
  await nextTick()
  initCharts()
})

function initCharts() {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis', formatter: (p) => {
        let html = `<b>${p[0].axisValue}</b><br/>`
        p.forEach(i => html += `${i.marker} ${i.seriesName}: <b>$${Number(i.value).toLocaleString()}</b><br/>`)
        return html
      }},
      legend: { data: ['收入', '支出', '利润'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: data.monthlyTrend.map(m => m.month) },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (v) => {
            if (Math.abs(v) >= 1000000) return '$' + (v/1000000).toFixed(1) + 'M'
            if (Math.abs(v) >= 1000) return '$' + (v/1000).toFixed(1) + 'K'
            return '$' + v
          }
        }
      },
      series: [
        { name: '收入', type: 'bar', data: data.monthlyTrend.map(m => m.income), itemStyle: { color: '#10b981', borderRadius: [4,4,0,0] } },
        { name: '支出', type: 'bar', data: data.monthlyTrend.map(m => m.expenses), itemStyle: { color: '#f59e0b', borderRadius: [4,4,0,0] } },
        { name: '利润', type: 'line', data: data.monthlyTrend.map(m => Number(m.profit)), itemStyle: { color: '#3b82f6' }, lineStyle: { width: 3 }, symbol: 'circle', symbolSize: 8, areaStyle: { color: 'rgba(59,130,246,0.08)' } },
      ],
    })
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: ${c} ({d}%)' },
      series: [{
        type: 'pie', radius: ['30%', '70%'],
        data: data.expenseBreakdown.map(e => ({ name: e.category, value: Number(e.amount) })),
        label: { formatter: '{b}\n{d}%' },
        colors: ['#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4'],
      }],
    })
  }
}

onBeforeUnmount(() => {
  trendChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.overview-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.bank-mini-card {
  background: #f9fafb; border-radius: 8px; padding: 16px; text-align: center;
}
.bank-mini-name { font-size: 14px; font-weight: 600; margin-bottom: 6px; }
.bank-mini-balance { font-size: 22px; font-weight: 700; }
@media print {
  .page-header el-button { display: none; }
}
</style>
