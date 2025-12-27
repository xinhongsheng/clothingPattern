<template>
  <div class="mj-pattern-generation-page">
    <!-- 未登录提示 -->
    <a-alert
      v-if="!isUserLoggedIn"
      message="需要登录才能使用智能创作功能"
      description="请先登录您的账号，然后即可开始创作属于您的独特图案。"
      type="warning"
      show-icon
      closable
      class="login-alert"
    >
      <template #icon>
        <span style="font-size: 20px">🔔</span>
      </template>
    </a-alert>

    <!-- 主内容区域 - 左右分栏布局 -->
    <div class="main-layout">
      <!-- 左侧操作面板 -->
      <div class="left-panel">
        <!-- 输入描述区域 -->
        <div class="panel-section">
          <div class="section-header">请输入关键词描述</div>
          <a-textarea
            v-model:value="formState.prompt"
            placeholder="请简要输入关键词描述，例如：卡通小熊，并使用自动扩写功能完成完整关键词扩写"
            :rows="4"
            :maxlength="1000"
            class="prompt-textarea"
          />
          <div class="prompt-footer">
            <span class="char-count">{{ formState.prompt?.length || 0 }} / 1000</span>
            <div class="prompt-actions-inline">
              <a-button type="text" size="small" @click="resetPrompt">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a-button
                type="text"
                size="small"
                :loading="expanding"
                :disabled="expanding || !formState.prompt || formState.prompt.length < 2"
                @click="handleExpandPrompt"
                class="expand-action-btn"
              >
                <template #icon><EditOutlined v-if="!expanding" /></template>
                自动扩写
              </a-button>
            </div>
          </div>
        </div>

        <!-- 风格引擎区域 -->
        <div class="panel-section">
          <div class="section-header style-header">风格引擎：</div>

          <!-- 风格选择器 -->
          <div class="style-grid">
            <div
              v-for="style in styleEngines"
              :key="style.id"
              class="style-item"
              :class="{ active: selectedStyleEngine?.id === style.id }"
              @click="selectStyleEngine(style)"
            >
              <div class="style-icon">{{ style.icon }}</div>
              <div class="style-name">{{ style.name }}</div>
            </div>
          </div>
        </div>

        <!-- 选择标签区域 -->
        <div class="panel-section">
          <div class="section-header">选择标签</div>
          <div class="tags-group">
            <div class="tag-row">
              <span class="tag-label">季节：</span>
              <div class="tag-options">
                <span
                  v-for="season in seasonOptions"
                  :key="season"
                  class="tag-option"
                  :class="{ active: formState.season === season }"
                  @click="toggleSeason(season)"
                >
                  {{ season }}
                </span>
              </div>
            </div>
            <div class="tag-row">
              <span class="tag-label">受众：</span>
              <div class="tag-options">
                <span
                  v-for="audience in audienceOptions"
                  :key="audience"
                  class="tag-option"
                  :class="{ active: formState.targetAudience === audience }"
                  @click="toggleAudience(audience)"
                >
                  {{ audience }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="panel-footer">
          <a-button
            type="primary"
            size="large"
            block
            :loading="generating"
            :disabled="generating || !formState.prompt"
            @click="handleGenerate"
            class="generate-btn"
          >
            <span class="btn-text">智能生成</span>
          </a-button>
        </div>
      </div>

      <!-- 右侧结果展示区 -->
      <div class="right-panel">
        <!-- 生成中状态 -->
        <div v-if="generating" class="result-loading">
          <div class="loading-grid">
            <div class="loading-item" v-for="i in 4" :key="i">
              <a-spin size="large" />
            </div>
          </div>
          <div class="loading-text">
            <p class="loading-title">🎨 AI 正在创作中...</p>
            <p class="loading-desc">预计需要 1-2 分钟，请耐心等待</p>
          </div>
        </div>

        <!-- 已生成结果 - 2x2 网格 -->
        <div v-else-if="currentStep === 2 && mjResponse" class="result-grid">
          <div class="result-images">
            <div
              v-for="i in 4"
              :key="i"
              class="result-image-item"
              :class="{ selected: selectedImageIndex === i }"
              @click="selectImage(i)"
            >
              <a-image
                :src="getQuadrantImage(mjResponse.imageUrl, i)"
                :preview="{ src: mjResponse.rawImageUrl }"
                :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5Ij7lm77niYc8L3RleHQ+PC9zdmc+'"
              />
              <div class="image-index">{{ i }}</div>
            </div>
          </div>

          <!-- 操作按钮区域 -->
          <div class="result-actions" v-if="selectedImageIndex">
            <div class="action-row">
              <a-button @click="handleUpsample(selectedImageIndex)" :loading="executing">
                🔍 放大图片 {{ selectedImageIndex }}
              </a-button>
              <a-button @click="handleVariation(selectedImageIndex)" :loading="executing">
                🎨 变体图片 {{ selectedImageIndex }}
              </a-button>
            </div>
            <a-button @click="handleReroll" :loading="executing" block>
              🔄 重新生成
            </a-button>
          </div>
        </div>

        <!-- 放大/变体后的结果展示 -->
        <div v-else-if="currentStep === 3 && finalResult" class="result-final">
          <!-- 单图结果（放大后） -->
          <div v-if="!isVariationResult" class="final-single">
            <div class="final-image-container">
              <a-image
                :src="finalResult.imageUrl"
                :preview="{ src: finalResult.rawImageUrl }"
                class="final-image"
              />
            </div>
            <div class="final-info">
              <div class="info-text">✅ 已放大为高清图片</div>
            </div>

            <!-- 保存表单 -->
            <div class="save-section">
              <a-input
                v-model:value="saveForm.patternName"
                placeholder="输入图案名称"
                class="save-input"
              />
              <a-button
                type="primary"
                @click="saveToDatabase"
                :loading="saving"
                :disabled="!saveForm.patternName"
                class="save-btn"
              >
                💾 保存图案
              </a-button>
            </div>

            <!-- 继续优化操作 -->
            <div class="continue-actions">
              <div class="action-title">继续优化</div>
              <div class="action-row">
                <a-button
                  v-for="i in 4"
                  :key="i"
                  @click="handleContinueVariation(i)"
                  :loading="executing"
                >
                  🎨 变体 {{ i }}
                </a-button>
              </div>
            </div>

            <a-button @click="backToStep2" class="back-btn"> ← 返回重新选择 </a-button>
          </div>

          <!-- 四图结果（变体重生成后） -->
          <div v-else class="final-grid">
            <div class="result-images">
              <div
                v-for="i in 4"
                :key="i"
                class="result-image-item"
                :class="{ selected: selectedImageIndex === i }"
                @click="selectImage(i)"
              >
                <a-image
                  :src="getQuadrantImage(finalResult.imageUrl, i)"
                  :preview="{ src: finalResult.rawImageUrl }"
                  :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5Ij7lm77niYc8L3RleHQ+PC9zdmc+'"
                />
                <div class="image-index">{{ i }}</div>
              </div>
            </div>

            <!-- 操作按钮区域 -->
            <div class="result-actions" v-if="selectedImageIndex">
              <div class="action-row">
                <a-button @click="handleContinueUpsample(selectedImageIndex)" :loading="executing">
                  🔍 放大图片 {{ selectedImageIndex }}
                </a-button>
                <a-button @click="handleContinueVariation(selectedImageIndex)" :loading="executing">
                  🎨 变体图片 {{ selectedImageIndex }}
                </a-button>
              </div>
              <a-button @click="handleContinueReroll" :loading="executing" block>
                🔄 重新生成
              </a-button>
            </div>

            <a-button @click="backToStep2" class="back-btn"> ← 返回重新选择 </a-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="result-empty">
          <div class="empty-preview">
            <div class="preview-grid">
              <div class="preview-item" v-for="i in 4" :key="i">
                <div class="preview-placeholder"></div>
              </div>
            </div>
          </div>
          <div class="empty-text">
            <h3>您还未生成作品？</h3>
            <p>在左侧操作面板选择提示词即可开始创作~</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { EditOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import {
  imagineAsync,
  getImagineStatus,
  executeAction as mjExecuteAction,
  savePattern,
  expandPrompt,
} from '@/api/midjourneyjiekou'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useMJTaskStore } from '@/stores/useMJTaskStore'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const mjTaskStore = useMJTaskStore()

const storageKey = 'mj_generate_task'
const pollIntervalMs = 2000

// 判断用户是否已登录
const isUserLoggedIn = computed(() => {
  return loginUserStore.loginUser && loginUserStore.loginUser.id
})

// 页面加载时获取登录用户信息
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
})

