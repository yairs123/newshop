<template>
  <div class="orders-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h3>📋 订单管理</h3>
        <p class="page-desc">查看和处理所有订单</p>
      </div>
      <el-button size="small" @click="load" :icon="Refresh" circle />
    </div>

    <!-- 图例说明 -->
    <el-card shadow="never" class="legend-card">
      <div class="legend-items">
        <span class="legend-item"><span class="lg-dot" style="background:#f56c6c">!</span> 超时未支付（≥3天）</span>
        <span class="legend-item"><span class="lg-dot" style="background:#e6a23c">!</span> 超时未发货（≥2天）</span>
        <span class="legend-item"><span class="lg-dot" style="background:#409eff">›</span> 待发货（新订单）</span>
        <span class="legend-item"><span class="lg-dot" style="background:#9ca3af">✓</span> 已发货超7天未确认</span>
        <span class="legend-item"><span class="lg-dot" style="background:#fef3c7;color:#d97706">📝</span> 有备注</span>
      </div>
    </el-card>

    <!-- 状态统计条 -->
    <el-row :gutter="12" class="stats-bar">
      <el-col :span="4" v-for="s in statusStats" :key="s.key">
        <div class="stat-chip" :class="'chip-' + s.type" @click="statusFilter = s.key">
          <span class="chip-count">{{ s.count }}</span>
          <span class="chip-label">{{ s.label }}</span>
        </div>
      </el-col>
    </el-row>

    <!-- 订单列表卡片 -->
    <el-card shadow="never" class="list-card">
      <template v-if="orders.length">
        <div v-for="(o, idx) in orders" :key="o.id" class="order-row">
          <!-- 主行 -->
          <div class="order-main" @click="expandedId = expandedId === o.id ? null : o.id">
            <div class="order-cell order-index">#{{ idx + 1 + (page-1) * size }}</div>
            <div class="order-cell order-no-col">
              <code class="order-no">{{ o.orderNo }}</code>
            </div>
            <div class="order-cell order-status-col">
              <span class="status-pill" :class="'status-' + o.status">{{ STATUS_LABELS[o.status] || o.status }}</span>
              <!-- 智能提醒小标签 -->
              <span v-if="getAlert(o)" class="alert-tag" :style="{ background: getAlert(o).color }">{{ getAlert(o).icon }} {{ getAlert(o).text }}</span>
            </div>
            <div class="order-cell order-amount-col">
              <span class="amount">{{ o.currency }} {{ formatPrice(o.totalAmount) }}</span>
            </div>
            <div class="order-cell order-buyer-col">{{ o.buyerName || '—' }}</div>
            <div class="order-cell order-time-col">{{ formatDate(o.createdAt) }}</div>
            <div class="order-cell order-actions-col" @click.stop>
              <div class="action-btn-row">
                <!-- 点击行可展开详情，这里不再放重复的详情按钮 -->
                <template v-if="o.status === 'PENDING_PAYMENT'">
                  <button class="act act-pay" @click="markPaid(o)">💰 确认收款</button>
                  <button class="act act-cancel" @click="cancelOrder(o)">❌ 取消订单</button>
                </template>

                <!-- 待发货可操作 -->
                <template v-if="o.status === 'PAID'">
                  <button class="act act-ship" @click="openShip(o)">📦 发货</button>
                  <button class="act act-cancel" @click="cancelOrder(o)">❌ 取消</button>
                </template>

                <!-- 已发货可操作 -->
                <template v-if="o.status === 'SHIPPED'">
                  <button class="act act-force" @click="forceComplete(o)">✅ 完成订单</button>
                </template>

                <!-- 已完成/已取消可操作 -->
                <template v-if="o.status === 'COMPLETED' || o.status === 'CANCELLED'">
                  <button class="act act-invoice" @click="$router.push('/orders/' + o.id + '/invoice')">📄 查看账单</button>
                </template>

                <!-- 备注按钮：有备注显示内容，无备注显示添加 -->
                <button class="act act-note" :class="{ 'has-note': o.adminNote }" @click="openNote(o)">
                  {{ o.adminNote ? '📝 备注' : '📄 添加备注' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 备注内容直接显示 -->
          <div v-if="o.adminNote" class="order-note-line" @click="openNote(o)">
            <span class="note-marker">📝</span>
            <span class="note-text">{{ o.adminNote }}</span>
            <span class="note-edit">点击编辑</span>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无订单" :image-size="80" />
    </el-card>

    <!-- 发货弹窗 -->
    <el-dialog v-model="showShip" width="480px" :close-on-click-modal="false" class="ship-dialog">
      <template #header>
        <div class="dialog-header">
          <span class="dialog-icon">📦</span>
          <div>
            <h4>确认发货</h4>
            <p class="dialog-sub">填写物流信息后提交，订单状态将变为已发货</p>
          </div>
        </div>
      </template>
      <template v-if="shipOrder">
        <div class="ship-order-info">
          <div class="ship-field"><span class="ship-field-label">订单号</span><code>{{ shipOrder.orderNo }}</code></div>
          <div class="ship-field"><span class="ship-field-label">金额</span><strong>{{ shipOrder.currency }} {{ formatPrice(shipOrder.totalAmount) }}</strong></div>
          <div class="ship-field"><span class="ship-field-label">买家</span>{{ shipOrder.buyerName || '-' }}</div>
        </div>
        <div class="ship-form">
          <div class="ship-form-row">
            <label>快递公司</label>
            <el-select v-model="shipForm.company" placeholder="选择快递公司" style="width:100%">
              <el-option label="USPS" value="USPS" />
              <el-option label="UPS" value="UPS" />
              <el-option label="FedEx" value="FedEx" />
              <el-option label="DHL" value="DHL" />
              <el-option label="EMS" value="EMS" />
              <el-option label="顺丰速运" value="顺丰速运" />
              <el-option label="其他" value="其他" />
            </el-select>
          </div>
          <div class="ship-form-row">
            <label>快递单号</label>
            <el-input v-model="shipForm.number" placeholder="输入快递单号" size="large" />
          </div>
        </div>
      </template>
      <template #footer>
        <button class="btn-cancel" @click="showShip = false">取消</button>
        <button class="btn-confirm" @click="confirmShip" :disabled="shipping">
          {{ shipping ? '提交中...' : '✅ 确认发货' }}
        </button>
      </template>
    </el-dialog>

    <!-- 备注编辑弹窗 -->
    <el-dialog v-model="showNote" width="460px" :close-on-click-modal="false" class="note-dialog">
      <template #header>
        <div class="dialog-header">
          <span class="dialog-icon">📝</span>
          <div>
            <h4>{{ noteOrder?.adminNote ? '编辑备注' : '添加备注' }}</h4>
            <p class="dialog-sub">内部备注，只有管理员能看到</p>
          </div>
        </div>
      </template>
      <template v-if="noteOrder">
        <div class="note-order-info">
          <code>{{ noteOrder.orderNo }}</code>
          <el-tag :type="statusType(noteOrder.status)" size="small" effect="plain">{{ STATUS_LABELS[noteOrder.status] }}</el-tag>
        </div>
        <el-input
          v-model="noteText"
          type="textarea"
          :rows="4"
          placeholder="输入备注内容..."
          maxlength="500"
          show-word-limit
        />
      </template>
      <template #footer>
        <div class="note-footer">
          <div>
            <button v-if="noteOrder?.adminNote" class="btn-delete" @click="deleteNote">🗑️ 删除备注</button>
          </div>
          <div>
            <button class="btn-cancel" @click="showNote = false">取消</button>
            <button class="btn-confirm" @click="saveNote" :disabled="savingNote">
              {{ savingNote ? '保存中...' : '💾 保存备注' }}
            </button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="load"
        background
        small
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const size = 20
const statusFilter = ref('')

// Ship
const showShip = ref(false)
const shipOrder = ref(null)
const shipping = ref(false)
const shipForm = ref({ company: '', number: '' })

// Note
const showNote = ref(false)
const noteOrder = ref(null)
const noteText = ref('')
const savingNote = ref(false)

const STATUS_LABELS = {
  PENDING_PAYMENT: '待支付', PAID: '待发货', SHIPPED: '已发货',
  COMPLETED: '已完成', CANCELLED: '已取消'
}

const STATUS_KEYS = ['PENDING_PAYMENT', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED']

const statusStats = computed(() => {
  const counts = {}
  for (const o of orders.value) {
    counts[o.status] = (counts[o.status] || 0) + 1
  }
  return STATUS_KEYS.map(key => ({
    key, type: statusType(key), label: STATUS_LABELS[key] || key, count: counts[key] || 0
  }))
})

function statusType(s) {
  const map = { PENDING_PAYMENT: 'danger', PAID: 'success', SHIPPED: 'warning', COMPLETED: 'success', CANCELLED: 'info' }
  return map[s] || 'info'
}

/** 智能提醒：基于状态和时间判断 */
function getAlert(o) {
  if (!o.createdAt) return null
  const created = new Date(o.createdAt)
  const now = new Date()
  const daysDiff = (now - created) / (1000 * 60 * 60 * 24)

  if (o.status === 'PENDING_PAYMENT' && daysDiff > 3) {
    return { icon: '🔴', text: `${Math.floor(daysDiff)}天未支付`, color: '#f56c6c' }
  }
  if (o.status === 'PAID') {
    const paidAt = o.paidAt ? new Date(o.paidAt) : created
    const paidDays = (now - paidAt) / (1000 * 60 * 60 * 24)
    if (paidDays > 2) {
      return { icon: '🟡', text: `${Math.floor(paidDays)}天未发货`, color: '#e6a23c' }
    }
    return { icon: '🆕', text: '待发货', color: '#409eff' }
  }
  if (o.status === 'SHIPPED') {
    const shippedDays = (now - created) / (1000 * 60 * 60 * 24)
    if (shippedDays > 7) {
      return { icon: '🔵', text: `${Math.floor(shippedDays)}天未确认`, color: '#9ca3af' }
    }
  }
  return null
}

// --- Ship actions ---
function openShip(row) {
  shipOrder.value = row
  shipForm.value = { company: '', number: '' }
  showShip.value = true
}
async function confirmShip() {
  if (!shipForm.value.company || !shipForm.value.number) {
    ElMessage.warning('请填写快递公司和单号')
    return
  }
  shipping.value = true
  try {
    await api.post(`/admin/orders/${shipOrder.value.id}/ship`, null, {
      params: { trackingCompany: shipForm.value.company, trackingNumber: shipForm.value.number }
    })
    ElMessage.success('发货成功！')
    showShip.value = false
    load()
  } catch (e) { /* handled */ }
  finally { shipping.value = false }
}

// --- Note actions ---
function openNote(o) {
  noteOrder.value = o
  noteText.value = o.adminNote || ''
  showNote.value = true
}
async function saveNote() {
  savingNote.value = true
  try {
    await api.put(`/admin/orders/${noteOrder.value.id}`, { adminNote: noteText.value, reason: '管理员备注' })
    ElMessage.success('备注已保存')
    showNote.value = false
    const found = orders.value.find(o => o.id === noteOrder.value.id)
    if (found) found.adminNote = noteText.value
  } catch (e) { /* handled */ }
  savingNote.value = false
}
async function deleteNote() {
  try {
    await ElMessageBox.confirm('确定删除此备注？', '删除备注', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    noteText.value = ''
    await api.put(`/admin/orders/${noteOrder.value.id}`, { adminNote: '', reason: '删除备注' })
    ElMessage.success('备注已删除')
    showNote.value = false
    const found = orders.value.find(o => o.id === noteOrder.value.id)
    if (found) found.adminNote = ''
  } catch (e) { /* cancelled */ }
}

// --- Status actions ---
async function markPaid(row) {
  try {
    await ElMessageBox.confirm(`确认收到 ${row.orderNo} 的付款？`, '确认收款', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' })
    await api.post(`/admin/orders/${row.id}/mark-paid`)
    ElMessage.success('已标记为待发货')
    load()
  } catch (e) { /* cancelled */ }
}
async function cancelOrder(row) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${row.orderNo} ？`, '取消订单', { confirmButtonText: '确认取消', cancelButtonText: '再想想', type: 'warning' })
    await api.post(`/admin/orders/${row.id}/cancel`)
    ElMessage.success('订单已取消')
    load()
  } catch (e) { /* cancelled */ }
}
async function forceComplete(row) {
  try {
    await ElMessageBox.confirm(`确定完成订单 ${row.orderNo} ？`, '完成订单', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    await api.post(`/admin/orders/${row.id}/complete`)
    ElMessage.success('订单已完成')
    load()
  } catch (e) { /* cancelled */ }
}

function formatPrice(val) { return val != null ? Number(val).toFixed(2) : '0.00' }
function formatDate(d) { return d ? d.slice(0, 16).replace('T', ' ') : '-' }

async function load() {
  loading.value = true
  try {
    const p = page.value - 1
    const res = await api.get(`/admin/orders?page=${p}&size=${size}`)
    orders.value = res.data?.content || []
    total.value = res.data?.totalElements || 0
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

onMounted(() => load())
</script>

<style scoped>
.orders-page { max-width: 1280px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.page-header h3 { font-size: 20px; font-weight: 700; color: #111827; margin: 0; }
.page-desc { font-size: 13px; color: #9ca3af; margin-top: 2px; }

/* Legend */
.legend-card { margin-bottom: 12px; padding: 8px 0; border-radius: 8px; }
.legend-items { display: flex; gap: 20px; flex-wrap: wrap; }
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #6b7280; }
.lg-dot { display: inline-flex; align-items: center; justify-content: center; width: 18px; height: 18px; border-radius: 50%; color: #fff; font-size: 10px; font-weight: 700; }

/* Stats */
.stats-bar { margin-bottom: 12px; }
.stat-chip { background: #fff; border-radius: 10px; padding: 10px; cursor: pointer; text-align: center; border: 2px solid #f0f0f0; transition: all 0.2s; }
.stat-chip:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.chip-count { display: block; font-size: 20px; font-weight: 800; line-height: 1.2; }
.chip-label { display: block; font-size: 11px; margin-top: 2px; font-weight: 500; }
.chip-danger .chip-count { color: #f56c6c; } .chip-danger .chip-label { color: #f56c6c; }
.chip-success .chip-count { color: #67c23a; } .chip-success .chip-label { color: #67c23a; }
.chip-warning .chip-count { color: #e6a23c; } .chip-warning .chip-label { color: #e6a23c; }
.chip-info .chip-count { color: #909399; } .chip-info .chip-label { color: #909399; }

/* List card */
.list-card { border-radius: 12px; overflow: hidden; border: 1px solid #e5e7eb; }
.order-row { border-bottom: 1px solid #f3f4f6; }
.order-row:last-child { border-bottom: none; }
.order-row:hover { background: #f9fafb; }

.order-main {
  display: grid; grid-template-columns: 44px 1fr 180px 110px 90px 120px 1fr;
  align-items: center; padding: 12px 14px; cursor: pointer; gap: 8px;
}
.order-cell { font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.order-index { color: #9ca3af; font-weight: 500; font-size: 11px; }
.order-no { font-family: 'SF Mono', monospace; font-size: 12px; color: #409eff; font-weight: 600; }

/* Status */
.status-pill { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 600; margin-right: 6px; }
.status-PENDING_PAYMENT { background: #fef2f2; color: #dc2626; }
.status-PAID { background: #f0fdf4; color: #16a34a; }
.status-SHIPPED { background: #fffbeb; color: #d97706; }
.status-COMPLETED { background: #f0fdf4; color: #16a34a; }
.status-CANCELLED { background: #f3f4f6; color: #6b7280; }

/* Alert tag inline */
.alert-tag { display: inline-block; padding: 2px 6px; border-radius: 4px; color: #fff; font-size: 10px; font-weight: 600; white-space: nowrap; }

.amount { font-weight: 700; color: #dc2626; font-size: 14px; }
.order-time-col { color: #6b7280; font-size: 12px; }

/* Action buttons row */
.action-btn-row { display: flex; gap: 4px; flex-wrap: wrap; }
.act {
  padding: 4px 8px; border-radius: 6px; border: 1px solid #e5e7eb;
  background: #fff; cursor: pointer; font-size: 11px; font-weight: 500;
  transition: all 0.15s; white-space: nowrap;
}
.act:hover { transform: translateY(-1px); box-shadow: 0 2px 6px rgba(0,0,0,.06); }
.act-detail { color: #3b82f6; border-color: #bfdbfe; }
.act-detail:hover { background: #eff6ff; }
.act-pay { color: #0284c7; border-color: #bae6fd; background: #f0f9ff; }
.act-pay:hover { background: #e0f2fe; }
.act-ship { color: #d97706; border-color: #fde68a; background: #fffbeb; }
.act-ship:hover { background: #fef3c7; }
.act-force { color: #16a34a; border-color: #bbf7d0; background: #f0fdf4; }
.act-force:hover { background: #dcfce7; }
.act-cancel { color: #dc2626; border-color: #fecaca; }
.act-cancel:hover { background: #fef2f2; }
.act-invoice { color: #6b7280; border-color: #e5e7eb; }
.act-invoice:hover { background: #f3f4f6; }
.act-note { color: #6b7280; border-color: #e5e7eb; }
.act-note:hover { background: #f3f4f6; }
.act-note.has-note { color: #d97706; border-color: #fde68a; background: #fffbeb; }

/* Inline note display */
.order-note-line {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 14px 6px 58px; background: #fffbeb;
  border-top: 1px solid #fef3c7; cursor: pointer; font-size: 12px;
  transition: background 0.15s;
}
.order-note-line:hover { background: #fef3c7; }
.note-marker { font-size: 13px; }
.note-text { flex: 1; color: #92400e; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.note-edit { color: #d97706; font-size: 11px; font-weight: 500; opacity: 0; transition: opacity 0.15s; }
.order-note-line:hover .note-edit { opacity: 1; }

/* Ship dialog */
.ship-dialog :deep(.el-dialog__body) { padding: 0 24px 20px; }
.ship-dialog :deep(.el-dialog__header) { padding: 20px 24px 0; }
.dialog-header { display: flex; align-items: center; gap: 12px; }
.dialog-icon { font-size: 32px; }
.dialog-header h4 { margin: 0; font-size: 16px; font-weight: 700; color: #111827; }
.dialog-sub { margin: 2px 0 0; font-size: 13px; color: #9ca3af; }
.ship-order-info { background: #f8fafc; border-radius: 10px; padding: 14px 16px; margin-bottom: 20px; display: flex; flex-direction: column; gap: 6px; }
.ship-field { display: flex; gap: 8px; font-size: 13px; align-items: center; }
.ship-field-label { color: #6b7280; min-width: 56px; font-size: 12px; }
.ship-form { display: flex; flex-direction: column; gap: 16px; }
.ship-form-row label { display: block; font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 6px; }

/* Note dialog */
.note-order-info { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
.note-footer { display: flex; justify-content: space-between; align-items: center; }
.btn-delete { padding: 8px 14px; border-radius: 8px; border: 1px solid #fecaca; background: #fff; color: #dc2626; font-size: 13px; cursor: pointer; }
.btn-delete:hover { background: #fef2f2; }

/* Shared btn styles */
.btn-cancel { padding: 8px 20px; border-radius: 8px; border: 1px solid #d1d5db; background: #fff; color: #374151; font-size: 14px; cursor: pointer; }
.btn-confirm { padding: 8px 20px; border-radius: 8px; border: none; background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; transition: opacity 0.15s; margin-left: 8px; }
.btn-confirm:hover { opacity: 0.9; }
.btn-confirm:disabled { opacity: 0.5; cursor: not-allowed; }

/* Pagination */
.pagination-bar { display: flex; justify-content: center; margin-top: 20px; padding: 8px 0; }
</style>
