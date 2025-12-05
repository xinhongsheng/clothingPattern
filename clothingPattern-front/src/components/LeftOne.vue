# LeftOne.vue

<template>
  <div ref="chartRef1" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useWindowSize } from '@vueuse/core' // 使用 vueuse 来获取窗口大小
import { getUserGrowth } from '@/api/homeController' // 导入获取用户增长数据的API

const chartRef1 = ref(null)
let chartInstance = null
let refreshTimer = null

const { width, height } = useWindowSize()

// 存储用户增长数据
const userGrowthData = ref([])

// 获取近15天用户增长数据
const fetchUserGrowthData = async () => {
  try {
    const response = await getUserGrowth()
    if (response.data.code === 0) {
      userGrowthData.value = response.data.data
      updateChart()
    }
  } catch (error) {
    console.error('获取用户增长数据失败:', error)
  }
}

const initChart1 = () => {
  chartInstance = echarts.init(chartRef1.value)
  fetchUserGrowthData() // 获取数据
  resizeChart()
}

const updateChart = () => {
  // 提取日期和用户增长数量
  const dates = userGrowthData.value.map((item) => item.date)
  const counts = userGrowthData.value.map((item) => parseInt(item.count))

  const option = {
    title: {
      
      textStyle: {
        color: '#00bfff',
        fontSize: 16,
        fontWeight: 'bold',
      },
      left: 'center',
      top: 10,
    },
    color: ['#00bfff', '#1e90ff'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: 'rgba(0, 191, 255, 0.8)',
        },
      },
    },
    xAxis: {
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#00bfff',
        },
      },
      axisLabel: {
        color: '#00bfff',
        rotate: 45,
        fontSize: 10,
      },
    },
    yAxis: {
      axisLine: {
        lineStyle: {
          color: '#00bfff',
        },
      },
      axisLabel: {
        color: '#00bfff',
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 191, 255, 0.2)',
        },
      },
    },
    series: [
      {
        name: '用户增长',
        type: 'line',
        data: counts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#00bfff',
        },
        lineStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#00bfff' },
            { offset: 1, color: '#1e90ff' },
          ]),
          width: 3,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 191, 255, 0.3)' },
            { offset: 1, color: 'rgba(0, 191, 255, 0.05)' },
          ]),
        },
        label: {
          show: true,
          position: 'top',
          color: '#00bfff',
          formatter: '{c}',
        },
      },
    ],
    graphic: [
      {
        type: 'group',
        right: 20,
        top: 20,
        children: [
          {
            type: 'rect',
            shape: {
              width: 30,
              height: 30,
            },
            style: {
              lineWidth: 1,
              cursor: 'pointer',
              shadowBlur: 5,
              shadowColor: 'rgba(0, 0, 0, 0.3)',
            },
            onclick: () => {
              toggleFullScreen(chartRef1.value)
            },
          },
          {
            type: 'text',
            style: {
              // text: '👀',
              textAlign: 'center',
              fontSize: 20,
              color: '#00bfff',
              cursor: 'pointer',
            },
            onclick: () => {
              toggleFullScreen(chartRef1.value)
            },
          },
        ],
      },
    ],
  }
  chartInstance.setOption(option)
}

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

const toggleFullScreen = (element) => {
  if (!document.fullscreenElement) {
    element
      .requestFullscreen()
      .then(() => {
        element.classList.add('echarts-fullscreen')
      })
      .catch((err) => {
        console.error(`Error attempting to enable full-screen mode: ${err.message} (${err.name})`)
      })
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen().then(() => {
        element.classList.remove('echarts-fullscreen')
      })
    }
  }
}

onMounted(() => {
  initChart1()
  window.addEventListener('resize', resizeChart)
  // 设置定时器，每隔5分钟刷新一次数据
  refreshTimer = setInterval(() => {
    fetchUserGrowthData()
  }, 300000) // 5分钟 = 300000毫秒
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  if (chartInstance) {
    chartInstance.dispose()
  }
  // 清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

watch([width, height], () => {
  resizeChart()
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