// 当前步骤：1=输入提示词，2=已生成图片
const currentStep = ref(1)

// 表单状态
const formState = reactive({
  prompt: '',
  style: undefined as string | undefined,
  season: undefined as string | undefined,
  targetAudience: undefined as string | undefined,
})

// 保存表单
const saveForm = reactive({
  patternName: '',
})

// 生成状态
const generating = ref(false)
const executing = ref(false)
const saving = ref(false)
const expanding = ref(false)

// MJ 响应数据
const mjResponse = ref<any>(null)
const finalResult = ref<any>(null)
const originalPrompt = ref('')
const currentTaskId = ref<string | null>(null)

// 选中的操作
const selectedAction = ref<string | null>(null)
// 最后执行的操作（用于显示）
const lastExecutedAction = ref<string>('')
// 判断当前结果是否为变体结果（4张图）
const isVariationResult = ref(true)

// 新布局需要的变量
const selectedImageIndex = ref<number | null>(null)
const showStyleSelector = ref(false)
const selectedStyleEngine = ref<any>(null)

// 季节选项
const seasonOptions = ['春季', '夏季', '秋季', '冬季', '四季']

// 受众选项
const audienceOptions = ['儿童', '青少年', '成人', '中老年', '通用']

// 风格引擎选项
const styleEngines = ref([
  { id: 1, name: '简约', icon: '✨' },
  { id: 2, name: '可爱', icon: '🐶' },
  { id: 3, name: '复古', icon: '🌲' },
  { id: 4, name: '卡通', icon: '🔛' },
  { id: 5, name: '抽象', icon: '🎨' },
  { id: 6, name: '民族', icon: '🎁' },
  { id: 7, name: '未来', icon: '🚀' },
  { id: 8, name: '写实', icon: '🌶' },
  { id: 9, name: '手绘', icon: '✏️' },
])

