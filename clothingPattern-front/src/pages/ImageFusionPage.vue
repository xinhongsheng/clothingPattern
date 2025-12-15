<template>
  <div class="fusion-page">
    <!-- 主布局 - 左右分栏 -->
    <div class="main-layout">
      <!-- 左侧操作面板 -->
      <div class="left-panel">
        <!-- 上传图案A图 -->
        <div class="panel-section">
          <div class="section-header">
            <span>上传服装图</span>
            <a class="example-link" href="javascript:;">款式图例 &gt;</a>
          </div>
          <div class="upload-box">
            <a-upload
              v-if="!garmentImageUrl"
              :show-upload-list="false"
              :before-upload="(file) => handleUploadGarment(file)"
              accept="image/*"
            >
              <div class="upload-content">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1M16 7l-4-4m0 0L8 7m4-4v12"/>
                  </svg>
                </div>
                <div class="upload-text">上传图片</div>
                <div class="upload-tip">支持JPG、JPEG、PNG、BMP图片<br>格式，大小不超过5M</div>
              </div>
            </a-upload>
            <div v-else class="uploaded-preview">
              <a-image :src="garmentImageUrl" :preview="{ src: garmentImageUrl }" />
              <div class="preview-actions">
                <a-button size="small" @click="clearGarment">重新上传</a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 上传图案B图 -->
        <div class="panel-section">
          <div class="section-header">上传图案图</div>
          <div class="upload-box">
            <a-upload
              v-if="patternImageUrls.length === 0"
              :show-upload-list="false"
              :before-upload="(file) => handleUploadPattern(file)"
              accept="image/*"
            >
              <div class="upload-content">
                <div class="upload-icon">
                  <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1M16 7l-4-4m0 0L8 7m4-4v12"/>
                  </svg>
                </div>
                <div class="upload-text">上传图片</div>
                <div class="upload-tip">支持JPG、JPEG、PNG、BMP图片<br>格式，大小不超过5M</div>
              </div>
            </a-upload>
            <div v-else class="uploaded-preview">
              <a-image :src="patternImageUrls[0]" :preview="{ src: patternImageUrls[0] }" />
              <div class="preview-actions">
                <a-button size="small" @click="removePattern(0)">重新上传</a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 原图相似度 -->
        <div class="panel-section">
          <div class="slider-header">
            <span>原图相似度</span>
            <span class="slider-value">{{ similarity }}</span>
          </div>
          <a-slider v-model:value="similarity" :min="0" :max="100" class="similarity-slider" />
        </div>

        <!-- 底部生成按钮 -->
        <div class="panel-footer">
          <a-button
            type="primary"
            block
            size="large"
            :loading="submitting || polling"
            :disabled="!canSubmit"
            @click="handleSubmit"
            class="generate-btn"
          >
            <span class="btn-text">智能生成</span>
            <!-- <span class="btn-coin">
              <span>🪙</span>
              <span>20</span>
            </span> -->
          </a-button>
        </div>
      </div>

      <!-- 右侧结果展示区域 -->
      <div class="right-panel">
        <!-- 加载中状态 -->
        <div v-if="polling" class="result-loading">
          <a-spin size="large" />
          <div class="loading-text">
            <h3>正在融合中...</h3>
            <p>请稍候，AI正在处理您的图片</p>
          </div>
        </div>

        <!-- 已生成结果 - 展示大图 -->
        <div v-else-if="resultUrls.length" class="result-content">
          <h2 class="result-title">图案融合</h2>
          <p class="result-subtitle">将服装与图案在设计风格上进行自由融合，生成全新服装效果</p>

          <!-- 生成结果大图 -->
          <div class="result-main">
            <a-image 
              :src="resultUrls[0]" 
              :preview="{ src: resultUrls[0] }" 
              class="result-large-image"
            />
          </div>

          <!-- 保存按钮 -->
          <div class="save-section">
            <a-button type="primary" :loading="saving" @click="handleSaveResult" class="save-btn">
              💾 保存图案
            </a-button>
          </div>
        </div>

        <!-- 空状态 - 显示示例图片 -->
        <div v-else class="result-empty">
          <h2 class="result-title">图案融合</h2>
          <p class="result-subtitle">将服装图与图案图在设计风格上进行自由融合，生成全新服装效果</p>

          <!-- 步骤指示 -->
          <div class="flow-steps">
            <span>服装图</span>
            <span class="flow-arrow">»</span>
            <span>图案图</span>
            <span class="flow-arrow">»</span>
            <span>生成结果</span>
          </div>

          <!-- 示例图片 -->
          <div class="example-images">
            <div class="example-item">
              <img :src="exampleGarment" alt="服装图示例" />
              <span class="example-label">服装图示例</span>
            </div>
            <span class="image-arrow">»</span>
            <div class="example-item">
              <img :src="examplePattern" alt="图案图示例" />
              <span class="example-label">图案图示例</span>
            </div>
            <span class="image-arrow">»</span>
            <div class="example-item">
              <img :src="exampleResult" alt="生成效果示例" />
              <span class="example-label">生成效果示例</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadImage, submitTask, queryStatus, getResults, saveSelectedImage } from '@/api/imageFusionController'

