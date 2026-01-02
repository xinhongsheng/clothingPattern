<template>
  <div id="userManagePage">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
    </div>

    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="账号" class="form-item">
          <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
        </a-form-item>
        <a-form-item label="用户名" class="form-item">
          <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
        </a-form-item>
        <a-form-item class="form-item">
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="table-card" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        :scroll="{ x: 800 }"
        @change="doTableChange"
        class="user-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-avatar :src="record.userAvatar" :size="40">
              {{ record.userName?.charAt(0) }}
            </a-avatar>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <a-tag v-if="record.userRole === 'admin'" color="green">管理员</a-tag>
            <a-tag v-else color="blue">普通用户</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="primary" danger size="small" @click="doDelete(record.id)">删除</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { listUserVoByPage , deleteUser } from '@/api/userController.ts'


// 定义表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 80,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    width: 120,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    width: 100,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    width: 150,
    ellipsis: true,
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
  },
]

// 数据
const dataList = ref([])
const total = ref(0)

// 搜索条件
const searchParams = reactive({
  current: 1,
  pageSize: 10,
  userAccount: '',
  userName: '',
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索处理
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f1a15;
  margin: 0;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(31, 26, 21, 0.08);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.form-item {
  margin-bottom: 0 !important;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(31, 26, 21, 0.08);
  overflow: hidden;
}

.user-table :deep(.ant-table) {
  font-size: 14px;
}

.user-table :deep(.ant-table-thead > tr > th) {
  background: #faf8f5;
  font-weight: 600;
  color: #1f1a15;
}

.user-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #fffbf7;
}

/* 响应式适配 */
@media (max-width: 992px) {
  #userManagePage {
    padding: 20px;
  }

  .page-title {
    font-size: 22px;
  }
}

@media (max-width: 768px) {
  #userManagePage {
    padding: 16px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 12px;
  }

  .search-card {
    margin-bottom: 12px;
    border-radius: 10px;
  }

  .search-form {
    flex-direction: column;
    gap: 10px;
  }

  .form-item {
    width: 100%;
  }

  .form-item :deep(.ant-form-item-label) {
    width: 60px;
  }

  .form-item :deep(.ant-input) {
    width: 100%;
  }

  .form-item :deep(.ant-btn) {
    width: 100%;
  }

  .table-card {
    border-radius: 10px;
  }

  .user-table :deep(.ant-table) {
    font-size: 13px;
  }

  .user-table :deep(.ant-table-thead > tr > th) {
    padding: 10px 8px;
    font-size: 12px;
  }

  .user-table :deep(.ant-table-tbody > tr > td) {
    padding: 10px 8px;
  }

  .user-table :deep(.ant-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }

  .user-table :deep(.ant-pagination-total-text) {
    width: 100%;
    text-align: center;
    margin-bottom: 8px;
  }
}

@media (max-width: 576px) {
  #userManagePage {
    padding: 12px;
  }

  .page-header {
    margin-bottom: 14px;
  }

  .page-title {
    font-size: 18px;
  }

  .search-card {
    padding: 12px;
    border-radius: 8px;
  }

  .search-card :deep(.ant-card-body) {
    padding: 12px;
  }

  .search-form {
    gap: 8px;
  }

  .form-item :deep(.ant-form-item-label) {
    font-size: 13px;
  }

  .form-item :deep(.ant-input) {
    height: 36px;
    font-size: 13px;
  }

  .form-item :deep(.ant-btn) {
    height: 36px;
    font-size: 13px;
  }

  .table-card {
    border-radius: 8px;
  }

  .table-card :deep(.ant-card-body) {
    padding: 12px;
  }

  .user-table :deep(.ant-table) {
    font-size: 12px;
  }

  .user-table :deep(.ant-table-thead > tr > th) {
    padding: 8px 6px;
    font-size: 11px;
  }

  .user-table :deep(.ant-table-tbody > tr > td) {
    padding: 8px 6px;
  }

  .user-table :deep(.ant-avatar) {
    width: 32px !important;
    height: 32px !important;
    font-size: 12px !important;
  }

  .user-table :deep(.ant-tag) {
    font-size: 10px;
    padding: 0 6px;
  }

  .user-table :deep(.ant-btn-sm) {
    font-size: 11px;
    padding: 2px 8px;
    height: 26px;
  }

  .user-table :deep(.ant-pagination) {
    font-size: 12px;
  }

  .user-table :deep(.ant-pagination-item),
  .user-table :deep(.ant-pagination-prev),
  .user-table :deep(.ant-pagination-next) {
    min-width: 28px;
    height: 28px;
    line-height: 28px;
  }
}

@media (max-width: 375px) {
  #userManagePage {
    padding: 10px;
  }

  .page-title {
    font-size: 16px;
  }

  .search-card :deep(.ant-card-body),
  .table-card :deep(.ant-card-body) {
    padding: 10px;
  }

  .user-table :deep(.ant-table-thead > tr > th),
  .user-table :deep(.ant-table-tbody > tr > td) {
    padding: 6px 4px;
  }

  .user-table :deep(.ant-avatar) {
    width: 28px !important;
    height: 28px !important;
  }

  .user-table :deep(.ant-btn-sm) {
    font-size: 10px;
    padding: 1px 6px;
    height: 24px;
  }
}
</style>
