import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../api'

export const useCartStore = defineStore('cart', () => {
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))
  const synced = ref(false)

  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

  function isLoggedIn() {
    return !!localStorage.getItem('token')
  }

  async function addItem(product, quantity = 1) {
    if (isLoggedIn()) {
      try {
        await api.post('/cart', { productId: product.id, quantity })
        await syncFromServer()
        return
      } catch (_) {}
    }
    // Fallback to local
    const existing = items.value.find(i => i.id === product.id)
    if (existing) {
      existing.quantity += quantity
    } else {
      items.value.push({
        id: product.id, title: product.title, price: product.price,
        image: product.primaryImage, quantity,
        country: product.country, year: product.year, material: product.material,
        ratingCompany: product.ratingCompany, ratingGrade: product.ratingGrade,
      })
    }
    saveLocal()
  }

  async function removeItem(productId) {
    if (isLoggedIn()) {
      try {
        await api.delete(`/cart/${productId}`)
        await syncFromServer()
        return
      } catch (_) {}
    }
    items.value = items.value.filter(i => i.id !== productId)
    saveLocal()
  }

  async function clear() {
    if (isLoggedIn()) {
      try {
        await api.delete('/cart')
      } catch (_) {}
    }
    items.value = []
    saveLocal()
  }

  function save() {
    saveLocal()
  }

  function saveLocal() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  async function syncFromServer() {
    if (!isLoggedIn()) return
    try {
      const res = await api.get('/cart')
      const serverItems = (res.data || []).map(i => ({
        id: i.productId,
        title: i.title,
        price: i.price,
        image: i.image,
        quantity: i.quantity,
        country: i.country,
        year: i.year,
        material: i.material,
        ratingCompany: i.ratingCompany,
        ratingGrade: i.ratingGrade,
      }))
      items.value = serverItems
      synced.value = true
      saveLocal()
    } catch (_) {}
  }

  async function mergeLocalToServer() {
    const localItems = JSON.parse(localStorage.getItem('cart') || '[]')
    if (localItems.length === 0) {
      await syncFromServer()
      return
    }
    try {
      await api.post('/cart/merge', localItems.map(i => ({
        productId: i.id,
        quantity: i.quantity
      })))
      localStorage.removeItem('cart')
      await syncFromServer()
    } catch (_) {}
  }

  return { items, totalCount, totalAmount, synced, addItem, removeItem, clear, save, syncFromServer, mergeLocalToServer }
})
