<template>
  <div class="fusion-page">

    <a-card class="fusion-card" :bordered="false">
      <a-row :gutter="[32, 32]">
        <!-- 左侧：上传区域 -->
        <a-col :xs="24" :lg="10">
          <div class="config-section">
            <h2 class="section-title"><UploadOutlined class="title-icon" /> 素材上传</h2>

            <!-- 服装图片 -->
            <a-form layout="vertical" class="fusion-form">
              <a-form-item label="服装图片" required>
                <div class="upload-container">
                  <a-upload
                    :show-upload-list="false"
                    :before-upload="(file) => handleUploadGarment(file)"
                    accept="image/*"
                  >
                    <!-- 修复：has-image 加引号 -->
                    <div class="upload-area" :class="{ 'has-image': garmentImageUrl }">
                      <a-button type="default" size="large" class="upload-btn">
                        <PictureOutlined class="btn-icon" /> 选择服装图片
                      </a-button>
                      <span class="upload-tip">支持 JPG / PNG，大小 ≤ 5MB，仅 1 张</span>
                    </div>
                  </a-upload>

                  <div v-if="garmentImageUrl" class="preview-wrapper">
                    <div class="preview-card">
                      <a-image
                        :src="garmentImageUrl"
                        class="preview-image"
                        :preview="{ src: garmentImageUrl }"
                      />
                      <a-button type="text" danger class="remove-btn" @click="clearGarment">
                        <CloseOutlined />
                      </a-button>
                    </div>
                  </div>
                </div>
              </a-form-item>

              <!-- 图案图片 -->
              <a-form-item label="图案图片" required>
                <div class="upload-container">
                  <a-upload
                    :show-upload-list="false"
                    :before-upload="(file) => handleUploadPattern(file)"
                    accept="image/*"
                  >
                    <!-- 修复：has-image 加引号 -->
                    <div class="upload-area" :class="{ 'has-image': patternImageUrls.length > 0 }">
                      <a-button type="default" size="large" class="upload-btn">
                        <PictureOutlined class="btn-icon" /> 添加图案图片
                      </a-button>
                      <span class="upload-tip"
                        >支持 1-2 张图案，总数量（服装+图案）不超过 3 张</span
                      >
                    </div>
                  </a-upload>

                  <div v-if="patternImageUrls.length" class="pattern-list">
                    <div v-for="(url, index) in patternImageUrls" :key="index" class="pattern-item">
                      <div class="preview-card">
                        <a-image :src="url" class="pattern-preview-image" :preview="{ src: url }" />
                        <a-button
                          type="text"
                          danger
                          class="remove-btn"
                          @click="removePattern(index)"
                        >
                          <CloseOutlined />
                        </a-button>
                      </div>
                    </div>
                  </div>
                </div>
              </a-form-item>

              <a-form-item>
                <a-button
                  type="primary"
                  block
                  size="large"
                  :loading="submitting || polling"
                  :disabled="!canSubmit"
                  @click="handleSubmit"
                  class="fusion-btn"
                >
                  <template #icon>
                    <PlayCircleOutlined />
                  </template>
                  {{ submitting || polling ? '正在融合中...' : '开始图案融合' }}
                </a-button>
              </a-form-item>

              <a-alert
                v-if="errorMessage"
                type="error"
                :message="errorMessage"
                show-icon
                class="error-alert"
                closable
                @close="errorMessage = ''"
              />
            </a-form>
          </div>
        </a-col>

        <!-- 右侧：结果展示 -->
        <a-col :xs="24" :lg="14">
          <div class="preview-section">
            <h2 class="section-title"><EyeOutlined class="title-icon" /> 融合效果</h2>

            <a-spin :spinning="polling" size="large" class="result-spin">
              <div v-if="resultUrls.length" class="result-grid">
                <div v-for="(url, index) in resultUrls" :key="index" class="result-item">
                  <a-image :src="url" class="result-image" :preview="{ src: url }" />
                  <div class="result-mask">
                    <span class="result-index">效果 {{ index + 1 }}</span>
                  </div>
                </div>
              </div>

              <div v-else class="empty-result">
                <a-empty description="上传服装与图案后，点击开始图案融合即可生成效果图">
                  <template #image>
                    <img
                      style="width: 160px"
                      src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYwIiBoZWlnaHQ9IjE2MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTYwIiBoZWlnaHQ9IjE2MCIgZmlsbD0iI2Y1ZjVmNSIgcng9IjEwIi8+PHBhdGggZD0iTTExMCA2MEMxMTAgMzguOSA5MS4xIDIwIDcwIDIwQzQ4LjkgMjAgMzAgMzguOSAzMCA2MEMzMCA4MS4xIDQ4LjkgMTAwIDcwIDEwMEM5MS4xIDEwMCAxMTAgODEuMSAxMTAgNjBaTTEwMCA3MkM5Ni43IDcyIDk0IDY5LjMgOTQgNjZDMjk0IDYzIDk2LjcgNjAgOTkgNjBDMTAxLjMgNjAgMTA0IDYzIDEwNCA2NkMxMDQgNjkuMyAxMDEuMyA3MiAxMDAgNzJaIiBmaWxsPSIjZDlkOWQ5Ii8+PHRleHQgeD0iODAlIiB5PSI4MCUiIGZvbnQtc2l6ZT0iMzAiIGZpbGw9IiM3MTgwOTYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7wn46oPC90ZXh0Pjwvc3ZnPg=="
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
import {
  PictureOutlined,
  PlayCircleOutlined,
  UploadOutlined,
  EyeOutlined,
  CloseOutlined,
} from '@ant-design/icons-vue'
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
/* 全局基础样式 */
.fusion-page {
  min-height: 100vh;
  background-image:  url('@/assets/backgroundImage/mj-bg.png'); ;
  padding: 40px 20px;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  color: #ffffff;
}

