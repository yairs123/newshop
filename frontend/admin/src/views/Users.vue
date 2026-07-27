<template>
  <div class="users-page">
    <!-- Page Header -->
    <div class="page-head">
      <div>
        <h2>{{ $t('users.title') || '买家管理' }}</h2>
        <p class="page-desc">查看和管理平台注册用户</p>
      </div>
      <div class="user-count-badge">
        <span class="count-value">{{ total }}</span>
        <span class="count-label">总用户</span>
      </div>
    </div>

    <!-- Users Table -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </div>
            <h3>{{ $t('users.title') || '全部用户' }}</h3>
          </div>
        </div>
      </template>
      <el-table :data="users" stripe style="width:100%" v-loading="loading" :empty-text="'暂无用户数据'">
        <el-table-column prop="id" :label="$t('users.id')" width="80" />
        <el-table-column prop="username" :label="$t('users.username')" width="140" />
        <el-table-column prop="email" :label="$t('users.email')" min-width="220" />
        <el-table-column :label="$t('users.enabled')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" effect="plain" size="small">{{ row.enabled ? $t('common.yes') : $t('common.no') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('users.roles')" width="200">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" effect="plain" style="margin-right:4px; margin-bottom:2px">{{ r }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('users.toggleStatus')" width="140" align="center">
          <template #default="{ row }">
            <button class="tbl-btn" :class="row.enabled ? 'tbl-btn-warn' : 'tbl-btn-approve'" @click="toggle(row)">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path v-if="row.enabled" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle v-if="row.enabled" cx="12" cy="12" r="3"/>
                <g v-else><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="9" x2="15" y2="15"/><line x1="15" y1="9" x2="9" y2="15"/></g>
              </svg>
              {{ row.enabled ? $t('users.disable') || '禁用' : $t('users.enable') || '启用' }}
            </button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :total="total"
          :page-size="size"
          layout="total, prev, pager, next"
          background
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const page = ref(0)
const total = ref(0)
const size = 20

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await api.get(`/admin/users?page=${page.value}&size=${size}`)
    users.value = res.data.content
    total.value = res.data.totalElements
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

async function toggle(row) {
  try {
    await api.put(`/admin/users/${row.id}/toggle-status`)
    ElMessage.success('Updated')
    load()
  } catch (e) { /* ignore */ }
}
</script>

<style scoped>
.users-page { max-width: 1200px; }

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

/* User count badge */
.user-count-badge {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px;
  background: var(--surface, #fff);
  border: 1px solid var(--border, #e5e7eb);
  border-radius: var(--radius-sm, 6px);
}
.count-value { font-size: 20px; font-weight: 800; color: var(--ink, #0f172a); }
.count-label { font-size: 12px; color: var(--text-muted, #94a3b8); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 500; }

/* List card */
.list-card {
  border-radius: var(--radius, 10px);
  border: 1px solid var(--border, #e5e7eb);
}
.list-card :deep(.el-card__header) {
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
.card-header h3 {
  font-size: 15px; font-weight: 600; color: var(--ink, #0f172a); margin: 0;
}

/* Pagination */
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }

/* Table action buttons */
.tbl-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 10px; border-radius: 6px; border: 1px solid var(--border, #e5e7eb);
  font-size: 12px; font-weight: 500; font-family: inherit;
  background: var(--surface, #fff); color: var(--text, #1e293b);
  cursor: pointer; transition: all 0.15s;
}
.tbl-btn-approve:hover { border-color: var(--success, #059669); color: var(--success, #059669); }
.tbl-btn-warn:hover { border-color: var(--danger, #dc2626); color: var(--danger, #dc2626); }
</style>
