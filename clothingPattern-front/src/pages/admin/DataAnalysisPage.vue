<template>
  <div id="dataAnalysisPage">
    <a-page-header title="数据分析中心" sub-title="平台核心数据监控与分析报告" />

    <!-- 核心指标监控 -->
    <a-card title="核心指标监控" class="section-card" :bordered="false">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic
            title="总图案数"
            :value="statisticsData?.totalPatterns"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <PictureOutlined />
            </template>
          </a-statistic>
          <div class="growth-rate" v-if="growthRates.patterns !== undefined">
            <template v-if="growthRates.patterns >= 0">
              <ArrowUpOutlined style="color: #52c41a" />
              <span style="color: #52c41a">{{ growthRates.patterns.toFixed(1) }}%</span>
            </template>
            <template v-else>
              <ArrowDownOutlined style="color: #ff4d4f" />
              <span style="color: #ff4d4f">{{ Math.abs(growthRates.patterns).toFixed(1) }}%</span>
            </template>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic
            title="总用户数"
            :value="statisticsData?.totalUsers"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
          <div class="growth-rate" v-if="growthRates.users !== undefined">
            <template v-if="growthRates.users >= 0">
              <ArrowUpOutlined style="color: #52c41a" />
              <span style="color: #52c41a">{{ growthRates.users.toFixed(1) }}%</span>
            </template>
            <template v-else>
              <ArrowDownOutlined style="color: #ff4d4f" />
              <span style="color: #ff4d4f">{{ Math.abs(growthRates.users).toFixed(1) }}%</span>
            </template>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic title="今日新增" :value="todayCount" :value-style="{ color: '#faad14' }">
            <template #prefix>
              <FireOutlined />
            </template>
          </a-statistic>
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-statistic title="热门风格" :value="topStyle">
            <template #prefix>
              <CrownOutlined />
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </a-card>

    <!-- 数据统计图表 -->
    <a-row :gutter="16" class="chart-row">
      <!-- 热门风格分布 -->
      <a-col :xs="24" :md="8">
        <a-card title="热门风格分布" class="chart-card">
          <div
            ref="styleChartRef"
            class="chart-container"
            :style="{ height: '300px', width: '100%' }"
          >
            <a-empty
              v-if="
                !statisticsData?.styleDistribution ||
                Object.keys(statisticsData.styleDistribution).length === 0
              "
              description="暂无数据"
            />
          </div>
        </a-card>
      </a-col>

      <!-- 活跃用户排行 -->
      <a-col :xs="24" :md="8">
        <a-card title="活跃用户TOP5" class="chart-card">
          <div
            ref="userChartRef"
            class="chart-container"
            :style="{ height: '300px', width: '100%' }"
          >
            <a-empty
              v-if="!statisticsData?.activeUsers || statisticsData.activeUsers.length === 0"
              description="暂无数据"
            />
          </div>
        </a-card>
      </a-col>

      <!-- 创作趋势 -->
      <a-col :xs="24" :md="8">
        <a-card title="创作趋势（最近7天）" class="chart-card">
          <div
            ref="trendChartRef"
            class="chart-container"
            :style="{ height: '300px', width: '100%' }"
          >
            <a-empty
              v-if="!statisticsData?.trendData || statisticsData.trendData.length === 0"
              description="暂无数据"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户行为分析 -->
    <a-card title="用户行为分析" class="section-card">
      <a-row :gutter="16">
        <a-col :xs="24" :md="12">
          <a-card title="生成方式分布" :bordered="false" size="small">
            <div
              ref="generationChartRef"
              class="chart-container"
              :style="{ height: '300px', width: '100%' }"
            ></div>
          </a-card>
        </a-col>
        <a-col :xs="24" :md="12">
          <a-card title="用户创作活跃度分析" :bordered="false" size="small">
            <a-descriptions :column="1" size="small">
              <a-descriptions-item label="活跃用户">
                {{ activeUserStats.active }}
              </a-descriptions-item>
              <a-descriptions-item label="普通用户">
                {{ activeUserStats.normal }}
              </a-descriptions-item>
              <a-descriptions-item label="沉睡用户">
                {{ activeUserStats.inactive }}
              </a-descriptions-item>
              <a-descriptions-item label="活跃率">
                <a-tag color="green">{{ activeUserStats.activeRate }}%</a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>
      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :xs="24" :md="24">
          <a-card title="风格趋势变化" :bordered="false" size="small">
            <div
              ref="styleTrendChartRef"
              class="chart-container"
              :style="{ height: '400px', width: '100%' }"
            ></div>
          </a-card>
        </a-col>
      </a-row>
    </a-card>

    <!-- 数据报告导出 -->
    <a-card title="数据报告导出" class="section-card">
      <a-space direction="vertical" :size="16" style="width: 100%">
        <a-alert
          message="导出说明"
          description="选择导出格式和时间范围，系统将生成详细的数据分析报告"
          type="info"
          show-icon
        />

        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-select
              v-model:value="exportConfig.format"
              style="width: 100%"
              placeholder="选择导出格式"
            >
              <a-select-option value="excel">Excel表格</a-select-option>
              <a-select-option value="pdf">PDF文档</a-select-option>
              <a-select-option value="csv">CSV文件</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-range-picker
              v-model:value="exportConfig.dateRange"
              style="width: 100%"
              :placeholder="['开始日期', '结束日期']"
            />
          </a-col>
          <a-col :xs="24" :md="8">
            <a-space>
              <a-button
                type="primary"
                :icon="h(DownloadOutlined)"
                :loading="exportLoading"
                @click="handleExport"
              >
                导出报告
              </a-button>
              <a-button @click="resetExportConfig">重置</a-button>
            </a-space>
          </a-col>
        </a-row>

        <a-divider />

        <a-descriptions title="报告包含内容" :column="{ xs: 1, sm: 2, md: 3 }" bordered>
          <a-descriptions-item label="作品统计"> 总数、增长趋势、分类分布 </a-descriptions-item>
          <a-descriptions-item label="用户分析"> 用户总数、活跃度、创作排行 </a-descriptions-item>
          <a-descriptions-item label="风格分析"> 热门风格、风格分布、趋势变化 </a-descriptions-item>
          <a-descriptions-item label="时间分析"> 日/周/月趋势图、高峰时段 </a-descriptions-item>
          <a-descriptions-item label="行为分析"> 生成方式偏好、使用习惯 </a-descriptions-item>
          <a-descriptions-item label="质量指标"> 审核通过率、平均质量分 </a-descriptions-item>
        </a-descriptions>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, h, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  PictureOutlined,
  UserOutlined,
  FireOutlined,
  CrownOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { getHomeStatistics } from '@/api/homeController'
