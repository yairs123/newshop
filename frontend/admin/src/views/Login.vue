<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>CoinMarket Admin</h2>
      <el-form @submit.prevent="login">
        <el-form-item>
          <el-input v-model="username" placeholder="Username" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="Password" show-password @keyup.enter="login" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login" :loading="loading" style="width:100%">Login</el-button>
        </el-form-item>
        <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function login() {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.post('/api/auth/login', {
      username: username.value,
      password: password.value
    })
    const data = res.data.data
    const roles = data.user?.roles || data.roles || []
    if (!roles.includes('ROLE_ADMIN')) {
      error.value = 'Access denied: admin role required'
      return
    }
    localStorage.setItem('token', data.token)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f2f5; }
.login-card { width: 400px; }
.login-card h2 { text-align: center; margin-bottom: 24px; }
</style>
