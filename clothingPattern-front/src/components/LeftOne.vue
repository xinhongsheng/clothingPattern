# LeftOne.vue

<template>
  <div ref="chartRef1" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useWindowSize } from '@vueuse/core' // 使用 vueuse 来获取窗口大小
import { getUserGrowth } from '@/api/homeController' // 引入API函数

const chartRef1 = ref(null)
let chartInstance = null
let userGrowthData = []
let dates = []
let counts = []

const { width, height } = useWindowSize()

// 获取近15日用户增长量数据
const fetchUserGrowthData = async () => {
  try {
    const response = await getUserGrowth()
    const result = response.data
    if (result.code === 0) {
      userGrowthData = result.data
      // 提取日期和用户增长数量
      dates = userGrowthData.map((item) => item.date)
      counts = userGrowthData.map((item) => item.count)
      updateChart()
    }
  } catch (error) {
    console.error('获取用户增长数据失败:', error)
  }
}

const initChart1 = () => {
  chartInstance = echarts.init(chartRef1.value)
  // 初始调用一次获取数据
  fetchUserGrowthData()
  // 设置定时器，每30秒更新一次数据
  setInterval(() => {
    fetchUserGrowthData()
  }, 30000)
  resizeChart()
}

const updateChart = () => {
  const option = {
    title: {
      text: '近15日用户增长趋势',
      left: 'center',
      textStyle: {
        color: '#00bfff',
        fontSize: 16,
      },
    },
    color: ['#00bfff'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    xAxis: {
      type: 'category',
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
      type: 'value',
      name: '用户增长数',
      nameTextStyle: {
        color: '#00bfff',
      },
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
          color: '#00bfff',
          width: 2,
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
          fontSize: 10,
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
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  if (chartInstance) {
    chartInstance.dispose()
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
