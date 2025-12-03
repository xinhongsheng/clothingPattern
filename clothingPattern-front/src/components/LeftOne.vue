# LeftOne.vue

<template>
  <div ref="chartRef1" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import * as echarts from "echarts";
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useWindowSize } from '@vueuse/core'; // 使用 vueuse 来获取窗口大小

const chartRef1 = ref(null);
let chartInstance = null;
let intervalId = null;

const { width, height } = useWindowSize();

const data = ["衬衫", "羊毛衫", "雪纺衫", "裤子", "高跟鞋", "袜子", "裤子1", "鞋子2", "鞋子3", "鞋子4"];
const dataSeries = [5, 20, 36, 10, 10, 20, 100, 50, 60, 70];

const groupedData = [];
const groupedDataSeries = [];

for (let i = 0; i < data.length; i += 5) {
  groupedData.push(data.slice(i, i + 5));
  groupedDataSeries.push(dataSeries.slice(i, i + 5));
}

let currentGroupIndex = 0;

const initChart1 = () => {
  chartInstance = echarts.init(chartRef1.value);
  updateChart();
  resizeChart();
  startDataSwitching();
};

const updateChart = () => {
  const option = {
    color: ["#80FFA5", "#00DDFF", "#37A2FF", "#FF0087", "#FFBF00"],
    tooltip: {},
    xAxis: {
      data: groupedData[currentGroupIndex],
      axisLine: {
        lineStyle: {
          color: '#00bfff'
        }
      },
      axisLabel: {
        color: '#00bfff'
      }
    },
    yAxis: {
      axisLine: {
        lineStyle: {
          color: '#00bfff'
        }
      },
      axisLabel: {
        color: '#00bfff'
      }
    },
    series: [
      {
        name: "销量",
        type: "bar",
        data: groupedDataSeries[currentGroupIndex],
        barWidth: 26,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00bfff' },
            { offset: 1, color: '#1e90ff' }
          ])
        },
        label: {
          show: true,
          position: 'top',
          color: '#00bfff',
          formatter: '{c}'
        }
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
              toggleFullScreen(chartRef1.value);
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
              toggleFullScreen(chartRef1.value);
            },
          },
        ],
      },
    ],
  };
  chartInstance.setOption(option);
};

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

const toggleFullScreen = (element) => {
  if (!document.fullscreenElement) {
    element.requestFullscreen().then(() => {
      element.classList.add('echarts-fullscreen');
    }).catch(err => {
      console.error(`Error attempting to enable full-screen mode: ${err.message} (${err.name})`);
    });
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen().then(() => {
        element.classList.remove('echarts-fullscreen');
      });
    }
  }
};

const startDataSwitching = () => {
  intervalId = setInterval(() => {
    currentGroupIndex = (currentGroupIndex + 1) % groupedData.length;
    updateChart();
  }, 2000);
};

onMounted(() => {
  initChart1();
  window.addEventListener('resize', resizeChart);
});

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart);
  if (chartInstance) {
    chartInstance.dispose();
  }
  clearInterval(intervalId);
});

watch([width, height], () => {
  resizeChart();
});
</script>

<style scoped >
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
