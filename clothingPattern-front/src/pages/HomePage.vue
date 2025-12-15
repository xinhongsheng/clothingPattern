<template>
  <div id="homePage">
    <!-- 顶部欢迎区域 -->
    <!-- 顶部欢迎区域 (Compact Mode) -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-left">
          <h1 class="hero-title">发现精美服装图案</h1>
          <p class="hero-subtitle">海量原创设计，激发创意</p>
        </div>
        
        <div class="hero-right">
          <div class="search-container">
            <div :class="['expandable-search', { expanded: isSearchExpanded }]">
              <a-input-search
                placeholder="搜索图案..."
                v-model:value="searchParams.patternName"
                @search="doSearch"
                @blur="handleSearchBlur"
                ref="searchInputRef"
              />
            </div>
            <div class="search-icon-btn" @click="toggleSearch">
              <SearchOutlined />
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
const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 搜索栏展开逻辑
const isSearchExpanded = ref(false)
const searchInputRef = ref()

const toggleSearch = () => {
  isSearchExpanded.value = !isSearchExpanded.value
  if (isSearchExpanded.value) {
    nextTick(() => {
      // 尝试聚焦输入框，需要确保 ref 绑定正确且组件暴露了 focus 方法
      // Antdv InputSearch 可能需要 input.focus()
      searchInputRef.value?.focus?.()
    })
  } else {
    // 如果是关闭，且有搜索内容，是否要清空？
    // 暂时保持不清空，仅收起
  }
}

const handleSearchBlur = () => {
  // 可选：失去焦点时是否自动收起？
  // 为避免用户体验不好（点搜索按钮还没触发就收起了），这里暂时不自动收起
  // 或者加个延时
  if (!searchParams.patternName) {
    isSearchExpanded.value = false
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
/* 全局样式重置与基础设置 */
#homePage {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 统一的动画缓动函数 */
:root {
  --transition-duration: 0.3s;
  --transition-easing: cubic-bezier(0.4, 0, 0.2, 1);
}

/* 顶部欢迎区域 - 悬浮圆角框模式 */
.hero-section {
  max-width: 1400px;
  margin: 24px auto 0;
  padding: 12px 32px;
  background: #ffffff;
  border-radius: 24px;
  position: relative;
  z-index: 10;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.02);
}

.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 48px;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  padding-left: 16px;
  border-left: 1px solid #e5e7eb;
  line-height: 1.2;
}

/* 搜索区域 */
.hero-right {
  display: flex;
  align-items: center;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon-btn {
  font-size: 20px;
  color: #4b5563;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    background-color: #f3f4f6;
    color: #4f46e5;
  }
}

.expandable-search {
  width: 0;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  margin-right: 0;
  
  &.expanded {
    width: 260px;
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
  border-radius: 20px;
  padding-left: 16px;
  padding-right: 40px; /* 留出按钮空间 */
  height: 36px;
  font-size: 14px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
  
  &:focus {
    background: #ffffff;
    border-color: #4f46e5;
    box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
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
}

/* 筛选区域样式优化 */
.filter-section {
  background: #ffffff;
  padding: 32px;
  border-radius: 24px;
  margin-bottom: 32px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.02);
  transition: all var(--transition-duration) var(--transition-easing);
}

.filter-section:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-weight: 600;
  margin-right: 20px;
  min-width: 80px;
  color: #1f2937;
  font-size: 15px;
  line-height: 32px;
  white-space: nowrap;
}

/* 标签样式优化 */
:deep(.ant-tag-checkable) {
  cursor: pointer;
  border-radius: 12px;
  padding: 6px 20px;
  height: 32px;
  line-height: 20px;
  margin-right: 12px;
  margin-bottom: 12px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  border: 1px solid transparent;
  color: #6b7280;
  background-color: #f3f4f6;
  font-weight: 500;
}

:deep(.ant-tag-checkable:hover) {
  color: #4f46e5;
  background-color: #eef2ff;
  transform: translateY(-1px);
}

:deep(.ant-tag-checkable-checked) {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: #ffffff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
  font-weight: 600;
}

:deep(.ant-tag-checkable-checked:hover) {
  background: linear-gradient(135deg, #4338ca 0%, #6d28d9 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}

/* 分页样式优化 */
.pagination {
  text-align: center;
  margin-top: 64px;
  padding: 24px 0;
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
  border-color: #818cf8;
  color: #4f46e5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.1);
}

:deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}

:deep(.ant-pagination-item-active a) {
  color: #ffffff !important;
}

:deep(.ant-pagination-item-active:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
}

:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  border-radius: 12px !important;
  background: #ffffff;
  border: 1px solid transparent;
}

:deep(.ant-pagination-prev:hover),
:deep(.ant-pagination-next:hover) {
  border-color: #818cf8;
  color: #4f46e5;
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


/* 响应式优化 */
@media (max-width: 1024px) {
  .main-content {
    padding: 24px 16px;
  }

  .hero-section {
    padding: 48px 16px 40px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .filter-section {
    padding: 20px;
  }

  .filter-row {
    margin-bottom: 14px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 40px 12px 36px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 15px;
    margin-bottom: 32px;
  }

  .main-content {
    padding: 16px 12px;
  }

  .search-bar {
    max-width: 100%;
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

