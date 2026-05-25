<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header><h3>{{ $t('auth.resetPasswordTitle') }}</h3></template>
      <el-form v-if="!done" @submit.prevent="submit">
        <el-form-item :label="$t('auth.resetPasswordNewPassword')">
          <el-input v-model="password" type="password" minlength="8" />
        </el-form-item>
        <el-form-item :label="$t('auth.resetPasswordConfirmPassword')">
          <el-input v-model="confirm" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading">{{ $t('common.submit') }}</el-button>
        </el-form-item>
        <div v-if="error" style="color: #ef4444; font-size: 13px;">{{ error }}</div>
      </el-form>
      <template v-else>
        <el-alert type="success" :description="$t('auth.resetPasswordSuccess')" show-icon />
        <div style="text-align: center; margin-top: 16px;">
          <router-link to="/auth" style="font-size: 13px; color: #3b82f6;">{{ $t('auth.backToLogin') }}</router-link>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const token = route.query.token || ''
const password = ref('')
const confirm = ref('')
const loading = ref(false)
const done = ref(false)
const error = ref('')

async function submit() {
  if (password.value.length < 8) { error.value = 'Password must be at least 8 characters'; return }
  if (password.value !== confirm.value) { error.value = 'Passwords do not match'; return }
  loading.value = true; error.value = ''
  try {
    await api.post('/auth/reset-password', { token, newPassword: password.value })
    done.value = true
  } catch (e) {
    error.value = e.response?.data?.message || 'Reset failed. The link may be expired.'
  } finally {
    loading.value = false
  }
}
</script>