// 选择风格引擎
const selectStyleEngine = (style: any) => {
  if (selectedStyleEngine.value?.id === style.id) {
    selectedStyleEngine.value = null
    formState.style = undefined
  } else {
    selectedStyleEngine.value = style
    formState.style = style.name
  }
}

// 清除风格引擎
const clearStyleEngine = () => {
  selectedStyleEngine.value = null
  formState.style = undefined
}

// 切换季节选择
const toggleSeason = (season: string) => {
  if (formState.season === season) {
    formState.season = undefined
  } else {
    formState.season = season
  }
}

// 切换受众选择
const toggleAudience = (audience: string) => {
  if (formState.targetAudience === audience) {
    formState.targetAudience = undefined
  } else {
    formState.targetAudience = audience
  }
}

// 重置提示词
const resetPrompt = () => {
  formState.prompt = ''
}

// 选择图片
const selectImage = (index: number) => {
  if (selectedImageIndex.value === index) {
    selectedImageIndex.value = null
  } else {
    selectedImageIndex.value = index
  }
}

// 获取四象限图片（模拟2x2网格中的单张图片）
const getQuadrantImage = (imageUrl: string, index: number) => {
  // 实际项目中这里可能需要处理图片裁剪，这里直接返回原图
  return imageUrl
}

// 放大图片操作
const handleUpsample = async (index: number) => {
  selectedAction.value = `upsample${index}`
  await executeAction()
}

