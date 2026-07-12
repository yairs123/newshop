<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- Left: Brand/hero -->
      <div class="auth-hero">
        <div class="hero-content">
          <div class="hero-logo">
            <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
              <circle cx="28" cy="28" r="26" fill="url(#auth-logo)" />
              <text x="28" y="34" text-anchor="middle" fill="#fff" font-size="26" font-weight="800" font-family="serif">C</text>
              <defs>
                <linearGradient id="auth-logo" x1="0" y1="0" x2="56" y2="56">
                  <stop offset="0%" stop-color="#f59e0b" />
                  <stop offset="100%" stop-color="#b45309" />
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="hero-title">CoinMarket</h1>
          <p class="hero-subtitle">{{ $t('footer.about') }}</p>
          <div class="hero-features">
            <div class="hero-feature">
              <span class="feature-icon">&#x1F6E1;&#xFE0F;</span>
              <div>
                <strong>{{ $t('auth.featureSecure', '安全加密交易') }}</strong>
                <p>{{ $t('auth.featureSecureDesc', '256-bit SSL + 双因素认证') }}</p>
              </div>
            </div>
            <div class="hero-feature">
              <span class="feature-icon">&#x1F4E6;</span>
              <div>
                <strong>{{ $t('auth.featureShipping', '全球配送') }}</strong>
                <p>{{ $t('auth.featureShippingDesc', '安全包装，追踪保障') }}</p>
              </div>
            </div>
            <div class="hero-feature">
              <span class="feature-icon">&#x1F50D;</span>
              <div>
                <strong>{{ $t('auth.featureAuthentic', '正品保证') }}</strong>
                <p>{{ $t('auth.featureAuthenticDesc', '所有藏品经专业鉴定') }}</p>
              </div>
            </div>
          </div>
          <div class="hero-testimonial">
            <p>"{{ $t('auth.community', '加入超过10,000名收藏家的社区') }}"</p>
          </div>
        </div>
      </div>

      <!-- Right: Auth forms -->
      <div class="auth-form-panel">
        <div class="auth-form-inner">

          <!-- ===== 登录 ===== -->
          <template v-if="mode === 'login' || mode === 'register'">
            <div class="form-header">
              <h2>{{ mode === 'login' ? $t('nav.login') : $t('nav.register') }}</h2>
              <p class="form-desc">
                <template v-if="mode === 'login'">{{ $t('auth.welcomeBack', '欢迎回来') }}</template>
                <template v-else>{{ $t('auth.joinUs', '加入我们') }}</template>
              </p>
            </div>

            <!-- Step 1: Username / Email -->
            <template v-if="authStep === 1">
              <div class="step-indicator">{{ $t('auth.step', '步骤') }} 1/2</div>
              <el-form @submit.prevent="nextStep" class="auth-form">
                <el-form-item>
                  <el-input
                    v-model="username"
                    size="large"
                    :placeholder="$t('common.username', '用户名/邮箱')"
                    autofocus
                    @keyup.enter="nextStep"
                  >
                    <template #prefix><span style="color:#9ca3af">&#x1F464;</span></template>
                  </el-input>
                </el-form-item>
                <el-button type="primary" size="large" class="auth-btn" @click="nextStep">
                  {{ $t('common.next', '下一步') }}
                </el-button>
              </el-form>
            </template>

            <!-- Step 2: Password (login) or full form (register) -->
            <template v-if="authStep === 2">
              <template v-if="mode === 'login'">
                <div class="step-indicator">{{ $t('auth.step', '步骤') }} 2/2</div>
                <el-form @submit.prevent="handleLogin" class="auth-form">
                  <div class="username-display">
                    <span>{{ username }}</span>
                    <button class="change-btn" @click="authStep = 1">{{ $t('auth.change', '更换') }}</button>
                  </div>
                  <el-form-item>
                    <el-input
                      v-model="password"
                      type="password"
                      size="large"
                      :placeholder="$t('common.password', '密码')"
                      show-password
                      @keyup.enter="handleLogin"
                    >
                      <template #prefix><span style="color:#9ca3af">&#x1F512;</span></template>
                    </el-input>
                  </el-form-item>

                  <div class="form-options">
                    <el-checkbox v-model="rememberMe">{{ $t('auth.rememberMe', '记住我') }}</el-checkbox>
                    <router-link to="/forgot-password" class="forgot-link">{{ $t('auth.forgotPassword', '忘记密码？') }}</router-link>
                  </div>

                  <el-button type="primary" size="large" class="auth-btn" :loading="loading" @click="handleLogin">
                    {{ $t('common.login', '登录') }}
                  </el-button>
                </el-form>
              </template>

              <template v-if="mode === 'register'">
                <div class="step-indicator">{{ $t('auth.step', '步骤') }} 2/2</div>
                <el-form @submit.prevent="handleRegister" class="auth-form">
                  <el-form-item>
                    <el-input v-model="regUsername" size="large" :placeholder="$t('common.username', '用户名')" autofocus>
                      <template #prefix><span style="color:#9ca3af">&#x1F464;</span></template>
                    </el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-input v-model="regEmail" size="large" type="email" :placeholder="$t('common.email', '邮箱')">
                      <template #prefix><span style="color:#9ca3af">&#x2709;&#xFE0F;</span></template>
                    </el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-input v-model="regPassword" type="password" size="large" show-password :placeholder="$t('common.password', '密码')">
                      <template #prefix><span style="color:#9ca3af">&#x1F512;</span></template>
                    </el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-select v-model="regLang" style="width:100%">
                      <el-option label="English" value="en" />
                      <el-option label="&#x7B80;&#x4F53;&#x4E2D;&#x6587;" value="zh-CN" />
                      <el-option label="&#x7E41;&#x9AD4;&#x4E2D;&#x6587;" value="zh-TW" />
                      <el-option label="&#x65E5;&#x672C;&#x8A9E;" value="ja" />
                    </el-select>
                  </el-form-item>
                  <el-button type="primary" size="large" class="auth-btn" :loading="loading" @click="handleRegister">
                    {{ $t('nav.register') }}
                  </el-button>
                </el-form>
              </template>
            </template>



            <!-- Switch login/register -->
            <div class="form-footer">
              <span v-if="mode === 'login'">
                {{ $t('auth.noAccount', '还没有账号？') }}
                <button class="link-btn" @click="switchMode('register')">{{ $t('nav.register') }}</button>
              </span>
              <span v-if="mode === 'register'">
                {{ $t('auth.hasAccount', '已有账号？') }}
                <button class="link-btn" @click="switchMode('login')">{{ $t('nav.login') }}</button>
              </span>
            </div>
          </template>

          <!-- ===== 2FA Code ===== -->
          <template v-if="mode === '2fa'">
            <div class="form-header">
              <h2>&#x1F510; {{ $t('auth.twoFactor', '双因素认证') }}</h2>
              <p class="form-desc">{{ $t('auth.enterCode', '请输入发送到您邮箱的验证码') }}</p>
            </div>
            <div class="code-input-section">
              <el-input
                v-model="twoFactorCode"
                size="large"
                :placeholder="$t('auth.sixDigitCode', '6位验证码')"
                maxlength="6"
                class="code-input"
                @keyup.enter="verify2FA"
              />
              <div class="code-timer" v-if="codeTimer > 0">{{ $t('auth.resendIn', '重新发送') }} {{ codeTimer }}s</div>
              <el-button type="primary" size="large" class="auth-btn" :loading="loading" @click="verify2FA">
                {{ $t('auth.verify', '验证') }}
              </el-button>
            </div>
          </template>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { useAuthStore } from '../store/auth'
