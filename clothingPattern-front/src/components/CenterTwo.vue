<template>
  <div class="tech-map-wrapper">
    <div ref="chartRef" class="tech-map-container"></div>
    <div v-if="loading" class="map-loading">省份数据加载中...</div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import chinaJson from '@/assets/china.json'
import { getProvinceUserCount } from '@/api/homeController'
import { mapProvinceUserCountData } from '@/utils/provinceMapData'

const props = defineProps({
  provinceData: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['province-click'])

const chartRef = ref(null)
let chartInstance = null
const localProvinceData = ref([])
const mapRegionNames = Array.isArray(chinaJson.features)
  ? chinaJson.features.map((feature) => feature.properties?.name).filter(Boolean)
  : []

const mapProvinceData = (data) => {
  return mapProvinceUserCountData(data, mapRegionNames)
}

const chartProvinceData = computed(() => {
  const parentData = mapProvinceData(props.provinceData)
  return parentData.length > 0 ? parentData : localProvinceData.value
})

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
    if (!props.provinceData.length) {
      await fetchProvinceData()
    }

    chartInstance = echarts.init(chartRef.value, 'tech')
    echarts.registerMap('china', chinaJson)
    chartInstance.setOption(getChartOption())
    chartInstance.on('click', handleMapClick)
  } catch (error) {
    console.error('地图初始化失败:', error)
  }
}

const fetchProvinceData = async () => {
  try {
    const res = await getProvinceUserCount()
    if (res.data.code === 0 && res.data.data) {
      localProvinceData.value = mapProvinceData(res.data.data)
    }
  } catch (error) {
    console.error('获取省份统计数据失败:', error)
  }
}

const handleMapClick = (params) => {
  if (params.componentType === 'series' && params.seriesType === 'map') {
    const provinceName = params.name
    emit('province-click', provinceName)

    chartInstance.dispatchAction({
      type: 'highlight',
      seriesIndex: 0,
      name: provinceName,
    })
  }
}

const getChartOption = () => {
  const currentProvinceData = chartProvinceData.value
  const maxValue =
    currentProvinceData.length > 0
      ? Math.max(...currentProvinceData.map((item) => item.value || 0), 10)
      : 100

  return {
    backgroundColor: 'transparent',
    title: {
      text: '全国图案创作版图',
      left: 'center',
      textStyle: {
        color: '#00f2ff',
        fontSize: getResponsiveFontSize(window.innerWidth, 24, 16),
        fontWeight: 'bold',
        textShadow: '0 0 10px rgba(0, 242, 255, 0.7)',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const value = params.value || 0
        return `
          <div style="font-weight:bold; color: #00f2ff;">${params.name}</div>
          <div style="color: #fff; margin-top: 4px;">用户数: <span style="color: #50d2ff; font-weight: bold;">${value}</span> 人</div>
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
      max: maxValue,
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
        name: '用户数量',
        type: 'map',
        map: 'china',
        roam: true,
        zoom: 1.2,
        data: currentProvinceData,
        label: {
          show: true,
          color: '#fff',
          fontSize: getResponsiveFontSize(window.innerWidth, 10, 8),
          formatter: (params) => `${params.name}\n${params.value || 0}人`,
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
            fontSize: getResponsiveFontSize(window.innerWidth, 12, 10),
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
            fontSize: getResponsiveFontSize(window.innerWidth, 12, 10),
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
  if (!chartInstance) {
    return
  }
  chartInstance.resize()
  chartInstance.setOption(getChartOption())
}

const getResponsiveFontSize = (width, largeSize, smallSize) => {
  return width < 768 ? smallSize : largeSize
}

echarts.registerTheme('tech', {
  backgroundColor: 'rgba(0, 10, 30, 0.8)',
  color: ['#00f2ff', '#1990ff', '#0b5bce', '#0a2dae'],
  title: {
    textStyle: {
      color: '#00f2ff',
    },
  },
})

watch(
  chartProvinceData,
  () => {
    if (chartInstance) {
      chartInstance.setOption(getChartOption())
    }
  },
  { deep: true },
)
</script>

<style scoped>
.tech-map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.tech-map-container {
  width: 100%;
  height: 100%;
  overflow: hidden !important;
}

.map-loading {
  position: absolute;
  top: 16px;
  right: 18px;
  padding: 6px 10px;
  border: 1px solid rgba(0, 242, 255, 0.45);
  border-radius: 6px;
  background: rgba(0, 20, 50, 0.72);
  color: #00f2ff;
  font-size: 12px;
  box-shadow: 0 0 14px rgba(0, 242, 255, 0.2);
}
</style>
