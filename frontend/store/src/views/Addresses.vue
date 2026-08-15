<template>
  <div class="addresses-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/account' }">{{ $t('account.myAccount') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ $t('account.addresses') }}</el-breadcrumb-item>
    </el-breadcrumb>
    <h2>{{ $t('account.addresses') }}</h2>
    <el-button type="primary" style="margin-bottom:16px" @click="showAddDialog">{{ $t('account.addAddress') }}</el-button>

    <div v-loading="loading">
      <el-empty v-if="addresses.length === 0" :description="$t('account.noAddresses')" />
      <el-table v-else :data="addresses" border stripe>
        <el-table-column prop="fullName" :label="$t('common.fullName')" width="150" />
        <el-table-column prop="phone" :label="$t('common.phone')" width="130" />
        <el-table-column prop="address" :label="$t('common.address')" />
        <el-table-column prop="city" :label="$t('common.city')" width="120" />
        <el-table-column prop="zipCode" :label="$t('common.zipCode')" width="100" />
        <el-table-column prop="country" :label="$t('common.country')" width="120" />
        <el-table-column :label="$t('common.default')" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">{{ $t('account.defaultAddress') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.action')" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editAddress(row)">{{ $t('account.editAddress') }}</el-button>
            <el-button size="small" type="danger" @click="deleteAddress(row)">{{ $t('account.deleteAddress') }}</el-button>
            <el-button v-if="!row.isDefault" size="small" text @click="setDefault(row)">{{ $t('account.setDefault') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEditing ? $t('account.editAddress') : $t('account.addAddress')" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item :label="$t('common.fullName')">
          <el-input v-model="form.fullName" />
        </el-form-item>
        <el-form-item :label="$t('common.phone')">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item :label="$t('common.country')">
          <el-input v-model="form.country" />
        </el-form-item>
        <el-form-item :label="$t('common.city')">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item :label="$t('common.zipCode')">
          <el-input v-model="form.zipCode" />
        </el-form-item>
        <el-form-item :label="$t('common.address')">
          <el-input v-model="form.address" type="textarea" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">{{ $t('account.setDefault') }}</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveAddress">{{ $t('common.submit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const addresses = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const form = reactive({ fullName: '', phone: '', country: '', city: '', zipCode: '', address: '', isDefault: false })

onMounted(fetchAddresses)

async function fetchAddresses() {
  try {
    const res = await api.get('/addresses')
    addresses.value = res.data || []
  } catch (e) { addresses.value = [] }
  loading.value = false
}

function showAddDialog() {
  isEditing.value = false
  editingId.value = null
  form.fullName = ''; form.phone = ''; form.country = ''; form.city = ''; form.zipCode = ''; form.address = ''; form.isDefault = false
  dialogVisible.value = true
}

function editAddress(row) {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(form, { fullName: row.fullName, phone: row.phone, country: row.country, city: row.city, zipCode: row.zipCode, address: row.address, isDefault: row.isDefault })
  dialogVisible.value = true
}

async function saveAddress() {
  try {
    if (isEditing.value) {
      await api.put(`/addresses/${editingId.value}`, form)
    } else {
      await api.post('/addresses', form)
    }
    dialogVisible.value = false
    ElMessage.success(t('common.success'))
    fetchAddresses()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

async function deleteAddress(row) {
  try {
    await ElMessageBox.confirm(t('account.confirmDelete'))
    await api.delete(`/addresses/${row.id}`)
    ElMessage.success(t('common.deleted'))
    fetchAddresses()
  } catch (e) {}
}

async function setDefault(row) {
  try {
    await api.put(`/addresses/${row.id}/default`)
    ElMessage.success(t('common.defaultSet'))
    fetchAddresses()
  } catch (e) {}
}
</script>

<style scoped>
.addresses-page { max-width: 1000px; margin: 0 auto; padding: 24px; }
h2 { margin-bottom: 16px; }
.empty { text-align: center; padding: 60px; color: #9ca3af; }
</style>
