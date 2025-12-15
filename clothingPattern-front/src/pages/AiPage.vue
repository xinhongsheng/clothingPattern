<template>
  <div id="aiPage" :class="{ 'is-mobile': isMobile }">
    <div v-if="isMobile" class="mobile-header">
      <div class="logo-area">
        <BulbFilled class="logo-icon" />
        <span>Fashion AI</span>
      </div>
      <a-button type="text" @click="drawerVisible = true">
        <MenuOutlined style="font-size: 20px" />
      </a-button>
    </div>

    <div class="sidebar-container" v-show="!isMobile">
      <div class="sidebar-header">
        <div class="app-logo">
          <BulbFilled class="logo-icon" />
          <span class="title-text">Fashion AI</span>
        </div>
        <div class="role-selector">
          <div
            v-for="role in roles"
            :key="role.id"
            class="role-item"
            :class="{ active: currentRole === role.id }"
            @click="handleRoleSwitch(role.id)"
            :title="role.name"
          >
            {{ role.icon }}
          </div>
        </div>
      </div>

      <div class="sidebar-content">
        <div class="section-label">灵感快捷键</div>
        <div class="inspiration-list">
          <div
            v-for="(item, index) in commonQuestions"
            :key="index"
            class="inspire-item"
            @click="selectQuestion(item)"
          >
            <ThunderboltOutlined class="icon-small" />
            <span class="text-truncate">{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="user-info">
          <a-avatar size="small" :style="{ backgroundColor: '#00d2ff' }">U</a-avatar>
          <span>设计师</span>
        </div>
      </div>
    </div>

    <a-drawer
      v-model:open="drawerVisible"
      placement="left"
      :closable="false"
      width="240px"
      :bodyStyle="{ padding: 0, background: '#1a1a2e' }"
    >
      <div class="drawer-menu">
        <h3>切换角色</h3>
        <div class="role-list-mobile">
          <div
            v-for="role in roles"
            :key="role.id"
            class="mobile-role-item"
            @click="handleMobileRoleSelect(role.id)"
          >
            <span>{{ role.icon }} {{ role.name }}</span>
          </div>
        </div>
      </div>
    </a-drawer>

    <div class="main-chat-area">
      <div class="chat-header">
        <div class="current-role-tag">
          <span class="role-icon">{{ getRoleIcon(currentRole) }}</span>
          正在与 <strong>{{ getRoleName(currentRole) }}</strong> 对话
        </div>
        <a-button type="text" size="small" @click="clearChat" title="清空对话">
          <DeleteOutlined />
        </a-button>
      </div>

      <div class="messages-container" ref="messagesAreaRef">
        <template v-if="messages.length === 0">
          <div class="empty-state">
            <div class="empty-icon">🎨</div>
            <h3>AI 服装设计灵感</h3>
            <p>输入关键词或上传图片，开始创作。</p>
            <div class="tags-row">
              <span class="tag" @click="selectQuestion('2025春夏流行色')">2025流行色</span>
              <span class="tag" @click="selectQuestion('新中式风格面料推荐')">新中式面料</span>
              <span class="tag" @click="selectQuestion('生成赛博朋克风格图案')">赛博朋克图案</span>
            </div>
          </div>
        </template>

        <div v-else class="message-list">
          <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
            <div class="avatar-col">
              <a-avatar :size="32" :src="msg.role === 'user' ? userAvatar : aiAvatar" />
            </div>
            <div class="content-col">
              <div class="bubble">
                <a-image v-if="msg.imageUrl" :src="msg.imageUrl" class="chat-img" :width="150" />
                <div class="text markdown-body" v-html="formatMarkdown(msg.content)"></div>
              </div>
              <div v-if="msg.loading" class="loading-dots">
                <span>.</span><span>.</span><span>.</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-wrapper">
        <div v-if="uploadedImageUrl" class="image-preview-mini">
          <img :src="uploadedImageUrl" />
          <div class="del-btn" @click="clearImage"><CloseOutlined /></div>
        </div>

        <div class="input-box glass-panel">
          <a-upload :before-upload="handleImageUpload" :show-upload-list="false" accept="image/*">
            <a-button type="text" class="icon-btn">
              <PictureOutlined />
            </a-button>
          </a-upload>

          <a-textarea
            v-model:value="currentQuestion"
            placeholder="提问..."
            :auto-size="{ minRows: 1, maxRows: 3 }"
            @pressEnter="handleSend"
            class="chat-input"
          />

          <a-button
            type="primary"
            shape="circle"
            size="middle"
            :loading="loading"
            @click="handleSend"
            class="send-btn"
          >
            <template #icon><SendOutlined /></template>
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  BulbFilled,
  MenuOutlined,
  ThunderboltOutlined,
  PictureOutlined,
  SendOutlined,
  CloseOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { getCommonQuestions } from '@/api/aiController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { useRouter } from 'vue-router'

// --- 基础配置 ---
const router = useRouter()
const loginUserStore = useLoginUserStore()
const isMobile = ref(false)
const drawerVisible = ref(false)
const userAvatar = 'https://api.dicebear.com/7.x/micah/svg?seed=User'
const aiAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=FashionAI'

const roles = [
  { id: 'general', name: '全能顾问', icon: '🧠' },
  { id: 'pattern', name: '图案设计', icon: '💠' },
  { id: 'color', name: '色彩搭配', icon: '🎨' },
  { id: 'fabric', name: '面料工艺', icon: '🧵' },
]
const currentRole = ref('general')

// 🔥 定义不同角色的专属提示词 (Prompt Engineering)
const rolePrompts: Record<string, string> = {
  general:
    '你是一位全能的时尚设计顾问。请用专业但通俗易懂的语言，为用户提供关于服装设计、市场趋势和审美的综合建议。',
  pattern:
    '你是一位资深的服装图案设计师。请专注于图案的构成、纹样历史、文化寓意以及在印花工艺中的实现。请从视觉美学和文化深度的角度回答。',
  color:
    '你是一位专业的色彩搭配专家。请专注于色彩心理学、流行色趋势、色轮理论以及不同肤色与面料的色彩关系。回答时请给出具体的配色方案建议。',
  fabric:
    '你是一位精通面料与工艺的纺织专家。请专注于面料的成分、织造结构、物理特性（垂感、透气性等）以及剪裁缝制工艺。请给由于具体的面料选择和处理建议。',
}

// --- 数据接口 ---
interface Message {
  role: 'user' | 'ai'
  content: string
  imageUrl?: string
  loading?: boolean
}

const commonQuestions = ref<string[]>([
  '2024年流行趋势',
  '波点图案应用',
  '丝绸面料特性',
  '极简风格配色',
])
const messages = ref<Message[]>([])
const currentQuestion = ref('')
const uploadedImageUrl = ref('')
const loading = ref(false)
const messagesAreaRef = ref<HTMLElement>()

// --- 方法逻辑 ---
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}
const getRoleName = (id: string) => roles.find((r) => r.id === id)?.name
const getRoleIcon = (id: string) => roles.find((r) => r.id === id)?.icon

