<template>
  <div class="article-page">
    <!-- 顶部banner -->
    <div class="banner">
      <h1>资讯中心</h1>
      <p>最新的时尚资讯和穿搭指南</p>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <a-space :size="12" wrap>
        <a-button
          :type="selectedCategory === null ? 'primary' : 'default'"
          @click="handleCategoryChange(null)"
        >
          全部
        </a-button>
        <a-button
          v-for="category in categories"
          :key="category.id"
          :type="selectedCategory === category.id ? 'primary' : 'default'"
          @click="handleCategoryChange(category.id)"
        >
          {{ category.categoryName }}
        </a-button>
      </a-space>
    </div>

    <!-- 搜索和排序 -->
    <div class="search-sort-bar">
      <a-input-search
        v-model:value="searchKeyword"
        placeholder="搜索文章..."
        enter-button="搜索"
        size="large"
        style="width: 400px"
        @search="handleSearch"
      />

      <a-space :size="12">
        <a-select v-model:value="sortField" style="width: 120px" @change="handleSortChange">
          <a-select-option value="publishTime">发布时间</a-select-option>
          <a-select-option value="viewCount">阅读量</a-select-option>
          <a-select-option value="likeCount">点赞量</a-select-option>
        </a-select>
        <a-select v-model:value="sortOrder" style="width: 100px" @change="handleSortChange">
          <a-select-option value="desc">降序</a-select-option>
          <a-select-option value="asc">升序</a-select-option>
        </a-select>
      </a-space>
    </div>

    <!-- 文章列表 -->
    <a-spin :spinning="loading">
      <div v-if="articleList.length > 0" class="article-list">
        <div
          v-for="article in articleList"
          :key="article.id"
          class="article-item"
          @click="goToDetail(article.id)"
        >
          <div class="article-cover">
            <a-image
              :src="article.coverImage || '/default-article-cover.jpg'"
              :alt="article.title"
              :preview="false"
            />
            <div v-if="article.isTop === 1" class="top-badge">置顶</div>
            <div v-if="article.isHot === 1" class="hot-badge">热门</div>
          </div>
          <div class="article-content">
            <h3 class="article-title">{{ article.title }}</h3>
            <p class="article-summary">{{ article.summary }}</p>
            <div class="article-meta">
              <a-tag color="blue">{{ article.categoryName }}</a-tag>
              <span class="author">
                <UserOutlined />
                {{ article.author }}
              </span>
              <span class="time">
                <ClockCircleOutlined />
                {{ formatTime(article.publishTime) }}
              </span>
            </div>
            <div class="article-stats">
              <span>
                <EyeOutlined />
                {{ article.viewCount }}
              </span>
              <span>
                <LikeOutlined />
                {{ article.likeCount }}
              </span>
              <span>
                <MessageOutlined />
                {{ article.commentCount }}
              </span>
              <span>
                <StarOutlined />
                {{ article.collectCount }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else description="暂无文章" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
    </a-spin>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <a-pagination
        v-model:current="currentPage"
        v-model:pageSize="pageSize"
        :total="total"
        :show-size-changer="false"
        :show-total="(total) => `共 ${total} 篇文章`"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import {
  UserOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  LikeOutlined,
  MessageOutlined,
  StarOutlined,
} from '@ant-design/icons-vue'
import { getArticleList } from '@/api/articleController'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

import { formatTime } from '@/utils/time'
import { getCategories } from '@/api/articleCategoryController'
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 响应式数据
const categories = ref<API.ArticleCategory[]>([])
const articleList = ref<API.ArticleVO[]>([])
const selectedCategory = ref<string | null>(null)
const searchKeyword = ref('')
const sortField = ref('publishTime')
const sortOrder = ref('desc')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.data.code === 0 && res.data.data) {
      categories.value = res.data.data
    } else {
      message.error('加载分类失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('加载分类失败:', error)
    message.error('加载分类失败：' + error.message)
  }
}

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    const query: API.ArticleQueryRequest = {
      categoryId: selectedCategory.value || undefined,
      keyword: searchKeyword.value || undefined,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: 'PUBLISHED',
      auditStatus: 'APPROVED',
    }

    const res = await getArticleList(query)
    if (res.data.code === 0 && res.data.data) {
      articleList.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    } else {
      message.error('加载文章失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('加载文章失败:', error)
    message.error('加载文章失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 分类切换
const handleCategoryChange = (categoryId: string | null) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
  loadArticles()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

// 排序切换
const handleSortChange = () => {
  currentPage.value = 1
  loadArticles()
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadArticles()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 跳转到详情页
const goToDetail = (articleId: string | undefined) => {
  if (articleId) {
    router.push(`/article/${articleId}`)
  }
}

// 格式化时间
const formatTime = (time: any) => {
  if (!time) return ''
  return dayjs(time).fromNow()
}

// 生命周期
onMounted(() => {
  loadCategories()
  loadArticles()
})
</script>

<style scoped lang="scss">
.article-page {
  min-height: 100vh;
  background: #f5f5f5;

  .banner {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-align: center;
    padding: 60px 20px;
    margin-bottom: 30px;

    h1 {
      font-size: 36px;
      font-weight: bold;
      margin: 0 0 10px 0;
      color: white;
    }

    p {
      font-size: 16px;
      margin: 0;
      opacity: 0.9;
    }
  }

  .category-nav {
    max-width: 1200px;
    margin: 0 auto 30px;
    padding: 0 20px;
  }

  .search-sort-bar {
    max-width: 1200px;
    margin: 0 auto 30px;
    padding: 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 20px;

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: stretch;

      .ant-input-search {
        width: 100% !important;
      }
    }
  }

  .article-list {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 24px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .article-item {
    background: white;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    .article-cover {
      position: relative;
      width: 100%;
      height: 200px;
      overflow: hidden;

      :deep(.ant-image) {
        width: 100%;
        height: 100%;
      }

      :deep(img) {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s;
      }

      &:hover :deep(img) {
        transform: scale(1.05);
      }

      .top-badge,
      .hot-badge {
        position: absolute;
        top: 10px;
        right: 10px;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: bold;
        color: white;
      }

      .top-badge {
        background: #ff4d4f;
      }

      .hot-badge {
        background: #ff7a45;
        right: auto;
        left: 10px;
      }
    }

    .article-content {
      padding: 20px;

      .article-title {
        font-size: 18px;
        font-weight: 600;
        margin: 0 0 12px 0;
        color: #333;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        line-height: 1.5;
      }

      .article-summary {
        color: #666;
        margin: 0 0 16px 0;
        font-size: 14px;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .article-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        font-size: 12px;
        color: #999;
        flex-wrap: wrap;

        .author,
        .time {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }

      .article-stats {
        display: flex;
        gap: 20px;
        font-size: 13px;
        color: #666;
        padding-top: 12px;
        border-top: 1px solid #f0f0f0;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .pagination-wrapper {
    max-width: 1200px;
    margin: 40px auto;
    padding: 0 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
