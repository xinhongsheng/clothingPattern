<template>
  <div id="patternDetailPage">
    <a-spin :spinning="loading" size="large">
      <a-card v-if="pattern" :bordered="false" class="detail-card">
        <!-- 返回按钮 -->
        <a-button type="link" @click="goBack" class="back-btn">
          <ArrowLeftOutlined /> 返回
        </a-button>

        <a-row :gutter="[32, 32]">
          <!-- 左侧：图片展示 -->
          <a-col :xs="24" :md="12">
            <div class="image-container">
              <a-image
                :src="pattern.patternUrl"
                :preview="{
                  src: pattern.patternUrl,
                }"
                :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDAwIiBoZWlnaHQ9IjQwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5Yqg6L295aSx6LSlPC90ZXh0Pjwvc3ZnPg=='"
                class="pattern-image"
              />
            </div>
          </a-col>

          <!-- 右侧：详细信息 -->
          <a-col :xs="24" :md="12">
            <div class="pattern-info">
              <!-- 图案名称 -->
              <h1 class="pattern-title">{{ pattern.patternName }}</h1>

              <!-- 审核状态 -->
              <a-tag
                v-if="pattern.auditStatus"
                :color="AUDIT_STATUS_COLOR_MAP[pattern.auditStatus]"
                class="status-tag"
              >
                {{ AUDIT_STATUS_MAP[pattern.auditStatus] }}
              </a-tag>

              <!-- 描述 -->
              <a-divider />
              <div class="info-section">
                <h3>图案描述</h3>
                <p class="description">{{ pattern.description || '暂无描述' }}</p>
              </div>

              <!-- 详细信息 -->
              <a-divider />
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="生成方式">
                  <a-tag
                    v-if="pattern.generationType"
                    :color="GENERATION_TYPE_COLOR_MAP[pattern.generationType]"
                  >
                    {{ GENERATION_TYPE_MAP[pattern.generationType] }}
                  </a-tag>
                  <span v-else>-</span>
                </a-descriptions-item>
                <a-descriptions-item label="风格" v-if="pattern.style">
                  {{ pattern.style }}
                </a-descriptions-item>
                <a-descriptions-item label="季节" v-if="pattern.season">
                  {{ pattern.season }}
                </a-descriptions-item>
                <a-descriptions-item label="目标受众" v-if="pattern.targetAudience">
                  {{ pattern.targetAudience }}
                </a-descriptions-item>
                <a-descriptions-item label="文件大小">
                  {{ formatFileSize(pattern.fileSize) }}
                </a-descriptions-item>
                <a-descriptions-item label="文件类型">
                  {{ pattern.fileType || 'image/png' }}
                </a-descriptions-item>
                <a-descriptions-item label="创建时间">
                  {{ formatDateTime(pattern.createTime) }}
                </a-descriptions-item>
                <a-descriptions-item label="创作者" v-if="pattern.user">
                  <a-space>
                    <a-avatar :src="pattern.user.userAvatar" :size="24">
                      {{ pattern.user.userName?.charAt(0) }}
                    </a-avatar>
                    {{ pattern.user.userName }}
                  </a-space>
                </a-descriptions-item>
              </a-descriptions>

              <!-- 操作按钮 -->
              <a-divider />
              <a-space class="action-buttons">
                <!-- 点赞按钮 -->
                <a-button size="large" @click="handleLike" :loading="likeLoading">
                  <HeartOutlined v-if="!pattern.isLiked" />
                  <HeartFilled v-else style="color: #ff4d4f" />
                  {{ pattern.isLiked ? '已点赞' : '点赞' }} ({{ pattern.likeCount || 0 }})
                </a-button>
                <a-button type="primary" size="large" @click="downloadPattern">
                  <DownloadOutlined /> 下载图案
                </a-button>
                <a-button size="large" v-if="isMyPattern" @click="showEditModal">
                  <EditOutlined /> 编辑
                </a-button>
                <a-button size="large" v-if="isMyPattern || isAdmin" danger @click="handleDelete">
                  <DeleteOutlined /> 删除
                </a-button>
              </a-space>
            </div>
          </a-col>
        </a-row>

        <!-- 参考图片 -->
        <a-row v-if="pattern.referenceImageUrl" :gutter="16" style="margin-top: 32px">
          <a-col :span="24">
            <a-divider orientation="left">参考图片</a-divider>
            <div class="reference-image">
              <a-image
                :src="pattern.referenceImageUrl"
                :preview="{
                  src: pattern.referenceImageUrl,
                }"
                :width="300"
              />
            </div>
          </a-col>
        </a-row>
      </a-card>

      <!-- 评论区 -->
      <CommentSection v-if="pattern" :patternId="pattern.id!" />

      <!-- 空状态 -->
      <a-empty v-else description="图案不存在" />
    </a-spin>

    <!-- 编辑模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑图案信息"
      width="600px"
      :confirm-loading="editLoading"
      @ok="handleEdit"
    >
      <a-form
        :model="editForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="图案名称" required>
          <a-input v-model:value="editForm.patternName" placeholder="请输入图案名称" />
        </a-form-item>
        <a-form-item label="图案描述">
          <a-textarea
            v-model:value="editForm.description"
            placeholder="请输入图案描述"
            :rows="4"
            show-count
            :maxlength="500"
          />
        </a-form-item>
        <a-form-item label="风格">
          <a-select v-model:value="editForm.style" placeholder="请选择风格">
            <a-select-option value="简约">简约</a-select-option>
            <a-select-option value="复古">复古</a-select-option>
            <a-select-option value="卡通">卡通</a-select-option>
            <a-select-option value="抽象">抽象</a-select-option>
            <a-select-option value="民族">民族</a-select-option>
            <a-select-option value="未来">未来</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="季节">
          <a-select v-model:value="editForm.season" placeholder="请选择季节">
            <a-select-option value="春季">春季</a-select-option>
            <a-select-option value="夏季">夏季</a-select-option>
            <a-select-option value="秋季">秋季</a-select-option>
            <a-select-option value="冬季">冬季</a-select-option>
            <a-select-option value="四季通用">四季通用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标受众">
          <a-input v-model:value="editForm.targetAudience" placeholder="请输入目标受众" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  DeleteOutlined,
  EditOutlined,
  HeartOutlined,
  HeartFilled
} from '@ant-design/icons-vue'
import { getPatternVoById, deletePattern, updatePattern, recordViewBehavior } from '@/api/patternController'
import { toggleLike } from '@/api/likeController'
import {
  AUDIT_STATUS_MAP,
  AUDIT_STATUS_COLOR_MAP,
  GENERATION_TYPE_MAP,
  GENERATION_TYPE_COLOR_MAP
} from '@/constants/pattern'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const pattern = ref<API.PatternVO>()
const loading = ref(true)
const likeLoading = ref(false)
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = ref<API.PatternUpdateRequest>({})

