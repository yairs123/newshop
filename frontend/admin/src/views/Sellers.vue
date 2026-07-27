<template>
  <div class="sellers-page">
    <!-- Page Header -->
    <div class="page-head">
      <div>
        <h2>{{ $t('sellers.title') || '卖家管理' }}</h2>
        <p class="page-desc">管理卖家入驻申请与卖家资料</p>
      </div>
    </div>

    <!-- Applications Section -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg>
            </div>
            <h3>{{ $t('sellers.applications') }}</h3>
          </div>
          <el-tag v-if="pendingCount > 0" type="warning" effect="plain" size="small">{{ pendingCount }} 待审核</el-tag>
        </div>
      </template>
      <el-table :data="applications" stripe style="width:100%" v-loading="loading">
        <el-table-column prop="id" :label="$t('sellers.id')" width="80" />
        <el-table-column prop="userId" label="User ID" width="80" />
        <el-table-column prop="shopName" :label="$t('sellers.shopName')" min-width="200" />
        <el-table-column :label="$t('sellers.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('sellers.actions')" width="220" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <div class="action-btns">
                <button class="tbl-btn tbl-btn-approve" @click="approve(row)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                  {{ $t('sellers.approve') }}
                </button>
                <button class="tbl-btn tbl-btn-reject" @click="rejectDialog(row)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                  {{ $t('sellers.reject') }}
                </button>
              </div>
            </template>
            <span v-else class="status-text">--</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Profiles Section -->
    <el-card shadow="never" class="section-card" style="margin-top:20px">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon card-icon-profile">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </div>
            <h3>{{ $t('sellers.profiles') }}</h3>
          </div>
        </div>
      </template>
      <el-table :data="profiles" stripe style="width:100%">
        <el-table-column prop="id" :label="$t('sellers.id')" width="80" />
        <el-table-column prop="userId" label="User ID" width="80" />
        <el-table-column prop="shopName" :label="$t('sellers.shopName')" min-width="200" />
        <el-table-column prop="status" :label="$t('sellers.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.locked ? 'danger' : 'success'" effect="plain" size="small">{{ row.locked ? 'LOCKED' : 'ACTIVE' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const applications = ref([])
const profiles = ref([])
const loading = ref(false)

const pendingCount = computed(() => applications.value.filter(a => a.status === 'PENDING').length)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [appRes, profileRes] = await Promise.all([
      api.get('/admin/sellers/applications'),
      api.get('/admin/sellers/profiles')
    ])
    applications.value = appRes.data
    profiles.value = profileRes.data
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

async function approve(row) {
  try {
    await api.post(`/admin/sellers/applications/${row.id}/approve?adminId=1`)
    ElMessage.success('Approved')
    load()
  } catch (e) { /* ignore */ }
}

async function rejectDialog(row) {
  try {
    const { value } = await ElMessageBox.prompt('Enter reject reason', 'Reject', { confirmButtonText: 'OK' })
    await api.post(`/admin/sellers/applications/${row.id}/reject?adminId=1&reason=${encodeURIComponent(value)}`)
    ElMessage.success('Rejected')
    load()
  } catch (e) { /* cancelled or error */ }
}
</script>

<style scoped>
.sellers-page { max-width: 1200px; }

/* Page head */
.page-head {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 20px;
}
.page-head h2 {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 24px; font-weight: 700; color: var(--ink, #0f172a); margin: 0;
}
.page-desc { font-size: 13px; color: var(--text-muted, #94a3b8); margin-top: 2px; }

/* Section cards */
.section-card {
  border-radius: var(--radius, 10px);
  border: 1px solid var(--border, #e5e7eb);
}
.section-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  background: var(--surface, #fff);
}
.card-header {
  display: flex; justify-content: space-between; align-items: center;
}
.card-header-left {
  display: flex; align-items: center; gap: 10px;
}
.card-icon {
  width: 32px; height: 32px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--gold-dark, #b8932a), var(--gold, #d4a843));
  color: #fff;
}
.card-icon-profile {
  background: linear-gradient(135deg, #6366f1, #818cf8);
}
.card-header h3 {
  font-size: 15px; font-weight: 600; color: var(--ink, #0f172a); margin: 0;
}

/* Table action buttons */
.action-btns { display: flex; gap: 6px; justify-content: center; }
.tbl-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 10px; border-radius: 6px; border: 1px solid var(--border, #e5e7eb);
  font-size: 12px; font-weight: 500; font-family: inherit;
  background: var(--surface, #fff); color: var(--text, #1e293b);
  cursor: pointer; transition: all 0.15s;
}
.tbl-btn-approve:hover { border-color: var(--success, #059669); color: var(--success, #059669); }
.tbl-btn-reject:hover { border-color: var(--danger, #dc2626); color: var(--danger, #dc2626); }

.status-text { color: var(--text-muted, #94a3b8); font-size: 12px; }
</style>
