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

    <!-- Desktop Sidebar -->
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
          <a-avatar size="small" :style="{ backgroundColor: 'var(--accent-2)' }">U</a-avatar>
          <span>设计师</span>
        </div>
      </div>
    </div>

    <!-- Mobile Drawer -->
    <a-drawer
      v-model:open="drawerVisible"
      placement="left"
      :closable="false"
      width="260px"
      :bodyStyle="{ padding: 0, background: 'transparent' }"
    >
      <div class="drawer-shell">
        <div class="drawer-head">
          <div class="drawer-brand">
            <BulbFilled class="logo-icon" />
            <span>Fashion AI</span>
          </div>
          <div class="drawer-sub">切换角色</div>
        </div>

        <div class="drawer-menu">
          <div class="role-list-mobile">
            <div
              v-for="role in roles"
              :key="role.id"
              class="mobile-role-item"
              :class="{ active: currentRole === role.id }"
              @click="handleMobileRoleSelect(role.id)"
            >
              <span class="emoji">{{ role.icon }}</span>
              <span class="name">{{ role.name }}</span>
            </div>
          </div>

          <div class="drawer-divider"></div>

          <div class="drawer-quick">
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
        </div>
      </div>
    </a-drawer>

    <!-- Main Chat -->
    <div class="main-chat-area">
      <div class="chat-header">
        <div class="current-role-tag">
          <span class="role-icon">{{ getRoleIcon(currentRole) }}</span>
          正在与 <strong>{{ getRoleName(currentRole) }}</strong> 对话
        </div>
        <a-button
          type="text"
          size="small"
          class="header-icon-btn"
          @click="clearChat"
          title="清空对话"
        >
          <DeleteOutlined />
        </a-button>
      </div>

      <!-- Messages (only scrollable area) -->
      <div class="messages-container" ref="messagesAreaRef">
        <template v-if="messages.length === 0">
          <div class="empty-state">
            <div class="empty-card">
              <div class="empty-icon">🎨</div>
              <h3>AI 服装设计灵感</h3>
              <p>输入关键词或上传图片，开始创作。</p>
              <div class="tags-row">
                <span class="tag" @click="selectQuestion('2025春夏流行色')">2025流行色</span>
                <span class="tag" @click="selectQuestion('新中式风格面料推荐')">新中式面料</span>
                <span class="tag" @click="selectQuestion('生成赛博朋克风格图案')"
                  >赛博朋克图案</span
                >
              </div>
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
                <a-image v-if="msg.imageUrl" :src="msg.imageUrl" class="chat-img" :width="170" />
                <div class="text markdown-body">
                  <MdPreview 
                    :modelValue="msg.content" 
                    :showCodeRowNumber="false"
                    previewTheme="default"
                    codeTheme="atom"
                    class="ai-md-preview"
                  />
                </div>
              </div>
              <div v-if="msg.loading" class="loading-dots">
                <span>.</span><span>.</span><span>.</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="input-wrapper">
        <div v-if="uploadedImageUrl" class="image-preview-mini">
          <img :src="uploadedImageUrl" />
          <div class="del-btn" @click="clearImage"><CloseOutlined /></div>
        </div>

        <div class="input-box glass-panel">
          <a-upload :before-upload="handleImageUpload" :show-upload-list="false" accept="image/*">
            <a-button type="text" class="icon-btn" title="上传参考图">
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
            title="发送"
          >
            <template #icon><SendOutlined /></template>
          </a-button>
        </div>

        <div class="input-hint">
          <span class="dot"></span>
          Enter 发送，Shift + Enter 换行
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
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

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
    const response = await fetch('http://localhost:8123/api/ai/ask/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        question: finalPrompt,
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
      let buffer = ''
      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        
        // 保留最后一个不完整的行
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const content = line.slice(5)
            // 只去掉开头的一个空格（SSE格式），保留其他内容
            const cleanContent = content.startsWith(' ') ? content.slice(1) : content
            if (cleanContent === '[DONE]') continue
            // 将后端转义的 \\n 还原为真正的换行符
            const decodedContent = cleanContent.replace(/\\n/g, '\n')
            messages.value[aiMsgIndex].content += decodedContent
            scrollToBottom()
          }
        }
      }
      // 处理剩余的buffer
      if (buffer.startsWith('data:')) {
        const content = buffer.slice(5)
        const cleanContent = content.startsWith(' ') ? content.slice(1) : content
        if (cleanContent && cleanContent !== '[DONE]') {
          // 将后端转义的 \\n 还原为真正的换行符
          const decodedContent = cleanContent.replace(/\\n/g, '\n')
          messages.value[aiMsgIndex].content += decodedContent
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
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700&family=Newsreader:wght@400;600;700&display=swap');

* {
  box-sizing: border-box;
}

#aiPage {
  /* ===== Design Tokens（与首页一致）===== */
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

  height: 100vh;
  width: 100vw;
  overflow: hidden;
  display: flex;
  color: var(--ink);
  font-family: var(--font-body);
  position: relative;
  isolation: isolate;

  background:
    radial-gradient(900px 420px at 8% -10%, rgba(240, 181, 128, 0.4), transparent 65%),
    radial-gradient(800px 380px at 92% 5%, rgba(122, 210, 196, 0.35), transparent 60%),
    linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
}

#aiPage::before {
  content: '';
  position: absolute;
  inset: -10% -20% auto;
  height: 420px;
  background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.7), transparent 65%);
  opacity: 0.75;
  z-index: 0;
  pointer-events: none;
}

