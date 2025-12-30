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
                <div class="upload-tip">支持JPG、JPEG、PNG、BMPͼƬ<br>格式，大小不超过5M</div>
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
                <div class="upload-tip">支持JPG、JPEG、PNG、BMPͼƬ<br>格式，大小不超过5M</div>
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

          <!-- 保存结果 -->
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
            <span class="flow-arrow">→</span>
            <span>图案图</span>
            <span class="flow-arrow">→</span>
            <span>生成结果</span>
          </div>

          <!-- 示例图片 -->
          <div class="example-images">
            <div class="example-item">
              <img :src="exampleGarment" alt="服装图示例" />
              <span class="example-label">服装图示例</span>
            </div>
            <span class="image-arrow">→</span>
            <div class="example-item">
              <img :src="examplePattern" alt="图案图示例" />
              <span class="example-label">图案图示例</span>
            </div>
            <span class="image-arrow">→</span>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadImage, submitTask, queryStatus, getResults, saveSelectedImage } from '@/api/imageFusionController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useImageFusionTaskStore } from '@/stores/useImageFusionTaskStore'

const loginUserStore = useLoginUserStore()
const fusionTaskStore = useImageFusionTaskStore()

const storageKey = 'image_fusion_task'
const pollIntervalMs = 3000

// 示例图片
import exampleGarment from '@/assets/imagefusion/服装.png'
import examplePattern from '@/assets/imagefusion/图案.png'
import exampleResult from '@/assets/imagefusion/生成效果图.png'

// 图片上传状态
const garmentImageUrl = ref<string>('')
const garmentImageFile = ref<File | null>(null)
const patternImageUrls = ref<string[]>([])
const patternImageFiles = ref<File[]>([])
const uploadedGarmentUrl = ref<string>('')
const uploadedPatternUrls = ref<string[]>([])

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

let pollTimer: ReturnType<typeof window.setTimeout> | null = null

const clearPollTimer = () => {
  if (pollTimer !== null) {
    window.clearTimeout(pollTimer)
    pollTimer = null
  }
}

onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  fusionTaskStore.markRead()

  const snapshot = readTaskSnapshot()
  const creationParams = fusionTaskStore.getCreationParams()
  if (snapshot) {
    applySnapshot(snapshot)
  } else if (creationParams) {
    applyCreationParams(creationParams)
  }

  if (!snapshot) {
    return
  }

  if (snapshot.status === 'SUCCEEDED') {
    if (snapshot.resultUrls?.length) {
      resultUrls.value = snapshot.resultUrls
      polling.value = false
      return
    }
    polling.value = true
    pollFusionStatus(snapshot.taskId)
    return
  }

  if (snapshot.status === 'PENDING' || snapshot.status === 'PROCESSING') {
    polling.value = true
    pollFusionStatus(snapshot.taskId)
  }
})

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
  const runningStatus = fusionTaskStore.notification?.status
  const isTaskRunning =
    submitting.value ||
    polling.value ||
    runningStatus === 'PENDING' ||
    runningStatus === 'PROCESSING'
  // 至少需要 1 张服装图 + 1 张图案图，且总张数 <= 3
  return (
    !!garmentImageUrl.value &&
    patternImageUrls.value.length >= 1 &&
    1 + patternImageUrls.value.length <= 3 &&
    !isTaskRunning
  )
})

type FusionTaskSnapshot = {
  taskId: string
  status: string
  resultUrls?: string[]
  errorMessage?: string
  updateTime?: number
  garmentUrl?: string
  patternUrls?: string[]
  similarity?: number
}

const readTaskSnapshot = (): FusionTaskSnapshot | null => {
  const raw = localStorage.getItem(storageKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as FusionTaskSnapshot
  } catch {
    return null
  }
}

const saveTaskSnapshot = (snapshot: FusionTaskSnapshot) => {
  localStorage.setItem(storageKey, JSON.stringify(snapshot))
}

const stopPolling = () => {
  clearPollTimer()
}

