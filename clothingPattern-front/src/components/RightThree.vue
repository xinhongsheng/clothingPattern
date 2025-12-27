# RightThree.vue

<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getInteraction } from '@/api/homeController'

const chartRef = ref(null)
let chartInstance = null
let timer = null

// 动态数据状态
const chartData = ref({
  patternNames: [], // 显示的图案名称，用于y轴，只显示前3个字符
  fullPatternNames: [], // 完整的图案名称，用于tooltip显示
  scores: [], // 评分，用于柱状图数据
})

// 从后端获取数据
const fetchData = async () => {
  try {
    const response = await getInteraction()
    // 正确访问API响应数据，axios返回完整response对象
    if (response.data.code === 0 && response.data.data) {
      // 提取数据，名称较长的只显示前三个字，后面省略
      const patternNames = []
      const fullPatternNames = []
      const scores = []

      response.data.data.forEach((item) => {
        const name = item.patternName
        patternNames.push(name.length > 3 ? name.substring(0, 3) + '...' : name)
        fullPatternNames.push(name) // 存储完整名称
        scores.push(item.score)
      })

      // 更新数据状态
      chartData.value.patternNames = patternNames
      chartData.value.fullPatternNames = fullPatternNames
      chartData.value.scores = scores

      // 更新图表
      updateChart()
    }
  } catch (error) {
    console.error('获取交互数据失败:', error)
  }
}

const initChart = () => {
  if (!chartRef.value) {
    console.error('图表容器不存在')
    return
  }
  chartInstance = echarts.init(chartRef.value)
  updateChart() // 先显示默认数据
  fetchData() // 然后获取真实数据
}

// 启动数据刷新定时器
const startRefreshTimer = () => {
  // 每五分钟刷新一次数据
  timer = setInterval(
    () => {
      fetchData()
    },
    5 * 60 * 1000,
  )
}

const updateChart = () => {
  if (!chartInstance) {
    console.error('图表实例不存在')
    return
  }

  // 使用默认数据防止图表报错
  const patternNames =
    chartData.value.patternNames.length > 0 ? chartData.value.patternNames : ['暂无数据']
  const scores = chartData.value.scores.length > 0 ? chartData.value.scores : [0]

  const option = {
    color: ['#00bfff'], // 设置柱状图的颜色为科技感蓝色
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      formatter: (params) => {
        if (params && params.length > 0) {
          const index = params[0].dataIndex
          const fullName = chartData.value.fullPatternNames[index] || ''
          const value = params[0].value
          return `${fullName}<br/>评分: ${value}`
        }
        return ''
      },
    },
    yAxis: {
      type: 'category',
      data: patternNames,
      axisLine: {
        lineStyle: {
          color: '#00bfff',
        },
      },
      axisLabel: {
        color: '#00bfff',
        fontSize: getResponsiveFontSize(window.innerWidth, 14, 12), // 根据屏幕宽度调整字体大小
      },
    },
    xAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#00bfff',
        },
      },
      axisLabel: {
        color: '#00bfff',
        fontSize: getResponsiveFontSize(window.innerWidth, 14, 12), // 根据屏幕宽度调整字体大小
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.3)',
        },
      },
    },
    series: [
      {
        data: scores,
        type: 'bar',
        label: {
          show: true,
          position: 'insideRight',
          color: '#fff',
          fontSize: getResponsiveFontSize(window.innerWidth, 12, 10), // 根据屏幕宽度调整字体大小
        },
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
        },
      },
    ],
    grid: {
      left: '10%',
      right: '10%',
      bottom: '10%',
      containLabel: true,
    },
  }

  // 应用配置
  chartInstance.setOption(option)
}

const getResponsiveFontSize = (width, largeSize, smallSize) => {
  return width < 768 ? smallSize : largeSize
}

// 窗口大小变化时调整图表大小
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  // 初始化图表
  initChart()
  // 启动刷新定时器
  startRefreshTimer()
  // 添加窗口大小变化监听
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // 组件卸载时销毁图表实例和清除定时器
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
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
