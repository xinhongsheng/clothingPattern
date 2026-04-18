<template>
  <div class="bailian-pattern-generation-page">
    <!-- 背景层：霓虹渐变 + 网格 + 噪点 + 浮动光团 -->
    <div class="bg-layer" aria-hidden="true">
      <span class="blob b1" />
      <span class="blob b2" />
      <span class="blob b3" />
      <span class="grid" />
      <span class="noise" />
    </div>



    <!-- 主内容区域 - 左右分栏布局 -->
    <div class="main-layout">
      <!-- 左侧操作面板 -->
      <div class="left-panel">
        <!-- 顶部标题/状态 -->
        <div class="left-hero">

          <div class="hero-title">
            智能创作工作台
            <span class="shine" />
          </div>
          <div class="hero-subtitle">像调色一样精确，像灵感一样自由。</div>

          <!-- 生成模式切换 -->
          <div class="generation-mode-switch">
            <div
              class="mode-btn"
              :class="{ active: generationMode === 'text' }"
              @click="generationMode = 'text'"
            >
              <span class="mode-icon">✍️</span>
              <span class="mode-text">文字生成</span>
            </div>
            <div
              class="mode-btn"
              :class="{ active: generationMode === 'image' }"
              @click="generationMode = 'image'"
            >
              <span class="mode-icon">🖼️</span>
              <span class="mode-text">图片生成</span>
            </div>
          </div>
        </div>

        <!-- 以图生图区域 - 仅在图片生成模式下显示 -->
        <div v-if="generationMode === 'image'" class="panel-section img2img-section">
          <!-- 流程指引 -->
          <div class="img2img-flow">
            <div class="flow-step" :class="{ active: !referenceImageUrl, done: referenceImageUrl }">
              <span class="step-num">1</span>
              <span class="step-text">上传参考图</span>
            </div>
            <div class="flow-arrow">→</div>
            <div class="flow-step" :class="{ active: referenceImageUrl && !analysisResult, done: analysisResult }">
              <span class="step-num">2</span>
              <span class="step-text">AI分析</span>
            </div>
            <div class="flow-arrow">→</div>
            <div class="flow-step" :class="{ active: analysisResult }">
              <span class="step-num">3</span>
              <span class="step-text">生成图案</span>
            </div>
          </div>

          <div class="img2img-content">
            <!-- 未上传状态 -->
            <a-upload
              v-if="!referenceImageUrl"
              :show-upload-list="false"
              :before-upload="handleReferenceImageUpload"
              accept="image/*"
              class="img2img-upload"
            >
              <div class="upload-area">
                <div class="upload-icon-wrapper">
                  <PictureOutlined class="upload-main-icon" />
                  <span class="upload-plus">+</span>
                </div>
                <div class="upload-text">点击或拖拽上传参考图片</div>
                <div class="upload-hint">AI 将自动提取图像元素，生成相似风格图案</div>
                <div class="upload-formats">
                  <span class="format-tag">JPG</span>
                  <span class="format-tag">PNG</span>
                  <span class="format-tag">≤ 10MB</span>
                </div>
              </div>
            </a-upload>

            <!-- 已上传状态 -->
            <div v-else class="img2img-preview">
              <div class="preview-card">
                <div class="preview-image-wrapper">
                  <a-image
                    :src="referenceImageUrl"
                    :preview="{ src: referenceImageUrl }"
                    class="preview-image"
                  />
                  <div class="preview-badge">参考图</div>
                  <a-button
                    type="text"
                    size="small"
                    class="preview-delete-btn"
                    @click.stop="clearReferenceImage"
                  >
                    <DeleteOutlined />
                  </a-button>
                </div>
                
                <div class="preview-actions">
                  <a-button
                    type="primary"
                    :loading="analyzing"
                    :disabled="analyzing"
                    @click="handleAnalyzeImage"
                    class="analyze-btn"
                    block
                  >
                    <template #icon><ScanOutlined v-if="!analyzing" /></template>
                    {{ analyzing ? 'AI 分析中...' : '✨ AI 智能分析' }}
                  </a-button>
                </div>
              </div>

              <!-- AI分析结果展示 -->
              <div v-if="analysisResult" class="analysis-result">
                <div class="analysis-header">
                  <span class="analysis-icon">✨</span>
                  <span>AI 提取的图案元素</span>
                  <span class="analysis-badge">已完成</span>
                </div>
                <div class="analysis-content">{{ analysisResult }}</div>
                <a-button
                  type="primary"
                  size="small"
                  @click="applyAnalysisToPrompt"
                  class="apply-btn"
                >
                  应用到描述框 →
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入描述区域 -->
        <div class="panel-section">
          <div class="section-header">
            请输入关键词描述
            <span class="mini-tip">（建议：主体 + 材质/光泽 + 构图 + 风格）</span>
          </div>

          <a-textarea
            v-model:value="formState.prompt"
            placeholder="例：赛博霓虹小熊，镭射反光，T恤胸口居中，潮流街头涂鸦质感，无缝可平铺"
            :rows="3"
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

        <!-- 风格引擎区域 - 仅文字生成模式显示 -->
        <div v-if="generationMode === 'text'" class="panel-section">
          <div class="section-header style-header">
            风格引擎
            <span v-if="selectedStyleEngine" class="style-pill">
              已选：{{ selectedStyleEngine.name }}
              <span class="x" @click.stop="clearStyleEngine">×</span>
            </span>
          </div>

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
              <div class="style-glow" />
            </div>
          </div>
        </div>

        <!-- 选择标签区域 - 仅文字生成模式显示 -->
        <div v-if="generationMode === 'text'" class="panel-section">
          <div class="section-header">选择标签</div>

          <div class="tags-group">
            <div class="tag-row">
              <span class="tag-label">季节</span>
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
              <span class="tag-label">受众</span>
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
            class="generate-btn neon-primary"
          >
            <span class="btn-text">{{ generating ? '创作中…' : '智能生成' }}</span>
          </a-button>

          <div class="footer-hint">
            <span class="pulse" />
            生成后在右侧选择 1-4 号图可进行「放大 / 变体 / 重生成」
          </div>
        </div>
      </div>

      <!-- 右侧结果展示区 -->
      <div class="right-panel">
        <!-- 生成中状态：骨架屏 + shimmer + 阶段节奏 + 文案轮播 -->
        <div v-if="generating" class="result-loading">
          <div class="loading-head">
            <div class="loading-badge">
              <span class="live-dot" />
              创作进行时
            </div>
            <div class="loading-title">
              {{ loadingCopy?.main || '' }}<span class="dots">{{ dots }}</span>
            </div>
            <div class="loading-sub">
              {{ loadingCopy?.sub || '' }}
            </div>
          </div>

          <div class="phase-bar">
            <div
              v-for="(p, idx) in phaseList"
              :key="p"
              class="phase-item"
              :class="{ active: idx === phaseIndex }"
            >
              <span class="phase-icon">✦</span>
              <span class="phase-text">{{ p }}</span>
            </div>
          </div>

          <!-- 2x2 骨架网格 -->
          <div class="skeleton-grid shimmer">
            <div class="sk-tile" v-for="i in 4" :key="i">
              <div class="sk-img" />
              <div class="sk-meta">
                <div class="sk-line w70" />
                <div class="sk-line w55" />
              </div>
            </div>
          </div>

          <div class="loading-footer">
            <div class="fake-progress">
              <span class="bar" />
            </div>
            <div class="mini-note">
              小提示：加入「材质/光泽/边缘干净/无缝平铺」关键词，会更像专业服装印花。
            </div>
          </div>
        </div>

        <!-- 已生成结果 - 2x2 网格 -->
        <div v-else-if="currentStep === 2 && mjResponse" class="result-grid">
          <div class="result-topline">
            <div class="result-title">
              <span class="spark">⚡</span> 选择你想继续精修的方案
            </div>
            <div class="result-sub">
              点击任意格选中（1-4），再执行「放大 / 变体」；或直接「重新生成」。
            </div>
          </div>

          <div class="result-images frame-grid">
            <div
              v-for="i in 4"
              :key="i"
              class="result-image-item"
              :class="{ selected: selectedImageIndex === i }"
              @click="selectImage(i)"
            >
              <div class="thumb-frame">
                <a-image
                  :src="getQuadrantImage(mjResponse.imageUrl, i, mjResponse.subImageUrls)"
                  :preview="{ src: mjResponse.subImageUrls?.[i - 1] ?? mjResponse.rawImageUrl }"
                  :fallback="fallbackSvgSmall"
                />
                <span class="glow-ring" />
              </div>
              <div class="image-index">{{ i }}</div>
            </div>
          </div>

          <!-- 操作按钮区域 -->
          <div class="result-actions" v-if="selectedImageIndex">
            <div class="action-row">
              <a-button class="ghost-btn" @click="handleUpsample(selectedImageIndex)" :loading="executing">
                🔍 放大 {{ selectedImageIndex }}
              </a-button>
              <a-button class="ghost-btn" @click="handleVariation(selectedImageIndex)" :loading="executing">
                🎨 变体 {{ selectedImageIndex }}
              </a-button>
            </div>

            <a-button class="ghost-btn" @click="handleReroll" :loading="executing" block>
              🔄 重新生成
            </a-button>
          </div>
        </div>

        <!-- 放大/变体后的结果展示 -->
        <div v-else-if="currentStep === 3 && finalResult" class="result-final">
          <!-- 单图结果（放大后） -->
          <div v-if="!isVariationResult" class="final-single">
            <div class="final-image-container frame-single">
              <span class="frame-glow" />
              <a-image
                :src="finalResult.imageUrl"
                :preview="{ src: finalResult.rawImageUrl }"
                class="final-image"
              />
            </div>

            <div class="final-info">
              <div class="info-text">✅ 已放大为高清图片</div>
              <div class="info-sub">建议：命名更具体（系列/季节/风格/编号）便于后续管理</div>
            </div>

            <!-- 保存表单 -->
            <div class="save-section">
              <a-input
                v-model:value="saveForm.patternName"
                placeholder="输入图案名称（必填）"
                class="save-input"
              />
              <a-button
                type="primary"
                @click="saveToDatabase"
                :loading="saving"
                :disabled="!saveForm.patternName"
                class="save-btn neon-primary"
              >
                💾 保存
              </a-button>
            </div>

            <!-- 继续优化操作 -->
            <div class="continue-actions">
              <div class="action-title">继续优化</div>
              <div class="action-row">
                <a-button
                  v-for="i in 4"
                  :key="i"
                  class="ghost-btn"
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
            <div class="result-topline">
              <div class="result-title">
                <span class="spark">🎛</span> 变体/重生成结果
              </div>
              <div class="result-sub">
                继续选择 1-4 号图，执行「放大 / 变体」或「重新生成」。
              </div>
            </div>

            <div class="result-images frame-grid">
              <div
                v-for="i in 4"
                :key="i"
                class="result-image-item"
                :class="{ selected: selectedImageIndex === i }"
                @click="selectImage(i)"
              >
                <div class="thumb-frame">
                  <a-image
                    :src="getQuadrantImage(finalResult.imageUrl, i, finalResult.subImageUrls)"
                    :preview="{ src: finalResult.subImageUrls?.[i - 1] ?? finalResult.rawImageUrl }"
                    :fallback="fallbackSvgSmall"
                  />
                  <span class="glow-ring" />
                </div>
                <div class="image-index">{{ i }}</div>
              </div>
            </div>

            <!-- 操作按钮区域 -->
            <div class="result-actions" v-if="selectedImageIndex">
              <div class="action-row">
                <a-button class="ghost-btn" @click="handleContinueUpsample(selectedImageIndex)" :loading="executing">
                  🔍 放大 {{ selectedImageIndex }}
                </a-button>
                <a-button class="ghost-btn" @click="handleContinueVariation(selectedImageIndex)" :loading="executing">
                  🎨 变体 {{ selectedImageIndex }}
                </a-button>
              </div>
              <a-button class="ghost-btn" @click="handleContinueReroll" :loading="executing" block>
                🔄 重新生成
              </a-button>
            </div>

            <a-button @click="backToStep2" class="back-btn"> ← 返回重新选择 </a-button>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="result-empty">
          <div class="empty-preview frame-grid">
            <div class="preview-item" v-for="i in 4" :key="i">
              <div class="preview-placeholder shimmer-soft"></div>
              <div class="preview-index">{{ i }}</div>
            </div>
          </div>

          <div class="empty-text">
            <h3>还没有作品</h3>
            <p>在左侧输入提示词并选择风格/标签，然后点击「智能生成」开始创作。</p>
            <div class="empty-tags">
              <span class="t">霓虹渐变</span>
              <span class="t">街头涂鸦</span>
              <span class="t">金属光泽</span>
              <span class="t">无缝平铺</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { message } from 'ant-design-vue'
