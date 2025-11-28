<template>
  <div class="mj-pattern-generation-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🎨 Midjourney 智能图案创作</h1>
      <p class="subtitle">专业服装图案设计，AI 驱动创意无限</p>
    </div>

    <!-- 主内容区域 -->
    <a-card class="generation-card" :bordered="false">
      <!-- 步骤1：输入提示词并生成 -->
      <div v-if="currentStep === 1" class="step-content">
        <h2 class="section-title"><EditOutlined /> 步骤 1：描述你的图案创意</h2>

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
            <div class="tip-text">💡 提示：支持中文输入，AI 会自动翻译并添加服装专业术语</div>
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
              <a-select-option value="写实">写实</a-select-option>
              <a-select-option value="手绘">手绘</a-select-option>
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

          <!-- 提交按钮 -->
          <a-form-item>
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
              {{ generating ? '正在生成中，请耐心等待（约1-2分钟）...' : '开始生成图案' }}
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 生成进度提示 -->
        <div v-if="generating" class="generating-tips">
          <a-spin size="large" />
          <p class="tip-title">🎨 AI 正在创作中...</p>
          <p class="tip-desc">Midjourney 会生成 4 张候选图案（2x2 网格）</p>
          <p class="tip-desc">预计需要 1-2 分钟，请耐心等待</p>
        </div>
      </div>

      <!-- 步骤2：选择图片并执行操作 -->
      <div v-if="currentStep === 2" class="step-content">
        <h2 class="section-title"><PictureOutlined /> 步骤 2：选择喜欢的图案</h2>

        <!-- 显示生成的 2x2 网格图片 -->
        <div class="grid-preview">
          <a-image
            :src="mjResponse.imageUrl"
            :preview="{
              src: mjResponse.rawImageUrl,
            }"
            class="grid-image"
          />
          <div class="grid-info">
            <p><strong>图案描述：</strong>{{ originalPrompt }}</p>
            <p v-if="formState.style"><strong>风格：</strong>{{ formState.style }}</p>
            <p v-if="formState.season"><strong>季节：</strong>{{ formState.season }}</p>
            <p v-if="formState.targetAudience">
              <strong>受众：</strong>{{ formState.targetAudience }}
            </p>
            <p><strong>任务ID：</strong>{{ mjResponse.taskId }}</p>
            <p><strong>图片ID：</strong>{{ mjResponse.imageId }}</p>
          </div>
        </div>

        <!-- 操作选择 -->
        <div class="action-selection">
          <h3>选择你喜欢的图案位置：</h3>
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
            >
              <template #icon>
                <ThunderboltOutlined v-if="!executing" />
              </template>
              {{ executing ? '执行中...' : '执行操作' }}
            </a-button>
          </div>
        </div>
      </div>

      <!-- 步骤3：确认并保存 -->
      <div v-if="currentStep === 3" class="step-content">
        <h2 class="section-title"><CheckOutlined /> 步骤 3：确认并保存</h2>

        <!-- 显示最终结果 -->
        <div class="final-preview">
          <a-image
            :src="finalResult.imageUrl"
            :preview="{
              src: finalResult.rawImageUrl,
            }"
            class="final-image"
          />
          <div class="final-info">
            <p><strong>操作：</strong>{{ getActionName(selectedAction!) }}</p>
            <p><strong>图案描述：</strong>{{ originalPrompt }}</p>
            <p v-if="formState.style"><strong>风格：</strong>{{ formState.style }}</p>
            <p v-if="formState.season"><strong>季节：</strong>{{ formState.season }}</p>
            <p v-if="formState.targetAudience">
              <strong>受众：</strong>{{ formState.targetAudience }}
            </p>
          </div>
        </div>

        <!-- 图案信息表单 -->
        <a-form :model="saveForm" layout="vertical">
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

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <a-button size="large" @click="backToStep2"> <LeftOutlined /> 返回重新选择 </a-button>
            <a-button type="default" size="large" @click="continueWithoutSaving">
              继续操作（不保存）
            </a-button>
            <a-button
              type="primary"
              size="large"
              :loading="saving"
              :disabled="!saveForm.patternName || saving"
              @click="saveToDatabase"
            >
              <template #icon>
                <SaveOutlined v-if="!saving" />
              </template>
              {{ saving ? '保存中...' : '确认保存' }}
            </a-button>
          </div>
        </a-form>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  PictureOutlined,
  ThunderboltOutlined,
  CheckOutlined,
  LeftOutlined,
  SaveOutlined,
} from '@ant-design/icons-vue'
import { imagine, executeAction as mjExecuteAction, savePattern } from '@/api/midjourneyjiekou'
import { useRouter } from 'vue-router'

