<template>
  <div class="tryon-page">
    <div class="page-header">
      <h1>👗 AI 智能试衣</h1>
      <p class="subtitle">上传人物与服装图片，AI 为你生成试穿效果</p>
      <div class="header-actions">
        <a-button type="default" @click="showGuideModal = true">
          <template #icon><question-circle-outlined /></template>
          使用指南
        </a-button>
        <a-button type="default" @click="handleOpenHistoryDrawer">
          <template #icon><history-outlined /></template>
          历史记录
        </a-button>
      </div>
    </div>

    <!-- 功能特性展示 -->
    <div class="features-section">
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :lg="6" v-for="feature in features" :key="feature.title">
          <div class="feature-card">
            <div class="feature-icon">{{ feature.icon }}</div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </a-col>
      </a-row>
    </div>

    <a-card class="tryon-card" :bordered="false">
      <a-row :gutter="[32, 32]">
        <!-- 左侧：上传与参数配置 -->
        <a-col :xs="24" :lg="10">
          <div class="config-section">
            <h2 class="section-title">
              <span class="title-icon">⚙️</span>
              试衣配置
            </h2>

            <a-form layout="vertical">
              <!-- 预设模特选择 -->
              <a-form-item label="预设模特" class="form-item-custom">
                <div class="preset-models">
                  <div
                    v-for="model in presetModels"
                    :key="model.key"
                    :class="['preset-item', { active: selectedPresetKey === model.key }]"
                    @click="handleSelectPreset(model)"
                  >
                    <a-image :src="model.assetUrl" :width="72" :preview="false" />
                    <div class="preset-name">{{ model.name }}</div>
                  </div>
                </div>
              </a-form-item>
              <a-form-item label="人物照片" required class="form-item-custom">
                <a-alert
                  message="最佳效果提示"
                  description="建议使用正面全身照，背景简洁，光线充足，人物清晰"
                  type="info"
                  show-icon
                  closable
                  class="upload-tip-alert"
                />
                <a-upload
                  :show-upload-list="false"
                  :before-upload="(file) => handleUpload('person', file)"
                  accept="image/*"
                >
                  <div class="upload-box">
                    <a-button>
                      <picture-outlined /> 选择人物照片
                    </a-button>
                    <span class="upload-tip">支持 JPG / PNG，大小 ≤ 10MB</span>
                  </div>
                </a-upload>
             
                <div v-if="personImageUrl || selectedPresetPreviewUrl" class="preview-wrapper">
                  <a-image :src="personImageUrl || selectedPresetPreviewUrl" :width="160" />
                  <a-button type="link" danger @click="clearImage('person')">移除</a-button>
                </div>
              </a-form-item>

              <a-form-item label="上衣图片" class="form-item-custom">
                <a-upload
                  :show-upload-list="false"
                  :before-upload="(file) => handleUpload('top', file)"
                  accept="image/*"
                >
                  <div class="upload-box">
                    <a-button>
                      <picture-outlined /> 选择上衣图片
                    </a-button>
                    <span class="upload-tip">可选，如不上传则只试穿下装</span>
                  </div>
                </a-upload>
                <div v-if="topGarmentUrl" class="preview-wrapper">
                  <a-image :src="topGarmentUrl" :width="120" />
                  <a-button type="link" danger @click="clearImage('top')">移除</a-button>
                </div>
              </a-form-item>

              <a-form-item label="下装图片" class="form-item-custom">
                <a-upload
                  :show-upload-list="false"
                  :before-upload="(file) => handleUpload('bottom', file)"
                  accept="image/*"
                >
                  <div class="upload-box">
                    <a-button>
                      <picture-outlined /> 选择下装图片
                    </a-button>
                    <span class="upload-tip">可选，如不上传则只试穿上衣</span>
                  </div>
                </a-upload>
                <div v-if="bottomGarmentUrl" class="preview-wrapper">
                  <a-image :src="bottomGarmentUrl" :width="120" />
                  <a-button type="link" danger @click="clearImage('bottom')">移除</a-button>
                </div>
              </a-form-item>

              <a-form-item>
                <a-button
                  type="primary"
                  block
                  :loading="submitting || polling"
                  :disabled="(!personImageUrl && !selectedPresetKey) || (!topGarmentUrl && !bottomGarmentUrl)"
                  @click="handleSubmit"
                >
                  <template #icon>
                    <play-circle-outlined />
                  </template>
                  {{ submitting || polling ? '正在生成试衣效果...' : '开始 AI 试衣' }}
                </a-button>
              </a-form-item>

              <a-alert
                v-if="errorMessage"
                type="error"
                :message="errorMessage"
                show-icon
                class="mt-2"
              />

              <a-alert
                v-if="currentTaskId && !errorMessage"
                type="info"
                :message="`任务 ID：${currentTaskId}`"
                show-icon
                class="mt-2"
              />
            </a-form>
          </div>
        </a-col>

        <!-- 右侧：试衣结果展示 -->
        <a-col :xs="24" :lg="14">
          <div class="preview-section">
            <h2 class="section-title">
              <span class="title-icon">✨</span>
              试衣效果
            </h2>

            <a-spin :spinning="polling">
              <div v-if="resultImageUrl" class="result-wrapper">
                <a-image :src="resultImageUrl" :width="360" />
                <div class="result-actions">
                  <a-space>
                    <a-button type="primary" @click="downloadResult">
                      <template #icon><download-outlined /></template>
                      下载图片
                    </a-button>
                    <a-button @click="saveToHistory">
                      <template #icon><save-outlined /></template>
                      保存到历史
                    </a-button>
                    <a-button @click="shareResult">
                      <template #icon><share-alt-outlined /></template>
                      分享
                    </a-button>
                  </a-space>
                </div>
                <div class="result-meta" v-if="taskDetail">
                  <p>任务状态：{{ taskDetail.taskStatus }}</p>
                  <p v-if="taskDetail.createTime">创建时间：{{ taskDetail.createTime }}</p>
                  <p v-if="taskDetail.endTime">完成时间：{{ taskDetail.endTime }}</p>
                </div>
              </div>

              <div v-else class="empty-result">
                <a-empty description="上传图片并点击开始，即可查看试衣效果">
                  <template #image>
                    <img
                      style="width: 120px"
                      src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIgcng9IjEwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iNDAiIGZpbGw9IiNkOWQ5ZDkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7wn46oPC90ZXh0Pjwvc3ZnPg=="
                    />
                  </template>
                </a-empty>
              </div>
            </a-spin>
          </div>
        </a-col>
     </a-row>
    </a-card>

    <!-- 使用指南弹窗 -->
    <a-modal
      v-model:open="showGuideModal"
      title="📖 AI 试衣使用指南"
      :footer="null"
      width="600px"
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
    >
      <a-spin :spinning="historyLoading">
        <div class="history-content">
          <a-empty v-if="historyList.length === 0" description="暂无历史记录">
            <template #image>
              <history-outlined style="font-size: 64px; color: #d9d9d9" />
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
                :width="120"
                :preview="true"
                class="history-image"
              />
              <div class="history-info">
                <p class="history-time">
                  <clock-circle-outlined /> {{ formatTime(item.submitTime) }}
                </p>
                <p class="history-time" v-if="item.endTime">
                  完成: {{ formatTime(item.endTime) }}
                </p>
                <div class="history-actions">
                  <a-space>
                    <a-button
                      type="primary"
                      size="small"
                      @click="loadHistoryItem(item)"
                    >
                      查看
                    </a-button>
                    <a-button
                      size="small"
                      @click="downloadHistoryImage(item.localImageUrl)"
                    >
                      <download-outlined /> 下载
                    </a-button>
                    <a-button
                      size="small"
                      @click="shareHistoryImage(item.localImageUrl)"
                    >
                      <share-alt-outlined /> 分享
                    </a-button>
                    <a-button
                      type="text"
                      danger
                      size="small"
                      @click.stop="deleteHistoryItem(item, index)"
                    >
                      删除
                    </a-button>
                  </a-space>
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
  PictureOutlined,
  PlayCircleOutlined,
  QuestionCircleOutlined,
  HistoryOutlined,
  DownloadOutlined,
  SaveOutlined,
  ShareAltOutlined,
  ThunderboltOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { upload, submit, getStatus, getTryOnHistory, deleteTryOnRecord } from '@/api/aiTryOnController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()

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
    icon: '🎨',
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
    icon: '💾',
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

