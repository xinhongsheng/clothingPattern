<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <!-- <img class="logo" src="../assets/logo.png" alt="logo" /> -->
            <!-- <div class="title">服装图案智能创作</div> -->
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
          style="justify-content: center; border-bottom: none"
        />
      </a-col>
      <a-col flex="200px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <!-- 通知铃铛图标 -->
            <a-popover 
              v-if="mjTaskStore.notification" 
              placement="bottomRight"
              trigger="click"
            >
              <template #content>
                <div class="notification-content" @click="goToMJGeneration">
                  <div class="notification-item">
                    <HighlightOutlined style="color: #1890ff; margin-right: 8px" />
                    <span>{{ getMJTaskStatusText() }}</span>
                  </div>
                </div>
              </template>
              <a-badge :count="mjTaskStore.hasUnread ? 1 : 0" :offset="[-2, 2]" class="notification-badge">
                <BellOutlined class="notification-icon" />
              </a-badge>
            </a-popover>
            
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
                  <a-menu-item>
                    <router-link to="/user/profile">
                      <UserOutlined />
                      个人中心
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
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
// 补充：导入导航栏所需的图标
import {
  HomeOutlined,
  LogoutOutlined,
  UserOutlined,
  HighlightOutlined,
  PicCenterOutlined,
  SkinOutlined,
  RobotOutlined,
  ReadOutlined,
  TeamOutlined,
  AppstoreOutlined,
  CrownOutlined,
  BellOutlined,
} from '@ant-design/icons-vue'
import { MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useMJTaskStore } from '@/stores/useMJTaskStore.ts'
import { ref, h, computed } from 'vue' // 注意：h 函数需要导入（用于渲染图标）
import { userLoginOut } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router' // 补充：导入 useRouter

const router = useRouter() // 初始化 router（之前代码漏了，会导致 router.afterEach 报错）
const loginUserStore = useLoginUserStore()
const mjTaskStore = useMJTaskStore()

// 获取MJ任务状态文本
const getMJTaskStatusText = () => {
  const notification = mjTaskStore.notification
  if (!notification) return ''
  switch (notification.status) {
    case 'PENDING':
    case 'PROCESSING':
      return '智能创作中...'
    case 'SUCCEEDED':
      return '创作完成，点击查看'
    case 'FAILED':
      return '创作失败，点击重试'
    default:
      return '智能创作'
  }
}

// 跳转到智能创作页面
const goToMJGeneration = () => {
  mjTaskStore.markRead()
  router.push('/mj/generation')
}

const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },

  {
    key: '/mj/generation',
    icon: () => h(HighlightOutlined),
    label: '智能创作',
    title: '智能创作',
  },
  // {
  //   key: '/patternGeneration',
  //   label: '智能创作',
  //   title: '智能创作',
  // },
  {
    key: '/image-fusion',
    icon: () => h(PicCenterOutlined),
    label: '衣图智融',
    title: '衣图智融',
  },

  {
    key: '/ai/try-on',
    icon: () => h(SkinOutlined),
    label: 'AI试衣',
    title: 'AI试衣',
  },
  {
    key: '/ai',
    icon: () => h(RobotOutlined),
    label: 'AI服装知识问答',
    title: 'AI服装知识问答',
  },
  {
    key: '/article',
    icon: () => h(ReadOutlined),
    label: '文章资讯',
    title: '文章资讯',
  },
  {
    key: '/admin/sys',
    label: '系统管理',
    title: '系统管理',
    icon: () => h(CrownOutlined),
    children: [
      {
        key: '/admin/userManage',
        icon: () => h(TeamOutlined),
        label: '用户管理',
        title: '用户管理',
      },
      {
        key: '/admin/patternManage',
        icon: () => h(AppstoreOutlined),
        label: '图案管理',
        title: '图案管理',
      },
      {
        key: '/admin/article/manage',
        icon: () => h(ReadOutlined),
        label: '文章管理',
        title: '文章管理',
      },

      {
        key: '/admin/comment/manage',
        icon: () => h(ReadOutlined),
        label: '评论管理',
        title: '评论管理',
      },
      {
        key: '/admin/DataAnalysisV2',
        icon: () => h(AppstoreOutlined),
        label: '数据分析中心',
        title: '数据分析中心',
      },
    ],
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
  /** 设置为透明 */
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

/* 通知图标样式 */
.notification-badge {
  margin-right: 16px;
  cursor: pointer;
}

.notification-icon {
  font-size: 20px;
  color: #666;
  transition: color 0.3s;
}

.notification-icon:hover {
  color: #1890ff;
}

/* 通知内容样式 */
.notification-content {
  min-width: 200px;
  cursor: pointer;
}

.notification-item {
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.user-login-status > div {
  display: flex;
  align-items: center;
}
</style>
