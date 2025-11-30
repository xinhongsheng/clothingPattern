<template>
    <div class="comment-section">
      <!-- 评论统计 -->
      <div class="comment-header">
        <h3>
          <CommentOutlined /> 评论 
          <span class="comment-count">({{ statistics.totalComments || 0 }})</span>
        </h3>
        <div class="statistics">
          <span><MessageOutlined /> {{ statistics.mainComments || 0 }} 条主评论</span>
          <span><HeartOutlined /> {{ statistics.totalLikes || 0 }} 个赞</span>
        </div>
      </div>
  
      <!-- 添加评论输入框 -->
      <div class="add-comment-section">
        <a-avatar :src="loginUser?.userAvatar" :size="40">
          {{ loginUser?.userName?.charAt(0) || '游' }}
        </a-avatar>
        <div class="input-wrapper">
          <a-textarea
            v-model:value="commentContent"
            :placeholder="replyTarget ? `回复 @${replyTarget.userName}` : '写下你的评论...'"
            :rows="3"
            :maxlength="500"
            show-count
            @pressEnter="handleSubmitComment"
          />
          <div class="action-bar">
            <a-button v-if="replyTarget" size="small" @click="cancelReply">
              取消回复
            </a-button>
            <a-button
              type="primary"
              :loading="submitLoading"
              :disabled="!commentContent.trim()"
              @click="handleSubmitComment"
            >
              {{ replyTarget ? '回复' : '发表评论' }}
            </a-button>
          </div>
        </div>
      </div>
  
      <!-- 评论列表 -->
      <a-spin :spinning="loading">
        <div class="comment-list">
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-item"
          >
            <!-- 主评论 -->
            <div class="comment-main">
              <a-avatar :src="comment.userAvatar" :size="40">
                {{ comment.userName?.charAt(0) }}
              </a-avatar>
              <div class="comment-content-wrapper">
                <div class="comment-header-info">
                  <span class="user-name">{{ comment.userName }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div class="comment-actions">
                  <a-button
                    type="text"
                    size="small"
                    @click="handleLikeComment(comment)"
                    :class="{ liked: comment.liked }"
                  >
                    <HeartOutlined v-if="!comment.liked" />
                    <HeartFilled v-else />
                    {{ comment.likeCount || 0 }}
                  </a-button>
                  <a-button
                    type="text"
                    size="small"
                    @click="handleReply(comment)"
                  >
                    <MessageOutlined />
                    回复 {{ comment.replyCount > 0 ? `(${comment.replyCount})` : '' }}
                  </a-button>
                  <a-button
                    v-if="comment.userId === loginUser?.id"
                    type="text"
                    size="small"
                    danger
                    @click="handleDeleteComment(comment.id)"
                  >
                    <DeleteOutlined />
                    删除
                  </a-button>
                </div>
  
                <!-- 回复列表 -->
                <div v-if="comment.children && comment.children.length > 0" class="reply-list">
                  <div
                    v-for="reply in comment.children"
                    :key="reply.id"
                    class="reply-item"
                  >
                    <a-avatar :src="reply.userAvatar" :size="32">
                      {{ reply.userName?.charAt(0) }}
                    </a-avatar>
                    <div class="reply-content-wrapper">
                      <div class="reply-header-info">
                        <span class="user-name">{{ reply.userName }}</span>
                        <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                      </div>
                      <div class="reply-content">
                        <span v-if="reply.replyToUserName" class="reply-to">@{{ reply.replyToUserName }} </span>
                        {{ reply.content }}
                      </div>
                      <div class="reply-actions">
                        <a-button
                          type="text"
                          size="small"
                          @click="handleLikeComment(reply)"
                          :class="{ liked: reply.liked }"
                        >
                          <HeartOutlined v-if="!reply.liked" />
                          <HeartFilled v-else />
                          {{ reply.likeCount || 0 }}
                        </a-button>
                        <a-button
                          type="text"
                          size="small"
                          @click="handleReply(reply, comment)"
                        >
                          <MessageOutlined />
                          回复
                        </a-button>
                        <a-button
                          v-if="reply.userId === loginUser?.id"
                          type="text"
                          size="small"
                          danger
                          @click="handleDeleteComment(reply.id)"
                        >
                          <DeleteOutlined />
                          删除
                        </a-button>
                      </div>
                    </div>
                  </div>
                  <!-- 查看更多回复 -->
                  <a-button
                    v-if="comment.replyCount > 3 && !isExpanded(comment.id) && comment.id"
                    type="link"
                    size="small"
                    class="view-more-replies"
                    @click="loadAllReplies(comment)"
                  >
                    查看全部 {{ comment.replyCount }} 条回复 <DownOutlined />
                  </a-button>
                </div>
              </div>
            </div>
          </div>
  
          <!-- 空状态 -->
          <a-empty
            v-if="!loading && comments.length === 0"
            description="暂无评论，快来发表第一条评论吧~"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
          />
        </div>
  
        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination-wrapper">
          <a-pagination
            v-model:current="current"
            v-model:pageSize="pageSize"
            :total="total"
            :show-size-changer="false"
            :show-total="(total) => `共 ${total} 条评论`"
            @change="loadComments"
          />
        </div>
      </a-spin>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, computed } from 'vue'
  import { message, Modal, Empty } from 'ant-design-vue'
  import {
    CommentOutlined,
    MessageOutlined,
    HeartOutlined,
    HeartFilled,
    DeleteOutlined,
    DownOutlined
  } from '@ant-design/icons-vue'
  import {
    addComment,
    getPatternComments,
    toggleCommentLike,
    deleteComment,
    getCommentStatistics,
    getCommentReplies
  } from '@/api/commentController'
  import { useLoginUserStore } from '@/stores/useLoginUserStore'
  import dayjs from 'dayjs'
  import relativeTime from 'dayjs/plugin/relativeTime'
  import 'dayjs/locale/zh-cn'
  
  dayjs.extend(relativeTime)
  dayjs.locale('zh-cn')
  
  const props = defineProps<{
    patternId: number
  }>()
  
  const loginUserStore = useLoginUserStore()
  const loginUser = computed(() => loginUserStore.loginUser)
  
