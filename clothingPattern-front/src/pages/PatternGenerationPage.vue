<template>
  <div id="patternGenerationPage" class="pattern-generation-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🎨 智能服装图案创作</h1>
      <p class="subtitle">AI驱动，让创意无限可能</p>
    </div>

    <!-- 主内容区域 -->
    <a-card class="generation-card" :bordered="false">
      <a-row :gutter="[32, 32]">
        <!-- 左侧：参数配置区 -->
        <a-col :xs="24" :lg="10">
          <div class="config-section">
            <h2 class="section-title"><EditOutlined /> 创作参数</h2>

            <a-form :model="formState" layout="vertical" @finish="handleGenerate">
              <!-- AI服务选择 -->
              <a-form-item label="AI服务" name="serviceType">
                <a-segmented
                  v-model:value="formState.serviceType"
                  size="large"
                  block
                  :options="[
                    { label: '🌟 基础服务', value: 'qwen', icon: h(FileTextOutlined) },
                    { label: '⭐ 高级服务', value: 'doubao', icon: h(ThunderboltOutlined) },
                  ]"
                />
                <div class="tip-text" style="margin-top: 8px">
                  <span v-if="formState.serviceType === 'qwen'"
                    >💡 基础服务：使用千问图片模型，支持文字和图片生成</span
                  >
                  <span v-else>⚡ 高级服务：使用豆包图片模型，生成更高质量的图案</span>
                </div>
              </a-form-item>

              <!-- 图案名称 -->
              <a-form-item
                label="图案名称"
                name="patternName"
                :rules="[{ required: true, message: '请输入图案名称' }]"
              >
                <a-input
                  v-model:value="formState.patternName"
                  placeholder="例如：夏日花卉系列"
                  size="large"
                  :maxlength="50"
                  show-count
                />
              </a-form-item>

              <!-- 生成方式（基础服务） -->
              <a-form-item
                v-if="formState.serviceType === 'qwen'"
                label="生成方式"
                name="generationType"
                :rules="[{ required: true, message: '请选择生成方式' }]"
              >
                <a-radio-group
                  v-model:value="formState.generationType"
                  size="large"
                  button-style="solid"
                >
                  <a-radio-button :value="GENERATION_TYPE_ENUM.TEXT_GENERATED">
                    <FileTextOutlined /> 文字描述
                  </a-radio-button>
                  <a-radio-button :value="GENERATION_TYPE_ENUM.IMAGE_REFERENCED">
                    <PictureOutlined /> 参考图片
                  </a-radio-button>
                </a-radio-group>
              </a-form-item>

              <!-- 高级服务-生成模式选择 -->
              <a-form-item
                v-if="formState.serviceType === 'doubao'"
                label="生成模式"
                name="doubaoMode"
              >
                <a-select v-model:value="formState.doubaoMode" size="large">
                  <a-select-option value="single_text">📝 文生图（单张）</a-select-option>
                  <a-select-option value="single_image">🖼️ 图生图（单张）</a-select-option>
                  <a-select-option value="multi_image">🎭 多图生图（单张）</a-select-option>
                  <a-select-option value="batch_text">📦 文生组图（多张）</a-select-option>
                  <a-select-option value="batch_single_image"
                    >🌐 单图生组图（多张）</a-select-option
                  >
                  <a-select-option value="batch_multi_image">🎨 多图生组图（多张）</a-select-option>
                </a-select>
                <div class="tip-text" style="margin-top: 8px">
                  <span v-if="formState.doubaoMode.startsWith('batch')"
                    >💡 组图模式：一次生成多张图案，提供更多设计选择</span
                  >
                  <span v-else>⚡ 单图模式：生成一张高质量图案</span>
                </div>
              </a-form-item>

              <!-- 高级服务-文字描述（文生图/文生组图） -->
              <a-form-item
                v-if="
                  formState.serviceType === 'doubao' &&
                  (formState.doubaoMode === 'single_text' || formState.doubaoMode === 'batch_text')
                "
                label="设计描述"
                name="description"
                :rules="[{ required: true, message: '请输入设计描述' }]"
              >
                <a-textarea
                  v-model:value="formState.description"
                  placeholder="简单描述你想要的图案，例如：
