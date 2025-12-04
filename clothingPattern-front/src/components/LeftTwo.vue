# LeftTwo.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { ref, onMounted, onUnmounted, onBeforeUnmount } from 'vue'
import { getTargetTopFive } from '@/api/homeController'

const chartRef = ref(null)
let chartInstance = null
const chartData = ref([])
let refreshTimer = null

// 获取目标人群数据
const fetchTargetAudienceData = async () => {
  try {
    const response = await getTargetTopFive()
    console.log('目标人群数据:', response)
    if (response.data.code === 0 && response.data.data) {
      // 将后端数据转换为ECharts需要的格式
      const formattedData = response.data.data.map((item) => ({
        name: item.targetAudience,
        value: parseInt(item.count),
      }))
      console.log('格式化后的数据:', formattedData)
      chartData.value = formattedData
    } else {
      // 数据为空时，设置空数组
      chartData.value = []
      console.log('数据为空')
    }
    // 确保图表实例存在后再更新
    if (chartInstance) {
      updateChart()
    }
  } catch (error) {
    console.error('获取目标人群数据失败:', error)
    // 错误时，设置空数组
    chartData.value = []
    if (chartInstance) {
      updateChart()
    }
  }
}

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    console.log('图表实例初始化成功')
    // 先设置默认数据，确保图表能显示
    chartData.value = [{ name: '默认', value: 1 }]
    updateChart()
    // 然后获取真实数据
    fetchTargetAudienceData()
  }
}

const updateChart = () => {
  if (!chartInstance) {
    console.log('图表实例不存在')
    return
  }

  console.log('更新图表，数据:', chartData.value)

  const isSmallScreen = window.innerWidth < 768

  // 使用全新配置，不合并
  const option = {
    color: ['#00bfff', '#1e90ff', '#4682b4', '#6495ed', '#778899'],
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    title: {
      text: '目标人群Top5',
      left: 'center',
      top: '10%',
      textStyle: {
        color: '#00bfff',
        // fontSize: isSmallScreen ? 16 : 20,
        fontSize:16 ,
        fontWeight: 'bold',
      },
    },
    legend: {
      show: false,
    },
    series: [
      {
        name: '目标人群分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '60%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}: {d}%',
          color: '#00bfff',
          fontSize: isSmallScreen ? 10 : 12,
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isSmallScreen ? 16 : 20,
            fontWeight: 'bold',
            color: '#00bfff',
          },
        },
        labelLine: {
          show: true,
          lineStyle: {
            color: '#00bfff',
          },
          length: 10,
          length2: 15,
        },
        itemStyle: {
          borderRadius: 8,
          borderColor: '#000',
          borderWidth: 1,
        },
        data: chartData.value,
      },
    ],
    graphic: [
      {
        type: 'group',
        right: isSmallScreen ? 10 : 20, // 根据屏幕大小调整全屏按钮的位置
        top: isSmallScreen ? 10 : 20, // 根据屏幕大小调整全屏按钮的位置
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
              toggleFullScreen(chartRef.value)
            },
          },
          {
            type: 'text',
            style: {
              // text: '👀',
              textAlign: 'center',
              fontSize: isSmallScreen ? 16 : 20, // 根据屏幕大小调整全屏按钮文字大小
              color: '#00bfff',
              cursor: 'pointer',
            },
            onclick: () => {
              toggleFullScreen(chartRef.value)
            },
          },
        ],
      },
    ],
  }

  // 使用notMerge: true确保每次都使用新配置
  chartInstance.setOption(option, true)
}

// 专门的图表大小调整方法
const resizeChart = () => {
  if (chartInstance) {
    console.log('调整图表大小')
    chartInstance.resize()
    // 调整大小后更新图表，确保数据正确显示
    updateChart()
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
  initChart()
  window.addEventListener('resize', resizeChart) // 监听窗口大小变化并调整图表大小
  // 启动定时器，每5分钟刷新一次数据
  refreshTimer = setInterval(
    () => {
      fetchTargetAudienceData()
    },
    5 * 60 * 1000,
  ) // 5分钟 = 5 * 60 * 1000毫秒
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart) // 移除事件监听器
  // 清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
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
