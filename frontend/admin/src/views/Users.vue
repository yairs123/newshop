<template>
  <div>
    <h3>{{ $t('users.title') }}</h3>
    <el-table :data="users" stripe style="margin-top:20px" v-loading="loading">
      <el-table-column prop="id" :label="$t('users.id')" width="80" />
      <el-table-column prop="username" :label="$t('users.username')" width="140" />
      <el-table-column prop="email" :label="$t('users.email')" min-width="200" />
      <el-table-column :label="$t('users.enabled')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'danger'">{{ row.enabled ? $t('common.yes') : $t('common.no') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('users.roles')" width="200">
        <template #default="{ row }">
          <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('users.toggleStatus')" width="140">
        <template #default="{ row }">
          <el-button size="small" :type="row.enabled ? 'danger' : 'success'" @click="toggle(row)">{{ $t('users.toggleStatus') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      layout="prev, pager, next"
      @current-change="load"
      style="margin-top:20px; justify-content:center"
    />
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