const selectQuestion = (q: string) => {
  currentQuestion.value = q
  if (isMobile.value) drawerVisible.value = false
}

// 桌面端切换角色
const handleRoleSwitch = (id: string) => {
  if (currentRole.value !== id) {
    currentRole.value = id
    // 可选：切换角色时清空对话，避免上下文混淆
    // messages.value = []
    message.success(`已切换为：${getRoleName(id)}`)
  }
}

// 移动端切换角色
const handleMobileRoleSelect = (id: string) => {
  if (currentRole.value !== id) {
    currentRole.value = id
    message.success(`已切换为：${getRoleName(id)}`)
  }
  drawerVisible.value = false
}

const clearChat = () => {
  messages.value = []
}

const handleImageUpload = (file: File) => {
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片太大，请限制在 5MB 以内')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadedImageUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

const clearImage = () => {
  uploadedImageUrl.value = ''
}

// --- 核心：发送消息 ---
const handleSend = async (e?: Event) => {
  if (e && (e as KeyboardEvent).shiftKey) return
  e?.preventDefault()

  if (!loginUserStore.loginUser.id) {
    message.warning('请登录后再使用')
    router.push('/user/login')
    return
  }

  const originalQuestion = currentQuestion.value.trim()
  if (!originalQuestion && !uploadedImageUrl.value) return

  // 1. UI 显示：只显示用户的原始问题
  messages.value.push({ role: 'user', content: originalQuestion, imageUrl: uploadedImageUrl.value })

  // 2. 添加 AI 正在输入的占位符
  messages.value.push({ role: 'ai', content: '', loading: true })

  // 3. 暂存数据并清空输入框
  const imgData = uploadedImageUrl.value
  currentQuestion.value = ''
  uploadedImageUrl.value = ''
  loading.value = true
  scrollToBottom()

  // 4. 构建带角色设定的 Prompt
  const systemInstruction = rolePrompts[currentRole.value] || rolePrompts.general

  const finalPrompt = `
    【角色设定】
    ${systemInstruction}

    【用户问题】
    ${originalQuestion}
  `.trim()

  try {
    // 5. 发起 Fetch 请求
    // const response = await fetch('http://localhost:8123/api/ai/ask/stream', {
    const response = await fetch('http://bs.xinxiangyang.work/api/ai/ask/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },

      // 🔥 关键配置：携带 Session Cookie，解决“未登录”错误
      credentials: 'include',

      body: JSON.stringify({
        question: finalPrompt, // 发送处理后的 Prompt
        imageUrl: imgData,
      }),
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()
    const aiMsgIndex = messages.value.length - 1

    messages.value[aiMsgIndex].loading = false

    if (reader) {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const text = decoder.decode(value, { stream: true })
        const lines = text.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const content = line.slice(5).trim()
            if (content && content !== '[DONE]') {
              messages.value[aiMsgIndex].content += content
              scrollToBottom()
            }
          }
        }
      }
    }
  } catch (err: any) {
    console.error(err)
    const lastMsg = messages.value[messages.value.length - 1]
    lastMsg.loading = false
    lastMsg.content += '\n\n[系统提示] 网络请求失败，请检查登录状态或后台服务。'
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesAreaRef.value) {
      messagesAreaRef.value.scrollTop = messagesAreaRef.value.scrollHeight
    }
  })
}