.page-header h1 {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 12px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 18px;
  margin: 0;
  opacity: 0.85;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 步骤引导 */
.guide-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.guide-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4a5568;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.step-text {
  margin-top: 8px;
  font-size: 14px;
  color: #a0aec0;
  transition: all 0.3s;
  white-space: nowrap;
}

.guide-divider {
  flex: 1;
  height: 3px;
  background-color: #4a5568;
  margin: 0 16px;
  transition: all 0.3s;
}

/* 步骤状态样式 */
.guide-step.active .step-number {
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%);
  box-shadow: 0 0 20px rgba(66, 153, 225, 0.5);
  transform: scale(1.1);
}

.guide-step.active .step-text {
  color: #ffffff;
  font-weight: 500;
}

.guide-step.has-image .step-number {
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%);
  box-shadow: 0 0 20px rgba(66, 153, 225, 0.5);
  transform: scale(1.1);
}

.guide-step.has-image .step-text {
  color: #ffffff;
  font-weight: 500;
}

/* 主卡片样式 */
.fusion-card {
  max-width: 1400px;
  margin: 0 auto;
  border-radius: 20px;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.3);
  background-color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.fusion-card :deep(.ant-card-body) {
  padding: 40px;
}

/* 区域标题 */
.section-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.title-icon {
  color: #4299e1;
  font-size: 24px;
}

/* 表单样式 */
.fusion-form {
  background-color: #f8f9fa;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #f0f2f5;
}

:deep(.ant-form-item) {
  margin-bottom: 28px;
}

:deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.ant-form-item-label > label) {
  font-size: 16px;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

:deep(.ant-form-item-label > label::after) {
  margin-left: 6px;
  color: #e53e3e;
}

/* 上传区域样式 */
.upload-container {
  width: 100%;
}

.upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border: 2px dashed #dee2e6;
  border-radius: 12px;
  background-color: #ffffff;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #4299e1;
  background-color: #f0f8fb;
}

.upload-area.has-image {
  margin-bottom: 16px;
}

.upload-btn {
  border-radius: 8px !important;
  font-size: 15px !important;
  padding: 10px 20px !important;
  border-color: #4299e1 !important;
  color: #4299e1 !important;
  transition: all 0.3s !important;
}

:deep(.upload-btn:hover) {
  background-color: #f0f8fb !important;
  border-color: #3182ce !important;
  color: #3182ce !important;
}

.btn-icon {
  margin-right: 8px;
}

.upload-tip {
  font-size: 13px;
  color: #718096;
  flex: 1;
  line-height: 1.5;
}

/* 预览图片样式 */
.preview-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.preview-card {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.preview-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.preview-image {
  width: 180px !important;
  height: 180px !important;
  object-fit: cover;
  border-radius: 10px;
}

.pattern-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.pattern-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.pattern-preview-image {
  width: 140px !important;
  height: 140px !important;
  object-fit: cover;
  border-radius: 10px;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: rgba(229, 62, 62, 0.8);
  color: #ffffff !important;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s;
}

.preview-card:hover .remove-btn {
  opacity: 1;
}

:deep(.remove-btn:hover) {
  background-color: #e53e3e;
  color: #ffffff !important;
}

/* 融合按钮样式 */
.fusion-btn {
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
  height: 56px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.3);
}