import { EditOutlined, ReloadOutlined, PictureOutlined, DeleteOutlined, ScanOutlined } from '@ant-design/icons-vue'
import {
  imagineAsync,
  getImagineStatus,
  executeAction as mjExecuteAction,
  savePattern,
  expandPrompt,
} from '@/api/bailianImageApi'
import { analyzeImage, uploadReferenceImage } from '@/api/aiController'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useMJTaskStore } from '@/stores/useMJTaskStore'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const mjTaskStore = useMJTaskStore()

const storageKey = 'mj_generate_task'
const pollIntervalMs = 2000

// 小型 fallback（更契合暗色背景）
const fallbackSvgSmall =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iIzEwMTQyNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOEE5M0I4IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+UGxhY2Vob2xkZXI8L3RleHQ+PC9zdmc+'

// 判断用户是否已登录
const isUserLoggedIn = computed(() => {
  return loginUserStore.loginUser && loginUserStore.loginUser.id
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

// ====== 生成模式 ======
const generationMode = ref<'text' | 'image'>('text')  // 'text'=文字生成, 'image'=图片生成

// ====== 以图生图相关状态 ======
const referenceImageUrl = ref('')  // 参考图片URL
const analyzing = ref(false)        // AI分析中状态
const analysisResult = ref('')      // AI分析结果

// 上传参考图片
const handleReferenceImageUpload = async (file: File) => {
  if (file.size > 10 * 1024 * 1024) {
    message.error('图片大小不能超过 10MB')
    return false
  }

  try {
    message.loading({ content: '正在上传图片...', key: 'upload', duration: 0 })

    // 适配OpenAPI生成的接口签名：body为第一个参数，file为第二个参数
    const res = await uploadReferenceImage({}, file)
    const url = (res as any)?.data?.data

    if (!url) {
      throw new Error('上传失败，未获取到图片地址')
    }

    referenceImageUrl.value = url
    analysisResult.value = ''  // 清空之前的分析结果
    message.success({ content: '图片上传成功', key: 'upload' })
  } catch (e: any) {
    console.error('上传失败:', e)
    message.error({ content: e?.message || '上传失败，请稍后重试', key: 'upload' })
  }

  return false  // 阻止默认上传行为
}

// 清除参考图片
const clearReferenceImage = () => {
  referenceImageUrl.value = ''
  analysisResult.value = ''
}

// AI分析图片
const handleAnalyzeImage = async () => {
  if (!referenceImageUrl.value) {
    message.warning('请先上传参考图片')
    return
  }

  if (!isUserLoggedIn.value) {
    message.warning('请先登录后再使用AI分析功能')
    router.push({ path: '/user/login', query: { redirect: '/mj/generation' } })
    return
  }

  try {
    analyzing.value = true
    message.loading({ content: 'AI正在分析图片元素...', key: 'analyzing', duration: 0 })

    // 调用专用的图片分析接口
    const res = await analyzeImage({ imageUrl: referenceImageUrl.value })

    if (res.data.code === 0 && res.data.data) {
      analysisResult.value = res.data.data
      message.success({ content: 'AI分析完成', key: 'analyzing' })
    } else {
      throw new Error(res.data.message || 'AI分析失败')
    }
  } catch (error: any) {
    console.error('AI分析失败:', error)
    message.error({ content: error.message || 'AI分析失败，请稍后重试', key: 'analyzing' })
  } finally {
    analyzing.value = false
  }
}

// 应用分析结果到描述框
const applyAnalysisToPrompt = () => {
  if (!analysisResult.value) {
    message.warning('没有可应用的分析结果')
    return
  }

  // 如果当前有描述，追加；否则直接设置
  if (formState.prompt) {
    formState.prompt = `${formState.prompt}\n\n${analysisResult.value}`
  } else {
    formState.prompt = analysisResult.value
  }

  message.success('已应用到描述框')
}

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
  formState.season = formState.season === season ? undefined : season
}

// 切换受众选择
const toggleAudience = (audience: string) => {
  formState.targetAudience = formState.targetAudience === audience ? undefined : audience
}

// 重置提示词
const resetPrompt = () => {
  formState.prompt = ''
}

// 选择图片
const selectImage = (index: number) => {
  selectedImageIndex.value = selectedImageIndex.value === index ? null : index
}

// 获取四象限图片：优先使用 subImageUrls 中对应索引的图片
const getQuadrantImage = (imageUrl: string, index?: number, subImageUrls?: string[]) => {
  if (subImageUrls && subImageUrls.length > 0 && index != null) {
    return subImageUrls[index - 1] ?? imageUrl
  }
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

// ====== 生成中：情绪化节奏（阶段 + 文案轮播 + 跳动省略号） ======
const phaseList = ['解构关键词', '构建构图', '渲染纹理', '输出四宫格']
const phaseIndex = ref(0)
const dots = ref('')
const loadingCopyIndex = ref(0)

const loadingCopies = [
  { main: 'AI 正在把你的灵感翻译成「可穿的视觉」', sub: '建议：加上材质（丝绸/牛仔/金属光泽）会更像服装印花。' },
  { main: '正在生成纹理层与光影层', sub: '想更潮：加“霓虹渐变 / 镭射反光 / 街头涂鸦”。' },
  { main: '正在优化构图与边缘干净度', sub: '想做平铺：写“seamless repeat / tileable”。' },
  { main: '最后一笔：提升细节与对比', sub: '如果太杂：可用“clean, minimal, no blur”类负面词。' },
]
const loadingCopy = computed(() => loadingCopies[loadingCopyIndex.value] || loadingCopies[0])

let dotsTimer: ReturnType<typeof setInterval> | null = null
let phaseTimer: ReturnType<typeof setInterval> | null = null
let copyTimer: ReturnType<typeof setInterval> | null = null

const startLoadingMood = () => {
  stopLoadingMood()

  dotsTimer = setInterval(() => {
    dots.value = dots.value.length >= 3 ? '' : dots.value + '.'
  }, 420)

  phaseTimer = setInterval(() => {
    phaseIndex.value = (phaseIndex.value + 1) % phaseList.length
  }, 1500)

  copyTimer = setInterval(() => {
    loadingCopyIndex.value = (loadingCopyIndex.value + 1) % loadingCopies.length
  }, 2400)
}

const stopLoadingMood = () => {
  if (dotsTimer) clearInterval(dotsTimer)
  if (phaseTimer) clearInterval(phaseTimer)
  if (copyTimer) clearInterval(copyTimer)
  dotsTimer = phaseTimer = copyTimer = null
  dots.value = ''
  phaseIndex.value = 0
  loadingCopyIndex.value = 0
}

watch(generating, (v) => {
  if (v) startLoadingMood()
  else stopLoadingMood()
})

// ====== 任务快照/轮询逻辑（原样保留） ======
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
  // 以图生图相关
  referenceImageUrl?: string
  generationMode?: 'text' | 'image'
  notified?: boolean
}

let pollTimer: ReturnType<typeof setTimeout> | null = null

const readTaskSnapshot = (): MjTaskSnapshot | null => {
  const raw = localStorage.getItem(storageKey)
  if (!raw) return null
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

const syncStyleEngineSelection = (style?: string) => {
  if (style) {
    const matchedStyle = styleEngines.value.find((s) => s.name === style)
    selectedStyleEngine.value = matchedStyle || null
    return
  }
  selectedStyleEngine.value = null
}

const applyCreationParams = (params: {
  prompt?: string
  style?: string
  season?: string
  targetAudience?: string
}) => {
  if (params.prompt !== undefined) {
    originalPrompt.value = params.prompt
    formState.prompt = params.prompt
  }
  if (params.style !== undefined) formState.style = params.style
  if (params.season !== undefined) formState.season = params.season
  if (params.targetAudience !== undefined) formState.targetAudience = params.targetAudience
  syncStyleEngineSelection(formState.style)
}

const applySnapshot = (snapshot: MjTaskSnapshot) => {
  currentTaskId.value = snapshot.taskId
  applyCreationParams({
    prompt: snapshot.originalPrompt,
    style: snapshot.formState?.style,
    season: snapshot.formState?.season,
    targetAudience: snapshot.formState?.targetAudience,
  })
  // 恢复参考图和生成模式
  if (snapshot.referenceImageUrl) {
    referenceImageUrl.value = snapshot.referenceImageUrl
  }
  if (snapshot.generationMode) {
    generationMode.value = snapshot.generationMode
  }
}

const pollGenerateStatus = async (taskId: string) => {
  try {
    const res = await getImagineStatus({ taskId })
    if (res.data.code !== 0) throw new Error(res.data.message || '获取任务状态失败')

    const taskData = res.data.data
    if (!taskData) throw new Error('任务状态为空')
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
        // 保存参考图和生成模式
        referenceImageUrl: referenceImageUrl.value || undefined,
        generationMode: generationMode.value,
      }
      saveTaskSnapshot(snapshot)

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
        // 保存参考图和生成模式
        referenceImageUrl: referenceImageUrl.value || undefined,
        generationMode: generationMode.value,
      }
      saveTaskSnapshot(snapshot)

      mjTaskStore.markFailed(taskId, taskData.errorMessage || '图案生成失败')

      message.error({
        content: taskData.errorMessage || '图案生成失败，请稍后重试',
        key: 'generating',
      })
      return
    }

    stopPolling()
    pollTimer = setTimeout(() => pollGenerateStatus(taskId), pollIntervalMs)
  } catch (error: any) {
    generating.value = false
    stopPolling()
    message.error({
      content: error.message || '获取任务状态失败',
      key: 'generating',
    })
  }
}

