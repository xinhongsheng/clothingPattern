<template>
  <div class="fusion-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🎨 AI 图案融合</h1>
      <p class="subtitle">将您的服装图片与精美图案完美融合，创造独特的设计效果</p>
    </div>

    <!-- 功能特性卡片 -->
    <div class="features-section">
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" v-for="(feature, index) in features" :key="index">
          <div class="feature-card">
            <div class="feature-icon" v-html="feature.icon"></div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 使用步骤指引 -->
    <div class="steps-guide">
      <h2 class="guide-title">🚀 快速开始</h2>
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :md="8" :lg="8" v-for="(step, index) in steps" :key="index">
          <div class="step-card">
            <div class="step-number-badge">{{ index + 1 }}</div>
            <div class="step-icon" v-html="step.icon"></div>
            <h3 class="step-title">{{ step.title }}</h3>
            <p class="step-desc">{{ step.desc }}</p>
          </div>
        </a-col>
      </a-row>
    </div>

    <a-card class="fusion-card" :bordered="false">
      <a-row :gutter="[24, 24]">
        <!-- 左侧：上传区域 -->
        <a-col :xs="24" :sm="24" :md="24" :lg="10" :xl="10">
          <div class="config-section">
            <h2 class="section-title"><UploadOutlined class="title-icon" /> 服装图片上传</h2>

            <!-- 服装图片 -->
            <a-form layout="vertical" class="fusion-form">
              <a-form-item label="服装图片" required>
                <div class="upload-container">
                  <!-- 没有图片时显示上传按钮 -->
                  <a-upload
                    v-if="!garmentImageUrl"
                    :show-upload-list="false"
                    :before-upload="(file) => handleUploadGarment(file)"
                    accept="image/*"
                  >
                    <div class="upload-area">
                      <div class="upload-overlay">
                        <a-button type="default" size="large" class="upload-btn">
                          <PictureOutlined class="btn-icon" /> 选择服装图片
                        </a-button>
                        <span class="upload-tip">支持 JPG / PNG，尺寸 384-5000 像素，大小 ≤ 5MB</span>
                      </div>
                    </div>
                  </a-upload>

                  <!-- 有图片时显示图片预览 -->
                  <div v-else class="image-preview-wrapper">
                    <div class="image-preview-container">
                      <a-image
                        :src="garmentImageUrl"
                        class="uploaded-image"
                        :preview="{ src: garmentImageUrl }"
                      />
                      <div class="image-actions">
                        <a-button type="primary" size="small" @click="garmentImageUrl = ''">
                          <PictureOutlined /> 重新选择
                        </a-button>
                        <a-button type="primary" danger size="small" @click="clearGarment">
                          <CloseOutlined /> 清除
                        </a-button>
                      </div>
                    </div>
                  </div>
                </div>
              </a-form-item>

              <!-- 图案图片 -->
              <a-form-item label="图案图片" required>
                <div class="upload-container">
                  <!-- 上传按钮（始终显示） -->
                  <a-upload
                    v-if="patternImageUrls.length < 2"
                    :show-upload-list="false"
                    :before-upload="(file) => handleUploadPattern(file)"
                    accept="image/*"
                  >
                    <div class="upload-area pattern-upload">
                      <div class="upload-overlay">
                        <a-button type="default" size="large" class="upload-btn">
                          <PictureOutlined class="btn-icon" /> 添加图案图片
                        </a-button>
                        <span class="upload-tip"
                          >支持 1-2 张图案，尺寸 384-5000 像素，总数量（服装+图案）不超过 3 张</span
                        >
                      </div>
                    </div>
                  </a-upload>

                  <!-- 图案预览列表 -->
                  <div v-if="patternImageUrls.length" class="pattern-images-wrapper">
                    <div v-for="(url, index) in patternImageUrls" :key="index" class="pattern-image-item">
                      <a-image
                        :src="url"
                        class="pattern-uploaded-image"
                        :preview="{ src: url }"
                      />
                      <a-button
                        type="primary"
                        danger
                        size="small"
                        class="pattern-remove-action"
                        @click="removePattern(index)"
                      >
                        <CloseOutlined /> 删除
                      </a-button>
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
        <a-col :xs="24" :sm="24" :md="24" :lg="14" :xl="14">
          <div class="preview-section">
            <h2 class="section-title"><EyeOutlined class="title-icon" /> 融合效果</h2>

            <a-spin :spinning="polling" size="large" class="result-spin">
              <div v-if="resultUrls.length" class="result-grid">
                <div
                  v-for="(url, index) in resultUrls"
                  :key="index"
                  class="result-item"
                  :class="{ 'selected': selectedImageUrl === url }"
                  @click="selectImage(url)"
                >
                  <a-image :src="url" class="result-image" :preview="{ src: url }" />
                  <div class="result-mask">
                    <span class="result-index">效果 {{ index + 1 }}</span>
                  </div>
                  <div v-if="selectedImageUrl === url" class="selected-badge">
                    <CheckCircleFilled class="check-icon" />
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

            <!-- 保存按钮区域 -->
            <div v-if="resultUrls.length && selectedImageUrl" class="save-section">
              <a-button
                type="primary"
                size="large"
                :loading="saving"
                @click="handleSaveSelected"
                class="save-btn"
              >
                <template #icon>
                  <SaveOutlined />
                </template>
                {{ saving ? '保存中...' : '保存选中的图片' }}
              </a-button>
              <p class="save-tip">已选中图片，点击保存后将替换原有的四张图片</p>
            </div>
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
  CheckCircleFilled,
  SaveOutlined,
} from '@ant-design/icons-vue'
import { uploadImage, submitTask, queryStatus, getResults, saveSelectedImage } from '@/api/imageFusionController'

