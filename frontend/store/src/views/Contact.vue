<template>
  <div class="contact-page">
    <div class="page-inner">
      <el-breadcrumb separator="/" style="margin-bottom: 16px;">
        <el-breadcrumb-item :to="{ path: '/' }">{{ $t('nav.home') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('contact.title') }}</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>{{ $t('contact.title') }}</h1>
      <p class="subtitle">{{ $t('contact.subtitle') }}</p>

      <div class="contact-grid">
        <div class="contact-info">
          <div class="info-card">
            <h4>{{ $t('contact.address') }}</h4>
            <p>{{ $t('contact.addressLine') }}</p>
          </div>
          <div class="info-card">
            <h4>{{ $t('contact.phone') }}</h4>
            <p>{{ $t('contact.phoneLine') }}</p>
          </div>
          <div class="info-card">
            <h4>{{ $t('contact.email') }}</h4>
            <p>support@coinmarket.com</p>
          </div>
        </div>
        <div class="contact-form">
          <el-form>
            <el-form-item :label="$t('account.needHelp')">
              <el-select v-model="form.ticketType" style="width:100%">
                <el-option label="General Inquiry" value="GENERAL" />
                <el-option label="Order Issue" value="ORDER" />
                <el-option label="Payment" value="PAYMENT" />
                <el-option label="Shipping" value="SHIPPING" />
                <el-option label="Return" value="RETURN" />
                <el-option label="Seller" value="SELLER" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('account.subject')">
              <el-input v-model="form.subject" />
            </el-form-item>
            <el-form-item :label="$t('account.describeIssue')">
              <el-input v-model="form.message" type="textarea" :rows="5" />
            </el-form-item>
            <el-button type="primary" size="large" @click="submitForm" :loading="submitting">{{ $t('account.submit') }}</el-button>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const submitting = ref(false)
const form = reactive({ ticketType: 'GENERAL', subject: '', message: '' })

async function submitForm() {
  if (!form.subject || !form.message) {
    ElMessage.warning('Please fill in all fields')
    return
  }
  submitting.value = true
  try {
    await api.post('/contact', form)
    ElMessage.success('Message sent successfully!')
    form.ticketType = 'GENERAL'
    form.subject = ''
    form.message = ''
  } catch (e) {
    ElMessage.error('Failed to send message')
  }
  submitting.value = false
}
</script>

<style scoped>
.contact-page { background: #fff; min-height: 60vh; }
.page-inner { max-width: 900px; margin: 0 auto; padding: 48px 24px; }
h1 { font-size: 32px; font-weight: 700; color: #111827; }
.subtitle { color: #6b7280; margin: 8px 0 32px; }
.contact-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 48px; }
.info-card { margin-bottom: 24px; }
.info-card h4 { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 4px; }
.info-card p { font-size: 14px; color: #6b7280; }
@media (max-width: 768px) { .contact-grid { grid-template-columns: 1fr; } }
</style>