// 简单的 Markdown 格式化
const formatMarkdown = (text: string) => {
  if (!text) return ''
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>') // 粗体
    .replace(
      /`(.*?)`/g,
      '<code style="background:rgba(255,255,255,0.1);padding:2px 4px;border-radius:3px;">$1</code>',
    ) // 行内代码
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)

  getCommonQuestions()
    .then((res) => {
      if (res.data && res.data.code === 0 && res.data.data) {
        commonQuestions.value = res.data.data.slice(0, 5)
      }
    })
    .catch((err) => {
      console.warn('获取常见问题失败，使用默认值', err)
    })
})

onUnmounted(() => window.removeEventListener('resize', checkMobile))
</script>

<style scoped lang="less">
/* --- 全局变量与 Reset --- */
@bg-dark: #12121c;
@bg-panel: #1e1e2d;
@accent: #00d2ff;
@text-main: #e0e0e0;
@text-sub: #8a8a9a;
@border: 1px solid rgba(255, 255, 255, 0.08);

* {
  box-sizing: border-box;
}

#aiPage {
  height: 100vh;
  width: 100vw;
  background: @bg-dark;
  color: @text-main;
  display: flex;
  overflow: hidden;
  font-family:
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    'Helvetica Neue',
    Arial,
    sans-serif;
}

/* --- 侧边栏 --- */
.sidebar-container {
  width: 240px;
  background: @bg-panel;
  border-right: @border;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: @border;
}

.app-logo {
  font-size: 18px;
  font-weight: bold;
  color: white;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  .logo-icon {
    color: @accent;
  }
}

.role-selector {
  display: flex;
  justify-content: space-between;
  background: rgba(0, 0, 0, 0.2);
  padding: 4px;
  border-radius: 8px;
}

.role-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  &.active {
    background: @accent;
    color: #000;
    box-shadow: 0 2px 8px rgba(0, 210, 255, 0.4);
  }
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.section-label {
  font-size: 12px;
  color: @text-sub;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.inspire-item {
  padding: 10px;
  border-radius: 6px;
  color: @text-sub;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 4px;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
    color: @accent;
  }
  .icon-small {
    font-size: 12px;
  }
  .text-truncate {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: @border;
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    font-weight: 500;
  }
}