const applyCreationParams = (params: {
  garmentUrl?: string
  patternUrls?: string[]
  similarity?: number
}) => {
  if (params.garmentUrl !== undefined) {
    garmentImageUrl.value = params.garmentUrl
    garmentImageFile.value = null
    uploadedGarmentUrl.value = params.garmentUrl
  }
  if (params.patternUrls !== undefined) {
    patternImageUrls.value = [...params.patternUrls]
    patternImageFiles.value = []
    uploadedPatternUrls.value = [...params.patternUrls]
  }
  if (params.similarity !== undefined) {
    similarity.value = params.similarity
  }
}

const applySnapshot = (snapshot: FusionTaskSnapshot) => {
  currentTaskId.value = snapshot.taskId
  applyCreationParams({
    garmentUrl: snapshot.garmentUrl,
    patternUrls: snapshot.patternUrls,
    similarity: snapshot.similarity,
  })
  if (snapshot.resultUrls?.length) {
    resultUrls.value = snapshot.resultUrls
  }
}

const pollFusionStatus = async (taskId: string) => {
  try {
    const res = await queryStatus({ taskId })
    const taskData: any = res?.data?.data || res?.data
    if (!taskData) {
      throw new Error('任务状态为空')
    }
    const status = taskData.taskStatus

    if (status === 'SUCCEEDED') {
      const resultRes = await getResults({ taskId })
      const resultData: any = resultRes?.data?.data || resultRes?.data

      if (Array.isArray(resultData)) {
        resultUrls.value = resultData
      } else if (resultData?.results && Array.isArray(resultData.results)) {
        resultUrls.value = resultData.results.map((item: any) => item.url || item)
      } else if (Array.isArray(taskData?.localImageUrlList)) {
        resultUrls.value = taskData.localImageUrlList
      } else {
        resultUrls.value = []
      }

      polling.value = false
      stopPolling()

      saveTaskSnapshot({
        taskId,
        status,
        resultUrls: resultUrls.value,
        errorMessage: taskData.errorMessage,
        updateTime: taskData.updateTime,
        garmentUrl: uploadedGarmentUrl.value,
        patternUrls: uploadedPatternUrls.value,
        similarity: similarity.value,
      })

      fusionTaskStore.markSucceeded(taskId, resultUrls.value)
      message.success('图案融合完成，返回衣图智融页可查看最新效果')
      return
    }

    if (status === 'FAILED') {
      errorMessage.value = taskData.errorMessage || '图案融合失败，请稍后重试'
      polling.value = false
      stopPolling()

      saveTaskSnapshot({
        taskId,
        status,
        errorMessage: errorMessage.value,
        updateTime: taskData.updateTime,
        garmentUrl: uploadedGarmentUrl.value,
        patternUrls: uploadedPatternUrls.value,
        similarity: similarity.value,
      })

      fusionTaskStore.markFailed(taskId, errorMessage.value)
      return
    }

    polling.value = true
    fusionTaskStore.markProcessing(taskId)
    saveTaskSnapshot({
      taskId,
      status: status || 'PENDING',
      updateTime: taskData.updateTime,
      garmentUrl: uploadedGarmentUrl.value,
      patternUrls: uploadedPatternUrls.value,
      similarity: similarity.value,
    })

    stopPolling()
    pollTimer = window.setTimeout(() => {
      pollFusionStatus(taskId)
    }, pollIntervalMs)
  } catch (e: any) {
    console.error('查询任务状态失败:', e)
    errorMessage.value = e?.message || '查询任务状态失败'
    polling.value = false
    stopPolling()
  }
}

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
  uploadedGarmentUrl.value = ''
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
  if (uploadedPatternUrls.value.length) {
    uploadedPatternUrls.value = []
  }
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
  uploadedGarmentUrl.value = ''
}