#aiPage::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(31, 26, 21, 0.06) 1px, transparent 1px);
  background-size: 22px 22px;
  opacity: 0.35;
  z-index: 0;
  pointer-events: none;
}

/* ====== Sidebar（桌面）====== */
.sidebar-container {
  width: 216px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 14px 14px 12px;
  position: relative;
  z-index: 1;

  background: rgba(255, 255, 255, 0.72);
  border-right: 1px solid var(--stroke);
  backdrop-filter: blur(10px);
  box-shadow: 12px 0 40px rgba(31, 26, 21, 0.06);
  border-top-right-radius: 22px;
  border-bottom-right-radius: 22px;
}

.sidebar-header {
  padding: 10px 10px 12px;
  border-bottom: 1px solid var(--stroke);
}

.app-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;

  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.2px;
  color: var(--ink);

  .logo-icon {
    color: var(--accent-2);
    font-size: 18px;
  }
}

.role-selector {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
  padding: 6px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--stroke);
}

.role-item {
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.25s var(--transition-easing);
  color: var(--muted);
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-1px);
    background: rgba(212, 91, 45, 0.08);
    border-color: rgba(212, 91, 45, 0.18);
    color: var(--accent);
  }

  &.active {
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
    color: #fff;
    border-color: transparent;
    box-shadow: 0 10px 20px rgba(212, 91, 45, 0.25);
  }
}

.sidebar-content {
  flex: 1;
  padding: 12px 8px 8px;
  overflow: hidden;
}

.section-label {
  font-size: 11px;
  color: var(--accent-2);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  margin: 8px 6px 10px;
}

.inspiration-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.inspire-item {
  padding: 8px 10px;
  border-radius: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;

  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(31, 26, 21, 0.06);
  color: var(--muted);
  transition: all 0.25s var(--transition-easing);
  font-size: 12px;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(212, 91, 45, 0.22);
    box-shadow: 0 14px 24px rgba(31, 26, 21, 0.08);
    color: var(--ink);
  }

  .icon-small {
    font-size: 12px;
    color: var(--accent);
  }

  .text-truncate {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }
}

.sidebar-footer {
  padding: 12px 10px 8px;
  border-top: 1px solid var(--stroke);

  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    font-weight: 600;
    color: var(--ink);
  }
}

/* ====== Main Chat ====== */
.main-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 18px 18px 14px;
  position: relative;
  z-index: 1;
}

