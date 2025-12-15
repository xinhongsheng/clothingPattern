<template>
  <div class="mj-pattern-generation-page">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1>✨ AI 服装图案生成</h1>
      <p class="subtitle">使用 Ai 技术，让创意无限可能</p>
    </div>

    <!-- 未登录提示 -->
    <a-alert
      v-if="!isUserLoggedIn"
      message="需要登录才能使用智能创作功能"
      description="请先登录您的账号，然后即可开始创作属于您的独特图案🎨"
      type="warning"
      show-icon
      closable
      class="login-alert"
    >
      <template #icon>
        <span style="font-size: 20px">🔒</span>
      </template>
    </a-alert>

    <!-- 主内容区域 -->
    <a-card class="generation-card" :bordered="false">
      <!-- 步骤1:输入提示词并生成 -->
      <div v-if="currentStep === 1" class="step-content">
        <h2 class="section-title">📝 步骤 1:描述你的创意</h2>

        <!-- 功能特性展示 -->
        <div class="features-showcase">
          <div class="feature-item">
            <div class="feature-icon">🎨</div>
            <div class="feature-text">
              <strong>专业级AI生成</strong>
              <span>AI 技术支持</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⚡</div>
            <div class="feature-text">
              <strong>快速出图</strong>
              <span>1-2分钟生成4张候选</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🔄</div>
            <div class="feature-text">
              <strong>无限迭代</strong>
              <span>放大、变体、重新生成</span>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">💎</div>
            <div class="feature-text">
              <strong>高清输出</strong>
              <span>专业服装图案品质</span>
            </div>
          </div>
        </div>

        <a-form :model="formState" layout="vertical" @finish="handleGenerate">
          <!-- 提示词输入 -->
          <a-form-item
            label="图案描述"
            name="prompt"
            :rules="[{ required: true, message: '请输入图案描述' }]"
          >
            <a-textarea
              v-model:value="formState.prompt"
              placeholder="例如：可爱的小猫图案、复古花卉图案、简约几何线条等&#10;&#10;支持中文输入，系统会自动翻译并优化为专业的服装图案描述"
              :rows="4"
              size="large"
              :maxlength="200"
              show-count
            />
            <div class="prompt-actions">
              <div class="tip-text">💡 提示:支持中文输入,AI 会自动翻译并添加服装专业术语</div>
              <a-button
                type="link"
                size="small"
                :loading="expanding"
                :disabled="expanding || !formState.prompt || formState.prompt.length < 2"
                @click="handleExpandPrompt"
                class="expand-btn"
              >
                <template #icon>
                  <EditOutlined v-if="!expanding" />
                </template>
                {{ expanding ? '扩写中...' : '✨ AI扩写' }}
              </a-button>
            </div>
          </a-form-item>

          <!-- 快捷提示词标签 -->
          <div class="quick-prompts">
            <div class="prompt-label">💫 快速灵感:</div>
            <div class="prompt-tags">
              <a-tag
                v-for="tag in quickPrompts"
                :key="tag"
                color="blue"
                class="prompt-tag"
                @click="formState.prompt = tag"
              >
                {{ tag }}
              </a-tag>
            </div>
          </div>

          <!-- 风格、季节、受众选择 - 横向布局 -->
          <a-row :gutter="[16, 24]">
            <!-- 风格选择 -->
            <a-col :xs="24" :sm="24" :md="8">
              <a-form-item label="图案风格">
                <a-select
                  v-model:value="formState.style"
                  placeholder="选择风格（可选）"
                  size="large"
                  allow-clear
                >
                  <a-select-option value="简约">简约</a-select-option>
                  <a-select-option value="可爱">可爱</a-select-option>
                  <a-select-option value="复古">复古</a-select-option>
                  <a-select-option value="卡通">卡通</a-select-option>
                  <a-select-option value="抽象">抽象</a-select-option>
                  <a-select-option value="民族">民族</a-select-option>
                  <a-select-option value="未来">未来</a-select-option>
                  <a-select-option value="写实">写实</a-select-option>
                  <a-select-option value="手绘">手绘</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <!-- 适用季节 -->
            <a-col :xs="24" :sm="24" :md="8">
              <a-form-item label="适用季节">
                <a-select
                  v-model:value="formState.season"
                  placeholder="选择季节（可选）"
                  size="large"
                  allow-clear
                >
                  <a-select-option value="春季">春季</a-select-option>
                  <a-select-option value="夏季">夏季</a-select-option>
                  <a-select-option value="秋季">秋季</a-select-option>
                  <a-select-option value="冬季">冬季</a-select-option>
                  <a-select-option value="四季">四季通用</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <!-- 目标受众 -->
            <a-col :xs="24" :sm="24" :md="8">
              <a-form-item label="目标受众">
                <a-select
                  v-model:value="formState.targetAudience"
                  placeholder="选择受众（可选）"
                  size="large"
                  allow-clear
                >
                  <a-select-option value="儿童">儿童</a-select-option>
                  <a-select-option value="青少年">青少年</a-select-option>
                  <a-select-option value="成人">成人</a-select-option>
                  <a-select-option value="中老年">中老年</a-select-option>
                  <a-select-option value="通用">通用</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <!-- 提交按钮 -->
          <a-form-item>
            <a-space direction="vertical" style="width: 100%" :size="12">
              <a-button
                type="primary"
                html-type="submit"
                size="large"
                block
                :loading="generating"
                :disabled="generating"
              >
                <template #icon>
                  <ThunderboltOutlined v-if="!generating" />
                </template>
                {{ generating ? '正在生成中,请耐心等待(约1-2分钟)...' : '开始生成图案' }}
              </a-button>
              
              <!-- 智能推荐按钮 -->
              <a-button
                size="large"
                block
                :loading="recommending"
                :disabled="recommending || !formState.prompt"
                @click="fetchRecommendations"
                class="recommend-btn"
              >
                <template #icon>
                  <SearchOutlined v-if="!recommending" />
                </template>
                {{ recommending ? '正在查找相似图案...' : '🔍 先看看已有的相似图案' }}
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>

        <!-- 智能推荐结果 -->
        <div v-if="recommendedPatterns.length > 0" class="recommend-section">
          <h3 class="recommend-title">
            🎯 发现 {{ recommendedPatterns.length }} 个相似图案
            <span class="recommend-hint">点击查看详情，或继续生成新图案</span>
          </h3>
          <div class="recommend-grid">
            <div
              v-for="pattern in recommendedPatterns"
              :key="pattern.id"
              class="recommend-card"
              @click="viewPatternDetail(pattern)"
            >
              <div class="recommend-image">
                <a-image
                  :src="pattern.thumbUrl || pattern.patternUrl"
                  :preview="false"
                  :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5Ij7ml6Dlm77niYc8L3RleHQ+PC9zdmc+'"
                />
              </div>
              <div class="recommend-info">
                <div class="recommend-name">{{ pattern.patternName || '未命名图案' }}</div>
                <div class="recommend-meta">
                  <a-tag v-if="pattern.style" color="blue" size="small">{{ pattern.style }}</a-tag>
                  <a-tag v-if="pattern.season" color="green" size="small">{{ pattern.season }}</a-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 创意灵感展示 -->
        <div class="inspiration-gallery" v-if="!generating">
          <h3 class="gallery-title">🌟 创意灵感参考</h3>
          <div class="gallery-grid">
            <div v-for="(item, index) in inspirationExamples" :key="index" class="gallery-item">
              <div class="gallery-image-placeholder">
                <span class="placeholder-icon">{{ item.icon }}</span>
              </div>
              <div class="gallery-info">
                <div class="gallery-name">{{ item.name }}</div>
                <div class="gallery-desc">{{ item.desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 生成进度提示 -->
        <div v-if="generating" class="generating-tips">
          <a-spin size="large" />
          <p class="tip-title">🎨 AI 正在创作中...</p>
          <p class="tip-desc">会生成 4 张候选图案（2x2 网格）</p>
          <p class="tip-desc">预计需要 1-2 分钟，请耐心等待</p>
        </div>
      </div>

      <!-- 步骤2：选择图片并执行操作 -->
      <div v-if="currentStep === 2" class="step-content">
        <h2 class="section-title">🎯 步骤 2：选择你喜欢的图案并进行操作</h2>
        <!-- 显示生成的 2x2 网格图片 -->
        <div class="grid-preview">
          <div class="image-container">
            <a-image
              :src="mjResponse.imageUrl"
              :preview="{
                src: mjResponse.rawImageUrl,
              }"
              class="grid-image"
            />
            <!-- 图片hover效果层 -->
            <div class="image-overlay">
              <span class="zoom-hint">👆 点击放大预览</span>
            </div>
          </div>
          <div class="grid-info">
            <div class="info-header">📋 生成信息</div>
            <p><strong>图案描述:</strong>{{ originalPrompt }}</p>
            <p v-if="formState.style"><strong>风格:</strong>{{ formState.style }}</p>
            <p v-if="formState.season"><strong>季节:</strong>{{ formState.season }}</p>
            <p v-if="formState.targetAudience">
              <strong>受众:</strong>{{ formState.targetAudience }}
            </p>
            <p class="tech-info"><strong>任务ID:</strong>{{ mjResponse.taskId }}</p>
            <p class="tech-info"><strong>图片ID:</strong>{{ mjResponse.imageId }}</p>

            <!-- 操作提示 -->
            <div class="operation-tips">
              <div class="tip-item">
                <span class="tip-icon">💡</span>
                <span class="tip-content">图片按 1-4 从左上到右下排列</span>
              </div>
              <div class="tip-item">
                <span class="tip-icon">🔍</span>
                <span class="tip-content">放大可生成高清单图</span>
              </div>
              <div class="tip-item">
                <span class="tip-icon">🎨</span>
                <span class="tip-content">变体可生成相似风格</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作选择 -->
        <div class="action-selection">
          <a-row :gutter="[16, 16]">
            <!-- Upsample 操作 -->
            <a-col :span="24">
              <div class="action-group">
                <h4>🔍 放大（Upsample）- 生成高清大图</h4>
                <a-space wrap>
                  <a-button
                    v-for="i in 4"
                    :key="'upsample' + i"
                    :type="selectedAction === 'upsample' + i ? 'primary' : 'default'"
                    @click="selectAction('upsample' + i)"
                    class="action-btn"
                  >
                    放大图片 {{ i }}
                  </a-button>
                </a-space>
              </div>
            </a-col>

            <!-- Variation 操作 -->
            <a-col :span="24">
              <div class="action-group">
                <h4>🎨 变体（Variation）- 生成相似风格的新图案</h4>
                <a-space wrap>
                  <a-button
                    v-for="i in 4"
                    :key="'variation' + i"
                    :type="selectedAction === 'variation' + i ? 'primary' : 'default'"
                    @click="selectAction('variation' + i)"
                    class="action-btn"
                  >
                    变体图片 {{ i }}
                  </a-button>
                </a-space>
              </div>
            </a-col>

            <!-- Reroll 操作 -->
            <a-col :span="24">
              <div class="action-group">
                <h4>🔄 重新生成（Reroll）- 生成全新的 4 张图案</h4>
                <a-button
                  :type="selectedAction === 'reroll' ? 'primary' : 'default'"
                  @click="selectAction('reroll')"
                  class="action-btn"
                >
                  重新生成
                </a-button>
              </div>
            </a-col>
          </a-row>

          <!-- 执行按钮 -->
          <div class="action-buttons">
            <a-button size="large" @click="backToStep1"> <LeftOutlined /> 返回重新生成 </a-button>
            <a-button
              type="primary"
              size="large"
              :loading="executing"
              :disabled="!selectedAction || executing"
              @click="executeAction"
              class="primary-action-btn"
            >
              <template #icon>
                <ThunderboltOutlined v-if="!executing" />
              </template>
              {{ executing ? '执行中...' : '执行操作' }}
            </a-button>
          </div>
        </div>
      </div>

      <!-- 步骤3：继续优化或保存 -->
      <div v-if="currentStep === 3" class="step-content">
        <h2 class="section-title"><CheckOutlined /> 步骤 3：继续优化或保存</h2>

        <!-- 显示最终结果 -->
        <div class="final-preview">
          <div class="image-container">
            <a-image
              :src="finalResult.imageUrl"
              :preview="{
                src: finalResult.rawImageUrl,
              }"
              class="final-image"
            />
            <div class="image-overlay">
              <span class="zoom-hint">👆 点击放大预览</span>
            </div>
          </div>
          <div class="final-info">
            <div class="info-header">✅ 操作结果</div>
            <p><strong>执行操作:</strong>{{ getActionName(lastExecutedAction) }}</p>
            <p><strong>图案描述:</strong>{{ originalPrompt }}</p>
            <p v-if="formState.style"><strong>风格:</strong>{{ formState.style }}</p>
            <p v-if="formState.season"><strong>季节:</strong>{{ formState.season }}</p>
            <p v-if="formState.targetAudience">
              <strong>受众:</strong>{{ formState.targetAudience }}
            </p>

            <!-- 操作提示 -->
            <div class="operation-tips">
              <div class="tip-item">
                <span class="tip-icon">💡</span>
                <span class="tip-content">图片按 1-4 从左上到右下排列</span>
              </div>
              <div class="tip-item" v-if="isVariationResult">
                <span class="tip-icon">🔍</span>
                <span class="tip-content">放大可生成高清单图，放大后可保存</span>
              </div>
              <div class="tip-item">
                <span class="tip-icon">🎨</span>
                <span class="tip-content">变体可生成相似风格的新图案</span>
              </div>
              <div class="tip-item" v-if="!isVariationResult">
                <span class="tip-icon">💾</span>
                <span class="tip-content">单图可直接保存或继续变体优化</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作选择区域 -->
        <div class="action-selection">
          <a-row :gutter="[16, 16]">
            <!-- 变体结果：显示放大、变体、重新生成 -->
            <template v-if="isVariationResult">
              <!-- Upsample 操作 -->
              <a-col :span="24">
                <div class="action-group">
                  <h4>🔍 放大（Upsample）- 生成高清大图</h4>
                  <a-space wrap>
                    <a-button
                      v-for="i in 4"
                      :key="'upsample' + i"
                      :type="selectedAction === 'upsample' + i ? 'primary' : 'default'"
                      @click="selectAction('upsample' + i)"
                      class="action-btn"
                    >
                      放大图片 {{ i }}
                    </a-button>
                  </a-space>
                </div>
              </a-col>

              <!-- Variation 操作 -->
              <a-col :span="24">
                <div class="action-group">
                  <h4>🎨 变体（Variation）- 生成相似风格的新图案</h4>
                  <a-space wrap>
                    <a-button
                      v-for="i in 4"
                      :key="'variation' + i"
                      :type="selectedAction === 'variation' + i ? 'primary' : 'default'"
                      @click="selectAction('variation' + i)"
                      class="action-btn"
                    >
                      变体图片 {{ i }}
                    </a-button>
                  </a-space>
                </div>
              </a-col>

              <!-- Reroll 操作 -->
              <a-col :span="24">
                <div class="action-group">
                  <h4>🔄 重新生成（Reroll）- 生成全新的 4 张图案</h4>
                  <a-button
                    :type="selectedAction === 'reroll' ? 'primary' : 'default'"
                    @click="selectAction('reroll')"
                    class="action-btn"
                  >
                    重新生成
                  </a-button>
                </div>
              </a-col>
            </template>

            <!-- 放大结果：显示变体和保存 -->
            <template v-else>
              <!-- Variation 操作 -->
              <a-col :span="24">
                <div class="action-group">
                  <h4>🎨 变体（Variation）- 基于当前图片生成4张相似风格</h4>
                  <a-space wrap>
                    <a-button
                      v-for="i in 4"
                      :key="'variation' + i"
                      :type="selectedAction === 'variation' + i ? 'primary' : 'default'"
                      @click="selectAction('variation' + i)"
                      class="action-btn"
                    >
                      变体方案 {{ i }}
                    </a-button>
                  </a-space>
                  <div class="operation-note">
                    💡 提示：对于放大后的单图，变体操作会生成4张不同角度的相似图案
                  </div>
                </div>
              </a-col>

              <!-- 保存操作 -->
              <a-col :span="24">
                <div class="action-group save-group">
                  <h4>💾 保存图案 - 将高清图保存到我的创意</h4>
                  <a-form :model="saveForm" layout="vertical" class="save-form">
                    <a-form-item
                      label="图案名称"
                      name="patternName"
                      :rules="[{ required: true, message: '请输入图案名称' }]"
                    >
                      <a-input
                        v-model:value="saveForm.patternName"
                        placeholder="为你的图案起个名字"
                        size="large"
                        :maxlength="50"
                        show-count
                      />
                    </a-form-item>
                  </a-form>
                </div>
              </a-col>
            </template>
          </a-row>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <a-button size="large" @click="backToStep1"> 
              <LeftOutlined /> 重新开始 
            </a-button>
            
            <!-- 如果是放大结果，显示保存按钮 -->
            <template v-if="!isVariationResult">
              <a-button
                type="primary"
                size="large"
                :loading="saving"
                :disabled="!saveForm.patternName || saving"
                @click="saveToDatabase"
                class="primary-action-btn"
              >
                <template #icon>
                  <SaveOutlined v-if="!saving" />
                </template>
                {{ saving ? '保存中...' : '保存图案' }}
              </a-button>
            </template>
            
            <!-- 继续操作按钮（变体结果或已选择操作时显示） -->
            <a-button
              v-if="isVariationResult || selectedAction"
              type="primary"
              size="large"
              :loading="executing"
              :disabled="!selectedAction || executing"
              @click="executeContinueAction"
              class="primary-action-btn"
            >
              <template #icon>
                <ThunderboltOutlined v-if="!executing" />
              </template>
              {{ executing ? '执行中...' : '执行操作' }}
            </a-button>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ThunderboltOutlined,
  CheckOutlined,
  LeftOutlined,
  SaveOutlined,
  SearchOutlined,
  EditOutlined,
} from '@ant-design/icons-vue'
import { imagine, executeAction as mjExecuteAction, savePattern, recommendPatterns, expandPrompt } from '@/api/midjourneyjiekou'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 判断用户是否已登录
const isUserLoggedIn = computed(() => {
  return loginUserStore.loginUser && loginUserStore.loginUser.id
})