import type { Dayjs } from 'dayjs'
import * as echarts from 'echarts'

// 统计数据
const statisticsData = ref<API.HomeStatisticsVO>()
const exportLoading = ref(false)

// 增长率数据（模拟）
const growthRates = ref({
  patterns: 12.5,
  users: 8.3,
})

// 导出配置
const exportConfig = ref({
  format: 'excel',
  dateRange: undefined as [Dayjs, Dayjs] | undefined,
})

// 生成方式分布（模拟数据，实际应该从后端获取）
const generationTypePercent = ref({
  text: 65,
  image: 35,
})

// 用户活跃度统计（模拟数据）
const activeUserStats = ref({
  active: 0,
  normal: 0,
  inactive: 0,
  activeRate: 0,
})

// 计算属性：今日新增
const todayCount = computed(() => {
  if (!statisticsData.value?.trendData || statisticsData.value.trendData.length === 0) {
    return 0
  }
  return statisticsData.value.trendData[statisticsData.value.trendData.length - 1]?.count || 0
})

// 计算属性：最热门风格
const topStyle = computed(() => {
  if (!statisticsData.value?.styleDistribution) {
    return '无'
  }
  const styles = Object.entries(statisticsData.value.styleDistribution)
  if (styles.length === 0) {
    return '无'
  }
  const maxEntry = styles.reduce((max, curr) =>
    (curr[1] as number) > (max[1] as number) ? curr : max,
  )
  return maxEntry[0]
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getHomeStatistics()
    if (res.data.code === 0 && res.data.data) {
      statisticsData.value = res.data.data
      // 计算用户活跃度
      calculateActiveUserStats()
    }
  } catch (error: any) {
    message.error('获取统计数据失败：' + error.message)
  }
}

// 计算用户活跃度
const calculateActiveUserStats = () => {
  if (!statisticsData.value?.activeUsers || !statisticsData.value.totalUsers) {
    return
  }

  const totalUsers = statisticsData.value.totalUsers
  const activeCount = statisticsData.value.activeUsers.filter(
    (u) => (u.patternCount || 0) >= 5,
  ).length
  const normalCount = statisticsData.value.activeUsers.filter(
    (u) => (u.patternCount || 0) > 0 && (u.patternCount || 0) < 5,
  ).length
  const inactiveCount = totalUsers - activeCount - normalCount

  activeUserStats.value = {
    active: activeCount,
    normal: normalCount,
    inactive: inactiveCount > 0 ? inactiveCount : 0,
    activeRate: totalUsers > 0 ? Math.round((activeCount / totalUsers) * 100) : 0,
  }
}