.chat-header {
  height: 46px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 0 16px;
  margin-bottom: 12px;
  border-radius: 18px;

  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--stroke);
  box-shadow: 0 18px 36px rgba(31, 26, 21, 0.08);
  backdrop-filter: blur(10px);

  .current-role-tag {
    font-size: 13px;
    color: var(--muted);

    .role-icon {
      margin-right: 6px;
    }
    strong {
      color: var(--ink);
      font-weight: 700;
    }
  }
}

.header-icon-btn {
  border-radius: 12px;
  transition: all 0.25s var(--transition-easing);
  color: var(--muted);

  &:hover {
    background: rgba(212, 91, 45, 0.08);
    color: var(--accent);
    transform: translateY(-1px);
  }
}

/* ====== Messages：唯一可滚动区域 ====== */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px 10px 0;
  scroll-behavior: smooth;
  border-radius: 22px;

  /* 轻柔边缘，让内容像“嵌入式卡片” */
  background: rgba(255, 255, 255, 0.35);
  border: 1px solid rgba(31, 26, 21, 0.06);
  backdrop-filter: blur(6px);

  &::-webkit-scrollbar {
    width: 8px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(31, 26, 21, 0.12);
    border-radius: 999px;
  }
}

.message-list {
  padding: 10px 8px 6px;
}

.message-row {
  display: flex;
  gap: 10px;
  margin: 0 auto 14px;
  max-width: 880px;

  &.user {
    flex-direction: row-reverse;
  }

  .content-col {
    max-width: 78%;
  }

  .bubble {
    padding: 10px 14px;
    border-radius: 18px;
    font-size: 14px;
    line-height: 1.65;
    word-break: break-word;

    border: 1px solid rgba(31, 26, 21, 0.06);
    box-shadow: 0 16px 28px rgba(31, 26, 21, 0.06);
    transition: transform 0.25s var(--transition-easing);
  }
}

.user .bubble {
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
  color: #ffffff;
  border: none;
  box-shadow: 0 16px 28px rgba(212, 91, 45, 0.22);
  border-radius: 18px 18px 6px 18px;
}

.ai .bubble {
  background: rgba(255, 255, 255, 0.86);
  color: var(--ink);
  border-radius: 18px 18px 18px 6px;
  backdrop-filter: blur(8px);
}

.chat-img {
  border-radius: 14px;
  margin-bottom: 10px;
  display: block;
  border: 1px solid rgba(31, 26, 21, 0.08);
  box-shadow: 0 14px 24px rgba(31, 26, 21, 0.08);
}

.loading-dots {
  color: var(--muted);
  font-size: 12px;
  margin-top: 6px;
  letter-spacing: 2px;
}

/* ====== Empty State ====== */
.empty-state {
  min-height: 100%;
  display: grid;
  place-items: center;
  padding: 18px 0;
}

.empty-card {
  width: min(720px, 92%);
  padding: 24px 22px;
  border-radius: 26px;

  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--stroke);
  box-shadow: var(--shadow);
  backdrop-filter: blur(10px);

  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  animation: fadeUp 0.6s var(--transition-easing) both;
}

.empty-icon {
  font-size: 52px;
  margin-bottom: 14px;
  opacity: 0.95;
  filter: drop-shadow(0 12px 20px rgba(31, 26, 21, 0.1));
}

.empty-card h3 {
  font-family: var(--font-display);
  color: var(--ink);
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.4px;
}

.empty-card p {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.7;
}

.tags-row {
  display: flex;
  gap: 8px;
  margin-top: 18px;
  flex-wrap: wrap;
  justify-content: center;
}

.tag {
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;

  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(31, 26, 21, 0.1);
  color: var(--muted);

  transition: all 0.25s var(--transition-easing);

  &:hover {
    border-color: rgba(212, 91, 45, 0.25);
    color: var(--accent);
    transform: translateY(-2px);
    box-shadow: 0 12px 22px rgba(31, 26, 21, 0.08);
  }
}