const router = useRouter()

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

// MJ 响应数据
const mjResponse = ref<any>(null)
const finalResult = ref<any>(null)
const originalPrompt = ref('')

// 选中的操作
const selectedAction = ref<string | null>(null)

// 步骤1：生成图片
const handleGenerate = async () => {
  try {
    generating.value = true

    // 保存原始提示词（用于显示）
    originalPrompt.value = formState.prompt

    message.loading({
      content: 'Midjourney 正在生成图案，请耐心等待...',
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

// 执行操作
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
      finalResult.value = res.data.data

      // 自动填充图案名称
      saveForm.patternName = `MJ-${originalPrompt.value.substring(0, 20)}-${selectedAction.value}`

      currentStep.value = 3
      message.success({
        content: '操作执行成功！请确认并保存',
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

// 继续操作（不保存）
const continueWithoutSaving = () => {
  // 将当前结果作为新的起点
  mjResponse.value = finalResult.value
  selectedAction.value = null
  currentStep.value = 2
  message.info('可以继续对当前图片执行操作')
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
    upsample1: '放大图片1',
    upsample2: '放大图片2',
    upsample3: '放大图片3',
    upsample4: '放大图片4',
    variation1: '变体图片1',
    variation2: '变体图片2',
    variation3: '变体图片3',
    variation4: '变体图片4',
    reroll: '重新生成',
  }
  return actionMap[action] || action
}
</script>

<style scoped>
.mj-pattern-generation-page {
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
    max-width: 1200px;
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

  .tip-text {
    margin-top: 8px;
    font-size: 13px;
    color: #8c8c8c;
    line-height: 1.6;
  }

  .generating-tips {
    text-align: center;
    padding: 60px 20px;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 12px;
    margin-top: 24px;

    .tip-title {
      margin-top: 20px;
      font-size: 20px;
      font-weight: 600;
      color: #1f1f1f;
    }

    .tip-desc {
      margin-top: 10px;
      font-size: 14px;
      color: #8c8c8c;
    }
  }

  .grid-preview {
    margin-bottom: 32px;

    .grid-image {
      width: 100%;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }

    .grid-info {
      margin-top: 16px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;

      p {
        margin: 8px 0;
        font-size: 14px;
        color: #595959;
      }
    }
  }

  .action-selection {
    .action-group {
      padding: 20px;
      background: #fafafa;
      border-radius: 8px;
      margin-bottom: 16px;

      h4 {
        margin: 0 0 12px 0;
        font-size: 16px;
        font-weight: 600;
        color: #1f1f1f;
      }
    }
  }

  .final-preview {
    margin-bottom: 32px;

    .final-image {
      width: 100%;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }

    .final-info {
      margin-top: 16px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;

      p {
        margin: 8px 0;
        font-size: 14px;
        color: #595959;
      }
    }
  }

  .action-buttons {
    margin-top: 24px;
    display: flex;
    gap: 12px;
    justify-content: flex-end;

    .ant-btn {
      min-width: 120px;
    }

    .ant-btn-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
    }
  }
}

@media (max-width: 768px) {
  .mj-pattern-generation-page {
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

    .action-buttons {
      flex-direction: column;

      .ant-btn {
        width: 100%;
      }
    }
  }
}
</style>
