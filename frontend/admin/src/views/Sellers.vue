<template>
  <div>
    <h3>{{ $t('sellers.applications') }}</h3>
    <el-table :data="applications" stripe style="margin-top:20px" v-loading="loading">
      <el-table-column prop="id" :label="$t('sellers.id')" width="80" />
      <el-table-column prop="userId" label="User ID" width="80" />
      <el-table-column prop="shopName" :label="$t('sellers.shopName')" min-width="180" />
      <el-table-column :label="$t('sellers.status')" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('sellers.actions')" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="approve(row)">{{ $t('sellers.approve') }}</el-button>
            <el-button size="small" type="danger" @click="rejectDialog(row)">{{ $t('sellers.reject') }}</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <h3 style="margin-top:30px">{{ $t('sellers.profiles') }}</h3>
    <el-table :data="profiles" stripe style="margin-top:20px">
      <el-table-column prop="id" :label="$t('sellers.id')" width="80" />
      <el-table-column prop="userId" label="User ID" width="80" />
      <el-table-column prop="shopName" :label="$t('sellers.shopName')" min-width="180" />
      <el-table-column prop="status" :label="$t('sellers.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.locked ? 'danger' : 'success'">{{ row.locked ? 'LOCKED' : 'ACTIVE' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const applications = ref([])
const profiles = ref([])
const loading = ref(false)

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
