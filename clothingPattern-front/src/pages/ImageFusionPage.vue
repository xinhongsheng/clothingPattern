<template>
  <div class="fusion-page">
    <div class="page-header">
      <h1>🧵 服装图案智能融合</h1>
      <p class="subtitle">上传服装与图案，由 AI 帮你自动贴图生成效果图</p>
    </div>

    <a-card class="fusion-card" :bordered="false">
      <a-row :gutter="[32, 32]">
        <!-- 左侧：上传区域 -->
        <a-col :xs="24" :lg="10">
          <div class="config-section">
            <h2 class="section-title">素材上传</h2>

            <!-- 服装图片 -->
            <a-form layout="vertical">
              <a-form-item label="服装图片" required>
                <a-upload
                  :show-upload-list="false"
                  :before-upload="(file) => handleUploadGarment(file)"
                  accept="image/*"
                >
                  <div class="upload-box">
                    <a-button>
                      <picture-outlined /> 选择服装图片
                    </a-button>
                    <span class="upload-tip">支持 JPG / PNG，大小 ≤ 5MB，仅 1 张</span>
                  </div>
                </a-upload>
                <div v-if="garmentImageUrl" class="preview-wrapper">
                  <a-image :src="garmentImageUrl" :width="160" />
                  <a-button type="link" danger @click="clearGarment">移除</a-button>
                </div>
              </a-form-item>

              <!-- 图案图片 -->
              <a-form-item label="图案图片" required>
                <a-upload
                  :show-upload-list="false"
                  :before-upload="(file) => handleUploadPattern(file)"
                  accept="image/*"
                >
                  <div class="upload-box">
                    <a-button>
                      <picture-outlined /> 添加图案图片
                    </a-button>
                    <span class="upload-tip">支持 1-2 张图案，总数量（服装+图案）不超过 3 张</span>
                  </div>
                </a-upload>
                <div v-if="patternImageUrls.length" class="pattern-list">
                  <div
                    v-for="(url, index) in patternImageUrls"
                    :key="index"
                    class="pattern-item"
                  >
                    <a-image :src="url" :width="100" />
                    <a-button type="link" danger @click="removePattern(index)">移除</a-button>
                  </div>
                </div>
              </a-form-item>

              <a-form-item>
                <a-button
                  type="primary"
                  block
                  :loading="submitting || polling"
                  :disabled="!canSubmit"
                  @click="handleSubmit"
                >
                  <template #icon>
                    <play-circle-outlined />
                  </template>
                  {{ submitting || polling ? '正在融合中...' : '开始图案融合' }}
                </a-button>
              </a-form-item>

              <a-alert
                v-if="errorMessage"
                type="error"
                :message="errorMessage"
                show-icon
                class="mt-2"
              />
            </a-form>
          </div>
        </a-col>

        <!-- 右侧：结果展示 -->
        <a-col :xs="24" :lg="14">
          <div class="preview-section">
            <h2 class="section-title">融合效果</h2>

            <a-spin :spinning="polling">
              <div v-if="resultUrls.length" class="result-grid">
                <a-image
                  v-for="(url, index) in resultUrls"
                  :key="index"
                  :src="url"
                  :width="220"
                  class="result-image"
                />
              </div>

              <div v-else class="empty-result">
                <a-empty description="上传服装与图案后，点击开始图案融合即可生成效果图">
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
import { computed, onBeforeUnmount, ref } from 'vue'
import { message } from 'ant-design-vue'
import { PictureOutlined, PlayCircleOutlined } from '@ant-design/icons-vue'
import { uploadImage, submitTask, queryStatus, getResults } from '@/api/imageFusionController'

const garmentImageUrl = ref<string>('')
const patternImageUrls = ref<string[]>([])

const submitting = ref(false)
const polling = ref(false)
const currentTaskId = ref<string | null>(null)
const resultUrls = ref<string[]>([])
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

const canSubmit = computed(() => {
  // 至少需要 1 张服装图 + 1 张图案图，且总张数 <= 3
  return (
    !!garmentImageUrl.value &&
    patternImageUrls.value.length >= 1 &&
    1 + patternImageUrls.value.length <= 3 &&
    !submitting.value &&
    !polling.value
  )
})

const doUpload = async (file: File): Promise<string | null> => {
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return null
  }
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await uploadImage(formData)
    const data: any = res?.data
    const url = data?.data || data?.url || null
    if (!url) {
      throw new Error('上传失败，未获取到图片地址')
    }
    return url
  } catch (e: any) {
    console.error('上传失败:', e)
    message.error(e?.message || '上传失败，请稍后重试')
    return null
  }
}

const handleUploadGarment = async (file: File) => {
  const url = await doUpload(file)
  if (url) {
    garmentImageUrl.value = url
  }
  return false
}

const handleUploadPattern = async (file: File) => {
  if (patternImageUrls.value.length >= 2) {
    message.warning('最多只能上传 2 张图案图片')
    return false
  }
  const url = await doUpload(file)
  if (url) {
    patternImageUrls.value.push(url)
  }
  return false
}

const clearGarment = () => {
  garmentImageUrl.value = ''
}

const removePattern = (index: number) => {
  patternImageUrls.value.splice(index, 1)
}

const startPolling = (taskId: string) => {
  clearPollTimer()
  polling.value = true

  pollTimer = window.setInterval(async () => {
    try {
      const res = await queryStatus({ taskId })
      const data: any = res?.data?.data || res?.data
      if (!data) return

      const status = data.taskStatus
      if (status === 'SUCCEEDED') {
        // 成功后再获取结果图片列表
        const resultRes = await getResults({ taskId })
        const resultData: any = resultRes?.data?.data || resultRes?.data
        resultUrls.value = Array.isArray(resultData) ? resultData : []
        polling.value = false
        clearPollTimer()
        message.success('图案融合完成')
      } else if (status === 'FAILED') {
        errorMessage.value = data.errorMessage || '图案融合失败，请稍后重试'
        polling.value = false
        clearPollTimer()
      }
    } catch (e: any) {
      console.error('查询任务状态失败:', e)
      errorMessage.value = e?.message || '查询任务状态失败'
      polling.value = false
      clearPollTimer()
    }
  }, 3000)
}

const handleSubmit = async () => {
  if (!canSubmit.value) {
    return
  }

  submitting.value = true
  errorMessage.value = ''
  resultUrls.value = []

  try {
    const allUrls = [garmentImageUrl.value, ...patternImageUrls.value]
    const imageUrls = allUrls.join(',')

    // TODO：根据项目实际登录逻辑替换 userId
    const userId = 0

    const res = await submitTask({ userId, imageUrls } as any)
    const taskId = (res as any)?.data?.data || (res as any)?.data
    if (!taskId) {
      throw new Error('未获取到任务 ID')
    }

    currentTaskId.value = String(taskId)
    message.success('任务提交成功，正在进行图案融合...')
    startPolling(String(taskId))
  } catch (e: any) {
    console.error('提交融合任务失败:', e)
    errorMessage.value = e?.message || '提交融合任务失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.fusion-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.fusion-card {
  max-width: 1400px;
  margin: 0 auto;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.fusion-card :deep(.ant-card-body) {
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

.pattern-list {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.pattern-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.preview-section {
  min-height: 360px;
}

.result-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.result-image {
  border-radius: 8px;
}

.empty-result {
  padding: 80px 0;
}

.mt-2 {
  margin-top: 12px;
}
</style>
