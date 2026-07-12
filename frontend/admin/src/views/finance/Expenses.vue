<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>💰 其他费用 / Other Expenses</h2>
      <div>
        <el-button @click="printPage('其他费用')">🖨️ 打印</el-button>
        <el-button type="primary" @click="openAddDialog">＋ 添加费用</el-button>
      </div>
    </div>

    <el-card style="margin-bottom:16px">
      <el-select v-model="categoryFilter" placeholder="选择类别" clearable @change="loadData" style="width:200px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </el-card>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="expenseDate" label="日期" width="120" />
      <el-table-column prop="categoryName" label="类别" width="100" />
      <el-table-column label="金额" width="140" align="right">
        <template #default="{ row }">${{ fmt(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="180" />
      <el-table-column prop="vendor" label="收款方" width="120" />
      <el-table-column prop="paymentMethod" label="支付方式" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="openEditDialog(row)">✏️</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row.id)">🗑️</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑费用' : '添加费用'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类别" prop="categoryId">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="日期" prop="expenseDate">
          <el-date-picker v-model="form.expenseDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="收款方">
          <el-input v-model="form.vendor" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="form.paymentMethod" style="width:100%">
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="现金" value="CASH" />
            <el-option label="信用卡" value="CREDIT_CARD" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">{{ editingId ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { printPage } from '../../utils/print'

const list = ref([])
const categories = ref([])
const loading = ref(false)
const categoryFilter = ref(null)
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  categoryId: null, amount: 0, currency: 'SGD',
  expenseDate: '', description: '', vendor: '', paymentMethod: 'BANK_TRANSFER', bankAccountId: null,
})

const rules = {
  categoryId: [{ required: true, message: '请选择类别' }],
  amount: [{ required: true, message: '请输入金额' }],
  expenseDate: [{ required: true, message: '请选择日期' }],
  description: [{ required: true, message: '请输入说明' }],
}

onMounted(async () => {
  try {
    const res = await api.get('/admin/finance/expense-categories')
    categories.value = res.data || []
  } catch (_) {}
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = { page: 0, size: 50 }
    if (categoryFilter.value) params.categoryId = categoryFilter.value
    const res = await api.get('/admin/finance/expenses', { params })
    list.value = res.data?.content || []
  } catch (_) { list.value = [] }
  finally { loading.value = false }
}

function openAddDialog() {
  editingId.value = null
  Object.assign(form, { categoryId: null, amount: 0, currency: 'SGD', expenseDate: '', description: '', vendor: '', paymentMethod: 'BANK_TRANSFER', bankAccountId: null })
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingId.value = row.id
  Object.assign(form, {
    categoryId: row.categoryId, amount: row.amount, currency: row.currency,
    expenseDate: row.expenseDate, description: row.description, vendor: row.vendor || '',
    paymentMethod: row.paymentMethod || 'BANK_TRANSFER', bankAccountId: row.bankAccountId,
  })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/admin/finance/expenses/${editingId.value}`, { ...form })
      ElMessage.success('保存成功')
    } else {
      await api.post('/admin/finance/expenses', { ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '操作失败') }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定删除？'); await api.delete(`/admin/finance/expenses/${id}`); ElMessage.success('已删除'); loadData() }
  catch (_) {}
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.finance-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
