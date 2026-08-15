<template>
  <div class="payment-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/account' }">{{ $t('account.myAccount') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ $t('account.paymentMethods') }}</el-breadcrumb-item>
    </el-breadcrumb>
    <h2>{{ $t('account.paymentMethods') }}</h2>
    <el-button type="primary" style="margin-bottom:16px" @click="showAddDialog">{{ $t('account.addPayment') }}</el-button>

    <div v-loading="loading">
      <el-empty v-if="methods.length === 0" :description="$t('account.noPaymentMethods')" />
      <el-table v-else :data="methods" border stripe>
        <el-table-column :label="$t('paymentForm.type')" width="140">
          <template #default="{ row }">
            <span class="pm-type-badge" :class="row.methodType.toLowerCase()">
              {{ $t('paymentMethodTypes.' + row.methodType) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="provider" :label="$t('paymentForm.provider')" width="120" />
        <el-table-column prop="accountLastFour" :label="$t('paymentForm.lastFour')" width="80" />
        <el-table-column prop="cardholderName" :label="$t('common.fullName')" width="150" />
        <el-table-column :label="$t('paymentForm.default')" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">{{ $t('account.defaultPayment') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.submit')" width="200">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="deleteMethod(row)">{{ $t('account.deletePayment') }}</el-button>
            <el-button v-if="!row.isDefault" size="small" text @click="setDefault(row)">{{ $t('account.setDefault') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="$t('account.addPayment')" width="480px">
      <el-form :model="form" label-width="140px">
        <el-form-item :label="$t('paymentMethodTypes.CREDIT_CARD')">
          <el-select v-model="form.methodType" style="width:100%" @change="onTypeChange">
            <el-option v-for="mt in methodTypes" :key="mt.value" :label="$t('paymentMethodTypes.' + mt.value)" :value="mt.value">
              <span class="pm-option-icon">{{ mt.icon }}</span>
              {{ $t('paymentMethodTypes.' + mt.value) }}
            </el-option>
          </el-select>
        </el-form-item>

        <!-- Credit Card Form -->
        <template v-if="form.methodType === 'CREDIT_CARD'">
          <el-form-item :label="$t('paymentForm.cardNumber')">
            <el-input v-model="form.cardNumber" placeholder="1234 5678 9012 3456" maxlength="19" />
          </el-form-item>
          <el-form-item :label="$t('paymentForm.expiry')">
            <el-input v-model="form.expiryDate" placeholder="MM/YY" maxlength="5" style="width:140px" />
          </el-form-item>
          <el-form-item :label="$t('paymentForm.cvv')">
            <el-input v-model="form.cvv" placeholder="123" maxlength="4" style="width:100px" type="password" />
          </el-form-item>
          <el-form-item :label="$t('common.fullName')">
            <el-input v-model="form.cardholderName" />
          </el-form-item>
        </template>

        <!-- Bank Transfer Form -->
        <template v-else-if="form.methodType === 'BANK_TRANSFER'">
          <el-form-item :label="$t('paymentForm.bankName')">
            <el-input v-model="form.provider" />
          </el-form-item>
          <el-form-item :label="$t('paymentForm.accountNumber')">
            <el-input v-model="form.accountNumber" />
          </el-form-item>
          <el-form-item :label="$t('paymentForm.routingNumber')">
            <el-input v-model="form.routingNumber" />
          </el-form-item>
          <el-form-item :label="$t('paymentForm.accountHolder')">
            <el-input v-model="form.cardholderName" />
          </el-form-item>
        </template>

        <!-- Wallet / App Payment Methods -->
        <template v-else>
          <div class="wallet-connect">
            <span class="wallet-icon-big">{{ getMethodIcon(form.methodType) }}</span>
            <p>{{ $t('paymentMethodTypes.' + form.methodType) }}</p>
            <p class="wallet-hint">{{ $t('paymentForm.walletHint') }}</p>
            <el-button type="primary" size="large" class="connect-btn">
              <el-icon style="margin-right:6px"><Link /></el-icon>
              {{ $t('paymentForm.connect') }} {{ $t('paymentMethodTypes.' + form.methodType) }}
            </el-button>
          </div>
        </template>

        <el-form-item v-if="['CREDIT_CARD','BANK_TRANSFER'].includes(form.methodType)" style="margin-top:12px">
          <el-checkbox v-model="form.isDefault">{{ $t('account.setDefault') }}</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button v-if="['CREDIT_CARD','BANK_TRANSFER'].includes(form.methodType)" type="primary" @click="saveMethod">{{ $t('common.submit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Link } from '@element-plus/icons-vue'

const { t } = useI18n()
const methods = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const form = reactive({
  methodType: 'CREDIT_CARD', provider: '', accountLastFour: '',
  expiryDate: '', cardholderName: '', isDefault: false,
  cardNumber: '', cvv: '', accountNumber: '', routingNumber: '',
})

const methodTypes = [
  { value: 'CREDIT_CARD', icon: '💳' },
  { value: 'PAYPAL', icon: 'P' },
  { value: 'ALIPAY', icon: 'A' },
  { value: 'WECHAT_PAY', icon: 'W' },
  { value: 'GRABPAY', icon: 'G' },
  { value: 'PAYNOW', icon: 'N' },
  { value: 'BANK_TRANSFER', icon: '🏦' },
]

function getMethodIcon(type) {
  const m = methodTypes.find(t => t.value === type)
  return m ? m.icon : '💳'
}

onMounted(fetchMethods)

async function fetchMethods() {
  try {
    const res = await api.get('/payment-methods')
    methods.value = res.data || []
  } catch (e) { methods.value = [] }
  loading.value = false
}

function showAddDialog() {
  form.methodType = 'CREDIT_CARD'
  form.provider = ''; form.accountLastFour = ''; form.expiryDate = ''
  form.cardholderName = ''; form.isDefault = false
  form.cardNumber = ''; form.cvv = ''; form.accountNumber = ''; form.routingNumber = ''
  dialogVisible.value = true
}

function onTypeChange() {
  // reset fields when type changes
}

async function saveMethod() {
  const payload = { methodType: form.methodType, isDefault: form.isDefault }
  if (form.methodType === 'CREDIT_CARD') {
    payload.cardholderName = form.cardholderName
    payload.provider = form.cardNumber ? form.cardNumber.slice(0, 4) : ''
    payload.accountLastFour = form.cardNumber ? form.cardNumber.slice(-4) : ''
    payload.expiryDate = form.expiryDate
  } else if (form.methodType === 'BANK_TRANSFER') {
    payload.provider = form.provider
    payload.cardholderName = form.cardholderName
    payload.accountLastFour = form.accountNumber ? form.accountNumber.slice(-4) : ''
  } else {
    payload.provider = form.methodType
  }
  try {
    await api.post('/payment-methods', payload)
    dialogVisible.value = false
    ElMessage.success(t('common.success'))
    fetchMethods()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

async function deleteMethod(row) {
  try {
    await ElMessageBox.confirm(t('account.confirmDeletePayment'))
    await api.delete(`/payment-methods/${row.id}`)
    ElMessage.success(t('common.deleted'))
    fetchMethods()
  } catch (e) {}
}

async function setDefault(row) {
  try {
    await api.put(`/payment-methods/${row.id}/default`)
    ElMessage.success(t('common.defaultSet'))
    fetchMethods()
  } catch (e) {}
}
</script>

<style scoped>
.payment-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
h2 { margin-bottom: 16px; }
.empty { text-align: center; padding: 60px; color: #9ca3af; }

.pm-type-badge {
  display: inline-block;
  padding: 3px 10px; border-radius: 12px;
  font-size: 12px; font-weight: 600;
  background: #f3f4f6; color: #374151;
}
.pm-type-badge.credit_card { background: #eff6ff; color: #2563eb; }
.pm-type-badge.paypal { background: #eff6ff; color: #003087; }
.pm-type-badge.alipay { background: #eff6ff; color: #1677ff; }
.pm-type-badge.wechat_pay { background: #ecfdf5; color: #059669; }
.pm-type-badge.grabpay { background: #ecfdf5; color: #00b14f; }
.pm-type-badge.paynow { background: #fef2f2; color: #dc2626; }
.pm-type-badge.bank_transfer { background: #f5f3ff; color: #7c3aed; }

.pm-option-icon { margin-right: 8px; }

.wallet-connect {
  text-align: center;
  padding: 32px 16px;
}
.wallet-icon-big { font-size: 48px; display: block; margin-bottom: 12px; }
.wallet-hint { font-size: 13px; color: #6b7280; margin: 8px 0 20px; }
.connect-btn { margin-top: 8px; }
</style>
