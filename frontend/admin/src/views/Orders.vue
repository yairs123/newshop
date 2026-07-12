<template>
  <div class="orders-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h3>订单管理</h3>
        <p class="page-desc">查看和处理所有订单</p>
      </div>
      <el-button size="small" @click="load" :icon="Refresh" circle />
    </div>

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
        <div v-for="(o, idx) in orders" :key="o.id" class="order-row" :class="{ 'order-row-expand': expandedId === o.id }">
          <!-- 主行 -->
          <div class="order-main" @click="expandedId = expandedId === o.id ? null : o.id">
            <div class="order-cell order-index">#{{ idx + 1 + (page-1) * size }}</div>
            <div class="order-cell order-no-col">
              <code class="order-no">{{ o.orderNo }}</code>
            </div>
            <div class="order-cell order-status-col">
              <span class="status-pill" :class="'status-' + o.status">{{ STATUS_LABELS[o.status] || o.status }}</span>
            </div>
            <div class="order-cell order-amount-col">
              <span class="amount">{{ o.currency }} {{ formatPrice(o.totalAmount) }}</span>
            </div>
            <div class="order-cell order-buyer-col">{{ o.buyerName || '—' }}</div>
            <div class="order-cell order-time-col">{{ formatDate(o.createdAt) }}</div>
            <div class="order-cell order-actions-col" @click.stop>
              <div class="action-group">
                <!-- 智能提醒图标 -->
                <span v-if="getAlert(o)" class="alert-badge" :title="getAlert(o).text" :style="{ background: getAlert(o).color }">{{ getAlert(o).icon }}</span>
                <!-- 备注标记 -->
                <span class="note-badge" :class="{ 'has-note': o.adminNote }" @click="openNote(o)" title="备注">
                  {{ o.adminNote ? '📝' : '📄' }}
                </span>
                <button class="action-btn action-detail" @click="viewDetail(o)">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                  详情
                </button>
                <button v-if="o.status === 'PENDING_PAYMENT'" class="action-btn action-pay" @click="markPaid(o)">
                  💰 收款
                </button>
                <button v-if="o.status === 'PAID'" class="action-btn action-ship" @click="openShip(o)">
                  📦 发货
                </button>
                <button v-if="o.status === 'SHIPPED'" class="action-btn action-force" @click="forceComplete(o)">
                  ✅ 完成
                </button>
                <button v-if="o.status === 'PENDING_PAYMENT' || o.status === 'PAID'" class="action-btn action-cancel" @click="cancelOrder(o)">
                  ❌ 取消
                </button>
              </div>
            </div>
          </div>

          <!-- 展开详情 -->
          <div v-if="expandedId === o.id" class="order-expand">
            <div class="expand-inner">
              <div class="expand-section">
                <span class="expand-label">商品</span>
                <div v-for="item in (o.items || [])" :key="item.id" class="expand-item">
                  <span class="item-title">{{ item.productTitle }}</span>
                  <span class="item-qty">x{{ item.quantity }}</span>
                  <span class="item-subtotal">{{ o.currency }} {{ formatPrice(item.subtotal) }}</span>
                </div>
              </div>
              <div class="expand-section">
                <span class="expand-label">物流</span>
                <span class="expand-text">{{ o.trackingCompany ? o.trackingCompany + ' · ' + o.trackingNumber : '未发货' }}</span>
              </div>
              <div class="expand-section">
                <span class="expand-label">买家备注</span>
                <span class="expand-text">{{ o.buyerNote || '无' }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无订单" :image-size="80" />
    </el-card>

    <!-- 备注编辑弹窗 -->
    <el-dialog v-model="showNote" width="420px" :close-on-click-modal="false" class="note-dialog">
      <template #header>
        <div class="dialog-header">
          <span class="dialog-icon">📝</span>
          <div>
            <h4>订单备注</h4>
            <p class="dialog-sub">添加内部备注，方便日后查看</p>
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
        <div class="note-history" v-if="noteHistory.length">
          <div class="note-history-title">修改记录</div>
          <div v-for="(h, i) in noteHistory" :key="i" class="note-history-item">
            <span class="note-history-text">{{ h }}</span>
          </div>
        </div>
      </template>
      <template #footer>
        <button class="btn-cancel" @click="showNote = false">取消</button>
        <button class="btn-confirm" @click="saveNote" :disabled="savingNote">
          {{ savingNote ? '保存中...' : '💾 保存备注' }}
        </button>
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

    <!-- 发货弹窗 -->
    <el-dialog v-model="showShip" width="480px" :close-on-click-modal="false" class="ship-dialog">
      <template #header>
        <div class="dialog-header">
          <span class="dialog-icon">📦</span>
          <div>
            <h4>确认发货</h4>
            <p class="dialog-sub">填写物流信息后提交</p>
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
              <el-option label="📮 USPS" value="USPS" />
              <el-option label="🚚 UPS" value="UPS" />
              <el-option label="✈️ FedEx" value="FedEx" />
              <el-option label="🌍 DHL" value="DHL" />
              <el-option label="📬 EMS" value="EMS" />
              <el-option label="📦 顺丰速运" value="顺丰速运" />
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
const expandedId = ref(null)

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
const noteHistory = ref([])

/** 智能提醒：基于订单状态和时间算出是否需要关注 */
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
    const shippedDays = daysDiff > 0 ? daysDiff : 0  // rough
    if (shippedDays > 7) {
      return { icon: '🔵', text: `${Math.floor(shippedDays)}天未确认`, color: '#909399' }
    }
  }
  return null
}

