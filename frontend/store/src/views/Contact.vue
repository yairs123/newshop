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
            <h4>🤖 {{ $t('contact.aiAssistant', '智能客服') }}</h4>
            <p class="bot-status">
              <span class="status-dot"></span>
              {{ $t('contact.online24h', '24 小时在线') }}
            </p>
          </div>
          <div class="info-card">
            <h4>📞 {{ $t('contact.phone') }}</h4>
            <p>{{ $t('contact.phoneLine') }}</p>
          </div>
          <div class="info-card">
            <h4>✉️ {{ $t('contact.email') }}</h4>
            <p>support@coinmarket.com</p>
          </div>
          <div class="info-card">
            <h4>🏢 {{ $t('contact.address') }}</h4>
            <p>{{ $t('contact.addressLine') }}</p>
          </div>
        </div>

        <!-- 聊天窗口 -->
        <div class="chat-window">
          <div class="chat-header">
            <div class="chat-bot-avatar">🤖</div>
            <div class="chat-header-info">
              <strong>CoinMarket 客服</strong>
              <span class="bot-status"><span class="status-dot"></span>{{ $t('contact.online24h', '24 小时在线') }}</span>
            </div>
          </div>

          <div ref="msgContainer" class="chat-messages">
            <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.role">
              <div v-if="msg.role === 'bot'" class="msg-avatar">🤖</div>
              <div class="msg-bubble" v-html="formatMsg(msg.text)"></div>
              <div v-if="msg.role === 'user'" class="msg-avatar user">👤</div>
            </div>
            <!-- 打字指示 -->
            <div v-if="typing" class="msg-row bot">
              <div class="msg-avatar">🤖</div>
              <div class="msg-bubble typing">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
              </div>
            </div>
          </div>

          <!-- 快捷问题 -->
          <div class="quick-questions">
            <button v-for="(q, i) in quickQuestions" :key="i" @click="sendQuick(q)">
              {{ q }}
            </button>
          </div>

          <div class="chat-input">
            <input
              v-model="input"
              :placeholder="$t('contact.typeQuestion', '输入您的问题...')"
              @keyup.enter="send"
              :disabled="typing"
            />
            <button @click="send" :disabled="typing" :class="{ disabled: typing }">
              {{ $t('contact.send', '发送') }}
            </button>
          </div>

          <!-- 人工留言折叠区 -->
          <div class="human-fallback">
            <button class="fallback-toggle" @click="showForm = !showForm">
              {{ showForm ? '▲' : '▼' }} {{ $t('contact.needHuman', '需要人工帮助？留言给我们') }}
            </button>
            <div v-if="showForm" class="message-form">
              <el-form label-position="top">
                <el-form-item :label="$t('account.subject')">
                  <el-input v-model="form.subject" />
                </el-form-item>
                <el-form-item :label="$t('account.describeIssue')">
                  <el-input v-model="form.message" type="textarea" :rows="3" />
                </el-form-item>
                <el-button type="primary" size="small" @click="submitForm" :loading="submitting">
                  {{ $t('account.submit') }}
                </el-button>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '../api'
import { ElMessage } from 'element-plus'
import { getBotReply, getGreeting, getQuickQuestions } from '../composables/useChatbot'

const { t, locale } = useI18n()
const messages = ref([])
const input = ref('')
const typing = ref(false)
const msgContainer = ref(null)
const showForm = ref(false)
const submitting = ref(false)
const form = reactive({ subject: '', message: '' })

const quickQuestions = getQuickQuestions(locale.value)

function formatMsg(text) {
  // 把 \n 转成 <br>
  return String(text).replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
}

function pushMsg(role, text) {
  messages.value.push({ role, text })
  scrollToBottom()
}

function sendQuick(q) {
  input.value = q
  send()
}

function send() {
  const text = input.value.trim()
  if (!text || typing.value) return
  pushMsg('user', text)
  input.value = ''
  typing.value = true

  // 模拟思考延迟
  setTimeout(() => {
    const reply = getBotReply(text, locale.value)
    typing.value = false
    pushMsg('bot', reply)
    // 答不上来时提示留言
    if (reply.startsWith('🤔')) {
      showForm.value = true
    }
  }, 600)
}

async function submitForm() {
  if (!form.subject || !form.message) {
    ElMessage.warning(t('common.fillRequiredFields'))
    return
  }
  submitting.value = true
  try {
    await api.post('/contact', form)
    ElMessage.success(t('contact.success'))
    form.subject = ''
    form.message = ''
    showForm.value = false
  } catch (e) {
    ElMessage.error(t('common.failedToSend'))
  }
  submitting.value = false
}

