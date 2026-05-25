<template>
  <div>
    <h3>{{ $t('tickets.title') }}</h3>

    <div style="margin:16px 0">
      <el-select v-model="statusFilter" @change="fetchTickets" style="width:160px">
        <el-option label="All" value="" />
        <el-option label="Open" value="OPEN" />
        <el-option label="In Progress" value="IN_PROGRESS" />
        <el-option label="Resolved" value="RESOLVED" />
        <el-option label="Closed" value="CLOSED" />
      </el-select>
    </div>

    <el-table :data="tickets" border stripe v-loading="loading" @row-click="openDetail">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="subject" :label="$t('tickets.subject')" min-width="200" />
      <el-table-column prop="userId" label="User ID" width="80" />
      <el-table-column prop="ticketType" :label="$t('tickets.type')" width="120" />
      <el-table-column prop="status" :label="$t('tickets.status')" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" :label="$t('tickets.date')" width="170" />
    </el-table>

    <el-drawer v-model="drawerVisible" :title="detail?.subject" size="500px">
      <template v-if="detail">
        <div class="ticket-meta">
          <span>Type: {{ detail.ticketType }}</span>
          <span>Status: <el-tag :type="statusTag(detail.status)" size="small">{{ detail.status }}</el-tag></span>
          <span>User ID: {{ detail.userId }}</span>
          <span>Date: {{ detail.createdAt }}</span>
        </div>
        <el-divider />
        <h4>Message</h4>
        <p class="ticket-message">{{ detail.message }}</p>

        <div v-if="detail.adminReply" class="ticket-reply">
          <h4>Admin Reply</h4>
          <p>{{ detail.adminReply }}</p>
          <small v-if="detail.repliedAt">{{ detail.repliedAt }}</small>
        </div>

        <el-divider />
        <h4>{{ $t('tickets.reply') }}</h4>
        <el-input v-model="replyText" type="textarea" :rows="4" :placeholder="$t('tickets.replyPlaceholder')" />
        <div class="ticket-actions">
          <el-button type="primary" @click="sendReply" :loading="replying">{{ $t('tickets.sendReply') }}</el-button>
          <el-button v-if="detail.status === 'OPEN'" @click="updateStatus('IN_PROGRESS')">Start Progress</el-button>
          <el-button v-if="detail.status !== 'RESOLVED'" type="success" @click="updateStatus('RESOLVED')">Resolve</el-button>
          <el-button v-if="detail.status !== 'CLOSED'" type="info" @click="updateStatus('CLOSED')">Close</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'

const tickets = ref([])
const loading = ref(true)
const statusFilter = ref('')
const drawerVisible = ref(false)
const detail = ref(null)
const replyText = ref('')
const replying = ref(false)

onMounted(fetchTickets)

async function fetchTickets() {
  loading.value = true
  try {
    const params = statusFilter.value ? { status: statusFilter.value } : {}
    const res = await api.get('/admin/tickets', { params })
    tickets.value = res.data || []
  } catch (e) { tickets.value = [] }
  loading.value = false
}

function statusTag(s) {
  switch (s) {
    case 'OPEN': return 'danger'
    case 'IN_PROGRESS': return 'warning'
    case 'RESOLVED': return 'success'
    case 'CLOSED': return 'info'
    default: return ''
  }
}

async function openDetail(row) {
  try {
    const res = await api.get(`/admin/tickets/${row.id}`)
    detail.value = res.data
    replyText.value = ''
    drawerVisible.value = true
  } catch (e) { ElMessage.error('Failed to load ticket') }
}

async function sendReply() {
  if (!replyText.value) return
  replying.value = true
  try {
    await api.post(`/admin/tickets/${detail.value.id}/reply`, { reply: replyText.value })
    ElMessage.success('Reply sent')
    drawerVisible.value = false
    fetchTickets()
  } catch (e) { ElMessage.error('Failed') }
  replying.value = false
}

async function updateStatus(status) {
  try {
    await api.put(`/admin/tickets/${detail.value.id}/status`, { status })
    ElMessage.success('Status updated')
    detail.value.status = status
    fetchTickets()
  } catch (e) { ElMessage.error('Failed') }
}
</script>

<style scoped>
.ticket-meta { display: flex; flex-wrap: wrap; gap: 12px; font-size: 13px; color: #6b7280; }
.ticket-message { background: #f9fafb; padding: 16px; border-radius: 8px; line-height: 1.6; }
.ticket-reply { background: #eff6ff; padding: 16px; border-radius: 8px; margin-top: 12px; }
.ticket-reply small { color: #9ca3af; display: block; margin-top: 8px; }
.ticket-actions { display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
</style>
