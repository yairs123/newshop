<template>
  <div class="cart-page">
    <el-breadcrumb separator="/" style="margin-bottom: 16px;">
      <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
      <el-breadcrumb-item>{{ $t('cart.pageTitle') }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="cart-header">
      <h2>{{ $t('cart.pageTitle') }}</h2>
      <span v-if="cartStore.items.length" class="cart-count">{{ cartStore.totalCount }} 件商品</span>
    </div>

    <div v-if="cartStore.items.length" class="cart-items">
      <div v-for="item in cartStore.items" :key="item.id" class="cart-card">
        <div class="cart-card-left">
          <div class="cart-image" @click="$router.push(`/products/${item.id}`)">
            <span>{{ item.title.charAt(0) }}</span>
          </div>
        </div>
        <div class="cart-card-body">
          <div class="cart-card-top">
            <div class="cart-product-info">
              <router-link :to="`/products/${item.id}`" class="cart-product-title">{{ item.title }}</router-link>
              <div class="cart-meta" v-if="item.country || item.year || item.material">
                <span v-if="item.country">{{ $t('countries.' + item.country) }}</span>
                <span v-if="item.year">{{ item.year }}</span>
                <span v-if="item.material">{{ item.material }}</span>
                <span v-if="item.ratingGrade" class="cart-rating">{{ item.ratingGrade }}</span>
              </div>
            </div>
            <div class="cart-price">{{ '$' + Number(item.price).toLocaleString() }}</div>
          </div>

          <div class="cart-card-actions">
            <div class="qty-control">
              <el-button size="small" circle @click="decrement(item)" :disabled="item.quantity <= 1">−</el-button>
              <span class="qty-value">{{ item.quantity }}</span>
              <el-button size="small" circle @click="increment(item)">+</el-button>
            </div>
            <div class="item-subtotal">
              {{ '$' + (item.price * item.quantity).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
            </div>
            <div class="item-actions">
              <el-button size="small" text @click="findSimilar(item)">
                🔍 类似商品
              </el-button>
              <el-button size="small" text type="primary" @click="saveForLater(item)">
                💾 稍后购买
              </el-button>
              <el-button size="small" text type="danger" @click="confirmRemove(item.id)">
                🗑️ 删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else :description="$t('cart.empty')">
      <el-button type="primary" @click="$router.push('/products')">{{ $t('nav.shop') }}</el-button>
    </el-empty>

    <!-- Footer -->
    <div v-if="cartStore.items.length" class="cart-footer">
      <div class="footer-left">
        <span class="footer-count">共 {{ cartStore.totalCount }} 件</span>
        <span class="footer-divider">|</span>
        <span class="footer-total-label">{{ $t('cartDrawer.total') }}:</span>
        <span class="footer-total">{{ '$' + cartStore.totalAmount.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</span>
      </div>
      <el-button type="primary" size="large" class="checkout-btn" @click="checkout">
        {{ $t('cart.checkout') }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

function checkout() { router.push('/checkout') }

async function confirmRemove(id) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '删除确认')
    cartStore.removeItem(id)
    ElMessage.success('已删除')
  } catch (_) {}
}

function decrement(item) {
  if (item.quantity > 1) {
    item.quantity--
    cartStore.save()
  }
}

function increment(item) {
  item.quantity++
  cartStore.save()
}

function saveForLater(item) {
  ElMessage.info('稍后购买功能开发中')
}

function findSimilar(item) {
  router.push(`/products/${item.id}`)
}
</script>

<style>
.cart-page { max-width: 960px; margin: 0 auto; padding: 32px 24px; }
.cart-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.cart-header h2 { font-size: 24px; font-weight: 700; color: #111827; margin: 0; }
.cart-count { font-size: 14px; color: #6b7280; }

/* Card-style items */
.cart-items { display: flex; flex-direction: column; gap: 12px; }
.cart-card {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  border: 1px solid #f0f0f0;
  transition: box-shadow .15s;
}
.cart-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,.08); }

.cart-card-left { flex-shrink: 0; }
.cart-image {
  width: 80px;
  height: 80px;
  background: #f3f4f6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 24px;
  color: #9ca3af;
  cursor: pointer;
  transition: background .15s;
}
.cart-image:hover { background: #e5e7eb; }

.cart-card-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 12px; }
.cart-card-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
.cart-product-info { flex: 1; min-width: 0; }

.cart-product-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  text-decoration: none;
  line-height: 1.3;
  display: block;
  margin-bottom: 4px;
}
.cart-product-title:hover { color: #3b82f6; }

.cart-meta { display: flex; gap: 4px; flex-wrap: wrap; }
.cart-meta span { font-size: 11px; color: #6b7280; background: #f3f4f6; padding: 1px 6px; border-radius: 3px; }
.cart-rating { background: #111827 !important; color: #fff !important; font-weight: 600; }

.cart-price {
  font-size: 16px;
  font-weight: 700;
  color: #059669;
  white-space: nowrap;
}

.cart-card-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  flex-wrap: wrap;
}

.qty-control {
  display: flex;
  align-items: center;
  gap: 8px;
}
.qty-value {
  font-size: 15px;
  font-weight: 600;
  min-width: 24px;
  text-align: center;
  color: #111827;
}

.item-subtotal {
  font-size: 15px;
  font-weight: 700;
  color: #dc2626;
}

.item-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
}

/* Footer */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
  border: 1px solid #f0f0f0;
}
.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.footer-count { font-size: 14px; color: #6b7280; }
.footer-divider { color: #d1d5db; }
.footer-total-label { font-size: 14px; color: #374151; }
.footer-total { font-size: 22px; font-weight: 800; color: #dc2626; }
.checkout-btn { height: 48px; padding: 0 32px; font-size: 16px; border-radius: 10px; }

@media (max-width: 768px) {
  .cart-page { padding: 20px 12px; }
  .cart-card { padding: 14px; gap: 12px; }
  .cart-image { width: 60px; height: 60px; font-size: 18px; }
  .cart-card-top { flex-direction: column; }
  .cart-card-actions { flex-direction: column; align-items: stretch; }
  .item-actions { margin-left: 0; justify-content: flex-end; }
  .cart-footer { flex-direction: column; gap: 16px; }
  .footer-left { flex-wrap: wrap; justify-content: center; }
}
</style>
