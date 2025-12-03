# RightTwo.vue

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
  [140, 232, 101, 264, 90, 340, 250],
  [120, 282, 111, 234, 220, 340, 310],
  [320, 132, 201, 334, 190, 130, 220],
  [220, 402, 231, 134, 190, 230, 120],
  [220, 302, 181, 234, 210, 290, 150],
  [180, 252, 151, 284, 120, 320, 230],
  [160, 272, 131, 304, 100, 300, 210],
  [190, 262, 141, 294, 110, 330, 240],
  [210, 242, 121, 274, 90, 310, 220],
  [230, 222, 111, 254, 80, 290, 200]
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
    color: ["#80FFA5", "#00DDFF", "#37A2FF", "#FF0087", "#FFBF00"],
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "cross",
        label: {
          backgroundColor: "rgba(0, 0, 0, 0.7)",
          borderColor: "#00bfff",
          borderWidth: 1,
          color: "#fff",
        },
      },
    },
    legend: {
      data: ["Line 1", "Line 2", "Line 3", "Line 4", "Line 5"],
      bottom: "10%", // 将图例位置调低
      textStyle: {
        color: "#00bfff", // 设置图例文字颜色为科技感蓝色
      },
    },
    toolbox: {
      iconStyle: {
        color: "#00bfff", // 设置工具箱图标颜色为科技感蓝色
      },
      backgroundColor: "rgba(0, 0, 0, 0.7)", // 设置工具箱背景颜色为半透明黑色
      borderColor: "#00bfff", // 设置工具箱边框颜色为科技感蓝色
      borderWidth: 1,
      bottom: "10%", // 将工具箱位置调低
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "20%", // 增加底部空间以容纳图例和工具箱
      containLabel: true,
      backgroundColor: "rgba(0, 0, 0, 0.1)", // 设置背景颜色为半透明黑色
      borderColor: "#00bfff", // 设置网格边框颜色为科技感蓝色
      borderWidth: 1,
    },
    xAxis: [
      {
        type: "category",
        boundaryGap: false,
        data: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
        axisLine: {
          lineStyle: {
            color: "#00bfff", // 设置X轴线颜色为科技感蓝色
          },
        },
        axisLabel: {
          color: "#00bfff", // 设置X轴标签颜色为科技感蓝色
        },
        splitLine: {
          lineStyle: {
            color: "rgba(0, 0, 0, 0.3)", // 设置X轴分割线颜色为半透明黑色
          },
        },
      },
    ],
    yAxis: [
      {
        type: "value",
        axisLine: {
          lineStyle: {
            color: "#00bfff", // 设置Y轴线颜色为科技感蓝色
          },
        },
        axisLabel: {
          color: "#00bfff", // 设置Y轴标签颜色为科技感蓝色
        },
        splitLine: {
          lineStyle: {
            color: "rgba(0, 0, 0, 0.3)", // 设置Y轴分割线颜色为半透明黑色
          },
        },
      },
    ],
    series: [
      {
        name: "Line 1",
        type: "line",
        stack: "Total",
        smooth: true,
        lineStyle: {
          width: 2, // 设置线条宽度
          shadowColor: "rgba(0, 0, 0, 0.5)",
          shadowBlur: 10,
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(0, 175, 255, 0.3)" },
            { offset: 1, color: "rgba(0, 0, 0, 0)" },
          ]),
        },
        emphasis: {
          focus: "series",
        },
        data: dataGroups[currentGroupIndex][0],
      },
      {
        name: "Line 2",
        type: "line",
        stack: "Total",
        smooth: true,
        lineStyle: {
          width: 2, // 设置线条宽度
          shadowColor: "rgba(0, 0, 0, 0.5)",
          shadowBlur: 10,
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(0, 145, 255, 0.3)" },
            { offset: 1, color: "rgba(0, 0, 0, 0)" },
          ]),
        },
        emphasis: {
          focus: "series",
        },
        data: dataGroups[currentGroupIndex][1],
      },
      {
        name: "Line 3",
        type: "line",
        stack: "Total",
        smooth: true,
        lineStyle: {
          width: 2, // 设置线条宽度
          shadowColor: "rgba(0, 0, 0, 0.5)",
          shadowBlur: 10,
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(0, 115, 255, 0.3)" },
            { offset: 1, color: "rgba(0, 0, 0, 0)" },
          ]),
        },
        emphasis: {
          focus: "series",
        },
        data: dataGroups[currentGroupIndex][2],
      },
      {
        name: "Line 4",
        type: "line",
        stack: "Total",
        smooth: true,
        lineStyle: {
          width: 2, // 设置线条宽度
          shadowColor: "rgba(0, 0, 0, 0.5)",
          shadowBlur: 10,
        },
        showSymbol: false,
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(0, 85, 255, 0.3)" },
            { offset: 1, color: "rgba(0, 0, 0, 0)" },
          ]),
        },
        emphasis: {
          focus: "series",
        },
        data: dataGroups[currentGroupIndex][3],
      },
      {
        name: "Line 5",
        type: "line",
        stack: "Total",
        smooth: true,
        lineStyle: {
          width: 2, // 设置线条宽度
          shadowColor: "rgba(0, 0, 0, 0.5)",
          shadowBlur: 10,
        },
        showSymbol: false,
        label: {
          show: true,
          position: "top",
          color: "#00bfff", // 设置标签文字颜色为科技感蓝色
        },
        areaStyle: {
          opacity: 0.8,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(0, 55, 255, 0.3)" },
            { offset: 1, color: "rgba(0, 0, 0, 0)" },
          ]),
        },
        emphasis: {
          focus: "series",
        },
        data: dataGroups[currentGroupIndex][4],
      },
    ],
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

onMounted(() => {
  // 初始化图表
  initChart();
});

onBeforeUnmount(() => {
  // 组件卸载时销毁图表实例
  chartInstance?.dispose();
  // 清除定时器
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