// 页面初始化：合并为一次 onMounted（更干净）
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
  mjTaskStore.markRead()

  const snapshot = readTaskSnapshot()
  const creationParams = mjTaskStore.getCreationParams()

  if (snapshot) applySnapshot(snapshot)
  if (creationParams) {
    applyCreationParams({
      prompt: snapshot?.originalPrompt ?? creationParams.prompt,
      style: snapshot?.formState?.style ?? creationParams.style,
      season: snapshot?.formState?.season ?? creationParams.season,
      targetAudience: snapshot?.formState?.targetAudience ?? creationParams.targetAudience,
    })
  }

  if (!snapshot) return

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
  stopLoadingMood()
})

// 步骤1：生成图片
const handleGenerate = async () => {
  if (!isUserLoggedIn.value) {
    message.warning('请先登录后再使用智能创作功能')
    router.push({
      path: '/user/login',
      query: { redirect: '/mj/generation' },
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
      content: '正在生成图案，请稍候查看...',
      key: 'generating',
      duration: 2,
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
        // 保存参考图和生成模式
        referenceImageUrl: referenceImageUrl.value || undefined,
        generationMode: generationMode.value,
      })

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
    message.error({
      content: error.message || '生成失败',
      key: 'generating',
    })
    generating.value = false
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
      content: `正在执行 ${getActionName(selectedAction.value)}...`,
      key: 'executing',
      duration: 2,
    })

    const res = await mjExecuteAction({
      imageId: mjResponse.value.imageId,
      action: selectedAction.value,
      sourceResult: mjResponse.value,  // 传递完整的原始结果
    })

    if (res.data.code === 0 && res.data.data) {
      const newResult = res.data.data

      if (newResult.imageUrl) newResult.imageUrl = newResult.imageUrl.replace(/:\d+$/, '')
      if (newResult.rawImageUrl) newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\d+$/, '')

      finalResult.value = newResult
      lastExecutedAction.value = selectedAction.value

      isVariationResult.value =
        selectedAction.value.startsWith('variation') || selectedAction.value === 'reroll'

      if (!isVariationResult.value) {
        const timestamp = new Date().getTime()
        saveForm.patternName = `${originalPrompt.value.substring(0, 15)}-${timestamp.toString().slice(-6)}`
      }

      selectedAction.value = null
      currentStep.value = 3
      message.destroy('executing')
    } else {
      throw new Error(res.data.message || '操作失败')
    }
  } catch (error: any) {
    message.error({
      content: error.message || '操作失败',
      key: 'executing',
      duration: 3,
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
      content: `正在执行 ${getActionName(selectedAction.value)}...`,
      key: 'executing',
      duration: 2,
    })

    const res = await mjExecuteAction({
      imageId: finalResult.value.imageId,
      action: selectedAction.value,
      sourceResult: finalResult.value,  // 传递完整的变体结果
    })

    if (res.data.code === 0 && res.data.data) {
      const newResult = res.data.data

      if (newResult.imageUrl) newResult.imageUrl = newResult.imageUrl.replace(/:\d+$/, '')
      if (newResult.rawImageUrl) newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\d+$/, '')

      finalResult.value = newResult
      lastExecutedAction.value = selectedAction.value

      isVariationResult.value =
        selectedAction.value.startsWith('variation') || selectedAction.value === 'reroll'

      if (!isVariationResult.value) {
        const timestamp = new Date().getTime()
        saveForm.patternName = `${originalPrompt.value.substring(0, 15)}-${timestamp.toString().slice(-6)}`
      }

      selectedAction.value = null
      message.destroy('executing')
    } else {
      throw new Error(res.data.message || '操作失败')
    }
  } catch (error: any) {
    message.error({
      content: error.message || '操作失败',
      key: 'executing',
      duration: 3,
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

    const saveData = {
      ...finalResult.value,
      patternName: saveForm.patternName,
      prompt: originalPrompt.value,
      style: formState.style,
      season: formState.season,
      targetAudience: formState.targetAudience,
      referenceImageUrl: referenceImageUrl.value || undefined,
    }

    const res = await savePattern(saveData)

    if (res.data.code === 0) {
      message.success({
        content: '图案保存成功！',
        key: 'saving',
      })

      localStorage.removeItem(storageKey)
      mjTaskStore.clearTask()

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

      setTimeout(() => router.push('/my_idea'), 1000)
    } else {
      throw new Error(res.data.message || '保存失败')
    }
  } catch (error: any) {
    message.error({
      content: error.message || '保存失败，请重试',
      key: 'saving',
    })
  } finally {
    saving.value = false
  }
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
    message.error({
      content: error.message || '扩写失败',
      key: 'expanding',
    })
  } finally {
    expanding.value = false
  }
}
</script>