// 计算风格百分比
const getStylePercent = (count: number) => {
  if (!statisticsData.value?.totalPatterns || statisticsData.value.totalPatterns === 0) {
    return 0
  }
  return Math.round((count / statisticsData.value.totalPatterns) * 100)
}

// 计算趋势百分比
const getTrendPercent = (count: number | undefined) => {
  if (!count || !statisticsData.value?.trendData) {
    return 0
  }
  const maxCount = Math.max(...statisticsData.value.trendData.map((item) => item.count || 0))
  if (maxCount === 0) {
    return 0
  }
  return Math.round((count / maxCount) * 100)
}

// 格式化日期
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

// 导出报告
const handleExport = async () => {
  if (!exportConfig.value.format) {
    message.warning('请选择导出格式')
    return
  }

  exportLoading.value = true

  try {
    // 准备请求参数
    const params = {
      format: exportConfig.value.format,
      startDate: exportConfig.value.dateRange?.[0]?.format('YYYY-MM-DD'),
      endDate: exportConfig.value.dateRange?.[1]?.format('YYYY-MM-DD'),
    }

    // 调用后端导出接口
    const response = await fetch('http://localhost:8123/api/home/data/export', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // 携带cookie
      body: JSON.stringify(params),
    })

    if (!response.ok) {
      throw new Error('导出失败')
    }

    // 获取文件blob
    const blob = await response.blob()

    // 确定文件扩展名
    let fileExtension = exportConfig.value.format
    if (exportConfig.value.format === 'excel') {
      fileExtension = 'xlsx'
    }

    // 生成文件名
    const fileName = `数据报告_${new Date().getTime()}.${fileExtension}`

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()

    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success(`报告导出成功（${exportConfig.value.format.toUpperCase()}格式）`)
  } catch (error: any) {
    message.error('导出失败：' + error.message)
  } finally {
    exportLoading.value = false
  }
}

// 重置导出配置
const resetExportConfig = () => {
  exportConfig.value = {
    format: 'excel',
    dateRange: undefined,
  }
}

// 图表引用
const styleChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
const generationChartRef = ref<HTMLElement>()
const styleTrendChartRef = ref<HTMLElement>()

// 图表实例
let styleChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null
let generationChart: echarts.ECharts | null = null
let styleTrendChart: echarts.ECharts | null = null

// 初始化图表
const initCharts = () => {
  if (styleChartRef.value) {
    styleChart = echarts.init(styleChartRef.value)
  }
  if (userChartRef.value) {
    userChart = echarts.init(userChartRef.value)
  }
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (generationChartRef.value) {
    generationChart = echarts.init(generationChartRef.value)
  }
  if (styleTrendChartRef.value) {
    styleTrendChart = echarts.init(styleTrendChartRef.value)
  }
}

// 更新图表数据
const updateCharts = () => {
  // 热门风格分布 - 饼图
  if (styleChart && statisticsData.value?.styleDistribution) {
    const styleData = Object.entries(statisticsData.value.styleDistribution).map(
      ([name, value]) => ({
        name,
        value,
      }),
    )

    const styleOption: echarts.EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
      },
      series: [
        {
          name: '风格分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: {
            show: false,
            position: 'center',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: false,
          },
          data: styleData,
        },
      ],
    }
    styleChart.setOption(styleOption)
  }

  // 活跃用户排行 - 柱状图
  if (userChart && statisticsData.value?.activeUsers) {
    const userData = statisticsData.value.activeUsers.slice(0, 5)
    const userNames = userData.map((item) => item.user?.userName || '未知用户')
    const patternCounts = userData.map((item) => item.patternCount || 0)

    const userOption: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      xAxis: {
        type: 'category',
        data: userNames,
        axisLabel: {
          rotate: 45,
          fontSize: 10,
        },
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          data: patternCounts,
          type: 'bar',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' },
            ]),
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#2378f7' },
                { offset: 0.7, color: '#2378f7' },
                { offset: 1, color: '#83bff6' },
              ]),
            },
          },
        },
      ],
    }
    userChart.setOption(userOption)
  }

  // 创作趋势 - 折线图
  if (trendChart && statisticsData.value?.trendData) {
    const dates = statisticsData.value.trendData.map((item) => formatDate(item.date))
    const counts = statisticsData.value.trendData.map((item) => item.count || 0)

    const trendOption: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: dates,
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          data: counts,
          type: 'line',
          smooth: true,
          itemStyle: {
            color: '#52c41a',
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(82, 196, 26, 0.5)' },
              { offset: 1, color: 'rgba(82, 196, 26, 0.1)' },
            ]),
          },
        },
      ],
    }
    trendChart.setOption(trendOption)
  }

  // 生成方式分布 - 环形图
  if (generationChart) {
    const generationOption: echarts.EChartsOption = {
      tooltip: {
        trigger: 'item',
      },
      series: [
        {
          name: '生成方式',
          type: 'pie',
          radius: ['50%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: true,
            formatter: '{b}: {c} ({d}%)',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold',
            },
          },
          data: [
            {
              value: generationTypePercent.value.text,
              name: '文字生成',
              itemStyle: { color: '#1890ff' },
            },
            {
              value: generationTypePercent.value.image,
              name: '图片参考',
              itemStyle: { color: '#52c41a' },
            },
          ],
        },
      ],
    }
    generationChart.setOption(generationOption)
  }

  // 风格趋势变化 - 柱状图
  if (styleTrendChart && statisticsData.value?.trendData) {
    // 模拟风格趋势数据（实际应从后端获取）
    const styleTrendData = [
      { name: '简约', data: [12, 19, 3, 5, 2, 3, 20] },
      { name: '复古', data: [2, 3, 20, 15, 10, 13, 23] },
      { name: '卡通', data: [15, 10, 13, 23, 20, 15, 10] },
    ]

    const trendOption: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      legend: {
        data: styleTrendData.map((item) => item.name),
      },
      xAxis: {
        type: 'category',
        data: statisticsData.value.trendData.map((item) => formatDate(item.date)),
      },
      yAxis: {
        type: 'value',
      },
      series: styleTrendData.map((item) => ({
        name: item.name,
        type: 'bar',
        data: item.data,
      })),
    }
    styleTrendChart.setOption(trendOption)
  }
}

