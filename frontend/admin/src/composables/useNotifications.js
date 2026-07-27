import { ref, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

let stompClient = null
const connected = ref(false)

export function useNotifications() {
  let subscriptions = []

  function connect() {
    const token = localStorage.getItem('token')
    if (!token || stompClient?.active) return

    const socket = new SockJS('/ws')
    stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        connected.value = true
        const sub = stompClient.subscribe('/topic/admin/notifications', (message) => {
          try {
            const data = JSON.parse(message.body)
            handleNotification(data)
          } catch (e) {
            console.warn('Failed to parse notification:', e)
          }
        })
        subscriptions.push(sub)
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: () => {
        connected.value = false
      }
    })

    stompClient.activate()
  }

  function disconnect() {
    if (stompClient) {
      subscriptions.forEach(sub => {
        try { sub.unsubscribe() } catch (e) { /* ignore */ }
      })
      subscriptions = []
      stompClient.deactivate()
      stompClient = null
      connected.value = false
    }
  }

  function handleNotification(data) {
    switch (data.type) {
      case 'NEW_ORDER':
        ElNotification({
          title: '新订单',
          message: `订单 #${data.orderNo} 已创建`,
          type: 'success',
          duration: 5000
        })
        break
      case 'ORDER_STATUS':
        ElNotification({
          title: '订单状态更新',
          message: `订单 #${data.orderId} 状态已变更为: ${data.status}`,
          type: 'info',
          duration: 4000
        })
        break
      case 'LOW_STOCK':
        ElNotification({
          title: '库存不足',
          message: `商品 "${data.title}" 库存仅剩 ${data.stock}`,
          type: 'warning',
          duration: 6000
        })
        break
      default:
        ElNotification({
          title: '系统通知',
          message: data.message || JSON.stringify(data),
          type: 'info',
          duration: 3000
        })
    }
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connect,
    disconnect
  }
}
