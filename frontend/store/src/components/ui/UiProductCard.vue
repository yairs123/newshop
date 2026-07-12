<template>
  <div class="product-card" @click="handleClick">
    <div class="card-image">
      <div class="image-placeholder">
        <span>{{ titleInitial }}</span>
      </div>
      <div class="card-badge" v-if="product.ratingGrade">
        {{ product.ratingCompany }} {{ product.ratingGrade }}
      </div>
      <button class="favorite-btn" :class="{ active: isFavorite }" @click.stop="toggleFavorite">
        <el-icon :size="18">
          <StarFilled v-if="isFavorite" />
          <Star v-else />
        </el-icon>
      </button>
    </div>

    <div class="card-body">
      <h4 class="card-title">{{ product.title }}</h4>
      <div class="card-meta">
        <span v-if="product.country">{{ $t('countries.' + product.country) }}</span>
        <span v-if="product.year">{{ product.year }}</span>
        <span v-if="product.material">{{ product.material }}</span>
      </div>
      <div class="card-footer">
        <span class="card-price">{{ formattedPrice }} {{ product.currency || 'USD' }}</span>
        <div class="card-actions">
          <el-button
            v-if="product.stock > 0"
            size="small"
            type="primary"
            @click.stop="addToCart"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ $t('product.addToCart') }}
          </el-button>
          <el-tag v-if="product.stock > 0" size="small" type="success" effect="plain">
            {{ $t('home.inStock') }}
          </el-tag>
          <el-tag v-else size="small" type="danger" effect="plain">
            {{ $t('home.sold') }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Star, StarFilled, ShoppingCart } from '@element-plus/icons-vue'

const props = defineProps({
  product: { type: Object, required: true },
  isFavorite: { type: Boolean, default: false }
})

const emits = defineEmits(['add-to-cart', 'toggle-favorite', 'open-product'])

const titleInitial = computed(() => props.product.title?.charAt(0).toUpperCase() || '')
const formattedPrice = computed(() => Number(props.product.price).toLocaleString())

function addToCart() {
  emits('add-to-cart', props.product)
}

function toggleFavorite() {
  emits('toggle-favorite', props.product)
}

function handleClick() {
  emits('open-product', props.product.id)
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
  cursor: pointer;
  transition: box-shadow .2s, transform .2s;
  display: flex;
  flex-direction: column;
}
.product-card:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,.12);
  transform: translateY(-2px);
}
.card-image {
  position: relative;
  height: 200px;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-placeholder {
  width: 80px;
  height: 80px;
  background: #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #9ca3af;
}
.card-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #111827;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
}
.favorite-btn {
  position: absolute;
  top: 8px;
  left: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.9);
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all .15s;
  z-index: 2;
}
.favorite-btn:hover { color: #ef4444; background: #fff; }
.favorite-btn.active { color: #ef4444; }
.card-body { padding: 14px; display: flex; flex-direction: column; flex: 1; }
.card-title { font-size: 14px; font-weight: 600; color: #111827; line-height: 1.4; margin-bottom: 8px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; min-height: 2.8em; }
.card-meta { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; min-height: 20px; }
.card-meta span { font-size: 12px; color: #6b7280; background: #f3f4f6; padding: 2px 8px; border-radius: 4px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-actions { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.card-price { font-size: 18px; font-weight: 700; color: #1f2937; }
</style>
