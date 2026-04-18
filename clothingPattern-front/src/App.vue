<template>
  <div id="app">
    <BasicLayout></BasicLayout>
  </div>
</template>
<script setup lang="ts">
import BasicLayout from "@/layout/BasicLayout.vue"
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useMJTaskStore } from '@/stores/useMJTaskStore'
import { useImageFusionTaskStore } from '@/stores/useImageFusionTaskStore'
import { useTryOnTaskStore } from '@/stores/useTryOnTaskStore'
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getImagineStatus } from '@/api/bailianImageApi'
import { getResults, queryStatus } from '@/api/imageFusionController'
import { getStatus as getTryOnStatus } from '@/api/aiTryOnController'

const loginUserStore = useLoginUserStore()
const mjTaskStore = useMJTaskStore()
const fusionTaskStore = useImageFusionTaskStore()
const tryOnTaskStore = useTryOnTaskStore()
const route = useRoute()

const mjStorageKey = 'mj_generate_task'
const fusionStorageKey = 'image_fusion_task'
const tryOnStorageKey = 'try_on_task'
const pollIntervalMs = 2000
let pollTimer: ReturnType<typeof setInterval> | null = null

type MjTaskSnapshot = {
  taskId: string
  status: string
  result?: any
  errorMessage?: string
  updateTime?: number
  notified?: boolean
}

const readMjTaskSnapshot = (): MjTaskSnapshot | null => {
  const raw = localStorage.getItem(mjStorageKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as MjTaskSnapshot
  } catch {
    return null
  }
}

const saveMjTaskSnapshot = (snapshot: MjTaskSnapshot) => {
  localStorage.setItem(mjStorageKey, JSON.stringify(snapshot))
}

type FusionTaskSnapshot = {
  taskId: string
  status: string
  resultUrls?: string[]
  errorMessage?: string
  updateTime?: number
  notified?: boolean
  garmentUrl?: string
  patternUrls?: string[]
  similarity?: number
}

const readFusionTaskSnapshot = (): FusionTaskSnapshot | null => {
  const raw = localStorage.getItem(fusionStorageKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as FusionTaskSnapshot
  } catch {
    return null
  }
}

const saveFusionTaskSnapshot = (snapshot: FusionTaskSnapshot) => {
  localStorage.setItem(fusionStorageKey, JSON.stringify(snapshot))
}

type TryOnTaskSnapshot = {
  taskId: string
  status: string
  resultUrl?: string
  errorMessage?: string
  updateTime?: number
  notified?: boolean
  personImageUrl?: string
  topGarmentUrl?: string
  bottomGarmentUrl?: string
}

const readTryOnTaskSnapshot = (): TryOnTaskSnapshot | null => {
  const raw = localStorage.getItem(tryOnStorageKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as TryOnTaskSnapshot
  } catch {
    return null
  }
}

const saveTryOnTaskSnapshot = (snapshot: TryOnTaskSnapshot) => {
  localStorage.setItem(tryOnStorageKey, JSON.stringify(snapshot))
}

const pollMjTaskStatus = async () => {
  const snapshot = readMjTaskSnapshot()
  if (!snapshot) {
    return
  }
  if (route.path === '/mj/generation') {
    return
  }
  if (snapshot.status === 'SUCCEEDED' || snapshot.status === 'FAILED') {
    return
  }
  try {
    const res = await getImagineStatus({ taskId: snapshot.taskId })
    if (res.data.code !== 0 || !res.data.data) {
      return
    }
    const taskData = res.data.data
    if (taskData.status === 'SUCCEEDED' && taskData.result) {
      saveMjTaskSnapshot({
        taskId: snapshot.taskId,
        status: taskData.status,
        result: taskData.result,
        updateTime: taskData.updateTime,
        notified: true,
      })
      mjTaskStore.markSucceeded(snapshot.taskId, taskData.result)
      if (!snapshot.notified) {
        message.success('图案生成完成，返回智能创作页可继续变体/放大/保存')
      }
      return
    }
    if (taskData.status === 'FAILED') {
      saveMjTaskSnapshot({
        taskId: snapshot.taskId,
        status: taskData.status,
        errorMessage: taskData.errorMessage,
        updateTime: taskData.updateTime,
        notified: true,
      })
      mjTaskStore.markFailed(snapshot.taskId, taskData.errorMessage || '图案生成失败')
      if (!snapshot.notified) {
        message.error(taskData.errorMessage || '图案生成失败，请稍后重试')
      }
      return
    }
    if (taskData.status === 'PROCESSING') {
      mjTaskStore.markProcessing(snapshot.taskId)
    }
    saveMjTaskSnapshot({
      taskId: snapshot.taskId,
      status: taskData.status || snapshot.status,
      updateTime: taskData.updateTime,
      notified: snapshot.notified,
    })
  } catch {
    return
  }
}

