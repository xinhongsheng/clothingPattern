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
      <!-- 生成方式 -->
      <!-- <div class="filter-row">
        <span class="filter-label">生成方式：</span>
        <a-radio-group v-model:value="searchParams.generationType" button-style="solid" @change="doSearch">
          <a-radio-button value="">全部</a-radio-button>
          <a-radio-button value="TEXT_GENERATED">文字生成</a-radio-button>
          <a-radio-button value="IMAGE_REFERENCED">图片参考</a-radio-button>
        </a-radio-group>
      </div> -->

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
#homePage {
  padding: 24px;
  min-height: 100vh;
  /* background: #f5f5f5; */
  background-image:  url('../assets/backgroundImage/homePage-bg.png');

  .search-bar {
    max-width: 600px;
    margin: 0 auto 32px;
  }

  .filter-section {
    background: white;
    padding: 24px;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .filter-row {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-label {
        font-weight: 500;
        margin-right: 16px;
        min-width: 80px;
        color: #333;
      }

      :deep(.ant-radio-group) {
        .ant-radio-button-wrapper {
          border-radius: 4px;
          margin-right: 8px;

          &:first-child {
            border-radius: 4px;
          }

          &:last-child {
            border-radius: 4px;
          }
        }
      }

      :deep(.ant-tag-checkable) {
        cursor: pointer;
        border-radius: 16px;
        padding: 4px 12px;
        margin-right: 8px;
        margin-bottom: 8px;
        transition: all 0.3s;

        &:hover {
          border-color: #1890ff;
        }

        &.ant-tag-checkable-checked {
          background-color: #1890ff;
          color: white;
        }
      }
    }
  }

  .pagination {
    text-align: center;
    margin-top: 32px;
    padding: 16px 0;
  }
}

@media (max-width: 768px) {
  #homePage {
    padding: 16px;

    .filter-section {
      padding: 16px;

      .filter-row {
        flex-direction: column;
        align-items: flex-start;

        .filter-label {
          margin-bottom: 8px;
        }
      }
    }
  }
}
</style>
