<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import LeftOne from '@/components/LeftOne.vue'
import LeftTwo from '@/components/LeftTwo.vue'
import LeftThree from '@/components/LeftThree.vue'
import CenterOne from '@/components/CenterOne.vue'
import CenterTwo from '@/components/CenterTwo.vue'
import RightOne from '@/components/RightOne.vue'
import RightTwo from '@/components/RightTwo.vue'
import RightThree from '@/components/RightThree.vue'
import {
  getUserCount,
  getPatternCount,
  getUserGrowth,
  getTargetAudienceTopFive,
  getHotStyleTopFive,
  getStylePreference,
  getInteraction,
  getArticleTopOne,
} from '@/api/homeController'

// 定义一个响应式变量来存储当前时间
const currentTime = ref(getCurrentDateTime())

function getCurrentDateTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = (now.getMonth() + 1).toString().padStart(2, '0')
  const day = now.getDate().toString().padStart(2, '0')
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  const seconds = now.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

let timer = null

// ========== AI 小助手功能 ==========
const showAiAssistant = ref(false)
const aiLoading = ref(false)
const aiResponse = ref('')
const aiChatRef = ref(null)

// 收集可视化数据
const collectDashboardData = async () => {
  try {
    const [userCountRes, patternCountRes, userGrowthRes, targetAudienceRes, hotStyleRes, stylePreferenceRes, interactionRes, articleTopRes] = await Promise.all([
      getUserCount(),
      getPatternCount(),
      getUserGrowth(),
      getTargetAudienceTopFive(),
      getHotStyleTopFive(),
      getStylePreference(),
      getInteraction(),
      getArticleTopOne(),
    ])

    const data = {
      userCount: userCountRes.data.code === 0 ? userCountRes.data.data : 0,
      patternCount: patternCountRes.data.code === 0 ? patternCountRes.data.data : 0,
      userGrowth: userGrowthRes.data.code === 0 ? userGrowthRes.data.data : [],
      targetAudience: targetAudienceRes.data.code === 0 ? targetAudienceRes.data.data : [],
      hotStyles: hotStyleRes.data.code === 0 ? hotStyleRes.data.data : [],
      stylePreference: stylePreferenceRes.data.code === 0 ? stylePreferenceRes.data.data : [],
      interaction: interactionRes.data.code === 0 ? interactionRes.data.data : [],
      articleTop: articleTopRes.data.code === 0 ? articleTopRes.data.data : [],
    }
    return data
  } catch (error) {
    console.error('收集数据失败:', error)
    return null
  }
}

// 构建分析提示词
const buildAnalysisPrompt = (data) => {
  if (!data) return ''

  const prompt = `
你是一位专业的服装图案市场分析师，请根据以下可视化大屏数据，分析当前服装图案市场趋势，并给设计师提供专业建议。请用简洁清晰的语言回答，使用Markdown格式。

## 平台基础数据
- 当前总用户量: ${data.userCount}人
- 图案总数: ${data.patternCount}个

## 近期用户增长趋势
${data.userGrowth.map(item => `${item.date}: 新增${item.count}人`).join('\n')}

## 目标人群分布(Top5)
${data.targetAudience.map(item => `${item.targetAudience}: ${item.count}个作品`).join('\n')}

## 热门风格分布(Top5)
${data.hotStyles.map(item => `${item.style}: ${item.count}个作品`).join('\n')}

## 风格偏好趋势(近期)
${data.stylePreference.map(day => `${day.date}: ${day.topStyles?.map(s => s.style + '(' + s.count + ')').join(', ') || '无数据'}`).join('\n')}

## 图案互动评分Top
${data.interaction.map(item => `${item.patternName}: 评分${item.score}`).join('\n')}

## 文章互动趋势
${data.articleTop.slice(0, 10).map(item => `${item.date_day} ${item.type}: ${item.count}`).join('\n')}

请从以下方面进行分析：
1. **市场趋势**: 当前流行的风格和目标人群特点
2. **增长洞察**: 用户增长和内容生产的健康度
3. **设计建议**: 给设计师的创作方向建议
4. **机会点**: 可能存在的市场空白和机会

请确保分析专业、实用，对设计师有实际参考价值。
  `.trim()

  return prompt
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (aiChatRef.value) {
      aiChatRef.value.scrollTop = aiChatRef.value.scrollHeight
    }
  })
}