<style scoped lang="scss">
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700&family=Newsreader:wght@400;600;700&display=swap');

/* ========= 灵感社区一致的暖调主题变量 ========= */
.bailian-pattern-generation-page {
  --ink: #1f1a15;
  --muted: #7a6f66;
  --muted-2: #a39a92;
  --accent: #d45b2d;
  --accent-2: #2a9d8f;
  --surface: #fffdf8;
  --surface-2: #f6efe6;
  --stroke: rgba(31, 26, 21, 0.08);
  --shadow: 0 22px 60px rgba(31, 26, 21, 0.12);
  --r12: 12px;
  --r16: 16px;
  --r18: 18px;
  --ease: cubic-bezier(0.22, 1, 0.36, 1);
  --font-body: 'Manrope', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-display: 'Newsreader', 'Noto Serif SC', 'Songti SC', serif;

  height: 100vh;
  overflow: hidden;
  position: relative;
  font-family: var(--font-body);
  color: var(--ink);
  background:
    radial-gradient(900px 420px at 8% -10%, rgba(240, 181, 128, 0.4), transparent 65%),
    radial-gradient(800px 380px at 92% 5%, rgba(122, 210, 196, 0.35), transparent 60%),
    linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
}

/* ========= 背景层 ========= */
.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;

  .blob {
    position: absolute;
    width: 560px;
    height: 560px;
    filter: blur(44px);
    opacity: 0.65;
    border-radius: 999px;
    mix-blend-mode: screen;
    animation: floaty 8s ease-in-out infinite;
  }
  .b1 {
    left: -140px;
    top: -160px;
    background: radial-gradient(circle at 30% 30%, rgba(212, 91, 45, 0.45), transparent 65%);
  }
  .b2 {
    right: -170px;
    top: -110px;
    background: radial-gradient(circle at 40% 40%, rgba(42, 157, 143, 0.4), transparent 65%);
    animation-delay: -2s;
  }
  .b3 {
    left: 18%;
    bottom: -300px;
    background: radial-gradient(circle at 40% 40%, rgba(240, 181, 128, 0.35), transparent 65%);
    animation-delay: -4s;
  }

  .grid {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(31, 26, 21, 0.06) 1px, transparent 1px),
      linear-gradient(90deg, rgba(31, 26, 21, 0.06) 1px, transparent 1px);
    background-size: 22px 22px;
    mask-image: radial-gradient(circle at 50% 10%, rgba(0, 0, 0, 0.7), rgba(0, 0, 0, 0));
    opacity: 0.35;
  }

  .noise {
    position: absolute;
    inset: 0;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='.85' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='180' height='180' filter='url(%23n)' opacity='.22'/%3E%3C/svg%3E");
    mix-blend-mode: multiply;
    opacity: 0.12;
  }
}

@keyframes floaty {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(12px, 20px, 0) scale(1.04); }
}

@keyframes panelEnter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========= 未登录提示 ========= */
.login-alert {
  margin: 16px;
  border-radius: 10px;
  position: relative;
  z-index: 2;
}

/* ========= 主布局 ========= */
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* ========= 左侧面板 ========= */
.left-panel {
  width: 360px;
  min-width: 360px;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;

  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(255, 249, 242, 0.92));
  border-right: 1px solid var(--stroke);
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);
  animation: panelEnter 0.7s var(--ease) both;

  /* 滚动条美化 */
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: #f4efe6; }
  &::-webkit-scrollbar-thumb { background: rgba(31, 26, 21, 0.14); border-radius: 6px; }
  &::-webkit-scrollbar-thumb:hover { background: rgba(31, 26, 21, 0.22); }
}

.left-hero {
  padding: 14px 18px 10px;
  border-bottom: 1px solid var(--stroke);
  position: relative;

  .hero-badge {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 6px 10px;
    border-radius: 999px;
    border: 1px solid var(--stroke);
    background: rgba(255, 255, 255, 0.82);
    color: var(--accent-2);
    font-size: 12px;

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 999px;
      background: var(--accent);
      box-shadow: 0 0 14px rgba(212, 91, 45, 0.35);
    }
    .chip {
      padding: 2px 8px;
      border-radius: 999px;
      background: #ffffff;
      border: 1px solid var(--stroke);
      color: var(--muted);
    }
  }

  .hero-title {
    position: relative;
    margin-top: 10px;
    font-size: 18px;
    font-weight: 700;
    font-family: var(--font-display);
    letter-spacing: 0.2px;
    color: var(--ink);

    .shine {
      position: absolute;
      inset: -8px -10px;
      background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.16) 50%, transparent 100%);
      transform: skewX(-14deg);
      animation: shine 2.8s ease-in-out infinite;
      pointer-events: none;
      opacity: 0.65;
    }
  }

  .hero-subtitle {
    margin-top: 4px;
    font-size: 11px;
    color: var(--muted);
  }
}

/* ========= 生成模式切换按钮 ========= */
.generation-mode-switch {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding: 4px;
  background: rgba(31, 26, 21, 0.04);
  border-radius: 12px;
  border: 1px solid var(--stroke);

  .mode-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    font-size: 13px;
    font-weight: 600;
    color: var(--muted);
    background: transparent;
    transition: all 0.25s var(--ease);
    border: 1px solid transparent;

    .mode-icon {
      font-size: 15px;
      transition: transform 0.25s var(--ease);
    }

    .mode-text {
      transition: color 0.25s var(--ease);
    }

    &:hover {
      color: var(--ink);
      background: rgba(255, 255, 255, 0.5);
    }

    &.active {
      color: var(--ink);
      background: #ffffff;
      border-color: var(--stroke);
      box-shadow: 0 2px 8px rgba(31, 26, 21, 0.08);

      .mode-icon {
        transform: scale(1.1);
      }
    }

    &.active:first-child {
      border-color: rgba(42, 157, 143, 0.3);

      .mode-text {
        color: var(--accent-2);
      }
    }

    &.active:last-child {
      border-color: rgba(212, 91, 45, 0.3);

      .mode-text {
        color: var(--accent);
      }
    }
  }
}

@keyframes shine {
  0% { transform: translateX(-45%) skewX(-14deg); opacity: 0; }
  35% { opacity: 0.8; }
  100% { transform: translateX(45%) skewX(-14deg); opacity: 0; }
}

.panel-section {
  padding: 12px 18px;
  border-bottom: 1px solid var(--stroke);
}

.section-header {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;

  .mini-tip {
    font-size: 11px;
    font-weight: 600;
    color: var(--muted-2);
  }
}

.section-header.style-header {
  color: var(--accent);
  .style-pill {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 4px 10px;
    border-radius: 999px;
    border: 1px solid var(--stroke);
    background: rgba(255, 255, 255, 0.82);
    color: var(--ink);
    font-size: 11px;
    .x {
      cursor: pointer;
      width: 16px;
      height: 16px;
      border-radius: 999px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.9);
      border: 1px solid var(--stroke);
    }
    .x:hover {
      border-color: rgba(212, 91, 45, 0.35);
      color: var(--accent);
    }
  }
}