// 变体图片操作
const handleVariation = async (index: number) => {
  selectedAction.value = `variation${index}`
  await executeAction()
}

// 重新生成操作
const handleReroll = async () => {
  selectedAction.value = 'reroll'
  await executeAction()
}

// 在步骤3继续放大
const handleContinueUpsample = async (index: number) => {
  selectedAction.value = `upsample${index}`
  await executeContinueAction()
}

// 在步骤3继续变体
const handleContinueVariation = async (index: number) => {
  selectedAction.value = `variation${index}`
  await executeContinueAction()
}

// 在步骤3重新生成
const handleContinueReroll = async () => {
  selectedAction.value = 'reroll'
  await executeContinueAction()
}

// 返回步骤2
const backToStep2 = () => {
  currentStep.value = 2
  selectedImageIndex.value = null
  finalResult.value = null
}

// 快捷提示词
const quickPrompts = ref([
  '可爱的卡通小猫图案',
  '复古花草印花设计',
  '简约几何线条图案',
  '森系小清新植物',
  '赛博朋克科技风',
  '中国风水墨山水',
  '波普艺术风格',
  '极简主义条纹',
])

// 灵感示例
const inspirationExamples = ref([
  { icon: '🌳', name: '花草印花', desc: '适合春夏季节' },
  { icon: '🦋', name: '蝴蝶元素', desc: '轻盈浪漫风格' },
  { icon: '⭐', name: '星空图案', desc: '梦幻神秘感' },
  { icon: '🍀', name: '植物叶子', desc: '自然清新风' },
  { icon: '🎨', name: '抽象艺术', desc: '独特个性化' },
  { icon: '🔺', name: '几何形状', desc: '现代简约风' },
])

type MjTaskSnapshot = {
  taskId: string
  status: string
  result?: any
  errorMessage?: string
  updateTime?: number
  originalPrompt?: string
  formState?: {
    style?: string
    season?: string
    targetAudience?: string
  }
  notified?: boolean
}

let pollTimer: ReturnType<typeof setTimeout> | null = null

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

const stopPolling = () => {
  if (pollTimer) {
    clearTimeout(pollTimer)
    pollTimer = null
  }
}

const applySnapshot = (snapshot: MjTaskSnapshot) => {
  currentTaskId.value = snapshot.taskId
  if (snapshot.originalPrompt) {
    originalPrompt.value = snapshot.originalPrompt
    // 回填prompt到表单
    formState.prompt = snapshot.originalPrompt
  }
  if (snapshot.formState) {
    formState.style = snapshot.formState.style
    formState.season = snapshot.formState.season
    formState.targetAudience = snapshot.formState.targetAudience
    // 回填风格引擎选中状态
    if (snapshot.formState.style) {
      const matchedStyle = styleEngines.value.find((s) => s.name === snapshot.formState!.style)
      if (matchedStyle) {
        selectedStyleEngine.value = matchedStyle
      }
    }
  }
}

const pollGenerateStatus = async (taskId: string) => {
  try {
    const res = await getImagineStatus({ taskId })
    if (res.data.code !== 0) {
      throw new Error(res.data.message || '获取任务状态失败')
    }
    const taskData = res.data.data
    if (!taskData) {
      throw new Error('任务状态为空')
    }
    currentTaskId.value = taskData.taskId || taskId

    if (taskData.status === 'SUCCEEDED' && taskData.result) {
      mjResponse.value = taskData.result
      generating.value = false
      currentStep.value = 2
      stopPolling()

      const snapshot: MjTaskSnapshot = {
        taskId,
        status: taskData.status,
        result: taskData.result,
        errorMessage: taskData.errorMessage,
        updateTime: taskData.updateTime,
        originalPrompt: originalPrompt.value,
        formState: {
          style: formState.style,
          season: formState.season,
          targetAudience: formState.targetAudience,
        },
      }
      saveTaskSnapshot(snapshot)

      // 更新通知store，显示未读通知
      mjTaskStore.markSucceeded(taskId, taskData.result)

      message.success({
        content: '图案生成完成，返回智能创作页可继续变体/放大/保存',
        key: 'generating',
      })
      return
    }

    if (taskData.status === 'FAILED') {
      generating.value = false
      stopPolling()
      const snapshot: MjTaskSnapshot = {
        taskId,
        status: taskData.status,
        errorMessage: taskData.errorMessage,
        updateTime: taskData.updateTime,
        originalPrompt: originalPrompt.value,
        formState: {
          style: formState.style,
          season: formState.season,
          targetAudience: formState.targetAudience,
        },
      }
      saveTaskSnapshot(snapshot)

      // 更新通知store，显示失败通知
      mjTaskStore.markFailed(taskId, taskData.errorMessage || '图案生成失败')

      message.error({
        content: taskData.errorMessage || '图案生成失败，请稍后重试',
        key: 'generating',
      })
      return
    }

    stopPolling()
    pollTimer = setTimeout(() => {
      pollGenerateStatus(taskId)
    }, pollIntervalMs)
  } catch (error: any) {
    generating.value = false
    stopPolling()
    message.error({
      content: error.message || '获取任务状态失败',
      key: 'generating',
    })
  }
}