// 开始AI分析
const startAiAnalysis = async () => {
  if (aiLoading.value) return

  aiLoading.value = true
  aiResponse.value = ''

  try {
    // 收集数据
    const dashboardData = await collectDashboardData()
    if (!dashboardData) {
      aiResponse.value = '获取数据失败，请稍后重试。'
      aiLoading.value = false
      return
    }

    // 构建提示词
    const prompt = buildAnalysisPrompt(dashboardData)

    // 调用AI流式接口（使用市场分析师角色）
    const response = await fetch('http://localhost:8123/api/ai/ask/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ 
        question: prompt,
        role: 'analyst'  // 使用市场分析师角色预设
      }),
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

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
              aiResponse.value += content
              scrollToBottom()
            }
          }
        }
      }
    }
  } catch (err) {
    console.error('AI分析失败:', err)
    aiResponse.value += '\n\n[系统提示] 分析请求失败，请检查登录状态或后台服务。'
  } finally {
    aiLoading.value = false
  }
}

// 切换AI助手显示
const toggleAiAssistant = () => {
  showAiAssistant.value = !showAiAssistant.value
  if (showAiAssistant.value && !aiResponse.value) {
    // 首次打开时自动开始分析
    startAiAnalysis()
  }
}

// 在组件挂载时启动定时器
onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = getCurrentDateTime()
  }, 1000)
})

// 在组件卸载时清除定时器
onBeforeUnmount(() => {
  clearInterval(timer)
})
</script>

<template>
  <div class="home">
    <header class="top">
      <dv-border-box-11 title="数据分析中心" class="top-nav" :color="['#188ffe']">
        <div class="header-info">
          <div class="name">创作人：小辛同学</div>
          <div class="time">{{ currentTime }}</div>
        </div>
      </dv-border-box-11>
    </header>

    <main class="bottom">
      <div class="left">
        <dv-border-box-1 class="left-item">
          <LeftOne />
        </dv-border-box-1>
        <dv-border-box-1 class="left-item">
          <LeftTwo />
        </dv-border-box-1>
        <dv-border-box-1 class="left-item">
          <LeftThree />
        </dv-border-box-1>
      </div>

      <div class="center">
        <dv-border-box-1 class="center-top">
          <div class="center-top-item">
            <div class="center-top-item-span">
              <span>当前总用户量</span>
              <CenterOne type="user" />
            </div>

            <div class="line"></div>

            <div class="center-top-item-span">
              <span>图案总数</span>
              <CenterOne type="pattern" />
            </div>
          </div>
        </dv-border-box-1>
        <dv-border-box-1 class="center-bottom">
          <CenterTwo />
        </dv-border-box-1>
      </div>

      <div class="right">
        <dv-border-box-1 class="right-item">
          <RightOne />
        </dv-border-box-1>

        <dv-border-box-1 class="right-item">
          <RightTwo />
        </dv-border-box-1>

        <dv-border-box-1 class="right-item">
          <RightThree />
        </dv-border-box-1>
      </div>
    </main>

    <!-- AI 小助手悬浮按钮 -->
    <div class="ai-assistant-btn" @click="toggleAiAssistant" :class="{ active: showAiAssistant }">
      <span class="ai-icon">🤖</span>
      <span class="ai-text">AI助手</span>
    </div>

    <!-- AI 小助手对话框 -->
    <transition name="slide-up">
      <div v-if="showAiAssistant" class="ai-assistant-panel">
        <div class="ai-panel-header">
          <div class="ai-panel-title">
            <span class="ai-avatar">🤖</span>
            <span>AI 市场分析助手</span>
          </div>
          <div class="ai-panel-actions">
            <button class="refresh-btn" @click="startAiAnalysis" :disabled="aiLoading" title="重新分析">
              <span :class="{ rotating: aiLoading }">🔄</span>
            </button>
            <button class="close-btn" @click="showAiAssistant = false">✕</button>
          </div>
        </div>
        <div class="ai-panel-body" ref="aiChatRef">
          <div v-if="aiLoading && !aiResponse" class="ai-loading">
            <div class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <div class="loading-text">正在分析市场数据...</div>
          </div>
          <div v-else class="ai-content markdown-body" v-html="formatMarkdown(aiResponse)"></div>
        </div>
        <div class="ai-panel-footer">
          <span class="tip-text">💡 基于当前可视化数据实时分析</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
