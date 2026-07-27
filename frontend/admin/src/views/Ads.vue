<template>
  <div class="ads-page">
    <div class="page-head">
      <div>
        <h2>📢 {{ $t('ads.title') }}</h2>
        <p class="page-desc">管理首页广告轮播</p>
      </div>
      <button class="btn-primary" @click="openCreate">＋ {{ $t('ads.addAd') }}</button>
    </div>

    <div class="table-wrap">
      <el-table :data="ads" border stripe v-loading="loading" style="width:100%">
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width:48px;height:48px;border-radius:6px" fit="cover" />
            <span v-else style="color:#ccc">—</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('ads.title_')" prop="title" min-width="160" />
        <el-table-column label="链接" prop="linkUrl" min-width="180" show-overflow-tooltip />
        <el-table-column label="排序" prop="sortOrder" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="status-pill" :class="row.isActive ? 'pill-on' : 'pill-off'">
              {{ row.isActive ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="起止日期" min-width="200">
          <template #default="{ row }">{{ row.startDate || '—' }} ~ {{ row.endDate || '—' }}</template>
        </el-table-column>
        <el-table-column :label="$t('products.actions')" width="240" fixed="right">
          <template #default="{ row }">
            <div class="tbl-actions">
              <button class="tbl-btn" @click="openEdit(row)">✏️ 编辑</button>
              <button class="tbl-btn" :class="row.isActive ? 'btn-warn' : 'btn-ok'" @click="toggleStatus(row)">
                {{ row.isActive ? '⏸ 停用' : '▶️ 启用' }}
              </button>
              <button class="tbl-btn btn-del" @click="remove(row)">🗑️ 删除</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑广告' : '添加广告'" width="560px" :close-on-click-modal="false" class="theme-dialog">
      <el-form :model="form" label-position="top" v-if="dialogVisible">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="广告标题" />
        </el-form-item>
        <el-form-item label="图片链接">
          <div class="upload-row">
            <el-input v-model="form.imageUrl" placeholder="https://..." style="flex:1" />
            <el-upload :action="uploadUrl" :show-file-list="false" :on-success="handleUploadSuccess"
              :before-upload="beforeUpload" :headers="uploadHeaders">
              <button class="btn-upload">{{ uploading ? '上传中...' : '📁 上传' }}</button>
            </el-upload>
          </div>
          <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:100px;height:66px;margin-top:8px;border-radius:6px" fit="cover" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="https://..." />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用">
              <el-switch v-model="form.isActive" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="d => form.endDate && d > new Date(form.endDate)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="d => form.startDate && d < new Date(form.startDate)" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <button class="btn-cancel" @click="dialogVisible = false">取消</button>
        <button class="btn-confirm" @click="save">💾 保存</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'

const ads = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(false)
const form = ref({ title: '', imageUrl: '', linkUrl: '', sortOrder: 0, isActive: true, startDate: null, endDate: null })
const uploadUrl = '/api/files/upload/general'
const uploading = ref(false)
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

function beforeUpload(file) {
  uploading.value = true
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片不能超过10MB'); return false }
  return true
}
function handleUploadSuccess(res) {
  uploading.value = false
  if (res.success && res.data?.url) { form.value.imageUrl = res.data.url; ElMessage.success('上传成功') }
  else ElMessage.error('上传失败')
}

async function fetch() {
  loading.value = true
  try { const res = await api.get('/admin/ads'); ads.value = res.data || [] } catch (e) {}
  loading.value = false
}
function openCreate() {
  editing.value = false
  form.value = { title: '', imageUrl: '', linkUrl: '', sortOrder: 0, isActive: true, startDate: null, endDate: null }
  dialogVisible.value = true
}
function openEdit(row) { editing.value = true; form.value = { ...row }; dialogVisible.value = true }
async function save() {
  try {
    if (editing.value) await api.put(`/admin/ads/${form.value.id}`, form.value)
    else await api.post('/admin/ads', form.value)
    dialogVisible.value = false; await fetch()
  } catch (e) {}
}
async function toggleStatus(row) {
  try { await api.put(`/admin/ads/${row.id}/toggle-status`); await fetch() } catch (e) {}
}
async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除此广告？', '确认')
    await api.delete(`/admin/ads/${row.id}`); await fetch()
  } catch (e) {}
}
onMounted(fetch)
</script>

<style scoped>
.page-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-head h2 { font-family: 'DM Serif Display', Georgia, serif; font-size: 22px; font-weight: 700; color: var(--ink,#0f172a); margin: 0; }
.page-desc { font-size: 13px; color: var(--text-muted,#94a3b8); margin-top: 2px; }
.btn-primary {
  display: inline-flex; align-items: center; gap: 4px; padding: 8px 18px;
  border-radius: 8px; border: none;
  background: linear-gradient(135deg, #d4a843, #b8932a); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-primary:hover { opacity: 0.9; }

.table-wrap { border-radius: 10px; border: 1px solid var(--border,#e5e7eb); overflow: hidden; }
.tbl-actions { display: flex; gap: 4px; flex-wrap: wrap; }
.tbl-btn {
  padding: 4px 10px; border-radius: 6px; border: 1px solid var(--border,#e5e7eb);
  background: #fff; font-size: 12px; cursor: pointer; transition: all 0.15s;
}
.tbl-btn:hover { transform: translateY(-1px); box-shadow: 0 2px 6px rgba(0,0,0,.06); }
.btn-ok { color: #16a34a; border-color: #bbf7d0; background: #f0fdf4; }
.btn-ok:hover { background: #dcfce7; }
.btn-warn { color: #d97706; border-color: #fde68a; background: #fffbeb; }
.btn-warn:hover { background: #fef3c7; }
.btn-del { color: #dc2626; border-color: #fecaca; }
.btn-del:hover { background: #fef2f2; }

.status-pill { display: inline-block; padding: 2px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.pill-on { background: #f0fdf4; color: #16a34a; }
.pill-off { background: #f3f4f6; color: #6b7280; }

.upload-row { display: flex; gap: 8px; align-items: flex-start; }
.btn-upload { padding: 8px 14px; border-radius: 6px; border: 1px solid var(--border,#e5e7eb); background: #fff; cursor: pointer; font-size: 13px; white-space: nowrap; }
.btn-upload:hover { border-color: var(--gold,#d4a843); }

.btn-cancel { padding: 8px 20px; border-radius: 8px; border: 1px solid #d1d5db; background: #fff; color: #374151; font-size: 14px; cursor: pointer; }
.btn-confirm { padding: 8px 20px; border-radius: 8px; border: none; background: linear-gradient(135deg, #d4a843, #b8932a); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; margin-left: 8px; }
.btn-confirm:hover { opacity: 0.9; }
</style>
