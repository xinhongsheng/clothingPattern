<template>
  <div class="tryon-page">
    <!-- <div class="page-header">
      <h1>👗 AI 智能试衣</h1>
      <p class="subtitle">上传人物与服装图片，AI 为你生成试穿效果</p>
    </div> -->

    <a-card class="tryon-card" :bordered="false">
      <a-row :gutter="[32, 32]">
        <!-- 左侧：上传与参数配置 -->
        <a-col :xs="24" :lg="10">
          <div class="config-section">
            <h2 class="section-title">试衣配置</h2>

            <a-form layout="vertical">
              <!-- 预设模特选择 -->
              <a-form-item label="预设模特">
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
              <a-form-item label="人物照片" required>
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

              <a-form-item label="上衣图片">
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

              <a-form-item label="下装图片">
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
            <h2 class="section-title">试衣效果</h2>

            <a-spin :spinning="polling">
              <div v-if="resultImageUrl" class="result-wrapper">
                <a-image :src="resultImageUrl" :width="360" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, computed } from 'vue'
import { message } from 'ant-design-vue'
import { PictureOutlined, PlayCircleOutlined } from '@ant-design/icons-vue'
import { upload, submit, getStatus } from '@/api/aiTryOnController'

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
</script>

<style scoped>
.tryon-page {
  min-height: 100vh;
  background-image: url('@/assets/backgroundImage/aiTryOn-bg.png');
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
  color: #fff;
}

.page-header h1 {
  font-size: 40px;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
}

.tryon-card {
  max-width: 1400px;
  margin: 0 auto;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.7);
}

.tryon-card :deep(.ant-card-body) {
  padding: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
}

.upload-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
}

.preview-wrapper {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-section {
  min-height: 360px;
}

.preset-models {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.preset-item {
  width: 90px;
  padding: 6px;
  border-radius: 8px;
  border: 1px solid #d9d9d9;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.preset-item:hover {
  border-color: #667eea;
  box-shadow: 0 0 6px rgba(102, 126, 234, 0.5);
}

.preset-item.active {
  border-color: #667eea;
  background: #f0f5ff;
}

.preset-name {
  margin-top: 4px;
  font-size: 12px;
  color: #555;
}

.result-wrapper {
  text-align: center;
}

.result-meta {
  margin-top: 12px;
  font-size: 13px;
  color: #666;
}

.empty-result {
  padding: 80px 0;
}

.mt-2 {
  margin-top: 12px;
}
</style>
