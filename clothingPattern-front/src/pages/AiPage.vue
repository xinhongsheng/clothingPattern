<template>
  <div id="aiPage">
    <!-- 顶部标题区域 -->
    <!-- <div class="header-section">
      <div class="title-area">
        <h1 class="page-title">
          <BulbOutlined class="title-icon" />
          AI 服装设计顾问
        </h1>
        <p class="page-subtitle">专业的服装图案知识问答，为您的创作提供灵感与指导</p>
      </div>
    </div> -->

    <!-- 主要内容区域 -->
    <div class="content-wrapper">
      <!-- 左侧：常见问题 -->
      <div class="sidebar">
        <a-card title="💡 常见问题" :bordered="false" class="questions-card">
          <a-spin :spinning="questionsLoading">
            <div class="questions-list">
              <div
                v-for="(q, index) in commonQuestions"
                :key="index"
                class="question-item"
                @click="selectQuestion(q)"
              >
                <QuestionCircleOutlined class="question-icon" />
                <span class="question-text">{{ q }}</span>
              </div>
            </div>
          </a-spin>
        </a-card>

        <!-- 使用提示 -->
        <a-card title="📖 使用提示" :bordered="false" class="tips-card">
          <div class="tips-content">
            <p><CheckCircleOutlined /> 支持纯文本问答</p>
            <p><CheckCircleOutlined /> 支持图片分析</p>
            <p><CheckCircleOutlined /> 流式实时回答</p>
            <p><CheckCircleOutlined /> 专业设计建议</p>
          </div>
        </a-card>
      </div>

      <!-- 右侧：聊天区域 -->
      <div class="chat-container">
        <a-card :bordered="false" class="chat-card">
          <!-- 对话历史 -->
          <div class="messages-area" ref="messagesAreaRef">
            <a-empty
              v-if="messages.length === 0"
              description="开始您的第一个问题吧～"
              class="empty-state"
            >
              <template #image>
                <CommentOutlined :style="{ fontSize: '64px', color: '#bfbfbf' }" />
              </template>
            </a-empty>

            <div v-else class="messages-list">
              <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
                <!-- 用户消息 -->
                <div v-if="msg.role === 'user'" class="message user-message">
                  <div class="message-content">
                    <div class="message-text">{{ msg.content }}</div>
                    <a-image
                      v-if="msg.imageUrl"
                      :src="msg.imageUrl"
                      :width="200"
                      class="message-image"
                    />
                  </div>
                  <a-avatar class="message-avatar" :style="{ backgroundColor: '#1890ff' }">
                    <template #icon><UserOutlined /></template>
                  </a-avatar>
                </div>

                <!-- AI 回答 -->
                <div v-else class="message ai-message">
                  <a-avatar class="message-avatar" :style="{ backgroundColor: '#52c41a' }">
                    <template #icon><RobotOutlined /></template>
                  </a-avatar>
                  <div class="message-content">
                    <div class="message-text" v-html="formatMarkdown(msg.content)"></div>
                    <a-spin v-if="msg.loading" class="message-loading" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <a-space direction="vertical" style="width: 100%" :size="12">
              <!-- 图片上传（可选） -->
              <div v-if="uploadedImageUrl" class="uploaded-image-preview">
                <a-image :src="uploadedImageUrl" :width="100" />
                <a-button
                  type="text"
                  danger
                  size="small"
                  @click="clearImage"
                  class="remove-image-btn"
                >
                  <CloseCircleOutlined /> 移除图片
                </a-button>
              </div>

              <!-- 输入框 -->
              <a-textarea
                v-model:value="currentQuestion"
                placeholder="请输入您的问题，例如：什么是波点图案？如何在服装设计中应用？"
                :auto-size="{ minRows: 3, maxRows: 6 }"
                :maxlength="1000"
                show-count
                @pressEnter="handleSend"
              />

              <!-- 操作按钮 -->
              <a-space class="action-buttons">
                <a-upload
                  :before-upload="handleImageUpload"
                  :show-upload-list="false"
                  accept="image/*"
                >
                  <a-button :disabled="loading">
                    <PictureOutlined /> 上传图片
                  </a-button>
                </a-upload>

                <a-button type="primary" @click="handleSend" :loading="loading" size="large">
                  <SendOutlined /> 发送
                </a-button>

                <a-button @click="clearChat" :disabled="loading">
                  <ClearOutlined /> 清空对话
                </a-button>
              </a-space>
            </a-space>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  BulbOutlined,
  QuestionCircleOutlined,
  CheckCircleOutlined,
  CommentOutlined,
  UserOutlined,
  RobotOutlined,
  SendOutlined,
  PictureOutlined,
  ClearOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue'