import { useCartStore } from '../store/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const authStore = useAuthStore()
const cartStore = useCartStore()

// Mode: 'login' | 'register' | '2fa'
const mode = ref('login')
const authStep = ref(1)
const loading = ref(false)

// Login
const username = ref('')
const password = ref('')
const rememberMe = ref(true)

// Register
const regUsername = ref('')
const regEmail = ref('')
const regPassword = ref('')
const regLang = ref('en')

// 2FA
const twoFactorCode = ref('')
const twoFactorToken = ref('')
const codeTimer = ref(0)
let codeTimerInterval = null

watch(function() { return route.query.tab }, function(tab) {
  if (tab === 'register') {
    mode.value = 'register'
  } else {
    mode.value = 'login'
  }
  authStep.value = 1
  username.value = ''
  password.value = ''
})

onUnmounted(() => {
  if (codeTimerInterval) clearInterval(codeTimerInterval)
})

function switchMode(m) {
  mode.value = m
  authStep.value = 1
  username.value = ''
  password.value = ''
}

function nextStep() {
  if (!username.value) {
    ElMessage.warning(t('auth.enterUsername', '请输入用户名或邮箱'))
    return
  }
  if (mode.value === 'register') {
    regUsername.value = username.value
    authStep.value = 2
    return
  }
  // mode === 'login'
  authStep.value = 2
}

async function handleLogin() {
  if (!password.value) {
    ElMessage.warning(t('common.enterPassword', '请输入密码'))
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/login', {
      username: username.value,
      password: password.value
    })
    const data = res.data || {}

    // If 2FA required
    if (data.require2fa) {
      mode.value = '2fa'
      twoFactorToken.value = data.verifyToken || ''
      startCodeTimer()
      return
    }

    // Normal login
    authStore.setToken(data.token)
    await cartStore.mergeLocalToServer()
    router.push('/')
  } catch (e) { /* handled by interceptor */ }
  finally { loading.value = false }
}

