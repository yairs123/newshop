<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header><h3>{{ $t('auth.forgotPasswordTitle') }}</h3></template>
      <p style="margin-bottom: 16px; color: #6b7280;">{{ $t('auth.forgotPasswordDesc') }}</p>
      <el-form v-if="!sent" @submit.prevent="submit">
        <el-form-item :label="$t('common.email')">
          <el-input v-model="email" type="email" placeholder="your@email.com" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading">{{ $t('auth.resetPasswordSend') }}</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-else type="success" :description="$t('auth.forgotPasswordSent')" show-icon />
      <div style="text-align: center; margin-top: 16px;">
        <router-link to="/auth" style="font-size: 13px; color: #3b82f6;">{{ $t('auth.backToLogin') }}</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'

const email = ref('')
const loading = ref(false)
const sent = ref(false)

async function submit() {
  if (!email.value) return
  loading.value = true
  try {
    await api.post('/auth/forgot-password', { email: email.value })
    sent.value = true
  } finally {
    loading.value = false
  }
}
</script>