import { getCommonQuestions } from '@/api/aiController'

// 消息接口
interface Message {
  role: 'user' | 'ai'
  content: string
  imageUrl?: string
  loading?: boolean
}

// 常见问题
const commonQuestions = ref<string[]>([])
const questionsLoading = ref(false)

// 对话消息
const messages = ref<Message[]>([])
const currentQuestion = ref('')
const uploadedImageUrl = ref('')
const loading = ref(false)
const messagesAreaRef = ref<HTMLElement>()

// 获取常见问题
const fetchCommonQuestions = async () => {
  questionsLoading.value = true
  try {
    const res = await getCommonQuestions()
    if (res.data.code === 0 && res.data.data) {
      commonQuestions.value = res.data.data
    }
  } catch (error: any) {
    console.error('获取常见问题失败:', error)
  } finally {
    questionsLoading.value = false
  }
}

// 选择常见问题
const selectQuestion = (question: string) => {
  currentQuestion.value = question
}

// 处理图片上传
const handleImageUpload = (file: File) => {
  // 限制文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过 5MB')
    return false
  }

  // 转换为 base64
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImageUrl.value = e.target?.result as string
    message.success('图片上传成功')
  }
  reader.readAsDataURL(file)
  return false // 阻止自动上传
}

// 清除图片
const clearImage = () => {
  uploadedImageUrl.value = ''
}

// 发送消息
const handleSend = async (e?: Event) => {
  // 如果是按 Enter 键且有 Shift，则换行
  if (e && (e as KeyboardEvent).shiftKey) {
    return
  }
  e?.preventDefault()

  const question = currentQuestion.value.trim()
  if (!question) {
    message.warning('请输入问题')
    return
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: question,
    imageUrl: uploadedImageUrl.value,
  })

  // 添加 AI 消息占位
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'ai',
    content: '',
    loading: true,
  })

  // 清空输入
  const imageUrl = uploadedImageUrl.value
  currentQuestion.value = ''
  uploadedImageUrl.value = ''
  loading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 使用 fetch 调用流式接口（axios 不支持 SSE）
    const response = await fetch('http://localhost:8123/api/ai/ask/stream', {
      //  const response = await fetch('http://bs.xinxiangyang.work/ai/ask/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({
        question,
        imageUrl: imageUrl || undefined,
      }),
    })

    if (!response.ok || !response.body) {
      throw new Error('网络请求失败')
    }

    // 读取流式响应
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let done = false

    // 移除 loading 状态
    const aiMessage = messages.value[aiMessageIndex]
    if (aiMessage) {
      aiMessage.loading = false
    }

    while (!done) {
      const { value, done: streamDone } = await reader.read()
      done = streamDone

      if (value) {
        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5).trim()
            if (data && data !== '[DONE]') {
              // 追加到 AI 消息内容
              const currentMessage = messages.value[aiMessageIndex]
              if (currentMessage) {
                currentMessage.content += data
              }
              // 实时滚动到底部
              await nextTick()
              scrollToBottom()
            }
          }
        }
      }
    }

    // 如果没有收到任何内容
    const finalMessage = messages.value[aiMessageIndex]
    if (finalMessage && !finalMessage.content) {
      finalMessage.content = '请先登录以使用 AI 服装设计顾问功能。'
    }
  } catch (error: any) {
    messages.value[aiMessageIndex] = {
      role: 'ai',
      content: '抱歉，网络连接出现问题，请稍后再试。',
      loading: false,
    }
    message.error('请求失败：' + error.message)
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 清空对话
const clearChat = () => {
  messages.value = []
  currentQuestion.value = ''
  uploadedImageUrl.value = ''
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesAreaRef.value) {
    messagesAreaRef.value.scrollTop = messagesAreaRef.value.scrollHeight
  }
}

