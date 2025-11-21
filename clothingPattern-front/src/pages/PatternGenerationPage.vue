<template>
  <div class="pattern-generation-page">
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
            <h2 class="section-title">
              <EditOutlined /> 创作参数
            </h2>

            <a-form
              :model="formState"
              layout="vertical"
              @finish="handleGenerate"
            >
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

              <!-- 生成方式 -->
              <a-form-item
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

              <!-- 文字描述模式 -->
              <a-form-item
                v-if="formState.generationType === GENERATION_TYPE_ENUM.TEXT_GENERATED"
                label="设计描述"
                name="description"
                :rules="[{ required: formState.generationType === GENERATION_TYPE_ENUM.TEXT_GENERATED, message: '请输入设计描述' }]"
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

              <!-- 参考图片模式 -->
              <a-form-item
                v-if="formState.generationType === GENERATION_TYPE_ENUM.IMAGE_REFERENCED"
                label="参考图片"
                name="referenceImageUrl"
                :rules="[{ required: formState.generationType === GENERATION_TYPE_ENUM.IMAGE_REFERENCED, message: '请输入参考图片URL' }]"
              >
                <a-input
                  v-model:value="formState.referenceImageUrl"
                  placeholder="请输入图片URL地址"
                  size="large"
                />
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
                  <a-form-item label="图片尺寸">
                    <a-radio-group v-model:value="formState.size">
                      <a-radio value="1664*928">1664*928</a-radio>
                      <a-radio value="1472*1140">1472*1140</a-radio>
                      <a-radio value="1328*1328">1328*1328</a-radio>
                      <a-radio value="1140*1472">1140*1472</a-radio>
                      <a-radio value="928*1664">928*1664</a-radio>
                    </a-radio-group>
                  </a-form-item>

                  <a-form-item label="负面提示词">
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
            <h2 class="section-title">
              <EyeOutlined /> 生成预览
            </h2>

            <!-- 生成中状态 -->
            <div v-if="generating" class="generating-placeholder">
              <a-spin size="large" />
              <p class="generating-text">AI正在创作中，请稍候...</p>
              <p class="generating-tip">通常需要30-60秒</p>
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
                  <a-button @click="viewMyPatterns">
                    <FolderOpenOutlined /> 我的作品
                  </a-button>
                  <a-button @click="resetForm">
                    <ReloadOutlined /> 重新创作
                  </a-button>
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
                <img src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIgcng9IjEwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iNDAiIGZpbGw9IiNkOWQ5ZDkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7wn46oPC90ZXh0Pjwvc3ZnPg==" alt="empty" />
              </template>
            </a-empty>
          </div>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  EyeOutlined,
  FileTextOutlined,
  PictureOutlined,
  ThunderboltOutlined,
  DownloadOutlined,
  FolderOpenOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { generatePattern } from '@/api/patternController'
import { useRouter } from 'vue-router'
import {
  AUDIT_STATUS_MAP,
  AUDIT_STATUS_COLOR_MAP,
  GENERATION_TYPE_ENUM
} from '@/constants/pattern'

const router = useRouter()

// 表单状态
const formState = reactive<{
  patternName: string
  generationType: 'TEXT_GENERATED' | 'IMAGE_REFERENCED'
  description: string
  referenceImageUrl: string
  style: string | undefined
  season: string | undefined
  targetAudience: string | undefined
  size: string
  negativePrompt: string
  promptExtend: boolean
}>({
  patternName: '',
  generationType: GENERATION_TYPE_ENUM.TEXT_GENERATED,
  description: '',
  referenceImageUrl: '',
  style: undefined,
  season: undefined,
  targetAudience: undefined,
  size: '1024*1024',
  negativePrompt: '',
  promptExtend: true
})

// 生成状态
const generating = ref(false)
const generatedPattern = ref<any>(null)
const advancedOpen = ref<string[]>([])

// 生成图案
const handleGenerate = async () => {
  try {
    generating.value = true
    message.loading({ content: '正在生成图案，请稍候...', key: 'generating', duration: 0 })

    const res = await generatePattern(formState)
        
    if (res.data.code === 0 && res.data.data) {
      message.success({ content: '图案生成成功！', key: 'generating' })
          
      // 直接使用后端返回的 PatternVO 对象
      generatedPattern.value = res.data.data
          
      message.info('图案已提交审核，请稍后在"我的作品"中查看')
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
  router.push('/my-patterns')
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
    minute: '2-digit'
  })
}
</script>

<style  scoped>
.pattern-generation-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;

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
      font-size: 18px;
      margin-top: 10px;
      opacity: 0.95;
    }
  }

  .generation-card {
    max-width: 1400px;
    margin: 0 auto;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);

    :deep(.ant-card-body) {
      padding: 40px;
    }
  }

  .section-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 24px;
    color: #1f1f1f;
    display: flex;
    align-items: center;
    gap: 10px;

    .anticon {
      color: #667eea;
    }
  }

  .config-section {
    .tip-text {
      margin-top: 8px;
      font-size: 13px;
      color: #8c8c8c;
      line-height: 1.6;
    }

    .mt-2 {
      margin-top: 8px;
    }

    .ml-2 {
      margin-left: 8px;
    }

    .submit-btn-wrapper {
      margin-top: 32px;
      margin-bottom: 0;

      .ant-btn-primary {
        height: 50px;
        font-size: 16px;
        font-weight: 600;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        transition: all 0.3s ease;

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
        }

        &:disabled {
          background: #d9d9d9;
          box-shadow: none;
        }
      }
    }
  }

  .preview-section {
    .generating-placeholder {
      text-align: center;
      padding: 100px 20px;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      border-radius: 12px;

      .generating-text {
        margin-top: 20px;
        font-size: 18px;
        font-weight: 500;
        color: #1f1f1f;
      }

      .generating-tip {
        margin-top: 10px;
        font-size: 14px;
        color: #8c8c8c;
      }
    }

    .result-container {
      .image-preview {
        margin-bottom: 24px;
        border-radius: 12px;
        overflow: hidden;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

        :deep(.ant-image) {
          width: 100%;
          display: block;

          img {
            width: 100%;
            height: auto;
            display: block;
          }
        }
      }

      .result-info {
        .action-buttons {
          margin-top: 16px;
          display: flex;
          gap: 12px;
          flex-wrap: wrap;

          .ant-btn {
            flex: 1;
            min-width: 120px;
          }
        }
      }
    }

    .empty-placeholder {
      padding: 100px 20px;

      :deep(.ant-empty-description) {
        color: #8c8c8c;
        font-size: 14px;
      }
    }
  }
}

@media (max-width: 768px) {
  .pattern-generation-page {
    padding: 20px 10px;

    .page-header {
      h1 {
        font-size: 32px;
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