• 花朵
• 几何图形
• 抽象线条
• 动物图案"
                  :rows="4"
                  size="large"
                  :maxlength="200"
                  show-count
                />
                <div class="tip-text">
                  ⚡ 高级服务：使用豆包模型，自动优化提示词，生成更专业的服装图案
                </div>
              </a-form-item>

              <!-- 高级服务-单图上传（单图生图/单图生组图） -->
              <a-form-item
                v-if="
                  formState.serviceType === 'doubao' &&
                  (formState.doubaoMode === 'single_image' ||
                    formState.doubaoMode === 'batch_single_image')
                "
                label="参考图片"
                name="referenceImageUrl"
                :rules="[{ required: true, message: '请上传参考图片' }]"
              >
                <a-upload
                  list-type="picture-card"
                  :max-count="1"
                  :before-upload="beforeUpload"
                  @remove="handleRemoveImage"
                  accept="image/*"
                >
                  <div v-if="!formState.referenceImageUrl">
                    <PlusOutlined />
                    <div style="margin-top: 8px">上传图片</div>
                  </div>
                </a-upload>
                <div class="tip-text">💡 支持JPG、PNG格式，图片会自动压缩</div>
                <a-textarea
                  v-model:value="formState.description"
                  placeholder="补充描述（可选）"
                  :rows="2"
                  size="large"
                  class="mt-2"
                />
              </a-form-item>

              <!-- 高级服务-多图上传（多图生图/多图生组图） -->
              <a-form-item
                v-if="
                  formState.serviceType === 'doubao' &&
                  (formState.doubaoMode === 'multi_image' ||
                    formState.doubaoMode === 'batch_multi_image')
                "
                label="参考图片"
                name="referenceImageUrls"
                :rules="[{ required: true, message: '请上传至少2张参考图片' }]"
              >
                <a-upload
                  list-type="picture-card"
                  :max-count="5"
                  :before-upload="beforeUploadMultiple"
                  @remove="handleRemoveMultipleImage"
                  accept="image/*"
                  multiple
                >
                  <div v-if="formState.referenceImageUrls.length < 5">
                    <PlusOutlined />
                    <div style="margin-top: 8px">上传图片</div>
                  </div>
                </a-upload>
                <div class="tip-text">💡 支持2-5张图片，AI将融合这些图片生成新图案</div>
                <a-textarea
                  v-model:value="formState.description"
                  placeholder="补充描述（可选）"
                  :rows="2"
                  size="large"
                  class="mt-2"
                />
              </a-form-item>

              <!-- 高级服务-生成数量（组图模式） -->
              <a-form-item
                v-if="
                  formState.serviceType === 'doubao' && formState.doubaoMode.startsWith('batch')
                "
                label="生成数量"
              >
                <a-slider
                  v-model:value="formState.maxImages"
                  :min="2"
                  :max="8"
                  :marks="{ 2: '2', 4: '4', 6: '6', 8: '8' }"
                />
                <div class="tip-text">
                  💡 一次生成 {{ formState.maxImages }} 张图案，提供更多设计选择
                </div>
              </a-form-item>

              <!-- 基础服务-文字描述模式 -->
              <a-form-item
                v-if="
                  formState.serviceType === 'qwen' &&
                  formState.generationType === GENERATION_TYPE_ENUM.TEXT_GENERATED
                "
                label="设计描述"
                name="description"
                :rules="[
                  {
                    required: formState.generationType === GENERATION_TYPE_ENUM.TEXT_GENERATED,
                    message: '请输入设计描述',
                  },
                ]"
              >
                <a-textarea
                  v-model:value="formState.description"
                  placeholder="简单描述你想要的图案，例如：\n• 花朵\n• 几何图形\n• 抽象线条\n• 动物图案"
                  :rows="4"
                  size="large"
                  :maxlength="200"
                  show-count
                />
                <div class="tip-text">
                  💡 提示：只需输入简单关键词，AI会自动优化为专业的服装图案描述
                </div>
              </a-form-item>

              <!-- 基础服务-参考图片模式 -->
              <a-form-item
                v-if="
                  formState.serviceType === 'qwen' &&
                  formState.generationType === GENERATION_TYPE_ENUM.IMAGE_REFERENCED
                "
                label="参考图片"
                name="referenceImageUrl"
                :rules="[
                  {
                    required: formState.generationType === GENERATION_TYPE_ENUM.IMAGE_REFERENCED,
                    message: '请上传参考图片',
                  },
                ]"
              >
                <a-upload
                  list-type="picture-card"
                  :max-count="1"
                  :before-upload="beforeUpload"
                  @remove="handleRemoveImage"
                  accept="image/*"
                >
                  <div v-if="!formState.referenceImageUrl">
                    <PlusOutlined />
                    <div style="margin-top: 8px">上传图片</div>
                  </div>
                </a-upload>
                <div class="tip-text">
                  💡 支持JPG、PNG格式，图片会自动压缩至800px宽度，质量70%，建议上传较小的图片
                </div>
                <a-textarea
                  v-model:value="formState.description"
                  placeholder="补充描述（可选）"
                  :rows="2"
                  size="large"
                  class="mt-2"
                />
              </a-form-item>

              <!-- 风格选择 -->
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
                </a-select>
              </a-form-item>

              <!-- 适用季节 -->
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

              <!-- 目标受众 -->
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

              <!-- 高级选项 -->
              <a-collapse v-model:activeKey="advancedOpen" ghost>
                <a-collapse-panel key="1" header="⚙️ 高级选项">
                  <!-- 豆包服务尺寸选项 -->
                  <a-form-item v-if="formState.serviceType === 'doubao'" label="图片尺寸">
                    <a-radio-group v-model:value="formState.size">
                      <a-radio value="1K">1K</a-radio>
                      <a-radio value="2K">2K</a-radio>
                      <a-radio value="4K">4K</a-radio>
                    </a-radio-group>
                  </a-form-item>

                  <!-- 千问服务尺寸选项 -->
                  <a-form-item v-if="formState.serviceType === 'qwen'" label="图片尺寸">
                    <a-radio-group v-model:value="formState.size">
                      <a-radio value="1328*1328">1328*1328</a-radio>
                      <a-radio value="1664*928">1664*928</a-radio>
                      <a-radio value="1472*1140">1472*1140</a-radio>

                      <a-radio value="1140*1472">1140*1472</a-radio>
                      <a-radio value="928*1664">928*1664</a-radio>
                    </a-radio-group>
                  </a-form-item>

                  <a-form-item v-if="formState.serviceType === 'qwen'" label="负面提示词">
                    <a-input
                      v-model:value="formState.negativePrompt"
                      placeholder="不希望出现的元素（可选）"
                    />
                  </a-form-item>

                  <a-form-item label="提示词扩展">
                    <a-switch v-model:checked="formState.promptExtend" />
                    <span class="ml-2">自动优化提示词</span>
                  </a-form-item>
                </a-collapse-panel>
              </a-collapse>

              <!-- 提交按钮 -->
              <a-form-item class="submit-btn-wrapper">
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
                  {{ generating ? '正在生成中...' : '开始创作' }}
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </a-col>

        <!-- 右侧：预览区域 -->
        <a-col :xs="24" :lg="14">
          <div class="preview-section">
            <h2 class="section-title"><EyeOutlined /> 生成预览</h2>

            <!-- 生成中状态 -->
            <div v-if="generating" class="generating-placeholder">
              <a-spin size="large" />
              <p class="generating-text">
                {{ isBatchMode ? '正在生成组图，请耐心等待...' : 'AI正在创作中，请稍候...' }}
              </p>
              <p class="generating-tip">
                {{
                  isBatchMode
                    ? `批量生成${formState.maxImages}张图片，预计需要2-4分钟`
                    : '通常需要30-60秒'
                }}
              </p>
            </div>

            <!-- 生成结果 -->
            <div v-else-if="generatedPattern" class="result-container">
              <div class="image-preview">
                <a-image
                  :src="generatedPattern.patternUrl"
                  :preview="{
                    src: generatedPattern.patternUrl,
                  }"
                  :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295aSx6LSlPC90ZXh0Pjwvc3ZnPg=='"
                />
              </div>

              <!-- 生成信息 -->
              <div class="result-info">
                <a-descriptions :column="1" size="small" bordered>
                  <a-descriptions-item label="图案名称">
                    {{ generatedPattern.patternName }}
                  </a-descriptions-item>
                  <a-descriptions-item label="审核状态">
                    <a-tag :color="AUDIT_STATUS_COLOR_MAP[generatedPattern.auditStatus]">
                      {{ AUDIT_STATUS_MAP[generatedPattern.auditStatus] }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item label="文件大小">
                    {{ formatFileSize(generatedPattern.fileSize) }}
                  </a-descriptions-item>
                  <a-descriptions-item label="创建时间">
                    {{ formatDateTime(generatedPattern.createTime) }}
                  </a-descriptions-item>
                </a-descriptions>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <a-button type="primary" @click="downloadPattern">
                    <DownloadOutlined /> 下载图案
                  </a-button>
                  <a-button @click="viewMyPatterns"> <FolderOpenOutlined /> 我的作品 </a-button>
                  <a-button @click="resetForm"> <ReloadOutlined /> 重新创作 </a-button>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <a-empty
              v-else
              description="填写创作参数后，点击开始创作生成图案"
              class="empty-placeholder"
            >
              <template #image>
                <img
                  src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIgcng9IjEwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iNDAiIGZpbGw9IiNkOWQ5ZDkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7wn46oPC90ZXh0Pjwvc3ZnPg=="
                  alt="empty"
                />
              </template>
            </a-empty>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  EyeOutlined,
  FileTextOutlined,
  PictureOutlined,
  ThunderboltOutlined,
  DownloadOutlined,
  FolderOpenOutlined,
  ReloadOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'
import { generatePattern } from '@/api/patternController'
import { useRouter } from 'vue-router'
import { AUDIT_STATUS_MAP, AUDIT_STATUS_COLOR_MAP, GENERATION_TYPE_ENUM } from '@/constants/pattern'

const router = useRouter()

// 表单状态
const formState = reactive<{
  serviceType: 'qwen' | 'doubao'
  patternName: string
  generationType: 'TEXT_GENERATED' | 'IMAGE_REFERENCED'
  doubaoMode:
    | 'single_text'
    | 'single_image'
    | 'multi_image'
    | 'batch_text'
    | 'batch_single_image'
    | 'batch_multi_image'
  description: string
  referenceImageUrl: string
  referenceImageUrls: string[] // 多图上传
  maxImages: number // 批量生成数量
  style: string | undefined
  season: string | undefined
  targetAudience: string | undefined
  size: string
  negativePrompt: string
  promptExtend: boolean
}>({
  serviceType: 'qwen',
  patternName: '',
  generationType: GENERATION_TYPE_ENUM.TEXT_GENERATED,
  doubaoMode: 'single_text',
  description: '',
  referenceImageUrl: '',
  referenceImageUrls: [],
  maxImages: 4,
  style: undefined,
  season: undefined,
  targetAudience: undefined,
  size: '1328*1328',
  negativePrompt: '',
  promptExtend: true,
})

// 生成状态
const generating = ref(false)
const generatedPattern = ref<any>(null)
const advancedOpen = ref<string[]>([])

// 计算是否是批量模式
const isBatchMode = ref(false)

// 监听服务类型变化，自动调整尺寸默认值
watch(
  () => formState.serviceType,
  (newType) => {
    if (newType === 'doubao') {
      formState.size = '2K' // 豆包默认2K
    } else {
      formState.size = '1328*1328' // 千问默认1328*1328
    }
  },
)

// 图片压缩函数
const compressImage = (file: File, maxWidth = 800, quality = 0.7): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = (e) => {
      const img = new Image()
      img.src = e.target?.result as string
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        // 计算缩放比例
        if (width > maxWidth) {
          height = (maxWidth / width) * height
          width = maxWidth
        }

        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        ctx?.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('图片压缩失败'))
            }
          },
          'image/jpeg',
          quality,
        )
      }
      img.onerror = () => reject(new Error('图片加载失败'))
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
  })
}

