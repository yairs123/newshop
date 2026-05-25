import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: () => import('../views/Dashboard.vue'), meta: { requiresAuth: true } },
  { path: '/products', component: () => import('../views/Products.vue'), meta: { requiresAuth: true } },
  { path: '/inventory', component: () => import('../views/InventoryEntry.vue'), meta: { requiresAuth: true } },
  { path: '/orders', component: () => import('../views/Orders.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id/invoice', component: () => import('../views/Invoice.vue'), meta: { requiresAuth: true } },
  { path: '/users', component: () => import('../views/Users.vue'), meta: { requiresAuth: true } },
  { path: '/sellers', component: () => import('../views/Sellers.vue'), meta: { requiresAuth: true } },
  { path: '/ads', component: () => import('../views/Ads.vue'), meta: { requiresAuth: true } },
  { path: '/news', component: () => import('../views/News.vue'), meta: { requiresAuth: true } },
  { path: '/tickets', component: () => import('../views/Tickets.vue'), meta: { requiresAuth: true } },
  { path: '/print-labels', component: () => import('../views/PrintLabels.vue'), meta: { requiresAuth: true } },
  { path: '/import-images', component: () => import('../views/ImageImport.vue'), meta: { requiresAuth: true } },
  { path: '/product-history', component: () => import('../views/ProductHistory.vue'), meta: { requiresAuth: true } },
  { path: '/barcode-codes', component: () => import('../views/BarcodeCodes.vue'), meta: { requiresAuth: true } },
  { path: '/audit-logs', component: () => import('../views/AuditLogs.vue'), meta: { requiresAuth: true } },
  { path: '/finance', component: () => import('../views/Finance.vue'), meta: { requiresAuth: true } },
  { path: '/finance/sales-revenue', component: () => import('../views/SalesRevenue.vue'), meta: { requiresAuth: true } },
  { path: '/finance/purchase-report', component: () => import('../views/PurchaseReport.vue'), meta: { requiresAuth: true } },
  { path: '/finance/profit-report', component: () => import('../views/ProfitReport.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
