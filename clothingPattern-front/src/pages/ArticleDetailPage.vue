<template>
  <div class="article-detail-page">
    <a-spin :spinning="loading">
      <div v-if="article" class="article-container">
        <!-- 返回按钮 -->
        <a-button class="back-button" @click="goBack">
          <LeftOutlined />
          返回列表
        </a-button>

        <!-- 文章头部 -->
        <div class="article-header">
          <a-tag v-if="article.isTop === 1" color="red">置顶</a-tag>
          <a-tag v-if="article.isHot === 1" color="orange">热门</a-tag>
          <a-tag v-if="article.isRecommend === 1" color="blue">推荐</a-tag>
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <a-space :size="16" wrap>
              <span>
                <a-tag color="blue">{{ article.categoryName }}</a-tag>
              </span>
              <span>
                <UserOutlined />
                {{ article.author }}
              </span>
              <span>
                <ClockCircleOutlined />
                {{ formatTime(article.publishTime) }}
              </span>
              <span v-if="article.source">
                <LinkOutlined />
                {{ article.source }}
              </span>
            </a-space>
          </div>
          <div class="article-stats">
            <a-space :size="24">
              <span>
                <EyeOutlined />
                {{ article.viewCount }} 阅读
              </span>
              <span>
                <LikeOutlined />
                {{ article.likeCount }} 点赞
              </span>
              <span>
                <MessageOutlined />
                {{ article.commentCount }} 评论
              </span>
              <span>
                <StarOutlined />
                {{ article.collectCount }} 收藏
              </span>
            </a-space>
          </div>
        </div>

        <!-- 封面图 -->
        <div v-if="article.coverImage" class="article-cover">
          <a-image :src="article.coverImage" :alt="article.title" />
        </div>

        <!-- 文章摘要 -->
        <div v-if="article.summary" class="article-summary">
          <div class="summary-title">文章摘要</div>
          <p>{{ article.summary }}</p>
        </div>

        <!-- 文章内容 -->
        <div class="article-content" v-html="article.content"></div>

        <!-- 标签 -->
        <div v-if="article.tags" class="article-tags">
          <strong>标签：</strong>
          <a-space :size="8" wrap>
            <a-tag v-for="tag in parseTags(article.tags)" :key="tag" color="default">
              {{ tag }}
            </a-tag>
          </a-space>
        </div>

        <!-- 操作按钮 -->
        <div class="article-actions">
          <a-space :size="16">
            <a-button
              :type="article.liked ? 'primary' : 'default'"
              :icon="article.liked ? h(LikeFilled) : h(LikeOutlined)"
              size="large"
              @click="handleLike"
            >
              {{ article.liked ? '已点赞' : '点赞' }} ({{ article.likeCount }})
            </a-button>
            <a-button
              :type="article.collected ? 'primary' : 'default'"
              :icon="article.collected ? h(StarFilled) : h(StarOutlined)"
              size="large"
              @click="handleCollect"
            >
              {{ article.collected ? '已收藏' : '收藏' }} ({{ article.collectCount }})
            </a-button>
            <a-button :icon="h(ShareAltOutlined)" size="large" @click="handleShare">
              分享
            </a-button>
          </a-space>
        </div>

        <!-- 推荐文章 -->
        <div v-if="recommendArticles.length > 0" class="recommend-section">
          <h3>相关推荐</h3>
          <div class="recommend-list">
            <div
              v-for="item in recommendArticles"
              :key="item.id"
              class="recommend-item"
              @click="goToArticle(item.id)"
            >
              <div class="recommend-cover">
                <a-image
                  :src="item.coverImage || '/default-article-cover.jpg'"
                  :alt="item.title"
                  :preview="false"
                />
              </div>
              <div class="recommend-info">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary }}</p>
                <div class="recommend-meta">
                  <span><EyeOutlined /> {{ item.viewCount }}</span>
                  <span><LikeOutlined /> {{ item.likeCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <a-empty v-else description="文章不存在" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  LeftOutlined,
  UserOutlined,
  ClockCircleOutlined,
  LinkOutlined,
  EyeOutlined,
  LikeOutlined,
  LikeFilled,
  MessageOutlined,
  StarOutlined,
  StarFilled,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import {
  getArticleDetail,
  likeArticle,
  collectArticle,
  cancelCollectArticle,
  getRecommendArticles,
} from '@/api/articleController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

// 响应式数据
const article = ref<API.ArticleVO | null>(null)
const recommendArticles = ref<API.ArticleVO[]>([])
const loading = ref(false)

// 加载文章详情
const loadArticleDetail = async () => {
  loading.value = true
  try {
    const articleId = route.params.id as string
    if (!articleId) {
      message.error('文章ID无效')
      return
    }

    const res = await getArticleDetail({ id: articleId })
    if (res.data.code === 0 && res.data.data) {
      article.value = res.data.data
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

// 加载推荐文章
const loadRecommendArticles = async () => {
  try {
    const res = await getRecommendArticles({ limit: 3 })
    if (res.data.code === 0 && res.data.data) {
      recommendArticles.value = res.data.data.filter((item) => item.id !== article.value?.id)
    }
  } catch (error: any) {
    console.error('加载推荐文章失败:', error)
  }
}

// 点赞
const handleLike = async () => {
  if (!loginUserStore.loginUser) {
    message.warning('请先登录')
    return
  }

  if (!article.value?.id) return

  try {
    const res = await likeArticle({ articleId: article.value.id })
    if (res.data.code === 0 && res.data.data) {
      article.value.liked = res.data.data.isLiked
      article.value.likeCount = res.data.data.likeCount || 0
      message.success(res.data.data.isLiked ? '点赞成功' : '取消点赞')
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('点赞失败:', error)
    message.error('操作失败：' + error.message)
  }
}

// 收藏
const handleCollect = async () => {
  if (!loginUserStore.loginUser) {
    message.warning('请先登录')
    return
  }

  if (!article.value?.id) return

  try {
    if (article.value.collected) {
      // 取消收藏
      const res = await cancelCollectArticle({ articleId: article.value.id })
      if (res.data.code === 0) {
        article.value.collected = false
        article.value.collectCount = Math.max(0, (article.value.collectCount || 0) - 1)
        message.success('取消收藏')
      } else {
        message.error('操作失败：' + res.data.message)
      }
    } else {
      // 收藏
      const res = await collectArticle({ articleId: article.value.id })
      if (res.data.code === 0) {
        article.value.collected = true
        article.value.collectCount = (article.value.collectCount || 0) + 1
        message.success('收藏成功')
      } else {
        message.error('操作失败：' + res.data.message)
      }
    }
  } catch (error: any) {
    console.error('收藏失败:', error)
    message.error('操作失败：' + error.message)
  }
}

// 分享
const handleShare = () => {
  const url = window.location.href
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => {
      message.success('链接已复制到剪贴板')
    })
  } else {
    message.info('分享链接：' + url)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 跳转到文章
const goToArticle = (articleId: string | undefined) => {
  if (articleId) {
    router.push(`/article/${articleId}`)
    // 重新加载文章
    loadArticleDetail()
    loadRecommendArticles()
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 解析标签
const parseTags = (tags: string[] | string) => {
  if (!tags) return []
  if (Array.isArray(tags)) {
    return tags
  }
  return tags.split(',').filter((tag) => tag.trim())
}

// 格式化时间
const formatTime = (time: any) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 生命周期
onMounted(() => {
  loadArticleDetail()
  loadRecommendArticles()
})
</script>

<style scoped lang="scss">
.article-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;

  .article-container {
    max-width: 900px;
    margin: 0 auto;
    background: white;
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    @media (max-width: 768px) {
      padding: 20px;
    }

    .back-button {
      margin-bottom: 20px;
    }

    .article-header {
      margin-bottom: 30px;

      .article-title {
        font-size: 32px;
        font-weight: bold;
        margin: 16px 0 20px;
        line-height: 1.4;
        color: #333;

        @media (max-width: 768px) {
          font-size: 24px;
        }
      }

      .article-meta {
        margin-bottom: 16px;
        color: #666;
        font-size: 14px;

        span {
          display: inline-flex;
          align-items: center;
          gap: 4px;
        }
      }

      .article-stats {
        padding: 16px 0;
        border-top: 1px solid #f0f0f0;
        border-bottom: 1px solid #f0f0f0;
        color: #666;
        font-size: 14px;

        span {
          display: inline-flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .article-cover {
      margin-bottom: 30px;
      text-align: center;

      :deep(.ant-image) {
        width: 100%;
        border-radius: 8px;
        overflow: hidden;
      }

      :deep(img) {
        width: 100%;
        max-height: 500px;
        object-fit: cover;
      }
    }

    .article-summary {
      background: #f9f9f9;
      border-left: 4px solid #1890ff;
      padding: 20px;
      margin-bottom: 30px;
      border-radius: 4px;

      .summary-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: #333;
      }

      p {
        margin: 0;
        line-height: 1.8;
        color: #666;
      }
    }

    .article-content {
      font-size: 16px;
      line-height: 1.8;
      color: #333;
      margin-bottom: 30px;

      :deep(img) {
        max-width: 100%;
        height: auto;
        display: block;
        margin: 20px auto;
        border-radius: 4px;
      }

      :deep(p) {
        margin-bottom: 16px;
      }

      :deep(h2) {
        font-size: 24px;
        font-weight: 600;
        margin: 32px 0 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid #f0f0f0;
      }

      :deep(h3) {
        font-size: 20px;
        font-weight: 600;
        margin: 24px 0 12px;
      }

      :deep(ul),
      :deep(ol) {
        margin-bottom: 16px;
        padding-left: 24px;
      }

      :deep(li) {
        margin-bottom: 8px;
      }

      :deep(blockquote) {
        border-left: 4px solid #e0e0e0;
        padding-left: 16px;
        margin: 16px 0;
        color: #666;
        font-style: italic;
      }

      :deep(code) {
        background: #f5f5f5;
        padding: 2px 6px;
        border-radius: 3px;
        font-family: 'Courier New', monospace;
        font-size: 14px;
      }

      :deep(pre) {
        background: #f5f5f5;
        padding: 16px;
        border-radius: 4px;
        overflow-x: auto;
        margin: 16px 0;

        code {
          background: none;
          padding: 0;
        }
      }
    }

    .article-tags {
      margin-bottom: 30px;
      padding: 20px;
      background: #fafafa;
      border-radius: 4px;

      strong {
        margin-right: 12px;
      }
    }

    .article-actions {
      margin-bottom: 40px;
      padding: 24px 0;
      border-top: 1px solid #f0f0f0;
      border-bottom: 1px solid #f0f0f0;
      text-align: center;
    }

    .recommend-section {
      h3 {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 20px;
        color: #333;
      }

      .recommend-list {
        display: grid;
        gap: 16px;
      }

      .recommend-item {
        display: flex;
        gap: 16px;
        padding: 16px;
        background: #fafafa;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: #f0f0f0;
        }

        .recommend-cover {
          width: 120px;
          height: 80px;
          flex-shrink: 0;

          :deep(.ant-image) {
            width: 100%;
            height: 100%;
            border-radius: 4px;
            overflow: hidden;
          }

          :deep(img) {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }

        .recommend-info {
          flex: 1;
          min-width: 0;

          h4 {
            font-size: 16px;
            font-weight: 600;
            margin: 0 0 8px 0;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          p {
            font-size: 14px;
            color: #666;
            margin: 0 0 8px 0;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.5;
          }

          .recommend-meta {
            display: flex;
            gap: 16px;
            font-size: 12px;
            color: #999;

            span {
              display: flex;
              align-items: center;
              gap: 4px;
            }
          }
        }

        @media (max-width: 768px) {
          .recommend-cover {
            width: 80px;
            height: 60px;
          }
        }
      }
    }
  }
}
</style>
