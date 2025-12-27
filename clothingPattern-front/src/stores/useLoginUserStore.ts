// Vue 和 Pinia 的函数现在会自动导入，无需手动 import

import { getLoginUser } from "@/api/userController";
import { defineStore } from 'pinia'
import { ref } from 'vue'
// 定义登录用户的 Pinia Store
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })
  /** 获取登录用户信息 */
  async function fetchLoginUser() {
    try {
      const res = await getLoginUser();
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data;
      } else {
        // 未登录或登录失效，设置为默认值
        loginUser.value = { userName: '未登录' };
      }
    } catch (error) {
      // 请求失败（未登录），设置为默认值
      loginUser.value = { userName: '未登录' };
    }
  }

  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