// 页面加载时获取登录用户信息
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
})

// 当前步骤：1=输入提示词，2=选择操作，3=确认保存
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
const recommending = ref(false)
const expanding = ref(false)

// 推荐结果
const recommendedPatterns = ref<any[]>([])

// MJ 响应数据
const mjResponse = ref<any>(null)
const finalResult = ref<any>(null)
const originalPrompt = ref('')

// 选中的操作
const selectedAction = ref<string | null>(null)
// 最后执行的操作（用于显示）
const lastExecutedAction = ref<string>('')
// 判断当前结果是否为变体结果（4张图）
const isVariationResult = ref(true)

// 快捷提示词
const quickPrompts = ref([
  '可爱的卡通小猫图案',
  '复古花卉印花设计',
  '简约几何线条图案',
  '森系小清新植物',
  '赛博朋克科技风',
  '中国风水墨山水',
  '波普艺术风格',
  '极简主义条纹',
])

// 灵感示例
const inspirationExamples = ref([
  { icon: '🌸', name: '花卉印花', desc: '适合春夏季节' },
  { icon: '🦋', name: '蝴蝶元素', desc: '轻盈浪漫风格' },
  { icon: '⭐', name: '星空图案', desc: '梦幻神秘感' },
  { icon: '🍃', name: '植物叶子', desc: '自然清新风' },
  { icon: '🎨', name: '抽象艺术', desc: '独特个性化' },
  { icon: '🔷', name: '几何形状', desc: '现代简约风' },
])

