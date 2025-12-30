<template>
  <div id="homePage">
    <!-- 顶部欢迎区域 -->
    <!-- 顶部欢迎区域 (Compact Mode) -->
    <div class="hero-section">
      <div class="hero-orb orb-a"></div>
      <div class="hero-orb orb-b"></div>
      <div class="hero-content">
        <div class="hero-left">
          <div class="hero-eyebrow">灵感图案库</div>
          <h1 class="hero-title">发现精美服装图案</h1>
          <p class="hero-subtitle">海量原创设计，激发创意</p>
          <div v-if="total" class="hero-stats">
            已收录 <strong>{{ total }}</strong> 个原创图案
          </div>
        </div>
        
        <div class="hero-right">
          <div class="search-card">
            <div class="search-label">快速查找</div>
            <div class="search-container">
              <div :class="['expandable-search', { expanded: isSearchExpanded }]">
                <a-input-search
                  placeholder="搜索图案..."
                  v-model:value="searchParams.patternName"
                  @search="doSearch"
                  @pressEnter="doSearch"
                  @blur="handleSearchBlur"
                  ref="searchInputRef"
                  allow-clear
                />
              </div>
              <div class="search-icon-btn" @click="toggleSearch">
                <SearchOutlined />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 筛选条件 -->
      <div class="filter-section">
      <!-- 风格标签 -->
      <div class="filter-row">
        <span class="filter-label">风格：</span>
        <a-space :size="[0, 8]" wrap>
          <a-checkable-tag
            v-for="style in styleList"
            :key="style"
            :checked="searchParams.style === style"
            @change="(checked: boolean) => handleStyleChange(style, checked)"
          >
            {{ style }}
          </a-checkable-tag>
        </a-space>
      </div>

      <!-- 季节标签 -->
      <div class="filter-row">
        <span class="filter-label">季节：</span>
        <a-space :size="[0, 8]" wrap>
          <a-checkable-tag
            v-for="season in seasonList"
            :key="season"
            :checked="searchParams.season === season"
            @change="(checked: boolean) => handleSeasonChange(season, checked)"
          >
            {{ season }}
          </a-checkable-tag>
        </a-space>
      </div>

      <!-- 目标受众标签 -->
      <div class="filter-row">
        <span class="filter-label">目标受众：</span>
        <a-space :size="[0, 8]" wrap>
          <a-checkable-tag
            v-for="audience in targetAudienceList"
            :key="audience"
            :checked="searchParams.targetAudience === audience"
            @change="(checked: boolean) => handleAudienceChange(audience, checked)"
          >
            {{ audience }}
          </a-checkable-tag>
        </a-space>
      </div>
    </div>

    <div class="gallery-header">
      <div>
        <h2 class="gallery-title">灵感精选</h2>
        <p class="gallery-subtitle">从社区最新发布中挑选灵感</p>
      </div>
      <div v-if="total" class="gallery-count">共 {{ total }} 个图案</div>
    </div>

    <!-- 图案列表 -->
    <PatternList :data-list="dataList" :loading="loading" />

      <!-- 分页 -->
      <a-pagination
        v-if="total > 0"
        class="pagination"
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        :show-size-changer="true"
        :show-total="(total: number) => `共 ${total} 个图案`"
        @change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import { listPatternVoByPage } from '@/api/patternController'
import PatternList from '@/components/PatternList.vue'

// 数据
const dataList = ref<API.PatternVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive<API.PatternQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
  auditStatus: 'APPROVED', // 默认只显示已通过的图案
  patternName: ''
})

// 筛选选项
const styleList = ref<string[]>(['简约', '可爱', '复古', '卡通', '抽象', '民族', '未来'])
const seasonList = ref<string[]>(['春季', '夏季', '秋季', '冬季', '四季通用'])
const targetAudienceList = ref<string[]>(['儿童', '青少年', '成人', '中老年', '通用'])

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listPatternVoByPage(searchParams)
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const lastSearchQuery = ref('')

const normalizeQuery = () => {
  return (searchParams.patternName ?? '').trim()
}

const doSearch = () => {
  const query = normalizeQuery()
  searchParams.patternName = query
  searchParams.current = 1
  lastSearchQuery.value = query
  fetchData()
}

// 搜索栏展开逻辑
const isSearchExpanded = ref(false)
const searchInputRef = ref()

