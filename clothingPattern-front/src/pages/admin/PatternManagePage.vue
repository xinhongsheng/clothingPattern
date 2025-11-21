<template>
  <div id="patternManagePage">
    <!-- 页面标题和操作区 -->
    <a-card :bordered="false" class="header-card">
      <a-flex justify="space-between" align="center">
        <div>
          <h2 class="page-title">
            <AppstoreOutlined /> 服装图案管理
          </h2>
          <p class="page-desc">管理用户生成的服装图案，进行审核和维护</p>
        </div>
        <a-space>
          <a-badge :count="pendingCount" :offset="[10, 0]">
            <a-button type="primary" @click="filterByStatus('PENDING')">
              <AuditOutlined /> 待审核图案
            </a-button>
          </a-badge>
          <a-button @click="doSearch">
            <ReloadOutlined /> 刷新
          </a-button>
        </a-space>
      </a-flex>
    </a-card>

    <!-- 搜索区域 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="图案名称" name="patternName">
          <a-input
            v-model:value="searchParams.patternName"
            placeholder="请输入图案名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="生成方式" name="generationType">
          <a-select
            v-model:value="searchParams.generationType"
            placeholder="请选择生成方式"
            style="width: 180px"
            allow-clear
          >
            <a-select-option value="TEXT_GENERATED">文字生成</a-select-option>
            <a-select-option value="IMAGE_REFERENCED">图片参考生成</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="审核状态" name="auditStatus">
          <a-select
            v-model:value="searchParams.auditStatus"
            :options="AUDIT_STATUS_OPTIONS"
            placeholder="请选择审核状态"
            style="width: 150px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="用户ID" name="userId">
          <a-input-number
            v-model:value="searchParams.userId"
            placeholder="请输入用户ID"
            style="width: 150px"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit">
            <SearchOutlined /> 搜索
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button @click="resetSearch">
            <ClearOutlined /> 重置
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 数据表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        :loading="loading"
        @change="doTableChange"
        :scroll="{ x: 1500 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 图案预览 -->
          <template v-if="column.dataIndex === 'patternUrl'">
            <div class="pattern-preview">
              <a-image
                :src="record.thumbUrl || record.patternUrl"
                :preview="{ src: record.patternUrl }"
                :width="100"
                :height="100"
                :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjEyIiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5aSx6LSlPC90ZXh0Pjwvc3ZnPg=='"
                style="object-fit: cover; border-radius: 8px;"
              />
            </div>
          </template>

          <!-- 图案信息 -->
          <template v-if="column.dataIndex === 'patternInfo'">
            <div class="pattern-info">
              <div><strong>{{ record.patternName }}</strong></div>
              <div class="info-item">
                <FileTextOutlined /> {{ record.description || '暂无描述' }}
              </div>
              <div class="info-item" v-if="record.style">
                <TagOutlined /> 风格：{{ record.style }}
              </div>
            </div>
          </template>

          <!-- 生成方式 -->
          <template v-if="column.dataIndex === 'generationType'">
            <a-tag :color="GENERATION_TYPE_COLOR_MAP[record.generationType]">
              {{ GENERATION_TYPE_MAP[record.generationType] }}
            </a-tag>
          </template>

          <!-- 文件信息 -->
          <template v-if="column.dataIndex === 'fileInfo'">
            <div class="file-info">
              <div>格式：{{ record.fileType || 'PNG' }}</div>
              <div>大小：{{ formatFileSize(record.fileSize) }}</div>
            </div>
          </template>

          <!-- 审核状态 -->
          <template v-if="column.dataIndex === 'auditStatus'">
            <a-tag :color="AUDIT_STATUS_COLOR_MAP[record.auditStatus]">
              {{ AUDIT_STATUS_MAP[record.auditStatus] }}
            </a-tag>
            <div v-if="record.rejectReason" class="reject-reason">
              拒绝原因：{{ record.rejectReason }}
            </div>
          </template>

          <!-- 创建时间 -->
          <template v-if="column.dataIndex === 'createTime'">
            <div>{{ formatDateTime(record.createTime) }}</div>
          </template>

          <!-- 审核时间 -->
          <template v-if="column.dataIndex === 'auditTime'">
            <div>{{ record.auditTime ? formatDateTime(record.auditTime) : '-' }}</div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space direction="vertical" size="small">
              <a-space wrap>
                <a-button
                  v-if="record.auditStatus === AUDIT_STATUS_ENUM.PENDING"
                  type="primary"
                  size="small"
                  @click="handleAudit(record, AUDIT_STATUS_ENUM.APPROVED)"
                >
                  <CheckOutlined /> 通过
                </a-button>
                <a-button
                  v-if="record.auditStatus === AUDIT_STATUS_ENUM.PENDING"
                  danger
                  size="small"
                  @click="showRejectModal(record)"
                >
                  <CloseOutlined /> 拒绝
                </a-button>
                <a-button
                  v-if="record.auditStatus === AUDIT_STATUS_ENUM.REJECTED"
                  type="primary"
                  size="small"
                  ghost
                  @click="handleAudit(record, AUDIT_STATUS_ENUM.APPROVED)"
                >
                  <CheckOutlined /> 重新通过
                </a-button>
              </a-space>
              <a-space wrap>
                <a-button size="small" @click="viewDetail(record)">
                  <EyeOutlined /> 详情
                </a-button>
                <a-button size="small" danger @click="doDelete(record.id)">
                  <DeleteOutlined /> 删除
                </a-button>
              </a-space>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 拒绝原因弹窗 -->
    <a-modal
      v-model:open="rejectModalVisible"
      title="拒绝审核"
      @ok="handleRejectConfirm"
      @cancel="rejectModalVisible = false"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item label="拒绝原因" required>
          <a-textarea
            v-model:value="rejectForm.rejectReason"
            placeholder="请输入拒绝原因"
            :rows="4"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="图案详情"
      :footer="null"
      width="800px"
    >
      <div v-if="currentPattern" class="pattern-detail">
        <a-row :gutter="24">
          <a-col :span="12">
            <div class="detail-image">
              <a-image
                :src="currentPattern.patternUrl"
                :width="'100%'"
                style="border-radius: 8px;"
              />
            </div>
          </a-col>
          <a-col :span="12">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="图案名称">
                {{ currentPattern.patternName }}
              </a-descriptions-item>
              <a-descriptions-item label="描述">
                {{ currentPattern.description || '暂无' }}
              </a-descriptions-item>
              <a-descriptions-item label="生成方式">
                {{ GENERATION_TYPE_MAP[currentPattern.generationType] }}
              </a-descriptions-item>
              <a-descriptions-item label="风格" v-if="currentPattern.style">
                {{ currentPattern.style }}
              </a-descriptions-item>
              <a-descriptions-item label="季节" v-if="currentPattern.season">
                {{ currentPattern.season }}
              </a-descriptions-item>
              <a-descriptions-item label="目标受众" v-if="currentPattern.targetAudience">
                {{ currentPattern.targetAudience }}
              </a-descriptions-item>
              <a-descriptions-item label="文件大小">
                {{ formatFileSize(currentPattern.fileSize) }}
              </a-descriptions-item>
              <a-descriptions-item label="审核状态">
                <a-tag :color="AUDIT_STATUS_COLOR_MAP[currentPattern.auditStatus]">
                  {{ AUDIT_STATUS_MAP[currentPattern.auditStatus] }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ formatDateTime(currentPattern.createTime) }}
              </a-descriptions-item>
              <a-descriptions-item label="用户信息" v-if="currentPattern.user">
                {{ currentPattern.user.userName }} (ID: {{ currentPattern.user.id }})
              </a-descriptions-item>
            </a-descriptions>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined,
  AuditOutlined,
  ReloadOutlined,
  SearchOutlined,
  ClearOutlined,
  FileTextOutlined,
  TagOutlined,
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  listPatternVoByPage,
  deletePattern,
  auditPattern
} from '@/api/patternController'
import {
  AUDIT_STATUS_ENUM,
  AUDIT_STATUS_MAP,
  AUDIT_STATUS_OPTIONS,
  AUDIT_STATUS_COLOR_MAP,
  GENERATION_TYPE_ENUM,
  GENERATION_TYPE_MAP,
  GENERATION_TYPE_OPTIONS,
  GENERATION_TYPE_COLOR_MAP
} from '@/constants/pattern'

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
    fixed: 'left'
  },
  {
    title: '图案预览',
    dataIndex: 'patternUrl',
    width: 120
  },
  {
    title: '图案信息',
    dataIndex: 'patternInfo',
    width: 250,
    ellipsis: true
  },
  {
    title: '生成方式',
    dataIndex: 'generationType',
    width: 120
  },
  {
    title: '文件信息',
    dataIndex: 'fileInfo',
    width: 120
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    width: 120
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180
  },
  {
    title: '审核时间',
    dataIndex: 'auditTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right'
  }
]