// Markdown 格式化（增强版）
export default {
  methods: {
    formatMarkdown(text) {
      if (!text) return ''
      
      let html = text
        // 分隔线
        .replace(/^---$/gm, '<hr class="md-hr">')
        // 标题（带emoji的标题特殊处理）
        .replace(/^### (.+)$/gm, '<h4 class="md-h4">$1</h4>')
        .replace(/^## (.+)$/gm, '<h3 class="md-h3">$1</h3>')
        .replace(/^# (.+)$/gm, '<h2 class="md-h2">$1</h2>')
        // 粗体
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        // 斜体
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        // 列表项（带特殊符号）
        .replace(/^[\-\*] (.+)$/gm, '<li class="md-li">$1</li>')
        .replace(/^✅ (.+)$/gm, '<li class="md-li md-success">✅ $1</li>')
        .replace(/^💡 (.+)$/gm, '<li class="md-li md-idea">💡 $1</li>')
        .replace(/^⚠️ (.+)$/gm, '<li class="md-li md-warning">⚠️ $1</li>')
        .replace(/^❌ (.+)$/gm, '<li class="md-li md-error">❌ $1</li>')
        // 数字列表
        .replace(/^(\d+)\. (.+)$/gm, '<li class="md-li md-num">$1. $2</li>')
        
      // 处理连续的li元素，包裹成ul
      html = html.replace(/(<li class="md-li[^"]*">.+?<\/li>\n?)+/g, (match) => {
        return '<ul class="md-ul">' + match + '</ul>'
      })
      
      // 换行处理（保留段落结构）
      html = html
        .replace(/\n\n/g, '</p><p class="md-p">')
        .replace(/\n/g, '<br>')
      
      // 包裹段落
      if (!html.startsWith('<')) {
        html = '<p class="md-p">' + html + '</p>'
      }
      
      return html
    }
  }
}
</script>

<style scoped lang="less">
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

.home {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background-color: #000;
  color: #fff;
}

.top {
  display: grid;
  height: 60px;
  flex-shrink: 0;
  .top-nav {
    width: 100%;
    height: 100%;
    position: relative;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);

    .header-info {
      position: absolute;
      left: 0;
      right: 0;
      top: 0;
      bottom: 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 100px;
    }

    .name {
      color: #00f2ff;
      text-shadow:
        0 0 5px #00f2ff,
        0 0 40px #00f2ff;
      font-weight: 700;
      font-size: 0.9rem;
    }
    .time {
      font-weight: 700;
      color: #00f2ff;
      text-shadow:
        0 0 5px #00f2ff,
        0 0 40px #00f2ff;
      font-size: 0.9rem;
    }
  }
}

.bottom {
  flex: 1;
  display: grid;
  grid-template-columns: 3fr 6fr 3fr;
  gap: 10px;
  padding: 10px;
}

.left {
  display: grid;
  grid-template-rows: repeat(3, 1fr);
  gap: 10px;
  .left-item {
    width: 100%;
    border-radius: 8px;
    height: 100%;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);
  }
}

.center {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  height: 100%;
  flex-direction: column;

  flex-direction: column;
  justify-content: space-between;
  .center-top {
    display: flex;
    width: 100%;
    height: 23%;
    background-color: #000;
    border-radius: 8px;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);

    .center-top-item {
      display: flex;
      width: 100%;
      height: 100%;
      justify-content: space-around;
      align-items: center;

      .line {
        display: flex;
        width: 5px;
        height: 30%;
        background-color: #3be1c4;
      }

      .center-top-item-span {
        display: flex;
        height: 100%;
        align-items: center;
        justify-content: center;
        flex-wrap: wrap;
        flex-direction: column;
        span {
          display: flex;
          width: 100%;
          justify-content: center;
          align-items: center;
          font-weight: 700;
          color: #00f2ff;
          text-shadow: 0 0 5px #00f2ff;
        }
      }
    }
  }

  .center-bottom {
    display: flex;
    width: 100%;
    height: 75%;
    background-color: #000;
    border-radius: 8px;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);
  }
}

.right {
  display: grid;
  grid-template-rows: repeat(3, 1fr);
  gap: 10px;
  .right-item {
    width: 100%;
    height: 100%;
    border-radius: 8px;
    box-shadow: inset 0 0 15px rgba(0, 98, 255, 0.2);
  }
}

/* 响应式适配 */
@media (max-width: 992px) {
  .home {
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }

  .bottom {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: auto;
    padding: 8px;
    gap: 8px;
  }

  .left {
    grid-column: 1;
    grid-row: 1;
  }

  .center {
    grid-column: 2;
    grid-row: 1 / 2;
  }

  .right {
    grid-column: 1 / 3;
    grid-row: 2;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: 1fr;
  }

  .right .right-item {
    min-height: 200px;
  }

  .left .left-item {
    min-height: 180px;
  }

  .center .center-top {
    height: 120px;
    min-height: 120px;
  }

  .center .center-bottom {
    height: 350px;
    min-height: 350px;
  }
}