const toggleSearch = () => {
  if (!isSearchExpanded.value) {
    isSearchExpanded.value = true
    nextTick(() => {
      // 尝试聚焦输入框，需要确保 ref 绑定正确且组件暴露了 focus 方法
      // Antdv InputSearch 可能需要 input.focus()
      searchInputRef.value?.focus?.()
    })
    return
  }

  const query = normalizeQuery()
  if (query) {
    doSearch()
    return
  }

  if (lastSearchQuery.value) {
    doSearch()
  }
  isSearchExpanded.value = false
}

const handleSearchBlur = () => {
  // 可选：失去焦点时是否自动收起？
  // 为避免用户体验不好（点搜索按钮还没触发就收起了），这里暂时不自动收起
  // 或者加个延时
  const query = normalizeQuery()
  if (!query) {
    searchParams.patternName = ''
    isSearchExpanded.value = false
    if (lastSearchQuery.value) {
      doSearch()
    }
  }
}

// 处理风格选择
const handleStyleChange = (style: string, checked: boolean) => {
  if (checked) {
    searchParams.style = style
  } else {
    searchParams.style = undefined
  }
  doSearch()
}

// 处理季节选择
const handleSeasonChange = (season: string, checked: boolean) => {
  if (checked) {
    searchParams.season = season
  } else {
    searchParams.season = undefined
  }
  doSearch()
}

// 处理目标受众选择
const handleAudienceChange = (audience: string, checked: boolean) => {
  if (checked) {
    searchParams.targetAudience = audience
  } else {
    searchParams.targetAudience = undefined
  }
  doSearch()
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700&family=Newsreader:wght@400;600;700&display=swap');

/* 全局样式重置与基础设置 */
#homePage {
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
  min-height: 100vh;
  background:
    radial-gradient(900px 420px at 8% -10%, rgba(240, 181, 128, 0.4), transparent 65%),
    radial-gradient(800px 380px at 92% 5%, rgba(122, 210, 196, 0.35), transparent 60%),
    linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
  color: var(--ink);
  font-family: var(--font-body);
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

#homePage::before {
  content: '';
  position: absolute;
  inset: -10% -20% auto;
  height: 420px;
  background: radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.7), transparent 65%);
  opacity: 0.8;
  z-index: 0;
  pointer-events: none;
}

#homePage::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(31, 26, 21, 0.06) 1px, transparent 1px);
  background-size: 22px 22px;
  opacity: 0.35;
  z-index: 0;
  pointer-events: none;
}

/* 顶部欢迎区域 */
.hero-section {
  max-width: 1400px;
  margin: 28px auto 0;
  padding: 32px 40px;
  background: linear-gradient(135deg, rgba(255, 252, 246, 0.95) 0%, rgba(255, 241, 226, 0.95) 60%, rgba(255, 252, 246, 0.9) 100%);
  border-radius: 28px;
  position: relative;
  z-index: 1;
  box-shadow: var(--shadow);
  border: 1px solid var(--stroke);
  overflow: hidden;
  animation: fadeUp 0.7s var(--transition-easing);
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(0);
  opacity: 0.7;
  pointer-events: none;
}

.orb-a {
  width: 220px;
  height: 220px;
  right: 8%;
  top: -120px;
  background: radial-gradient(circle at 30% 30%, rgba(244, 182, 124, 0.8), transparent 70%);
  animation: float 10s ease-in-out infinite;
}

.orb-b {
  width: 180px;
  height: 180px;
  right: 36%;
  bottom: -120px;
  background: radial-gradient(circle at 30% 30%, rgba(92, 184, 167, 0.75), transparent 70%);
  animation: float 12s ease-in-out infinite reverse;
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
  position: relative;
  z-index: 1;
}

.hero-left {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 0.28em;
  text-transform: uppercase;
  color: var(--accent-2);
  font-weight: 600;
}

.hero-title {
  font-family: var(--font-display);
  font-size: 38px;
  font-weight: 700;
  color: var(--ink);
  margin: 0;
  letter-spacing: -0.8px;
}

.hero-subtitle {
  font-size: 16px;
  color: var(--muted);
  margin: 0;
  line-height: 1.6;
}

.hero-stats {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--muted);
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--stroke);
  width: fit-content;
}

.hero-stats strong {
  color: var(--accent);
  font-weight: 700;
}

/* 搜索区域 */
.hero-right {
  display: flex;
  align-items: center;
}

.search-card {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 20px;
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 14px 32px rgba(31, 26, 21, 0.12);
  backdrop-filter: blur(10px);
}

