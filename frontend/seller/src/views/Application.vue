<template>
  <div class="application-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item>Application</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card v-if="!hasApplied">
      <template #header><h2>{{ $t('seller.application.title') }}</h2></template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item :label="$t('seller.application.shopName')" prop="shopName">
          <el-input v-model="form.shopName" />
        </el-form-item>
        <el-form-item :label="$t('seller.application.shopDescription')" prop="shopDescription">
          <el-input v-model="form.shopDescription" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitApplication">{{ $t('common.submit') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card v-else>
      <el-result :icon="statusIcon" :title="statusText">
        <template #extra>
          <el-button v-if="status === 'REJECTED'" type="primary" @click="reapply">{{ $t('seller.application.title') }}</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const formRef = ref(null)
const hasApplied = ref(false)
const status = ref('')

const form = reactive({ shopName: '', shopDescription: '', idDocumentUrl: '' })
const rules = { shopName: [{ required: true, message: 'Required', trigger: 'blur' }] }

const statusIcon = computed(() => status.value === 'APPROVED' ? 'success' : status.value === 'REJECTED' ? 'error' : 'info')
const statusText = computed(() => status.value === 'PENDING' ? '审核中' : status.value === 'APPROVED' ? '已通过' : status.value === 'REJECTED' ? '未通过' : '')

async function submitApplication() {
  const valid = await formRef.value.validate()
  if (!valid) return
  try {
    await api.post('/seller/apply', form)
    hasApplied.value = true
    status.value = 'PENDING'
  } catch (e) {}
}

function reapply() {
  hasApplied.value = false
  status.value = ''
}

async function loadStatus() {
  try {
    const res = await api.get('/seller/status')
    const data = res.data
    if (data) {
      if (data.hasProfile) {
        // Already a seller with a profile — go to dashboard
        router.push('/dashboard')
        return
      }
      if (data.hasApplied) {
        hasApplied.value = true
        status.value = data.status
      }
    }
  } catch (e) {}
}

onMounted(loadStatus)
</script>
