<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <el-tabs v-model="tab">
          <el-tab-pane label="Login" name="login" />
          <el-tab-pane label="Register" name="register" />
        </el-tabs>
      </template>
      <el-form v-if="tab === 'login'" ref="loginForm" :model="loginData" :rules="rules" label-width="80px">
        <el-form-item label="Username" prop="username"><el-input v-model="loginData.username" /></el-form-item>
        <el-form-item label="Password" prop="password"><el-input v-model="loginData.password" type="password" /></el-form-item>
        <el-form-item><el-button type="primary" @click="login" :loading="loading">Login</el-button></el-form-item>
        <div style="text-align: center; margin-top: -16px;">
          <router-link to="/forgot-password" style="font-size: 13px; color: #3b82f6; text-decoration: none;">Forgot Password?</router-link>
        </div>
      </el-form>
      <el-form v-else ref="regForm" :model="regData" :rules="regRules" label-width="100px">
        <el-form-item label="Username" prop="username"><el-input v-model="regData.username" /></el-form-item>
        <el-form-item label="Email" prop="email"><el-input v-model="regData.email" /></el-form-item>
        <el-form-item label="Password" prop="password"><el-input v-model="regData.password" type="password" /></el-form-item>
        <el-form-item label="Language" prop="preferredLanguage">
          <el-select v-model="regData.preferredLanguage">
            <el-option label="English" value="en" />
            <el-option label="简体中文" value="zh-CN" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="register" :loading="loading">Register</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { useAuthStore } from '../store/auth'
import { useCartStore } from '../store/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const tab = ref('login')
const loading = ref(false)

const loginData = reactive({ username: '', password: '' })
const rules = { username: [{ required: true }], password: [{ required: true }] }

const regData = reactive({ username: '', email: '', password: '', preferredLanguage: 'en' })
const regRules = { username: [{ required: true }], email: [{ required: true, type: 'email' }], password: [{ required: true, min: 8 }], preferredLanguage: [{ required: true }] }

async function login() {
  loading.value = true
  try {
    const res = await api.post('/auth/login', loginData)
    authStore.setToken(res.data.token)
    await cartStore.mergeLocalToServer()
    router.push('/')
  } finally { loading.value = false }
}

async function register() {
  loading.value = true
  try {
    const res = await api.post('/auth/register', regData)
    authStore.setToken(res.data.token)
    router.push('/')
  } finally { loading.value = false }
}
</script>

<style>
.auth-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
.auth-card { width: 450px; }
</style>