.search-label {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 8px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.16em;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon-btn {
  font-size: 18px;
  color: #ffffff;
  cursor: pointer;
  padding: 10px;
  border-radius: 999px;
  transition: all 0.25s var(--transition-easing);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
  box-shadow: 0 10px 20px rgba(212, 91, 45, 0.25);
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 14px 28px rgba(212, 91, 45, 0.3);
  }
}

.expandable-search {
  width: 0;
  opacity: 0;
  visibility: hidden;
  transition: all 0.35s var(--transition-easing);
  overflow: hidden;
  margin-right: 0;
  
  &.expanded {
    width: 280px;
    opacity: 1;
    visibility: visible;
    margin-right: 12px;
  }
}

/* 搜索框内部样式覆盖 */
:deep(.ant-input-search) {
  width: 100%;
}

:deep(.ant-input-search .ant-input) {
  border-radius: 999px;
  padding-left: 18px;
  padding-right: 44px;
  height: 40px;
  font-size: 14px;
  background: #ffffff;
  border: 1px solid rgba(31, 26, 21, 0.12);
  transition: all 0.25s var(--transition-easing);
  
  &:focus {
    border-color: var(--accent-2);
    box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.16);
  }
}

:deep(.ant-input-search .ant-input-search-button) {
  display: none; /* 隐藏默认搜索按钮，使用回车或输入框内图标 */
}

/* 主内容区域 */
.main-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px 24px;
  position: relative;
  z-index: 1;
}

/* 筛选区域样式优化 */
.filter-section {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 249, 242, 0.96) 100%);
  padding: 32px;
  border-radius: 26px;
  margin-bottom: 32px;
  box-shadow: 0 18px 36px rgba(31, 26, 21, 0.08);
  border: 1px solid var(--stroke);
  transition: all var(--transition-duration) var(--transition-easing);
  backdrop-filter: blur(6px);
}

.filter-section:hover {
  box-shadow: 0 24px 48px rgba(31, 26, 21, 0.12);
  transform: translateY(-3px);
}

.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  animation: fadeUp 0.6s var(--transition-easing) both;
}

.filter-row:nth-child(1) {
  animation-delay: 0.05s;
}

.filter-row:nth-child(2) {
  animation-delay: 0.12s;
}

.filter-row:nth-child(3) {
  animation-delay: 0.18s;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-weight: 600;
  margin-right: 20px;
  min-width: 80px;
  color: var(--ink);
  font-size: 15px;
  line-height: 32px;
  white-space: nowrap;
}

.gallery-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin: 8px 8px 24px;
}

.gallery-title {
  font-family: var(--font-display);
  font-size: 26px;
  margin: 0 0 6px;
  color: var(--ink);
  letter-spacing: -0.5px;
}

.gallery-subtitle {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
}