:deep(.fusion-btn:hover) {
  background: linear-gradient(135deg, #3182ce 0%, #319795 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(66, 153, 225, 0.4);
}

:deep(.fusion-btn:disabled) {
  background: linear-gradient(135deg, #a7c0ff 0%, #a7f3d0 100%) !important;
  transform: none;
  box-shadow: none;
}

/* 错误提示 */
.error-alert {
  margin-top: 16px;
  border-radius: 8px;
  background-color: #fef2f2;
  border-color: #fecaca;
}

:deep(.error-alert .ant-alert-message) {
  color: #dc2626;
  font-size: 14px;
}

/* 结果展示区域 */
.preview-section {
  min-height: 400px;
  background-color: #ffffff;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #f0f2f5;
}

.result-spin {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

:deep(.result-spin .ant-spin-dot) {
  font-size: 48px;
}

:deep(.result-spin .ant-spin-text) {
  font-size: 16px;
  color: #4a5568;
  margin-top: 16px;
}

.result-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  justify-content: flex-start;
}

.result-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.result-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
}

.result-image {
  width: 240px !important;
  height: 240px !important;
  object-fit: cover;
  border-radius: 12px;
  transition: all 0.5s;
}

.result-item:hover .result-image {
  transform: scale(1.05);
}

.result-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, transparent 100%);
  opacity: 0;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-item:hover .result-mask {
  opacity: 1;
}

.result-index {
  color: #ffffff;
  font-size: 14px;
  font-weight: 500;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 4px 12px;
  border-radius: 16px;
  backdrop-filter: blur(4px);
}

/* 空状态 */
.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  min-height: 320px;
}

:deep(.empty-result .ant-empty-description) {
  font-size: 15px;
  color: #718096;
  margin-top: 16px;
  line-height: 1.6;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .result-image {
    width: 200px !important;
    height: 200px !important;
  }
}

@media (max-width: 992px) {
  .fusion-card :deep(.ant-card-body) {
    padding: 30px;
  }

  .preview-image {
    width: 160px !important;
    height: 160px !important;
  }

  .pattern-preview-image {
    width: 120px !important;
    height: 120px !important;
  }

  .result-image {
    width: 180px !important;
    height: 180px !important;
  }
}

@media (max-width: 768px) {
  .fusion-page {
    padding: 24px 16px;
  }

  .page-header h1 {
    font-size: 32px;
  }

  .subtitle {
    font-size: 16px;
  }

  .guide-steps {
    margin-bottom: 24px;
  }

  .step-number {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .step-text {
    font-size: 12px;
  }

  .guide-divider {
    margin: 0 8px;
  }

  .fusion-card :deep(.ant-card-body) {
    padding: 20px;
  }

  .section-title {
    font-size: 20px;
    margin-bottom: 20px;
  }

  .fusion-form {
    padding: 18px;
  }

  :deep(.ant-form-item) {
    margin-bottom: 24px;
  }

  .upload-area {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }

  .upload-tip {
    margin-top: 8px;
  }

  .preview-section {
    padding: 18px;
    min-height: 320px;
  }

  .result-grid {
    gap: 16px;
    justify-content: center;
  }

  .result-image {
    width: 140px !important;
    height: 140px !important;
  }
}

@media (max-width: 480px) {
  .fusion-page {
    padding: 16px 8px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .subtitle {
    font-size: 14px;
  }

  .step-text {
    display: none;
  }

  .fusion-card :deep(.ant-card-body) {
    padding: 16px;
  }

  .upload-btn {
    width: 100% !important;
  }

  .upload-area {
    align-items: center;
  }

  .preview-image {
    width: 100% !important;
    height: auto !important;
  }

  .pattern-preview-image {
    width: 100% !important;
    height: auto !important;
  }

  .result-image {
    width: 100% !important;
    height: auto !important;
  }
}

/* 加载状态优化 */
:deep(.ant-spin-dot-item) {
  background-color: #4299e1 !important;
}

:deep(.ant-btn-loading .anticon-loading) {
  color: rgba(255, 255, 255, 0.8) !important;
}
</style>