// 将Blob转换为base64
const blobToBase64 = (blob: Blob): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(blob)
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = () => reject(new Error('转换失败'))
  })
}

// 上传前处理
const beforeUpload = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('图片大小不能超过10MB！')
    return false
  }

  try {
    message.loading({ content: '正在压缩图片...', key: 'compress', duration: 0 })

    // 压缩图片（更小的尺寸和质量以减少base64长度）
    const compressedBlob = await compressImage(file, 800, 0.7)

    // 检查压缩后的大小
    const compressedSizeKB = compressedBlob.size / 1024
    console.log(
      `原始大小: ${(file.size / 1024).toFixed(2)}KB, 压缩后: ${compressedSizeKB.toFixed(2)}KB`,
    )

    // 转换为base64
    const base64 = await blobToBase64(compressedBlob)

    // 检查base64长度（Qwen API有长度限制）
    if (base64.length > 100000) {
      message.error({
        content: `图片仍然过大（${(base64.length / 1000).toFixed(0)}K字符），请选择更小的图片`,
        key: 'compress',
      })
      return false
    }

    // 保存到表单
    formState.referenceImageUrl = base64

    message.success({
      content: `图片上传成功！压缩后: ${compressedSizeKB.toFixed(1)}KB`,
      key: 'compress',
    })
  } catch (error: any) {
    message.error({ content: '图片处理失败：' + error.message, key: 'compress' })
  }

  return false // 阻止自动上传
}

