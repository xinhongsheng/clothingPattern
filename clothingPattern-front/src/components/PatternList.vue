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
                <!-- 审核状态标签 -->
                <a-tag
                  :color="AUDIT_STATUS_COLOR_MAP[pattern.auditStatus]"
                  class="status-tag"
                >
                  {{ AUDIT_STATUS_MAP[pattern.auditStatus] }}
                </a-tag>
              </div>
            </template>
            <a-card-meta :title="pattern.patternName">
              <template #description>
                <div class="pattern-desc">
                  <div class="desc-text">
                    {{ pattern.description || '暂无描述' }}
                  </div>
                  <a-space wrap class="pattern-tags">
                    <a-tag :color="GENERATION_TYPE_COLOR_MAP[pattern.generationType]" v-if="pattern.generationType">
                      {{ GENERATION_TYPE_MAP[pattern.generationType] }}
                    </a-tag>
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
import { withDefaults } from 'vue'
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

  try {
    const res = await toggleLike({ patternId: id })
    if (res.data.code === 0 && res.data.data) {
      // 使用后端返回的准确数据更新
      const result = res.data.data
      pattern.isLiked = result.isLiked
      pattern.likeCount = result.likeCount
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error: any) {
    message.error('操作失败：' + error.message)
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
    transition: all 0.3s ease;
    border-radius: 8px;
    overflow: hidden;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    :deep(.ant-card-body) {
      padding: 16px;
    }

    :deep(.ant-card-actions) {
      background: #fafafa;

      > li {
        margin: 8px 0;

        &:hover {
          color: #1890ff;
        }
      }
    }
  }

  .like-action {
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover .like-icon {
      transform: scale(1.2);
    }

    .like-icon {
      font-size: 18px;
      transition: all 0.3s ease;

      &.liked {
        color: #ff4d4f;
        animation: heartBeat 0.3s ease;
      }
    }
  }

  @keyframes heartBeat {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.2);
    }
    100% {
      transform: scale(1);
    }
  }

  .pattern-cover {
    position: relative;
    height: 200px;
    overflow: hidden;
    background: #f5f5f5;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    &:hover img {
      transform: scale(1.05);
    }

    .status-tag {
      position: absolute;
      top: 8px;
      right: 8px;
      margin: 0;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }

  .pattern-desc {
    .desc-text {
      font-size: 13px;
      color: #8c8c8c;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.5;
      min-height: 40px;
    }

    .pattern-tags {
      margin-top: 8px;

      .ant-tag {
        margin-bottom: 4px;
        font-size: 12px;
      }
    }
  }

  :deep(.ant-card-meta-title) {
    font-size: 16px;
    font-weight: 600;
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
</style>