/* 输入框皮肤 */
.prompt-textarea {
  background-color: #ffffff !important;
  border: 1px solid rgba(31, 26, 21, 0.12) !important;
  border-radius: 14px !important;
  color: var(--ink) !important;
  font-size: 13px;
  resize: none;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5) !important;
}

.prompt-textarea::placeholder { color: var(--muted-2) !important; }

.prompt-textarea:focus {
  border-color: rgba(42, 157, 143, 0.45) !important;
  box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.16) !important;
}

.prompt-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

/* ========= 以图生图区域样式 ========= */
.img2img-section {
  padding-top: 8px !important;
}

/* 流程指引 */
.img2img-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 0;
  margin-bottom: 12px;
  background: linear-gradient(135deg, rgba(42, 157, 143, 0.06), rgba(240, 181, 128, 0.06));
  border-radius: 12px;
  border: 1px solid rgba(42, 157, 143, 0.1);
}

.flow-step {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--stroke);
  transition: all 0.3s ease;

  .step-num {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: var(--muted-2);
    color: #fff;
    font-size: 11px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .step-text {
    font-size: 12px;
    font-weight: 600;
    color: var(--muted);
  }

  &.active {
    background: rgba(42, 157, 143, 0.12);
    border-color: rgba(42, 157, 143, 0.3);
    
    .step-num {
      background: var(--accent-2);
      box-shadow: 0 0 12px rgba(42, 157, 143, 0.4);
    }
    .step-text {
      color: var(--accent-2);
    }
  }

  &.done {
    .step-num {
      background: var(--accent-2);
    }
    .step-text {
      color: var(--ink);
    }
  }
}

.flow-arrow {
  color: var(--muted-2);
  font-size: 14px;
}

.img2img-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.img2img-upload {
  width: 100%;

  .upload-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 32px 20px;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(246, 239, 230, 0.6) 100%);
    border: 2px dashed rgba(42, 157, 143, 0.25);
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at 50% 0%, rgba(42, 157, 143, 0.08), transparent 70%);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover {
      border-color: var(--accent-2);
      transform: translateY(-3px);
      box-shadow: 0 12px 32px rgba(42, 157, 143, 0.15);

      &::before {
        opacity: 1;
      }
    }
  }

  .upload-icon-wrapper {
    position: relative;
    width: 64px;
    height: 64px;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(42, 157, 143, 0.1), rgba(240, 181, 128, 0.1));
    border-radius: 20px;
    border: 1px solid rgba(42, 157, 143, 0.15);
    transition: all 0.3s ease;

    .upload-main-icon {
      font-size: 28px;
      color: var(--accent-2);
    }

    .upload-plus {
      position: absolute;
      bottom: -4px;
      right: -4px;
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--accent), #f08a5d);
      color: #fff;
      font-size: 14px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(212, 91, 45, 0.3);
    }
  }

  .upload-area:hover .upload-icon-wrapper {
    transform: scale(1.05);
    box-shadow: 0 8px 24px rgba(42, 157, 143, 0.2);
  }

  .upload-text {
    font-size: 15px;
    font-weight: 700;
    color: var(--ink);
    margin-bottom: 6px;
  }

  .upload-hint {
    font-size: 12px;
    color: var(--muted);
    margin-bottom: 12px;
    text-align: center;
    max-width: 240px;
    line-height: 1.5;
  }

  .upload-formats {
    display: flex;
    gap: 8px;
  }

  .format-tag {
    padding: 4px 10px;
    font-size: 10px;
    font-weight: 600;
    color: var(--muted);
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid var(--stroke);
    border-radius: 12px;
  }
}

.img2img-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(246, 239, 230, 0.8));
  border-radius: 18px;
  padding: 12px;
  border: 1px solid var(--stroke);
  box-shadow: 0 8px 24px rgba(31, 26, 21, 0.06);
}

.preview-image-wrapper {
  position: relative;
  border-radius: 14px;
  overflow: hidden;
  background: #f6efe6;

  :deep(.ant-image) {
    width: 100%;
    display: block;
  }

  :deep(.ant-image-img) {
    width: 100%;
    max-height: 160px;
    object-fit: cover;
    border-radius: 14px;
  }
}

.preview-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--accent-2), #3eb8a5);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(42, 157, 143, 0.3);
}

.preview-delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 10px !important;
  background: rgba(0, 0, 0, 0.5) !important;
  color: #fff !important;
  border: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.25s ease;

  &:hover {
    background: rgba(212, 91, 45, 0.9) !important;
    transform: scale(1.1);
  }
}

.preview-image-wrapper:hover .preview-delete-btn {
  opacity: 1;
}

.preview-actions {
  margin-top: 12px;
}

.analyze-btn {
  height: 44px !important;
  border-radius: 14px !important;
  font-weight: 700 !important;
  font-size: 14px !important;
  background: linear-gradient(135deg, var(--accent-2), #3eb8a5) !important;
  border: none !important;
  box-shadow: 0 8px 24px rgba(42, 157, 143, 0.25);
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(42, 157, 143, 0.35);
  }

  &:disabled {
    background: linear-gradient(135deg, #b5d9d3, #c5e0dc) !important;
    box-shadow: none;
  }
}

.analysis-result {
  background: linear-gradient(135deg, rgba(42, 157, 143, 0.08), rgba(255, 255, 255, 0.9));
  border: 1px solid rgba(42, 157, 143, 0.2);
  border-radius: 16px;
  padding: 16px;
  animation: fadeInUp 0.4s ease-out;

  .analysis-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    font-weight: 700;
    color: var(--accent-2);
    margin-bottom: 12px;

    .analysis-icon {
      font-size: 18px;
    }

    .analysis-badge {
      margin-left: auto;
      padding: 3px 10px;
      font-size: 10px;
      font-weight: 600;
      color: #fff;
      background: linear-gradient(135deg, var(--accent-2), #3eb8a5);
      border-radius: 10px;
    }
  }

  .analysis-content {
    font-size: 12px;
    color: var(--ink);
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 100px;
    overflow-y: auto;
    padding: 12px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    border: 1px solid var(--stroke);
    margin-bottom: 12px;
  }

  .apply-btn {
    width: 100%;
    height: 38px !important;
    border-radius: 12px !important;
    font-weight: 600 !important;
    background: linear-gradient(135deg, var(--accent), #f08a5d) !important;
    border: none !important;
    box-shadow: 0 6px 18px rgba(212, 91, 45, 0.25);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 28px rgba(212, 91, 45, 0.35);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.char-count { font-size: 12px; color: var(--muted-2); }

.prompt-actions-inline { display: flex; gap: 10px; }

.prompt-actions-inline :deep(.ant-btn) {
  color: var(--muted);
  font-size: 13px;
  border-radius: 10px;
}
.prompt-actions-inline :deep(.ant-btn:hover) { color: var(--accent); }

.expand-action-btn { color: var(--accent-2) !important; }

/* 风格网格 */
.style-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.style-item {
  cursor: pointer;
  text-align: center;
  padding: 10px 8px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--stroke);
  border-radius: 14px;
  transition: transform .18s ease, border-color .18s ease, box-shadow .18s ease;
  position: relative;
  overflow: hidden;

  .style-glow {
    position: absolute;
    inset: -20px;
    background: radial-gradient(circle at 30% 20%, rgba(212, 91, 45, 0.16), transparent 52%),
      radial-gradient(circle at 70% 70%, rgba(42, 157, 143, 0.14), transparent 56%);
    filter: blur(18px);
    opacity: 0;
    transition: opacity .18s ease;
    pointer-events: none;
  }
}

.style-item:hover {
  transform: translateY(-2px);
  border-color: rgba(212, 91, 45, 0.3);
  box-shadow: 0 18px 40px rgba(31, 26, 21, 0.12);
  .style-glow { opacity: 1; }
}

.style-item.active {
  background: linear-gradient(135deg, rgba(212, 91, 45, 0.14), rgba(42, 157, 143, 0.12));
  border-color: rgba(212, 91, 45, 0.45);
  box-shadow: 0 22px 60px rgba(31, 26, 21, 0.16);
  .style-glow { opacity: 1; }
}

.style-icon { font-size: 20px; margin-bottom: 4px; }
.style-name { font-size: 11px; color: var(--muted); font-weight: 700; }

/* 标签选择 */
.tags-group { display: flex; flex-direction: column; gap: 8px; }
.tag-row { display: flex; align-items: flex-start; gap: 8px; }
.tag-label {
  font-size: 12px;
  color: var(--muted);
  min-width: 44px;
  margin-top: 2px;
  font-weight: 700;
}
.tag-options { display: flex; flex-wrap: wrap; gap: 6px; }

.tag-option {
  padding: 5px 10px;
  font-size: 11px;
  color: var(--muted);
  background: #fff7ee;
  border: 1px solid rgba(31, 26, 21, 0.12);
  border-radius: 999px;
  cursor: pointer;
  transition: transform .18s ease, border-color .18s ease, background .18s ease;
}

.tag-option:hover {
  transform: translateY(-1px);
  border-color: rgba(212, 91, 45, 0.35);
}

.tag-option.active {
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
  border-color: transparent;
  color: #ffffff;
}

/* 底部按钮 */
.panel-footer {
  padding: 12px 18px 14px;
  margin-top: auto;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.86), rgba(246, 239, 230, 0.96));
  border-top: 1px solid var(--stroke);
}

.generate-btn {
  height: 44px;
  font-size: 15px;
  font-weight: 900;
  border: none !important;
  border-radius: 999px !important;
}

.neon-primary {
  background: linear-gradient(90deg, var(--accent), #f08a5d) !important;
  box-shadow: 0 12px 22px rgba(212, 91, 45, 0.25);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.22), transparent);
    transform: translateX(-60%);
    animation: btnShine 2.2s ease-in-out infinite;
  }
}
@keyframes btnShine {
  0% { transform: translateX(-60%); opacity: 0; }
  30% { opacity: 0.9; }
  100% { transform: translateX(60%); opacity: 0; }
}