const removePattern = (index: number) => {
  if (patternImageUrls.value[index]) {
    URL.revokeObjectURL(patternImageUrls.value[index]) // 释放内存
  }
  patternImageUrls.value.splice(index, 1)
  patternImageFiles.value.splice(index, 1)
  if (uploadedPatternUrls.value.length) {
    uploadedPatternUrls.value.splice(index, 1)
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value) {
    return
  }

  stopPolling()
  submitting.value = true
  polling.value = false
  errorMessage.value = ''
  resultUrls.value = []
  selectedImageUrl.value = '' // 清空选中的图片

  try {
    // 1. 上传所有图片到 COS
    message.loading({ content: '正在上传图片...', key: 'upload', duration: 0 })

    const uploadedUrls: string[] = []
    let garmentUrl = ''

    // 上传服装图片（或复用已上传URL）
    if (garmentImageFile.value) {
      const formData = new FormData()
      formData.append('file', garmentImageFile.value)
      const res = await uploadImage(formData)
      garmentUrl = (res as any)?.data?.data || (res as any)?.data?.url
      if (!garmentUrl) {
        throw new Error('服装图片上传失败')
      }
    } else if (uploadedGarmentUrl.value) {
      garmentUrl = uploadedGarmentUrl.value
    }

    if (!garmentUrl) {
      throw new Error('请先上传服装图片')
    }
    uploadedUrls.push(garmentUrl)

    const patternUrls: string[] = []
    // 上传图案图片（或复用已上传URL）
    if (patternImageFiles.value.length) {
      for (let i = 0; i < patternImageFiles.value.length; i++) {
        const formData = new FormData()
        formData.append('file', patternImageFiles.value[i])
        const res = await uploadImage(formData)
        const url = (res as any)?.data?.data || (res as any)?.data?.url
        if (!url) {
          throw new Error(`图案图片 ${i + 1} 上传失败`)
        }
        patternUrls.push(url)
      }
    } else if (uploadedPatternUrls.value.length) {
      patternUrls.push(...uploadedPatternUrls.value)
    }

    if (!patternUrls.length) {
      throw new Error('请先上传图案图片')
    }

    uploadedUrls.push(...patternUrls)
    uploadedGarmentUrl.value = garmentUrl
    uploadedPatternUrls.value = patternUrls

    message.success({ content: '图片上传成功！', key: 'upload' })

    // 2. 提交融合任务
    const imageUrls = uploadedUrls.join(',')
    const userId = loginUserStore.loginUser?.id ? Number(loginUserStore.loginUser.id) : 0

    const res = await submitTask({ userId, imageUrls } as any)
    const taskId = (res as any)?.data?.data || (res as any)?.data
    if (!taskId) {
      throw new Error('未获取到任务 ID')
    }

    currentTaskId.value = String(taskId)
    saveTaskSnapshot({
      taskId: String(taskId),
      status: 'PENDING',
      updateTime: Date.now(),
      garmentUrl: uploadedGarmentUrl.value,
      patternUrls: uploadedPatternUrls.value,
      similarity: similarity.value,
    })

    fusionTaskStore.createTask({
      taskId: String(taskId),
      garmentUrl: uploadedGarmentUrl.value,
      patternUrls: uploadedPatternUrls.value,
      similarity: similarity.value,
    })

    message.success('任务提交成功，正在进行图案融合...')
    polling.value = true
    pollFusionStatus(String(taskId))
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
      currentTaskId.value = null
      resultUrls.value = []
      selectedImageUrl.value = ''
      errorMessage.value = ''
      polling.value = false
      localStorage.removeItem(storageKey)
      fusionTaskStore.clearTask()
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
      currentTaskId.value = null
      resultUrls.value = []
      selectedImageUrl.value = ''
      errorMessage.value = ''
      polling.value = false
      localStorage.removeItem(storageKey)
      fusionTaskStore.clearTask()
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
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700&family=Newsreader:wght@400;600;700&display=swap');

/* 全局基础样式 */
.fusion-page {
  --ink: #1f1a15;
  --muted: #7a6f66;
  --accent: #d45b2d;
  --accent-2: #2a9d8f;
  --surface: #fffdf8;
  --surface-2: #f6efe6;
  --stroke: rgba(31, 26, 21, 0.08);
  --shadow: 0 22px 60px rgba(31, 26, 21, 0.12);
  --transition-duration: 0.35s;
  --transition-easing: cubic-bezier(0.22, 1, 0.36, 1);
  --font-body: 'Manrope', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-display: 'Newsreader', 'Noto Serif SC', 'Songti SC', serif;
  height: calc(100vh - 150px);
  min-height: calc(100vh - 150px);
  overflow: hidden;
  background:
    radial-gradient(900px 420px at 8% -10%, rgba(240, 181, 128, 0.4), transparent 65%),
    radial-gradient(800px 380px at 92% 5%, rgba(122, 210, 196, 0.35), transparent 60%),
    linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
  color: var(--ink);
  font-family: var(--font-body);
  position: relative;
  isolation: isolate;
}

.fusion-page::before {
  content: '';
  position: absolute;
  inset: -10% -20% auto;
  height: 420px;
  background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.7), transparent 65%);
  opacity: 0.8;
  z-index: 0;
  pointer-events: none;
}