onMounted(() => {
  // 进入页面时标记通知为已读
  mjTaskStore.markRead()

  const snapshot = readTaskSnapshot()
  if (!snapshot) {
    return
  }
  applySnapshot(snapshot)
  if (snapshot.status === 'SUCCEEDED' && snapshot.result) {
    mjResponse.value = snapshot.result
    currentStep.value = 2
    generating.value = false
    return
  }
  if (snapshot.status === 'PENDING' || snapshot.status === 'PROCESSING') {
    generating.value = true
    pollGenerateStatus(snapshot.taskId)
  }
})

onBeforeUnmount(() => {
  stopPolling()
})

// 步骤1：生成图片
const handleGenerate = async () => {
  if (!isUserLoggedIn.value) {
    message.warning('请先登录后再使用智能创作功能')
    router.push({
      path: '/user/login',
      query: {
        redirect: '/mj/generation',
      },
    })
    return
  }

  try {
    stopPolling()
    generating.value = true
    currentStep.value = 1
    selectedImageIndex.value = null
    mjResponse.value = null

    originalPrompt.value = formState.prompt

    message.loading({
      content: '正在生成图案，请稍候...',
      key: 'generating',
      duration: 0,
    })

    const res = await imagineAsync({
      prompt: formState.prompt,
      action: 'generate',
      style: formState.style,
      season: formState.season,
      targetAudience: formState.targetAudience,
    })

    if (res.data.code === 0 && res.data.data?.taskId) {
      const taskId = res.data.data.taskId
      currentTaskId.value = taskId
      saveTaskSnapshot({
        taskId,
        status: res.data.data.status || 'PENDING',
        updateTime: res.data.data.updateTime,
        originalPrompt: originalPrompt.value,
        formState: {
          style: formState.style,
          season: formState.season,
          targetAudience: formState.targetAudience,
        },
      })

      // 创建任务通知，保存创作参数
      mjTaskStore.createTask({
        taskId,
        prompt: originalPrompt.value,
        style: formState.style,
        season: formState.season,
        targetAudience: formState.targetAudience,
      })

      pollGenerateStatus(taskId)
      return
    }
    throw new Error(res.data.message || '生成失败')
  } catch (error: any) {
    console.error('生成失败:', error)
    message.error({
      content: error.message || '生成失败',
      key: 'generating',
    })
    generating.value = false
  }
}

// 选择操作
const selectAction = (action: string) => {
  // 如果点击的是已选中的操作，则取消选择
  if (selectedAction.value === action) {
    selectedAction.value = null
  } else {
    selectedAction.value = action
  }
}

