import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import UserLoginPage from '../pages/user/UserLoginPage.vue'
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'
import PatternGenerationPage from '@/pages/PatternGenerationPage.vue'
import PatternManagePage from '@/pages/admin/PatternManagePage.vue'
import PatternDetailPage from '@/pages/PatternDetailPage.vue'
import AiPage from '@/pages/AiPage.vue'
import MyIdeaPage from '@/pages/MyIdeaPage.vue'
import DataAnalysisPage from '@/pages/admin/DataAnalysisPage.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
    routes: [

  {
    path: '/',
    name: '主页',
    component: HomePage,

  },
  {
    path: '/user/login',
    name: '用户登录',
    component: UserLoginPage,
  },
  {
    path: '/user/register',
    name: '用户注册',
    component: UserRegisterPage,
  },
  {
    path: '/admin/userManage',
    name: '用户管理',
    component: UserManagePage,
  },
  {
    path: '/patternGeneration',
    name: '智能创作',
    component: PatternGenerationPage,
  },
  {
    path: '/admin/patternManage',
    name: '图案管理',
    component: PatternManagePage,
  },
  {
    path: '/pattern/:id',
    name: '图案详情',
    component: PatternDetailPage,
    props: true,
  },
  {
    path: '/ai',
    name: 'AI问答',
    component: AiPage,
  },
  {
    path: '/my_idea',
    name: '我的创意',
    component: MyIdeaPage,
  },
  {
    path: '/admin/dataAnalysis',
    name: '数据分析',
    component: DataAnalysisPage,
  },




],



})

export default router
