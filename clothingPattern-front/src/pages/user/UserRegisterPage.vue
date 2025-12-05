<template>
  <!-- 新增外层容器：承载背景图 + 居中注册框 -->
  <div class="login-container">
    <div id="userRegisterPage">
      <h2 class="title">服装图案智能创作平台-用户注册</h2>
      <div class="desc">服装图案智能创作平台</div>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item
          name="userAccount"
          :rules="[{ required: true ,message: '请输入账号!' }]"
        >
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[{ required: true, message: '请输入密码!' },
          { min: 8, message: '密码不能少于8位' }]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder='请输入密码'  />
        </a-form-item>

        <a-form-item
          name="checkPassword"
          :rules="[{ required: true, message: '请输入确认密码!' },
          { min: 8, message: '确认密码不能少于8位' }]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder='请输入确认密码'  />
        </a-form-item>

        <div class="tips">
          已有账号？<RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item >
          <a-button type="primary" html-type="submit">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import type { API } from '@/api/typings.d.ts'
import { useRouter } from 'vue-router'
import {userRegister} from '@/api/userController.ts'
import { message } from 'ant-design-vue'
const router = useRouter()
/** 表单数据 */
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

// 注册提交
const handleSubmit = async(values: any) => {
  if (values.userPassword !== values.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  const res=await userRegister(values)
  if (res.data.code === 0&& res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace:true
    })
  } else {
    message.error('注册失败'+res.data.message)
  }
}
</script>

<style scoped>
/* 外层容器：和登录页保持一致的背景图 + 居中布局 */
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  /* 与登录页相同的背景图路径 */
  background-image: url('@/assets/backgroundImage/loginAndRegister.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}

#userRegisterPage {
  max-width: 360px;
  padding: 24px;
  /* 半透明背景，和登录页保持一致的透明度 */
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.title {
  text-align: center;
  margin-bottom: 16px;
  color: #222; /* 加深文字颜色，保证透明背景下可读性 */
  font-weight: 600;
}

.desc {
  text-align: center;
  color: #444; /* 调整文字颜色 */
  margin-bottom: 24px;
}

.tips {
  margin-bottom: 16px;
  color: #555; /* 调整文字颜色 */
  font-size: 13px;
  text-align: right;
}

/* 修复按钮宽度：覆盖antd组件样式，保证占满宽度 */
:deep(.ant-btn) {
  width: 100%;
}

/* 可选：优化输入框样式，提升透明背景下的辨识度 */
:deep(.ant-input) {
  background-color: rgba(255, 255, 255, 0.85);
  border-color: rgba(200, 200, 200, 0.5);
}
</style>