function openNote(o) {
  noteOrder.value = o
  noteText.value = o.adminNote || ''
  showNote.value = true
}

async function saveNote() {
  savingNote.value = true
  try {
    await api.put(`/admin/orders/${noteOrder.value.id}`, { adminNote: noteText.value, reason: '管理员添加备注' })
    ElMessage.success('备注已保存')
    showNote.value = false
    // Update local state
    const found = orders.value.find(o => o.id === noteOrder.value.id)
    if (found) found.adminNote = noteText.value
  } catch (e) { /* handled */ }
  savingNote.value = false
}

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
    key,
    type: statusType(key),
    label: STATUS_LABELS[key] || key,
    count: counts[key] || 0
  }))
})

function statusType(s) {
  const map = { PENDING_PAYMENT: 'danger', PAID: 'success', SHIPPED: 'warning', COMPLETED: 'success', CANCELLED: 'info' }
  return map[s] || 'info'
}

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

function formatPrice(val) { return val != null ? Number(val).toFixed(2) : '0.00' }
function formatDate(d) { return d ? d.slice(0, 16).replace('T', ' ') : '-' }
function viewDetail(row) { router.push('/orders/' + row.id + '/invoice') }

async function markPaid(row) {
  try {
    await ElMessageBox.confirm(`确认收到 ${row.orderNo} 的付款？`, '确认收款', {
      confirmButtonText: '确认', cancelButtonText: '取消', type: 'info'
    })
    await api.post(`/admin/orders/${row.id}/mark-paid`)
    ElMessage.success('已标记为待发货')
    load()
  } catch (e) { /* cancelled */ }
}