// 移除图片
const handleRemoveImage = () => {
  formState.referenceImageUrl = ''
}

// 多图上传前处理
const beforeUploadMultiple = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('图片大小不能超过10MB！')
    return false
  }

  try {
    const fileKey = 'compress_' + Date.now()
    message.loading({ content: '正在压缩图片...', key: fileKey, duration: 0 })

    const compressedBlob = await compressImage(file, 800, 0.7)
    const compressedSizeKB = compressedBlob.size / 1024
    const base64 = await blobToBase64(compressedBlob)

    if (base64.length > 100000) {
      message.error({
        content: `图片仍然过大（${(base64.length / 1000).toFixed(0)}K字符），请选择更小的图片`,
        key: fileKey,
      })
      return false
    }

    // 添加到多图数组
    formState.referenceImageUrls.push(base64)

    message.success({
      content: `图片上传成功！压缩后: ${compressedSizeKB.toFixed(1)}KB`,
      key: fileKey,
    })
  } catch (error: any) {
    const fileKey = 'compress_' + Date.now()
    message.error({ content: '图片处理失败：' + error.message, key: fileKey })
  }

  return false
}

// 移除多图中的某张图片
const handleRemoveMultipleImage = (file: any) => {
  const index = formState.referenceImageUrls.indexOf(file.url || file.thumbUrl)
  if (index > -1) {
    formState.referenceImageUrls.splice(index, 1)
  }
}