// 判断是否为当前用户的图案
const isMyPattern = computed(() => {
  return pattern.value?.userId === loginUserStore.loginUser?.id
})

// Add this
const isAdmin = computed(() => {
  return loginUserStore.loginUser?.userRole === 'admin'
})

// 获取图案详情
const fetchPatternDetail = async () => {
  const id = route.params.id
  if (!id) {
    message.error('图案ID不存在')
    router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await getPatternVoById({ id: id as any })
    if (res.data.code === 0 && res.data.data) {
      pattern.value = res.data.data
      // 记录用户浏览行为（用于协同过滤推荐）
      recordViewBehaviorAction(pattern.value.id!)
    } else {
      message.error('获取图案详情失败：' + res.data.message)
      router.push('/')
    }
  } catch (error: any) {
    message.error('获取图案详情失败：' + error.message)
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 记录用户浏览行为
const recordViewBehaviorAction = async (patternId: number) => {
  // 只有登录用户才记录行为
  if (!loginUserStore.loginUser?.id) {
    return
  }
  try {
    await recordViewBehavior({ patternId })
  } catch (error) {
    // 记录行为失败不影响主流程
    console.error('记录浏览行为失败:', error)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 下载图案
const downloadPattern = async () => {
  if (!pattern.value?.patternUrl) {
    message.error('图案地址不存在')
    return
  }

  try {
    message.loading({ content: '正在下载...', key: 'download' })
    
    // 获取图片 Blob
    const response = await fetch(pattern.value.patternUrl)
    if (!response.ok) {
      throw new Error('图片获取失败')
    }
    const blob = await response.blob()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const fileName = pattern.value.patternName 
      ? `${pattern.value.patternName}.png`
      : `pattern-${pattern.value.id}-${Date.now()}.png`
    link.download = fileName
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    // 释放 Blob URL
    window.URL.revokeObjectURL(url)
    
    message.success({ content: '下载成功', key: 'download' })
  } catch (error: any) {
    console.error('下载失败:', error)
    message.error({ content: '下载失败，请稍后重试', key: 'download' })
  }
}

// 点赞/取消点赞
const handleLike = async () => {
  if (!pattern.value?.id) {
    return
  }

  // 检查用户是否登录
  if (!loginUserStore.loginUser || !loginUserStore.loginUser.id) {
    message.warning('登录后即可点赞')
    return
  }

  // 乐观更新UI
  const previousLiked = pattern.value.isLiked
  const previousCount = pattern.value.likeCount || 0
  
  // 立即更新UI
  pattern.value.isLiked = !previousLiked
  pattern.value.likeCount = previousLiked ? previousCount - 1 : previousCount + 1

  likeLoading.value = true
  try {
    const res = await toggleLike({ patternId: pattern.value.id })
    if (res.data.code === 0 && res.data.data) {
      // 使用后端返回的准确数据更新
      const result = res.data.data
      pattern.value.isLiked = result.isLiked
      pattern.value.likeCount = result.likeCount
      
      message.success(result.isLiked ? '点赞成功' : '已取消点赞', 1)
    } else {
      // 失败时回滚UI
      pattern.value.isLiked = previousLiked
      pattern.value.likeCount = previousCount
      message.error('操作失败：' + res.data.message)
    }
  } catch (error: any) {
    // 失败时回滚UI
    pattern.value.isLiked = previousLiked
    pattern.value.likeCount = previousCount
    message.error('操作失败，请稍后重试')
    console.error('点赞操作失败：', error)
  } finally {
    likeLoading.value = false
  }
}

// 显示编辑模态框
const showEditModal = () => {
  if (!pattern.value) return
  
  editForm.value = {
    id: pattern.value.id,
    patternName: pattern.value.patternName,
    description: pattern.value.description,
    style: pattern.value.style,
    season: pattern.value.season,
    targetAudience: pattern.value.targetAudience
  }
  editModalVisible.value = true
}

// 提交编辑
const handleEdit = async () => {
  if (!editForm.value.patternName?.trim()) {
    message.error('请输入图案名称')
    return
  }

  editLoading.value = true
  try {
    const res = await updatePattern(editForm.value)
    if (res.data.code === 0) {
      message.success('编辑成功')
      editModalVisible.value = false
      // 重新获取详情
      await fetchPatternDetail()
    } else {
      message.error('编辑失败：' + res.data.message)
    }
  } catch (error: any) {
    message.error('编辑失败：' + error.message)
  } finally {
    editLoading.value = false
  }
}

// 删除图案
const handleDelete = () => {
  if (!pattern.value?.id) return

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个图案吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        const res = await deletePattern({ id: pattern.value!.id! })
        if (res.data.code === 0) {
          message.success('删除成功')
          router.push('/')
        } else {
          message.error('删除失败：' + res.data.message)
        }
      } catch (error: any) {
        message.error('删除失败：' + error.message)
      }
    }
  })
}