onMounted(() => {
  pushMsg('bot', getGreeting(locale.value))
})
</script>

<style scoped>
.contact-page { background: #faf8f5; min-height: 60vh; }
.page-inner { max-width: 900px; margin: 0 auto; padding: 40px 24px; }
h1 { font-size: 32px; font-weight: 700; color: #1a1a2e; }
.subtitle { color: #8a8a93; margin: 8px 0 32px; }
.contact-grid { display: grid; grid-template-columns: 320px 1fr; gap: 32px; align-items: start; }
.info-card { margin-bottom: 20px; padding: 16px 20px; background: #fff; border: 1px solid #ece7e0; border-radius: 12px; }
.info-card h4 { font-size: 14px; font-weight: 600; color: #1a1a2e; margin-bottom: 4px; }
.info-card p { font-size: 13px; color: #6b7280; }
.bot-status { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #059669; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: #10b981; display: inline-block; animation: pulse 2s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.4} }

/* Chat window */
.chat-window { background: #fff; border: 1px solid #ece7e0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(26,26,46,.06); }
.chat-header { display: flex; align-items: center; gap: 12px; padding: 16px 20px; background: linear-gradient(135deg, #1a1a2e, #3a2f14); color: #fff; }
.chat-bot-avatar { width: 40px; height: 40px; border-radius: 50%; background: #d4a843; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.chat-header-info { display: flex; flex-direction: column; }
.chat-header-info strong { font-size: 15px; }
.chat-header-info .bot-status { color: #a7f3d0; font-size: 12px; }

.chat-messages { height: 380px; overflow-y: auto; padding: 20px; background: #faf8f5; display: flex; flex-direction: column; gap: 12px; }
.msg-row { display: flex; gap: 8px; align-items: flex-start; max-width: 85%; }
.msg-row.user { align-self: flex-end; flex-direction: row-reverse; }
.msg-avatar { width: 30px; height: 30px; border-radius: 50%; background: #d4a843; display: flex; align-items: center; justify-content: center; font-size: 15px; flex-shrink: 0; }
.msg-avatar.user { background: #3b82f6; }
.msg-bubble { padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.6; color: #1f2937; background: #fff; border: 1px solid #ece7e0; white-space: pre-line; }
.msg-row.user .msg-bubble { background: #d4a843; color: #fff; border: none; border-radius: 14px 4px 14px 14px; }

/* Typing indicator */
.msg-bubble.typing { display: flex; gap: 4px; align-items: center; padding: 14px 18px; }
.msg-bubble.typing .dot { width: 6px; height: 6px; border-radius: 50%; background: #b8860b; animation: bounce 1.2s infinite; }
.msg-bubble.typing .dot:nth-child(2) { animation-delay: .2s; }
.msg-bubble.typing .dot:nth-child(3) { animation-delay: .4s; }
@keyframes bounce { 0%,80%,100%{transform:translateY(0)} 40%{transform:translateY(-5px)} }

/* Quick questions */
.quick-questions { display: flex; flex-wrap: wrap; gap: 6px; padding: 12px 16px; border-top: 1px solid #f0ece6; }
.quick-questions button { border: 1px solid #e5e0d6; background: #fff; color: #6b7280; font-size: 12px; padding: 5px 12px; border-radius: 20px; cursor: pointer; transition: all .15s; }
.quick-questions button:hover { border-color: #b8860b; color: #b8860b; background: #fffbeb; }

/* Input */
.chat-input { display: flex; gap: 8px; padding: 12px 16px; border-top: 1px solid #f0ece6; }
.chat-input input { flex: 1; border: 1px solid #e5e0d6; border-radius: 10px; padding: 10px 14px; font-size: 14px; outline: none; }
.chat-input input:focus { border-color: #b8860b; }
.chat-input button { background: linear-gradient(135deg, #b8860b, #d4a843); color: #fff; border: none; border-radius: 10px; padding: 0 20px; font-size: 14px; font-weight: 600; cursor: pointer; }
.chat-input button.disabled { opacity: .5; cursor: not-allowed; }

/* Human fallback */
.human-fallback { border-top: 1px solid #f0ece6; }
.fallback-toggle { width: 100%; padding: 10px; background: #faf8f5; border: none; color: #8a8a93; font-size: 13px; cursor: pointer; }
.fallback-toggle:hover { color: #b8860b; }
.message-form { padding: 16px 20px 20px; }

@media (max-width: 768px) { .contact-grid { grid-template-columns: 1fr; } }
</style>
