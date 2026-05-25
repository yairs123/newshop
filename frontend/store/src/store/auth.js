import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)

  function setToken(t) {
    token.value = t
    localStorage.setItem('token', t)
    window.dispatchEvent(new Event('storage'))
  }

  function setUser(u) { user.value = u }
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, setToken, setUser, logout }
})
