<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header><h2>{{ $t('seller.title') }} - Login</h2></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login" :loading="loading">{{ $t('common.submit') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = { username: [{ required: true, message: 'Required', trigger: 'blur' }], password: [{ required: true, message: 'Required', trigger: 'blur' }] }

async function login() {
  const valid = await formRef.value.validate()
  if (!valid) return
  loading.value = true
  try {
    const res = await api.post('/auth/login', form)
    localStorage.setItem('token', res.data.token)
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style>
.login-page { display: flex; justify-content: center; align-items: center; height: 100vh; }
.login-card { width: 400px; }
</style>
