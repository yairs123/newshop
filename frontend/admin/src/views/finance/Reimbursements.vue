<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>📋 报销管理 / Reimbursements</h2>
      <el-button @click="printPage('报销管理')">🖨️ 打印</el-button>
    </div>
    <el-card style="margin-bottom:16px">
      <el-radio-group v-model="statusFilter" @change="loadData">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="PENDING">待审核</el-radio-button>
        <el-radio-button value="APPROVED">已批准</el-radio-button>
        <el-radio-button value="PAID">已支付</el-radio-button>
        <el-radio-button value="REJECTED">已驳回</el-radio-button>
      </el-radio-group>
    </el-card>
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="createdAt" label="日期" width="160">
        <template #default="{ row }">{{ row.createdAt?.substring(0, 10) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="金额" width="140" align="right">
        <template #default="{ row }">${{ fmt(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="approve(row.id)">通过</el-button>
          <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" @click="pay(row.id)">支付</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row.id)">🗑️</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { printPage } from '../../utils/print'

const list = ref([])
const loading = ref(false)
const statusFilter = ref('')

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const params = { page: 0, size: 50 }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await api.get('/admin/finance/reimbursements', { params })
    list.value = res.data?.content || []
  } catch (_) { list.value = [] }
  finally { loading.value = false }
}

async function approve(id) {
  try { await api.post(`/admin/finance/reimbursements/${id}/approve`); ElMessage.success('已通过'); loadData() }
  catch (e) { ElMessage.error('操作失败') }
}

async function pay(id) {
  try { await api.post(`/admin/finance/reimbursements/${id}/pay`); ElMessage.success('已支付'); loadData() }
  catch (e) { ElMessage.error('操作失败') }
}

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定删除？'); await api.delete(`/admin/finance/reimbursements/${id}`); ElMessage.success('已删除'); loadData() }
  catch (_) {}
}

function statusType(s) {
  return s === 'PAID' ? 'success' : s === 'APPROVED' ? 'primary' : s === 'REJECTED' ? 'danger' : 'warning'
}

function statusLabel(s) {
  const labels = { PENDING: '待审核', APPROVED: '已批准', PAID: '已支付', REJECTED: '已驳回' }
  return labels[s] || s
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.finance-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