.fusion-page::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(31, 26, 21, 0.06) 1px, transparent 1px);
  background-size: 22px 22px;
  opacity: 0.35;
  z-index: 0;
  pointer-events: none;
}

/* 主布局 - 左右分栏 */
.main-layout {
  display: flex;
  height: 100%;
  gap: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 左侧操作面板 */
.left-panel {
  width: 360px;
  min-width: 340px;
  background: rgba(255, 255, 255, 0.88);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--stroke);
  border-radius: 22px;
  box-shadow: var(--shadow);
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  backdrop-filter: blur(10px);
}

/* 面板区块 */
.panel-section {
  padding: 16px 18px;
  border-bottom: 1px solid var(--stroke);
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 12px;
}

.example-link {
  color: var(--accent);
  font-size: 12px;
  text-decoration: none;
}

.example-link:hover {
  color: #c24f26;
  text-decoration: underline;
}

/* 上传框 */
.upload-box {
  border: 1px dashed rgba(212, 91, 45, 0.35);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.75);
  min-height: 132px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-duration) var(--transition-easing);
  cursor: pointer;
}

.upload-box:hover {
  border-color: rgba(212, 91, 45, 0.6);
  background: rgba(255, 247, 238, 0.9);
  transform: translateY(-2px);
}

.upload-content {
  text-align: center;
  padding: 16px;
}

.upload-icon {
  color: rgba(31, 26, 21, 0.45);
  margin-bottom: 10px;
}

.upload-icon svg {
  stroke: currentColor;
}

.upload-text {
  font-size: 13px;
  color: var(--ink);
  margin-bottom: 6px;
  font-weight: 600;
}

.upload-tip {
  font-size: 11px;
  color: var(--muted);
  line-height: 1.5;
}

/* 已上传预览 */
.uploaded-preview {
  width: 100%;
  padding: 12px;
  text-align: center;
}

.uploaded-preview :deep(.ant-image) {
  max-width: 100%;
  max-height: 160px;
  border-radius: 12px;
  overflow: hidden;
}

.uploaded-preview :deep(.ant-image img) {
  max-width: 100%;
  max-height: 160px;
  object-fit: contain;
}

.preview-actions {
  margin-top: 12px;
}

.preview-actions :deep(.ant-btn) {
  background: rgba(212, 91, 45, 0.1);
  border: 1px solid rgba(212, 91, 45, 0.25);
  color: var(--accent);
}

.preview-actions :deep(.ant-btn:hover) {
  background: rgba(212, 91, 45, 0.18);
  border-color: rgba(212, 91, 45, 0.5);
}

/* 相似度滑块 */
.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.slider-header span:first-child {
  font-size: 14px;
  color: var(--ink);
  font-weight: 600;
}

.slider-value {
  font-size: 12px;
  color: var(--accent-2);
  padding: 4px 12px;
  background: rgba(42, 157, 143, 0.12);
  border: 1px solid rgba(42, 157, 143, 0.25);
  border-radius: 999px;
  font-weight: 600;
}

.similarity-slider {
  width: 100%;
}

:deep(.similarity-slider .ant-slider-rail) {
  background: rgba(31, 26, 21, 0.12);
}

:deep(.similarity-slider .ant-slider-track) {
  background: linear-gradient(90deg, var(--accent) 0%, #f08a5d 100%);
}

:deep(.similarity-slider .ant-slider-handle) {
  border-color: var(--accent);
  background: #fff7f0;
}

:deep(.similarity-slider .ant-slider-handle:hover) {
  border-color: #c24f26;
}

/* 底部生成按钮 */
.panel-footer {
  padding: 16px 18px;
  margin-top: auto;
  background: transparent;
}

.generate-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%) !important;
  border: none !important;
  border-radius: 14px !important;
  color: #ffffff !important;
  box-shadow: 0 16px 30px rgba(212, 91, 45, 0.22);
}