.footer-hint {
  margin-top: 8px;
  font-size: 11px;
  color: var(--muted);
  display: flex;
  align-items: center;
  gap: 8px;

  .pulse {
    width: 8px;
    height: 8px;
    border-radius: 999px;
    background: var(--accent-2);
    box-shadow: 0 0 12px rgba(42, 157, 143, 0.4);
    animation: pulse 1.2s ease-in-out infinite;
  }
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.35); opacity: 1; }
}

/* ========= 右侧结果区 ========= */
.right-panel {
  flex: 1;
  height: 100vh;
  overflow-y: auto;
  position: sticky;
  top: 0;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 34px;
  padding-bottom: 60px;

  background: radial-gradient(900px 650px at 60% 25%, rgba(240, 181, 128, 0.22), transparent 60%),
    radial-gradient(900px 650px at 30% 60%, rgba(122, 210, 196, 0.2), transparent 60%),
    rgba(255, 255, 255, 0.8);
  border-left: 1px solid var(--stroke);
  backdrop-filter: blur(10px);
  animation: panelEnter 0.7s var(--ease) both;
  animation-delay: 0.08s;

  /* 滚动条美化 */
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: rgba(31, 26, 21, 0.14); border-radius: 6px; }
  &::-webkit-scrollbar-thumb:hover { background: rgba(31, 26, 21, 0.22); }
}

/* ========= 生成中状态（情绪化 loading） ========= */
.result-loading {
  width: min(720px, 100%);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loading-head {
  text-align: center;
  display: grid;
  gap: 8px;
}

.loading-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--stroke);
  background: rgba(255, 255, 255, 0.82);
  width: fit-content;
  margin: 0 auto;
  font-size: 12px;
  color: var(--ink);

  .live-dot {
    width: 9px;
    height: 9px;
    border-radius: 999px;
    background: var(--accent-2);
    box-shadow: 0 0 12px rgba(42, 157, 143, 0.4);
    animation: pulse 1.2s ease-in-out infinite;
  }
}

.loading-title {
  font-size: 18px;
  font-weight: 1000;
  font-family: var(--font-display);
  letter-spacing: 0.2px;
  color: var(--ink);
  .dots {
    display: inline-block;
    width: 18px;
    text-align: left;
  }
}

.loading-sub {
  font-size: 12px;
  color: var(--muted);
}

/* 阶段条 */
.phase-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;

  .phase-item {
    border-radius: 14px;
    padding: 10px 10px;
    border: 1px solid var(--stroke);
    background: rgba(255, 255, 255, 0.9);
    color: var(--muted);
    display: flex;
    align-items: center;
    gap: 8px;
    transition: transform .18s ease, border-color .18s ease, background .18s ease;

    .phase-text { font-size: 12px; font-weight: 900; white-space: nowrap; }
    .phase-icon { opacity: 0.9; }
  }

  .phase-item.active {
    color: var(--ink);
    border-color: rgba(42, 157, 143, 0.45);
    background: rgba(42, 157, 143, 0.12);
    transform: translateY(-1px);
    box-shadow: 0 18px 55px rgba(42, 157, 143, 0.16);
  }

  @media (max-width: 560px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* shimmer 骨架 */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  border-radius: 22px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--stroke);
  box-shadow: var(--shadow);
}

.sk-tile {
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid var(--stroke);
  background: rgba(255, 255, 255, 0.6);
}

.sk-img {
  height: 220px;
  background: linear-gradient(135deg, rgba(212, 91, 45, 0.12), rgba(42, 157, 143, 0.08));
}

.sk-meta { padding: 12px; }
.sk-line {
  height: 10px;
  border-radius: 999px;
  background: #f4efe6;
  border: 1px solid rgba(31, 26, 21, 0.06);
  margin-top: 10px;
}
.sk-line.w70 { width: 70%; }
.sk-line.w55 { width: 55%; }

.shimmer {
  position: relative;
  overflow: hidden;
}
.shimmer::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.12) 50%, transparent 100%);
  transform: translateX(-60%);
  animation: shimmer 1.6s ease-in-out infinite;
}
@keyframes shimmer {
  0% { transform: translateX(-60%); opacity: 0; }
  40% { opacity: 1; }
  100% { transform: translateX(60%); opacity: 0; }
}

.loading-footer {
  display: grid;
  gap: 10px;
}

.fake-progress {
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--stroke);
  overflow: hidden;

  .bar {
    display: block;
    height: 100%;
    width: 38%;
    background: linear-gradient(90deg, var(--accent), #f08a5d);
    border-radius: 999px;
    animation: progressSlide 1.4s ease-in-out infinite;
  }
}
@keyframes progressSlide {
  0% { transform: translateX(-60%); opacity: 0.6; }
  50% { opacity: 1; }
  100% { transform: translateX(220%); opacity: 0.6; }
}

.mini-note {
  font-size: 12px;
  color: var(--muted);
  text-align: center;
}

/* ========= 结果区（2x2） ========= */
.result-grid, .final-grid {
  width: min(720px, 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.result-topline {
  width: 100%;
  text-align: center;
  display: grid;
  gap: 8px;
}
.result-title {
  font-size: 16px;
  font-weight: 1000;
  font-family: var(--font-display);
  letter-spacing: 0.2px;
  color: var(--ink);
  .spark { margin-right: 6px; filter: drop-shadow(0 0 10px rgba(212, 91, 45, 0.35)); }
}
.result-sub {
  font-size: 12px;
  color: var(--muted);
}

.frame-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  background: rgba(255, 255, 255, 0.92);
  padding: 18px;
  border-radius: 22px;
  border: 1px solid var(--stroke);
  box-shadow: var(--shadow);
}

.result-image-item {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: transform .18s ease, border-color .18s ease, box-shadow .18s ease;
  border: 2px solid rgba(31, 26, 21, 0.08);
}

.result-image-item:hover { transform: translateY(-2px); }

.result-image-item.selected {
  border-color: rgba(42, 157, 143, 0.6);
  box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.16), 0 24px 80px rgba(31, 26, 21, 0.12);
}

.thumb-frame {
  position: relative;
  padding: 10px;
  background: rgba(246, 239, 230, 0.7);
}

.result-image-item :deep(.ant-image) { width: 210px; height: 210px; }
.result-image-item :deep(.ant-image img) { width: 100%; height: 100%; object-fit: cover; border-radius: 14px; }

.glow-ring {
  position: absolute;
  inset: 10px;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px rgba(31, 26, 21, 0.08);
  pointer-events: none;
}

.image-index {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 26px;
  height: 26px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--stroke);
}