@media (max-width: 768px) {
  .home {
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }

  .top {
    height: auto;
    min-height: 50px;

    .top-nav {
      .header-info {
        position: static;
        flex-direction: column;
        justify-content: center;
        padding: 8px 15px;
        gap: 2px;
      }

      .name {
        font-size: 0.65rem;
      }

      .time {
        font-size: 0.6rem;
      }
    }
  }

  .bottom {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 8px;
  }

  .left {
    display: flex;
    flex-direction: column;
    gap: 8px;
    order: 2;

    .left-item {
      min-height: 220px;
      height: auto;
    }
  }

  .center {
    order: 1;
    gap: 8px;

    .center-top {
      height: auto;
      min-height: 110px;
      padding: 10px 0;

      .center-top-item {
        flex-direction: row;
        flex-wrap: nowrap;
        padding: 0 10px;

        .center-top-item-span {
          flex: 1;
          min-width: 0;
          padding: 5px;

          span {
            font-size: 0.75rem;
            white-space: nowrap;
          }
        }

        .line {
          height: 50px;
          width: 2px;
          flex-shrink: 0;
        }
      }
    }

    .center-bottom {
      height: auto;
      min-height: 280px;
    }
  }

  .right {
    display: flex;
    flex-direction: column;
    gap: 8px;
    order: 3;

    .right-item {
      min-height: 220px;
      height: auto;
    }
  }
}

@media (max-width: 576px) {
  .top {
    min-height: 45px;

    .top-nav {
      .header-info {
        padding: 6px 10px;
      }

      .name {
        font-size: 0.55rem;
      }

      .time {
        font-size: 0.5rem;
      }
    }
  }

  .bottom {
    padding: 6px;
    gap: 6px;
  }

  .left .left-item,
  .right .right-item {
    min-height: 200px;
  }

  .center {
    gap: 6px;

    .center-top {
      min-height: 100px;
      padding: 8px 0;

      .center-top-item {
        padding: 0 8px;

        .center-top-item-span {
          span {
            font-size: 0.65rem;
          }
        }

        .line {
          height: 40px;
        }
      }
    }

    .center-bottom {
      min-height: 250px;
    }
  }
}

@media (max-width: 375px) {
  .top {
    min-height: 40px;

    .top-nav {
      .header-info {
        padding: 5px 8px;
      }

      .name {
        font-size: 0.5rem;
      }

      .time {
        font-size: 0.45rem;
      }
    }
  }

  .bottom {
    padding: 5px;
    gap: 5px;
  }

  .left .left-item,
  .right .right-item {
    min-height: 180px;
  }

  .center {
    .center-top {
      min-height: 90px;

      .center-top-item {
        padding: 0 5px;

        .center-top-item-span {
          span {
            font-size: 0.55rem;
          }
        }

        .line {
          height: 30px;
        }
      }
    }

    .center-bottom {
      min-height: 220px;
    }
  }
}

/* AI 小助手样式 */
.ai-assistant-btn {
  position: fixed;
  left: 20px;
  bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: linear-gradient(135deg, #0066ff 0%, #00c6ff 100%);
  border-radius: 50px;
  cursor: pointer;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(0, 102, 255, 0.4), 0 0 30px rgba(0, 198, 255, 0.2);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 25px rgba(0, 102, 255, 0.5), 0 0 40px rgba(0, 198, 255, 0.3);
  }

  &.active {
    background: linear-gradient(135deg, #00c6ff 0%, #0066ff 100%);
  }

  .ai-icon {
    font-size: 20px;
  }

  .ai-text {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.ai-assistant-panel {
  position: fixed;
  left: 20px;
  bottom: 80px;
  width: 480px;
  max-height: 600px;
  background: rgba(10, 20, 40, 0.95);
  border-radius: 16px;
  border: 1px solid rgba(0, 198, 255, 0.3);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5), 0 0 30px rgba(0, 198, 255, 0.1);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.ai-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(90deg, rgba(0, 102, 255, 0.2) 0%, rgba(0, 198, 255, 0.1) 100%);
  border-bottom: 1px solid rgba(0, 198, 255, 0.2);
}

.ai-panel-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #00f2ff;
  font-weight: 600;
  font-size: 15px;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);

  .ai-avatar {
    font-size: 22px;
  }
}

