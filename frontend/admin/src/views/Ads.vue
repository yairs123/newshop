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
          <div class="upload-row">
            <el-input v-model="form.imageUrl" placeholder="https://... or upload" />
            <el-upload
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :headers="uploadHeaders"
            >
              <el-button type="primary" :loading="uploading">
                {{ uploading ? '上传中...' : '上传' }}
              </el-button>
            </el-upload>
          </div>
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
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="disabledStartDate" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('ads.endDate')">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="disabledEndDate" />
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
import { ElMessage } from 'element-plus'
import { api } from '../api'

const ads = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ title: '', imageUrl: '', linkUrl: '', sortOrder: 0, isActive: true, startDate: null, endDate: null })
const uploadUrl = '/api/files/upload/general'
const uploading = ref(false)
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

// 日期选择器限制：结束日期不能早于开始日期
function disabledStartDate(time) {
  if (form.value.endDate) {
    return time.getTime() > new Date(form.value.endDate).getTime()
  }
  return false
}
function disabledEndDate(time) {
  if (form.value.startDate) {
    return time.getTime() < new Date(form.value.startDate).getTime()
  }
  return false
}

function beforeUpload(file) {
  uploading.value = true
  const isImage = file.type.startsWith('image/')
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) { ElMessage.error('图片大小不能超过 10MB'); return false }
  return true
}

function handleUploadSuccess(res) {
  uploading.value = false
  if (res.success && res.data?.url) {
    form.value.imageUrl = res.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

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

<style scoped>
.upload-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
</style>
