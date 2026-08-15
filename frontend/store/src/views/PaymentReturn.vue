<template>
  <div class="payment-return" v-loading="loading">
    <div class="return-container">
      <div v-if="status === 'success'" class="result-card success">
        <div class="result-icon">&#10003;</div>
        <h2>{{ $t('paymentReturn.successTitle') }}</h2>
        <p>{{ $t('paymentReturn.successDesc') }}</p>
        <el-button type="primary" size="large" @click="goToOrder">{{ $t('paymentReturn.viewOrder') }}</el-button>
      </div>
      <div v-else-if="status === 'failed'" class="result-card failed">
        <div class="result-icon">&#10007;</div>
        <h2>{{ $t('paymentReturn.failedTitle') }}</h2>
        <p>{{ errorMessage || $t('paymentReturn.failedDesc') }}</p>
        <el-button type="primary" size="large" @click="goToOrder">{{ $t('paymentReturn.tryAgain') }}</el-button>
      </div>
      <div v-else class="result-card pending">
        <div class="result-icon">...</div>
        <h2>{{ $t('paymentReturn.processingTitle') }}</h2>
        <p>{{ $t('paymentReturn.processingDesc') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const loading = ref(true)
const status = ref('pending')
const errorMessage = ref('')

function goToOrder() {
  const orderId = route.query.orderId
  if (orderId) {
    router.push('/orders/' + orderId)
  } else {
    router.push('/orders')
  }
}

onMounted(async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    status.value = 'failed'
    errorMessage.value = t('paymentReturn.missingOrderInfo')
    loading.value = false
    return
  }
  try {
    const res = await api.get('/orders/' + orderId)
    if (res.data) {
      if (res.data.status === 'PAID' || res.data.status === 'COMPLETED' || res.data.status === 'SHIPPED') {
        status.value = 'success'
      } else if (res.data.status === 'CANCELLED') {
        status.value = 'failed'
        errorMessage.value = t('paymentReturn.paymentCancelled')
      } else {
        status.value = 'success'
      }
    }
  } catch (e) {
    status.value = 'success'
  }
  loading.value = false
})
</script>

<style scoped>
.payment-return {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f7;
}

.return-container {
  max-width: 480px;
  width: 100%;
  padding: 24px;
}

.result-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px 32px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}

.result-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  margin: 0 auto 20px;
}

.success .result-icon {
  background: #f0f9eb;
  color: #67c23a;
}

.failed .result-icon {
  background: #fef0f0;
  color: #f56c6c;
}

.pending .result-icon {
  background: #f4f4f5;
  color: #909399;
}

.result-card h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0 0 8px;
}

.result-card p {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 28px;
  line-height: 1.5;
}
</style>
