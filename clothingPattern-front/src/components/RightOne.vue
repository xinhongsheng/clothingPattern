# RightOne.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getHotStyleTopFive } from '@/api/homeController'

const chartRef = ref(null)
let chartInstance = null
let timer = null
const chartData = ref([])

// 从后端获取数据
const fetchData = async () => {
  try {
    console.log('开始调用getHotStyleTopFive接口...')
    const response = await getHotStyleTopFive()
    console.log('API返回结果:', response)

    // 检查响应结构，注意axios返回的response.data才是后端真正返回的数据
    if (response && response.data && response.data.code === 0) {
      // 检查数据是否存在
      const data = response.data.data
      console.log('API返回data:', data)

      if (data && Array.isArray(data)) {
        // 转换数据格式为图表所需的格式
        const newData = data.map((item) => ({
          value: parseInt(item.count),
          name: item.style,
        }))
        console.log('转换后的数据:', newData)

        chartData.value = newData
        updateChart()
      } else {
        console.error('API返回的数据不是数组:', data)
        chartData.value = []
        updateChart()
      }
    } else {
      console.error('API返回错误:', response)
      chartData.value = []
      updateChart()
    }
  } catch (error) {
    console.error('调用API失败:', error)
    // 如果请求失败，使用空数据更新图表
    chartData.value = []
    updateChart()
  }
}

const initChart = () => {
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  const option = {
    color: ['#80FFA5', '#00DDFF', '#37A2FF', '#FF0087', '#FFBF00'],
    legend: {
      top: 'bottom',
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
    },
    series: [
      {
        name: '热门风格',
        type: 'pie',
        radius: [30, 80],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 8,
          borderColor: '#000',
          borderWidth: 1,
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
        label: {
          show: true,
          formatter: '{b}: {c}',
          color: '#00bfff', // 设置标签文字颜色为科技感蓝色
          fontSize: 10, // 调整标签字体大小
        },
        labelLine: {
          show: true,
          length: 10, // 调整标签线长度
          length2: 10, // 调整标签线第二段长度
          lineStyle: {
            color: '#00bfff', // 设置标签线颜色为科技感蓝色
            width: 1, // 调整标签线宽度
          },
        },
        data: chartData.value,
      },
    ],
    backgroundColor: 'rgba(0, 0, 0, 0.1)', // 设置图表背景颜色为半透明黑色
  }
  chartInstance.setOption(option)
}

// 开始定时刷新数据（5分钟）
const startDataRefresh = () => {
  // 设置5分钟定时刷新
  timer = setInterval(
    () => {
      fetchData()
    },
    5 * 60 * 1000,
  )
}

onMounted(() => {
  initChart()
  // 图表初始化后立即获取一次数据
  fetchData()
  startDataRefresh()
})

onBeforeUnmount(() => {
  chartInstance?.dispose()
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
