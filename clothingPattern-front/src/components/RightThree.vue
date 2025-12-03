# RightThree.vue

<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import * as echarts from "echarts";

const chartRef = ref(null);
let chartInstance = null;
let timer = null;
let currentGroupIndex = 0;

// 定义固定的数据
const allData = [
  120, 200, 150, 80, 70, 180, 250, 220, 190, 160
];

// 将数据分成两个数组，每个数组包含5条数据
const dataGroups = [
  allData.slice(0, 5),
  allData.slice(5, 10),
];

const initChart = () => {
  chartInstance = echarts.init(chartRef.value);
  updateChart();
  startDataSwitching();
};

const updateChart = () => {
  const option = {
    color: ["#00bfff"], // 设置柱状图的颜色为科技感蓝色
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    yAxis: {
      type: "category",
      data: ["HTML", "CSS", "JS", "VUE", "NODE"],
      axisLine: {
        lineStyle: {
          color: "#00bfff",
        },
      },
      axisLabel: {
        color: "#00bfff",
        fontSize: getResponsiveFontSize(window.innerWidth, 14, 12) // 根据屏幕宽度调整字体大小
      },
    },
    xAxis: {
      type: "value",
      axisLine: {
        lineStyle: {
          color: "#00bfff",
        },
      },
      axisLabel: {
        color: "#00bfff",
        fontSize: getResponsiveFontSize(window.innerWidth, 14, 12) // 根据屏幕宽度调整字体大小
      },
      splitLine: {
        lineStyle: {
          color: "rgba(0, 0, 0, 0.3)",
        },
      },
    },
    series: [
      {
        data: dataGroups[currentGroupIndex],
        type: "bar",
        label: {
          show: true,
          position: "insideRight",
          color: "#fff",
          fontSize: getResponsiveFontSize(window.innerWidth, 12, 10) // 根据屏幕宽度调整字体大小
        },
        itemStyle: {
          borderRadius: [0, 10, 10, 0],
        },
      },
    ],
    grid: {
      left: "10%",
      right: "10%",
      bottom: "10%",
      containLabel: true,
    },
  };

  // 应用配置
  chartInstance.setOption(option);
};

const startDataSwitching = () => {
  timer = setInterval(() => {
    currentGroupIndex = (currentGroupIndex + 1) % dataGroups.length;
    updateChart();
  }, 2000);
};

const getResponsiveFontSize = (width, largeSize, smallSize) => {
  return width < 768 ? smallSize : largeSize;
};

onMounted(() => {
  // 初始化图表
  initChart();
});

onBeforeUnmount(() => {
  // 组件卸载时销毁图表实例和清除定时器
  if (chartInstance) {
    chartInstance.dispose();
  }
  if (timer) {
    clearInterval(timer);
  }
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
