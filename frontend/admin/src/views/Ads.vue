<template>
  <div class="ads-page">
    <div class="page-header">
      <h2>{{ $t('ads.title') }}</h2>
      <el-button type="primary" @click="openCreate">{{ $t('ads.addAd') }}</el-button>
    </div>

    <el-table :data="ads" border stripe v-loading="loading" style="width:100%">
      <el-table-column label="" width="80" align="center">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width:50px;height:50px" fit="cover" />
          <span v-else style="color:#999">—</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('ads.title_')" prop="title" min-width="140" />
      <el-table-column :label="$t('ads.sortOrder')" prop="sortOrder" width="100" align="center" />
      <el-table-column :label="$t('ads.isActive')" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
            {{ row.isActive ? $t('common.yes') : $t('common.no') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('ads.startDate')" prop="startDate" width="120" />
      <el-table-column :label="$t('ads.endDate')" prop="endDate" width="120" />
      <el-table-column :label="$t('products.actions')" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">{{ $t('common.submit') }} edit</el-button>
          <el-button size="small" :type="row.isActive ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.isActive ? $t('products.deactivate') : $t('products.activate') }}
          </el-button>
          <el-popconfirm :title="$t('ads.deleteConfirm')" @confirm="remove(row)">
            <template #reference>
              <el-button size="small" type="danger">{{ $t('common.cancel') }} del</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? $t('ads.editAd') : $t('ads.addAd')" width="600px">
      <el-form :model="form" label-position="top" v-if="dialogVisible">
        <el-form-item :label="$t('ads.title_')">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item :label="$t('ads.imageUrl')">
          <el-input v-model="form.imageUrl" placeholder="https://..." />
          <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:120px;height:80px;margin-top:8px" fit="cover" />
        </el-form-item>
        <el-form-item :label="$t('ads.linkUrl')">
          <el-input v-model="form.linkUrl" placeholder="https://..." />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item :label="$t('ads.sortOrder')">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('ads.isActive')">
              <el-switch v-model="form.isActive" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('ads.startDate')">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('ads.endDate')">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ $t('common.submit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const ads = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ title: '', imageUrl: '', linkUrl: '', sortOrder: 0, isActive: true, startDate: null, endDate: null })

async function fetch() {
  loading.value = true
  try {
    const res = await api.get('/admin/ads')
    ads.value = res.data || []
  } catch (e) {}
  loading.value = false
}

function openCreate() {
  editing.value = false
  form.value = { title: '', imageUrl: '', linkUrl: '', sortOrder: 0, isActive: true, startDate: null, endDate: null }
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  try {
    if (editing.value) {
      await api.put(`/admin/ads/${form.value.id}`, form.value)
    } else {
      await api.post('/admin/ads', form.value)
    }
    dialogVisible.value = false
    await fetch()
  } catch (e) {}
}

async function toggleStatus(row) {
  try {
    await api.put(`/admin/ads/${row.id}/toggle-status`)
    await fetch()
  } catch (e) {}
}

async function remove(row) {
  try {
    await api.delete(`/admin/ads/${row.id}`)
    await fetch()
  } catch (e) {}
}

onMounted(fetch)
</script>
