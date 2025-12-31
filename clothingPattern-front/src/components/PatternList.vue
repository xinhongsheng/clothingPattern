<template>
  <div class="pattern-list">
    <!-- 图案列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: pattern }">
        <a-list-item style="padding: 0">
          <!-- 单个图案 -->
          <a-card hoverable @click="doClickPattern(pattern)" class="pattern-card">
            <template #cover>
              <div class="pattern-cover">
                <img
                  :alt="pattern.patternName"
                  :src="pattern.thumbUrl ?? pattern.patternUrl"
                  loading="lazy"
                />
              </div>
            </template>
            <a-card-meta :title="pattern.patternName">
              <template #description>
                <div class="pattern-desc">
                  <div class="desc-text">
                    {{ pattern.description || '暂无描述' }}
                  </div>
                  <a-space wrap class="pattern-tags">
                    
                    <a-tag v-if="pattern.style">
                      {{ pattern.style }}
                    </a-tag>
                    <a-tag v-if="pattern.season" color="blue">
                      {{ pattern.season }}
                    </a-tag>
                  </a-space>
                </div>
              </template>
            </a-card-meta>
            <template #actions>
              <!-- 点赞按钮 -->
              <a-space @click="(e: Event) => handleLike(pattern, e)" class="like-action">
                <HeartOutlined v-if="!pattern.isLiked" class="like-icon" />
                <HeartFilled v-else class="like-icon liked" />
                <span>{{ pattern.likeCount || 0 }}</span>
              </a-space>
              <!-- 删除按钮（仅在showOp为true时显示） -->
              <a-space v-if="showOp" @click="(e: Event) => doDelete(pattern, e)">
                <DeleteOutlined />
                删除
              </a-space>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">

import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { DeleteOutlined, HeartOutlined, HeartFilled } from '@ant-design/icons-vue'
import { deletePattern } from '@/api/patternController'
import { toggleLike } from '@/api/likeController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import {
  AUDIT_STATUS_MAP,
  AUDIT_STATUS_COLOR_MAP,
  GENERATION_TYPE_MAP,
  GENERATION_TYPE_COLOR_MAP
} from '@/constants/pattern'

interface Props {
  dataList?: API.PatternVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 跳转至图案详情
const doClickPattern = (pattern: API.PatternVO) => {
  router.push({
    path: `/pattern/${pattern.id}`
  })
}

// 点赞/取消点赞
const handleLike = async (pattern: API.PatternVO, e: Event) => {
  e.stopPropagation()

  const id = pattern.id
  if (!id) {
    return
  }

  // 检查用户是否登录
  if (!loginUserStore.loginUser || !loginUserStore.loginUser.id) {
    message.warning('登录后即可点赞')
    return
  }

  // 乐观更新UI（先更新界面，给用户即时反馈）
  const previousLiked = pattern.isLiked
  const previousCount = pattern.likeCount || 0

  // 立即更新UI
  pattern.isLiked = !previousLiked
  pattern.likeCount = previousLiked ? previousCount - 1 : previousCount + 1

  try {
    const res = await toggleLike({ patternId: id })
    if (res.data.code === 0 && res.data.data) {
      // 使用后端返回的准确数据更新（确保数据一致性）
      const result = res.data.data
      pattern.isLiked = result.isLiked
      pattern.likeCount = result.likeCount

      // 提示用户操作成功
      message.success(result.isLiked ? '点赞成功' : '已取消点赞', 1)
    } else {
      // 失败时回滚UI
      pattern.isLiked = previousLiked
      pattern.likeCount = previousCount
      message.error('操作失败：' + res.data.message)
    }
  } catch (error: any) {
    // 失败时回滚UI
    pattern.isLiked = previousLiked
    pattern.likeCount = previousCount
    message.error('操作失败，请稍后重试')
    console.error('点赞操作失败：', error)
  }
}

// 删除
const doDelete = async (pattern: API.PatternVO, e: Event) => {
  e.stopPropagation()
  const id = pattern.id
  if (!id) {
    return
  }

  try {
    const res = await deletePattern({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 让外层刷新
      props?.onReload?.()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error: any) {
    message.error('删除失败：' + error.message)
  }
}
</script>

<style scoped>
.pattern-list {
  padding: 16px 0;

  .pattern-card {
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 16px;
    overflow: hidden;
    border: 1px solid rgba(0, 0, 0, 0.04);
    background: #ffffff;

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
      border-color: rgba(79, 70, 229, 0.1);
    }

    :deep(.ant-card-body) {
      padding: 20px;
    }

    :deep(.ant-card-actions) {
      background: #fafafa;
      border-top: 1px solid rgba(0, 0, 0, 0.04);
      
      > li {
        margin: 12px 0;
        color: #6b7280;
        transition: color 0.2s;

        &:hover {
          color: #4f46e5;
        }
      }
    }
  }

  .like-action {
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    display: inline-flex;
    align-items: center;
    gap: 6px;

    &:hover {
      color: #ef4444;
    }

    &:hover .like-icon {
      transform: scale(1.2);
    }

    .like-icon {
      font-size: 20px;
      transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

      &.liked {
        color: #ef4444;
        filter: drop-shadow(0 4px 6px rgba(239, 68, 68, 0.25));
        animation: heartBeat 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
      }
    }
    
    span {
      font-weight: 500;
      font-size: 14px;
    }
  }

  @keyframes heartBeat {
    0% { transform: scale(1); }
    50% { transform: scale(1.4); }
    100% { transform: scale(1); }
  }

  .pattern-cover {
    position: relative;
    height: 240px;
    overflow: hidden;
    background: #f3f4f6;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover img {
      transform: scale(1.1);
    }

    .status-tag {
      position: absolute;
      top: 12px;
      right: 12px;
      margin: 0;
      font-size: 12px;
      padding: 4px 10px;
      border-radius: 6px;
      font-weight: 600;
      backdrop-filter: blur(4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.04) 100%);
      opacity: 0;
      transition: opacity 0.3s;
    }
    
    &:hover::after {
      opacity: 1;
    }
  }

