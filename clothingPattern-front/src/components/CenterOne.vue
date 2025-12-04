# CenterOne.vue

<template>
  <div class="center-one-container">
    <div v-if="!loading" class="animate__animated animate__fadeIn myData">
      <div class="number-display">{{ displayNumber }}</div>
    </div>
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { getUserCount, getPatternCount } from '@/api/homeController'

// 定义props，用于区分显示的数据类型
const props = defineProps({
  type: {
    type: String,
    default: 'user', // 'user'表示用户总数，'pattern'表示图案总数
    validator: (value) => ['user', 'pattern'].includes(value),
  },
})

const loading = ref(false)
const currentNumber = ref(0)

// 格式化数字，添加千分位分隔符
const formatNumber = (num) => {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 计算属性，用于显示格式化后的数字
const displayNumber = computed(() => {
  return `${formatNumber(currentNumber.value)}个`
})

let refreshTimer

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    if (props.type === 'user') {
      // 获取用户数量
      const res = await getUserCount()
      console.log('getUserCount返回结果:', res)
      if (res.data.code === 0) {
        currentNumber.value = Number(res.data.data) || 0
      }
    } else if (props.type === 'pattern') {
      // 获取图案总数
      const res = await getPatternCount()
      console.log('getPatternCount返回结果:', res)
      if (res.data.code === 0) {
        currentNumber.value = Number(res.data.data) || 0
      }
    }
  } catch (error) {
    console.error(`获取${props.type === 'user' ? '用户' : '图案'}数量失败:`, error)
    currentNumber.value = 0
  } finally {
    loading.value = false
  }
}

// 监听type变化，重新获取数据
watch(
  () => props.type,
  () => {
    fetchData()
  },
)

onMounted(() => {
  fetchData() // 初始加载
  // 每5分钟刷新一次
  refreshTimer = setInterval(fetchData, 5 * 60 * 1000)
})

onUnmounted(() => {
  clearInterval(refreshTimer)
})
</script>

<style scoped>
.center-one-container {
  width: 200px;
  height: 50px;
  position: relative;
}

.myData {
  display: flex;
  width: 100%;
  height: 100%;
  justify-content: center;
  align-items: center;
}

.number-display {
  font-size: 32px;
  font-weight: bold;
  color: #00f2ff;
  text-shadow: 0 0 10px rgba(0, 242, 255, 0.7);
  letter-spacing: 2px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #00f2ff;
  text-shadow: 0 0 5px #00f2ff;
  font-weight: 700;
}
</style>
