<template>
  <div class="article-page">
    <div class="page-container">
      <!-- 顶部轮播图 -->
      <section class="banner-section">
        <a-carousel autoplay effect="fade" :dots="true" :autoplay-speed="4000">
          <div v-for="banner in bannerList" :key="banner.id" class="carousel-item">
            <a href="#" target="_blank" rel="noopener noreferrer">
              <div class="image-wrapper">
                <img :src="banner.imageUrl" :alt="banner.title" class="carousel-image" />
                <div class="banner-content">
                  <h2 class="banner-title">{{ banner.title }}</h2>
                </div>
              </div>
            </a>
          </div>
        </a-carousel>
      </section>

      <!-- 主要内容区 -->
      <div class="main-content-wrapper">
        <!-- 左侧文章列表 -->
        <main class="left-column">
          <!-- 分类和筛选栏 -->
          <div class="filter-bar card-glass">
            <div class="category-scroll">
              <a-button
                v-for="category in [{ id: null, categoryName: '全部' }, ...categories]"
                :key="category.id || 'all'"
                :type="selectedCategory === category.id ? 'primary' : 'text'"
                class="category-btn"
                @click="handleCategoryChange(category.id)"
              >
                {{ category.categoryName }}
              </a-button>
            </div>
            
            <div class="sort-actions">
               <a-input-search
                  v-model:value="searchKeyword"
                  placeholder="搜索精彩文章..."
                  class="custom-search"
                  @search="handleSearch"
                />
               <a-radio-group v-model:value="sortField" button-style="solid" @change="handleSortChange">
                  <a-radio-button value="publishTime">最新</a-radio-button>
                  <a-radio-button value="viewCount">最热</a-radio-button>
                </a-radio-group>
            </div>
          </div>

          <!-- 文章列表 -->
          <a-spin :spinning="loading">
            <div v-if="articleList.length > 0" class="article-list">
              <div
                v-for="article in articleList"
                :key="article.id"
                class="article-card"
                @click="goToDetail(article.id)"
              >
                <div class="card-cover">
                  <a-image
                    :src="article.coverImage || '/default-article-cover.jpg'"
                    :alt="article.title"
                    :preview="false"
                    class="cover-img"
                  />
                  <div class="badges">
                    <span v-if="article.isTop === 1" class="badge top">置顶</span>
                    <span v-if="article.isHot === 1" class="badge hot">热门</span>
                  </div>
                </div>
                <div class="card-content">
                  <div class="meta-top">
                    <a-tag color="blue" class="category-tag">{{ article.categoryName }}</a-tag>
                    <span class="publish-time">{{ formatTime(article.publishTime) }}</span>
                  </div>
                  <h3 class="title" :title="article.title">{{ article.title }}</h3>
                  <p class="summary">{{ article.summary }}</p>
                  
                  <div class="meta-footer">
                    <div class="author-info">
                       <a-avatar size="small" :src="article.userAvatar" class="avatar">
                          {{ article.author?.[0]?.toUpperCase() }}
                       </a-avatar>
                       <span class="author-name">{{ article.author }}</span>
                    </div>
                    <div class="stats">
                      <span title="阅读">
                        <EyeOutlined /> {{ formatNumber(article.viewCount) }}
                      </span>
                      <span title="点赞">
                        <LikeOutlined /> {{ formatNumber(article.likeCount) }}
                      </span>
                      <span title="评论">
                        <MessageOutlined /> {{ formatNumber(article.commentCount) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <a-empty v-else description="暂无相关文章" :image="Empty.PRESENTED_IMAGE_SIMPLE" class="empty-state" />
          </a-spin>

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination-wrapper">
            <a-pagination
              v-model:current="currentPage"
              v-model:pageSize="pageSize"
              :total="total"
              :show-size-changer="false"
              @change="handlePageChange"
            />
          </div>
        </main>

        <!-- 右侧边栏 -->
        <aside class="right-column">
          <!-- 热门推荐 -->
          <div class="sidebar-card hot-articles card-glass">
            <h3 class="sidebar-title">
              <FireOutlined class="icon-fire" /> 热门推荐
            </h3>
            <a-spin :spinning="hotLoading">
              <ul class="hot-list">
                <li v-for="(article, index) in hotArticles" :key="article.id" class="hot-item" @click="goToDetail(article.id)">
                  <span class="rank-num" :class="{ 'top-3': index < 3 }">{{ index + 1 }}</span>
                  <div class="hot-info">
                    <div class="hot-title">{{ article.title }}</div>
                    <div class="hot-view">{{ formatNumber(article.viewCount) }} 阅读</div>
                  </div>
                </li>
              </ul>
            </a-spin>
          </div>

          <!-- 推广/广告位 (ռλ) -->
          <div class="sidebar-card promotion-card card-glass">
            <div class="promo-content">
              <h4>分享你的灵感</h4>
              <p>加入我们的社区，发布你的第一篇设计文章。</p>
              <a-button type="primary" block>立即发布</a-button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import {
  UserOutlined,
  EyeOutlined,
  LikeOutlined,
  MessageOutlined,
  FireOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { getArticleList } from '@/api/articleController'
import { getBannerList } from '@/api/bannerController'
import { getCategories } from '@/api/articleCategoryController'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 数据定义
const categories = ref<API.ArticleCategory[]>([])
const articleList = ref<API.ArticleVO[]>([])
const hotArticles = ref<API.ArticleVO[]>([])
const bannerList = ref<any[]>([])
const selectedCategory = ref<string | null>(null)
const searchKeyword = ref('')
const sortField = ref('publishTime')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const hotLoading = ref(false)

// 辅助函数
const formatTime = (time: string) => time ? dayjs(time).fromNow() : ''
const formatNumber = (num: number) => {
  return num > 999 ? (num / 1000).toFixed(1) + 'k' : num
}

// 加载数据
const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.data.code === 0 && res.data.data) {
      categories.value = res.data.data
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      categoryId: selectedCategory.value || undefined,
      keyword: searchKeyword.value || undefined,
      sortField: sortField.value,
      sortOrder: 'desc',
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: 'PUBLISHED',
      auditStatus: 'APPROVED'
    })
    if (res.data.code === 0) {
      articleList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const loadHotArticles = async () => {
  hotLoading.value = true
  try {
    const res = await getArticleList({
      sortField: 'viewCount',
      sortOrder: 'desc',
      pageNum: 1,
      pageSize: 5,
      status: 'PUBLISHED',
      auditStatus: 'APPROVED'
    })
    if (res.data.code === 0) {
      hotArticles.value = res.data.data.records || []
    }
  } finally {
    hotLoading.value = false
  }
}

const loadBanners = async () => {
  try {
    const res = await getBannerList()
    if (res.data.code === 0) {
      bannerList.value = res.data.data
    }
  } catch (error) {
    console.error(error)
  }
}

// 事件处理
const handleCategoryChange = (val: string | null) => {
  if (selectedCategory.value === val) return
  selectedCategory.value = val
  currentPage.value = 1
  loadArticles()
}

const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

const handleSortChange = () => {
  currentPage.value = 1
  loadArticles()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToDetail = (id?: string) => {
  if (id) router.push(`/article/${id}`)
}

onMounted(() => {
  loadCategories()
  loadArticles()
  loadHotArticles()
  loadBanners()
})
</script>

<style scoped lang="scss">
// 变量
$primary-color: #1890ff;
$text-color: #333;
$secondary-text: #666;
$muted-text: #999;
$bg-color: #f0f2f5;
$card-bg: #ffffff;
$radius: 12px;
$shadow: 0 4px 20px rgba(0, 0, 0, 0.05);

.article-page {
  min-height: 100vh;
  background-color: $bg-color;
  padding: 24px 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.page-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 轮播图 */
.banner-section {
  margin-bottom: 24px;
  border-radius: $radius;
  overflow: hidden;
  box-shadow: $shadow;

  .carousel-item {
    height: 380px;
    position: relative;
    
    .image-wrapper {
        width: 100%;
        height: 100%;
        position: relative;
    }

    .carousel-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .banner-content {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        padding: 40px 24px 24px;
        background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
        color: white;
        
        .banner-title {
            color: white;
            font-size: 24px;
            font-weight: 600;
            margin: 0;
            text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }
    }
  }
}

/* 主内容布局 */
.main-content-wrapper {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 左侧列 */
.left-column {
  flex: 1;
  min-width: 0; // 防止flex子项溢出
}

/* 筛选栏 */
.filter-bar {
  background: $card-bg;
  padding: 16px 24px;
  border-radius: $radius;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: $shadow;

  .category-scroll {
    display: flex;
    overflow-x: auto;
    padding-bottom: 8px;
    gap: 8px;
    
    &::-webkit-scrollbar {
        height: 4px;
    }
    &::-webkit-scrollbar-thumb {
        background: #ddd;
        border-radius: 2px;
    }

    .category-btn {
      border-radius: 20px;
      font-weight: 500;
      flex-shrink: 0;
      
      &.ant-btn-text {
          color: $secondary-text;
          &:hover {
              color: $primary-color;
              background: rgba($primary-color, 0.05);
          }
      }
    }
  }
  
  .sort-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      
      .custom-search {
          max-width: 300px;
          :deep(.ant-input) {
              border-radius: 20px;
          }
           :deep(.ant-input-search-button) {
              border-radius: 0 20px 20px 0 !important;
          }
      }
  }
}

/* 文章卡片 */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-card {
  display: flex;
  background: $card-bg;
  border-radius: $radius;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  height: 200px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    
    .cover-img {
        transform: scale(1.05);
    }
  }

  .card-cover {
    width: 280px;
    height: 100%;
    position: relative;
    overflow: hidden;
    flex-shrink: 0;

    .cover-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s ease;
    }

    .badges {
      position: absolute;
      top: 12px;
      left: 12px;
      display: flex;
      gap: 6px;
      
      .badge {
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          color: white;
          font-weight: bold;
          backdrop-filter: blur(4px);
          
          &.top { background: rgba(255, 77, 79, 0.9); }
          &.hot { background: rgba(255, 122, 69, 0.9); }
      }
    }
  }

  .card-content {
    flex: 1;
    padding: 20px 24px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;

    .meta-top {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;
        
        .publish-time {
            color: $muted-text;
            font-size: 13px;
        }
    }

    .title {
      font-size: 18px;
      font-weight: 700;
      color: $text-color;
      margin-bottom: 8px;
      line-height: 1.4;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .summary {
      color: $secondary-text;
      font-size: 14px;
      line-height: 1.6;
      margin-bottom: 16px;
      flex: 1;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .meta-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid #f0f0f0;
      padding-top: 12px;
      
      .author-info {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: $text-color;
      }

      .stats {
        display: flex;
        gap: 16px;
        color: $muted-text;
        font-size: 13px;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
          &:hover { color: $primary-color; }
        }
      }
    }
  }
}

/* 右侧边栏 */
.right-column {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-card {
  background: $card-bg;
  border-radius: $radius;
  padding: 20px;
  box-shadow: $shadow;
  
  .sidebar-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .icon-fire { color: #ff4d4f; }
  }
}

.hot-list {
    list-style: none;
    padding: 0;
    margin: 0;
    
    .hot-item {
        display: flex;
        gap: 12px;
        padding: 10px 0;
        cursor: pointer;
        border-bottom: 1px dashed #f0f0f0;
        
        &:last-child { border-bottom: none; }
        &:hover .hot-title { color: $primary-color; }
        
        .rank-num {
            width: 20px;
            height: 20px;
            line-height: 20px;
            text-align: center;
            background: #f0f0f0;
            color: #999;
            font-size: 12px;
            font-weight: bold;
            border-radius: 4px;
            margin-top: 2px;
            
            &.top-3 {
                background: #ff4d4f;
                color: white;
            }
        }
        
        .hot-info {
            flex: 1;
            overflow: hidden;
            
            .hot-title {
                font-size: 14px;
                color: $text-color;
                margin-bottom: 4px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                transition: color 0.3s;
            }
            
            .hot-view {
                font-size: 12px;
                color: $muted-text;
            }
        }
    }
}

.promotion-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-align: center;
    
    .promo-content {
        h4 { color: white; margin-bottom: 8px; font-size: 18px; }
        p { color: rgba(255,255,255,0.8); margin-bottom: 16px; font-size: 13px; }
        button {
            background: white;
            color: #764ba2;
            border: none;
            font-weight: 600;
            &:hover { background: #f0f0f0; }
        }
    }
}

.pagination-wrapper {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .main-content-wrapper {
      flex-direction: column;
  }
  
  .right-column {
      width: 100%;
      order: 2; // 移动到下方
  }
  
  .left-column {
      width: 100%;
  }
  
  .article-card {
      height: auto;
      flex-direction: column;
      
      .card-cover {
          width: 100%;
          height: 180px;
      }
      
      .card-content {
          padding: 16px;
      }
  }
}

@media (max-width: 576px) {
    .banner-section .carousel-item {
        height: 200px;
    }
    
    .sort-actions {
        flex-direction: column;
        align-items: stretch;
        
        .custom-search {
            max-width: none;
        }
        
        .ant-radio-group {
            display: flex;
            .ant-radio-button-wrapper { flex: 1; text-align: center; }
        }
    }
}
</style>
