import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/Home.vue') },
  { path: '/products', component: () => import('../views/ProductList.vue') },
  { path: '/products/:id', component: () => import('../views/ProductDetail.vue') },
  { path: '/cart', component: () => import('../views/Cart.vue'), meta: { requiresAuth: true } },
  { path: '/checkout', component: () => import('../views/Checkout.vue'), meta: { requiresAuth: true } },
  { path: '/orders', component: () => import('../views/OrderList.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id', component: () => import('../views/OrderDetail.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id/invoice', component: () => import('../views/Invoice.vue'), meta: { requiresAuth: true } },
  { path: '/payment/return', component: () => import('../views/PaymentReturn.vue'), meta: { requiresAuth: true } },
  { path: '/auth', component: () => import('../views/Auth.vue') },
  { path: '/forgot-password', component: () => import('../views/ForgotPassword.vue') },
  { path: '/reset-password', component: () => import('../views/ResetPassword.vue') },
  { path: '/contact', component: () => import('../views/Contact.vue') },
  { path: '/news/:id', component: () => import('../views/NewsDetail.vue') },
  { path: '/addresses', component: () => import('../views/Addresses.vue'), meta: { requiresAuth: true } },
  { path: '/payment-methods', component: () => import('../views/PaymentMethods.vue'), meta: { requiresAuth: true } },
  { path: '/messages', component: () => import('../views/Messages.vue'), meta: { requiresAuth: true } },
  { path: '/seller/apply', component: () => import('../views/SellerApply.vue'), meta: { requiresAuth: true } },
  { path: '/account', component: () => import('../views/Account.vue'), meta: { requiresAuth: true } },
  { path: '/terms', component: () => import('../views/LegalPage.vue'), meta: { page: 'terms' } },
  { path: '/privacy', component: () => import('../views/LegalPage.vue'), meta: { page: 'privacy' } },
  { path: '/cookie', component: () => import('../views/LegalPage.vue'), meta: { page: 'cookie' } },
  { path: '/refund', component: () => import('../views/LegalPage.vue'), meta: { page: 'refund' } },
  { path: '/shipping', component: () => import('../views/LegalPage.vue'), meta: { page: 'shipping' } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    next('/auth')
  } else {
    next()
  }
})

export default router