// 格式化 Markdown（增强处理）
const formatMarkdown = (text: string) => {
  if (!text) return ''
  return text
    // 换行处理
    .replace(/\n/g, '<br>')
    // 标题处理
    .replace(/###\s+(.*?)(<br>|$)/g, '<h3 class="ai-h3">$1</h3>')
    .replace(/####\s+(.*?)(<br>|$)/g, '<h4 class="ai-h4">$1</h4>')
    .replace(/##\s+(.*?)(<br>|$)/g, '<h2 class="ai-h2">$1</h2>')
    // 粗体
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/__(.*?)__/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/_(.*?)_/g, '<em>$1</em>')
    // 列表项（带图标）
    .replace(/-\s+/g, '<span class="list-icon">•</span> ')
    .replace(/\d+\.\s+/g, (match) => `<span class="list-number">${match}</span>`)
    // Emoji 和特殊符号处理
    .replace(/✅/g, '<span class="icon-success">✅</span>')
    .replace(/❌/g, '<span class="icon-error">❌</span>')
    .replace(/▶/g, '<span class="icon-arrow">▶</span>')
    // 引用块
    .replace(/>(.*?)(<br>|$)/g, '<blockquote class="ai-quote">$1</blockquote>')
    // 分隔线
    .replace(/---/g, '<hr class="ai-divider">')
}

// 页面加载
onMounted(() => {
  fetchCommonQuestions()
})
</script>

<style scoped>
#aiPage {
  min-height: 100vh;
  background-image: url('@/assets/backgroundImage/ai-bg.png');
  padding: 24px;
}

.header-section {
  margin-bottom: 24px;
}

.title-area {
  text-align: center;
  color: white;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;

  .title-icon {
    font-size: 40px;
  }
}

.page-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.questions-card,
.tips-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f5f5;

  &:hover {
    background: #e6f7ff;
    transform: translateX(4px);
  }

  .question-icon {
    color: #1890ff;
    margin-top: 2px;
    flex-shrink: 0;
  }

  .question-text {
    font-size: 14px;
    line-height: 1.5;
    color: #333;
  }
}

.tips-content {
  p {
    margin: 8px 0;
    color: #666;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

/* 聊天容器 */
.chat-container {
  .chat-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    height: calc(100vh - 180px);
    display: flex;
    flex-direction: column;

    :deep(.ant-card-body) {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 0;
      height: 100%;
    }
  }
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #fafafa;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;
  }
}

.empty-state {
  margin-top: 100px;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-wrapper {
  display: flex;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;

  &.user-message {
    margin-left: auto;
    flex-direction: row-reverse;

    .message-content {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 18px 18px 4px 18px;
    }
  }

  &.ai-message {
    .message-content {
      background: white;
      color: #333;
      border-radius: 18px 18px 18px 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }
  }
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  padding: 12px 16px;
  position: relative;
}

.message-text {
  line-height: 1.8;
  word-wrap: break-word;
  font-size: 15px;

  /* Markdown 样式 */
  :deep(.ai-h2) {
    font-size: 20px;
    font-weight: 700;
    margin: 16px 0 12px 0;
    color: #1890ff;
    border-bottom: 2px solid #e6f7ff;
    padding-bottom: 8px;
  }

  :deep(.ai-h3) {
    font-size: 18px;
    font-weight: 600;
    margin: 14px 0 10px 0;
    color: #333;
  }

  :deep(.ai-h4) {
    font-size: 16px;
    font-weight: 600;
    margin: 12px 0 8px 0;
    color: #555;
  }

  :deep(strong) {
    font-weight: 600;
    color: #262626;
  }

  :deep(em) {
    font-style: italic;
    color: #595959;
  }

  :deep(.list-icon) {
    color: #1890ff;
    margin-right: 8px;
    font-weight: bold;
  }

  :deep(.list-number) {
    color: #1890ff;
    font-weight: 600;
    margin-right: 4px;
  }

  :deep(.icon-success) {
    color: #52c41a;
    margin-right: 4px;
  }

  :deep(.icon-error) {
    color: #ff4d4f;
    margin-right: 4px;
  }

  :deep(.icon-arrow) {
    color: #1890ff;
    margin-right: 4px;
  }

  :deep(.ai-quote) {
    border-left: 4px solid #1890ff;
    padding-left: 12px;
    margin: 12px 0;
    color: #595959;
    font-style: italic;
    background: #f0f5ff;
    padding: 8px 12px;
    border-radius: 4px;
  }

  :deep(.ai-divider) {
    border: none;
    border-top: 1px dashed #d9d9d9;
    margin: 16px 0;
  }
}

.message-image {
  margin-top: 8px;
  border-radius: 8px;
}

.message-loading {
  margin-top: 8px;
}

/* 输入区域 */
.input-area {
  padding: 24px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.uploaded-image-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 8px;
}

.remove-image-btn {
  margin-left: auto;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: 2;
  }

  .chat-container {
    order: 1;
  }
}
</style>
