# LeftTwo.vue

<template>
  <div ref="chartRef" style="width: 100%; height: 100%"></div>
</template>

<script setup>
import * as echarts from "echarts";
import { ref, onMounted, onUnmounted, onBeforeUnmount, watch } from "vue";

const chartRef = ref(null);
let chartInstance = null;
let intervalId = null;

const dataGroups = [
  [
    { value: 1048, name: "Search Engine" },
    { value: 735, name: "Direct" },
    { value: 580, name: "Email" },
    { value: 484, name: "Union Ads" },
    { value: 300, name: "Video Ads" },
  ],
  [
    { value: 800, name: "Social Media" },
    { value: 600, name: "Referral" },
    { value: 450, name: "Affiliate" },
    { value: 350, name: "Newsletter" },
    { value: 250, name: "Other" },
  ],
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
    color: ['#00bfff', '#1e90ff', '#4682b4', '#6495ed', '#778899'],
    tooltip: {
      trigger: "item",
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      top: isSmallScreen ? "10%" : "5%",
      left: "center",
      textStyle: {
        color: '#00bfff',
        fontSize: isSmallScreen ? 12 : 14, // 根据屏幕大小调整字体大小
      },
    },
    series: [
      {
        name: "Access From",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        label: {
          show: !isSmallScreen,
          position: "center",
          fontSize: isSmallScreen ? 10 : 12, // 根据屏幕大小调整标签字体大小
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isSmallScreen ? 16 : 20, // 根据屏幕大小调整强调标签字体大小
            fontWeight: "bold",
            color: '#00bfff'
          },
        },
        labelLine: {
          show: !isSmallScreen, // 在小屏幕上隐藏标签线
        },
        itemStyle: {
          borderRadius: 8,
          borderColor: '#000',
          borderWidth: 1
        },
        data: dataGroups[currentGroupIndex],
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
              toggleFullScreen(chartRef.value);
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
              toggleFullScreen(chartRef.value);
            },
          },
        ],
      },
    ],
  };
  chartInstance.setOption(option);
};

const startDataSwitching = () => {
  intervalId = setInterval(() => {
    currentGroupIndex = (currentGroupIndex + 1) % dataGroups.length;
    updateChart();
  }, 2000);
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

onMounted(() => {
  initChart();
  window.addEventListener('resize', updateChart); // 监听窗口大小变化并更新图表
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateChart); // 移除事件监听器
});

onUnmounted(() => {
  clearInterval(intervalId);
  if (chartInstance) {
    chartInstance.dispose();
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
