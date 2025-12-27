import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

// MJ任务通知信息
export interface MJTaskNotification {
  taskId: string
  status: 'PENDING' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED'
  result?: any
  errorMessage?: string
  createTime?: number
  updateTime?: number
  // 保存创作参数用于回填
  prompt?: string
  style?: string
  season?: string
  targetAudience?: string
  // 是否已读
  read: boolean
}

const STORAGE_KEY = 'mj_task_notification'

// 定义MJ任务通知的Pinia Store
export const useMJTaskStore = defineStore('mjTask', () => {
  // 当前任务通知
  const notification = ref<MJTaskNotification | null>(null)
  // 是否有未读通知
  const hasUnread = ref(false)

  // 从localStorage读取
  const loadFromStorage = () => {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      try {
        const data = JSON.parse(raw) as MJTaskNotification
        notification.value = data
        // 生成成功且未读则显示未读状态
        hasUnread.value = data.status === 'SUCCEEDED' && !data.read
      } catch {
        notification.value = null
        hasUnread.value = false
      }
    }
  }

  // 保存到localStorage
  const saveToStorage = () => {
    if (notification.value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(notification.value))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  // 创建新任务
  const createTask = (params: {
    taskId: string
    prompt: string
    style?: string
    season?: string
    targetAudience?: string
  }) => {
    notification.value = {
      taskId: params.taskId,
      status: 'PENDING',
      prompt: params.prompt,
      style: params.style,
      season: params.season,
      targetAudience: params.targetAudience,
      createTime: Date.now(),
      read: false,
    }
    hasUnread.value = false
    saveToStorage()
  }

  // 更新任务状态为处理中
  const markProcessing = (taskId: string) => {
    if (notification.value && notification.value.taskId === taskId) {
      notification.value.status = 'PROCESSING'
      notification.value.updateTime = Date.now()
      saveToStorage()
    }
  }

  // 标记任务成功
  const markSucceeded = (taskId: string, result: any) => {
    if (notification.value && notification.value.taskId === taskId) {
      notification.value.status = 'SUCCEEDED'
      notification.value.result = result
      notification.value.updateTime = Date.now()
      notification.value.read = false
      hasUnread.value = true
      saveToStorage()
    }
  }

  // 标记任务失败
  const markFailed = (taskId: string, errorMessage: string) => {
    if (notification.value && notification.value.taskId === taskId) {
      notification.value.status = 'FAILED'
      notification.value.errorMessage = errorMessage
      notification.value.updateTime = Date.now()
      notification.value.read = false
      hasUnread.value = true
      saveToStorage()
    }
  }

  // 标记已读
  const markRead = () => {
    if (notification.value) {
      notification.value.read = true
      hasUnread.value = false
      saveToStorage()
    }
  }

  // 清除任务
  const clearTask = () => {
    notification.value = null
    hasUnread.value = false
    localStorage.removeItem(STORAGE_KEY)
  }

  // 获取创作参数（用于回填）
  const getCreationParams = () => {
    if (!notification.value) return null
    return {
      prompt: notification.value.prompt,
      style: notification.value.style,
      season: notification.value.season,
      targetAudience: notification.value.targetAudience,
    }
  }

  // 初始化时从localStorage加载
  loadFromStorage()

  return {
    notification,
    hasUnread,
    createTask,
    markProcessing,
    markSucceeded,
    markFailed,
    markRead,
    clearTask,
    getCreationParams,
    loadFromStorage,
  }
})