// 执行操作（从步骤2到步骤3）
const executeAction = async () => {
  if (!selectedAction.value) {
    message.warning('请先选择一个操作')
    return
  }

  try {
    executing.value = true

    message.loading({
      content: `Executing ${getActionName(selectedAction.value)}...`,
      key: 'executing',
      duration: 0,
    })

    const res = await mjExecuteAction({
      taskId: mjResponse.value.taskId,
      imageId: mjResponse.value.imageId,
      action: selectedAction.value,
    })

    if (res.data.code === 0 && res.data.data) {
      const newResult = res.data.data

      if (newResult.imageUrl) {
        newResult.imageUrl = newResult.imageUrl.replace(/:\\d+$/, '')
      }
      if (newResult.rawImageUrl) {
        newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\\d+$/, '')
      }

      finalResult.value = newResult
      lastExecutedAction.value = selectedAction.value
      isVariationResult.value =
        selectedAction.value.startsWith('variation') || selectedAction.value === 'reroll'

      if (!isVariationResult.value) {
        const timestamp = new Date().getTime()
        saveForm.patternName = `MJ-${originalPrompt.value.substring(0, 15)}-${timestamp.toString().slice(-6)}`
      }

      selectedAction.value = null
      currentStep.value = 3
      message.success({
        content: '操作完成，可保存或继续优化',
        key: 'executing',
      })
    } else {
      throw new Error(res.data.message || '操作失败')
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    message.error({
      content: error.message || '操作失败',
      key: 'executing',
    })
  } finally {
    executing.value = false
  }
}

// 在步骤3继续执行操作
const executeContinueAction = async () => {
  if (!selectedAction.value) {
    message.warning('请先选择一个操作')
    return
  }

  try {
    executing.value = true

    message.loading({
      content: `Executing ${getActionName(selectedAction.value)}...`,
      key: 'executing',
      duration: 0,
    })

    const res = await mjExecuteAction({
      taskId: finalResult.value.taskId,
      imageId: finalResult.value.imageId,
      action: selectedAction.value,
    })

    if (res.data.code === 0 && res.data.data) {
      const newResult = res.data.data

      if (newResult.imageUrl) {
        newResult.imageUrl = newResult.imageUrl.replace(/:\\d+$/, '')
      }
      if (newResult.rawImageUrl) {
        newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\\d+$/, '')
      }

      finalResult.value = newResult
      lastExecutedAction.value = selectedAction.value
      isVariationResult.value =
        selectedAction.value.startsWith('variation') || selectedAction.value === 'reroll'

      if (!isVariationResult.value) {
        const timestamp = new Date().getTime()
        saveForm.patternName = `MJ-${originalPrompt.value.substring(0, 15)}-${timestamp.toString().slice(-6)}`
      }

      selectedAction.value = null
      message.success({
        content: '操作完成，可继续优化或保存',
        key: 'executing',
      })
    } else {
      throw new Error(res.data.message || '操作失败')
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    message.error({
      content: error.message || '操作失败',
      key: 'executing',
    })
  } finally {
    executing.value = false
  }
}

// 保存到数据库
const saveToDatabase = async () => {
  if (!saveForm.patternName) {
    message.warning('请输入图案名称')
    return
  }

  try {
    saving.value = true

    message.loading({
      content: '正在保存图案...',
      key: 'saving',
      duration: 0,
    })

    // 准备保存数据，包含所有字段
    const saveData = {
      ...finalResult.value,
      patternName: saveForm.patternName,
      prompt: originalPrompt.value,
      style: formState.style,
      season: formState.season,
      targetAudience: formState.targetAudience,
    }

    // 调用后端保存接口
    const res = await savePattern(saveData)

    if (res.data.code === 0) {
      message.success({
        content: '图案保存成功！',
        key: 'saving',
      })

      // 清除任务数据，避免刷新页面后数据残留
      localStorage.removeItem(storageKey)
      mjTaskStore.clearTask()

      // 重置页面状态
      currentStep.value = 1
      mjResponse.value = null
      finalResult.value = null
      formState.prompt = ''
      formState.style = undefined
      formState.season = undefined
      formState.targetAudience = undefined
      selectedStyleEngine.value = null
      originalPrompt.value = ''
      saveForm.patternName = ''

      // 跳转到我的作品页面
      setTimeout(() => {
        router.push('/my_idea')
      }, 1000)
    } else {
      throw new Error(res.data.message || '保存失败')
    }
  } catch (error: any) {
    console.error('保存图案失败:', error)
    message.error({
      content: error.message || '保存失败，请重试',
      key: 'saving',
    })
  } finally {
    saving.value = false
  }
}