// 示例图片
import exampleGarment from '@/assets/imagefusion/服装.png'
import examplePattern from '@/assets/imagefusion/图案.png'
import exampleResult from '@/assets/imagefusion/生成效果图.png'

// 图片上传状态
const garmentImageUrl = ref<string>('')
const garmentImageFile = ref<File | null>(null)
const patternImageUrls = ref<string[]>([])
const patternImageFiles = ref<File[]>([])

// 相似度滑块
const similarity = ref(44)

// 提交状态
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

// 保存生成结果图片
const handleSaveResult = async () => {
  if (!currentTaskId.value || !resultUrls.value.length) {
    message.warning('没有可保存的图片')
    return
  }

  saving.value = true
  try {
    const res = await saveSelectedImage({
      taskId: currentTaskId.value,
      imageUrl: resultUrls.value[0],
    })

    if ((res as any)?.data?.data || (res as any)?.code === 0) {
      message.success('保存成功！')
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
  height: 100vh;
  overflow: hidden;
  background-color: #1a1a2e;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 主布局 - 左右分栏 */
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 左侧操作面板 */
.left-panel {
  width: 400px;
  min-width: 400px;
  background-color: #252540;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #3a3a5c;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 左侧滚动条美化 */
.left-panel::-webkit-scrollbar {
  width: 6px;
}

.left-panel::-webkit-scrollbar-track {
  background: #1a1a2e;
}

.left-panel::-webkit-scrollbar-thumb {
  background: #3a3a5c;
  border-radius: 3px;
}

.left-panel::-webkit-scrollbar-thumb:hover {
  background: #4a4a6c;
}

/* 面板区块 */
.panel-section {
  padding: 20px;
  border-bottom: 1px solid #3a3a5c;
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  font-weight: 500;
  color: #ffffff;
  margin-bottom: 16px;
}

.example-link {
  color: #d4a574;
  font-size: 13px;
  text-decoration: none;
}

.example-link:hover {
  color: #e8c19a;
  text-decoration: underline;
}

/* 上传框 */
.upload-box {
  border: 1px dashed #4a4a6c;
  border-radius: 12px;
  background: #1a1a2e;
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-box:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.upload-content {
  text-align: center;
  padding: 20px;
}

.upload-icon {
  color: #666680;
  margin-bottom: 12px;
}

.upload-icon svg {
  stroke: currentColor;
}

.upload-text {
  font-size: 14px;
  color: #888;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #555;
  line-height: 1.6;
}

/* 已上传预览 */
.uploaded-preview {
  width: 100%;
  padding: 16px;
  text-align: center;
}

.uploaded-preview :deep(.ant-image) {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.uploaded-preview :deep(.ant-image img) {
  max-width: 100%;
  max-height: 200px;
  object-fit: contain;
}

.preview-actions {
  margin-top: 12px;
}

.preview-actions :deep(.ant-btn) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.preview-actions :deep(.ant-btn:hover) {
  background: rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

/* 相似度滑块 */
.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.slider-header span:first-child {
  font-size: 15px;
  color: #fff;
}

.slider-value {
  font-size: 14px;
  color: #888;
  padding: 2px 10px;
  background: #1a1a2e;
  border: 1px solid #3a3a5c;
  border-radius: 4px;
}

.similarity-slider {
  width: 100%;
}

:deep(.similarity-slider .ant-slider-rail) {
  background: #3a3a5c;
}

:deep(.similarity-slider .ant-slider-track) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

:deep(.similarity-slider .ant-slider-handle) {
  border-color: #667eea;
  background: #fff;
}

:deep(.similarity-slider .ant-slider-handle:hover) {
  border-color: #764ba2;
}

/* 底部生成按钮 */
.panel-footer {
  padding: 20px;
  margin-top: auto;
  background: #252540;
}

.generate-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 50px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(90deg, #d4a574 0%, #c9956a 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #1a1a2e !important;
}

.generate-btn:hover {
  background: linear-gradient(90deg, #e8c19a 0%, #d4a574 100%) !important;
}

.btn-text {
  font-size: 18px;
}

.btn-coin {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  font-size: 14px;
}

/* 右侧结果区域 */
.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background-color: #1a1a2e;
  height: 100vh;
  overflow: hidden;
}

/* 加载状态 */
.result-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.loading-text {
  text-align: center;
}

.loading-text h3 {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px;
}

.loading-text p {
  font-size: 14px;
  color: #888;
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
  max-width: 900px;
}

.result-title {
  font-size: 32px;
  font-weight: 700;
  color: #d4a574;
  margin: 0 0 16px;
}

.result-subtitle {
  font-size: 16px;
  color: #888;
  margin: 0 0 32px;
  line-height: 1.6;
}

/* 步骤指示 */
.flow-steps {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  font-size: 16px;
  color: #888;
}

.flow-arrow {
  color: #d4a574;
  font-weight: bold;
}

/* 生成结果大图 */
.result-main {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.result-large-image {
  max-width: 100%;
  max-height: 60vh;
  border-radius: 16px;
  border: 3px solid rgba(212, 165, 116, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.result-main :deep(.ant-image) {
  display: flex;
  justify-content: center;
}

.result-main :deep(.ant-image img) {
  max-width: 100%;
  max-height: 60vh;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 16px;
}

/* 结果图片展示 */
.result-images {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;
}

.result-image-item {
  position: relative;
  width: 220px;
  height: 280px;
  border-radius: 16px;
  overflow: hidden;
  border: 3px solid transparent;
  transition: all 0.3s ease;
  cursor: pointer;
}

.result-image-item:hover {
  transform: translateY(-4px);
}

.result-image-item.selected {
  border-color: #667eea;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.4);
}

.result-image-item :deep(.ant-image) {
  width: 100%;
  height: 100%;
}

.result-image-item :deep(.ant-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-arrow {
  font-size: 24px;
  color: #d4a574;
  font-weight: bold;
}

/* 示例图片 */
.example-images {
  display: flex;
  align-items: center;
  gap: 24px;
  justify-content: center;
}

.example-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.example-item img {
  width: 220px;
  height: 280px;
  object-fit: cover;
  border-radius: 16px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.example-item img:hover {
  border-color: rgba(212, 165, 116, 0.5);
  transform: translateY(-4px);
}

.example-label {
  font-size: 13px;
  color: #888;
}

/* 保存按钮 */
.save-section {
  margin-top: 32px;
}

.save-btn {
  height: 42px;
  border-radius: 8px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  font-weight: 500;
  padding: 0 32px;
}

.save-btn:hover {
  background: linear-gradient(135deg, #7b8ff0 0%, #8a5db5 100%) !important;
}

/* 响应式 */
@media (max-width: 1200px) {
  .left-panel {
    width: 360px;
    min-width: 360px;
  }

  .result-image-item,
  .example-item img {
    width: 180px;
    height: 230px;
  }
}

@media (max-width: 992px) {
  .main-layout {
    flex-direction: column;
    height: auto;
    overflow: visible;
  }

  .left-panel {
    width: 100%;
    min-width: 100%;
    border-right: none;
    border-bottom: 1px solid #3a3a5c;
    height: auto;
    overflow: visible;
  }

  .right-panel {
    min-height: 60vh;
    height: auto;
    overflow: visible;
    padding: 30px 20px;
  }

  .result-images,
  .example-images {
    flex-wrap: wrap;
  }

  .result-image-item,
  .example-item img {
    width: 150px;
    height: 190px;
  }
}

@media (max-width: 576px) {
  .result-title {
    font-size: 24px;
  }

  .result-subtitle {
    font-size: 14px;
  }

  .flow-steps {
    font-size: 14px;
    gap: 10px;
  }

  .result-image-item,
  .example-item img {
    width: 120px;
    height: 150px;
  }

  .image-arrow {
    font-size: 18px;
  }
}
</style>