/* --- 主聊天区 (关键布局) --- */
.main-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%; /* 占满高度 */
  position: relative;
  background-image: radial-gradient(circle at top right, rgba(0, 210, 255, 0.05), transparent 40%);
}

.chat-header {
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: @border;
  background: rgba(18, 18, 28, 0.8);
  backdrop-filter: blur(10px);
  z-index: 10;

  .current-role-tag {
    font-size: 13px;
    color: @text-sub;
    .role-icon {
      margin-right: 6px;
    }
    strong {
      color: @text-main;
    }
  }
}

/* 消息容器：唯一可滚动的区域 */
.messages-container {
  flex: 1; /* 占据剩余空间 */
  overflow-y: auto; /* 允许内部滚动 */
  padding: 20px;
  scroll-behavior: smooth;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
  }
}

/* 消息气泡 */
.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;

  &.user {
    flex-direction: row-reverse;
  }

  .content-col {
    max-width: 80%;
  }

  .bubble {
    padding: 10px 16px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.6;
    word-break: break-word;
  }
}

.user .bubble {
  background: @accent;
  color: #000;
  border-radius: 12px 12px 2px 12px;
}
.ai .bubble {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px 12px 12px 2px;
}

.chat-img {
  border-radius: 8px;
  margin-bottom: 8px;
  display: block;
}
.loading-dots {
  color: @text-sub;
  font-size: 12px;
  margin-top: 4px;
}

/* 空状态 */
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: @text-sub;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.8;
  }
  h3 {
    color: @text-main;
    margin-bottom: 8px;
    font-size: 20px;
  }
  .tags-row {
    display: flex;
    gap: 8px;
    margin-top: 24px;
    flex-wrap: wrap;
    justify-content: center;
  }
  .tag {
    font-size: 12px;
    padding: 6px 16px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 20px;
    cursor: pointer;
    border: 1px solid transparent;
    transition: all 0.2s;
    &:hover {
      border-color: @accent;
      color: @accent;
      background: rgba(0, 210, 255, 0.1);
    }
  }
}

/* --- 输入区域 (固定底部) --- */
.input-wrapper {
  flex-shrink: 0; /* 禁止被压缩 */
  padding: 20px;
  display: flex;
  justify-content: center;
  position: relative;
  background: linear-gradient(to top, @bg-dark 80%, transparent); /* 渐变遮罩 */
  z-index: 20;
}

.input-box {
  width: 100%;
  max-width: 800px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 8px;
  display: flex;
  align-items: flex-end;
  gap: 8px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.2);
  transition: border-color 0.3s;

  &:focus-within {
    border-color: @accent;
    background: rgba(255, 255, 255, 0.12);
  }
}

.chat-input {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: white !important;
  padding: 6px 0;
  font-size: 15px;
  resize: none;

  &::placeholder {
    color: rgba(255, 255, 255, 0.3);
  }
}

.icon-btn {
  color: @text-sub;
  &:hover {
    color: @text-main;
  }
}
.send-btn {
  flex-shrink: 0;
  background: @accent;
  border-color: @accent;
  box-shadow: 0 2px 10px rgba(0, 210, 255, 0.3);
}

.image-preview-mini {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 800px;
  padding-left: 20px;

  img {
    height: 50px;
    border-radius: 4px;
    border: 2px solid @accent;
  }
  .del-btn {
    position: absolute;
    top: -8px;
    margin-left: -10px;
    display: inline-flex;
    background: #ff4d4f;
    color: white;
    border-radius: 50%;
    width: 20px;
    height: 20px;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 10px;
  }
}

/* 移动端适配 */
.mobile-header {
  height: 50px;
  background: @bg-panel;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-bottom: @border;
  color: white;
  flex-shrink: 0;
}
.is-mobile {
  flex-direction: column;
  .main-chat-area {
    height: calc(100vh - 50px);
  }
}
.mobile-role-item {
  padding: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: #fff;
}

/* Markdown 样式 */
:deep(.markdown-body) {
  p {
    margin: 0;
  }
  ul,
  ol {
    margin: 4px 0 4px 20px;
    padding: 0;
  }
  li {
    margin-bottom: 2px;
  }
  h2,
  h3 {
    margin: 12px 0 8px 0;
    color: @accent;
    font-size: 16px;
  }
}
</style>