const submitting = ref(false)
const polling = ref(false)
const currentTaskId = ref<string>('')
const resultImageUrl = ref<string>('')
const taskDetail = ref<API.TryOnTask | null>(null)
const errorMessage = ref<string>('')

let pollTimer: number | null = null

const clearPollTimer = () => {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
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

const startPolling = (taskId: string) => {
  clearPollTimer()
  polling.value = true

  pollTimer = window.setInterval(async () => {
    try {
      const res = await getStatus({ taskId })
      const data = res?.data
      taskDetail.value = data

      if (!data) return

      const status = data.taskStatus
      const isSuccess = status === 'SUCCESS' || status === 'SUCCEEDED'

      if (isSuccess && (data.localResultUrl || data.resultImageUrl)) {
        resultImageUrl.value = data.localResultUrl || data.resultImageUrl || ''
        message.success('试衣任务已完成')
        polling.value = false
        clearPollTimer()
      } else if (status === 'FAILED') {
        errorMessage.value = data.errorMessage || '试衣任务失败，请稍后重试'
        polling.value = false
        clearPollTimer()
      }
    } catch (e: any) {
      console.error('查询任务状态失败:', e)
      polling.value = false
      clearPollTimer()
      errorMessage.value = e?.message || '查询任务状态失败'
    }
  }, 3000)
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

  submitting.value = true
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
    message.success('任务提交成功，正在生成试衣效果...')
    startPolling(taskId)
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

// 保存到历史记录
const saveToHistory = () => {
  if (!resultImageUrl.value) return
  message.success('已自动保存，可在历史记录中查看')
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
/* 全局样式重置与基础设置 */
.tryon-page {
  padding: 32px 24px;
  min-height: 100vh;
  background-image: url('@/assets/backgroundImage/aiTryOn-bg.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题样式 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  color: #fff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.header-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

.header-actions .ant-btn {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  backdrop-filter: blur(5px);
  transition: all 0.3s;
}

.header-actions .ant-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.page-header h1 {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #fff 0%, #e6f7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 18px;
  opacity: 0.95;
  font-weight: 400;
  letter-spacing: 0.5px;
}

/* 功能特性展示区 */
.features-section {
  max-width: 1400px;
  margin: 0 auto 40px;
}

.feature-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.95) 100%);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #fff 0%, #f0f5ff 100%);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 14px;
  color: #4e5969;
  line-height: 1.6;
  margin: 0;
}

/* 卡片容器样式 */
.tryon-card {
  max-width: 1400px;
  margin: 0 auto;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tryon-card:hover {
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.2);
}

.tryon-card :deep(.ant-card-body) {
  padding: 40px;
}

/* 区块标题样式 */
.section-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1d2129;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e6eb;
}

.title-icon {
  font-size: 24px;
}

/* 表单项样式 */
.form-item-custom :deep(.ant-form-item-label > label) {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

/* 上传框样式 */
.upload-box {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.upload-box .ant-btn {
  height: 40px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;
  transition: all 0.3s;
}

.upload-box .ant-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.upload-tip {
  font-size: 13px;
  color: #86909c;
}

/* 预览包裹样式 */
.preview-wrapper {
  margin-top: 16px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f7f8fa;
  border-radius: 10px;
  border: 1px solid #e5e6eb;
}

.preview-wrapper :deep(.ant-image) {
  border-radius: 8px;
  overflow: hidden;
}

/* 上传提示样式 */
.upload-tip-alert {
  margin-bottom: 12px;
}

.quick-example {
  margin-top: 8px;
}

/* 预设模特网格 */
.preset-models {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 12px;
  justify-items: center;
}

.preset-item {
  padding: 10px;
  border-radius: 12px;
  border: 2px solid #e5e6eb;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
}

.preset-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  transform: translateY(-4px);
}

.preset-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.preset-item :deep(.ant-image) {
  border-radius: 8px;
  overflow: hidden;
}

.preset-name {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #4e5969;
}

/* 提交按钮样式 */
:deep(.ant-btn-primary) {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover:not(:disabled)) {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

:deep(.ant-btn-primary:active) {
  transform: translateY(0);
}

/* 预览区域样式 */
.preview-section {
  min-height: 400px;
  background: #f7f8fa;
  border-radius: 16px;
  padding: 24px;
}

.result-wrapper {
  text-align: center;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.result-wrapper :deep(.ant-image) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
  display: flex;
  justify-content: center;
}

.result-meta {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
  font-size: 14px;
  color: #4e5969;
  text-align: left;
}

.result-meta p {
  margin-bottom: 8px;
  line-height: 1.6;
}

.empty-result {
  padding: 100px 20px;
  text-align: center;
}

.mt-2 {
  margin-top: 16px;
}

/* 警告提示框样式优化 */
:deep(.ant-alert) {
  border-radius: 10px;
  border: none;
}

/* 使用指南样式 */
.guide-content {
  padding: 8px 0;
}

.guide-tips {
  background: #f7f8fa;
  padding: 16px;
  border-radius: 8px;
}

.guide-tips h4 {
  margin-bottom: 12px;
  color: #1d2129;
  font-size: 15px;
}

.guide-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #4e5969;
}

.guide-tips li {
  margin-bottom: 8px;
  line-height: 1.6;
}

/* 历史记录样式 */
.history-content {
  height: 100%;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.history-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 12px;
  border: 1px solid #e5e6eb;
  transition: all 0.3s;
}

.history-item:hover {
  background: #fff;
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.1);
}

.history-image {
  flex-shrink: 0;
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
  margin-top: 12px;
}

.history-actions :deep(.ant-space) {
  flex-wrap: wrap;
}

/* 响应式设计 - 桌面端 */
@media (min-width: 1200px) {
  .preset-models {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
    max-width: 100%;
  }
}

@media (min-width: 992px) and (max-width: 1199px) {
  .preset-models {
    grid-template-columns: repeat(5, 1fr);
  }
}

/* 响应式设计 - 平板端 */
@media (max-width: 1024px) {
  .tryon-page {
    padding: 24px 16px;
  }

  .page-header h1 {
    font-size: 40px;
  }

  .subtitle {
    font-size: 16px;
  }

  .tryon-card :deep(.ant-card-body) {
    padding: 32px 24px;
  }

  .section-title {
    font-size: 20px;
  }

  .features-section {
    margin-bottom: 32px;
  }

  .feature-card {
    padding: 20px;
  }

  .feature-icon {
    font-size: 40px;
  }

  .feature-title {
    font-size: 16px;
  }

  .preset-item {
    padding: 8px;
  }

  .preview-section {
    margin-top: 32px;
  }
}

/* 响应式设计 - 移动端 */
@media (max-width: 768px) {
  .tryon-page {
    padding: 16px 12px;
  }

  .page-header {
    margin-bottom: 24px;
  }

  .page-header h1 {
    font-size: 32px;
    margin-bottom: 8px;
  }

  .subtitle {
    font-size: 14px;
  }

  .tryon-card {
    border-radius: 16px;
  }

  .tryon-card :deep(.ant-card-body) {
    padding: 20px 16px;
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 16px;
  }

  .title-icon {
    font-size: 20px;
  }

  .upload-box {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .upload-box .ant-btn {
    width: 100%;
    height: 44px;
  }

  .preset-models {
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
  }

  .preset-item {
    padding: 6px;
  }

  .preset-item {
    width: 100%;
  }

  .preset-item :deep(.ant-image) {
    width: 100% !important;
    height: auto;
  }

  .preset-item :deep(.ant-image img) {
    width: 100%;
    height: auto;
    object-fit: cover;
  }

  .preset-name {
    font-size: 12px;
    margin-top: 6px;
  }

  .preview-wrapper {
    flex-direction: column;
    align-items: flex-start;
  }

  .preview-wrapper :deep(.ant-image) {
    width: 100% !important;
  }

  :deep(.ant-btn-primary) {
    height: 44px;
    font-size: 15px;
  }

  .preview-section {
    margin-top: 24px;
    min-height: 300px;
    padding: 16px;
  }

  .result-wrapper {
    padding: 16px;
  }

  .result-wrapper :deep(.ant-image) {
    width: 100% !important;
  }

  .empty-result {
    padding: 60px 16px;
  }

  .result-meta {
    font-size: 13px;
  }

  .result-actions :deep(.ant-space) {
    flex-wrap: wrap;
    justify-content: center;
  }

  .result-actions :deep(.ant-btn) {
    font-size: 13px;
    padding: 0 12px;
  }

  .header-actions {
    flex-direction: column;
    align-items: center;
  }

  .header-actions .ant-btn {
    width: 160px;
  }
}

/* 响应式设计 - 小屏幕移动端 */
@media (max-width: 480px) {
  .page-header h1 {
    font-size: 28px;
  }

  .preset-models {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .section-title {
    font-size: 16px;
  }

  .feature-card {
    padding: 16px;
  }

  .feature-icon {
    font-size: 32px;
  }

  .feature-title {
    font-size: 15px;
  }

  .feature-desc {
    font-size: 13px;
  }

  .result-actions :deep(.ant-btn) {
    width: 100%;
  }

  .result-actions :deep(.ant-space) {
    width: 100%;
  }
}
</style>
