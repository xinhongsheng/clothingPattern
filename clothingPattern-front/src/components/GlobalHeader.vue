<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">服装智能创作平台</div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar"></a-avatar>
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_idea">
                      <UserOutlined />
                      我的创意
                    </router-link>
                  </a-menu-item>
                  <!-- 退出登录：直接放在同一个 a-menu 内，去掉多余的 a-menu 嵌套 -->
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
                <!-- 闭合 a-menu -->
              </template>
            </a-dropdown>
          </div>

          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
// 补充：导入 UserOutlined 图标（之前代码漏了，会导致图标不显示）
import { HomeOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { ref, h ,computed} from 'vue' // 注意：h 函数需要导入（用于渲染图标）
import { userLoginOut } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router' // 补充：导入 useRouter

const router = useRouter() // 初始化 router（之前代码漏了，会导致 router.afterEach 报错）
const loginUserStore = useLoginUserStore()

const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/patternGeneration',
    label: '智能创作',
    title: '智能创作',
  },
  {
    key: '/admin/patternManage',
    label: '图案管理',
    title: '图案管理',
  },
  {
    key: '/ai',
    label: 'AI服装知识问答',
    title: 'AI服装知识问答',
  },
  {
    key: '/admin/DataAnalysis',
    label: '作品库分析',
    title: '作品库分析',
  },
  {
    key: '/article',
    label: '文章资讯',
    title: '文章资讯',
  },
  {
    key: '/admin/article/manage',
    label: '文章管理',
    title: '文章管理',
  },

  {
    key: 'others',
    label: h('a', { href: 'https://chat.deepseek.com/', target: '_blank' }, '免费壁纸库'),
    title: 'deepseek',
  },
]

const filterMenus = (menus = [] as MenuProps['items']) => {
  if (!Array.isArray(menus)) {
    return []
  }
  return menus.filter((menu) => {
    if (menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

const items = computed(<MenuProps['items']>(() => filterMenus(originItems)))

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  })
}

// 退出登录
const doLogout = async () => {
  const res = await userLoginOut()
  console.log(res)
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '',
      id: '', // 补充：清空 id，避免登录状态判断异常
      userAvatar: '', // 补充：清空头像
    })
    message.success('退出登录成功')
    router.push({ path: '/user/login' })
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

// 当前选中菜单
const current = ref<string[]>([])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  current.value = [to.path]
})
</script>
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 24px;
}

/* 优化：下拉菜单宽度自适应 */
:deep(.ant-dropdown-menu) {
  min-width: 120px;
}
</style>