/* ====== Input Area（固定底部）====== */
.input-wrapper {
  flex-shrink: 0;
  padding: 12px 10px 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;

  background: linear-gradient(to top, rgba(246, 239, 230, 0.92) 70%, transparent);
}

.glass-panel {
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 26, 21, 0.1);
  backdrop-filter: blur(10px);
}

.input-box {
  width: 100%;
  max-width: 880px;
  border-radius: 22px;
  padding: 10px 10px;
  display: flex;
  align-items: flex-end;
  gap: 10px;

  box-shadow: 0 18px 36px rgba(31, 26, 21, 0.1);
  transition: all 0.25s var(--transition-easing);

  &:focus-within {
    border-color: rgba(42, 157, 143, 0.45);
    box-shadow:
      0 0 0 3px rgba(42, 157, 143, 0.16),
      0 18px 36px rgba(31, 26, 21, 0.1);
  }
}

.chat-input {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  color: var(--ink) !important;
  padding: 6px 2px;
  font-size: 14px;
  resize: none;

  &::placeholder {
    color: rgba(122, 111, 102, 0.75);
  }
}

.icon-btn {
  color: var(--muted);
  border-radius: 14px;
  transition: all 0.25s var(--transition-easing);

  &:hover {
    color: var(--accent);
    background: rgba(212, 91, 45, 0.08);
    transform: translateY(-1px);
  }
}

.send-btn {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
  border: none;
  box-shadow: 0 12px 22px rgba(212, 91, 45, 0.25);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 16px 26px rgba(212, 91, 45, 0.32);
  }
}

.input-hint {
  width: 100%;
  max-width: 880px;
  font-size: 12px;
  color: rgba(122, 111, 102, 0.85);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 999px;
    background: rgba(42, 157, 143, 0.7);
    box-shadow: 0 10px 18px rgba(42, 157, 143, 0.18);
  }
}

.image-preview-mini {
  position: absolute;
  top: -46px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 880px;
  padding-left: 14px;

  img {
    height: 44px;
    border-radius: 12px;
    border: 2px solid rgba(42, 157, 143, 0.6);
    box-shadow: 0 14px 24px rgba(31, 26, 21, 0.1);
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
    box-shadow: 0 12px 22px rgba(255, 77, 79, 0.22);
  }
}

/* ====== Mobile ====== */
.mobile-header {
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 14px;
  flex-shrink: 0;

  background: rgba(255, 255, 255, 0.75);
  border-bottom: 1px solid var(--stroke);
  backdrop-filter: blur(10px);

  .logo-area {
    display: flex;
    align-items: center;
    gap: 8px;
    font-family: var(--font-display);
    font-weight: 700;
    letter-spacing: -0.2px;
    color: var(--ink);
  }

  .logo-icon {
    color: var(--accent-2);
    font-size: 18px;
  }

  :deep(.ant-btn) {
    color: var(--ink);
    border-radius: 12px;
  }
}

.is-mobile {
  flex-direction: column;

  .main-chat-area {
    height: calc(100vh - 50px);
    padding: 14px 12px 12px;
  }

  .messages-container {
    border-radius: 18px;
  }
}

/* ====== Drawer（移动端）====== */
.drawer-shell {
  height: 100%;
  padding: 14px;
  background:
    radial-gradient(700px 320px at 8% -10%, rgba(240, 181, 128, 0.35), transparent 65%),
    radial-gradient(620px 300px at 92% 5%, rgba(122, 210, 196, 0.28), transparent 60%),
    linear-gradient(180deg, #fbf7f1 0%, #ffffff 60%, #f6efe6 100%);
}

.drawer-head {
  padding: 14px 14px 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--stroke);
  box-shadow: 0 18px 36px rgba(31, 26, 21, 0.08);
  backdrop-filter: blur(10px);
}

.drawer-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 18px;
  color: var(--ink);

  .logo-icon {
    color: var(--accent-2);
  }
}