const pollFusionTaskStatus = async () => {
  const snapshot = readFusionTaskSnapshot()
  if (!snapshot) {
    return
  }
  if (route.path === '/image-fusion') {
    return
  }
  if (snapshot.status === 'SUCCEEDED' || snapshot.status === 'FAILED') {
    return
  }
  try {
    const res = await queryStatus({ taskId: snapshot.taskId })
    const taskData: any = res?.data?.data || res?.data
    if (!taskData) {
      return
    }
    const status = taskData.taskStatus
    if (status === 'SUCCEEDED') {
      const resultRes = await getResults({ taskId: snapshot.taskId })
      const resultData: any = resultRes?.data?.data || resultRes?.data
      let resultUrls: string[] = []

      if (Array.isArray(resultData)) {
        resultUrls = resultData
      } else if (resultData?.results && Array.isArray(resultData.results)) {
        resultUrls = resultData.results.map((item: any) => item.url || item)
      } else if (Array.isArray(taskData?.localImageUrlList)) {
        resultUrls = taskData.localImageUrlList
      }

      saveFusionTaskSnapshot({
        ...snapshot,
        status,
        resultUrls,
        updateTime: taskData.updateTime,
        notified: true,
      })
      fusionTaskStore.markSucceeded(snapshot.taskId, resultUrls)
      if (!snapshot.notified) {
        message.success('图案融合完成，返回衣图智融页可查看最新效果')
      }
      return
    }
    if (status === 'FAILED') {
      const errorMessage = taskData.errorMessage || '图案融合失败，请稍后重试'
      saveFusionTaskSnapshot({
        ...snapshot,
        status,
        errorMessage,
        updateTime: taskData.updateTime,
        notified: true,
      })
      fusionTaskStore.markFailed(snapshot.taskId, errorMessage)
      if (!snapshot.notified) {
        message.error(errorMessage)
      }
      return
    }
    if (status === 'PROCESSING') {
      fusionTaskStore.markProcessing(snapshot.taskId)
    }
    saveFusionTaskSnapshot({
      ...snapshot,
      status: status || snapshot.status,
      updateTime: taskData.updateTime,
      notified: snapshot.notified,
    })
  } catch {
    return
  }
}

const pollTryOnTaskStatus = async () => {
  const snapshot = readTryOnTaskSnapshot()
  if (!snapshot) {
    return
  }
  if (route.path === '/ai/try-on') {
    return
  }
  if (snapshot.status === 'SUCCEEDED' || snapshot.status === 'FAILED') {
    return
  }
  try {
    const res = await getTryOnStatus({ taskId: snapshot.taskId })
    const taskData: any = res?.data
    if (!taskData) {
      return
    }
    const status = taskData.taskStatus
    const isSuccess = status === 'SUCCESS' || status === 'SUCCEEDED'

    if (isSuccess) {
      const resultUrl = taskData.localResultUrl || taskData.resultImageUrl || ''
      saveTryOnTaskSnapshot({
        ...snapshot,
        status: 'SUCCEEDED',
        resultUrl,
        updateTime: taskData.updateTime,
        notified: true,
      })
      tryOnTaskStore.markSucceeded(snapshot.taskId, resultUrl)
      if (!snapshot.notified) {
        message.success('试衣生成完成，返回AI试衣页可查看最新效果')
      }
      return
    }

    if (status === 'FAILED') {
      const errorMessage = taskData.errorMessage || '试衣任务失败，请稍后重试'
      saveTryOnTaskSnapshot({
        ...snapshot,
        status: 'FAILED',
        errorMessage,
        updateTime: taskData.updateTime,
        notified: true,
      })
      tryOnTaskStore.markFailed(snapshot.taskId, errorMessage)
      if (!snapshot.notified) {
        message.error(errorMessage)
      }
      return
    }

    if (status === 'PROCESSING' || status === 'PENDING' || status === 'RUNNING') {
      tryOnTaskStore.markProcessing(snapshot.taskId)
    }
    saveTryOnTaskSnapshot({
      ...snapshot,
      status: status || snapshot.status,
      updateTime: taskData.updateTime,
      notified: snapshot.notified,
    })
  } catch {
    return
  }
}

// 应用初始化时自动获取登录用户信息
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  pollTimer = setInterval(() => {
    void pollMjTaskStatus()
    void pollFusionTaskStatus()
    void pollTryOnTaskStatus()
  }, pollIntervalMs)
})

onBeforeUnmount(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>
<style scoped></style>