async function cancelOrder(row) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${row.orderNo} ？`, '取消订单', {
      confirmButtonText: '确认取消', cancelButtonText: '再想想', type: 'warning'
    })
    await api.post(`/admin/orders/${row.id}/cancel`)
    ElMessage.success('订单已取消')
    load()
  } catch (e) { /* cancelled */ }
}

async function forceComplete(row) {
  try {
    await ElMessageBox.confirm(`确定强制完成订单 ${row.orderNo} ？`, '确认', {
      confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning'
    })
    await api.post(`/admin/orders/${row.id}/complete`)
    ElMessage.success('操作成功')
    load()
  } catch (e) { /* cancelled or error */ }
}

onMounted(() => load())
</script>

<style scoped>
.orders-page { max-width: 1280px; }

/* Header */
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-header h3 { font-size: 20px; font-weight: 700; color: #111827; margin: 0; }
.page-desc { font-size: 13px; color: #9ca3af; margin-top: 2px; }

/* Stats bar */
.stats-bar { margin-bottom: 20px; }
.stat-chip {
  background: #fff; border-radius: 10px; padding: 12px; cursor: pointer;
  text-align: center; border: 2px solid #f0f0f0; transition: all 0.2s;
}
.stat-chip:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.chip-count { display: block; font-size: 22px; font-weight: 800; line-height: 1.2; }
.chip-label { display: block; font-size: 11px; margin-top: 2px; font-weight: 500; }
.chip-danger .chip-count { color: #f56c6c; }
.chip-success .chip-count { color: #67c23a; }
.chip-warning .chip-count { color: #e6a23c; }
.chip-info .chip-count { color: #909399; }
.chip-danger .chip-label { color: #f56c6c; }
.chip-success .chip-label { color: #67c23a; }
.chip-warning .chip-label { color: #e6a23c; }
.chip-info .chip-label { color: #909399; }

/* List card */
.list-card { border-radius: 12px; overflow: hidden; border: 1px solid #e5e7eb; }

/* Order row */
.order-row { border-bottom: 1px solid #f3f4f6; transition: background 0.15s; }
.order-row:last-child { border-bottom: none; }
.order-row:hover { background: #f9fafb; }
.order-main {
  display: grid; grid-template-columns: 50px 1fr 90px 120px 100px 140px 120px;
  align-items: center; padding: 14px 16px; cursor: pointer; gap: 8px;
}
.order-cell { font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.order-index { color: #9ca3af; font-weight: 500; font-size: 12px; }
.order-no { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 12px; color: #409eff; font-weight: 600; }

/* Status pill */
.status-pill {
  display: inline-block; padding: 3px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; letter-spacing: 0.02em;
}
.status-PENDING_PAYMENT { background: #fef2f2; color: #dc2626; }
.status-PAID { background: #f0fdf4; color: #16a34a; }
.status-SHIPPED { background: #fffbeb; color: #d97706; }
.status-COMPLETED { background: #f0fdf4; color: #16a34a; }
.status-CANCELLED { background: #f3f4f6; color: #6b7280; }

.amount { font-weight: 700; color: #dc2626; font-size: 14px; }
.order-time-col { color: #6b7280; font-size: 12px; }

/* Action buttons */
/* Alert & Note badges */
.alert-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 24px; height: 24px; border-radius: 50%;
  font-size: 12px; cursor: default; flex-shrink: 0;
  animation: pulse 2s infinite;
}
@keyframes pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.1); } }
.note-badge {
  display: inline-flex; align-items: center; justify-content: center;
  width: 24px; height: 24px; border-radius: 50%; cursor: pointer;
  font-size: 12px; transition: background 0.15s;
}
.note-badge.has-note { background: #fef3c7; }
.note-badge:hover { background: #f3f4f6; }

/* Note dialog */
.note-order-info { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; }
.note-history { margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.note-history-title { font-size: 12px; font-weight: 600; color: #9ca3af; margin-bottom: 6px; }
.note-history-item { font-size: 12px; color: #6b7280; padding: 4px 0; }

.action-group { display: flex; gap: 6px; }
.action-btn {
  padding: 5px 12px; border-radius: 8px; border: 1px solid #e5e7eb;
  display: inline-flex; align-items: center; gap: 4px;
  background: #fff; cursor: pointer; color: #374151; transition: all 0.15s;
  font-size: 12px; font-weight: 500;
}
.action-btn:hover { transform: translateY(-1px); box-shadow: 0 2px 6px rgba(0,0,0,.06); }
.action-detail { color: #3b82f6; border-color: #bfdbfe; }
.action-detail:hover { background: #eff6ff; }
.action-ship { background: #fffbeb; color: #d97706; border-color: #fde68a; }
.action-ship:hover { background: #fef3c7; }
.action-force { background: #f0fdf4; color: #16a34a; border-color: #bbf7d0; }
.action-force:hover { background: #dcfce7; }
.action-pay { background: #f0f9ff; color: #0284c7; border-color: #bae6fd; }
.action-pay:hover { background: #e0f2fe; }
.action-cancel { color: #dc2626; border-color: #fecaca; }
.action-cancel:hover { background: #fef2f2; }

/* Expanded detail */
.order-expand { background: #f8fafc; border-top: 1px solid #f0f0f0; }
.expand-inner { padding: 16px 24px 16px 66px; display: flex; flex-direction: column; gap: 8px; }
.expand-section { display: flex; gap: 12px; align-items: baseline; }
.expand-label { font-size: 11px; font-weight: 600; color: #9ca3af; text-transform: uppercase; min-width: 56px; letter-spacing: 0.04em; }
.expand-text { font-size: 13px; color: #374151; }
.expand-item { display: flex; gap: 8px; align-items: center; font-size: 13px; }
.item-title { color: #374151; }
.item-qty { color: #9ca3af; }
.item-subtotal { color: #dc2626; font-weight: 600; margin-left: auto; }

/* Pagination */
.pagination-bar { display: flex; justify-content: center; margin-top: 20px; padding: 8px 0; }

/* Ship dialog */
.ship-dialog :deep(.el-dialog__body) { padding: 0 24px 20px; }
.ship-dialog :deep(.el-dialog__header) { padding: 20px 24px 0; }
.dialog-header { display: flex; align-items: center; gap: 12px; }
.dialog-icon { font-size: 32px; }
.dialog-header h4 { margin: 0; font-size: 16px; font-weight: 700; color: #111827; }
.dialog-sub { margin: 2px 0 0; font-size: 13px; color: #9ca3af; }

.ship-order-info {
  background: #f8fafc; border-radius: 10px; padding: 14px 16px; margin-bottom: 20px;
  display: flex; flex-direction: column; gap: 6px;
}
.ship-field { display: flex; gap: 8px; font-size: 13px; align-items: center; }
.ship-field-label { color: #6b7280; min-width: 56px; font-size: 12px; }

.ship-form { display: flex; flex-direction: column; gap: 16px; }
.ship-form-row label { display: block; font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 6px; }

.btn-cancel {
  padding: 8px 20px; border-radius: 8px; border: 1px solid #d1d5db;
  background: #fff; color: #374151; font-size: 14px; cursor: pointer;
}
.btn-confirm {
  padding: 8px 20px; border-radius: 8px; border: none;
  background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: opacity 0.15s;
}
.btn-confirm:hover { opacity: 0.9; }
.btn-confirm:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
