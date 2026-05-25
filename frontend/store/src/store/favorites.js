import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useFavoritesStore = defineStore('favorites', () => {
  const items = ref(JSON.parse(localStorage.getItem('favorites') || '[]'))

  const totalCount = computed(() => items.value.length)

  function isFavorite(productId) {
    return items.value.some(i => i.id === productId)
  }

  function toggle(product) {
    const idx = items.value.findIndex(i => i.id === product.id)
    if (idx >= 0) {
      items.value.splice(idx, 1)
    } else {
      items.value.push({ id: product.id, title: product.title, price: product.price })
    }
    save()
  }

  function remove(productId) {
    items.value = items.value.filter(i => i.id !== productId)
    save()
  }

  function save() { localStorage.setItem('favorites', JSON.stringify(items.value)) }

  return { items, totalCount, isFavorite, toggle, remove }
})