// 数据
const dataList = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const pendingCount = ref(0)

// 搜索条件
const searchParams = reactive<any>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
    pageSizeOptions: ['10', '20', '50', '100']
  }
})

// 拒绝弹窗
const rejectModalVisible = ref(false)
const rejectForm = reactive({
  patternId: 0,
  rejectReason: ''
})

// 详情弹窗
const detailModalVisible = ref(false)
const currentPattern = ref<any>(null)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listPatternVoByPage(searchParams)
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
      
      // Debug: 打印第一条数据的图片URL
      if (dataList.value.length > 0) {
        console.log('First pattern URLs:', {
          patternUrl: dataList.value[0].patternUrl,
          thumbUrl: dataList.value[0].thumbUrl
        })
      }
      
      // 计算待审核数量
      pendingCount.value = dataList.value.filter(
        (item: any) => item.auditStatus === AUDIT_STATUS_ENUM.PENDING
      ).length
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const doSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchParams.patternName = undefined
  searchParams.generationType = undefined
  searchParams.auditStatus = undefined
  searchParams.userId = undefined
  doSearch()
}

// 按状态筛选
const filterByStatus = (status: string) => {
  searchParams.auditStatus = status
  doSearch()
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 审核通过
const handleAudit = async (record: any, auditStatus: string) => {
  try {
    const res = await auditPattern({
      id: record.id,
      auditStatus,
      rejectReason: ''
    })
    if (res.data.code === 0) {
      message.success('审核操作成功')
      fetchData()
    } else {
      message.error('审核操作失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('审核操作失败：' + error.message)
  }
}

// 显示拒绝弹窗
const showRejectModal = (record: any) => {
  rejectForm.patternId = record.id
  rejectForm.rejectReason = ''
  rejectModalVisible.value = true
}

// 确认拒绝
const handleRejectConfirm = async () => {
  if (!rejectForm.rejectReason) {
    message.warning('请输入拒绝原因')
    return
  }

  try {
    const res = await auditPattern({
      id: rejectForm.patternId,
      auditStatus: AUDIT_STATUS_ENUM.REJECTED,
      rejectReason: rejectForm.rejectReason
    })
    if (res.data.code === 0) {
      message.success('审核拒绝成功')
      rejectModalVisible.value = false
      fetchData()
    } else {
      message.error('审核操作失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('审核操作失败：' + error.message)
  }
}

// 查看详情
const viewDetail = (record: any) => {
  currentPattern.value = record
  detailModalVisible.value = true
}

// 删除
const doDelete = async (id: number) => {
  if (!id) {
    return
  }

  try {
    const res = await deletePattern({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchData()
    } else {
      message.error('删除失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('删除失败：' + error.message)
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 格式化日期时间
const formatDateTime = (date: Date | string) => {
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

// 页面加载时请求数据
onMounted(() => {
  fetchData()
})
</script>

<style  scoped>
#patternManagePage {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .header-card {
    margin-bottom: 16px;
    border-radius: 8px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      margin: 0;
      color: #1f1f1f;
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .page-desc {
      margin: 8px 0 0 0;
      color: #8c8c8c;
      font-size: 14px;
    }
  }

  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;

    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }
  }

  .table-card {
    border-radius: 8px;

    .pattern-preview {
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .pattern-info {
      .info-item {
        margin-top: 4px;
        font-size: 12px;
        color: #8c8c8c;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .file-info {
      font-size: 12px;
      color: #595959;
      line-height: 1.8;
    }

    .reject-reason {
      margin-top: 4px;
      font-size: 12px;
      color: #ff4d4f;
    }
  }

  .pattern-detail {
    .detail-image {
      margin-bottom: 16px;
    }

    :deep(.ant-descriptions-item-label) {
      font-weight: 500;
    }
  }
}

@media (max-width: 768px) {
  #patternManagePage {
    padding: 12px;

    .page-title {
      font-size: 20px;
    }

    :deep(.ant-form-inline .ant-form-item) {
      display: block;
      margin-right: 0;
    }
  }
}
</style>
