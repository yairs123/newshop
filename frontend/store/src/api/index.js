import axios from 'axios'
import { ElMessage } from 'element-plus'
import i18n from '../i18n'

const api = axios.create({ baseURL: '/api', timeout: 15000 })

// 将后端英文错误消息翻译为当前界面语言
function translateError(msg) {
  if (!msg) return i18n.global.t('errors.requestFailed')
  try {
    const locale = i18n.global.locale.value
    const map = i18n.global.getLocaleMessage(locale)?.errors || {}
    return map[msg] || msg
  } catch (e) {
    return msg
  }
}

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  response => response.data,
  error => {
    const url = error.config?.url || ''
    // 登录/注册请求的 401 是"账号密码错误"，不应跳转刷新页面
    const isAuthRequest = url.includes('/auth/login') || url.includes('/auth/register')
    ElMessage.error(translateError(error.response?.data?.message || error.message))
    if (error.response?.status === 401 && !isAuthRequest) {
      localStorage.removeItem('token')
      window.location.href = '/auth'
    }
    return Promise.reject(error)
  }
)

export { api }
