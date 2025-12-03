<template>
  <div id="homePage">
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="从海量服装图案中搜索"
        v-model:value="searchParams.patternName"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>

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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
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
  padding: 32px 24px;
  min-height: 100vh;
  background: linear-gradient(145deg, #f5f7fa 0%, #e4eaf5 100%);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 搜索框样式优化 */
.search-bar {
  max-width: 720px;
  margin: 0 auto 40px;
  position: relative;
}

:deep(.ant-input-search-large) {
  --ant-input-height: 56px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.ant-input-search-large:hover) {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-1px);
}

:deep(.ant-input-search-large .ant-input) {
  font-size: 16px;
  padding: 0 24px;
  border: none;
}

:deep(.ant-input-search-large .ant-input-search-button) {
  height: 100%;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border: none;
  transition: all 0.3s;
}

:deep(.ant-input-search-large .ant-input-search-button:hover) {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
  transform: scale(1.02);
}

:deep(.ant-input-search-large .ant-input-search-button:active) {
  transform: scale(0.98);
}

/* 筛选区域样式优化 */
.filter-section {
  background: #ffffff;
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 32px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f5;
}

.filter-row {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-weight: 600;
  margin-right: 20px;
  min-width: 70px;
  color: #1d2129;
  font-size: 15px;
  white-space: nowrap;
}

/* 标签样式优化 */
:deep(.ant-tag-checkable) {
  cursor: pointer;
  border-radius: 20px;
  padding: 6px 16px;
  margin-right: 12px;
  margin-bottom: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 14px;
  border-color: #e5e6eb;
  color: #4e5969;
  background-color: #f7f8fa;
}

:deep(.ant-tag-checkable:hover) {
  border-color: #1890ff;
  color: #1890ff;
  background-color: #e6f7ff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

:deep(.ant-tag-checkable-checked) {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #ffffff;
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

:deep(.ant-tag-checkable-checked:hover) {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
  color: #ffffff;
}

/* 分页样式优化 */
.pagination {
  text-align: center;
  margin-top: 40px;
  padding: 20px 0;
}

:deep(.ant-pagination) {
  font-size: 14px;
}

:deep(.ant-pagination-item) {
  border-radius: 8px !important;
  margin: 0 4px;
  transition: all 0.3s;
}

:deep(.ant-pagination-item:hover) {
  border-color: #1890ff;
  transform: translateY(-2px);
}

:deep(.ant-pagination-item-active) {
  border-color: #1890ff !important;
  background-color: #e6f7ff !important;
}

:deep(.ant-pagination-item-active a) {
  color: #1890ff !important;
  font-weight: 500;
}

:deep(.ant-pagination-prev, .ant-pagination-next) {
  border-radius: 8px !important;
  transition: all 0.3s;
}

:deep(.ant-pagination-prev:hover, .ant-pagination-next:hover) {
  border-color: #1890ff;
  background-color: #f0f9ff;
}

:deep(.ant-pagination-show-size-changer) {
  margin-left: 16px;
}

/* 响应式优化 */
@media (max-width: 1024px) {
  #homePage {
    padding: 24px 16px;
  }

  .filter-section {
    padding: 24px;
  }

  .filter-row {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  #homePage {
    padding: 16px 8px;
  }

  .search-bar {
    max-width: 100%;
    margin-bottom: 24px;
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
    border-radius: 12px;
  }

  .filter-row {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 16px;
  }

  .filter-label {
    margin-bottom: 12px;
    margin-right: 0;
    font-size: 14px;
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
