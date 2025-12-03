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
      <a-button type="primary" size="large" @click="goToCreate">
        <PlusOutlined />
        创作新图案
      </a-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <a-card class="filter-card" :bordered="false">
      <a-row :gutter="[16, 16]">
        <!-- 搜索框 -->
        <a-col :xs="24" :sm="12" :md="8">
          <a-input-search
            v-model:value="searchParams.patternName"
            placeholder="搜索图案名称..."
            allow-clear
            size="large"
            @search="doSearch"
          >
            <template #prefix>
              <SearchOutlined />
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
          <a-button size="large" style="width: 100%" @click="resetSearch">
            <ReloadOutlined />
            重置
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card total-card">
          <a-statistic
            title="总作品数"
            :value="total"
            :prefix="h(FileImageOutlined)"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card approved-card">
          <a-statistic
            title="已通过"
            :value="approvedCount"
            :prefix="h(CheckCircleOutlined)"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card pending-card">
          <a-statistic
            title="待审核"
            :value="pendingCount"
            :prefix="h(ClockCircleOutlined)"
          />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :md="6">
        <a-card class="stat-card rejected-card">
          <a-statistic
            title="已拒绝"
            :value="rejectedCount"
            :prefix="h(CloseCircleOutlined)"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 作品状态分布图表 -->
    <a-card class="chart-card" :bordered="false" title="作品状态分布">
      <div ref="statusChartRef" class="status-chart"></div>
    </a-card>

    <!-- 图案列表 -->
    <a-card class="list-card" :bordered="false">
      <template #title>
        <a-space>
          <AppstoreOutlined />
          <span>作品列表</span>
          <a-tag v-if="!loading" color="blue">{{ total }} 个作品</a-tag>
        </a-space>
      </template>

      <PatternList
        :data-list="dataList"
        :loading="loading"
        :show-op="true"
        :on-reload="fetchData"
      />

      <!-- 空状态 -->
      <a-empty
        v-if="!loading && dataList.length === 0"
        description="暂无作品，快去创作吧！"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
      >
        <a-button type="primary" @click="goToCreate">
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

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      bottom: 0,
    },
    series: [
      {
        name: '作品状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          formatter: '{b}: {c} ({d}%)',
        },
        data: [
          { value: approvedCount.value, name: '已通过' },
          { value: pendingCount.value, name: '待审核' },
          { value: rejectedCount.value, name: '已拒绝' },
        ],
      },
    ],
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
  router.push('/patternGeneration')
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
  background-image: url(https://api.imlcd.cn/bg/gq.php);

  .my-idea-page {
    max-width: 1200px;
    margin: 0 auto;

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding: 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: white;

      .header-content {
        .page-title {
          font-size: 32px;
          font-weight: 700;
          margin: 0 0 8px 0;
          color: white;
          display: flex;
          align-items: center;
          gap: 12px;

          .title-icon {
            font-size: 36px;
          }
        }

        .page-subtitle {
          font-size: 16px;
          margin: 0;
          opacity: 0.9;
        }
      }
    }

    .list-card {
      margin-top: 24px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      :deep(.ant-card-head) {
        border-bottom: 2px solid #f0f0f0;
      }

      :deep(.ant-card-head-title) {
        font-size: 18px;
        font-weight: 600;
      }
    }

    .chart-card {
      margin-top: 16px;
    }

    .status-chart {
      width: 100%;
      height: 320px;
    }

    .filter-card {
      margin-bottom: 24px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      :deep(.ant-card-body) {
        padding: 20px;
      }
    }

    .stats-row {
      margin-bottom: 24px;

      .stat-card {
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        }

        :deep(.ant-statistic-title) {
          font-size: 14px;
          color: #8c8c8c;
          margin-bottom: 8px;
        }

        :deep(.ant-statistic-content) {
          font-size: 28px;
          font-weight: 600;
        }

        &.total-card {
          border-left: 4px solid #1890ff;

          :deep(.ant-statistic-content) {
            color: #1890ff;
          }
        }

        &.approved-card {
          border-left: 4px solid #52c41a;

          :deep(.ant-statistic-content) {
            color: #52c41a;
          }
        }

        &.pending-card {
          border-left: 4px solid #faad14;

          :deep(.ant-statistic-content) {
            color: #faad14;
          }
        }

        &.rejected-card {
          border-left: 4px solid #ff4d4f;

          :deep(.ant-statistic-content) {
            color: #ff4d4f;
          }
        }
      }
    }

    .pagination {
      margin-top: 32px;
      text-align: center;
    }
  }
}

@media (max-width: 768px) {
  #myIdeaPage {
    padding: 16px;

    .my-idea-page {
      .page-header {
        flex-direction: column;
        gap: 16px;
        align-items: flex-start;
        padding: 20px;

        .header-content {
          .page-title {
            font-size: 24px;

            .title-icon {
              font-size: 28px;
            }
          }

          .page-subtitle {
            font-size: 14px;
          }
        }

        .ant-btn {
          width: 100%;
        }
      }

      .stats-row {
        .ant-col {
          margin-bottom: 12px;
        }
      }
    }
  }
}
</style>
