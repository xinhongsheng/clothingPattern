<template>
  <!-- 新增外层容器：承载背景图 + 居中登录框 -->
  <div class="login-container">
    <div id="userLoginPage">
      <h2 class="title">服装图案智能创作平台-用户登录</h2>
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

        <div class="tips">
          没有账号？<RouterLink to="/user/register">去注册</RouterLink>
        </div>
        <a-form-item >
          <a-button type="primary" html-type="submit">登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogin } from '@/api/userController.ts'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
/** 表单数据 */
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
  remember: true,
})

const loginUserStore = useLoginUserStore();
// 登录提交
const handleSubmit = async(values: any) => {
  const res=await userLogin(values)
  if (res.data.code === 0&& res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    
    // 获取redirect参数，如果有则跳转回原页面，否则跳转到首页
    const redirect = route.query.redirect as string || '/'
    router.push({
      path: redirect,
      replace: true
    })
  } else {
    message.error('登录失败'+res.data.message)
  }
}
</script>

<style scoped>
/* 外层容器：全屏背景图 + 居中登录框 */
.login-container {
  position: fixed; /* 固定定位，占满整个视口 */
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  /* 背景图核心属性 */
  background-image: url('@/assets/backgroundImage/loginAndRegister.png');
  background-size: cover; /* 覆盖整个容器，自适应拉伸 */
  background-position: center; /* 背景图居中显示 */
  background-repeat: no-repeat; /* 禁止重复 */
  /* 让登录框水平+垂直居中 */
  display: flex;
  align-items: center;
  justify-content: center;
}

#userLoginPage {
  max-width: 360px;
  padding: 24px; /* 内边距，提升美观度 */
  background-color: rgba(255, 255, 255, 0.5); /* 登录框半透明白色背景，避免文字看不清 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1); /* 轻微阴影，提升层次感 */
}

.title {
  text-align: center;
  margin-bottom: 16px;
  color: #333;
}

.desc {
  text-align: center;
  color: #666;
  margin-bottom: 24px;
}

.tips {
  margin-bottom: 16px;
  color: #666;
  font-size: 13px;
  text-align: right;
}

/* 修复按钮宽度：通过深度选择器覆盖antd组件样式 */
:deep(.ant-btn) {
  width: 100%; /* 按钮占满宽度 */
}
</style>