// 功能特性数据
const features = ref([
  {
    icon: '🎯',
    title: 'AI 智能融合',
    desc: '基于深度学习算法，自动识别服装轮廓与图案特征，实现完美融合'
  },
  {
    icon: '⚡',
    title: '快速生成',
    desc: '仅需几秒钟，即可获得多种融合效果，大幅提升设计效率'
  },
  {
    icon: '🎨',
    title: '多样化效果',
    desc: '一次生成多个融合结果，提供丰富的设计方案供您选择'
  },
  {
    icon: '✨',
    title: '高清输出',
    desc: '支持高分辨率图片处理，确保最终效果清晰细腻，满足专业需求'
  }
])

// 使用步骤数据
const steps = ref([
  {
    icon: '📤',
    title: '上传服装图片',
    desc: '选择您想要设计的服装照片，支持 JPG/PNG 格式，图片尺寸需在 384-5000 像素之间，建议使用白底图片效果更佳'
  },
  {
    icon: '🖼️',
    title: '添加图案素材',
    desc: '上传 1-2 张您喜欢的图案，可以是几何图形、艺术插画或任何创意元素，同样需要满足尺寸要求'
  },
  {
    icon: '🚀',
    title: '一键生成效果',
    desc: '点击开始融合按钮，AI 将自动处理并在几秒内呈现多种融合效果供您挑选'
  }
])

const garmentImageUrl = ref<string>('')
const garmentImageFile = ref<File | null>(null) // 保存服装图片文件对象
const patternImageUrls = ref<string[]>([])
const patternImageFiles = ref<File[]>([]) // 保存图案图片文件对象数组

const submitting = ref(false)
const polling = ref(false)
const saving = ref(false)
const currentTaskId = ref<string | null>(null)
const resultUrls = ref<string[]>([])
const selectedImageUrl = ref<string>('')
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
  // 释放所有本地预览URL的内存
  if (garmentImageUrl.value) {
    URL.revokeObjectURL(garmentImageUrl.value)
  }
  patternImageUrls.value.forEach(url => {
    URL.revokeObjectURL(url)
  })
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

// 验证图片尺寸（必须在 384-5000 像素之间）
const validateImageSize = (file: File): Promise<boolean> => {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const width = img.width
        const height = img.height
        const minSize = 384
        const maxSize = 5000

        if (width < minSize || height < minSize) {
          message.error(
            `图片尺寸过小！当前尺寸：${width}x${height}，最小要求：${minSize}x${minSize}像素`
          )
          resolve(false)
        } else if (width > maxSize || height > maxSize) {
          message.error(
            `图片尺寸过大！当前尺寸：${width}x${height}，最大限制：${maxSize}x${maxSize}像素`
          )
          resolve(false)
        } else {
          resolve(true)
        }
      }
      img.onerror = () => {
        message.error('图片格式错误，无法读取')
        resolve(false)
      }
      img.src = e.target?.result as string
    }
    reader.onerror = () => {
      message.error('读取图片失败')
      resolve(false)
    }
    reader.readAsDataURL(file)
  })
}

