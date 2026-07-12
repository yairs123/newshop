<template>
  <div class="codes-page">
    <div class="page-header">
      <h2>编码管理</h2>
      <span class="page-hint">在此添加或编辑选项，入库时即可从下拉菜单选择</span>
    </div>

    <el-card shadow="never" class="main-card">
      <!-- 分类标签 -->
      <div class="type-tabs">
        <el-tag v-for="t in codeTypes" :key="t.key" :type="activeType === t.key ? 'primary' : 'info'" effect="plain" style="cursor:pointer" @click="switchType(t.key)">{{ t.label }}</el-tag>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <el-input v-model="searchQuery" :placeholder="'搜索' + currentTypeLabel + '...'" clearable style="width:260px" @input="onSearch" />
        <el-button type="primary" @click="openCreate">＋ 新增</el-button>
      </div>

      <!-- 编码列表 -->
      <el-table :data="codes" v-loading="loading" stripe border style="width:100%" :empty-text="'暂无' + currentTypeLabel">
        <el-table-column prop="codeValue" label="编码值" width="100" />
        <el-table-column prop="labelZh" :label="currentTypeLabel + '(中文)'" min-width="140" />
        <el-table-column prop="labelEn" :label="currentTypeLabel + '(英文)'" min-width="140" />
        <el-table-column prop="parentType" label="父类型" width="90" />
        <el-table-column prop="parentValue" label="父值" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="60" align="center" />
        <el-table-column label="启用" width="70" align="center">
          <template #default="{ row }"><el-tag :type="row.isActive ? 'success' : 'danger'" size="small">{{ row.isActive ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" plain @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑编码' : '新增编码'" width="520px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" label-width="110px" size="small">
        <el-form-item label="编码类型">
          <el-select v-model="form.codeType" :disabled="isEdit" style="width:100%">
            <el-option v-for="t in codeTypes" :key="t.key" :label="t.label" :value="t.key" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码值">
          <el-input v-model="form.codeValue" :disabled="isEdit" :placeholder="'输入唯一编码，如 001'" />
        </el-form-item>
        <el-form-item :label="'名称(中文)'">
          <el-input v-model="form.labelZh" :placeholder="'中文名称'" />
        </el-form-item>
        <el-form-item :label="'名称(英文)'">
          <el-input v-model="form.labelEn" :placeholder="'English name'" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="父类型">
              <el-select v-model="form.parentType" clearable style="width:100%">
                <el-option label="国家(COUNTRY)" value="COUNTRY" />
                <el-option label="类别(CATEGORY)" value="CATEGORY" />
                <el-option label="面值(DENOM)" value="DENOM" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="父值">
              <el-input v-model="form.parentValue" placeholder="如 840" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const codeTypes = [
  { key: 'COUNTRY', label: '国家' },
  { key: 'CATEGORY', label: '类别' },
  { key: 'DENOM', label: '面值' },
  { key: 'ERA', label: '年代' },
  { key: 'GRADE', label: '评分' },
  { key: 'RATING_COMPANY', label: '评级公司' },
  { key: 'MATERIAL', label: '材质' },
  { key: 'SUPPLIER', label: '供应商' }
]

const activeType = ref('COUNTRY')
const currentTypeLabel = computed(() => { const t = codeTypes.find(t => t.key === activeType.value); return t ? t.label : '' })
const codes = ref([])
const loading = ref(false)
const searchQuery = ref('')
let searchTimer = null
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = ref({ codeType: 'COUNTRY', codeValue: '', labelEn: '', labelZh: '', parentType: '', parentValue: '', sortOrder: 0, isActive: true })

function switchType(type) { activeType.value = type; searchQuery.value = ''; loadCodes() }

async function loadCodes() {
  loading.value = true
  try { const r = await api.get('/admin/barcode-codes/type/' + activeType.value); codes.value = r.data || [] }
  catch (e) { codes.value = [] }
  finally { loading.value = false }
}

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (searchQuery.value) {
      try { const r = await api.get('/admin/barcode-codes/search', { params: { q: searchQuery.value } }); codes.value = (r.data || []).filter(c => c.codeType === activeType.value) }
      catch (e) { codes.value = [] }
    } else { loadCodes() }
  }, 400)
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.value = { codeType: activeType.value, codeValue: '', labelEn: '', labelZh: '', parentType: '', parentValue: '', sortOrder: 0, isActive: true }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (isEdit.value) { await api.put('/admin/barcode-codes/' + editId.value, form.value); ElMessage.success('保存成功') }
    else { await api.post('/admin/barcode-codes', form.value); ElMessage.success('创建成功') }
    dialogVisible.value = false; loadCodes()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定删除此编码？'); await api.delete('/admin/barcode-codes/' + id); ElMessage.success('已删除'); loadCodes() }
  catch (e) { /* cancelled */ }
}

onMounted(() => loadCodes())
</script>

<style scoped>
.codes-page { padding: 0; }
.page-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.page-header h2 { font-size: 18px; font-weight: 600; color: #303133; margin: 0; }
.page-hint { font-size: 12px; color: #909399; }
.main-card { border-radius: 8px; border: 1px solid #ebeef5; }
.type-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
</style>
