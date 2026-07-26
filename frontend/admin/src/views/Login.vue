<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">
          <svg viewBox="0 0 48 48" width="48" height="48">
            <defs>
              <linearGradient id="lg" x1="0" y1="0" x2="48" y2="48">
                <stop offset="0%" stop-color="#d4a843"/>
                <stop offset="100%" stop-color="#b8932a"/>
              </linearGradient>
            </defs>
            <circle cx="24" cy="24" r="22" fill="url(#lg)"/>
            <circle cx="24" cy="24" r="17" fill="none" stroke="#fff" stroke-width="1.5" opacity="0.3"/>
            <text x="24" y="24" text-anchor="middle" dominant-baseline="central"
                  font-size="22" font-weight="bold" fill="#fff" font-family="Georgia">$</text>
          </svg>
        </div>
        <h1 class="login-title">CoinMarket</h1>
        <p class="login-subtitle">管理后台</p>
      </div>

      <el-form @submit.prevent="login" class="login-form">
        <div class="input-group">
          <label class="input-label">用户名</label>
          <div class="input-wrap">
            <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <input v-model="username" type="text" placeholder="请输入用户名" class="input-field" />
          </div>
        </div>
        <div class="input-group">
          <label class="input-label">密码</label>
          <div class="input-wrap">
            <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            <input v-model="password" type="password" placeholder="请输入密码" class="input-field" @keyup.enter="login" />
          </div>
        </div>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
        <div v-if="error" class="login-error">{{ error }}</div>
      </el-form>

      <div class="login-footer">
        <el-dropdown @command="switchLanguage" trigger="click">
          <button class="lang-btn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
            <span>{{ currentLangLabel }}</span>
          </button>
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
import axios from 'axios'

const router = useRouter()
const { locale } = useI18n()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const currentLangLabel = computed(() => {
  const labels = { en: 'EN', 'zh-CN': '简体', 'zh-TW': '繁體', ja: '日語', ko: '한국어' }
  return labels[locale.value] || locale.value
})

function switchLanguage(lang) {
  locale.value = lang
  localStorage.setItem('language', lang)
}

async function login() {
  if (!username.value || !password.value) { error.value = '请输入用户名和密码'; return }
  loading.value = true; error.value = ''
  try {
    const res = await axios.post('/api/auth/login', { username: username.value, password: password.value })
    const data = res.data.data
    localStorage.setItem('token', data.token)
    router.push('/dashboard')
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: var(--ink, #0f172a);
  position: relative; overflow: hidden;
}
.login-bg {
  position: absolute; inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(212,168,67,.08) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 50%, rgba(212,168,67,.05) 0%, transparent 60%),
    radial-gradient(ellipse at 50% 100%, rgba(255,255,255,.03) 0%, transparent 50%);
}
.login-card {
  position: relative; z-index: 1;
  width: 400px; max-width: 90vw;
  background: rgba(255,255,255,.96);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0,0,0,.3);
}
.login-brand { text-align: center; margin-bottom: 32px; }
.login-logo { margin-bottom: 12px; }
.login-title {
  font-family: 'DM Serif Display', Georgia, serif;
  font-size: 26px; font-weight: 700; color: var(--ink, #0f172a);
  margin-bottom: 4px;
}
.login-subtitle {
  font-size: 13px; color: var(--text-muted, #94a3b8);
  letter-spacing: 0.03em; text-transform: uppercase;
}

.login-form { display: flex; flex-direction: column; gap: 20px; }
.input-group { display: flex; flex-direction: column; gap: 6px; }
.input-label { font-size: 12px; font-weight: 600; color: #374151; text-transform: uppercase; letter-spacing: 0.04em; }
.input-wrap {
  display: flex; align-items: center; gap: 10px;
  padding: 0 14px; border-radius: 10px;
  border: 1.5px solid #e5e7eb; background: #fff;
  transition: border-color 0.15s;
}
.input-wrap:focus-within { border-color: var(--gold, #d4a843); box-shadow: 0 0 0 3px rgba(212,168,67,.1); }
.input-icon { color: #9ca3af; flex-shrink: 0; }
.input-field {
  flex: 1; height: 46px; border: none; outline: none;
  font-size: 15px; font-family: inherit; color: var(--text, #1e293b);
  background: transparent;
}
.input-field::placeholder { color: #cbd5e1; }

.login-btn {
  height: 48px; border-radius: 10px; border: none;
  background: linear-gradient(135deg, #d4a843, #b8932a);
  color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: opacity 0.15s; font-family: inherit;
  letter-spacing: 0.05em;
}
.login-btn:hover { opacity: 0.92; }
.login-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.login-error {
  background: #fef2f2; color: #dc2626;
  padding: 10px 14px; border-radius: 8px;
  font-size: 13px; text-align: center;
}
.login-footer { text-align: center; margin-top: 24px; }
.lang-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 12px; border: 1px solid #e5e7eb; border-radius: 8px;
  background: #fff; font-size: 12px; color: #9ca3af;
  cursor: pointer; transition: all 0.15s; font-family: inherit;
}
.lang-btn:hover { border-color: var(--gold, #d4a843); color: var(--gold-dark, #b8932a); }
</style>
