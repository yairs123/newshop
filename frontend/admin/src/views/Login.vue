<template>
  <div class="login-wrapper">
    <div class="login-bg"></div>
    <div class="login-card">
      <div class="login-header">
        <svg viewBox="0 0 40 40" width="48" height="48">
          <circle cx="20" cy="20" r="18" fill="#f59e0b" stroke="#b45309" stroke-width="2"/>
          <text x="20" y="20" text-anchor="middle" dominant-baseline="central"
                font-size="16" font-weight="bold" fill="#fff" font-family="Arial">$</text>
        </svg>
        <h2>{{ $t('admin.title') }}</h2>
        <p class="login-subtitle">{{ $t('admin.loginSubtitle', '管理后台') }}</p>
      </div>
      <el-form @submit.prevent="login" class="login-form">
        <el-form-item>
          <el-input
            v-model="username"
            :placeholder="$t('admin.username', '用户名')"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="password"
            type="password"
            :placeholder="$t('admin.password', '密码')"
            size="large"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="login"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login" :loading="loading" size="large" class="login-btn">
            {{ $t('common.login', '登录') }}
          </el-button>
        </el-form-item>
        <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="login-error" />
      </el-form>
      <div class="login-footer">
        <el-dropdown @command="switchLanguage">
          <span class="lang-link">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
            {{ currentLangLabel }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="en">English</el-dropdown-item>
              <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
              <el-dropdown-item command="zh-TW">繁體中文</el-dropdown-item>
              <el-dropdown-item command="ja">日本語</el-dropdown-item>
              <el-dropdown-item command="ko">한국어</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { User, Lock } from '@element-plus/icons-vue'
import axios from 'axios'

const router = useRouter()
const { locale } = useI18n()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const currentLangLabel = computed(() => {
  const labels = { en: 'English', 'zh-CN': '简体中文', 'zh-TW': '繁體中文', ja: '日本語', ko: '한국어' }
  return labels[locale.value] || locale.value
})

function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}

async function login() {
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
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
      error.value = '权限不足：需要管理员角色'
      return
    }
    localStorage.setItem('token', data.token)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e1e2d 0%, #2d2d44 50%, #1a1a2e 100%);
  position: relative;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 40%, rgba(64,158,255,0.08) 0%, transparent 50%),
              radial-gradient(circle at 70% 60%, rgba(245,158,11,0.06) 0%, transparent 50%);
  animation: bgShift 20s ease-in-out infinite alternate;
}
@keyframes bgShift {
  0% { transform: translate(0, 0) rotate(0deg); }
  100% { transform: translate(-5%, -5%) rotate(5deg); }
}
.login-card {
  position: relative;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 40px 36px;
  width: 420px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1e1e2d;
  margin-top: 12px;
}
.login-subtitle {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.login-form { margin-bottom: 16px; }
.login-btn {
  width: 100%;
  font-size: 16px;
  height: 48px;
  border-radius: 8px;
}
.login-error { margin-top: 12px; }
.login-footer {
  text-align: center;
}
.lang-link {
  font-size: 13px;
  color: #909399;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.lang-link:hover { color: #409eff; }
</style>