const comments = ref<API.CommentVO[]>([])
const statistics = ref<API.CommentStatisticsVO>({})
const commentContent = ref('')
const replyTarget = ref<API.CommentVO | null>(null)
const parentComment = ref<API.CommentVO | null>(null)
const loading = ref(false)
const submitLoading = ref(false)
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const expandedComments = ref<Set<number>>(new Set()) // 记录已展开的评论ID
  
  // 加载评论列表
  const loadComments = async () => {
    loading.value = true
    try {
      const res = await getPatternComments({
        patternId: props.patternId,
        current: current.value,
        pageSize: pageSize.value
      })
      if (res.data.code === 0 && res.data.data) {
        comments.value = res.data.data.list || []
        total.value = res.data.data.total || 0
      } else {
        message.error('加载评论失败：' + res.data.message)
      }
    } catch (error: any) {
      message.error('加载评论失败：' + error.message)
    } finally {
      loading.value = false
    }
  }
  
  // 加载统计信息
  const loadStatistics = async () => {
    try {
      const res = await getCommentStatistics({ patternId: props.patternId })
      if (res.data.code === 0 && res.data.data) {
        statistics.value = res.data.data
      }
    } catch (error: any) {
      console.error('加载统计信息失败：', error)
    }
  }
  
  // 提交评论
  const handleSubmitComment = async () => {
    if (!loginUser.value) {
      message.warning('请先登录')
      return
    }
  
    if (!commentContent.value.trim()) {
      message.warning('请输入评论内容')
      return
    }
  
    submitLoading.value = true
    try {
      const res = await addComment({
        patternId: props.patternId,
        content: commentContent.value.trim(),
        parentId: replyTarget.value?.id || undefined
      })
  
      if (res.data.code === 0) {
        message.success(replyTarget.value ? '回复成功' : '评论成功')
        commentContent.value = ''
        replyTarget.value = null
        parentComment.value = null
        
        // 重新加载评论列表和统计信息
        await Promise.all([loadComments(), loadStatistics()])
      } else {
        message.error('发表失败：' + res.data.message)
      }
    } catch (error: any) {
      message.error('发表失败：' + error.message)
    } finally {
      submitLoading.value = false
    }
  }
  
  // 回复评论
  const handleReply = (comment: API.CommentVO, parent?: API.CommentVO) => {
    if (!loginUser.value) {
      message.warning('请先登录')
      return
    }
    
    replyTarget.value = comment
    parentComment.value = parent || comment
    
    // 滚动到输入框
    const inputSection = document.querySelector('.add-comment-section')
    if (inputSection) {
      inputSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
    
    // 聚焦输入框
    setTimeout(() => {
      const textarea = document.querySelector('.add-comment-section textarea') as HTMLTextAreaElement
      if (textarea) {
        textarea.focus()
      }
    }, 300)
  }
  
  // 取消回复
  const cancelReply = () => {
    replyTarget.value = null
    parentComment.value = null
  }
  
  // 点赞评论
  const handleLikeComment = async (comment: API.CommentVO) => {
    if (!loginUser.value) {
      message.warning('请先登录')
      return
    }
  
    try {
      const res = await toggleCommentLike({ commentId: comment.id! })
      if (res.data.code === 0) {
        const isLiked = res.data.data
        comment.liked = isLiked
        comment.likeCount = (comment.likeCount || 0) + (isLiked ? 1 : -1)
        
        // 更新统计信息
        loadStatistics()
      } else {
        message.error('操作失败：' + res.data.message)
      }
    } catch (error: any) {
      message.error('操作失败：' + error.message)
    }
  }
  
  // 删除评论
  const handleDeleteComment = (commentId: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除这条评论吗？删除后无法恢复。',
      okText: '确定',
      cancelText: '取消',
      okType: 'danger',
      onOk: async () => {
        try {
          const res = await deleteComment({ commentId })
          if (res.data.code === 0) {
            message.success('删除成功')
            // 重新加载评论列表和统计信息
            await Promise.all([loadComments(), loadStatistics()])
          } else {
            message.error('删除失败：' + res.data.message)
          }
        } catch (error: any) {
          message.error('删除失败：' + error.message)
        }
      }
    })
  }
  
  // 格式化时间
  const formatTime = (time: any) => {
    if (!time) return ''
    return dayjs(time).fromNow()
  }

  // 加载所有回复
  const loadAllReplies = async (comment: API.CommentVO) => {
    if (!comment.id) {
      message.error('评论ID无效，无法加载回复');
      return;
    }
    try {
      const res = await getCommentReplies({ commentId: comment.id })
      if (res.data.code === 0 && res.data.data) {
        // 更新评论的回复列表
        comment.children = res.data.data
        // 标记为已展开
        expandedComments.value.add(comment.id)
      } else {
        message.error('加载回复失败：' + res.data.message)
      }
    } catch (error: any) {
      message.error('加载回复失败：' + error.message)
    }
  }

  // 检查评论是否已展开
  const isExpanded = (commentId: number) => {
    return expandedComments.value.has(commentId)
  }
  
  // 初始化
  onMounted(() => {
    loadComments()
    loadStatistics()
  })
  </script>
  
  <style scoped lang="scss">
  .comment-section {
    margin-top: 32px;
    padding: 24px;
    background: #fff;
    border-radius: 8px;
  
    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid #f0f0f0;
  
      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
  
        .comment-count {
          color: #999;
          font-weight: normal;
          font-size: 14px;
        }
      }
  
      .statistics {
        display: flex;
        gap: 24px;
        color: #666;
        font-size: 14px;
  
        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  
    .add-comment-section {
      display: flex;
      gap: 12px;
      margin-bottom: 24px;
      padding: 16px;
      background: #fafafa;
      border-radius: 8px;
  
      .input-wrapper {
        flex: 1;
  
        .action-bar {
          display: flex;
          justify-content: flex-end;
          gap: 8px;
          margin-top: 8px;
        }
      }
    }
  
    .comment-list {
      .comment-item {
        margin-bottom: 24px;
        padding-bottom: 24px;
        border-bottom: 1px solid #f0f0f0;
  
        &:last-child {
          margin-bottom: 0;
          padding-bottom: 0;
          border-bottom: none;
        }
  
        .comment-main {
          display: flex;
          gap: 12px;
  
          .comment-content-wrapper {
            flex: 1;
  
            .comment-header-info {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 8px;
  
              .user-name {
                font-weight: 500;
                color: #333;
              }
  
              .comment-time {
                color: #999;
                font-size: 12px;
              }
            }
  
            .comment-content {
              color: #333;
              line-height: 1.6;
              margin-bottom: 8px;
              word-break: break-word;
            }
  
            .comment-actions {
              display: flex;
              gap: 16px;
  
              .ant-btn {
                padding: 0;
                height: auto;
                color: #666;
  
                &:hover {
                  color: #1890ff;
                }
  
                &.liked {
                  color: #ff4d4f;
  
                  &:hover {
                    color: #ff7875;
                  }
                }
              }
            }
  
            .reply-list {
              margin-top: 16px;
              padding: 12px;
              background: #fafafa;
              border-radius: 8px;
  
              .reply-item {
                display: flex;
                gap: 8px;
                margin-bottom: 12px;
  
                &:last-child {
                  margin-bottom: 0;
                }
  
                .reply-content-wrapper {
                  flex: 1;
  
                  .reply-header-info {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin-bottom: 4px;
  
                    .user-name {
                      font-weight: 500;
                      font-size: 14px;
                      color: #333;
                    }
  
                    .comment-time {
                      color: #999;
                      font-size: 12px;
                    }
                  }
  
                  .reply-content {
                    color: #333;
                    font-size: 14px;
                    line-height: 1.6;
                    margin-bottom: 4px;
                    word-break: break-word;
                    
                    .reply-to {
                      color: #1890ff;
                      font-weight: 500;
                      margin-right: 4px;
                    }
                  }
  
                  .reply-actions {
                    display: flex;
                    gap: 12px;
  
                    .ant-btn {
                      padding: 0;
                      height: auto;
                      font-size: 12px;
                      color: #666;
  
                      &:hover {
                        color: #1890ff;
                      }
  
                      &.liked {
                        color: #ff4d4f;
  
                        &:hover {
                          color: #ff7875;
                        }
                      }
                    }
                  }
                }
              }
  
              .view-more-replies {
                margin-top: 8px;
                padding: 0;
                height: auto;
                font-size: 13px;
              }
            }
          }
        }
      }
    }
  
    .pagination-wrapper {
      display: flex;
      justify-content: center;
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid #f0f0f0;
    }
  }
  
  @media (max-width: 768px) {
    .comment-section {
      padding: 16px;
  
      .comment-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
  
        .statistics {
          width: 100%;
          justify-content: space-between;
        }
      }
  
      .add-comment-section {
        flex-direction: column;
        gap: 8px;
      }
    }
  }
  </style>
  