// 监听窗口大小变化，调整图表大小
const handleResize = () => {
  styleChart?.resize()
  userChart?.resize()
  trendChart?.resize()
  generationChart?.resize()
  styleTrendChart?.resize()
}

// 页面加载时获取数据
onMounted(async () => {
  await fetchStatistics()
  initCharts()
  updateCharts()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除事件监听和销毁图表
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // 销毁图表实例，避免内存泄漏
  styleChart?.dispose()
  userChart?.dispose()
  trendChart?.dispose()
  generationChart?.dispose()
  styleTrendChart?.dispose()
})

// 监听统计数据变化，更新图表
watch(
  statisticsData,
  () => {
    updateCharts()
  },
  { deep: true },
)
</script>

<style scoped lang="scss">
#dataAnalysisPage {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .section-card {
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border-radius: 8px;

    :deep(.ant-card-head) {
      border-bottom: 2px solid #1890ff;
    }

    .growth-rate {
      margin-top: 8px;
      font-size: 14px;
    }
  }

  .chart-row {
    margin-bottom: 24px;

    .chart-card {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border-radius: 8px;
      height: 100%;

      .style-distribution,
      .active-users,
      .trend-chart {
        max-height: 300px;
        overflow-y: auto;

        &::-webkit-scrollbar {
          width: 6px;
        }

        &::-webkit-scrollbar-thumb {
          background: #d9d9d9;
          border-radius: 3px;
        }
      }

      .style-item {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .style-name {
          min-width: 60px;
          font-size: 14px;
        }

        .ant-progress {
          flex: 1;
        }

        .style-count {
          min-width: 40px;
          text-align: right;
          font-weight: 500;
          color: #1890ff;
        }
      }

      .user-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 8px;
        transition: all 0.3s;

        &:hover {
          background: #f5f5f5;
        }

        .user-rank {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 600;
          font-size: 12px;
          background: #f0f0f0;
          color: #666;

          &.rank-1 {
            background: linear-gradient(135deg, #ffd700, #ffed4e);
            color: #fff;
          }

          &.rank-2 {
            background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
            color: #fff;
          }

          &.rank-3 {
            background: linear-gradient(135deg, #cd7f32, #daa520);
            color: #fff;
          }
        }

        .user-info {
          flex: 1;

          .user-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
          }

          .user-count {
            font-size: 12px;
            color: #999;
          }
        }
      }

      .trend-item {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .trend-date {
          min-width: 50px;
          font-size: 14px;
          color: #666;
        }

        .ant-progress {
          flex: 1;
        }

        .trend-count {
          min-width: 40px;
          text-align: right;
          font-weight: 500;
          color: #52c41a;
        }
      }
    }
  }

  .behavior-chart {
    .chart-item {
      margin-bottom: 16px;

      span {
        display: block;
        margin-bottom: 8px;
        font-size: 14px;
        color: #666;
      }
    }
  }

  .chart-container {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.ant-empty) {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }
}
</style>
