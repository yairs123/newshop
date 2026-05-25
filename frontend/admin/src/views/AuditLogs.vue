<template>
  <div class="audit-page">
    <h2 style="margin-bottom:16px">📋 操作日志 / Audit Logs</h2>

    <!-- Filters -->
    <el-card style="margin-bottom:16px">
      <el-form :inline="true" :model="filters" label-width="80px">
        <el-form-item label="Entity">
          <el-select v-model="filters.entityType" clearable placeholder="All" style="width:140px">
            <el-option label="PRODUCT" value="PRODUCT" />
            <el-option label="USER" value="USER" />
            <el-option label="ORDER" value="ORDER" />
            <el-option label="REIMBURSEMENT" value="REIMBURSEMENT" />
            <el-option label="INVENTORY_BATCH" value="INVENTORY_BATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="Operation">
          <el-select v-model="filters.operation" clearable placeholder="All" style="width:140px">
            <el-option label="CREATE" value="CREATE" />
            <el-option label="UPDATE" value="UPDATE" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="STATUS_CHANGE" value="STATUS_CHANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="From">
          <el-date-picker v-model="filters.dateFrom" type="date" style="width:140px" />
        </el-form-item>
        <el-form-item label="To">
          <el-date-picker v-model="filters.dateTo" type="date" style="width:140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">Search</el-button>
          <el-button @click="resetFilters">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="logs" stripe v-loading="loading" @expand-change="onExpandChange" style="margin-top:8px">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div v-if="row.expanded" class="log-detail">
            <p><strong>Field:</strong> {{ row.fieldName || '-' }}</p>
            <p><strong>Old Value:</strong> <code>{{ row.oldValue || '-' }}</code></p>
            <p><strong>New Value:</strong> <code>{{ row.newValue || '-' }}</code></p>
            <p><strong>Summary:</strong> {{ row.changeSummary || '-' }}</p>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="160" />
      <el-table-column prop="entityType" label="Entity" width="130" />
      <el-table-column prop="entityId" label="Entity ID" width="90" align="center" />
      <el-table-column prop="operation" label="Operation" width="120">
        <template #default="{ row }">
          <el-tag :type="row.operation === 'CREATE' ? 'success' : row.operation === 'DELETE' ? 'danger' : 'warning'" size="small">
            {{ row.operation }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="changeSummary" label="Summary" min-width="300">
        <template #default="{ row }">
          <span class="summary-text">{{ row.changeSummary }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="Operator" width="140" />
    </el-table>

    <el-empty v-if="!loading && logs.length === 0" description="暂无日志" />
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="total, prev, pager, next"
      @current-change="loadLogs"
      style="margin-top:20px; justify-content:center"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'

const logs = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 30

const filters = reactive({
  entityType: '',
  operation: '',
  dateFrom: '',
  dateTo: '',
})

onMounted(() => loadLogs())

async function loadLogs() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size }
    if (filters.entityType) params.entityType = filters.entityType
    if (filters.operation) params.operation = filters.operation
    if (filters.dateFrom) params.dateFrom = formatDate(filters.dateFrom)
    if (filters.dateTo) params.dateTo = formatDate(filters.dateTo)
    const res = await api.get('/admin/audit-logs', { params })
    logs.value = (res.data?.content || []).map(l => ({ ...l, expanded: false }))
    total.value = res.data?.totalElements || 0
  } catch (_) {
    logs.value = []
  } finally { loading.value = false }
}

function onExpandChange(row) {
  row.expanded = !row.expanded
}

function search() {
  page.value = 1
  loadLogs()
}

function resetFilters() {
  filters.entityType = ''
  filters.operation = ''
  filters.dateFrom = ''
  filters.dateTo = ''
  page.value = 1
  loadLogs()
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}
</script>

<style scoped>
.audit-page { padding: 4px; }
.log-detail { padding: 8px 16px; font-size: 13px; }
.log-detail p { margin: 4px 0; }
.log-detail code { background: #f3f4f6; padding: 1px 6px; border-radius: 3px; }
.summary-text { font-size: 13px; color: #374151; }
</style>