// 返回步骤1
const backToStep1 = () => {
  currentStep.value = 1
  selectedAction.value = null
}

// 获取操作名称
const getActionName = (action: string) => {
  const actionMap: Record<string, string> = {
    upsample1: '放大图片 1',
    upsample2: '放大图片 2',
    upsample3: '放大图片 3',
    upsample4: '放大图片 4',
    variation1: '变体图片 1',
    variation2: '变体图片 2',
    variation3: '变体图片 3',
    variation4: '变体图片 4',
    reroll: '重新生成',
  }
  return actionMap[action] || action
}

// AI 扩写提示词
const handleExpandPrompt = async () => {
  if (!formState.prompt || formState.prompt.trim().length < 2) {
    message.warning('请至少输入2个字')
    return
  }

  try {
    expanding.value = true

    message.loading({
      content: '正在扩写中...',
      key: 'expanding',
      duration: 0,
    })

    const res = await expandPrompt({ prompt: formState.prompt })

    if (res.data.code === 0 && res.data.data) {
      formState.prompt = res.data.data
      message.success({
        content: '扩写成功',
        key: 'expanding',
      })
    } else {
      throw new Error(res.data.message || '扩写失败')
    }
  } catch (error: any) {
    console.error('Prompt expand failed:', error)
    message.error({
      content: error.message || '扩写失败',
      key: 'expanding',
    })
  } finally {
    expanding.value = false
  }
}
</script>

