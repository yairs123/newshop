<template>
  <el-dialog v-model="visible" title="💳 银行转账" width="480px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="从" prop="fromAccountId">
        <el-select v-model="form.fromAccountId" style="width:100%" @change="onFromChange">
          <el-option v-for="b in banks" :key="b.id" :label="`${b.bankName} (${b.currency})`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="到" prop="toAccountId">
        <el-select v-model="form.toAccountId" style="width:100%" @change="onToChange">
          <el-option v-for="b in banks" :key="b.id" :label="`${b.bankName} (${b.currency})`" :value="b.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width:100%" />
      </el-form-item>
      <el-form-item label="手续费">
        <el-input-number v-model="form.fee" :min="0" :precision="2" style="width:100%" />
      </el-form-item>
      <el-form-item label="日期" prop="transferDate">
        <el-date-picker v-model="form.transferDate" type="date" style="width:100%" />
      </el-form-item>
      <el-form-item label="参考号">
        <el-input v-model="form.referenceNo" placeholder="交易参考号" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="转账说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="saving">确认转账</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { api } from '../../api'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  banks: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, v => visible.value = v)
watch(visible, v => emit('update:modelValue', v))

const formRef = ref(null)
const saving = ref(false)
const form = reactive({
  fromAccountId: null,
  toAccountId: null,
  amount: 0,
  fee: 0,
  transferDate: new Date().toISOString().split('T')[0],
  referenceNo: '',
  description: '',
})

const rules = {
  fromAccountId: [{ required: true, message: '请选择转出账户' }],
  toAccountId: [{ required: true, message: '请选择转入账户' }],
  amount: [{ required: true, message: '请输入金额' }],
  transferDate: [{ required: true, message: '请选择日期' }],
}

function onFromChange() {
  const bank = props.banks.find(b => b.id === form.fromAccountId)
  if (bank) form.currency = bank.currency
}
function onToChange() {
  const bank = props.banks.find(b => b.id === form.toAccountId)
  if (bank && !form.currency) form.currency = bank.currency
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const data = { ...form, transferDate: form.transferDate }
    if (typeof data.transferDate === 'object') {
      data.transferDate = data.transferDate.toISOString().split('T')[0]
    }
    await api.post('/admin/finance/banks/transfer', data)
    ElMessage.success('转账成功')
    visible.value = false
    emit('success')
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '转账失败')
  } finally { saving.value = false }
}
</script>
