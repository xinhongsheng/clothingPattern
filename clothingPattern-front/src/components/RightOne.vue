# RightOne.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
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
  { value: 45, name: "rose 1" },
  { value: 60, name: "rose 2" },
  { value: 75, name: "rose 3" },
  { value: 30, name: "rose 4" },
  { value: 55, name: "rose 5" },
  { value: 80, name: "rose 6" },
  { value: 25, name: "rose 7" },
  { value: 90, name: "rose 8" },
  { value: 10, name: "rose 9" },
  { value: 65, name: "rose 10" }
];

// 将数据分成两个数组，每个数组包含5条数据
const dataGroups = [
  allData.slice(0, 5),
  allData.slice(5, 10),
];

const initChart = () => {
  chartInstance = echarts.init(chartRef.value);
  updateChart();
};

const updateChart = () => {
  const option = {
    color: ["#80FFA5", "#00DDFF", "#37A2FF", "#FF0087", "#FFBF00"],
    legend: {
      top: "bottom",
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
      borderWidth: 1
    },
    series: [
      {
        name: "Nightingale Chart",
        type: "pie",
        radius: [30, 80],
        center: ["50%", "50%"],
        roseType: "area",
        itemStyle: {
          borderRadius: 8,
          borderColor: '#000',
          borderWidth: 1,
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        },
        label: {
          show: true,
          formatter: '{b}: {c}',
          color: '#00bfff', // 设置标签文字颜色为科技感蓝色
        },
        labelLine: {
          show: true,
          lineStyle: {
            color: '#00bfff' // 设置标签线颜色为科技感蓝色
          }
        },
        data: dataGroups[currentGroupIndex],
      },
    ],
    backgroundColor: 'rgba(0, 0, 0, 0.1)', // 设置图表背景颜色为半透明黑色
  };
  chartInstance.setOption(option);
};

const startDataSwitching = () => {
  timer = setInterval(() => {
    currentGroupIndex = (currentGroupIndex + 1) % dataGroups.length;
    updateChart();
  }, 2000);
};

onMounted(() => {
  initChart();
  startDataSwitching();
});

onBeforeUnmount(() => {
  chartInstance?.dispose();
  clearInterval(timer);
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