// 上传服装图片（仅本地预览）
const handleUploadGarment = async (file: File) => {
  // 1. 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return false
  }

  // 2. 验证图片尺寸
  const isValidSize = await validateImageSize(file)
  if (!isValidSize) {
    return false
  }

  // 3. 保存文件对象，创建本地预览URL
  garmentImageFile.value = file
  garmentImageUrl.value = URL.createObjectURL(file)
  message.success('服装图片已选择，点击"开始图案融合"按钮后将上传')
  return false
}

// 上传图案图片（仅本地预览）
const handleUploadPattern = async (file: File) => {
  if (patternImageUrls.value.length >= 2) {
    message.warning('最多只能上传 2 张图案图片')
    return false
  }

  // 1. 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return false
  }

  // 2. 验证图片尺寸
  const isValidSize = await validateImageSize(file)
  if (!isValidSize) {
    return false
  }

  // 3. 保存文件对象，创建本地预览URL
  patternImageFiles.value.push(file)
  patternImageUrls.value.push(URL.createObjectURL(file))
  message.success('图案图片已选择')
  return false
}

const clearGarment = () => {
  if (garmentImageUrl.value) {
    URL.revokeObjectURL(garmentImageUrl.value) // 释放内存
  }
  garmentImageUrl.value = ''
  garmentImageFile.value = null
}

