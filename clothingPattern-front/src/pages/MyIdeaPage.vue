<template>
  <div id="myIdeaPage" class="my-idea-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <BulbOutlined class="title-icon" />
          我的创意作品
        </h1>
        <p class="page-subtitle">管理和查看您的所有服装图案创作</p>
      </div>
      <a-button type="primary" size="large" @click="goToCreate" class="create-btn">
        <PlusOutlined />
        创作新图案
      </a-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <a-card class="filter-card" :bordered="false">
      <a-row :gutter="[20, 20]" class="filter-row">
        <!-- 搜索框 -->
        <a-col :xs="24" :sm="12" :md="8">
          <a-input-search
            v-model:value="searchParams.patternName"
            placeholder="搜索图案名称..."
            allow-clear
            size="large"
            @search="doSearch"
            class="search-input"
          >
            <template #prefix>
              <SearchOutlined class="search-icon" />
            </template>
          </a-input-search>
        </a-col>

        <!-- 生成类型筛选 -->
        <a-col :xs="24" :sm="12" :md="5">
          <a-select
            v-model:value="searchParams.generationType"
            placeholder="生成类型"
            allow-clear
            size="large"
            style="width: 100%"
            @change="doSearch"
            class="filter-select"
          >
            <a-select-option
              v-for="option in GENERATION_TYPE_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-col>

        <!-- 审核状态筛选 -->
        <a-col :xs="24" :sm="12" :md="5">
          <a-select
            v-model:value="searchParams.auditStatus"
            placeholder="审核状态"
            allow-clear
            size="large"
            style="width: 100%"
            @change="doSearch"
            class="filter-select"
          >
            <a-select-option
              v-for="option in AUDIT_STATUS_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-col>

        <!-- 风格筛选 -->
        <a-col :xs="24" :sm="12" :md="4">
          <a-select
            v-model:value="searchParams.style"
            placeholder="风格"
            allow-clear
            size="large"
            style="width: 100%"
            @change="doSearch"
            class="filter-select"
          >
            <a-select-option
              v-for="style in styleList"
              :key="style"
              :value="style"
            >
              {{ style }}
            </a-select-option>
          </a-select>
        </a-col>

        <!-- 重置按钮 -->
        <a-col :xs="24" :sm="12" :md="2">
          <a-button size="large" style="width: 100%" @click="resetSearch" class="reset-btn">
            <ReloadOutlined />
            重置
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="20" class="stats-row">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card total-card">
          <div class="stat-header">
            <FileImageOutlined class="stat-icon" />
            <span class="stat-title">总作品数</span>
          </div>
          <div class="stat-value">{{ total }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card approved-card">
          <div class="stat-header">
            <CheckCircleOutlined class="stat-icon" />
            <span class="stat-title">已通过</span>
          </div>
          <div class="stat-value">{{ approvedCount }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card pending-card">
          <div class="stat-header">
            <ClockCircleOutlined class="stat-icon" />
            <span class="stat-title">待审核</span>
          </div>
          <div class="stat-value">{{ pendingCount }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card rejected-card">
          <div class="stat-header">
            <CloseCircleOutlined class="stat-icon" />
            <span class="stat-title">已拒绝</span>
          </div>
          <div class="stat-value">{{ rejectedCount }}</div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 作品状态分布图表 -->
    <a-card class="chart-card" :bordered="false" title="作品状态分布">
      <div class="chart-container">
        <div ref="statusChartRef" class="status-chart"></div>
      </div>
    </a-card>

    <!-- 图案列表 -->
    <a-card class="list-card" :bordered="false">
      <template #title>
        <a-space class="list-title">
          <AppstoreOutlined class="list-icon" />
          <span>作品列表</span>
          <a-tag v-if="!loading" color="blue" class="count-tag">{{ total }} 个作品</a-tag>
        </a-space>
      </template>

      <PatternList
        :data-list="dataList"
        :loading="loading"
        :show-op="true"
        :on-reload="fetchData"
        class="pattern-list-component"
      />

      <!-- 空状态 -->
      <a-empty
        v-if="!loading && dataList.length === 0"
        description="暂无作品，快去创作吧！"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
        class="empty-state"
      >
        <a-button type="primary" @click="goToCreate" class="empty-create-btn">
          <PlusOutlined />
          开始创作
        </a-button>
      </a-empty>

      <!-- 分页 -->
      <a-pagination
        v-if="total > 0"
        class="pagination"
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        :show-size-changer="true"
        :show-total="(total: number) => `共 ${total} 个作品`"
        :page-size-options="['8', '12', '16', '24', '32']"
        @change="onPageChange"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import {
  BulbOutlined,
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  FileImageOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  AppstoreOutlined,
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import { listMyPatternVoByPage } from '@/api/patternController'
import PatternList from '@/components/PatternList.vue'
import {
  AUDIT_STATUS_OPTIONS,
  GENERATION_TYPE_OPTIONS,
  AUDIT_STATUS_ENUM,
} from '@/constants/pattern'

const router = useRouter()

// 数据
const dataList = ref<API.PatternVO[]>([])
const total = ref(0)
const loading = ref(true)

// 统计数据（独立存储，不依赖当前页数据）
const approvedCount = ref(0)
const pendingCount = ref(0)
const rejectedCount = ref(0)

// ECharts 图表实例
const statusChartRef = ref<HTMLDivElement | null>(null)
let statusChart: echarts.ECharts | null = null

// 风格列表（与 HomePage 保持一致）
const styleList = ['简约','可爱', '复古', '民族', '抽象', '未来']

// 搜索条件
const searchParams = reactive<API.PatternQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取统计数据（获取所有数据用于统计）
const fetchStatistics = async () => {
  try {
    // 请求所有数据（设置一个大的 pageSize）
    const res = await listMyPatternVoByPage({
      current: 1,
      pageSize: 1000, // 获取所有数据用于统计
      sortField: 'createTime',
      sortOrder: 'descend',
      // 不带任何筛选条件，获取所有状态的数据
    })

    if (res.data.code === 0 && res.data.data) {
      const allData = res.data.data.records ?? []

      // 统计各状态数量
      approvedCount.value = allData.filter(
        (item) => item.auditStatus === AUDIT_STATUS_ENUM.APPROVED
      ).length

      pendingCount.value = allData.filter(
        (item) => item.auditStatus === AUDIT_STATUS_ENUM.PENDING
      ).length

      rejectedCount.value = allData.filter(
        (item) => item.auditStatus === AUDIT_STATUS_ENUM.REJECTED
      ).length

      // 统计更新后重新渲染图表
      await nextTick()
      renderStatusChart()
    }
  } catch (error: any) {
    console.error('获取统计数据失败：', error.message)
  }
}

// 渲染作品状态分布图表
const renderStatusChart = () => {
  if (!statusChartRef.value) {
    return
  }

  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }

  // 图表配色（与统计卡片保持一致）
  const chartColors = {
    approved: '#36d399',
    pending: '#fbbd23',
    rejected: '#f87272'
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8f4f8',
      borderWidth: 1,
      borderRadius: 8,
      padding: 12,
      textStyle: {
        color: '#2d3748',
        fontSize: 14
      },
      formatter: '{b}: {c} 个 ({d}%)',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)'
    },
    legend: {
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        color: '#4a5568',
        fontSize: 13
      },
      padding: [0, 0, 16, 0]
    },
    series: [
      {
        name: '作品状态',
        type: 'pie',
        radius: ['45%', '75%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#ffffff',
          borderWidth: 3,
          shadowBlur: 8,
          shadowOffsetX: 0,
          shadowOffsetY: 2,
          shadowColor: 'rgba(0, 0, 0, 0.05)'
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 600,
            color: '#2d3748'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: approvedCount.value,
            name: '已通过',
            itemStyle: {
              color: chartColors.approved
            }
          },
          {
            value: pendingCount.value,
            name: '待审核',
            itemStyle: {
              color: chartColors.pending
            }
          },
          {
            value: rejectedCount.value,
            name: '已拒绝',
            itemStyle: {
              color: chartColors.rejected
            }
          },
        ],
      },
    ],
    graphic: {
      elements: [
        {
          type: 'text',
          left: 'center',
          top: '45%',
          style: {
            text: '总计',
            fontSize: 14,
            fontWeight: 500,
            color: '#718096'
          }
        },
        {
          type: 'text',
          left: 'center',
          top: '55%',
          style: {
            text: `${approvedCount.value + pendingCount.value + rejectedCount.value}`,
            fontSize: 24,
            fontWeight: 700,
            color: '#2d3748'
          }
        }
      ]
    }
  }

  statusChart.setOption(option)
  statusChart.resize()
}

