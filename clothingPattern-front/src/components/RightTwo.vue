# RightTwo.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getStylePreference } from '@/api/homeController'

const chartRef = ref(null)
let chartInstance = null
let timer = null

// 存储API数据
const styleData = ref([])
const dates = ref([])
const topStyles = ref([])

// 处理API数据
const processData = (apiData) => {
  if (!apiData || apiData.length === 0) return

  // 获取所有日期
  dates.value = apiData.map((item) => item.date)

  // 收集所有风格及其总计数
  const styleTotalMap = new Map()
  apiData.forEach((day) => {
    day.topStyles.forEach((style) => {
      const name = style.style
      const count = parseInt(style.count) || 0
      styleTotalMap.set(name, (styleTotalMap.get(name) || 0) + count)
    })
  })

  // 按总计数排序，取前5名风格
  topStyles.value = Array.from(styleTotalMap.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map((item) => item[0])

  // 为每天创建数据点，确保每个风格都有值（没有则为0）
  const processedData = topStyles.value.map((style) => {
    return apiData.map((day) => {
      const styleItem = day.topStyles.find((s) => s.style === style)
      return styleItem ? parseInt(styleItem.count) || 0 : 0
    })
  })

  styleData.value = processedData
}

// 获取API数据
const fetchData = async () => {
  try {
    const response = await getStylePreference()
    // 正确访问API响应数据，axios返回完整response对象
    if (response.data.code === 0 && response.data.data) {
      processData(response.data.data)
      updateChart()
    }
  } catch (error) {
    console.error('获取风格偏好数据失败:', error)
  }
}

const initChart = () => {
  chartInstance = echarts.init(chartRef.value)
  fetchData()

  // 设置5分钟刷新一次
  timer = setInterval(
    () => {
      fetchData()
    },
    5 * 60 * 1000,
  )
}

const updateChart = () => {
  const option = {
    color: ['#80FFA5', '#00DDFF', '#37A2FF', '#FF0087', '#FFBF00'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          borderColor: '#00bfff',
          borderWidth: 1,
          color: '#fff',
        },
      },
    },
    legend: {
      data:
        topStyles.value.length > 0
          ? topStyles.value
          : ['Line 1', 'Line 2', 'Line 3', 'Line 4', 'Line 5'],
      bottom: '10%', // 将图例位置调低
      textStyle: {
        color: '#00bfff', // 设置图例文字颜色为科技感蓝色
      },
    },
    toolbox: {
      iconStyle: {
        color: '#00bfff', // 设置工具箱图标颜色为科技感蓝色
      },
      backgroundColor: 'rgba(0, 0, 0, 0.7)', // 设置工具箱背景颜色为半透明黑色
      borderColor: '#00bfff', // 设置工具箱边框颜色为科技感蓝色
      borderWidth: 1,
      bottom: '10%', // 将工具箱位置调低
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '20%', // 增加底部空间以容纳图例和工具箱
      containLabel: true,
      backgroundColor: 'rgba(0, 0, 0, 0.1)', // 设置背景颜色为半透明黑色
      borderColor: '#00bfff', // 设置网格边框颜色为科技感蓝色
      borderWidth: 1,
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data:
          dates.value.length > 0 ? dates.value : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
        axisLine: {
          lineStyle: {
            color: '#00bfff', // 设置X轴线颜色为科技感蓝色
          },
        },
        axisLabel: {
          color: '#00bfff', // 设置X轴标签颜色为科技感蓝色
        },
        splitLine: {
          lineStyle: {
            color: 'rgba(0, 0, 0, 0.3)', // 设置X轴分割线颜色为半透明黑色
          },
        },
      },
    ],
    yAxis: [
      {
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
    ],
    // 动态生成series数组，根据实际风格数量显示对应的数据系列
    series: (() => {
      // 颜色渐变数组，对应不同系列
      const gradientColors = [
        { offset0: 'rgba(0, 175, 255, 0.3)', offset1: 'rgba(0, 0, 0, 0)' },
        { offset0: 'rgba(0, 145, 255, 0.3)', offset1: 'rgba(0, 0, 0, 0)' },
        { offset0: 'rgba(0, 115, 255, 0.3)', offset1: 'rgba(0, 0, 0, 0)' },
        { offset0: 'rgba(0, 85, 255, 0.3)', offset1: 'rgba(0, 0, 0, 0)' },
        { offset0: 'rgba(0, 55, 255, 0.3)', offset1: 'rgba(0, 0, 0, 0)' },
      ]

      // 根据实际风格数量生成series
      const series = []
      const styleCount = topStyles.value.length

      for (let i = 0; i < styleCount; i++) {
        const color = gradientColors[i] || gradientColors[0]
        series.push({
          name: topStyles.value[i],
          type: 'line',
          stack: 'Total',
          smooth: true,
          lineStyle: {
            width: 2, // 设置线条宽度
            shadowColor: 'rgba(0, 0, 0, 0.5)',
            shadowBlur: 10,
          },
          showSymbol: false,
          label:
            i === styleCount - 1
              ? {
                  show: true,
                  position: 'top',
                  color: '#00bfff', // 设置标签文字颜色为科技感蓝色
                }
              : undefined,
          areaStyle: {
            opacity: 0.8,
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: color.offset0 },
              { offset: 1, color: color.offset1 },
            ]),
          },
          emphasis: {
            focus: 'series',
          },
          data: styleData.value[i] || [],
        })
      }

      return series
    })(),
  }

  // 应用配置
  chartInstance.setOption(option)
}

onMounted(() => {
  // 初始化图表
  initChart()
})

onBeforeUnmount(() => {
  // 组件卸载时销毁图表实例
  chartInstance?.dispose()
  // 清除定时器
  clearInterval(timer)
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