// 生成图案
const handleGenerate = async () => {
  try {
    generating.value = true
    isBatchMode.value =
      formState.serviceType === 'doubao' && formState.doubaoMode.startsWith('batch')

    message.loading({
      content: isBatchMode.value ? '正在生成组图，请稍候...' : '正在生成图案，请稍候...',
      key: 'generating',
      duration: 0,
    })

    const res = await generatePattern(formState)

    if (res.data.code === 0 && res.data.data) {
      if (isBatchMode.value) {
        // 组图模式：显示提示信息
        message.success({
          content: `组图生成成功！共生成 ${formState.maxImages} 张图案，已全部保存到数据库`,
          key: 'generating',
          duration: 5,
        })

        // 显示第一张图片的预览
        generatedPattern.value = res.data.data

        // 额外提示
        message.info({
          content: `所有 ${formState.maxImages} 张图案均已提交审核，请在管理后台查看全部图案`,
          duration: 5,
        })
      } else {
        // 单图模式：显示预览
        message.success({ content: '图案生成成功！', key: 'generating' })
        generatedPattern.value = res.data.data
        message.info('图案已提交审核，请稍后在“我的作品”中查看')
      }
    } else {
      throw new Error(res.data.message || '生成失败')
    }
  } catch (error: any) {
    console.error('生成图案失败:', error)
    message.error({ content: error.message || '生成失败，请重试', key: 'generating' })
  } finally {
    generating.value = false
  }
}

// 下载图案
const downloadPattern = () => {
  if (generatedPattern.value?.patternUrl) {
    window.open(generatedPattern.value.patternUrl, '_blank')
  }
}

// 查看我的作品
const viewMyPatterns = () => {
  router.push('/my_idea')
}

// 重置表单
const resetForm = () => {
  generatedPattern.value = null
  formState.patternName = ''
  formState.description = ''
  formState.referenceImageUrl = ''
  formState.style = undefined
  formState.season = undefined
  formState.targetAudience = undefined
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 格式化日期时间
const formatDateTime = (date: Date | string) => {
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<style scoped>
#patternGenerationPage {
  padding: 40px 20px;
  min-height: 100vh;
  background-image: url(https://api.imlcd.cn/bg/gq.php);

  .pattern-generation-page {
    .page-header {
      text-align: center;
      margin-bottom: 40px;
      color: white;

      h1 {
        font-size: 48px;
        font-weight: 700;
        margin: 0;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
      }

      .subtitle {
        font-size: 14px;
      }
    }

    .generation-card {
      :deep(.ant-card-body) {
        padding: 20px;
      }
    }

    .section-title {
      font-size: 20px;
    }

    .result-container {
      .action-buttons {
        .ant-btn {
          flex: 1 1 100%;
        }
      }
    }
  }
}
</style>