// 窗口缩放自适应
const handleResize = () => {
  if (statusChart) {
    statusChart.resize()
  }
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMyPatternVoByPage(searchParams)
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0

      // 同时更新统计数据
      await fetchStatistics()
    } else {
      message.error('获取数据失败：' + res.data.message)
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

// 重置搜索
const resetSearch = () => {
  searchParams.patternName = undefined
  searchParams.generationType = undefined
  searchParams.auditStatus = undefined
  searchParams.style = undefined
  searchParams.season = undefined
  doSearch()
}

// 分页变化
const onPageChange = () => {
  fetchData()
}

// 跳转创作页面
const goToCreate = () => {
  router.push('/mj/generation')
}

// 页面加载
onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (statusChart) {
    statusChart.dispose()
    statusChart = null
  }
})
</script>

<style scoped lang="scss">
#myIdeaPage {
  padding: 24px;
  min-height: 100vh;
  background-color: #f8f9fa;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  .my-idea-page {
    max-width: 1400px;
    margin: 0 auto;

    // 页面头部
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 32px;
      padding: 32px 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 16px;
      color: white;
      box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 12px 36px rgba(102, 126, 234, 0.3);
      }

      .header-content {
        .page-title {
          font-size: 36px;
          font-weight: 700;
          margin: 0 0 12px 0;
          color: white;
          display: flex;
          align-items: center;
          gap: 16px;
          text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

          .title-icon {
            font-size: 40px;
          }
        }

        .page-subtitle {
          font-size: 18px;
          margin: 0;
          opacity: 0.9;
          text-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
        }
      }

      .create-btn {
        background: linear-gradient(135deg, #4cc9f0 0%, #4895ef 100%) !important;
        border: none !important;
        border-radius: 12px !important;
        padding: 0 24px !important;
        height: 52px !important;
        font-size: 16px !important;
        font-weight: 600 !important;
        box-shadow: 0 4px 16px rgba(76, 201, 240, 0.3);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 8px 24px rgba(76, 201, 240, 0.4);
          background: linear-gradient(135deg, #43bffa 0%, #3a86ff 100%) !important;
        }

        &:active {
          transform: translateY(-1px);
        }
      }
    }

    // 筛选卡片
    .filter-card {
      margin-bottom: 32px;
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
      background-color: #ffffff;
      overflow: hidden;

      :deep(.ant-card-body) {
        padding: 24px;
      }

      .filter-row {
        align-items: center;
      }

      .search-input {
        :deep(.ant-input-affix-wrapper) {
          border-radius: 12px !important;
          padding: 0 16px !important;
          height: 52px !important;
          border: 1px solid #e8f4f8 !important;
          background-color: #fafafa !important;
          transition: all 0.3s;

          &:hover, &:focus-within {
            border-color: #4299e1 !important;
            box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1) !important;
          }
        }

        .search-icon {
          color: #718096;
          font-size: 18px;
        }

        :deep(.ant-input) {
          font-size: 15px !important;
          color: #2d3748 !important;
        }

        :deep(.ant-input::placeholder) {
          color: #a0aec0 !important;
        }
      }

      .filter-select {
        :deep(.ant-select-selector) {
          border-radius: 12px !important;
          height: 52px !important;
          border: 1px solid #e8f4f8 !important;
          background-color: #fafafa !important;
          transition: all 0.3s;

          &:hover, &:focus-within {
            border-color: #4299e1 !important;
            box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1) !important;
          }
        }

        :deep(.ant-select-selection-item) {
          font-size: 15px !important;
          color: #2d3748 !important;
        }

        :deep(.ant-select-placeholder) {
          color: #a0aec0 !important;
          font-size: 15px !important;
        }
      }

      .reset-btn {
        border-radius: 12px !important;
        height: 52px !important;
        font-size: 15px !important;
        border: 1px solid #e8f4f8 !important;
        color: #4a5568 !important;
        transition: all 0.3s;

        &:hover {
          border-color: #4299e1 !important;
          color: #4299e1 !important;
          background-color: #f0f8fb !important;
        }
      }
    }

    // 统计卡片行
    .stats-row {
      margin-bottom: 32px;

      .stat-card {
        border-radius: 16px;
        border: none !important;
        padding: 24px;
        background-color: #ffffff;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        overflow: hidden;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 4px;
          height: 100%;
        }

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
        }

        .stat-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 16px;
        }

        .stat-icon {
          font-size: 24px;
        }

        .stat-title {
          font-size: 16px;
          font-weight: 500;
          color: #718096;
        }

        .stat-value {
          font-size: 36px;
          font-weight: 700;
          line-height: 1.2;
        }

        // 总作品数卡片
        &.total-card {
          &::before {
            background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%);
          }

          .stat-icon {
            color: #4299e1;
          }

          .stat-value {
            background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
          }
        }

        // 已通过卡片
        &.approved-card {
          &::before {
            background: linear-gradient(135deg, #36d399 0%, #22c55e 100%);
          }

          .stat-icon {
            color: #36d399;
          }

          .stat-value {
            background: linear-gradient(135deg, #36d399 0%, #22c55e 100%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
          }
        }

        // 待审核卡片
        &.pending-card {
          &::before {
            background: linear-gradient(135deg, #fbbd23 0%, #f59e0b 100%);
          }

          .stat-icon {
            color: #fbbd23;
          }

          .stat-value {
            background: linear-gradient(135deg, #fbbd23 0%, #f59e0b 100%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
          }
        }

        // 已拒绝卡片
        &.rejected-card {
          &::before {
            background: linear-gradient(135deg, #f87272 0%, #ef4444 100%);
          }

          .stat-icon {
            color: #f87272;
          }

          .stat-value {
            background: linear-gradient(135deg, #f87272 0%, #ef4444 100%);
            -webkit-background-clip: text;
            background-clip: text;
            color: transparent;
          }
        }
      }
    }

    // 图表卡片
    .chart-card {
      margin-bottom: 32px;
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
      background-color: #ffffff;

      :deep(.ant-card-head) {
        border-bottom: 1px solid #f0f2f5;
        padding: 16px 24px;

        :deep(.ant-card-head-title) {
          font-size: 18px;
          font-weight: 600;
          color: #2d3748;
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      :deep(.ant-card-body) {
        padding: 24px;
      }

      .chart-container {
        width: 100%;
        height: 360px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .status-chart {
        width: 100%;
        height: 100%;
        min-width: 300px;
        max-width: 500px;
      }
    }

    // 列表卡片
    .list-card {
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
      background-color: #ffffff;
      overflow: hidden;

      :deep(.ant-card-head) {
        border-bottom: 1px solid #f0f2f5;
        padding: 16px 24px;
      }

      .list-title {
        display: flex;
        align-items: center;
        gap: 12px;

        .list-icon {
          color: #4299e1;
          font-size: 20px;
        }

        span {
          font-size: 18px;
          font-weight: 600;
          color: #2d3748;
        }

        .count-tag {
          height: 24px;
          line-height: 24px;
          padding: 0 10px;
          font-size: 13px;
          background-color: #e8f4f8;
          color: #4299e1;
          border-color: #e8f4f8;
        }
      }

      .pattern-list-component {
        padding: 16px 0;
      }

      // 空状态
      .empty-state {
        padding: 64px 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        :deep(.ant-empty-image) {
          margin-bottom: 24px;

          img {
            width: 160px !important;
          }
        }

        :deep(.ant-empty-description) {
          font-size: 16px;
          color: #718096;
          margin-bottom: 24px;
        }

        .empty-create-btn {
          background: linear-gradient(135deg, #4299e1 0%, #38b2ac 100%) !important;
          border: none !important;
          border-radius: 8px !important;
          padding: 0 20px !important;
          height: 44px !important;
          font-size: 15px !important;
          font-weight: 500 !important;
          transition: all 0.3s;

          &:hover {
            background: linear-gradient(135deg, #3182ce 0%, #319795 100%) !important;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(66, 153, 225, 0.2);
          }
        }
      }

      // 分页
      .pagination {
        margin-top: 32px;
        margin-bottom: 16px;
        text-align: center;

        :deep(.ant-pagination) {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
        }

        :deep(.ant-pagination-item) {
          border-radius: 8px !important;
          border: 1px solid #e8f4f8 !important;
          min-width: 40px !important;
          height: 40px !important;
          line-height: 40px !important;
          margin: 0 !important;
          transition: all 0.3s;

          &:hover {
            border-color: #4299e1 !important;
            background-color: #f0f8fb !important;
          }

          &.ant-pagination-item-active {
            background-color: #4299e1 !important;
            border-color: #4299e1 !important;
            color: white !important;

            &:hover {
              background-color: #3182ce !important;
              border-color: #3182ce !important;
            }
          }
        }

        :deep(.ant-pagination-prev, .ant-pagination-next) {
          border-radius: 8px !important;
          border: 1px solid #e8f4f8 !important;
          width: 40px !important;
          height: 40px !important;
          line-height: 40px !important;
          margin: 0 !important;
          transition: all 0.3s;

          &:hover {
            border-color: #4299e1 !important;
            background-color: #f0f8fb !important;
          }

          &.ant-pagination-disabled {
            border-color: #f0f2f5 !important;
            background-color: #fafafa !important;
            color: #cbd5e0 !important;
          }
        }

        :deep(.ant-pagination-jump-prev, .ant-pagination-jump-next) {
          margin: 0 4px !important;
        }

        :deep(.ant-pagination-show-size-changer) {
          margin-left: 16px !important;
        }

        :deep(.ant-select-selector) {
          border-radius: 8px !important;
          border: 1px solid #e8f4f8 !important;
        }

        :deep(.ant-pagination-total-text) {
          font-size: 14px;
          color: #4a5568;
          margin-right: 16px;
        }
      }
    }
  }
}

// 响应式适配
@media (max-width: 1200px) {
  #myIdeaPage {
    padding: 20px;

    .my-idea-page {
      .page-header {
        padding: 28px 32px;

        .header-content {
          .page-title {
            font-size: 32px;

            .title-icon {
              font-size: 36px;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 992px) {
  #myIdeaPage {
    .my-idea-page {
      .stats-row {
        .stat-card {
          padding: 20px;

          .stat-value {
            font-size: 32px;
          }
        }
      }

      .chart-container {
        height: 320px;
      }
    }
  }
}

@media (max-width: 768px) {
  #myIdeaPage {
    padding: 16px;

    .my-idea-page {
      .page-header {
        flex-direction: column;
        gap: 20px;
        align-items: flex-start;
        padding: 24px;

        .header-content {
          .page-title {
            font-size: 28px;

            .title-icon {
              font-size: 32px;
            }
          }

          .page-subtitle {
            font-size: 16px;
          }
        }

        .create-btn {
          width: 100% !important;
        }
      }

      .filter-card {
        :deep(.ant-card-body) {
          padding: 16px;
        }
      }

      .stats-row {
        .ant-col {
          margin-bottom: 16px;
        }

        .stat-card {
          padding: 18px;

          .stat-header {
            margin-bottom: 12px;
          }

          .stat-value {
            font-size: 28px;
          }
        }
      }

      .chart-container {
        height: 280px;
      }

      .list-card {
        :deep(.ant-card-head) {
          padding: 12px 16px;
        }

        .list-title {
          span {
            font-size: 16px;
          }
        }

        .empty-state {
          padding: 48px 0;

          :deep(.ant-empty-image) {
            img {
              width: 120px !important;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 480px) {
  #myIdeaPage {
    padding: 12px;

    .my-idea-page {
      .page-header {
        padding: 20px 16px;

        .header-content {
          .page-title {
            font-size: 24px;

            .title-icon {
              font-size: 28px;
            }
          }
        }
      }

      .filter-card {
        .search-input, .filter-select, .reset-btn {
          :deep(.ant-input-affix-wrapper),
          :deep(.ant-select-selector),
          & {
            height: 48px !important;
          }
        }
      }

      .chart-container {
        height: 240px;
      }

      .pagination {
        :deep(.ant-pagination) {
          flex-wrap: wrap;
        }

        :deep(.ant-pagination-show-size-changer) {
          margin-left: 0 !important;
          margin-top: 12px !important;
          width: 100%;
        }
      }
    }
  }
}

// 加载状态优化
:deep(.ant-spin-dot-item) {
  background-color: #4299e1 !important;
}

:deep(.ant-list-loading) {
  padding: 64px 0;
}
</style>
