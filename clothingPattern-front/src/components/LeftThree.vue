# LeftThree.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import * as echarts from "echarts";
import { ref, onMounted, onUnmounted } from "vue";

const chartRef = ref(null);
let chartInstance = null;
let intervalId = null;

const dataGroups = [
  [
    { name: "Email", data: [300, 350, 180, 380, 160, 150, 100] },
    { name: "Union Ads", data: [250, 200, 150, 300, 280, 350, 300] },
    { name: "Video Ads", data: [180, 260, 180, 160, 220, 380, 420] },
  ],
  [
    { name: "Email", data: [100, 140, 90, 160, 70, 240, 190] },
    { name: "Union Ads", data: [230, 160, 170, 250, 270, 340, 290] },
    { name: "Video Ads", data: [160, 250, 170, 150, 210, 370, 410] },
  ],
  [
    { name: "Email", data: [130, 165, 95, 179, 85, 265, 225] },
    { name: "Union Ads", data: [140, 575, 125, 665, 385, 155, 325] },
    { name: "Video Ads", data: [190, 275, 215, 165, 235, 395, 435] },
  ]
];

let currentGroupIndex = 0;

const initChart = () => {
  chartInstance = echarts.init(chartRef.value);
  updateChart();
  startDataSwitching();
};

const updateChart = () => {
  const isSmallScreen = window.innerWidth < 768;

  const option = {
    color: ["#80FFA5", "#00DDFF", "#37A2FF", "#FF0087", "#FFBF00"],
    title: {
      textStyle: {
        color: '#00bfff', // 设置标题文字颜色为科技感蓝色
      },
    },
    tooltip: {
      trigger: "axis",
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      borderColor: '#00bfff',
      borderWidth: 1,
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      data: dataGroups[currentGroupIndex].map(item => item.name),
      textStyle: {
        color: '#00bfff', // 设置图例文字颜色为科技感蓝色
      },
      top: isSmallScreen ? "10%" : "5%",
      left: "center",
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "10%",
      containLabel: true,
      backgroundColor: 'rgba(0, 0, 0, 0.1)', // 设置背景颜色为半透明黑色
      borderColor: '#00bfff', // 设置网格边框颜色为科技感蓝色
      borderWidth: 1
    },
    toolbox: {
      iconStyle: {
        color: '#00bfff', // 设置工具箱图标颜色为科技感蓝色
      },
      backgroundColor: 'rgba(0, 0, 0, 0.7)', // 设置工具箱背景颜色为半透明黑色
      borderColor: '#00bfff', // 设置工具箱边框颜色为科技感蓝色
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
      axisLine: {
        lineStyle: {
          color: '#00bfff' // 设置X轴线颜色为科技感蓝色
        }
      },
      axisLabel: {
        color: '#00bfff', // 设置X轴标签颜色为科技感蓝色
        rotate: isSmallScreen ? 45 : 0, // 小屏幕时旋转标签
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.3)' // 设置X轴分割线颜色为半透明黑色
        }
      }
    },
    yAxis: {
      type: "value",
      axisLine: {
        lineStyle: {
          color: '#00bfff' // 设置Y轴线颜色为科技感蓝色
        }
      },
      axisLabel: {
        color: '#00bfff', // 设置Y轴标签颜色为科技感蓝色
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.3)' // 设置Y轴分割线颜色为半透明黑色
        }
      }
    },
    series: dataGroups[currentGroupIndex].map(item => ({
      name: item.name,
      type: "line",
      stack: "Total",
      data: item.data,
      smooth: true, // 使折线平滑
      symbol: 'circle', // 设置数据点符号
      symbolSize: isSmallScreen ? 6 : 8, // 小屏幕时减小数据点大小
      lineStyle: {
        width: isSmallScreen ? 1.5 : 2, // 小屏幕时减小线条宽度
        shadowColor: 'rgba(0, 0, 0, 0.5)',
        shadowBlur: 10
      },
      itemStyle: {
        color: '#00bfff', // 设置数据点颜色
        borderColor: '#000',
        borderWidth: 1
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 175, 255, 0.3)' },
          { offset: 1, color: 'rgba(0, 0, 0, 0)' }
        ])
      }
    })),
  };
  chartInstance.setOption(option);
};

const startDataSwitching = () => {
  intervalId = setInterval(() => {
    currentGroupIndex = (currentGroupIndex + 1) % dataGroups.length;
    updateChart();
  }, 2000);
};

onMounted(() => {
  initChart();
  window.addEventListener('resize', resizeChart);
});

onUnmounted(() => {
  clearInterval(intervalId);
  if (chartInstance) {
    chartInstance.dispose();
  }
  window.removeEventListener('resize', resizeChart);
});

const resizeChart = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};
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