.ai-panel-actions {
  display: flex;
  gap: 8px;

  button {
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
  }

  .refresh-btn {
    background: rgba(0, 198, 255, 0.2);
    color: #00f2ff;
    font-size: 14px;

    &:hover:not(:disabled) {
      background: rgba(0, 198, 255, 0.4);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .rotating {
      animation: rotate 1s linear infinite;
    }
  }

  .close-btn {
    background: rgba(255, 100, 100, 0.2);
    color: #ff6464;
    font-size: 12px;

    &:hover {
      background: rgba(255, 100, 100, 0.4);
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.ai-panel-body {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  max-height: 480px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 198, 255, 0.3);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 198, 255, 0.5);
    }
  }
}

.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 16px;
}

.loading-dots {
  display: flex;
  gap: 6px;

  span {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #00f2ff;
    animation: bounce 1.4s infinite ease-in-out;
    box-shadow: 0 0 10px rgba(0, 242, 255, 0.5);

    &:nth-child(1) {
      animation-delay: -0.32s;
    }

    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.loading-text {
  color: #00f2ff;
  font-size: 14px;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);
}

.ai-content {
  color: #e0f0ff;
  font-size: 13px;
  line-height: 1.8;

  // 段落
  :deep(.md-p) {
    margin: 0 0 12px;
  }

  // 分隔线
  :deep(.md-hr) {
    border: none;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(0, 242, 255, 0.3), transparent);
    margin: 16px 0;
  }

  // 标题样式
  :deep(.md-h2) {
    font-size: 17px;
    color: #00f2ff;
    margin: 20px 0 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid rgba(0, 242, 255, 0.2);
    text-shadow: 0 0 10px rgba(0, 242, 255, 0.4);
    display: flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.md-h3) {
    font-size: 15px;
    color: #00f2ff;
    margin: 16px 0 10px;
    padding-left: 10px;
    border-left: 3px solid #00f2ff;
    text-shadow: 0 0 8px rgba(0, 242, 255, 0.3);
  }

  :deep(.md-h4) {
    font-size: 14px;
    color: #80d8ff;
    margin: 12px 0 8px;
    font-weight: 600;
  }

  // 列表样式
  :deep(.md-ul) {
    margin: 10px 0;
    padding-left: 0;
    list-style: none;
  }

  :deep(.md-li) {
    position: relative;
    padding: 6px 12px 6px 20px;
    margin: 4px 0;
    background: rgba(0, 102, 255, 0.08);
    border-radius: 6px;
    border-left: 2px solid rgba(0, 198, 255, 0.4);

    &::before {
      content: '▸';
      position: absolute;
      left: 6px;
      color: #00f2ff;
    }
  }

  :deep(.md-li.md-success) {
    background: rgba(0, 200, 100, 0.1);
    border-left-color: #00c864;

    &::before {
      content: '';
    }
  }

  :deep(.md-li.md-idea) {
    background: rgba(255, 200, 0, 0.1);
    border-left-color: #ffc800;

    &::before {
      content: '';
    }
  }

  :deep(.md-li.md-warning) {
    background: rgba(255, 150, 0, 0.1);
    border-left-color: #ff9600;

    &::before {
      content: '';
    }
  }

  :deep(.md-li.md-error) {
    background: rgba(255, 80, 80, 0.1);
    border-left-color: #ff5050;

    &::before {
      content: '';
    }
  }

  :deep(.md-li.md-num) {
    &::before {
      content: '';
    }
  }

  // 粗体和斜体
  strong {
    color: #00f2ff;
    font-weight: 600;
  }

  em {
    color: #80d8ff;
    font-style: normal;
    background: rgba(0, 198, 255, 0.15);
    padding: 1px 4px;
    border-radius: 3px;
  }
}

.ai-panel-footer {
  padding: 10px 16px;
  background: rgba(0, 102, 255, 0.1);
  border-top: 1px solid rgba(0, 198, 255, 0.2);
}

.tip-text {
  font-size: 12px;
  color: rgba(0, 242, 255, 0.7);
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .ai-assistant-btn {
    left: 10px;
    bottom: 10px;
    padding: 10px 14px;

    .ai-text {
      display: none;
    }
  }

  .ai-assistant-panel {
    left: 10px;
    right: 10px;
    bottom: 60px;
    width: auto;
    max-height: 400px;
  }
}
</style>