.gallery-count {
  padding: 6px 16px;
  border-radius: 999px;
  background: rgba(42, 157, 143, 0.12);
  border: 1px solid rgba(42, 157, 143, 0.25);
  color: var(--accent-2);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

/* 标签样式优化 */
:deep(.ant-tag-checkable) {
  cursor: pointer;
  border-radius: 999px;
  padding: 6px 18px;
  height: 32px;
  line-height: 20px;
  margin-right: 12px;
  margin-bottom: 12px;
  transition: all 0.25s var(--transition-easing);
  font-size: 14px;
  border: 1px solid rgba(31, 26, 21, 0.12);
  color: var(--muted);
  background-color: #fff7ee;
  font-weight: 600;
}

:deep(.ant-tag-checkable:hover) {
  color: var(--accent);
  background-color: #fff1e3;
  transform: translateY(-2px);
  border-color: rgba(212, 91, 45, 0.3);
}

:deep(.ant-tag-checkable-checked) {
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 10px 20px rgba(212, 91, 45, 0.3);
  font-weight: 700;
}

:deep(.ant-tag-checkable-checked:hover) {
  background: linear-gradient(135deg, #c24f26 0%, #e8784b 100%);
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(212, 91, 45, 0.35);
}

/* 图案列表视觉统一 */
:deep(.pattern-list) {
  padding-top: 0;
}

:deep(.pattern-list .ant-list-item) {
  animation: fadeUp 0.6s var(--transition-easing);
}

:deep(.pattern-list .pattern-card) {
  border-radius: 22px;
  border: 1px solid rgba(31, 26, 21, 0.08);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 32px rgba(31, 26, 21, 0.08);
  transition: all 0.35s var(--transition-easing);
}

:deep(.pattern-list .pattern-card:hover) {
  transform: translateY(-6px);
  border-color: rgba(212, 91, 45, 0.25);
  box-shadow: 0 26px 46px rgba(31, 26, 21, 0.12);
}

:deep(.pattern-list .ant-card-meta-title) {
  font-family: var(--font-display);
  letter-spacing: -0.3px;
  color: var(--ink);
}

:deep(.pattern-list .pattern-cover) {
  background: linear-gradient(135deg, #f5ede3 0%, #f0f7f6 100%);
}

:deep(.pattern-list .pattern-tags .ant-tag) {
  background: #f4efe6;
  color: var(--muted);
}

:deep(.pattern-list .ant-card-actions) {
  background: rgba(255, 255, 255, 0.92);
}

:deep(.pattern-list .ant-card-actions > li:hover) {
  color: var(--accent);
}

:deep(.pattern-list .like-action:hover) {
  color: #e4574b;
}

/* 分页样式优化 */
.pagination {
  text-align: center;
  margin-top: 64px;
  padding: 24px 0;
  animation: fadeUp 0.7s var(--transition-easing);
}

:deep(.ant-pagination) {
  font-size: 15px;
  font-weight: 500;
}

:deep(.ant-pagination-item) {
  border-radius: 12px !important;
  margin: 0 6px;
  border: 1px solid transparent;
  background: #ffffff;
  transition: all 0.2s ease;
}

:deep(.ant-pagination-item:hover) {
  border-color: rgba(212, 91, 45, 0.4);
  color: var(--accent);
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(212, 91, 45, 0.14);
}

:deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%) !important;
  border: none !important;
  box-shadow: 0 12px 22px rgba(212, 91, 45, 0.3);
}

:deep(.ant-pagination-item-active a) {
  color: #ffffff !important;
}

:deep(.ant-pagination-item-active:hover) {
  transform: translateY(-2px);
  box-shadow: 0 16px 26px rgba(212, 91, 45, 0.35);
}

:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  border-radius: 12px !important;
  background: #ffffff;
  border: 1px solid transparent;
}

:deep(.ant-pagination-prev:hover),
:deep(.ant-pagination-next:hover) {
  border-color: rgba(212, 91, 45, 0.4);
  color: var(--accent);
  transform: translateY(-2px);
  background: #ffffff;
}

:deep(.ant-pagination-disabled) {
  opacity: 0.5;
  pointer-events: none;
}

:deep(.ant-pagination-show-size-changer) {
  margin-left: 24px;
}

:deep(.ant-select-selector) {
  border-radius: 8px !important;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(12px);
  }
}

/* 响应式优化 */
@media (max-width: 1024px) {
  .main-content {
    padding: 24px 16px;
  }

  .hero-section {
    padding: 28px 24px;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .hero-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-right {
    width: 100%;
  }

  .search-card {
    width: 100%;
  }

  .filter-section {
    padding: 20px;
  }

  .filter-row {
    margin-bottom: 14px;
  }

  .gallery-header {
    margin: 0 0 20px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 24px 16px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .main-content {
    padding: 16px 12px;
  }

  .search-bar {
    max-width: 100%;
  }

  .search-container {
    width: 100%;
  }

  .expandable-search.expanded {
    width: 100%;
  }

  :deep(.ant-input-search-large) {
    --ant-input-height: 48px;
  }

  :deep(.ant-input-search-large .ant-input) {
    font-size: 14px;
    padding: 0 16px;
  }

  :deep(.ant-input-search-large .ant-input-search-button) {
    padding: 0 24px;
    font-size: 14px;
  }

  .filter-section {
    padding: 16px;
    border-radius: 10px;
  }

  .filter-row {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 12px;
  }

  .filter-label {
    margin-bottom: 10px;
    margin-right: 0;
    font-size: 13px;
  }

  .gallery-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .gallery-title {
    font-size: 22px;
  }

  :deep(.ant-tag-checkable) {
    padding: 4px 12px;
    margin-right: 8px;
    margin-bottom: 8px;
    font-size: 13px;
  }

  .pagination {
    margin-top: 24px;
    padding: 16px 0;
  }

  :deep(.ant-pagination) {
    font-size: 13px;
  }

  :deep(.ant-pagination-item) {
    margin: 0 2px;
    min-width: 32px;
    height: 32px;
    line-height: 32px;
  }
}

/* 加载状态相关（如果PatternList组件需要） */
:deep(.ant-spin) {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

:deep(.ant-spin-dot) {
  font-size: 48px;
}

:deep(.ant-spin-text) {
  font-size: 16px;
  color: #4e5969;
  margin-top: 16px;
}
</style>
