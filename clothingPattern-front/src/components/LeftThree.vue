# LeftThree.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { ref, onMounted, onUnmounted } from 'vue'
import { getArticleTopOne } from '@/api/homeController'

const chartRef = ref(null)
let chartInstance = null
let intervalId = null

// 存储从API获取的数据
let apiData = []

// 从API获取数据
const fetchData = async () => {
  try {
    const response = await getArticleTopOne()
    if (response.data.code === 0 && response.data.data) {
      apiData = response.data.data
      updateChart()
    }
  } catch (error) {
    console.error('获取文章Top数据失败:', error)
  }
}

// 处理API数据并转换为图表所需格式
const processApiData = () => {
  // 提取所有日期
  const allDates = [...new Set(apiData.map((item) => item.date_day))].sort()

  // 组织三个类型的数据
  const viewData = []
  const likeData = []
  const collectData = []

  allDates.forEach((date) => {
    // 查找该日期下的不同类型数据
    const viewItem = apiData.find((item) => item.date_day === date && item.type === 'view')
    const likeItem = apiData.find((item) => item.date_day === date && item.type === 'like')
    const collectItem = apiData.find((item) => item.date_day === date && item.type === 'collect')

    // 填充数据
    viewData.push(viewItem?.count || 0)
    likeData.push(likeItem?.count || 0)
    collectData.push(collectItem?.count || 0)
  })

  // 返回转换后的数据格式
  return [
    {
      name: '浏览量',
      data: viewData,
    },
    {
      name: '点赞',
      data: likeData,
    },
    {
      name: '收藏',
      data: collectData,
    },
  ]
}

const initChart = async () => {
  chartInstance = echarts.init(chartRef.value)
  // 初始获取数据
  await fetchData()
  // 开始定时刷新（5分钟）
  startAutoRefresh()
}

const updateChart = () => {
  const isSmallScreen = window.innerWidth < 768

  // 处理API数据，转换为图表所需格式
  const currentDataGroup = processApiData()

  // 提取所有日期作为X轴数据
  const dates = [...new Set(apiData.map((item) => item.date_day))].sort()

  const option = {
    color: ['#80FFA5', '#00DDFF', '#37A2FF', '#FF0087', '#FFBF00'],
    title: {
      textStyle: {
        color: '#00bfff', // 设置标题文字颜色为科技感蓝色
      },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      borderColor: '#00bfff',
      borderWidth: 1,
      textStyle: {
        color: '#fff',
      },
    },
    legend: {
      data: currentDataGroup.map((item) => item.name),
      textStyle: {
        color: '#00bfff', // 设置图例文字颜色为科技感蓝色
      },
      top: isSmallScreen ? '10%' : '5%',
      left: 'center',
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true,
      backgroundColor: 'rgba(0, 0, 0, 0.1)', // 设置背景颜色为半透明黑色
      borderColor: '#00bfff', // 设置网格边框颜色为科技感蓝色
      borderWidth: 1,
    },
    toolbox: {
      iconStyle: {
        color: '#00bfff', // 设置工具箱图标颜色为科技感蓝色
      },
      backgroundColor: 'rgba(0, 0, 0, 0.7)', // 设置工具箱背景颜色为半透明黑色
      borderColor: '#00bfff', // 设置工具箱边框颜色为科技感蓝色
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#00bfff', // 设置X轴线颜色为科技感蓝色
        },
      },
      axisLabel: {
        color: '#00bfff', // 设置X轴标签颜色为科技感蓝色
        rotate: isSmallScreen ? 45 : 0, // 小屏幕时旋转标签
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.3)', // 设置X轴分割线颜色为半透明黑色
        },
      },
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#00bfff', // 设置Y轴线颜色为科技感蓝色
        },
      },
      axisLabel: {
        color: '#00bfff', // 设置Y轴标签颜色为科技感蓝色
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.3)', // 设置Y轴分割线颜色为半透明黑色
        },
      },
    },
    series: currentDataGroup.map((item) => ({
      name: item.name,
      type: 'line',
      stack: 'Total',
      data: item.data,
      smooth: true, // 使折线平滑
      symbol: 'circle', // 设置数据点符号
      symbolSize: isSmallScreen ? 6 : 8, // 小屏幕时减小数据点大小
      lineStyle: {
        width: isSmallScreen ? 1.5 : 2, // 小屏幕时减小线条宽度
        shadowColor: 'rgba(0, 0, 0, 0.5)',
        shadowBlur: 10,
      },
      itemStyle: {
        color: '#00bfff', // 设置数据点颜色
        borderColor: '#000',
        borderWidth: 1,
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 175, 255, 0.3)' },
          { offset: 1, color: 'rgba(0, 0, 0, 0)' },
        ]),
      },
    })),
  }
  chartInstance.setOption(option)
}

// 开始自动刷新（5分钟）
const startAutoRefresh = () => {
  intervalId = setInterval(
    () => {
      fetchData()
    },
    5 * 60 * 1000,
  ) // 5分钟
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  clearInterval(intervalId)
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', resizeChart)
})

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}
</script>

<style scoped lang="less">
/* 添加一些基本样式以确保图表容器能够正确显示 */
.chart-container {
  width: 100%;
  height: 100%;
}

/* 全屏时的样式 */
:deep(.echarts-fullscreen) {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  background-color: #000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
}

:deep(.echarts-fullscreen .chart) {
  width: 100%;
  height: 100%;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}
</style>