<style scoped>
/* 全局基础样式 */
.mj-pattern-generation-page {
  height: 100vh;
  overflow: hidden;
  background-color: #ffffff;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 未登录提示 */
.login-alert {
  margin: 16px;
  border-radius: 8px;
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
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12px;
}

.section-header.style-header {
  color: #ff6b6b;
}

/* 提示词输入框 */
.prompt-textarea {
  background-color: #1a1a2e !important;
  border: 1px solid #3a3a5c !important;
  border-radius: 8px !important;
  color: #ffffff !important;
  font-size: 14px;
  resize: none;
}

.prompt-textarea::placeholder {
  color: #666680 !important;
}

.prompt-textarea:focus {
  border-color: #667eea !important;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) !important;
}

/* 提示词底部 */
.prompt-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.char-count {
  font-size: 12px;
  color: #666680;
}

.prompt-actions-inline {
  display: flex;
  gap: 8px;
}

.prompt-actions-inline :deep(.ant-btn) {
  color: #888;
  font-size: 13px;
}

.prompt-actions-inline :deep(.ant-btn:hover) {
  color: #fff;
}

.expand-action-btn {
  color: #667eea !important;
}

/* 风格网格 */
.style-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.style-item {
  cursor: pointer;
  text-align: center;
  padding: 12px 8px;
  background: #1a1a2e;
  border: 1px solid #3a3a5c;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.style-item:hover {
  transform: translateY(-2px);
  border-color: #667eea;
}

.style-item.active {
  background: rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.style-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.style-name {
  font-size: 12px;
  color: #888;
}

.style-item.active .style-name {
  color: #667eea;
}

/* 标签选择 */
.tags-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tag-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-label {
  font-size: 13px;
  color: #888;
  min-width: 45px;
}

.tag-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-option {
  padding: 4px 12px;
  font-size: 12px;
  color: #888;
  background: #1a1a2e;
  border: 1px solid #3a3a5c;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tag-option:hover {
  border-color: #667eea;
  color: #667eea;
}

.tag-option.active {
  background: #667eea;
  border-color: #667eea;
  color: #fff;
}

/* 底部按钮区域 */
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 25px !important;
}

.generate-btn:hover {
  background: linear-gradient(135deg, #7b8ff0 0%, #8a5db5 100%) !important;
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
  position: sticky;
  top: 0;
}

/* 加载状态 */
.result-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.loading-item {
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-text {
  text-align: center;
}

.loading-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px;
}

.loading-desc {
  font-size: 14px;
  color: #888;
  margin: 0;
}

/* 结果网格 */
.result-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.result-images {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  background: rgba(255, 255, 255, 0.05);
  padding: 24px;
  border-radius: 20px;
}

.result-image-item {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid transparent;
}

.result-image-item:hover {
  transform: scale(1.02);
}

.result-image-item.selected {
  border-color: #667eea;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.4);
}

.result-image-item :deep(.ant-image) {
  width: 200px;
  height: 200px;
}

.result-image-item :deep(.ant-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-index {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
}

/* 操作按钮 */
.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 440px;
}

.action-row {
  display: flex;
  gap: 12px;
}

.action-row :deep(.ant-btn) {
  flex: 1;
  height: 42px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.action-row :deep(.ant-btn:hover) {
  background: rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

.result-actions > :deep(.ant-btn) {
  height: 42px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.result-actions > :deep(.ant-btn:hover) {
  background: rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

/* 空状态 */
.result-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
}

.empty-preview {
  background: rgba(255, 255, 255, 0.03);
  padding: 24px;
  border-radius: 20px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.preview-item {
  width: 200px;
  height: 200px;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
  border-radius: 16px;
  border: 2px dashed rgba(255, 255, 255, 0.1);
}

.empty-text {
  text-align: center;
}

.empty-text h3 {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px;
}

.empty-text p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 放大后结果展示 */
.result-final {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  width: 100%;
  max-width: 600px;
}

.final-single {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.final-image-container {
  background: rgba(255, 255, 255, 0.05);
  padding: 24px;
  border-radius: 20px;
}

.final-image-container :deep(.ant-image) {
  max-width: 400px;
  max-height: 400px;
  border-radius: 16px;
  overflow: hidden;
}

.final-image-container :deep(.ant-image img) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.final-info {
  text-align: center;
}

.info-text {
  font-size: 16px;
  color: #52c41a;
  font-weight: 500;
}

/* 保存表单 */
.save-section {
  display: flex;
  gap: 12px;
  width: 100%;
  max-width: 400px;
}

.save-input {
  flex: 1;
  height: 42px;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 8px !important;
  color: #fff !important;
}

.save-input:focus {
  border-color: #667eea !important;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) !important;
}

.save-input::placeholder {
  color: #888 !important;
}

.save-btn {
  height: 42px;
  border-radius: 8px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  font-weight: 500;
}

.save-btn:hover {
  background: linear-gradient(135deg, #7b8ff0 0%, #8a5db5 100%) !important;
}

/* 继续优化操作 */
.continue-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 400px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.action-title {
  font-size: 14px;
  color: #888;
  text-align: center;
  margin-bottom: 4px;
}

.continue-actions .action-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.continue-actions .action-row :deep(.ant-btn) {
  flex: 0 0 auto;
  min-width: 80px;
  height: 36px;
  font-size: 13px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.continue-actions .action-row :deep(.ant-btn:hover) {
  background: rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

.back-btn {
  height: 36px;
  border-radius: 8px !important;
  background: transparent !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #888 !important;
}

.back-btn:hover {
  border-color: #667eea !important;
  color: #667eea !important;
}

.final-grid {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .left-panel {
    width: 360px;
    min-width: 360px;
  }

  .result-image-item :deep(.ant-image) {
    width: 180px;
    height: 180px;
  }

  .preview-item {
    width: 180px;
    height: 180px;
  }

  .loading-item {
    width: 180px;
    height: 180px;
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
    position: relative;
    overflow: visible;
  }
}

@media (max-width: 576px) {
  .style-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .result-image-item :deep(.ant-image) {
    width: 140px;
    height: 140px;
  }

  .preview-item {
    width: 140px;
    height: 140px;
  }

  .loading-item {
    width: 140px;
    height: 140px;
  }
}
</style>

