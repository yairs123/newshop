import { ref, reactive, computed } from 'vue'
import { api } from '../api'

export function useProductSearch(initialQuery = {}) {
  const products = ref([])
  const categories = ref([])
  const total = ref(0)
  const loading = ref(false)
  const page = ref(initialQuery.page || 1)
  const size = ref(initialQuery.size || 40)
  const sort = ref(initialQuery.sort || 'createdAt,desc')

  const filters = reactive({
    keyword: initialQuery.keyword || '',
    categoryId: initialQuery.categoryId || null,
    ratingCompany: initialQuery.ratingCompany || null,
    country: initialQuery.country || null,
    minPrice: initialQuery.minPrice || null,
    maxPrice: initialQuery.maxPrice || null
  })

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

  async function loadCategories() {
    try {
      const res = await api.get('/products/categories')
      categories.value = res.data || []
    } catch (error) {
      categories.value = []
    }
  }

  async function loadProducts() {
    loading.value = true
    const params = { page: page.value - 1, size: size.value }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.ratingCompany) params.ratingCompany = filters.ratingCompany
    if (filters.country) params.country = filters.country
    if (filters.minPrice) params.minPrice = filters.minPrice
    if (filters.maxPrice) params.maxPrice = filters.maxPrice
    if (sort.value) {
      const [field, dir] = sort.value.split(',')
      params.sort = `${field},${dir}`
    }

    try {
      const res = await api.get('/products', { params })
      products.value = res.data?.content || []
      total.value = res.data?.totalElements || 0
    } catch (error) {
      products.value = []
      total.value = 0
    }
    loading.value = false
  }

  function resetPage() {
    page.value = 1
  }

  function search() {
    resetPage()
    return loadProducts()
  }

  function setSort(value) {
    sort.value = value
    return search()
  }

  function updateQuery(query) {
    Object.assign(filters, query)
    return search()
  }

  return {
    products,
    categories,
    total,
    loading,
    page,
    size,
    sort,
    filters,
    totalPages,
    pageNumbers: computed(() => {
      const tp = totalPages.value
      const cp = page.value
      const pages = []
      if (tp <= 10) {
        for (let i = 1; i <= tp; i += 1) pages.push(i)
        return pages
      }
      pages.push(1)
      if (cp > 3) pages.push('...')
      for (let i = Math.max(2, cp - 1); i <= Math.min(tp - 1, cp + 1); i += 1) pages.push(i)
      if (cp < tp - 2) pages.push('...')
      pages.push(tp)
      return pages
    }),
    loadCategories,
    loadProducts,
    search,
    setSort,
    updateQuery
  }
}