// 步骤1：生成图片
const handleGenerate = async () => {
  // 检查登录状态
  if (!isUserLoggedIn.value) {
    message.warning('请先登录后再使用智能创作功能')
    // 跳转到登录页面
    router.push({
      path: '/user/login',
      query: {
        redirect: '/mj/generation' // 登录成功后返回当前页面
      }
    })
    return
  }

  try {
    generating.value = true

    // 保存原始提示词（用于显示）
    originalPrompt.value = formState.prompt

    message.loading({
      content: '正在生成图案，请耐心等待...',
      key: 'generating',
      duration: 0,
    })

    // 直接传递所有字段给后端，后端会处理组合和翻译
    const res = await imagine({
      prompt: formState.prompt,
      action: 'generate',
      style: formState.style,
      season: formState.season,
      targetAudience: formState.targetAudience,
    })

    if (res.data.code === 0 && res.data.data) {
      mjResponse.value = res.data.data
      currentStep.value = 2
      message.success({
        content: '图案生成成功！请选择你喜欢的图片进行操作',
        key: 'generating',
      })
    } else {
      throw new Error(res.data.message || '生成失败')
    }
  } catch (error: any) {
    console.error('生成图案失败:', error)
    message.error({
      content: error.message || '生成失败，请重试',
      key: 'generating',
    })
  } finally {
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
      content: `正在执行 ${getActionName(selectedAction.value)}...`,
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
      
      // 清理可能存在的URL格式问题
      if (newResult.imageUrl) {
        newResult.imageUrl = newResult.imageUrl.replace(/:\d+$/, '')
      }
      if (newResult.rawImageUrl) {
        newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\d+$/, '')
      }
      
      finalResult.value = newResult
      lastExecutedAction.value = selectedAction.value

      // 判断结果类型：variation和reroll返回4张图，upsample返回单图
      isVariationResult.value = selectedAction.value.startsWith('variation') || selectedAction.value === 'reroll'

      // 如果是放大操作，自动填充图案名称
      if (!isVariationResult.value) {
        const timestamp = new Date().getTime()
        saveForm.patternName = `MJ-${originalPrompt.value.substring(0, 15)}-${timestamp.toString().slice(-6)}`
      }

      // 重置选择
      selectedAction.value = null
      
      currentStep.value = 3
      message.success({
        content: '操作执行成功！可以保存或继续优化',
        key: 'executing',
      })
    } else {
      throw new Error(res.data.message || '执行失败')
    }
  } catch (error: any) {
    console.error('执行操作失败:', error)
    message.error({
      content: error.message || '执行失败，请重试',
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
      content: `正在执行 ${getActionName(selectedAction.value)}...`,
      key: 'executing',
      duration: 0,
    })

    const res = await mjExecuteAction({
      taskId: finalResult.value.taskId,
      imageId: finalResult.value.imageId,
      action: selectedAction.value,
    })

    if (res.data.code === 0 && res.data.data) {
      // 更新最终结果
      const newResult = res.data.data
      
      // 清理可能存在的URL格式问题
      if (newResult.imageUrl) {
        // 移除URL末尾可能存在的 :数字 格式
        newResult.imageUrl = newResult.imageUrl.replace(/:\d+$/, '')
      }
      if (newResult.rawImageUrl) {
        newResult.rawImageUrl = newResult.rawImageUrl.replace(/:\d+$/, '')
      }
      
      finalResult.value = newResult
      lastExecutedAction.value = selectedAction.value

      // 判断新结果类型
      isVariationResult.value = selectedAction.value.startsWith('variation') || selectedAction.value === 'reroll'

      // 如果是放大操作，自动填充图案名称
      if (!isVariationResult.value) {
        const timestamp = new Date().getTime()
        saveForm.patternName = `MJ-${originalPrompt.value.substring(0, 15)}-${timestamp.toString().slice(-6)}`
      }

      // 重置选择
      selectedAction.value = null
      
      // 打印调试信息
      console.log('继续操作成功，新结果：', finalResult.value)
      console.log('图片URL:', finalResult.value.imageUrl)
      console.log('原始URL:', finalResult.value.rawImageUrl)
      
      message.success({
        content: '操作执行成功！可以继续优化或保存',
        key: 'executing',
      })
    } else {
      throw new Error(res.data.message || '执行失败')
    }
  } catch (error: any) {
    console.error('执行操作失败:', error)
    message.error({
      content: error.message || '执行失败，请重试',
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

// 返回步骤2
const backToStep2 = () => {
  currentStep.value = 2
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
    message.warning('请先输入至少 2 个字的描述')
    return
  }

  try {
    expanding.value = true
    
    message.loading({
      content: 'AI 正在扩写中...',
      key: 'expanding',
      duration: 0,
    })

    const res = await expandPrompt({ prompt: formState.prompt })

    if (res.data.code === 0 && res.data.data) {
      formState.prompt = res.data.data
      message.success({
        content: '扩写成功！已更新为更详细的描述',
        key: 'expanding',
      })
    } else {
      throw new Error(res.data.message || '扩写失败')
    }
  } catch (error: any) {
    console.error('AI扩写失败:', error)
    message.error({
      content: error.message || '扩写失败，请重试',
      key: 'expanding',
    })
  } finally {
    expanding.value = false
  }
}

// 获取智能推荐
const fetchRecommendations = async () => {
  if (!formState.prompt || formState.prompt.trim().length < 2) {
    message.warning('请先输入至少 2 个字的描述')
    return
  }

  try {
    recommending.value = true
    recommendedPatterns.value = []

    const res = await recommendPatterns({ prompt: formState.prompt })

    if (res.data.code === 0 && res.data.data) {
      recommendedPatterns.value = res.data.data
      if (res.data.data.length === 0) {
        message.info('暂无相似图案，试试 AI 生成全新设计吧！')
      } else {
        message.success(`找到 ${res.data.data.length} 个相似图案`)
      }
    }
  } catch (error: any) {
    console.error('获取推荐失败:', error)
    message.error('获取推荐失败')
  } finally {
    recommending.value = false
  }
}

// 查看推荐图案详情
const viewPatternDetail = (pattern: any) => {
  router.push(`/pattern/detail/${pattern.id}`)
}
</script>

<style scoped>
/* 全局基础样式 */
.mj-pattern-generation-page {
  padding: 32px 24px;
  min-height: 100vh;
  background-image: url('@/assets/backgroundImage/gen-bg.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  color: #ffffff;
  animation: fadeInDown 0.6s ease-out;
}

.page-header h1 {
  font-size: clamp(28px, 5vw, 42px);
  font-weight: 700;
  margin: 0 0 12px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
}

.page-header .subtitle {
  font-size: clamp(14px, 2.5vw, 18px);
  margin: 0;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
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

/* 未登录提示 */
.login-alert {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(250, 173, 20, 0.2);
  animation: fadeInDown 0.6s ease-out;
}

:deep(.login-alert .ant-alert-message) {
  font-size: 16px;
  font-weight: 600;
  color: #d46b08;
}

:deep(.login-alert .ant-alert-description) {
  font-size: 14px;
  color: #ad6800;
  margin-top: 4px;
}

/* 步骤指示器 */
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 36px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.step-item {
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

.step-label {
  margin-top: 8px;
  font-size: 14px;
  color: #a0aec0;
  transition: all 0.3s;
}

.step-divider {
  flex: 1;
  height: 3px;
  background-color: #4a5568;
  margin: 0 16px;
  transition: all 0.3s;
}

/* 步骤状态样式 */
.step-item.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
  transform: scale(1.1);
}

.step-item.active .step-label {
  color: #ffffff;
  font-weight: 500;
}

.step-item.completed .step-number {
  background-color: #38b2ac;
  border-color: #4fd1c5;
  box-shadow: 0 0 15px rgba(62, 187, 182, 0.4);
}

.step-item.completed .step-label {
  color: #38b2ac;
}

.step-divider.completed {
  background-color: #38b2ac;
}

/* 主卡片样式 */
.generation-card {
  max-width: 100%;
  margin: 0 auto;
  border-radius: 20px;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.25);
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
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

:deep(.ant-card-body) {
  padding: clamp(20px, 4vw, 40px);
  background-color: transparent;
}

/* 步骤内容样式 */
.step-content {
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 功能特性展示 */
.features-showcase {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: clamp(16px, 2vw, 20px);
  margin-bottom: clamp(28px, 4vw, 36px);
  padding: clamp(18px, 3vw, 24px);
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border-radius: 16px;
  border: 1px solid #d6e4ff;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  padding: clamp(16px, 2.5vw, 20px);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}

.feature-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.feature-icon {
  font-size: clamp(28px, 4vw, 36px);
  flex-shrink: 0;
  line-height: 1;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.feature-text strong {
  font-size: clamp(15px, 2vw, 16px);
  color: #1a202c;
  font-weight: 600;
}

.feature-text span {
  font-size: clamp(12px, 1.8vw, 13px);
  color: #718096;
  line-height: 1.4;
}

@media (max-width: 1024px) {
  .features-showcase {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .features-showcase {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    padding: 14px;
  }
}

@media (max-width: 480px) {
  .features-showcase {
    grid-template-columns: 1fr;
  }
}

/* 快捷提示词 */
.quick-prompts {
  margin-bottom: clamp(20px, 3vw, 24px);
  padding: clamp(14px, 2.5vw, 18px);
  background: linear-gradient(135deg, #fff9f0 0%, #fffaf3 100%);
  border-radius: 12px;
  border: 1px solid #ffe7ba;
  border-left: 4px solid #fa8c16;
}

.prompt-label {
  font-size: clamp(14px, 2vw, 15px);
  font-weight: 600;
  color: #d46b08;
  margin-bottom: 12px;
  display: block;
}

.prompt-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.prompt-tag {
  cursor: pointer;
  font-size: clamp(12px, 1.8vw, 13px);
  padding: 6px 14px;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid currentColor;
  user-select: none;
}

.prompt-tag:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
}

.prompt-tag:active {
  transform: translateY(0) scale(0.98);
}

/* 创意灵感画廊 */
.inspiration-gallery {
  margin-top: clamp(32px, 4vw, 40px);
  padding: clamp(24px, 3vw, 32px);
  background: linear-gradient(135deg, #f9f0ff 0%, #f5f5ff 100%);
  border-radius: 16px;
  border: 1px solid #d3adf7;
}

.gallery-title {
  font-size: clamp(16px, 2.5vw, 18px);
  font-weight: 600;
  color: #531dab;
  margin-bottom: clamp(16px, 2.5vw, 20px);
  display: flex;
  align-items: center;
  gap: 8px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: clamp(16px, 2vw, 20px);
}

@media (max-width: 1200px) {
  .gallery-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .gallery-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .gallery-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.gallery-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: clamp(12px, 2vw, 16px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #efdbff;
  cursor: pointer;
  text-align: center;
}

.gallery-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(83, 29, 171, 0.15);
  border-color: #9254de;
}

.gallery-image-placeholder {
  width: 100%;
  aspect-ratio: 1;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  border: 2px dashed #d6e4ff;
}

.placeholder-icon {
  font-size: clamp(40px, 6vw, 56px);
  line-height: 1;
}

.gallery-info {
  text-align: center;
}

.gallery-name {
  font-size: clamp(13px, 2vw, 14px);
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 4px;
}

.gallery-desc {
  font-size: clamp(11px, 1.6vw, 12px);
  color: #718096;
  line-height: 1.4;
}

.section-title {
  font-size: clamp(20px, 3.5vw, 26px);
  font-weight: 600;
  margin-bottom: clamp(20px, 3vw, 32px);
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f2f5;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.section-title .anticon {
  color: #667eea;
  font-size: clamp(24px, 4vw, 28px);
}

/* 表单元素样式优化 */
:deep(.ant-form-item) {
  margin-bottom: 28px;
}

:deep(.ant-form-item-label > label) {
  font-size: 16px;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 10px;
}

:deep(.ant-input-lg),
:deep(.ant-select-lg .ant-select-selector),
:deep(.ant-input-textarea-lg) {
  border-radius: 12px !important;
  font-size: clamp(15px, 2vw, 16px);
  padding: 12px 16px;
  border-color: #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.ant-input-lg:focus),
:deep(.ant-select-lg.ant-select-focused .ant-select-selector),
:deep(.ant-input-textarea-lg:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
  outline: none;
  transform: translateY(-1px);
}

:deep(.ant-input-textarea-lg) {
  min-height: 140px !important;
  resize: vertical;
}

:deep(.ant-input-count) {
  font-size: clamp(11px, 1.5vw, 12px);
  color: #a0aec0;
}

/* 提示文字 */
.tip-text {
  margin-top: 8px;
  font-size: clamp(12px, 1.8vw, 13px);
  color: #718096;
  line-height: 1.6;
  background-color: #f7fafc;
  padding: 8px 12px;
  border-radius: 8px;
  display: block;
  border-left: 3px solid #667eea;
}

/* 提示词操作区域 */
.prompt-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.prompt-actions .tip-text {
  margin-top: 0;
  flex: 1;
}

/* AI扩写按钮 */
.expand-btn {
  font-weight: 500;
  color: #667eea !important;
  padding: 4px 12px;
  height: auto;
  border-radius: 6px;
  transition: all 0.3s;
  flex-shrink: 0;
}

.expand-btn:hover:not(:disabled) {
  background-color: rgba(102, 126, 234, 0.1) !important;
  color: #5a67d8 !important;
}

.expand-btn:disabled {
  color: #a0aec0 !important;
}

/* 生成按钮样式 */
/* 主按钮（Success 绿色系） */
:deep(.ant-btn-primary) {
  /* 成功色渐变：浅绿 → 深绿（匹配 Ant Design Success 色值） */
  background: linear-gradient(135deg, #73d13d 0%, #52c41a 100%);
  border: none;
  border-radius: 12px !important;
  font-size: 16px;
  font-weight: 500;
  height: 56px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  /* 阴影同步改为绿色系，保持质感 */
  box-shadow: 0 4px 15px rgba(82, 196, 26, 0.3);
}

/* 悬浮状态（略深的绿色） */
:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #84d850 0%, #5bcf22 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.4);
}

/* 点击激活状态 */
:deep(.ant-btn-primary:active) {
  background: linear-gradient(135deg, #52c41a 0%, #41a30f 100%);
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(82, 196, 26, 0.3);
}

/* 禁用状态（生成中时的按钮，浅灰绿） */
:deep(.ant-btn-primary:disabled) {
  background: linear-gradient(135deg, #b7eb8f 0%, #95de64 100%);
  transform: none;
  box-shadow: none;
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 生成进度提示 */
.generating-tips {
  text-align: center;
  padding: clamp(40px, 6vw, 60px) clamp(16px, 3vw, 20px);
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  margin-top: clamp(20px, 3vw, 24px);
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.1);
}

.generating-tips .ant-spin {
  font-size: clamp(36px, 6vw, 48px);
  color: #667eea;
}

.tip-title {
  margin-top: clamp(18px, 3vw, 24px);
  font-size: clamp(18px, 3vw, 22px);
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.tip-desc {
  margin-top: 8px;
  font-size: clamp(14px, 2vw, 15px);
  color: #718096;
  line-height: 1.8;
}

/* 图片预览区域 */
.grid-preview,
.final-preview {
  margin-bottom: clamp(24px, 4vw, 36px);
  display: flex;
  flex-direction: row;
  gap: clamp(16px, 2.5vw, 24px);
  align-items: flex-start;
}

@media (max-width: 968px) {
  .grid-preview,
  .final-preview {
    flex-direction: column;
  }
}

.image-container {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex: 0 0 58%;
  max-width: 58%;
}

@media (max-width: 968px) {
  .image-container {
    flex: 0 0 100%;
    max-width: 100%;
  }
}

.image-container:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  transform: translateY(-2px);
}

.grid-image,
.final-image {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 16px;
  transition: all 0.5s;
}

.image-container:hover .grid-image,
.image-container:hover .final-image {
  transform: scale(1.02);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  align-items: flex-end;
  padding: 20px;
  pointer-events: none;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.zoom-hint {
  color: #ffffff;
  font-size: 14px;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 6px 12px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

/* 信息卡片样式 */
.grid-info,
.final-info {
  padding: clamp(18px, 3vw, 24px);
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  border: 1px solid #e8eaf6;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  flex: 0 0 42%;
  max-width: 42%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 200px;
}

@media (max-width: 968px) {
  .grid-info,
  .final-info {
    flex: 0 0 100%;
    max-width: 100%;
    min-height: auto;
  }
}

.info-header {
  font-size: clamp(15px, 2.2vw, 17px);
  font-weight: 600;
  color: #667eea;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e8eaf6;
  display: flex;
  align-items: center;
  gap: 8px;
}

.grid-info p,
.final-info p {
  margin: 10px 0;
  font-size: clamp(14px, 2vw, 15px);
  color: #2d3748;
  line-height: 1.6;
  word-break: break-word;
}

.grid-info p strong,
.final-info p strong {
  color: #1a202c;
  margin-right: 8px;
  font-weight: 600;
}

.grid-info p.tech-info,
.final-info p.tech-info {
  font-size: clamp(12px, 1.8vw, 13px);
  color: #718096;
}

/* 操作提示 */
.operation-tips {
  margin-top: 16px;
  padding: 14px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border-radius: 10px;
  border-left: 3px solid #1890ff;
}

.operation-tips .tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  font-size: clamp(12px, 1.8vw, 13px);
  color: #2d3748;
}

.operation-tips .tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.operation-tips .tip-content {
  line-height: 1.5;
}

/* 下一步提示 */
.next-step-tips {
  margin-top: 16px;
  padding: 14px;
  background: linear-gradient(135deg, #f6ffed 0%, #f0fff4 100%);
  border-radius: 10px;
  border-left: 3px solid #52c41a;
}

.next-step-tips .tip-highlight {
  margin: 8px 0;
  font-size: clamp(12px, 1.8vw, 13px);
  color: #389e0d;
  line-height: 1.6;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 操作选择区域 */
.action-selection {
  margin-top: 8px;
}

.action-selection h3 {
  font-size: clamp(16px, 2.5vw, 18px);
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-group {
  padding: clamp(18px, 3vw, 24px);
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16px;
  margin-bottom: clamp(16px, 2.5vw, 20px);
  border: 1px solid #e8eaf6;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-group:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.1);
  border-color: #c5cae9;
  transform: translateY(-2px);
}

.action-group h4 {
  margin: 0 0 clamp(12px, 2vw, 16px) 0;
  font-size: clamp(14px, 2vw, 16px);
  font-weight: 600;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 操作按钮样式 */
.action-btn {
  border-radius: 8px !important;
  font-size: clamp(13px, 1.8vw, 14px) !important;
  padding: 10px 18px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  min-height: 40px !important;
}

@media (max-width: 576px) {
  .action-btn {
    width: 100% !important;
  }
}

:deep(.action-btn.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
}

:deep(.action-btn.ant-btn-primary:hover) {
  transform: translateY(-2px) !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3) !important;
}

:deep(.action-btn.ant-btn-default) {
  border-color: #e2e8f0 !important;
  color: #4a5568 !important;
}

:deep(.action-btn.ant-btn-default:hover) {
  border-color: #667eea !important;
  color: #667eea !important;
  background-color: #f0f5ff !important;
}

/* 底部操作按钮组 */
.action-buttons {
  margin-top: clamp(24px, 4vw, 32px);
  display: flex;
  gap: clamp(12px, 2vw, 16px);
  justify-content: flex-end;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .action-buttons {
    justify-content: stretch;
  }
}

:deep(.action-buttons .ant-btn) {
  border-radius: 12px !important;
  font-size: clamp(14px, 2vw, 16px) !important;
  font-weight: 500 !important;
  height: clamp(48px, 6vw, 52px) !important;
  padding: 0 clamp(18px, 3vw, 24px) !important;
  min-width: 140px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

@media (max-width: 768px) {
  :deep(.action-buttons .ant-btn) {
    flex: 1;
    min-width: auto !important;
  }
}

.primary-action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3) !important;
}

.primary-action-btn:hover {
  background: linear-gradient(135deg, #7486e0 0%, #8561b2 100%) !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4) !important;
}

.secondary-action-btn {
  background-color: #edf2f7 !important;
  color: #2d3748 !important;
  border: 1px solid #dee2e6 !important;
}

.secondary-action-btn:hover {
  background-color: #e2e8f0 !important;
  color: #1a202c !important;
  border-color: #cbd5e0 !important;
}

/* 响应式优化 */
@media (max-width: 1024px) {
  .mj-pattern-generation-page {
    padding: 32px 20px;
  }

  .generation-card {
    border-radius: 16px;
  }
}

@media (max-width: 768px) {
  .mj-pattern-generation-page {
    padding: 24px 16px;
  }

  .generation-card {
    border-radius: 12px;
  }

  .generating-tips {
    padding: 32px 16px;
  }

  :deep(.ant-form-item) {
    margin-bottom: 20px;
  }

  :deep(.ant-form-item-label > label) {
    font-size: 15px;
  }

  :deep(.ant-input-lg),
  :deep(.ant-select-lg .ant-select-selector),
  :deep(.ant-input-textarea-lg) {
    font-size: 15px;
    padding: 10px 14px;
  }

  :deep(.ant-space-wrap) {
    gap: 8px !important;
  }
}

@media (max-width: 480px) {
  .mj-pattern-generation-page {
    padding: 20px 12px;
  }

  .page-header {
    margin-bottom: 20px;
  }

  .generation-card {
    border-radius: 8px;
  }

  .generating-tips {
    padding: 24px 12px;
  }

  .tip-title {
    font-size: 16px;
  }

  .tip-desc {
    font-size: 13px;
  }

  :deep(.ant-space) {
    width: 100%;
  }

  :deep(.ant-space-wrap) {
    display: flex !important;
    flex-direction: column !important;
    width: 100%;
  }

  .image-container {
    border-radius: 12px;
  }

  .action-group {
    border-radius: 12px;
  }
}

/* 加载状态优化 */
:deep(.ant-spin-dot-item) {
  background-color: #667eea !important;
}

:deep(.ant-btn-loading .anticon-loading) {
  color: rgba(255, 255, 255, 0.8) !important;
}

/* 保存区域样式 */
.save-group {
  background: linear-gradient(135deg, #f6ffed 0%, #f0fff4 100%);
  border-color: #b7eb8f;
}

.save-group h4 {
  color: #389e0d;
}

.save-form {
  margin-top: 8px;
}

/* 操作注释样式 */
.operation-note {
  margin-top: 12px;
  padding: 10px 14px;
  background: #f0f5ff;
  border-radius: 8px;
  font-size: 13px;
  color: #1d39c4;
  border-left: 3px solid #597ef7;
}

@media (max-width: 768px) {
  .save-form :deep(.ant-form-item) {
    margin-bottom: 16px;
  }
}

/* 智能推荐按钮样式 */
.recommend-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 500;
}

.recommend-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
  color: white;
}

.recommend-btn:disabled {
  background: #e8e8e8;
  color: #999;
}

/* 智能推荐区域样式 */
.recommend-section {
  margin-top: 32px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-radius: 16px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.recommend-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.recommend-hint {
  font-size: 13px;
  font-weight: 400;
  color: #666;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.recommend-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
}

.recommend-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;
}

.recommend-image :deep(.ant-image) {
  width: 100%;
  height: 100%;
}

.recommend-image :deep(.ant-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-info {
  padding: 12px;
}

.recommend-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.recommend-meta :deep(.ant-tag) {
  margin: 0;
  font-size: 11px;
}

@media (max-width: 768px) {
  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .recommend-section {
    padding: 16px;
  }
}
</style>