/* 操作按钮 */
.result-actions {
  width: 100%;
  max-width: 520px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-row { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; }
.action-row .ghost-btn { flex: 1; min-width: 120px; white-space: nowrap; }

.ghost-btn {
  height: 42px;
  border-radius: 14px !important;
  background: rgba(255, 255, 255, 0.92) !important;
  border-color: var(--stroke) !important;
  color: var(--ink) !important;
}
.ghost-btn:hover {
  border-color: rgba(42, 157, 143, 0.45) !important;
  background: rgba(42, 157, 143, 0.12) !important;
}

/* ========= 放大后单图 ========= */
.result-final {
  width: min(720px, 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.final-single {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.frame-single {
  width: 100%;
  border-radius: 22px;
  padding: 18px;
  border: 1px solid var(--stroke);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow);
  position: relative;
  overflow: hidden;

  .frame-glow {
    position: absolute;
    inset: -2px;
    background: radial-gradient(circle at 30% 30%, rgba(212, 91, 45, 0.22), transparent 55%),
      radial-gradient(circle at 70% 70%, rgba(42, 157, 143, 0.2), transparent 55%);
    filter: blur(18px);
    opacity: 0.35;
    pointer-events: none;
  }

  :deep(.ant-image) {
    width: 100%;
    display: block;
  }

  :deep(.ant-image-img) {
    width: 100%;
    border-radius: 16px;
    object-fit: contain;
    background: #f6efe6;
  }
}

.final-info {
  text-align: center;
  display: grid;
  gap: 6px;
}
.info-text { font-size: 14px; font-weight: 1000; color: var(--accent-2); }
.info-sub { font-size: 12px; color: var(--muted); }

/* 保存表单 */
.save-section {
  display: flex;
  gap: 12px;
  width: 100%;
  max-width: 520px;
}

.save-input {
  flex: 1;
  height: 42px;
  background: #ffffff !important;
  border: 1px solid rgba(31, 26, 21, 0.12) !important;
  border-radius: 14px !important;
  color: var(--ink) !important;
}
.save-input::placeholder { color: var(--muted-2) !important; }
.save-input:focus {
  border-color: rgba(42, 157, 143, 0.45) !important;
  box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.16) !important;
}

.save-btn {
  height: 42px;
  border-radius: 14px !important;
  font-weight: 1000;
}

/* 继续优化 */
.continue-actions {
  width: 100%;
  max-width: 520px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid var(--stroke);
  background: rgba(255, 255, 255, 0.92);
  display: grid;
  gap: 10px;
}
.action-title {
  font-size: 12px;
  color: var(--muted);
  text-align: center;
  font-weight: 900;
}
.continue-actions .action-row { flex-wrap: wrap; justify-content: center; }
.continue-actions .ghost-btn { min-width: 110px; }

/* 返回按钮 */
.back-btn {
  height: 38px;
  border-radius: 14px !important;
  background: transparent !important;
  border: 1px solid var(--stroke) !important;
  color: var(--muted) !important;
}
.back-btn:hover {
  border-color: rgba(42, 157, 143, 0.45) !important;
  color: var(--accent-2) !important;
}

/* ========= 空状态 ========= */
.result-empty {
  width: min(720px, 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
}

.empty-preview {
  width: 100%;
  box-shadow: var(--shadow);
}

.preview-item {
  width: 210px;
  height: 210px;
  border-radius: 18px;
  position: relative;
  overflow: hidden;
  border: 2px solid rgba(31, 26, 21, 0.08);
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(246, 239, 230, 0.9) 100%);
  border-radius: 18px;
}

.preview-index {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 26px;
  height: 26px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--ink);
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--stroke);
}

.empty-text {
  text-align: center;
  h3 {
    font-size: 18px;
    font-weight: 1000;
    margin: 0 0 8px;
    color: var(--ink);
    font-family: var(--font-display);
    letter-spacing: 0.2px;
  }
  p {
    font-size: 12px;
    margin: 0;
    color: var(--muted);
  }
}

.empty-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;

  .t {
    padding: 6px 10px;
    border-radius: 999px;
    border: 1px solid var(--stroke);
    background: #f4efe6;
    font-size: 12px;
    color: var(--muted);
  }
}

/* 空状态轻 shimmer */
.shimmer-soft {
  position: relative;
  overflow: hidden;
}
.shimmer-soft::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.10) 50%, transparent 100%);
  transform: translateX(-60%);
  animation: shimmerSoft 2.2s ease-in-out infinite;
}
@keyframes shimmerSoft {
  0% { transform: translateX(-60%); opacity: 0; }
  40% { opacity: 1; }
  100% { transform: translateX(60%); opacity: 0; }
}

/* ========= 响应式 ========= */

/* 超大屏幕 (≥1600px) */
@media (min-width: 1600px) {
  .left-panel { width: 420px; min-width: 420px; }
  .result-loading,
  .result-grid,
  .final-grid,
  .result-final,
  .result-empty { width: min(900px, 100%); }
  .result-image-item :deep(.ant-image),
  .preview-item { width: 260px; height: 260px; }
  .frame-grid { padding: 22px; gap: 18px; }
  .sk-img { height: 260px; }
}

/* 大屏幕 (1201px - 1599px) - 默认样式 */

/* 中大屏幕 (993px - 1200px) */
@media (max-width: 1200px) {
  .left-panel { width: 320px; min-width: 320px; }
  .result-image-item :deep(.ant-image),
  .preview-item { width: 190px; height: 190px; }
  .sk-img { height: 200px; }
  .hero-title { font-size: 16px; }
  .panel-section { padding: 10px 14px; }
  .section-header { font-size: 12px; }
  .style-icon { font-size: 18px; }
  .style-name { font-size: 10px; }
  .tag-option { padding: 4px 8px; font-size: 10px; }
}

/* 平板端 (769px - 992px) */
@media (max-width: 992px) {
  .bailian-pattern-generation-page {
    height: auto;
    min-height: 100vh;
    overflow: visible;
  }
  .main-layout {
    flex-direction: column;
    height: auto;
    overflow: visible;
  }
  .left-panel {
    width: 100%;
    min-width: 100%;
    height: auto;
    overflow: visible;
    border-right: none;
    border-bottom: 1px solid var(--stroke);
  }
  .right-panel {
    min-height: 50vh;
    height: auto;
    position: relative;
    overflow: visible;
    border-left: none;
    border-top: 1px solid var(--stroke);
    padding: 24px 20px;
  }
  .left-hero { padding: 16px 20px 12px; }
  .hero-title { font-size: 20px; }
  .panel-section { padding: 14px 20px; }
  .style-grid { grid-template-columns: repeat(5, 1fr); gap: 10px; }
  .style-item { padding: 12px 10px; }
  .style-icon { font-size: 22px; }
  .style-name { font-size: 12px; }
  .tag-options { gap: 8px; }
  .tag-option { padding: 6px 12px; font-size: 12px; }
  .panel-footer { padding: 16px 20px; }
  .generate-btn { height: 48px; font-size: 16px; }
  .result-loading,
  .result-grid,
  .final-grid,
  .result-final,
  .result-empty { width: min(680px, 100%); }
  .result-image-item :deep(.ant-image),
  .preview-item { width: 200px; height: 200px; }
  .sk-img { height: 200px; }
  .phase-bar { grid-template-columns: repeat(4, 1fr); gap: 8px; }
  .phase-item { padding: 8px; }
  .phase-item .phase-text { font-size: 11px; }
}

