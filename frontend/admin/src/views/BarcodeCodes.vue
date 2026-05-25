<template>
  <div class="codes-page">
    <h2 style="margin-bottom:16px">🔣 编码维护 / Barcode Codes</h2>

    <!-- Tab selector for code type -->
    <el-tabs v-model="activeType" @tab-change="loadCodes">
      <el-tab-pane label="Country / 国家" name="COUNTRY" />
      <el-tab-pane label="Category / 类别" name="CATEGORY" />
      <el-tab-pane label="Denomination / 面值" name="DENOM" />
      <el-tab-pane label="Era / 朝代" name="ERA" />
      <el-tab-pane label="Grade / 评分" name="GRADE" />
    </el-tabs>

    <div class="action-bar">
      <el-input v-model="searchQuery" placeholder="搜索..." clearable style="width:240px" @input="onSearch" />
      <el-button type="primary" @click="openCreate">＋ 新增 / Add</el-button>
    </div>

    <el-table :data="codes" stripe v-loading="loading" style="margin-top:8px">
      <el-table-column prop="codeValue" label="编码" width="120" />
      <el-table-column prop="labelEn" label="英文名称" min-width="160" />
      <el-table-column prop="labelZh" label="中文名称" min-width="160" />
      <el-table-column prop="parentType" label="父类型" width="100" />
      <el-table-column prop="parentValue" label="父值" width="80" />
      <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">{{ row.isActive ? 'Active' : 'Disabled' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="openEdit(row)">Edit</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(row.id)">Del</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? 'Edit Code' : 'Add Code'" width="520px">
      <el-form ref="formRef" :model="form" label-width="120px">
        <el-form-item label="Code Type" prop="codeType">
          <el-select v-model="form.codeType" :disabled="isEdit" style="width:100%">
            <el-option label="COUNTRY" value="COUNTRY" />
            <el-option label="CATEGORY" value="CATEGORY" />
            <el-option label="DENOM" value="DENOM" />
            <el-option label="ERA" value="ERA" />
            <el-option label="GRADE" value="GRADE" />
          </el-select>
        </el-form-item>
        <el-form-item label="Code Value" prop="codeValue">
          <el-input v-model="form.codeValue" :disabled="isEdit" placeholder="3-digit code" />
        </el-form-item>
        <el-form-item label="Label (EN)">
          <el-input v-model="form.labelEn" />
        </el-form-item>
        <el-form-item label="Label (ZH)">
          <el-input v-model="form.labelZh" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="Sort Order">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Parent Type">
              <el-select v-model="form.parentType" clearable style="width:100%">
                <el-option label="COUNTRY" value="COUNTRY" />
                <el-option label="CATEGORY" value="CATEGORY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Parent Value">
              <el-input v-model="form.parentValue" placeholder="e.g. 840" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Active">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save" :loading="saving">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeType = ref('COUNTRY')
const codes = ref([])
const loading = ref(false)
const searchQuery = ref('')
let searchTimer = null

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = ref({
  codeType: 'COUNTRY',
  codeValue: '',
  labelEn: '',
  labelZh: '',
  parentType: '',
  parentValue: '',
  sortOrder: 0,
  isActive: true,
})

onMounted(() => loadCodes())

async function loadCodes() {
  loading.value = true
  try {
    const res = await api.get(`/admin/barcode-codes/type/${activeType.value}`)
    codes.value = res.data || []
  } catch (_) { codes.value = [] }
  finally { loading.value = false }
}

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (searchQuery.value) {
      try {
        const res = await api.get('/admin/barcode-codes/search', { params: { q: searchQuery.value } })
        codes.value = res.data || []
      } catch (_) {}
    } else {
      loadCodes()
    }
  }, 400)
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { codeType: activeType.value, codeValue: '', labelEn: '', labelZh: '', parentType: '', parentValue: '', sortOrder: 0, isActive: true }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put(`/admin/barcode-codes/${editId.value}`, form.value)
      ElMessage.success('Updated')
    } else {
      await api.post('/admin/barcode-codes', form.value)
      ElMessage.success('Created')
    }
    dialogVisible.value = false
    loadCodes()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Failed')
  } finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除此编码？')
    await api.delete(`/admin/barcode-codes/${id}`)
    ElMessage.success('Deleted')
    loadCodes()
  } catch (_) {}
}
</script>

<style scoped>
.codes-page { padding: 4px; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
</style>
