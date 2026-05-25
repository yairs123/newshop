<template>
  <div class="finance-page">
    <h2 style="margin-bottom:16px">💰 财务管理 / Finance</h2>

    <!-- Dashboard Cards -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">采购成本 / Purchase Cost</div>
            <div class="stat-value" style="color:#f59e0b">${{ dashboard.totalPurchaseCost }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">销售收入 / Sales Revenue</div>
            <div class="stat-value" style="color:#10b981">${{ dashboard.totalSalesRevenue }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">净利润 / Net Profit</div>
            <div class="stat-value" :style="{ color: dashboard.netProfit >= 0 ? '#10b981' : '#ef4444' }">${{ dashboard.netProfit }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">待审核报销</div>
            <div class="stat-value" style="color:#f97316">{{ dashboard.pendingReimbursements }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Reimbursement Tabs -->
    <el-card>
      <template #header>
        <div style="display:flex; justify-content:space-between; align-items:center">
          <span>报销管理 / Reimbursements</span>
          <el-button type="primary" size="small" @click="openCreateReimbursement">＋ 新建报销</el-button>
        </div>
      </template>

      <el-tabs v-model="reimbTab" @tab-change="loadReimbursements">
        <el-tab-pane label="全部 All" name="all" />
        <el-tab-pane label="待审核 Pending" name="PENDING" />
        <el-tab-pane label="已通过 Approved" name="APPROVED" />
        <el-tab-pane label="已驳回 Rejected" name="REJECTED" />
        <el-tab-pane label="已支付 Paid" name="PAID" />
      </el-tabs>

      <el-table :data="reimbursements" stripe v-loading="loadingReimb">
        <el-table-column prop="createdAt" label="日期" width="150" />
        <el-table-column prop="title" label="标题 Title" min-width="200" />
        <el-table-column label="金额 Amount" width="140" align="right">
          <template #default="{ row }">
            <span class="price">{{ row.currency }} {{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100" align="center" />
        <el-table-column label="状态 Status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="reimbStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" plain @click="approveReimb(row.id)">Approve</el-button>
            <el-button v-if="row.status === 'PENDING'" size="small" type="warning" plain @click="rejectReimb(row.id)">Reject</el-button>
            <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" plain @click="payReimb(row.id)">Pay</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loadingReimb && reimbursements.length === 0" description="暂无报销记录" />
    </el-card>

    <!-- Create Reimbursement Dialog -->
    <el-dialog v-model="dialogVisible" title="新建报销 / New Reimbursement" width="500px">
      <el-form ref="reimbFormRef" :model="reimbForm" label-width="120px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="reimbForm.title" placeholder="报销标题" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="Amount" prop="amount">
              <el-input-number v-model="reimbForm.amount" :min="0.01" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Currency">
              <el-select v-model="reimbForm.currency" style="width:100%">
                <el-option label="USD" value="USD" />
                <el-option label="CNY" value="CNY" />
                <el-option label="EUR" value="EUR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="Category">
          <el-select v-model="reimbForm.category" style="width:100%">
            <el-option label="Travel / 差旅" value="TRAVEL" />
            <el-option label="Office / 办公" value="OFFICE" />
            <el-option label="Shipping / 运费" value="SHIPPING" />
            <el-option label="Supplies / 采购" value="SUPPLIES" />
            <el-option label="Other / 其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="reimbForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submitReimbursement" :loading="savingReimb">Submit</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const dashboard = reactive({
  totalPurchaseCost: '0.00',
  totalSalesRevenue: '0.00',
  netProfit: '0.00',
  profitMargin: '0%',
  pendingReimbursements: 0,
  totalReimbursements: 0,
})

const reimbursements = ref([])
const loadingReimb = ref(false)
const reimbTab = ref('all')
const reimbPage = ref(1)
const reimbTotal = ref(0)

const dialogVisible = ref(false)
const savingReimb = ref(false)
const reimbFormRef = ref(null)
const reimbForm = reactive({
  title: '',
  amount: 0,
  currency: 'USD',
  category: 'TRAVEL',
  description: '',
})

onMounted(() => {
  loadDashboard()
  loadReimbursements()
})

async function loadDashboard() {
  try {
    const res = await api.get('/admin/finance/dashboard')
    if (res.data) Object.assign(dashboard, res.data)
  } catch (_) {}
}

async function loadReimbursements() {
  loadingReimb.value = true
  try {
    const params = { page: reimbPage.value - 1, size: 20 }
    if (reimbTab.value !== 'all') params.status = reimbTab.value
    const res = await api.get('/admin/finance/reimbursements', { params })
    reimbursements.value = res.data?.content || []
    reimbTotal.value = res.data?.totalElements || 0
  } catch (_) { reimbursements.value = [] }
  finally { loadingReimb.value = false }
}

function reimbStatusType(status) {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'PAID': return 'primary'
    default: return 'info'
  }
}

function openCreateReimbursement() {
  reimbForm.title = ''
  reimbForm.amount = 0
  reimbForm.currency = 'USD'
  reimbForm.category = 'TRAVEL'
  reimbForm.description = ''
  dialogVisible.value = true
}

async function submitReimbursement() {
  savingReimb.value = true
  try {
    await api.post('/admin/finance/reimbursements', reimbForm)
    ElMessage.success('报销已提交')
    dialogVisible.value = false
    loadReimbursements()
    loadDashboard()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  } finally { savingReimb.value = false }
}

async function approveReimb(id) {
  try {
    await api.post(`/admin/finance/reimbursements/${id}/approve`)
    ElMessage.success('已批准')
    loadReimbursements()
    loadDashboard()
  } catch (_) {}
}

async function rejectReimb(id) {
  try {
    const { value } = await ElMessageBox.prompt('输入驳回原因 / Reject reason:', 'Reject')
    await api.post(`/admin/finance/reimbursements/${id}/reject?reason=${encodeURIComponent(value)}`)
    ElMessage.success('已驳回')
    loadReimbursements()
  } catch (_) {}
}

async function payReimb(id) {
  try {
    await ElMessageBox.confirm('确认支付此报销？')
    await api.post(`/admin/finance/reimbursements/${id}/pay`)
    ElMessage.success('已标记支付')
    loadReimbursements()
    loadDashboard()
  } catch (_) {}
}
</script>

<style scoped>
.finance-page { padding: 4px; }
.stat-card { text-align: center; }
.stat-label { font-size: 13px; color: #6b7280; margin-bottom: 4px; }
.stat-value { font-size: 28px; font-weight: 700; }
.price { font-weight: 600; color: #059669; }
</style>