/* 小平板/大手机 (577px - 768px) */
@media (max-width: 768px) {
  .left-hero { padding: 14px 16px 10px; }
  .hero-title { font-size: 18px; }
  .hero-subtitle { font-size: 11px; }
  .panel-section { padding: 12px 16px; }
  .section-header { font-size: 12px; gap: 6px; }
  .section-header .mini-tip { font-size: 10px; }
  .style-grid { grid-template-columns: repeat(5, 1fr); gap: 8px; }
  .style-item { padding: 10px 6px; border-radius: 12px; }
  .style-icon { font-size: 20px; margin-bottom: 2px; }
  .style-name { font-size: 11px; }
  .tags-group { gap: 6px; }
  .tag-row { gap: 6px; flex-wrap: wrap; }
  .tag-label { min-width: 40px; font-size: 11px; }
  .tag-options { gap: 6px; }
  .tag-option { padding: 5px 10px; font-size: 11px; }
  .prompt-textarea { font-size: 13px; }
  .panel-footer { padding: 14px 16px; }
  .footer-hint { font-size: 10px; }
  .right-panel { padding: 20px 16px; }
  .result-loading,
  .result-grid,
  .final-grid,
  .result-final,
  .result-empty { width: 100%; }
  .result-image-item :deep(.ant-image),
  .preview-item { width: 180px; height: 180px; }
  .frame-grid { padding: 14px; gap: 12px; border-radius: 18px; }
  .result-image-item { border-radius: 14px; }
  .thumb-frame { padding: 8px; }
  .sk-img { height: 180px; }
  .phase-bar { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .phase-item { padding: 10px 8px; border-radius: 12px; }
  .phase-item .phase-text { font-size: 11px; }
  .skeleton-grid { padding: 14px; gap: 12px; border-radius: 18px; }
  .loading-title { font-size: 16px; }
  .loading-sub { font-size: 11px; }
  .result-title { font-size: 14px; }
  .result-sub { font-size: 11px; }
  .result-actions { max-width: 100%; }
  .action-row { gap: 10px; }
  .action-row .ghost-btn { min-width: 100px; }
  .ghost-btn { height: 40px; border-radius: 12px !important; font-size: 13px; }
  .save-section { flex-direction: column; gap: 10px; max-width: 100%; }
  .save-input { height: 44px; }
  .save-btn { height: 44px; width: 100%; }
  .continue-actions { max-width: 100%; padding: 12px; border-radius: 14px; }
  .continue-actions .ghost-btn { min-width: 80px; font-size: 12px; }
  .back-btn { height: 36px; font-size: 13px; }
  .empty-text h3 { font-size: 16px; }
  .empty-text p { font-size: 11px; }
  .empty-tags { gap: 6px; }
  .empty-tags .t { padding: 5px 8px; font-size: 11px; }
}

/* 手机端 (≤576px) */
@media (max-width: 576px) {
  .bailian-pattern-generation-page {
    height: auto;
    min-height: 100vh;
    overflow-x: hidden;
    overflow-y: auto;
  }
  .bg-layer .blob {
    width: 300px;
    height: 300px;
    filter: blur(30px);
  }
  .left-hero { padding: 12px 14px 8px; }
  .hero-title {
    font-size: 16px;
    letter-spacing: 0;
  }
  .hero-subtitle { font-size: 10px; margin-top: 2px; }
  .panel-section { padding: 10px 14px; }
  .section-header {
    font-size: 12px;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  .section-header .mini-tip { font-size: 10px; }
  .section-header.style-header { flex-direction: row; flex-wrap: wrap; }
  .section-header .style-pill { font-size: 10px; padding: 3px 8px; }
  .prompt-textarea {
    font-size: 13px;
    border-radius: 12px !important;
  }
  .prompt-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  .prompt-actions-inline { gap: 8px; }
  .prompt-actions-inline :deep(.ant-btn) { font-size: 12px; }
  .style-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }
  .style-item {
    padding: 8px 6px;
    border-radius: 10px;
  }
  .style-icon { font-size: 18px; margin-bottom: 2px; }
  .style-name { font-size: 10px; }
  .tags-group { gap: 8px; }
  .tag-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
  .tag-label {
    min-width: auto;
    font-size: 11px;
  }
  .tag-options { gap: 6px; }
  .tag-option {
    padding: 5px 10px;
    font-size: 11px;
  }
  .panel-footer { padding: 12px 14px 16px; }
  .generate-btn { height: 46px; font-size: 15px; }
  .footer-hint {
    font-size: 10px;
    text-align: center;
    justify-content: center;
  }
  .right-panel {
    padding: 16px 12px;
    min-height: 40vh;
  }
  .result-loading,
  .result-grid,
  .final-grid,
  .result-final,
  .result-empty { width: 100%; }
  .loading-head { gap: 6px; }
  .loading-badge { padding: 6px 10px; font-size: 11px; }
  .loading-title { font-size: 14px; }
  .loading-sub { font-size: 11px; }
  .phase-bar {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }
  .phase-item {
    padding: 8px 6px;
    border-radius: 10px;
    gap: 6px;
  }
  .phase-item .phase-text { font-size: 10px; }
  .phase-item .phase-icon { font-size: 10px; }
  .skeleton-grid {
    padding: 10px;
    gap: 8px;
    border-radius: 14px;
  }
  .sk-tile { border-radius: 12px; }
  .sk-img { height: 120px; }
  .sk-meta { padding: 8px; }
  .sk-line { height: 8px; margin-top: 6px; }
  .loading-footer { gap: 8px; }
  .fake-progress { height: 8px; }
  .mini-note { font-size: 11px; }
  .frame-grid {
    padding: 10px;
    gap: 8px;
    border-radius: 14px;
  }
  .result-image-item { border-radius: 12px; }
  .result-image-item :deep(.ant-image) {
    width: calc(50vw - 32px);
    height: calc(50vw - 32px);
    max-width: 160px;
    max-height: 160px;
  }
  .result-image-item :deep(.ant-image img) { border-radius: 10px; }
  .thumb-frame { padding: 6px; }
  .glow-ring { inset: 6px; border-radius: 12px; }
  .image-index {
    width: 22px;
    height: 22px;
    font-size: 10px;
    bottom: 8px;
    right: 8px;
  }
  .preview-item {
    width: calc(50vw - 32px);
    height: calc(50vw - 32px);
    max-width: 160px;
    max-height: 160px;
    border-radius: 12px;
  }
  .preview-placeholder { border-radius: 12px; }
  .preview-index {
    width: 22px;
    height: 22px;
    font-size: 10px;
    bottom: 8px;
    right: 8px;
  }
  .result-topline { gap: 4px; }
  .result-title { font-size: 13px; }
  .result-sub { font-size: 10px; }
  .result-actions { gap: 10px; }
  .action-row { gap: 8px; }
  .action-row .ghost-btn {
    min-width: 90px;
    flex: 1;
    font-size: 12px;
  }
  .ghost-btn {
    height: 38px;
    border-radius: 10px !important;
    font-size: 12px;
  }
  .frame-single {
    padding: 12px;
    border-radius: 16px;
  }
  .frame-single :deep(.ant-image-img) { border-radius: 12px; }
  .final-info { gap: 4px; }
  .info-text { font-size: 13px; }
  .info-sub { font-size: 11px; }
  .save-section {
    flex-direction: column;
    gap: 8px;
  }
  .save-input {
    height: 42px;
    border-radius: 12px !important;
    font-size: 13px;
  }
  .save-btn {
    height: 42px;
    width: 100%;
    border-radius: 12px !important;
    font-size: 14px;
  }
  .continue-actions {
    padding: 10px;
    border-radius: 12px;
    gap: 8px;
  }
  .action-title { font-size: 11px; }
  .continue-actions .action-row { gap: 6px; }
  .continue-actions .ghost-btn {
    min-width: 70px;
    font-size: 11px;
    height: 36px;
    padding: 0 8px;
  }
  .back-btn {
    height: 34px;
    font-size: 12px;
    border-radius: 10px !important;
  }
  .empty-text h3 { font-size: 15px; margin-bottom: 6px; }
  .empty-text p { font-size: 11px; }
  .empty-tags { gap: 6px; margin-top: 8px; }
  .empty-tags .t { padding: 4px 8px; font-size: 10px; }
}

/* 超小屏幕手机 (≤375px) */
@media (max-width: 375px) {
  .left-hero { padding: 10px 12px 6px; }
  .hero-title { font-size: 15px; }
  .panel-section { padding: 8px 12px; }
  .section-header { font-size: 11px; }
  .style-grid { gap: 4px; }
  .style-item { padding: 6px 4px; border-radius: 8px; }
  .style-icon { font-size: 16px; }
  .style-name { font-size: 9px; }
  .tag-option { padding: 4px 8px; font-size: 10px; }
  .prompt-textarea { font-size: 12px; }
  .panel-footer { padding: 10px 12px 14px; }
  .generate-btn { height: 44px; font-size: 14px; }
  .footer-hint { font-size: 9px; }
  .right-panel { padding: 12px 10px; }
  .loading-title { font-size: 13px; }
  .phase-item .phase-text { font-size: 9px; }
  .sk-img { height: 100px; }
  .frame-grid { padding: 8px; gap: 6px; border-radius: 12px; }
  .result-image-item :deep(.ant-image) {
    width: calc(50vw - 26px);
    height: calc(50vw - 26px);
    max-width: 140px;
    max-height: 140px;
  }
  .preview-item {
    width: calc(50vw - 26px);
    height: calc(50vw - 26px);
    max-width: 140px;
    max-height: 140px;
  }
  .result-title { font-size: 12px; }
  .ghost-btn { height: 36px; font-size: 11px; }
  .continue-actions .ghost-btn { min-width: 60px; font-size: 10px; height: 34px; }
}
</style>
