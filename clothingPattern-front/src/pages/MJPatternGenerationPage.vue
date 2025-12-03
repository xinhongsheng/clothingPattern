<template>
  <div class="mj-pattern-generation-page">

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

      <!-- 步骤3：确认并保存 -->
      <div v-if="currentStep === 3" class="step-content">
        <h2 class="section-title"><CheckOutlined /> 步骤 3：确认并保存</h2>

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
            <a-button type="default" size="large" @click="continueWithoutSaving" class="secondary-action-btn">
              继续操作（不保存）
            </a-button>
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
/* 全局基础样式 */
.mj-pattern-generation-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
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

.page-header .subtitle {
  font-size: 18px;
  margin: 0;
  opacity: 0.85;
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
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
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 20px;
  box-shadow: 0 20px 80px rgba(0, 0, 0, 0.3);
  background-color: #ffffff;
  overflow: hidden;
}

:deep(.ant-card-body) {
  padding: 40px;
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

.section-title {
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.section-title .anticon {
  color: #667eea;
  font-size: 28px;
}

/* 表单元素样式优化 */
:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-form-item-label > label) {
  font-size: 16px;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 8px;
}

:deep(.ant-input-lg),
:deep(.ant-select-lg .ant-select-selector),
:deep(.ant-input-textarea-lg) {
  border-radius: 12px !important;
  font-size: 16px;
  padding: 12px 16px;
  border-color: #e2e8f0;
  transition: all 0.3s;
}

:deep(.ant-input-lg:focus),
:deep(.ant-select-lg.ant-select-focused .ant-select-selector),
:deep(.ant-input-textarea-lg:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
  outline: none;
}

:deep(.ant-input-textarea-lg) {
  min-height: 140px !important;
  resize: vertical;
}

:deep(.ant-input-count) {
  font-size: 12px;
  color: #a0aec0;
}

/* 提示文字 */
.tip-text {
  margin-top: 8px;
  font-size: 13px;
  color: #718096;
  line-height: 1.6;
  background-color: #f7fafc;
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
}

/* 生成按钮样式 */
:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px !important;
  font-size: 16px;
  font-weight: 500;
  height: 56px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #7486e0 0%, #8561b2 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

:deep(.ant-btn-primary:active) {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

:deep(.ant-btn-primary:disabled) {
  background: linear-gradient(135deg, #a5b4fc 0%, #c5b4e3 100%);
  transform: none;
  box-shadow: none;
}

/* 生成进度提示 */
.generating-tips {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  margin-top: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.generating-tips .ant-spin {
  font-size: 48px;
  color: #667eea;
}

.tip-title {
  margin-top: 24px;
  font-size: 22px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.tip-desc {
  margin-top: 8px;
  font-size: 15px;
  color: #718096;
  line-height: 1.8;
}

/* 图片预览区域 */
.grid-preview, .final-preview {
  margin-bottom: 36px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.image-container {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transition: all 0.3s;
}

.image-container:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  transform: translateY(-2px);
}

.grid-image, .final-image {
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
.grid-info, .final-info {
  padding: 24px;
  background: #f8f9fa;
  border-radius: 16px;
  border: 1px solid #f0f2f5;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
}

.grid-info p, .final-info p {
  margin: 10px 0;
  font-size: 15px;
  color: #2d3748;
  line-height: 1.6;
}

.grid-info p strong, .final-info p strong {
  color: #1a202c;
  margin-right: 8px;
}

/* 操作选择区域 */
.action-selection {
  margin-top: 8px;
}

.action-selection h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-group {
  padding: 24px;
  background: #f8f9fa;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid #f0f2f5;
  transition: all 0.3s;
}

.action-group:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border-color: #e8eaf6;
}

.action-group h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 操作按钮样式 */
.action-btn {
  border-radius: 8px !important;
  font-size: 14px !important;
  padding: 8px 16px !important;
  transition: all 0.2s !important;
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
  margin-top: 32px;
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

:deep(.action-buttons .ant-btn) {
  border-radius: 12px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
  height: 52px !important;
  padding: 0 24px !important;
  min-width: 140px !important;
  transition: all 0.3s !important;
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
    padding: 30px 16px;
  }

  :deep(.ant-card-body) {
    padding: 30px;
  }

  .section-title {
    font-size: 24px;
    margin-bottom: 28px;
  }
}

@media (max-width: 768px) {
  .mj-pattern-generation-page {
    padding: 20px 10px;
  }

  .page-header h1 {
    font-size: 32px;
  }

  .page-header .subtitle {
    font-size: 15px;
  }

  .step-indicator {
    margin-bottom: 24px;
  }

  .step-number {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .step-label {
    font-size: 12px;
  }

  .step-divider {
    margin: 0 8px;
  }

  :deep(.ant-card-body) {
    padding: 20px;
  }

  .section-title {
    font-size: 20px;
    margin-bottom: 24px;
    padding-bottom: 12px;
  }

  .section-title .anticon {
    font-size: 24px;
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

  .generating-tips {
    padding: 40px 16px;
  }

  .tip-title {
    font-size: 18px;
  }

  .tip-desc {
    font-size: 14px;
  }

  .grid-info, .final-info {
    padding: 18px;
  }

  .grid-info p, .final-info p {
    font-size: 14px;
    margin: 8px 0;
  }

  .action-group {
    padding: 18px;
    margin-bottom: 16px;
  }

  .action-selection h3 {
    font-size: 16px;
    margin-bottom: 16px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 12px;
    margin-top: 24px;
  }

  :deep(.action-buttons .ant-btn) {
    width: 100% !important;
    min-width: auto !important;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 28px;
  }

  .step-label {
    display: none;
  }

  .action-btn {
    width: 100% !important;
    margin-bottom: 8px !important;
  }

  :deep(.ant-space-wrap) {
    display: flex !important;
    flex-direction: column !important;
  }
}

/* 加载状态优化 */
:deep(.ant-spin-dot-item) {
  background-color: #667eea !important;
}

:deep(.ant-btn-loading .anticon-loading) {
  color: rgba(255, 255, 255, 0.8) !important;
}
</style>