async function handleRegister() {
  if (!regUsername.value || !regEmail.value || !regPassword.value) {
    ElMessage.warning(t('auth.fillAll', '请填写所有字段'))
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/register', {
      username: regUsername.value,
      email: regEmail.value,
      password: regPassword.value,
      preferredLanguage: regLang.value
    })
    authStore.setToken(res.data.token)
    router.push('/')
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function startCodeTimer() {
  codeTimer.value = 60
  if (codeTimerInterval) clearInterval(codeTimerInterval)
  codeTimerInterval = setInterval(() => {
    codeTimer.value--
    if (codeTimer.value <= 0) {
      clearInterval(codeTimerInterval)
    }
  }, 1000)
}

async function verify2FA() {
  if (!twoFactorCode.value || twoFactorCode.value.length < 6) {
    ElMessage.warning(t('auth.enterValidCode', '请输入6位验证码'))
    return
  }
  loading.value = true
  try {
    const res = await api.post('/auth/verify-2fa', {
      code: twoFactorCode.value,
      verifyToken: twoFactorToken.value
    })
    if (res.data?.verified) {
      // Re-login with full 2fa verified token
      const loginRes = await api.post('/auth/login', {
        username: username.value,
        password: password.value,
        twoFactorCode: twoFactorCode.value
      })
      authStore.setToken(loginRes.data.token)
      await cartStore.mergeLocalToServer()
      router.push('/')
    }
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fefce8 0%, #fef3c7 30%, #fffbeb 60%, #fff 100%);
  padding: 24px;
}

.auth-container {
  display: flex;
  max-width: 1000px;
  width: 100%;
  min-height: 600px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08), 0 8px 24px rgba(180,83,9,0.06);
  overflow: hidden;
}

/* ===== Left Hero ===== */
.auth-hero {
  flex: 1;
  background: linear-gradient(160deg, #111827, #1e1e2d, #111827);
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-hero::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(245,158,11,0.06) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(245,158,11,0.04) 0%, transparent 50%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.hero-logo { margin-bottom: 16px; }

.hero-title {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}

.hero-subtitle {
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 40px;
  line-height: 1.5;
}

.hero-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  text-align: left;
  max-width: 280px;
  margin: 0 auto 40px;
}

.hero-feature {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.feature-icon { font-size: 20px; flex-shrink: 0; margin-top: 2px; }

.hero-feature strong {
  display: block;
  color: #fff;
  font-size: 14px;
  margin-bottom: 2px;
}

.hero-feature p {
  color: #6b7280;
  font-size: 12px;
  margin: 0;
}

.hero-testimonial {
  border-top: 1px solid #1f2937;
  padding-top: 20px;
}

.hero-testimonial p {
  color: #6b7280;
  font-size: 13px;
  font-style: italic;
  margin: 0;
}

/* ===== Right Form Panel ===== */
.auth-form-panel {
  width: 460px;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-form-inner {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 32px;
  text-align: center;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
}

.form-desc {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.step-indicator {
  font-size: 12px;
  color: #f59e0b;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 16px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 14px;
  box-shadow: 0 0 0 1px #e5e7eb !important;
}

.auth-form :deep(.el-input__wrapper):hover,
.auth-form :deep(.el-input__wrapper).is-focus {
  box-shadow: 0 0 0 1px #f59e0b !important;
}

.auth-form :deep(.el-form-item) { margin-bottom: 16px; }

.username-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #f9fafb;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 16px;
}

.change-btn {
  background: none;
  border: none;
  color: #f59e0b;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
}

.change-btn:hover { text-decoration: underline; }

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 20px;
}

.forgot-link {
  color: #f59e0b;
  font-size: 13px;
  text-decoration: none;
}

.forgot-link:hover { text-decoration: underline; }

.auth-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  border: none !important;
}

.auth-btn:hover {
  background: linear-gradient(135deg, #d97706, #b45309) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245,158,11,0.3);
}

/* Form footer */
.form-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}

.link-btn {
  background: none;
  border: none;
  color: #f59e0b;
  font-weight: 600;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.link-btn:hover { text-decoration: underline; }

/* 2FA */
.code-input-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.code-input {
  max-width: 260px;
  font-size: 24px;
  letter-spacing: 8px;
  text-align: center;
}

.code-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.code-timer {
  font-size: 12px;
  color: #9ca3af;
}

/* Responsive */
@media (max-width: 768px) {
  .auth-hero { display: none; }
  .auth-form-panel { width: 100%; padding: 32px 24px; }
  .auth-page { padding: 16px; }
  .auth-container { min-height: auto; border-radius: 16px; }
}
</style>
