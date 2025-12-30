import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface TryOnTaskNotification {
  taskId: string
  status: 'PENDING' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED'
  resultUrl?: string
  errorMessage?: string
  createTime?: number
  updateTime?: number
  personImageUrl?: string
  topGarmentUrl?: string
  bottomGarmentUrl?: string
  read: boolean
}

const STORAGE_KEY = 'try_on_task_notification'

export const useTryOnTaskStore = defineStore('tryOnTask', () => {
  const notification = ref<TryOnTaskNotification | null>(null)
  const hasUnread = ref(false)

  const loadFromStorage = () => {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return
    }
    try {
      const data = JSON.parse(raw) as TryOnTaskNotification
      notification.value = data
      hasUnread.value = (data.status === 'SUCCEEDED' || data.status === 'FAILED') && !data.read
    } catch {
      notification.value = null
      hasUnread.value = false
    }
  }

  const saveToStorage = () => {
    if (notification.value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(notification.value))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  const createTask = (params: {
    taskId: string
    personImageUrl?: string
    topGarmentUrl?: string
    bottomGarmentUrl?: string
  }) => {
    notification.value = {
      taskId: params.taskId,
      status: 'PENDING',
      personImageUrl: params.personImageUrl,
      topGarmentUrl: params.topGarmentUrl,
      bottomGarmentUrl: params.bottomGarmentUrl,
      createTime: Date.now(),
      read: false,
    }
    hasUnread.value = false
    saveToStorage()
  }

  const markProcessing = (taskId: string) => {
    if (notification.value && notification.value.taskId === taskId) {
      notification.value.status = 'PROCESSING'
      notification.value.updateTime = Date.now()
      saveToStorage()
    }
  }

  const markSucceeded = (taskId: string, resultUrl?: string) => {
    if (notification.value && notification.value.taskId === taskId) {
      notification.value.status = 'SUCCEEDED'
      notification.value.resultUrl = resultUrl
      notification.value.updateTime = Date.now()
      notification.value.read = false
      hasUnread.value = true
      saveToStorage()
    }
  }

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

  const markRead = () => {
    if (notification.value) {
      notification.value.read = true
      hasUnread.value = false
      saveToStorage()
    }
  }

  const clearTask = () => {
    notification.value = null
    hasUnread.value = false
    localStorage.removeItem(STORAGE_KEY)
  }

  const getCreationParams = () => {
    if (!notification.value) {
      return null
    }
    return {
      personImageUrl: notification.value.personImageUrl,
      topGarmentUrl: notification.value.topGarmentUrl,
      bottomGarmentUrl: notification.value.bottomGarmentUrl,
    }
  }

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
