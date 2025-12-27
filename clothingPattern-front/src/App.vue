<template>
  <div id="app">
    <BasicLayout></BasicLayout>
  </div>
</template>
<script setup lang="ts">
import BasicLayout from "@/layout/BasicLayout.vue"
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getImagineStatus } from '@/api/midjourneyjiekou'

const loginUserStore = useLoginUserStore()
const route = useRoute()

const storageKey = 'mj_generate_task'
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

const readTaskSnapshot = (): MjTaskSnapshot | null => {
  const raw = localStorage.getItem(storageKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as MjTaskSnapshot
  } catch {
    return null
  }
}

const saveTaskSnapshot = (snapshot: MjTaskSnapshot) => {
  localStorage.setItem(storageKey, JSON.stringify(snapshot))
}

const pollMjTaskStatus = async () => {
  const snapshot = readTaskSnapshot()
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
      saveTaskSnapshot({
        taskId: snapshot.taskId,
        status: taskData.status,
        result: taskData.result,
        updateTime: taskData.updateTime,
        notified: true,
      })
      if (!snapshot.notified) {
        message.success('图案生成完成，返回智能创作页可继续变体/放大/保存')
      }
      return
    }
    if (taskData.status === 'FAILED') {
      saveTaskSnapshot({
        taskId: snapshot.taskId,
        status: taskData.status,
        errorMessage: taskData.errorMessage,
        updateTime: taskData.updateTime,
        notified: true,
      })
      if (!snapshot.notified) {
        message.error(taskData.errorMessage || '图案生成失败，请稍后重试')
      }
      return
    }
    saveTaskSnapshot({
      taskId: snapshot.taskId,
      status: taskData.status || snapshot.status,
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
  pollTimer = setInterval(pollMjTaskStatus, pollIntervalMs)
})

onBeforeUnmount(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>
<style scoped></style>
