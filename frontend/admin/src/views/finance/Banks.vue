<template>
  <div class="banks-page">
    <div class="page-header">
      <h2>💳 银行管理 / Bank Accounts</h2>
      <div>
        <el-button @click="printPage('银行账户管理')">🖨️ 打印</el-button>
        <el-button type="primary" @click="openAddDialog">＋ 添加银行</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8" v-for="bank in banks" :key="bank.id">
        <BankCard :account="bank" @transfer="openTransfer" @adjust="openAdjust" @detail="showDetail" />
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <template #header>📋 转账记录 / Transfer History</template>
      <el-table :data="transfers" stripe v-loading="loadingTransfers">
        <el-table-column type="expand">
          <template #default="{ row }">
            <p style="margin:4px 0"><strong>备注：</strong>{{ row.description || '-' }}</p>
            <p style="margin:4px 0"><strong>参考号：</strong>{{ row.referenceNo || '-' }}</p>
          </template>
        </el-table-column>
        <el-table-column prop="transferDate" label="日期" width="120" />
        <el-table-column label="从" min-width="120">
          <template #default="{ row }">{{ row.fromBankName || 'Account #' + row.fromAccountId }}</template>
        </el-table-column>
        <el-table-column label="到" min-width="120">
          <template #default="{ row }">{{ row.toBankName || 'Account #' + row.toAccountId }}</template>
        </el-table-column>
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">${{ fmt(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="手续费" width="100" align="right">
          <template #default="{ row }">${{ fmt(row.fee) }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="bankDialog" :title="editingBank ? '编辑银行' : '添加银行'" width="500px">
      <el-form ref="bankFormRef" :model="bankForm" :rules="bankRules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="银行名称" prop="bankName">
              <el-select v-model="bankForm.bankName" style="width:100%" filterable allow-create>
                <el-option v-for="n in bankNames" :key="n" :label="n" :value="n" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="币种" prop="currency">
              <el-select v-model="bankForm.currency" style="width:100%">
                <el-option label="SGD" value="SGD" /><el-option label="MYR" value="MYR" /><el-option label="USD" value="USD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="户名" prop="accountName">
          <el-input v-model="bankForm.accountName" />
        </el-form-item>
        <el-form-item label="账号(后4位)" prop="accountNumber">
          <el-input v-model="bankForm.accountNumber" maxlength="4" placeholder="仅输入后4位" />
        </el-form-item>
        <el-form-item label="国家" prop="country">
          <el-select v-model="bankForm.country" style="width:100%">
            <el-option label="新加坡 SG" value="SG" /><el-option label="马来西亚 MY" value="MY" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前余额" prop="currentBalance">
          <el-input-number v-model="bankForm.currentBalance" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bankForm.notes" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bankDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBank" :loading="savingBank">{{ editingBank ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <TransferDialog v-model="transferDialogVisible" :banks="banks" @success="loadData" />

    <el-dialog v-model="adjustDialog" title="调整余额" width="400px">
      <el-form ref="adjustFormRef" :model="adjustForm" label-width="80px">
        <el-form-item label="银行">
          <el-tag>{{ adjustBank?.bankName }}</el-tag>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="adjustForm.amount" :precision="2" style="width:100%"
            :placeholder="'正数=入账，负数=出账'" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="adjustForm.notes" type="textarea" :rows="2" placeholder="调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust" :loading="savingAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../../api'
import { ElMessage } from 'element-plus'
import { printPage } from '../../utils/print'
import BankCard from '../../components/finance/BankCard.vue'
import TransferDialog from '../../components/finance/TransferDialog.vue'

const bankNames = ['DBS', 'OCBC', 'UOB', 'Maybank', 'CIMB', 'Public Bank', 'RHB', 'HSBC', 'Standard Chartered']
const banks = ref([])
const transfers = ref([])
const loadingTransfers = ref(false)
const bankDialog = ref(false)
const editingBank = ref(null)
const savingBank = ref(false)
const bankFormRef = ref(null)

const bankForm = reactive({
  bankName: '', accountName: '', accountNumber: '', currency: 'SGD',
  country: 'SG', currentBalance: 0, notes: '', sortOrder: 0,
})

const bankRules = {
  bankName: [{ required: true, message: '请选择银行' }],
  accountName: [{ required: true, message: '请输入户名' }],
  accountNumber: [{ required: true, message: '请输入账号' }],
  currency: [{ required: true, message: '请选择币种' }],
  country: [{ required: true, message: '请选择国家' }],
}

const transferDialogVisible = ref(false)
const adjustDialog = ref(false)
const adjustBank = ref(null)
const savingAdjust = ref(false)
const adjustFormRef = ref(null)
const adjustForm = reactive({ amount: 0, notes: '' })

onMounted(loadData)

async function loadData() {
  try {
    const [bankRes, transferRes] = await Promise.all([
      api.get('/admin/finance/banks'),
      api.get('/admin/finance/banks/transfers', { params: { page: 0, size: 20 } }),
    ])
    banks.value = bankRes.data || []
    transfers.value = transferRes.data?.content || []
  } catch (_) {}
}

function openAddDialog() {
  editingBank.value = null
  Object.assign(bankForm, { bankName: '', accountName: '', accountNumber: '', currency: 'SGD', country: 'SG', currentBalance: 0, notes: '', sortOrder: 0 })
  bankDialog.value = true
}

async function saveBank() {
  const valid = await bankFormRef.value.validate().catch(() => false)
  if (!valid) return
  savingBank.value = true
  try {
    await api.post('/admin/finance/banks', { ...bankForm })
    ElMessage.success('添加成功')
    bankDialog.value = false
    loadData()
  } catch (e) { ElMessage.error(e.response?.data?.message || '保存失败') }
  finally { savingBank.value = false }
}

function openTransfer(bank) {
  transferDialogVisible.value = true
}

function openAdjust(bank) {
  adjustBank.value = bank
  adjustForm.amount = 0
  adjustForm.notes = ''
  adjustDialog.value = true
}

async function submitAdjust() {
  savingAdjust.value = true
  try {
    await api.post(`/admin/finance/banks/${adjustBank.value.id}/adjust`, null, {
      params: { amount: adjustForm.amount, notes: adjustForm.notes || '' }
    })
    ElMessage.success('余额已调整')
    adjustDialog.value = false
    loadData()
  } catch (e) { ElMessage.error('调整失败') }
  finally { savingAdjust.value = false }
}

function showDetail(bank) {
  loadData()
}

function fmt(v) { return Number(v || 0).toLocaleString('en-US', { minimumFractionDigits: 2 }) }
</script>

<style scoped>
.banks-page { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>