.drawer-sub {
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: 0.16em;
  text-transform: uppercase;
  font-weight: 700;
}

.drawer-menu {
  margin-top: 12px;
  padding: 12px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--stroke);
  backdrop-filter: blur(10px);
}

.role-list-mobile {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.mobile-role-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid rgba(31, 26, 21, 0.08);
  background: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  transition: all 0.25s var(--transition-easing);

  .emoji {
    width: 22px;
    display: inline-flex;
    justify-content: center;
  }
  .name {
    color: var(--ink);
    font-weight: 700;
    font-size: 13px;
  }

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(212, 91, 45, 0.22);
    box-shadow: 0 14px 24px rgba(31, 26, 21, 0.08);
  }

  &.active {
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
    border-color: transparent;

    .name {
      color: #fff;
    }
  }
}

.drawer-divider {
  height: 1px;
  margin: 12px 0;
  background: rgba(31, 26, 21, 0.08);
}

/* ===== MdPreview 样式覆盖 ===== */
:deep(.ai-md-preview) {
  background: transparent !important;
  padding: 0 !important;
  font-size: 14px;
  line-height: 1.7;
  color: var(--ink);

  .md-editor-preview-wrapper {
    padding: 0 !important;
  }

  p {
    margin: 0 0 8px 0;
    color: var(--ink);
  }

  ul,
  ol {
    margin: 8px 0 8px 18px;
    padding: 0;
  }

  li {
    margin-bottom: 6px;
    padding-left: 2px;
    color: var(--ink);
  }

  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    margin: 14px 0 8px 0;
    color: var(--accent-2);
    font-weight: 800;
    letter-spacing: -0.2px;
    border-bottom: none;
    padding-bottom: 0;
  }

  h1 {
    font-size: 17px;
  }

  h2 {
    font-size: 16px;
  }

  h3 {
    font-size: 15px;
  }

  hr {
    border: none;
    height: 1px;
    background: rgba(31, 26, 21, 0.12);
    margin: 12px 0;
  }

  b,
  strong {
    color: var(--ink);
    font-weight: 800;
  }

  code {
    background: rgba(31, 26, 21, 0.06);
    padding: 2px 6px;
    border-radius: 8px;
    border: 1px solid rgba(31, 26, 21, 0.08);
    color: var(--accent);
  }

  pre {
    background: rgba(31, 26, 21, 0.04) !important;
    border-radius: 12px;
    padding: 12px !important;
    margin: 10px 0;
    border: 1px solid rgba(31, 26, 21, 0.08);

    code {
      background: transparent;
      border: none;
      padding: 0;
      color: var(--ink);
    }
  }

  blockquote {
    border-left: 3px solid var(--accent-2);
    margin: 10px 0;
    padding-left: 12px;
    color: var(--muted);
    background: rgba(42, 157, 143, 0.06);
    border-radius: 0 8px 8px 0;
    padding: 8px 12px;
  }

  table {
    border-collapse: collapse;
    margin: 10px 0;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid rgba(31, 26, 21, 0.1);
  }

  th,
  td {
    padding: 8px 12px;
    border: 1px solid rgba(31, 26, 21, 0.1);
    text-align: left;
  }

  th {
    background: rgba(42, 157, 143, 0.1);
    color: var(--accent-2);
    font-weight: 700;
  }

  a {
    color: var(--accent);
    text-decoration: none;
    border-bottom: 1px solid rgba(212, 91, 45, 0.25);
    &:hover {
      border-bottom-color: rgba(212, 91, 45, 0.5);
    }
  }
}

/* ===== Ant Design Vue 轻度覆盖（保持统一）===== */
:deep(.ant-btn-primary) {
  border-radius: 999px;
}
:deep(.ant-avatar) {
  box-shadow: 0 12px 22px rgba(31, 26, 21, 0.08);
}
:deep(.ant-drawer-content) {
  background: transparent;
}
:deep(.ant-drawer-body) {
  background: transparent;
}

/* ===== Animation ===== */
@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
