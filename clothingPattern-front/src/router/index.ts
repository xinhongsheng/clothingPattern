import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import UserLoginPage from '../pages/user/UserLoginPage.vue'
import UserRegisterPage from '../pages/user/UserRegisterPage.vue'
import UserManagePage from '../pages/admin/UserManagePage.vue'
import PatternGenerationPage from '@/pages/PatternGenerationPage.vue'
import MJPatternGenerationPage from '@/pages/MJPatternGenerationPage.vue'
import PatternManagePage from '@/pages/admin/PatternManagePage.vue'
import PatternDetailPage from '@/pages/PatternDetailPage.vue'
import AiPage from '@/pages/AiPage.vue'
import MyIdeaPage from '@/pages/MyIdeaPage.vue'
import AiTryOnPage from '@/pages/AiTryOnPage.vue'
import ImageFusionPage from '@/pages/ImageFusionPage.vue'
import DataAnalysisPage from '@/pages/admin/DataAnalysisPage.vue'
import ArticlePage from '@/pages/ArticlePage.vue'
import ArticleDetailPage from '@/pages/ArticleDetailPage.vue'
import ArticleManagePage from '@/pages/admin/ArticleManagePage.vue'
import ArticleEditPage from '@/pages/admin/ArticleEditPage.vue'
import BannerManagePage from '@/pages/admin/BannerManagePage.vue'
import UserProfilePage from '@/pages/user/UserProfilePage.vue'
import DataAnalysisPageV2 from '@/pages/admin/DataAnalysisPageV2.vue'



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
  // {
  //   path: '/patternGeneration',
  //   name: '智能创作',
  //   component: PatternGenerationPage,
  // },
  {
    path: '/mj/generation',
    name: '图案创作',
    component: MJPatternGenerationPage,
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
    path: '/ai/try-on',
    name: 'AI试衣',
    component: AiTryOnPage,
  },
  {
    path: '/image-fusion',
    name: '衣图智融',
    component: ImageFusionPage,
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
  {
    path: '/admin/dataAnalysisV2',
    name: '可视化大屏',
    component: DataAnalysisPageV2,
  },
  {
    path: '/article',
    name: '文章资讯',
    component: ArticlePage,
  },
  {
    path: '/article/:id',
    name: '文章详情',
    component: ArticleDetailPage,
    props: true,
  },
  {
    path: '/admin/article/manage',
    name: '文章管理',
    component: ArticleManagePage,
  },
  {
    path: '/admin/article/edit',
    name: '新建文章',
    component: ArticleEditPage,
  },
  {
    path: '/admin/article/edit/:id',
    name: '编辑文章',
    component: ArticleEditPage,
    props: true,
  },
  {
    path: '/admin/banner',
    name: '轮播图管理',
    component: BannerManagePage,
  },
  {
    path: '/user/profile',
    name: '个人中心',
    component: UserProfilePage,
  },
],



})

export default router
