import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({ baseURL: '/api', timeout: 15000 })

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
    ElMessage.error(error.response?.data?.message || error.message)
    if (error.response?.status === 401 && !isAuthRequest) {
      localStorage.removeItem('token')
      window.location.href = '/auth'
    }
    return Promise.reject(error)
  }
)

export { api }