  .pattern-desc {
    .desc-text {
      font-size: 14px;
      color: #6b7280;
      margin-bottom: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.6;
      min-height: 44px;
    }

    .pattern-tags {
      margin-top: 12px;

      .ant-tag {
        margin-bottom: 6px;
        font-size: 12px;
        border: none;
        background: #f3f4f6;
        color: #6b7280;
        border-radius: 6px;
        padding: 2px 10px;
        
        &:last-child {
          margin-right: 0;
        }
      }
    }
  }

  :deep(.ant-card-meta-title) {
    font-size: 18px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 8px !important;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@media (max-width: 768px) {
  .pattern-list {
    padding: 8px 0;

    .pattern-cover {
      height: 160px;
    }
  }
}

/* 大屏幕适配 - 1600px */
@media (min-width: 1600px) {
  .pattern-list {
    .pattern-cover {
      height: 260px;
    }

    .pattern-card {
      border-radius: 18px;
    }

    :deep(.ant-card-body) {
      padding: 22px;
    }

    :deep(.ant-card-meta-title) {
      font-size: 19px;
    }

    .pattern-desc .desc-text {
      font-size: 15px;
      min-height: 48px;
    }
  }
}

/* 大屏幕适配 - 1920px (Full HD) */
@media (min-width: 1920px) {
  .pattern-list {
    padding: 20px 0;

    .pattern-cover {
      height: 280px;
    }

    .pattern-card {
      border-radius: 20px;

      &:hover {
        transform: translateY(-10px);
        box-shadow: 0 24px 48px rgba(0, 0, 0, 0.1);
      }
    }

    :deep(.ant-card-body) {
      padding: 24px;
    }

    :deep(.ant-card-meta-title) {
      font-size: 20px;
      margin-bottom: 10px !important;
    }

    .pattern-desc {
      .desc-text {
        font-size: 15px;
        line-height: 1.7;
        min-height: 50px;
      }

      .pattern-tags .ant-tag {
        font-size: 13px;
        padding: 3px 12px;
      }
    }

    .like-action {
      .like-icon {
        font-size: 22px;
      }
      span {
        font-size: 15px;
      }
    }
  }
}

/* 超大屏幕适配 - 2560px (2K) */
@media (min-width: 2560px) {
  .pattern-list {
    padding: 24px 0;

    .pattern-cover {
      height: 320px;
    }

    .pattern-card {
      border-radius: 22px;

      &:hover {
        transform: translateY(-12px);
        box-shadow: 0 28px 56px rgba(0, 0, 0, 0.12);
      }
    }

    :deep(.ant-card-body) {
      padding: 28px;
    }

    :deep(.ant-card-meta-title) {
      font-size: 22px;
      margin-bottom: 12px !important;
    }

    :deep(.ant-card-actions) {
      > li {
        margin: 14px 0;
        font-size: 15px;
      }
    }

    .pattern-desc {
      .desc-text {
        font-size: 16px;
        line-height: 1.75;
        min-height: 56px;
        margin-bottom: 16px;
      }

      .pattern-tags {
        margin-top: 16px;

        .ant-tag {
          font-size: 14px;
          padding: 4px 14px;
          border-radius: 8px;
          margin-bottom: 8px;
        }
      }
    }

    .like-action {
      .like-icon {
        font-size: 24px;
      }
      span {
        font-size: 16px;
      }
    }
  }
}
</style>
