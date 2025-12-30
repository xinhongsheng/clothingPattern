<template>
  <div class="tryon-page">
    <!-- 主布局 - 左右分栏 -->
    <div class="main-layout">
      <!-- 左侧操作面板 -->
      <div class="left-panel">
        <!-- 页面标题 -->
        <div class="panel-header">
          <h1 class="panel-title">👗 AI 智能试衣</h1>
          <div class="header-btns">
            <a-button type="text" class="header-btn" @click="showGuideModal = true">
              <question-circle-outlined /> 指南
            </a-button>
            <a-button type="text" class="header-btn" @click="handleOpenHistoryDrawer">
              <history-outlined /> 历史
            </a-button>
          </div>
        </div>

        <!-- 预设模特选择 -->
        <div class="panel-section">
          <div class="section-header">
            <span>选择预设模特</span>
          </div>
          <div class="preset-models">
            <div
              v-for="model in presetModels"
              :key="model.key"
              :class="['preset-item', { active: selectedPresetKey === model.key }]"
              @click="handleSelectPreset(model)"
            >
              <img :src="model.assetUrl" :alt="model.name" />
              <span class="preset-name">{{ model.name }}</span>
            </div>
          </div>
        </div>

        <!-- 上传人物照片 -->
        <div class="panel-section">
          <div class="section-header">
            <span>人物照片</span>
            <span class="section-tip">或上传自定义照片</span>
          </div>
          <div class="upload-box">
            <a-upload
              v-if="!personImageUrl && !selectedPresetPreviewUrl"
              :show-upload-list="false"
              :before-upload="(file) => handleUpload('person', file)"
              accept="image/*"
            >
              <div class="upload-content">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1M16 7l-4-4m0 0L8 7m4-4v12"/>
                  </svg>
                </div>
                <div class="upload-text">上传人物照片</div>
                <div class="upload-tip">建议正面全身照，JPG/PNG ≤10MB</div>
              </div>
            </a-upload>
            <div v-else class="uploaded-preview">
              <a-image :src="personImageUrl || selectedPresetPreviewUrl" :preview="{ src: personImageUrl || selectedPresetPreviewUrl }" />
              <div class="preview-actions">
                <a-button size="small" @click="clearImage('person')">重新选择</a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 上传上衣图片 -->
        <div class="panel-section">
          <div class="section-header">上传上衣</div>
          <div class="upload-box small">
            <a-upload
              v-if="!topGarmentUrl"
              :show-upload-list="false"
              :before-upload="(file) => handleUpload('top', file)"
              accept="image/*"
            >
              <div class="upload-content">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1M16 7l-4-4m0 0L8 7m4-4v12"/>
                  </svg>
                </div>
                <div class="upload-text">选择上衣图片</div>
                <div class="upload-tip">可选，不上传则只试下装</div>
              </div>
            </a-upload>
            <div v-else class="uploaded-preview">
              <a-image :src="topGarmentUrl" :preview="{ src: topGarmentUrl }" />
              <div class="preview-actions">
                <a-button size="small" @click="clearImage('top')">重新上传</a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 上传下装图片 -->
        <div class="panel-section">
          <div class="section-header">上传下装</div>
          <div class="upload-box small">
            <a-upload
              v-if="!bottomGarmentUrl"
              :show-upload-list="false"
              :before-upload="(file) => handleUpload('bottom', file)"
              accept="image/*"
            >
              <div class="upload-content">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1M16 7l-4-4m0 0L8 7m4-4v12"/>
                  </svg>
                </div>
                <div class="upload-text">选择下装图片</div>
                <div class="upload-tip">可选，不上传则只试上衣</div>
              </div>
            </a-upload>
            <div v-else class="uploaded-preview">
              <a-image :src="bottomGarmentUrl" :preview="{ src: bottomGarmentUrl }" />
              <div class="preview-actions">
                <a-button size="small" @click="clearImage('bottom')">重新上传</a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部生成按钮 -->
        <div class="panel-footer">
          <a-button
            type="primary"
            block
            size="large"
            :loading="isTaskRunning"
            :disabled="isTaskRunning || (!personImageUrl && !selectedPresetKey) || (!topGarmentUrl && !bottomGarmentUrl)"
            @click="handleSubmit"
            class="generate-btn"
          >
            <span class="btn-text">{{ isTaskRunning ? '生成中...' : '开始AI试衣' }}</span>
          </a-button>
          <a-alert
            v-if="errorMessage"
            type="error"
            :message="errorMessage"
            show-icon
            class="error-alert"
          />
        </div>
      </div>

      <!-- 右侧结果展示区域 -->
      <div class="right-panel">
        <!-- 加载中状态 -->
        <div v-if="polling" class="result-loading">
          <a-spin size="large" />
          <div class="loading-text">
            <h3>正在生成试衣效果...</h3>
            <p>AI正在为您进行虚拟试衣，请稍候</p>
          </div>
        </div>

        <!-- 已生成结果 - 展示大图 -->
        <div v-else-if="resultImageUrl" class="result-content">
          <h2 class="result-title">AI 试衣效果</h2>
          <p class="result-subtitle">智能识别人物轮廓，真实还原服装穿着效果</p>

          <!-- 生成结果大图 -->
          <div class="result-main">
            <a-image
              :src="resultImageUrl"
              :preview="{ src: resultImageUrl }"
              class="result-large-image"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="result-actions">
            <a-button type="primary" @click="downloadResult" class="action-btn">
              <download-outlined /> 下载图片
            </a-button>
            <a-button @click="saveToHistory" class="action-btn">
              <save-outlined /> 保存图案
            </a-button>
            <a-button @click="shareResult" class="action-btn">
              <share-alt-outlined /> 分享
            </a-button>
          </div>

          <!-- 任务信息 -->
          <div class="result-meta" v-if="taskDetail">
            <span v-if="taskDetail.taskStatus">状态：{{ taskDetail.taskStatus }}</span>
            <span v-if="taskDetail.endTime">完成：{{ formatTime(taskDetail.endTime) }}</span>
          </div>
        </div>

        <!-- 空状态 - 显示引导 -->
        <div v-else class="result-empty">
          <h2 class="result-title">AI 智能试衣</h2>
          <p class="result-subtitle">上传人物与服装图片，AI 为你生成逼真的试穿效果</p>

          <!-- 步骤指示 -->
          <div class="flow-steps">
            <span>选择模特</span>
            <span class="flow-arrow">→</span>
            <span>上传服装</span>
            <span class="flow-arrow">→</span>
            <span>生成效果</span>
          </div>

          <!-- 功能特性 -->
          <div class="feature-grid">
            <div class="feature-item" v-for="feature in features" :key="feature.title">
              <span class="feature-icon">{{ feature.icon }}</span>
              <div class="feature-info">
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 使用指南弹窗 -->
    <a-modal
      v-model:open="showGuideModal"
      title="📘 AI 试衣使用指南"
      :footer="null"
      width="600px"
      class="guide-modal"
    >
      <div class="guide-content">
        <a-steps direction="vertical" :current="-1">
          <a-step title="选择模特">
            <template #description>
              <p>从预设模特中选择，或上传您自己的全身照</p>
            </template>
          </a-step>
          <a-step title="上传服装">
            <template #description>
              <p>上传上衣或下装图片，也可以同时上传</p>
            </template>
          </a-step>
          <a-step title="生成效果">
            <template #description>
              <p>点击"开始 AI 试衣"，等待 AI 生成试穿效果</p>
            </template>
          </a-step>
          <a-step title="保存分享">
            <template #description>
              <p>下载结果图片，或保存到历史记录</p>
            </template>
          </a-step>
        </a-steps>
        <a-divider />
        <div class="guide-tips">
          <h4>💡 最佳实践建议</h4>
          <ul>
            <li>人物照片：正面全身照，背景简洁，光线充足</li>
            <li>服装图片：平铺或模特展示，图案清晰可见</li>
            <li>图片格式：JPG 或 PNG，大小不超过 10MB</li>
            <li>生成时间：通常需要 30-60 秒，请耐心等待</li>
          </ul>
        </div>
      </div>
    </a-modal>

    <!-- 历史记录抽屉 -->
    <a-drawer
      v-model:open="showHistoryDrawer"
      title="试衣历史记录"
      placement="right"
      width="450"
      class="history-drawer"
    >
      <a-spin :spinning="historyLoading">
        <div class="history-content">
          <a-empty v-if="historyList.length === 0" description="暂无历史记录">
            <template #image>
              <history-outlined style="font-size: 64px; color: #666" />
            </template>
          </a-empty>
          <div v-else class="history-list">
            <div
              v-for="(item, index) in historyList"
              :key="index"
              class="history-item"
            >
              <a-image
                :src="item.localImageUrl"
                :width="100"
                :preview="true"
                class="history-image"
              />
              <div class="history-info">
                <p class="history-time">
                  <clock-circle-outlined /> {{ formatTime(item.submitTime) }}
                </p>
                <div class="history-actions">
                  <a-button type="primary" size="small" @click="loadHistoryItem(item)">
                    查看
                  </a-button>
                  <a-button size="small" @click="downloadHistoryImage(item.localImageUrl)">
                    下载
                  </a-button>
                  <a-button type="text" danger size="small" @click.stop="deleteHistoryItem(item, index)">
                    删除
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-spin>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {

  QuestionCircleOutlined,
  HistoryOutlined,
  DownloadOutlined,
  SaveOutlined,
  ShareAltOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { upload, submit, getStatus, getTryOnHistory, deleteTryOnRecord } from '@/api/aiTryOnController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useTryOnTaskStore } from '@/stores/useTryOnTaskStore'

const loginUserStore = useLoginUserStore()
const tryOnTaskStore = useTryOnTaskStore()

const storageKey = 'try_on_task'
const pollIntervalMs = 3000

const personImageUrl = ref<string>('')
const topGarmentUrl = ref<string>('')
const bottomGarmentUrl = ref<string>('')

// 预设模特列表
const presetModels = [
  {
    key: 'Aaron',
    name: 'Aaron',
    assetUrl: new URL('../assets/model/Aaron.png', import.meta.url).href,
  },
  {
    key: 'Asa',
    name: 'Asa',
    assetUrl: new URL('../assets/model/Asa.png', import.meta.url).href,
  },
  {
    key: 'Cyril',
    name: 'Cyril',
    assetUrl: new URL('../assets/model/Cyril.png', import.meta.url).href,
  },
  {
    key: 'Don',
    name: 'Don',
    assetUrl: new URL('../assets/model/Don.png', import.meta.url).href,
  },
  {
    key: 'Eli',
    name: 'Eli',
    assetUrl: new URL('../assets/model/Eli.png', import.meta.url).href,
  },
  {
    key: 'Eva',
    name: 'Eva',
    assetUrl: new URL('../assets/model/Eva.png', import.meta.url).href,
  },
  {
    key: 'Simon',
    name: 'Simon',
    assetUrl: new URL('../assets/model/Simon.png', import.meta.url).href,
  },
]

const selectedPresetKey = ref<string>('')

// 功能特性数据
const features = [
  {
    icon: '🧠',
    title: '智能识别',
    desc: 'AI 精准识别人物轮廓与服装款式',
  },
  {
    icon: '⚡',
    title: '快速生成',
    desc: '30秒内完成试衣效果生成',
  },
  {
    icon: '🎯',
    title: '真实还原',
    desc: '高度还原服装材质与穿着效果',
  },
  {
    icon: '🗂️',
    title: '历史保存',
    desc: '支持保存历史记录，随时查看',
  },
]

// 使用指南弹窗
const showGuideModal = ref(false)

// 历史记录抽屉
const showHistoryDrawer = ref(false)
const historyList = ref<API.QueryTaskHistoryResultVO[]>([])
const historyLoading = ref(false)

// 预设模特预览地址（用于人物预览，当还未上传到 COS 时使用本地 asset）
const selectedPresetPreviewUrl = computed(() => {
  const model = presetModels.find((m) => m.key === selectedPresetKey.value)
  return model ? model.assetUrl : ''
})

const isTaskRunning = computed(() => {
  const status = tryOnTaskStore.notification?.status
  return submitting.value || polling.value || status === 'PENDING' || status === 'PROCESSING'
})

const submitting = ref(false)
const polling = ref(false)
const currentTaskId = ref<string>('')
const resultImageUrl = ref<string>('')
const taskDetail = ref<API.TryOnTask | null>(null)
const errorMessage = ref<string>('')

let pollTimer: ReturnType<typeof window.setTimeout> | null = null

const clearPollTimer = () => {
  if (pollTimer !== null) {
    window.clearTimeout(pollTimer)
    pollTimer = null
  }
}

type TryOnTaskSnapshot = {
  taskId: string
  status: string
  resultUrl?: string
  errorMessage?: string
  updateTime?: number
  personImageUrl?: string
  topGarmentUrl?: string
  bottomGarmentUrl?: string
}

const readTaskSnapshot = (): TryOnTaskSnapshot | null => {
  const raw = localStorage.getItem(storageKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as TryOnTaskSnapshot
  } catch {
    return null
  }
}

const saveTaskSnapshot = (snapshot: TryOnTaskSnapshot) => {
  localStorage.setItem(storageKey, JSON.stringify(snapshot))
}

const stopPolling = () => {
  clearPollTimer()
}

const applySnapshot = (snapshot: TryOnTaskSnapshot) => {
  currentTaskId.value = snapshot.taskId
  if (snapshot.personImageUrl !== undefined) {
    personImageUrl.value = snapshot.personImageUrl || ''
  }
  if (snapshot.topGarmentUrl !== undefined) {
    topGarmentUrl.value = snapshot.topGarmentUrl || ''
  }
  if (snapshot.bottomGarmentUrl !== undefined) {
    bottomGarmentUrl.value = snapshot.bottomGarmentUrl || ''
  }
  if (snapshot.resultUrl) {
    resultImageUrl.value = snapshot.resultUrl
  }
}

const pollTryOnStatus = async (taskId: string) => {
  try {
    const res = await getStatus({ taskId })
    const data = res?.data
    taskDetail.value = data || null

    if (!data) {
      throw new Error('任务状态为空')
    }

    const status = data.taskStatus
    const isSuccess = status === 'SUCCESS' || status === 'SUCCEEDED'
    if (isSuccess) {
      const resultUrl = data.localResultUrl || data.resultImageUrl || ''
      resultImageUrl.value = resultUrl
      polling.value = false
      stopPolling()

      saveTaskSnapshot({
        taskId,
        status: 'SUCCEEDED',
        resultUrl,
        errorMessage: data.errorMessage,
        updateTime: data.updateTime,
        personImageUrl: personImageUrl.value,
        topGarmentUrl: topGarmentUrl.value,
        bottomGarmentUrl: bottomGarmentUrl.value,
      })

      tryOnTaskStore.markSucceeded(taskId, resultUrl)
      message.success('试衣任务已完成')
      return
    }

    if (status === 'FAILED') {
      errorMessage.value = data.errorMessage || '试衣任务失败，请稍后重试'
      polling.value = false
      stopPolling()

      saveTaskSnapshot({
        taskId,
        status: 'FAILED',
        errorMessage: errorMessage.value,
        updateTime: data.updateTime,
        personImageUrl: personImageUrl.value,
        topGarmentUrl: topGarmentUrl.value,
        bottomGarmentUrl: bottomGarmentUrl.value,
      })

      tryOnTaskStore.markFailed(taskId, errorMessage.value)
      return
    }

    const nextStatus = status === 'RUNNING' ? 'PROCESSING' : status || 'PENDING'
    polling.value = true
    tryOnTaskStore.markProcessing(taskId)
    saveTaskSnapshot({
      taskId,
      status: nextStatus,
      updateTime: data.updateTime,
      personImageUrl: personImageUrl.value,
      topGarmentUrl: topGarmentUrl.value,
      bottomGarmentUrl: bottomGarmentUrl.value,
    })

    stopPolling()
    pollTimer = window.setTimeout(() => {
      pollTryOnStatus(taskId)
    }, pollIntervalMs)
  } catch (e: any) {
    console.error('查询任务状态失败:', e)
    polling.value = false
    stopPolling()
    errorMessage.value = e?.message || '查询任务状态失败'
  }
}

onMounted(() => {
  void loginUserStore.fetchLoginUser()
  tryOnTaskStore.markRead()

  const snapshot = readTaskSnapshot()
  const creationParams = tryOnTaskStore.getCreationParams()
  if (snapshot) {
    applySnapshot(snapshot)
  } else if (creationParams) {
    personImageUrl.value = creationParams.personImageUrl || ''
    topGarmentUrl.value = creationParams.topGarmentUrl || ''
    bottomGarmentUrl.value = creationParams.bottomGarmentUrl || ''
  }

  if (snapshot?.status === 'SUCCEEDED' && snapshot.resultUrl) {
    polling.value = false
  } else if (snapshot?.status === 'PENDING' || snapshot?.status === 'PROCESSING' || snapshot?.status === 'RUNNING') {
    polling.value = true
    pollTryOnStatus(snapshot.taskId)
  }

  // 首次访问显示使用指南
  const hasVisited = localStorage.getItem('aiTryOn_hasVisited')
  if (!hasVisited) {
    setTimeout(() => {
      showGuideModal.value = true
      localStorage.setItem('aiTryOn_hasVisited', 'true')
    }, 500)
  }
})

onBeforeUnmount(() => {
  clearPollTimer()
})

const handleUpload = async (type: 'person' | 'top' | 'bottom', file: File) => {
  if (file.size > 10 * 1024 * 1024) {
    message.error('图片大小不能超过 10MB')
    return false
  }

  try {
    const formData = new FormData()
    formData.append('file', file)

    const res = await upload(formData as any)
    const url = res?.data
    if (!url) {
      throw new Error('上传失败，未获取到图片地址')
    }

    if (type === 'person') {
      personImageUrl.value = url
      // 用户手动上传人物图片时，清空预设选择
      selectedPresetKey.value = ''
    } else if (type === 'top') {
      topGarmentUrl.value = url
    } else if (type === 'bottom') {
      bottomGarmentUrl.value = url
    }

    message.success('图片上传成功')
  } catch (e: any) {
    console.error('上传失败:', e)
    message.error(e?.message || '上传失败，请稍后重试')
  }

  return false
}

// 选择预设模特：只记录选中，用于预览与后续提交时上传
const handleSelectPreset = (model: { key: string; name: string; assetUrl: string }) => {
  selectedPresetKey.value = model.key
  // 选中预设时，不立刻上传；如果之前有用户上传的人物照，则仅更新选中状态
  message.success(`已选择预设模特：${model.name}`)
}

const clearImage = (type: 'person' | 'top' | 'bottom') => {
  if (type === 'person') {
    // 清空人物图片（无论是上传的还是通过预设模特生成的 URL）
    personImageUrl.value = ''
    // 同时清空预设模特选择，这样再次点击“开始 AI 试衣”会强制用户重新选择/上传
    selectedPresetKey.value = ''
  }
  if (type === 'top') topGarmentUrl.value = ''
  if (type === 'bottom') bottomGarmentUrl.value = ''
}

const handleSubmit = async () => {
  // 必须要么上传人物照片，要么选择预设模特
  if (!personImageUrl.value && !selectedPresetKey.value) {
    message.warning('请先选择预设模特或上传人物照片')
    return
  }
  if (!topGarmentUrl.value && !bottomGarmentUrl.value) {
    message.warning('请至少上传上衣或下装图片')
    return
  }

  stopPolling()
  submitting.value = true
  polling.value = false
  errorMessage.value = ''
  resultImageUrl.value = ''
  taskDetail.value = null

  try {
    // 如果还没有人物的 COS 地址，但选中了预设模特，则此时先上传预设模特
    if (!personImageUrl.value && selectedPresetKey.value) {
      const model = presetModels.find((m) => m.key === selectedPresetKey.value)
      if (model) {
        const response = await fetch(model.assetUrl)
        const blob = await response.blob()
        const file = new File([blob], model.name + '.png', { type: blob.type || 'image/png' })

        const formData = new FormData()
        formData.append('file', file)

        const uploadRes = await upload(formData as any)
        const url = uploadRes?.data
        if (!url) {
          throw new Error('上传预设模特失败，未获取到图片地址')
        }
        personImageUrl.value = url
      }
    }

    const res = await submit({
      personImageUrl: personImageUrl.value,
      topGarmentUrl: topGarmentUrl.value || undefined,
      bottomGarmentUrl: bottomGarmentUrl.value || undefined,
    } as any)

    const taskId = res?.data
    if (!taskId) {
      throw new Error('未获取到任务 ID')
    }

    currentTaskId.value = taskId

    saveTaskSnapshot({
      taskId,
      status: 'PENDING',
      updateTime: Date.now(),
      personImageUrl: personImageUrl.value,
      topGarmentUrl: topGarmentUrl.value,
      bottomGarmentUrl: bottomGarmentUrl.value,
    })

    tryOnTaskStore.createTask({
      taskId,
      personImageUrl: personImageUrl.value,
      topGarmentUrl: topGarmentUrl.value,
      bottomGarmentUrl: bottomGarmentUrl.value,
    })

    message.success('任务提交成功，正在生成试衣效果...')
    polling.value = true
    pollTryOnStatus(taskId)
  } catch (e: any) {
    console.error('提交试衣任务失败:', e)
    errorMessage.value = e?.message || '提交试衣任务失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}



// 下载结果图片
const downloadResult = () => {
  if (!resultImageUrl.value) return

  const link = document.createElement('a')
  link.href = resultImageUrl.value
  link.download = `ai-tryon-${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  message.success('图片下载成功')
}

const clearCurrentResult = () => {
  stopPolling()
  currentTaskId.value = ''
  resultImageUrl.value = ''
  taskDetail.value = null
  errorMessage.value = ''
  polling.value = false
  submitting.value = false
  localStorage.removeItem(storageKey)
  tryOnTaskStore.clearTask()
}

// 保存到历史记录
const saveToHistory = () => {
  if (!resultImageUrl.value) return
  message.success('保存成功，可在历史记录中查看')
  clearCurrentResult()
}

// 从后端加载历史记录
const loadHistoryFromBackend = async () => {
  const userId = loginUserStore.loginUser?.id
  if (!userId) {
    message.warning('请先登录')
    return
  }

  historyLoading.value = true
  try {
    const res = await getTryOnHistory({ userId })
    if (res.data.code === 0 && res.data.data) {
      // 过滤掉无效数据（localImageUrl 为 null）
      historyList.value = res.data.data.filter(
        (item) => item.localImageUrl && item.submitTime
      )
    } else {
      message.error('加载历史记录失败：' + res.data.message)
    }
  } catch (e: any) {
    console.error('加载历史记录失败:', e)
    message.error(e?.message || '加载历史记录失败')
  } finally {
    historyLoading.value = false
  }
}

// 加载历史记录项
const loadHistoryItem = (item: API.QueryTaskHistoryResultVO) => {
  Modal.confirm({
    title: '查看历史记录',
    content: '是否查看此历史记录的试衣效果？',
    onOk: () => {
      resultImageUrl.value = item.localImageUrl || ''
      taskDetail.value = {
        taskStatus: 'SUCCESS',
        createTime: item.submitTime,
        endTime: item.endTime,
      } as any
      showHistoryDrawer.value = false
      message.success('历史记录已加载')
    },
  })
}

// 删除历史记录项（调用后端接口）
const deleteHistoryItem = async (item: API.QueryTaskHistoryResultVO, index: number) => {
  if (!item.id) {
    message.error('记录ID不存在，无法删除')
    return
  }

  Modal.confirm({
    title: '删除历史记录',
    content: '确定要删除此记录吗？删除后将无法恢复。',
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await deleteTryOnRecord({ id: item.id })
        if (res.data.code === 0 && res.data.data) {
          historyList.value.splice(index, 1)
          message.success('删除成功')
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (e: any) {
        console.error('删除失败:', e)
        message.error(e?.message || '删除失败')
      }
    },
  })
}

// 分享结果
const shareResult = () => {
  if (!resultImageUrl.value) return

  // 复制图片链接到剪贴板
  navigator.clipboard
    .writeText(resultImageUrl.value)
    .then(() => {
      message.success('图片链接已复制到剪贴板')
    })
    .catch(() => {
      message.error('复制失败，请手动复制')
    })
}

// 打开历史记录抽屉时加载数据
const handleOpenHistoryDrawer = () => {
  showHistoryDrawer.value = true
  loadHistoryFromBackend()
}

// 格式化时间
const formatTime = (timeStr: string | undefined) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 下载历史记录图片
const downloadHistoryImage = (imageUrl: string | undefined) => {
  if (!imageUrl) return

  const link = document.createElement('a')
  link.href = imageUrl
  link.download = `ai-tryon-history-${Date.now()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  message.success('图片下载成功')
}

// 分享历史记录图片
const shareHistoryImage = (imageUrl: string | undefined) => {
  if (!imageUrl) return

  navigator.clipboard
    .writeText(imageUrl)
    .then(() => {
      message.success('图片链接已复制到剪贴板')
    })
    .catch(() => {
      message.error('复制失败，请手动复制')
    })
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700&family=Newsreader:wght@500;600;700&display=swap');

/* 全局基础样式 */
.tryon-page {
  --ink: #1f1a15;
  --muted: #7a6f66;
  --accent: #d45b2d;
  --accent-2: #2a9d8f;
  --surface: #fffdf8;
  --surface-2: #f6efe6;
  --stroke: rgba(31, 26, 21, 0.1);
  --shadow: 0 20px 50px rgba(31, 26, 21, 0.12);
  height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(900px 420px at 8% -10%, rgba(240, 181, 128, 0.35), transparent 65%),
    radial-gradient(800px 380px at 92% 5%, rgba(122, 210, 196, 0.28), transparent 60%),
    linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
  font-family: 'Manrope', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
  isolation: isolate;
}

.tryon-page::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(31, 26, 21, 0.05) 1px, transparent 1px);
  background-size: 22px 22px;
  opacity: 0.4;
  pointer-events: none;
  z-index: 0;
}

/* 主布局 - 左右分栏 */
.main-layout {
  display: flex;
  height: 100vh;
  padding: 12px 16px;
  gap: 16px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

/* 左侧操作面板 */
.left-panel {
  width: 360px;
  min-width: 360px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  border-radius: 22px;
  border: 1px solid var(--stroke);
  box-shadow: var(--shadow);
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  backdrop-filter: blur(12px);
}

/* 面板头部 */
.panel-header {
  padding: 14px 14px 10px;
  border-bottom: 1px solid rgba(31, 26, 21, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-family: 'Newsreader', 'Noto Serif SC', 'Songti SC', serif;
  font-size: 20px;
  font-weight: 700;
  color: var(--ink);
  margin: 0;
  letter-spacing: -0.4px;
}

.header-btns {
  display: flex;
  gap: 6px;
}

.header-btn {
  color: var(--muted) !important;
  font-size: 12px;
}

.header-btn:hover {
  color: var(--accent) !important;
}

/* 面板区块 */
.panel-section {
  padding: 8px 14px;
  border-bottom: 1px solid rgba(31, 26, 21, 0.06);
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 8px;
}

.section-tip {
  color: var(--muted);
  font-size: 11px;
  font-weight: 500;
}

/* 预设模特网格 */
.preset-models {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.preset-item {
  padding: 6px;
  border-radius: 10px;
  border: 1px solid rgba(31, 26, 21, 0.12);
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.preset-item img {
  width: 100%;
  height: 78px;
  object-fit: contain;
  background: #f4efe6;
  border-radius: 6px;
}

.preset-item:hover {
  border-color: rgba(212, 91, 45, 0.45);
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(212, 91, 45, 0.15);
}

.preset-item.active {
  border-color: var(--accent);
  background: rgba(212, 91, 45, 0.08);
  box-shadow: 0 10px 20px rgba(212, 91, 45, 0.18);
}

.preset-name {
  margin-top: 4px;
  font-size: 10px;
  color: var(--muted);
}

/* 上传框 */
.upload-box {
  border: 1px dashed rgba(31, 26, 21, 0.2);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.85);
  min-height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-box.small {
  min-height: 76px;
}

.upload-box:hover {
  border-color: rgba(42, 157, 143, 0.6);
  background: rgba(42, 157, 143, 0.08);
}

.upload-content {
  text-align: center;
  padding: 10px 12px;
}

.upload-icon {
  color: rgba(31, 26, 21, 0.4);
  margin-bottom: 6px;
}

.upload-icon svg {
  stroke: currentColor;
}

.upload-text {
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 4px;
}

.upload-tip {
  font-size: 11px;
  color: rgba(31, 26, 21, 0.45);
  line-height: 1.4;
}

/* 已上传预览 */
.uploaded-preview {
  width: 100%;
  padding: 8px;
  text-align: center;
}

.uploaded-preview :deep(.ant-image) {
  max-width: 100%;
  max-height: 120px;
  border-radius: 10px;
  overflow: hidden;
}

.uploaded-preview :deep(.ant-image img) {
  max-width: 100%;
  max-height: 120px;
  object-fit: contain;
}

.preview-actions {
  margin-top: 8px;
}

.preview-actions :deep(.ant-btn) {
  background: rgba(212, 91, 45, 0.08);
  border: 1px solid rgba(212, 91, 45, 0.25);
  color: var(--ink);
}

.preview-actions :deep(.ant-btn:hover) {
  background: rgba(212, 91, 45, 0.16);
  border-color: rgba(212, 91, 45, 0.4);
}

/* 底部生成按钮 */
.panel-footer {
  position: sticky;
  bottom: 0;
  z-index: 2;
  padding: 10px 14px 12px;
  margin-top: auto;
  background: rgba(255, 255, 255, 0.98);
}

.generate-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  color: #ffffff !important;
  box-shadow: 0 12px 24px rgba(212, 91, 45, 0.28);
}

.generate-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #c24f26 0%, #e8784b 100%) !important;
  box-shadow: 0 16px 28px rgba(212, 91, 45, 0.32);
}

.generate-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-text {
  font-size: 15px;
}

.error-alert {
  margin-top: 10px;
}

/* 右侧结果区域 */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 28px;
  border: 1px solid var(--stroke);
  box-shadow: var(--shadow);
  height: 100%;
  overflow: hidden;
  backdrop-filter: blur(12px);
}

/* 加载状态 */
.result-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.loading-text {
  text-align: center;
}

.loading-text h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 6px;
}

.loading-text p {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

/* 结果内容 */
.result-content,
.result-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 100%;
  max-width: 860px;
}

.result-title {
  font-family: 'Newsreader', 'Noto Serif SC', 'Songti SC', serif;
  font-size: 28px;
  font-weight: 700;
  color: var(--ink);
  margin: 0 0 12px;
  letter-spacing: -0.5px;
}

.result-subtitle {
  font-size: 14px;
  color: var(--muted);
  margin: 0 0 24px;
  line-height: 1.6;
}

/* 步骤指示 */
.flow-steps {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
  font-size: 14px;
  color: var(--muted);
}

.flow-arrow {
  color: var(--accent);
  font-weight: bold;
}

/* 生成结果大图 */
.result-main {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 12px;
}

.result-large-image {
  max-width: 100%;
  max-height: 52vh;
  border-radius: 18px;
  border: 2px solid rgba(212, 91, 45, 0.25);
  box-shadow: 0 18px 36px rgba(31, 26, 21, 0.2);
}

.result-main :deep(.ant-image) {
  display: flex;
  justify-content: center;
}

.result-main :deep(.ant-image img) {
  max-width: 100%;
  max-height: 52vh;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 18px;
}

/* 操作按钮 */
.result-actions {
  display: flex;
  gap: 14px;
  margin-top: 22px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-btn {
  height: 40px;
  border-radius: 10px;
  padding: 0 22px;
  font-weight: 600;
}

.action-btn:first-child {
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%) !important;
  border: none !important;
  color: #ffffff !important;
  box-shadow: 0 10px 20px rgba(212, 91, 45, 0.25);
}

.action-btn:not(:first-child) {
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid rgba(31, 26, 21, 0.12) !important;
  color: var(--ink) !important;
}

.action-btn:not(:first-child):hover {
  background: rgba(255, 255, 255, 0.95) !important;
  border-color: rgba(212, 91, 45, 0.25) !important;
}

/* 任务信息 */
.result-meta {
  margin-top: 16px;
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: var(--muted);
}

/* 功能特性网格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  width: 100%;
  max-width: 560px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 14px;
  border: 1px solid rgba(31, 26, 21, 0.08);
  text-align: left;
  transition: all 0.3s ease;
  box-shadow: 0 10px 18px rgba(31, 26, 21, 0.08);
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(42, 157, 143, 0.25);
  transform: translateY(-2px);
}

.feature-item .feature-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.feature-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 4px;
}

.feature-info p {
  font-size: 12px;
  color: var(--muted);
  margin: 0;
  line-height: 1.5;
}

/* 使用指南样式 */
.guide-content {
  padding: 8px 0;
}

/* 使用指南样式 */
.guide-tips {
  background: #fff8ef;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(212, 91, 45, 0.15);
}

.guide-tips h4 {
  margin-bottom: 10px;
  color: var(--ink);
  font-size: 14px;
}

.guide-tips ul {
  margin: 0;
  padding-left: 18px;
  color: var(--muted);
}

.guide-tips li {
  margin-bottom: 6px;
  line-height: 1.5;
}

/* 历史记录样式 */
.history-content {
  height: 100%;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid rgba(31, 26, 21, 0.08);
  transition: all 0.3s;
  box-shadow: 0 8px 16px rgba(31, 26, 21, 0.08);
}

.history-item:hover {
  background: #fffaf4;
  border-color: rgba(212, 91, 45, 0.25);
  box-shadow: 0 12px 22px rgba(212, 91, 45, 0.12);
}

.history-image :deep(.ant-image) {
  border-radius: 8px;
  overflow: hidden;
}

.history-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.history-time {
  font-size: 13px;
  color: #4e5969;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.history-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 响应式 */
@media (max-width: 1200px) {
  .left-panel {
    width: 330px;
    min-width: 330px;
  }
}

@media (max-width: 992px) {
  .main-layout {
    flex-direction: column;
    height: auto;
    padding: 16px;
    overflow: visible;
  }

  .left-panel {
    width: 100%;
    min-width: 100%;
    height: auto;
  }

  .right-panel {
    min-height: 60vh;
    height: auto;
    padding: 24px 18px;
  }

  .preset-models {
    grid-template-columns: repeat(7, 1fr);
  }

  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .preset-models {
    grid-template-columns: repeat(4, 1fr);
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .result-title {
    font-size: 22px;
  }

  .result-subtitle {
    font-size: 13px;
  }

  .flow-steps {
    font-size: 13px;
  }

  .result-actions {
    flex-direction: column;
    width: 100%;
    max-width: 280px;
  }

  .action-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .panel-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .preset-models {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>