.generate-btn:hover {
  background: linear-gradient(135deg, #c24f26 0%, #e8784b 100%) !important;
}

.btn-text {
  font-size: 16px;
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
  padding: 26px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 26px;
  border: 1px solid var(--stroke);
  box-shadow: var(--shadow);
  height: 100%;
  overflow: hidden;
  backdrop-filter: blur(8px);
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
  color: var(--ink);
  margin: 0 0 8px;
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
  max-width: 900px;
}

.result-title {
  font-family: var(--font-display);
  font-size: 30px;
  font-weight: 700;
  color: var(--accent);
  margin: 0 0 16px;
}

.result-subtitle {
  font-size: 15px;
  color: var(--muted);
  margin: 0 0 26px;
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
  margin-top: 16px;
}

.result-large-image {
  max-width: 100%;
  max-height: 52vh;
  border-radius: 20px;
  border: 2px solid rgba(212, 91, 45, 0.2);
  box-shadow: 0 18px 32px rgba(31, 26, 21, 0.18);
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
  border-radius: 20px;
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
  border: 2px solid rgba(31, 26, 21, 0.08);
  background: rgba(255, 255, 255, 0.85);
  transition: all var(--transition-duration) var(--transition-easing);
  cursor: pointer;
}

.result-image-item:hover {
  transform: translateY(-4px);
}

.result-image-item.selected {
  border-color: rgba(212, 91, 45, 0.6);
  box-shadow: 0 12px 26px rgba(212, 91, 45, 0.25);
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
  color: var(--accent);
  font-weight: bold;
}

/* 示例图片 */
.example-images {
  display: flex;
  align-items: center;
  gap: 20px;
  justify-content: center;
}

.example-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.example-item img {
  width: 200px;
  height: 250px;
  object-fit: cover;
  border-radius: 18px;
  border: 1px solid rgba(31, 26, 21, 0.08);
  box-shadow: 0 16px 28px rgba(31, 26, 21, 0.12);
  transition: all var(--transition-duration) var(--transition-easing);
}

.example-item img:hover {
  border-color: rgba(212, 91, 45, 0.35);
  transform: translateY(-6px);
}

.example-label {
  font-size: 12px;
  color: var(--muted);
}

/* 保存结果 */
.save-section {
  margin-top: 24px;
}

.save-btn {
  height: 40px;
  border-radius: 999px !important;
  background: linear-gradient(135deg, var(--accent-2) 0%, #5cc2b0 100%) !important;
  border: none !important;
  font-weight: 500;
  padding: 0 28px;
  box-shadow: 0 14px 24px rgba(42, 157, 143, 0.22);
}

.save-btn:hover {
  background: linear-gradient(135deg, #228f81 0%, #3fb5a2 100%) !important;
}

/* 响应式 */
@media (max-width: 1200px) {
  .left-panel {
    width: 320px;
    min-width: 320px;
  }

  .result-image-item,
  .example-item img {
    width: 170px;
    height: 220px;
  }
}

@media (max-width: 992px) {
  .fusion-page {
    height: auto;
    min-height: 100%;
    overflow: visible;
  }

  .main-layout {
    flex-direction: column;
    height: auto;
    overflow: visible;
    gap: 20px;
  }

  .left-panel {
    width: 100%;
    min-width: 100%;
    border-right: none;
    border-bottom: none;
    height: auto;
    overflow: visible;
  }

  .right-panel {
    min-height: 60vh;
    height: auto;
    overflow: visible;
    padding: 26px 20px;
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
    width: 130px;
    height: 170px;
  }

  .image-arrow {
    font-size: 18px;
  }
}

@media (max-height: 860px) {
  .panel-section {
    padding: 14px 16px;
  }

  .upload-box {
    min-height: 118px;
  }

  .uploaded-preview :deep(.ant-image) {
    max-height: 140px;
  }

  .uploaded-preview :deep(.ant-image img) {
    max-height: 140px;
  }

  .panel-footer {
    padding: 14px 16px 16px;
  }

  .generate-btn {
    height: 44px;
    font-size: 15px;
  }

  .result-title {
    font-size: 26px;
  }

  .result-subtitle {
    margin-bottom: 20px;
  }

  .result-large-image {
    max-height: 48vh;
  }

  .result-main :deep(.ant-image img) {
    max-height: 48vh;
  }
}
</style>


