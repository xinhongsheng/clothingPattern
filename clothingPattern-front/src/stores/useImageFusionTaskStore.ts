import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ImageFusionTaskNotification {
  taskId: string
  status: 'PENDING' | 'PROCESSING' | 'SUCCEEDED' | 'FAILED'
  resultUrls?: string[]
  errorMessage?: string
  createTime?: number
  updateTime?: number
  garmentUrl?: string
  patternUrls?: string[]
  similarity?: number
  read: boolean
}

const STORAGE_KEY = 'image_fusion_task_notification'

export const useImageFusionTaskStore = defineStore('imageFusionTask', () => {
  const notification = ref<ImageFusionTaskNotification | null>(null)
  const hasUnread = ref(false)

  const loadFromStorage = () => {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return
    }
    try {
      const data = JSON.parse(raw) as ImageFusionTaskNotification
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
    garmentUrl?: string
    patternUrls?: string[]
    similarity?: number
  }) => {
    notification.value = {
      taskId: params.taskId,
      status: 'PENDING',
      garmentUrl: params.garmentUrl,
      patternUrls: params.patternUrls,
      similarity: params.similarity,
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

  const markSucceeded = (taskId: string, resultUrls?: string[]) => {
    if (notification.value && notification.value.taskId === taskId) {
      notification.value.status = 'SUCCEEDED'
      notification.value.resultUrls = resultUrls
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
      garmentUrl: notification.value.garmentUrl,
      patternUrls: notification.value.patternUrls,
      similarity: notification.value.similarity,
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