const removePattern = (index: number) => {
  if (patternImageUrls.value[index]) {
    URL.revokeObjectURL(patternImageUrls.value[index]) // 释放内存
  }
  patternImageUrls.value.splice(index, 1)
  patternImageFiles.value.splice(index, 1)
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

        // 后端返回的是数组或包含results字段的对象
        if (Array.isArray(resultData)) {
          resultUrls.value = resultData
        } else if (resultData?.results && Array.isArray(resultData.results)) {
          // 如果返回的是对象，从results字段中提取URL数组
          resultUrls.value = resultData.results.map((item: any) => item.url || item)
        } else {
          resultUrls.value = []
        }

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
  selectedImageUrl.value = '' // 清空选中的图片

  try {
    // 1. 上传所有图片到 COS
    message.loading({ content: '正在上传图片...', key: 'upload', duration: 0 })
    
    const uploadedUrls: string[] = []
    
    // 上传服装图片
    if (garmentImageFile.value) {
      const formData = new FormData()
      formData.append('file', garmentImageFile.value)
      const res = await uploadImage(formData)
      const url = (res as any)?.data?.data || (res as any)?.data?.url
      if (!url) {
        throw new Error('服装图片上传失败')
      }
      uploadedUrls.push(url)
    }

    // 上传图案图片
    for (let i = 0; i < patternImageFiles.value.length; i++) {
      const formData = new FormData()
      formData.append('file', patternImageFiles.value[i])
      const res = await uploadImage(formData)
      const url = (res as any)?.data?.data || (res as any)?.data?.url
      if (!url) {
        throw new Error(`图案图片 ${i + 1} 上传失败`)
      }
      uploadedUrls.push(url)
    }

    message.success({ content: '图片上传成功！', key: 'upload' })

    // 2. 提交融合任务
    const imageUrls = uploadedUrls.join(',')
    const userId = 0 // TODO：根据项目实际登录逻辑替换 userId

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
    message.error({ content: e?.message || '提交融合任务失败，请稍后重试', key: 'upload' })
    errorMessage.value = e?.message || '提交融合任务失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

// 选择图片
const selectImage = (url: string) => {
  selectedImageUrl.value = url
  message.info('已选中该图片，点击保存按钮保存')
}

// 保存选中的图片
const handleSaveSelected = async () => {
  if (!currentTaskId.value || !selectedImageUrl.value) {
    message.warning('请先选择一张图片')
    return
  }

  saving.value = true
  try {
    const res = await saveSelectedImage({
      taskId: currentTaskId.value,
      imageUrl: selectedImageUrl.value,
    })

    if ((res as any)?.data?.data || (res as any)?.code === 0) {
      message.success('保存成功！')
      // 保存成功后，只保留选中的图片
      resultUrls.value = [selectedImageUrl.value]
    } else {
      throw new Error('保存失败')
    }
  } catch (e: any) {
    console.error('保存失败:', e)
    message.error(e?.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
/* 全局基础样式 */
.fusion-page {
  min-height: 100vh;
  background-image: url('@/assets/backgroundImage/mj-bg.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  padding: 32px 24px;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  color: #ffffff;
  animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-header h1 {
  font-size: clamp(28px, 5vw, 48px);
  font-weight: 700;
  margin: 0 0 16px;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
  letter-spacing: 1px;
  background: linear-gradient(135deg, #fff 0%, #e0f2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: clamp(14px, 2.5vw, 18px);
  margin: 0;
  opacity: 0.9;
  max-width: 700px;
  margin: 0 auto;
  line-height: 1.8;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  font-weight: 300;
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
  border-radius: 24px;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.25);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  overflow: hidden;
  animation: fadeInUp 0.8s ease-out;
  transition: all 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fusion-card:hover {
  box-shadow: 0 25px 100px rgba(0, 0, 0, 0.3);
}

.fusion-card :deep(.ant-card-body) {
  padding: clamp(20px, 4vw, 48px);
}

/* 区域标题 */
.section-title {
  font-size: clamp(18px, 3vw, 24px);
  font-weight: 600;
  margin-bottom: 28px;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 2px solid transparent;
  background: linear-gradient(to right, #e2e8f0 0%, transparent 100%) bottom / 100% 2px no-repeat;
  position: relative;
}

.section-title::before {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: linear-gradient(90deg, #4299e1 0%, #38b2ac 100%);
}

.title-icon {
  color: #4299e1;
  font-size: clamp(20px, 3vw, 26px);
  filter: drop-shadow(0 2px 4px rgba(66, 153, 225, 0.3));
}

/* 表单样式 */
.fusion-form {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: clamp(16px, 3vw, 28px);
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
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
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: clamp(16px, 3vw, 24px);
  border: 2px dashed #cbd5e0;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  min-height: 180px;
}

.upload-area:hover {
  border-color: #4299e1;
  background: linear-gradient(135deg, #f0f8fb 0%, #e6f7ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(66, 153, 225, 0.15);
}

.upload-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

/* 图片预览容器 */
.image-preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-preview-container {
  position: relative;
  display: inline-block;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-preview-container:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
  transform: translateY(-4px);
}

.uploaded-image {
  display: block;
  max-width: 100%;
  height: auto;
  border-radius: 16px;
}

:deep(.uploaded-image img) {
  display: block;
  width: auto !important;
  height: auto !important;
  max-width: 100%;
  border-radius: 16px;
}

.image-actions {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview-container:hover .image-actions {
  opacity: 1;
}

.upload-btn {
  border-radius: 12px !important;
  font-size: clamp(13px, 2vw, 15px) !important;
  padding: 12px 24px !important;
  height: auto !important;
  border-color: #4299e1 !important;
  color: #4299e1 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  font-weight: 500 !important;
}

:deep(.upload-btn:hover) {
  background: linear-gradient(135deg, #ebf8ff 0%, #e6f7ff 100%) !important;
  border-color: #3182ce !important;
  color: #3182ce !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.2);
}

.btn-icon {
  margin-right: 8px;
}

.upload-tip {
  font-size: clamp(12px, 1.8vw, 13px);
  color: #64748b;
  text-align: center;
  line-height: 1.6;
  max-width: 100%;
}

/* 图案图片预览 */
.pattern-images-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
}

.pattern-image-item {
  position: relative;
  display: inline-block;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.pattern-image-item:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

.pattern-uploaded-image {
  display: block;
  max-width: 300px;
  height: auto;
  border-radius: 16px;
}

:deep(.pattern-uploaded-image img) {
  display: block;
  width: auto !important;
  height: auto !important;
  max-width: 300px;
  border-radius: 16px;
}

.pattern-remove-action {
  position: absolute;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 10;
}

.pattern-image-item:hover .pattern-remove-action {
  opacity: 1;
}

/* 融合按钮样式 */
.fusion-btn {
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%) !important;
  border: none !important;
  border-radius: 16px !important;
  font-size: clamp(14px, 2.5vw, 17px) !important;
  font-weight: 600 !important;
  height: clamp(50px, 8vw, 60px) !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(66, 153, 225, 0.35);
  letter-spacing: 0.5px;
}

:deep(.fusion-btn:hover:not(:disabled)) {
  background: linear-gradient(135deg, #3182ce 0%, #2c7a7b 100%) !important;
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(66, 153, 225, 0.45);
}

:deep(.fusion-btn:active:not(:disabled)) {
  transform: translateY(-1px);
}

:deep(.fusion-btn:disabled) {
  background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%) !important;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.6;
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
  min-height: 450px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  padding: clamp(20px, 3vw, 28px);
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(clamp(160px, 30vw, 260px), 1fr));
  gap: clamp(16px, 3vw, 28px);
  justify-content: center;
}

.result-item {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  aspect-ratio: 1;
  cursor: pointer;
}

.result-item:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.2);
}

.result-item.selected {
  border: 4px solid #4299e1;
  box-shadow: 0 0 0 4px rgba(66, 153, 225, 0.2), 0 16px 32px rgba(66, 153, 225, 0.3);
  transform: scale(1.05);
}

.result-image {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover;
  border-radius: 20px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.result-item:hover .result-image {
  transform: scale(1.08);
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

/* 选中徽章 */
.selected-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(66, 153, 225, 0.5);
  animation: scaleIn 0.3s ease-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.check-icon {
  font-size: 28px;
  color: #ffffff;
}

/* 保存按钮区域 */
.save-section {
  margin-top: 32px;
  padding: 28px;
  background: linear-gradient(135deg, #f0f8fb 0%, #e6f7ff 100%);
  border-radius: 20px;
  text-align: center;
  border: 2px solid #4299e1;
  animation: fadeInUp 0.5s ease-out;
}

.save-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%) !important;
  border: none !important;
  border-radius: 16px !important;
  font-size: clamp(14px, 2.5vw, 17px) !important;
  font-weight: 600 !important;
  height: clamp(50px, 8vw, 60px) !important;
  min-width: 200px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(72, 187, 120, 0.35);
  letter-spacing: 0.5px;
}

:deep(.save-btn:hover:not(:disabled)) {
  background: linear-gradient(135deg, #38a169 0%, #2f855a 100%) !important;
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(72, 187, 120, 0.45);
}

:deep(.save-btn:active:not(:disabled)) {
  transform: translateY(-1px);
}

:deep(.save-btn:disabled) {
  background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%) !important;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.6;
}

.save-tip {
  margin-top: 16px;
  margin-bottom: 0;
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
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

/* 功能特性卡片区域 */
.features-section {
  max-width: 1400px;
  margin: 0 auto 48px;
  animation: fadeInUp 1s ease-out 0.2s both;
}

.feature-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: clamp(24px, 4vw, 32px);
  text-align: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(66, 153, 225, 0.2);
  border-color: rgba(66, 153, 225, 0.3);
}

.feature-icon {
  font-size: clamp(40px, 6vw, 56px);
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.feature-card:nth-child(1) .feature-icon { animation-delay: 0s; }
.feature-card:nth-child(2) .feature-icon { animation-delay: 0.3s; }
.feature-card:nth-child(3) .feature-icon { animation-delay: 0.6s; }
.feature-card:nth-child(4) .feature-icon { animation-delay: 0.9s; }

.feature-title {
  font-size: clamp(16px, 2.5vw, 20px);
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 12px;
  letter-spacing: 0.3px;
}

.feature-desc {
  font-size: clamp(13px, 2vw, 14px);
  color: #64748b;
  line-height: 1.7;
  margin: 0;
}

/* 使用步骤指引 */
.steps-guide {
  max-width: 1400px;
  margin: 0 auto 48px;
  animation: fadeInUp 1.2s ease-out 0.4s both;
}

.guide-title {
  font-size: clamp(24px, 4vw, 36px);
  font-weight: 700;
  text-align: center;
  color: #ffffff;
  margin-bottom: 36px;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
}

.step-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(241, 245, 249, 0.9) 100%);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: clamp(28px, 4vw, 36px);
  text-align: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.step-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #4299e1 0%, #38b2ac 100%);
  transform: scaleX(0);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.step-card:hover::before {
  transform: scaleX(1);
}

.step-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(66, 153, 225, 0.25);
  border-color: rgba(66, 153, 225, 0.4);
}

.step-number-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.4);
}

.step-icon {
  font-size: clamp(48px, 7vw, 64px);
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.step-title {
  font-size: clamp(17px, 2.8vw, 22px);
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 16px;
  letter-spacing: 0.3px;
}

.step-desc {
  font-size: clamp(13px, 2vw, 14px);
  color: #64748b;
  line-height: 1.8;
  margin: 0;
}

/* 响应式优化 */
@media (max-width: 1200px) {
  .fusion-page {
    padding: 28px 20px;
  }

  .result-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}

@media (max-width: 992px) {
  .fusion-page {
    padding: 24px 16px;
  }

  .page-header {
    margin-bottom: 32px;
  }

  .fusion-card :deep(.ant-card-body) {
    padding: 32px;
  }

  .result-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .fusion-page {
    padding: 20px 12px;
  }

  .page-header {
    margin-bottom: 28px;
  }

  .page-header h1 {
    margin-bottom: 12px;
  }

  .features-section {
    margin-bottom: 36px;
  }

  .feature-card {
    padding: 24px;
  }

  .steps-guide {
    margin-bottom: 36px;
  }

  .guide-title {
    margin-bottom: 28px;
  }

  .step-card {
    padding: 28px;
  }

  .fusion-card {
    border-radius: 20px;
  }

  .fusion-card :deep(.ant-card-body) {
    padding: 24px;
  }

  .section-title {
    margin-bottom: 20px;
    padding-bottom: 12px;
  }

  .fusion-form {
    padding: 20px;
  }

  :deep(.ant-form-item) {
    margin-bottom: 20px;
  }

  .upload-area {
    padding: 18px;
  }

  .upload-btn {
    width: 100%;
  }

  .preview-section {
    padding: 20px;
    min-height: 350px;
  }

  .result-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 16px;
  }

  .selected-badge {
    width: 40px;
    height: 40px;
    top: 12px;
    right: 12px;
  }

  .check-icon {
    font-size: 24px;
  }

  .save-section {
    padding: 20px;
  }

  .save-btn {
    width: 100%;
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .fusion-page {
    padding: 16px 10px;
  }

  .page-header {
    margin-bottom: 24px;
  }

  .features-section {
    margin-bottom: 28px;
  }

  .feature-card {
    padding: 20px;
  }

  .feature-icon {
    margin-bottom: 16px;
  }

  .steps-guide {
    margin-bottom: 28px;
  }

  .guide-title {
    margin-bottom: 24px;
  }

  .step-card {
    padding: 24px;
  }

  .step-number-badge {
    width: 32px;
    height: 32px;
    font-size: 14px;
    top: 12px;
    right: 12px;
  }

  .step-icon {
    margin-bottom: 20px;
  }

  .fusion-card {
    border-radius: 16px;
  }

  .fusion-card :deep(.ant-card-body) {
    padding: 20px;
  }

  .section-title {
    font-size: 18px;
    gap: 8px;
  }

  .fusion-form {
    padding: 16px;
    border-radius: 16px;
  }

  .upload-area {
    padding: 16px;
  }

  .upload-btn {
    width: 100% !important;
    padding: 10px 18px !important;
  }

  .preview-section {
    padding: 16px;
    min-height: 300px;
    border-radius: 16px;
  }

  .result-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .result-item {
    border-radius: 16px;
  }

  .selected-badge {
    width: 36px;
    height: 36px;
    top: 10px;
    right: 10px;
  }

  .check-icon {
    font-size: 20px;
  }

  .save-section {
    padding: 16px;
    margin-top: 24px;
  }

  .save-btn {
    width: 100%;
    min-width: auto;
  }

  .save-tip {
    font-size: 13px;
    margin-top: 12px;
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
