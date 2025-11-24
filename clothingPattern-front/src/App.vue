<template>
  <div id="app">
    <BasicLayout></BasicLayout>
  </div>
</template>
<script setup lang="ts">
import BasicLayout from "@/layout/BasicLayout.vue"
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { onMounted } from 'vue'

const loginUserStore = useLoginUserStore()

// 应用初始化时自动获取登录用户信息
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
})
</script>
<style scoped></style>