// 格式化文件大小
const formatFileSize = (bytes?: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 格式化日期时间
const formatDateTime = (date?: Date | string) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 页面加载时获取详情
onMounted(() => {
  fetchPatternDetail()
})
</script>

<style  scoped>
#patternDetailPage {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 24px;

  .detail-card {
    max-width: 1200px;
    margin: 0 auto;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .back-btn {
      margin-bottom: 16px;
      padding-left: 0;
    }

    .image-container {
      position: relative;
      border-radius: 8px;
      overflow: hidden;
      background: #fafafa;
      display: flex;
      align-items: center;
      justify-content: center;

      .pattern-image {
        width: 100%;
        height: auto;
        border-radius: 8px;
      }
    }

    .pattern-info {
      .pattern-title {
        font-size: 32px;
        font-weight: 700;
        margin: 0 0 16px 0;
        color: #1f1f1f;
      }

      .status-tag {
        font-size: 14px;
        padding: 4px 12px;
      }

      .info-section {
        margin: 16px 0;

        h3 {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 12px;
          color: #333;
        }

        .description {
          font-size: 14px;
          line-height: 1.8;
          color: #666;
          white-space: pre-wrap;
        }
      }

      .action-buttons {
        width: 100%;
        display: flex;
        gap: 12px;
        flex-wrap: wrap;

        .ant-btn {
          flex: 1;
          min-width: 120px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }
        }
      }
    }

    .reference-image {
      display: flex;
      justify-content: center;
      padding: 16px;
      background: #fafafa;
      border-radius: 8px;
    }
  }
}

@media (max-width: 768px) {
  #patternDetailPage {
    padding: 16px;

    .detail-card {
      .pattern-info {
        .pattern-title {
          font-size: 24px;
        }

        .action-buttons {
          flex-direction: column;

          .ant-btn {
            width: 100%;
          }
        }
      }
    }
  }
}
</style>
