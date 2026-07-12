<template>
  <div class="finance-page">
    <div class="page-header">
      <h2>👥 人员开支 / Personnel Expenses</h2>
      <div>
        <el-button @click="printPage('人员开支')">🖨️ 打印</el-button>
        <el-button type="primary" @click="openAddDialog">＋ 添加</el-button>
      </div>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="payDate" label="发薪日" width="120" />
      <el-table-column prop="employeeName" label="姓名" width="120" />
      <el-table-column prop="position" label="岗位" width="120" />
      <el-table-column label="金额" width="140" align="right">
        <template #default="{ row }">${{ fmt(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="periodStart" label="周期开始" width="120" />
      <el-table-column prop="periodEnd" label="周期结束" width="120" />
      <el-table-column prop="paymentMethod" label="支付方式" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text @click="openEditDialog(row)">✏️</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row.id)">🗑️</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="totalPages > 1" style="text-align:center;margin-top:16px">
      <el-pagination v-model:current-page="page" :total="totalElements" :page-size="20" @current-change="loadData" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑人员开支' : '添加人员开支'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="姓名" prop="employeeName">
              <el-input v-model="form.employeeName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位" prop="position">
              <el-input v-model="form.position" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="实发金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="币种">
          <el-select v-model="form.currency" style="width:100%">
            <el-option label="SGD" value="SGD" /><el-option label="MYR" value="MYR" /><el-option label="USD" value="USD" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="发薪日" prop="payDate">
              <el-date-picker v-model="form.payDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="周期开始" prop="periodStart">
              <el-date-picker v-model="form.periodStart" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="周期结束" prop="periodEnd">
              <el-date-picker v-model="form.periodEnd" type="date" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="支付方式">
          <el-select v-model="form.paymentMethod" style="width:100%">
            <el-option label="银行转账" value="BANK_TRANSFER" />
            <el-option label="现金" value="CASH" />
            <el-option label="支票" value="CHEQUE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" />
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
const loading = ref(false)
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  employeeName: '', position: '', amount: 0, currency: 'SGD',
  payDate: '', periodStart: '', periodEnd: '', paymentMethod: 'BANK_TRANSFER', notes: '',
})

const rules = {
  employeeName: [{ required: true, message: '请输入姓名' }],
  amount: [{ required: true, message: '请输入金额' }],
  payDate: [{ required: true, message: '请选择发薪日' }],
  periodStart: [{ required: true, message: '请选择周期开始' }],
  periodEnd: [{ required: true, message: '请选择周期结束' }],
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await api.get('/admin/finance/personnel', { params: { page: page.value, size: 20 } })
    list.value = res.data?.content || []
    totalPages.value = res.data?.totalPages || 0
    totalElements.value = res.data?.totalElements || 0
  } catch (_) { list.value = [] }
  finally { loading.value = false }
}

function openAddDialog() {
  editingId.value = null
  Object.assign(form, { employeeName: '', position: '', amount: 0, currency: 'SGD', payDate: '', periodStart: '', periodEnd: '', paymentMethod: 'BANK_TRANSFER', notes: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingId.value = row.id
  Object.assign(form, {
    employeeName: row.employeeName, position: row.position, amount: row.amount,
    currency: row.currency, payDate: row.payDate, periodStart: row.periodStart,
    periodEnd: row.periodEnd, paymentMethod: row.paymentMethod, notes: row.notes || '',
  })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/admin/finance/personnel/${editingId.value}`, { ...form })
      ElMessage.success('保存成功')
    } else {
      await api.post('/admin/finance/personnel', { ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '操作失败') }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？')
    await api.delete(`/admin/finance/personnel/${id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (_) {}
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.finance-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
