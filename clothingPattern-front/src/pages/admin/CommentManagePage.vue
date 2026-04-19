<template>
  <div class="comment-manage-page">
    <a-card title="评论管理" :bordered="false">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-form layout="inline">
          <a-form-item label="用户名">
            <a-input
              v-model:value="searchParams.userName"
              placeholder="输入用户名"
              style="width: 150px"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="图案名">
            <a-input
              v-model:value="searchParams.patternName"
              placeholder="输入图案名"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="评论内容">
            <a-input
              v-model:value="searchParams.content"
              placeholder="搜索评论内容"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="doSearch">
                <SearchOutlined />
                搜索
              </a-button>
              <a-button @click="handleReset">
                <ReloadOutlined />
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>

      <!-- 评论列表 -->
      <a-table
        :columns="columns"
        :data-source="commentList"
        :loading="loading"
        :pagination="pagination"
        @change="doTableChange"
        row-key="id"
        :scroll="{ x: 900 }"
        class="comment-table"
      >
        <template #bodyCell="{ column, record }">
          <!-- 评论内容 -->
          <template v-if="column.key === 'content'">
            <a-tooltip :title="record.content">
              <div class="content-cell">{{ record.content }}</div>
            </a-tooltip>
          </template>

          <!-- 创建时间 -->
          <template v-else-if="column.key === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-popconfirm
              title="确定要删除这条评论吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" size="small" danger> 删除 </a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { listAdminCommentVoByPage, deleteComment } from '@/api/commentController'
import dayjs from 'dayjs'

// 数据
const commentList = ref<API.AdminCommentVO[]>([])
const loading = ref(false)

// 搜索参数
const searchParams = reactive({
  userName: '',
  patternName: '',
  content: '',
  current: 1,
  pageSize: 10,
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 150,
  },
  {
    title: '图案名',
    dataIndex: 'patternName',
    key: 'patternName',
    width: 200,
  },
  {
    title: '评论内容',
    dataIndex: 'content',
    key: 'content',
    ellipsis: true,
    width: 300,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right' as const,
  },
]

// 总数
const total = ref(0)

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const queryParams: any = {
      current: searchParams.current,
      pageSize: searchParams.pageSize,
    }

    // 只有当字段有值时才添加到查询参数
    if (searchParams.userName) {
      queryParams.userName = searchParams.userName
    }
    if (searchParams.patternName) {
      queryParams.patternName = searchParams.patternName
    }
    if (searchParams.content) {
      queryParams.content = searchParams.content
    }

    const res = await listAdminCommentVoByPage(queryParams)
    if (res.data.code === 0 && res.data.data) {
      commentList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    } else {
      message.error('加载评论失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('加载评论失败:', error)
    message.error('加载评论失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  loadComments()
}

// 重置
const handleReset = () => {
  searchParams.userName = ''
  searchParams.patternName = ''
  searchParams.content = ''
  searchParams.current = 1
  loadComments()
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  loadComments()
}

// 删除评论
const handleDelete = async (id: number) => {
  if (!id) {
    return
  }
  try {
    const res = await deleteComment({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      loadComments()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('删除失败:', error)
    message.error('删除失败：' + error.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-manage-page {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;
}

.search-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.content-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .comment-manage-page {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .comment-manage-page {
    padding: 16px;
  }

  .comment-manage-page :deep(.ant-card-head) {
    padding: 12px 16px;
  }

  .comment-manage-page :deep(.ant-card-head-title) {
    font-size: 16px;
  }

  .search-bar {
    padding: 12px;
  }

  .search-bar :deep(.ant-form-inline .ant-form-item) {
    display: block;
    margin-right: 0;
    margin-bottom: 10px;
    width: 100%;
  }

  .search-bar :deep(.ant-form-item-control) {
    width: 100%;
  }

  .search-bar :deep(.ant-input) {
    width: 100% !important;
  }

  .search-bar :deep(.ant-space) {
    width: 100%;
  }

  .search-bar :deep(.ant-btn) {
    flex: 1;
  }

  .comment-table :deep(.ant-table) {
    font-size: 13px;
  }

  .comment-table :deep(.ant-table-thead > tr > th) {
    padding: 10px 8px;
    font-size: 12px;
  }

  .comment-table :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
  }

  .content-cell {
    max-width: 150px;
    font-size: 12px;
  }

  .comment-manage-page :deep(.ant-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }

  .comment-manage-page :deep(.ant-pagination-total-text) {
    width: 100%;
    text-align: center;
  }
}

@media (max-width: 576px) {
  .comment-manage-page {
    padding: 12px;
  }

  .comment-manage-page :deep(.ant-card) {
    border-radius: 8px;
  }

  .comment-manage-page :deep(.ant-card-body) {
    padding: 12px;
  }

  .comment-manage-page :deep(.ant-card-head) {
    padding: 10px 12px;
    min-height: auto;
  }

  .comment-manage-page :deep(.ant-card-head-title) {
    font-size: 15px;
  }

  .search-bar {
    padding: 10px;
    border-radius: 6px;
  }

  .search-bar :deep(.ant-form-item-label) {
    font-size: 13px;
  }

  .search-bar :deep(.ant-input) {
    height: 34px;
    font-size: 13px;
  }

  .search-bar :deep(.ant-btn) {
    height: 34px;
    font-size: 13px;
  }

  .comment-table :deep(.ant-table) {
    font-size: 12px;
  }

  .comment-table :deep(.ant-table-thead > tr > th) {
    padding: 8px 6px;
    font-size: 11px;
  }

  .comment-table :deep(.ant-table-tbody > tr > td) {
    padding: 8px 6px;
  }

  .content-cell {
    max-width: 120px;
    font-size: 11px;
  }

  .comment-table :deep(.ant-btn-link) {
    padding: 2px 6px;
    font-size: 11px;
    height: auto;
  }

  .comment-manage-page :deep(.ant-pagination-item),
  .comment-manage-page :deep(.ant-pagination-prev),
  .comment-manage-page :deep(.ant-pagination-next) {
    min-width: 28px;
    height: 28px;
    line-height: 28px;
  }
}

@media (max-width: 375px) {
  .comment-manage-page {
    padding: 10px;
  }

  .comment-manage-page :deep(.ant-card-head) {
    padding: 8px 10px;
  }

  .comment-manage-page :deep(.ant-card-head-title) {
    font-size: 14px;
  }

  .comment-manage-page :deep(.ant-card-body) {
    padding: 10px;
  }

  .search-bar {
    padding: 8px;
  }

  .search-bar :deep(.ant-input) {
    height: 32px;
    font-size: 12px;
  }

  .search-bar :deep(.ant-btn) {
    height: 32px;
    font-size: 12px;
  }

  .comment-table :deep(.ant-table-thead > tr > th),
  .comment-table :deep(.ant-table-tbody > tr > td) {
    padding: 6px 4px;
  }

  .content-cell {
    max-width: 100px;
    font-size: 10px;
  }

  .comment-table :deep(.ant-btn-link) {
    padding: 1px 4px;
    font-size: 10px;
  }
}
</style>
