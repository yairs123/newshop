<template>
  <el-drawer v-model="drawerVisible" direction="rtl" size="400px">
    <template #title>
      <span class="drawer-title">{{ $t('cartDrawer.title') }} ({{ cartStore.totalCount }})</span>
    </template>

    <div v-if="cartStore.items.length === 0" class="drawer-empty">
      <el-empty :description="$t('cartDrawer.empty')" />
    </div>

    <div v-else class="drawer-items">
      <div v-for="item in cartStore.items" :key="item.id" class="drawer-item">
        <div class="di-image">
          <span>{{ item.title.charAt(0) }}</span>
          <span v-if="item.ratingGrade" class="di-badge">{{ item.ratingGrade }}</span>
        </div>
        <div class="di-info">
          <p class="di-title">{{ item.title }}</p>
          <div class="di-meta" v-if="item.country || item.year || item.material">
            <span v-if="item.country">{{ $t('countries.' + item.country) }}</span>
            <span v-if="item.year">{{ item.year }}</span>
            <span v-if="item.material">{{ item.material }}</span>
          </div>
          <div class="di-bottom">
            <div class="di-qty-row">
              <el-input-number v-model="item.quantity" :min="1" size="small" @change="cartStore.save()" />
              <span class="di-subtotal">{{ formatPrice(item.price * item.quantity) }}</span>
            </div>
            <el-button size="small" :icon="Delete" type="danger" plain @click="cartStore.removeItem(item.id)">
              {{ $t('common.remove') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <template #footer v-if="cartStore.items.length > 0">
      <div class="drawer-footer">
        <div class="df-row">
          <span>{{ $t('cartDrawer.subtotal') }}</span>
          <span class="df-amount">{{ formatPrice(cartStore.totalAmount) }}</span>
        </div>
        <el-button type="primary" size="large" style="width:100%" @click="goCheckout">
          {{ $t('cartDrawer.checkout') }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../store/cart'
import { Delete } from '@element-plus/icons-vue'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['update:visible'])

const router = useRouter()
const cartStore = useCartStore()

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
})

function formatPrice(p) { return '$' + Number(p).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

function goCheckout() {
  drawerVisible.value = false
  router.push('/checkout')
}
</script>

<style scoped>
.drawer-title { font-size: 16px; font-weight: 600; }
.drawer-empty { display: flex; justify-content: center; padding: 80px 0; }
.drawer-items { display: flex; flex-direction: column; gap: 12px; }
.drawer-item { display: flex; gap: 12px; padding: 12px; background: #f9fafb; border-radius: 8px; }
.di-image { width: 56px; height: 56px; background: #e5e7eb; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #9ca3af; flex-shrink: 0; position: relative; }
.di-badge { position: absolute; bottom: -4px; right: -4px; background: #111827; color: #fff; font-size: 9px; font-weight: 600; padding: 1px 5px; border-radius: 4px; white-space: nowrap; }
.di-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 8px; }
.di-title { font-size: 13px; font-weight: 600; color: #111827; line-height: 1.3; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.di-meta { display: flex; gap: 6px; flex-wrap: wrap; }
.di-meta span { font-size: 11px; color: #6b7280; background: #e5e7eb; padding: 1px 6px; border-radius: 3px; }
.di-bottom { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.di-qty-row { display: flex; align-items: center; gap: 10px; }
.di-subtotal { font-size: 15px; font-weight: 700; color: #dc2626; white-space: nowrap; }
.drawer-footer { padding-top: 8px; }
.df-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; font-size: 15px; color: #374151; }
.df-amount { font-size: 20px; font-weight: 700; color: #111827; }
</style>
