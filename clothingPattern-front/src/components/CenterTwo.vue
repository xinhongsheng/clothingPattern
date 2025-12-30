# CenterTwo.vue

<template>
  <div ref="chartRef" class="tech-map-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import chinaJson from '@/assets/china.json' // 导入 JSON 文件

const chartRef = ref(null)
let chartInstance = null

onMounted(async () => {
  await initChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
})

const initChart = async () => {
  try {
    // 初始化图表
    chartInstance = echarts.init(chartRef.value, 'tech')

    // 注册地图数据
    echarts.registerMap('china', chinaJson)

    // 设置图表配置
    chartInstance.setOption(getChartOption())

    // 添加点击事件
    chartInstance.on('click', handleMapClick)
  } catch (error) {
    console.error('地图初始化失败:', error)
  }
}

const handleMapClick = (params) => {
  if (params.componentType === 'series' && params.seriesType === 'map') {
    const provinceName = params.name
    emit('province-click', provinceName)

    // 高亮选中的省份
    chartInstance.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      name: provinceName,
    })
  }
}

const getChartOption = () => {
  return {
    backgroundColor: 'transparent',
    title: {
      text: "中国服装风格趋势数据可视化",
      left: 'center',
      textStyle: {
        color: '#00f2ff',
        fontSize: getResponsiveFontSize(window.innerWidth, 24, 16), // 根据屏幕宽度调整字体大小
        fontWeight: 'bold',
        textShadow: '0 0 10px rgba(0, 242, 255, 0.7)',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `
          <div style="font-weight:bold; color: #00f2ff;">${params.name}</div>
        `
      },
      backgroundColor: 'rgba(0, 20, 50, 0.9)',
      borderColor: '#00f2ff',
      padding: 10,
      textStyle: {
        color: '#fff',
      },
    },
    visualMap: {
      min: 0,
      max: 1000,
      text: ['高', '低'],
      realtime: false,
      calculable: true,
      inRange: {
        color: ['#0a2dae', '#0b5bce', '#1990ff', '#38b1ff', '#50d2ff'],
      },
      textStyle: {
        color: '#fff',
      },
    },
    series: [
      {
        name: '数据',
        type: 'map',
        map: 'china',
        roam: true,
        zoom: 1.2,
        label: {
          show: true,
          color: '#fff',
          fontSize: getResponsiveFontSize(window.innerWidth, 10, 8), // 根据屏幕宽度调整字体大小
        },
        itemStyle: {
          areaColor: '#0c2c5a',
          borderColor: '#00f2ff',
          borderWidth: 1,
          shadowColor: 'rgba(0, 242, 255, 0.3)',
          shadowBlur: 10,
        },
        emphasis: {
          label: {
            color: '#fff',
            fontSize: getResponsiveFontSize(window.innerWidth, 12, 10), // 根据屏幕宽度调整字体大小
            fontWeight: 'bold',
          },
          itemStyle: {
            areaColor: '#1990ff',
            borderWidth: 2,
            shadowBlur: 15,
          },
        },
        select: {
          label: {
            color: '#ff0',
            fontSize: getResponsiveFontSize(window.innerWidth, 12, 10), // 根据屏幕宽度调整字体大小
            fontWeight: 'bold',
          },
          itemStyle: {
            areaColor: '#ff0',
            borderColor: '#ff0',
          },
        },
      },
    ],
  }
}

const resizeChart = () => {
  chartInstance?.resize()
  chartInstance.setOption(getChartOption()) // 重新设置选项以应用响应式调整
}

// 根据屏幕宽度调整字体大小
const getResponsiveFontSize = (width, largeSize, smallSize) => {
  return width < 768 ? smallSize : largeSize
}

// 注册科技感主题
echarts.registerTheme('tech', {
  backgroundColor: 'rgba(0, 10, 30, 0.8)',
  color: ['#00f2ff', '#1990ff', '#0b5bce', '#0a2dae'],
  title: {
    textStyle: {
      color: '#00f2ff',
    },
  },
})
</script>

<style scoped>
.tech-map-container {
  width: 100%;
  height: 100%;
  overflow: hidden !important;
}
</style>
