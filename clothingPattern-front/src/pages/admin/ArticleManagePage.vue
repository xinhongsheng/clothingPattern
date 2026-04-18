<template>
  <div class="article-manage-page">
    <a-card title="文章管理" :bordered="false">
      <!-- 操作栏 -->
      <template #extra>
        <a-space>
          <a-button @click="showCategoryModal">
            <AppstoreOutlined />
            分类管理
          </a-button>
          <a-button @click="handleBannerManage">
            <PictureOutlined />
            轮播图管理
          </a-button>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined />
            新建文章
          </a-button>
        </a-space>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-form layout="inline">
          <a-form-item label="关键词">
            <a-input
              v-model:value="searchParams.keyword"
              placeholder="搜索标题或内容"
              style="width: 200px"
              allow-clear
            />
          </a-form-item>
          <a-form-item label="分类">
            <a-select
              v-model:value="searchParams.categoryId"
              placeholder="选择分类"
              style="width: 150px"
              allow-clear
            >
              <a-select-option :value="undefined">全部</a-select-option>
              <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.categoryName }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态">
            <a-select
              v-model:value="searchParams.status"
              placeholder="选择状态"
              style="width: 120px"
              allow-clear
            >
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="DRAFT">草稿</a-select-option>
              <a-select-option value="PUBLISHED">已发布</a-select-option>
              <a-select-option value="OFFLINE">已下架</a-select-option>
            </a-select>
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

      <!-- 文章列表 -->
      <a-table
        :columns="columns"
        :data-source="articleList"
        :loading="loading"
        :pagination="pagination"
        @change="doTableChange"
        row-key="id"
        :scroll="{ x: 1200 }"
        class="article-table"
      >
        <!-- 使用新的 bodyCell 插槽 -->
        <template #bodyCell="{ column, record }">
          <!-- 封面图 -->
          <template v-if="column.key === 'coverImage'">
            <a-image
              v-if="record?.coverImage"
              :src="record.coverImage"
              :width="80"
              :height="60"
              style="object-fit: cover"
            />
            <span v-else class="text-gray">无封面</span>
          </template>

          <!-- 标题 -->
          <template v-else-if="column.key === 'title'">
            <div class="title-cell">
              <a-tag v-if="record?.isTop === 1" color="red" size="small">置顶</a-tag>
              <a-tag v-if="record?.isHot === 1" color="orange" size="small">热门</a-tag>
              <a-tag v-if="record?.isRecommend === 1" color="blue" size="small">推荐</a-tag>
              <div class="title-text">{{ record?.title }}</div>
            </div>
          </template>

          <!-- 分类 -->
          <template v-else-if="column.key === 'category'">
            <a-tag color="blue">{{ record?.categoryName }}</a-tag>
          </template>

          <!--״状态“ -->
          <template v-else-if="column.key === 'status'">
            <a-tag v-if="record?.status === 'DRAFT'" color="default">草稿</a-tag>
            <a-tag v-else-if="record?.status === 'PUBLISHED'" color="success">已发布</a-tag>
            <a-tag v-else-if="record?.status === 'OFFLINE'" color="warning">已下架</a-tag>
          </template>

          <!-- 审核状态 -->
          <template v-else-if="column.key === 'auditStatus'">
            <a-tag v-if="record?.auditStatus === 'PENDING'" color="processing">待审核</a-tag>
            <a-tag v-else-if="record?.auditStatus === 'APPROVED'" color="success">已通过</a-tag>
            <a-tag v-else-if="record?.auditStatus === 'REJECTED'" color="error">已拒绝</a-tag>
          </template>

          <!-- 统计数据 -->
          <template v-else-if="column.key === 'stats'">
            <a-space direction="vertical" :size="4">
              <span><EyeOutlined /> {{ record?.viewCount || 0 }}</span>
              <span><LikeOutlined /> {{ record?.likeCount || 0 }}</span>
              <span><StarOutlined /> {{ record?.collectCount || 0 }}</span>
            </a-space>
          </template>

          <!-- 发布时间 -->
          <template v-else-if="column.key === 'publishTime'">
            {{ record?.publishTime ? dayjs(record.publishTime).format('YYYY-MM-DD HH:mm') : '-' }}
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record?.id)"> 查看 </a-button>
              <a-button type="link" size="small" @click="handleEdit(record?.id)"> 编辑 </a-button>
              <a-button
                v-if="record?.status === 'DRAFT'"
                type="link"
                size="small"
                @click="handlePublish(record?.id)"
              >
                发布
              </a-button>
              <a-button
                v-if="record?.status === 'PUBLISHED'"
                type="link"
                size="small"
                @click="handleOffline(record?.id)"
              >
                下架
              </a-button>
              <a-button
                v-if="record?.status === 'OFFLINE'"
                type="link"
                size="small"
                @click="handleListed(record?.id)"
              >
                上架
              </a-button>
              <a-popconfirm
                title="确定要删除这篇文章吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record?.id)"
              >
                <a-button type="link" size="small" danger> 删除 </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 分类管理对话框 -->
    <a-modal
      :open="categoryModalVisible"
      title="分类管理"
      :width="categoryModalWidth"
      @cancel="categoryModalVisible = false"
      :footer="null"
      class="category-modal"
    >
      <div class="category-manage">
        <!-- 新增分类表单 -->
        <a-card title="新增分类" size="small" class="mb-16">
          <a-form layout="vertical">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item label="分类名称">
                  <a-input v-model:value="newCategory.categoryName" placeholder="请输入分类名称" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="描述">
                  <a-input v-model:value="newCategory.categoryDesc" placeholder="请输入描述" />
                </a-form-item>
              </a-col>
              <a-col :span="4">
                <a-form-item label="排序">
                  <a-input-number
                    v-model:value="newCategory.sortOrder"
                    :min="0"
                    placeholder="排序"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item label="操作">
                  <a-button type="primary" @click="handleAddCategory" block>
                    <PlusOutlined />
                    添加分类
                  </a-button>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row>
              <a-col :span="24">
                <a-form-item label="分类图标">
                  <a-upload
                    list-type="picture-card"
                    :file-list="newCategoryFileList"
                    :before-upload="handleNewCategoryUpload"
                    @remove="handleNewCategoryRemove"
                    :max-count="1"
                    accept="image/*"
                  >
                    <div v-if="newCategoryFileList.length < 1">
                      <plus-outlined />
                      <div style="margin-top: 8px">上传图标</div>
                    </div>
                  </a-upload>
                  <div class="upload-tip">建议尺寸：200x200，支持jpg、png格式，大小不超过1MB</div>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>

        <!-- 分类列表 -->
        <a-table
          :columns="categoryColumns"
          :data-source="categories"
          :loading="categoryLoading"
          :pagination="false"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <!-- 图标 -->
            <template v-if="column.key === 'icon'">
              <a-image
                v-if="record.icon"
                :src="record.icon"
                :width="40"
                :height="40"
                style="object-fit: cover; border-radius: 4px"
              />
              <span v-else class="text-gray">-</span>
            </template>

            <!-- 状态 -->
            <template v-else-if="column.key === 'status'">
              <a-tag :color="record.status === 1 ? 'success' : 'default'">
                {{ record.status === 1 ? '启用' : '禁用' }}
              </a-tag>
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="handleEditCategory(record)">
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除这个分类吗?"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteCategory(record.id)"
                >
                  <a-button type="link" size="small" danger> 删除 </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>

    <!-- 编辑分类对话框 -->
    <a-modal
      :open="editCategoryModalVisible"
      title="编辑分类"
      @ok="handleUpdateCategory"
      @cancel="editCategoryModalVisible = false"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="分类名称">
          <a-input v-model:value="editingCategory.categoryName" />
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="editingCategory.categoryDesc" />
        </a-form-item>
        <a-form-item label="分类图标">
          <a-upload
            list-type="picture-card"
            :file-list="editCategoryFileList"
            :before-upload="handleEditCategoryUpload"
            @remove="handleEditCategoryRemove"
            :max-count="1"
            accept="image/*"
          >
            <div v-if="editCategoryFileList.length < 1">
              <plus-outlined />
              <div style="margin-top: 8px">上传图标</div>
            </div>
          </a-upload>
          <div class="upload-tip">建议尺寸：200x200，支持jpg、png格式，大小不超过1MB</div>
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="editingCategory.sortOrder" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="editingCategory.status" style="width: 100%">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted,computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  LikeOutlined,
  StarOutlined,
  AppstoreOutlined,
  PictureOutlined,
} from '@ant-design/icons-vue'
import {
  getArticleList,
  deleteArticle,
  publishArticle,
  offlineArticle,
  listedArticle,
} from '@/api/articleController'
import {
  getCategories,
  addCategory,
  updateCategory,
  deleteCategory,
} from '@/api/articleCategoryController'
import dayjs from 'dayjs'

const router = useRouter()

// 数据
const articleList = ref<API.ArticleVO[]>([])
const categories = ref<API.ArticleCategoryVO[]>([])
const loading = ref(false)

// 分类管理相关
const categoryModalVisible = ref(false)
const editCategoryModalVisible = ref(false)
const categoryLoading = ref(false)
const categoryModalWidth = computed(() => {
  if (typeof window !== 'undefined' && window.innerWidth < 576) {
    return '95%'
  }
  return '800px'
})
const newCategory = reactive({
  categoryName: '',
  categoryDesc: '',
  icon: '',
  sortOrder: 0,
})
const editingCategory = reactive({
  id: undefined as string | undefined,
  categoryName: '',
  categoryDesc: '',
  icon: '',
  sortOrder: 0,
  status: 1,
})

// 图片上传相关
const newCategoryFileList = ref<any[]>([])
const editCategoryFileList = ref<any[]>([])

// 搜索参数
const searchParams = reactive({
  keyword: '',
  categoryId: undefined as string | undefined,
  status: '',
  current: 1,
  pageSize: 10,
})

// 分页参数
const total = ref(0)
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 分类表格列定义
const categoryColumns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '分类名称',
    dataIndex: 'categoryName',
    key: 'categoryName',
  },
  {
    title: '描述',
    dataIndex: 'categoryDesc',
    key: 'categoryDesc',
  },
  {
    title: '图标',
    dataIndex: 'icon',
    key: 'icon',
    width: 100,
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
  },
]

// 表格列定义
const columns = [
  {
    title: '封面',
    dataIndex: 'coverImage',
    key: 'coverImage',
    width: 100,
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 300,
  },
  {
    title: '分类',
    dataIndex: 'categoryName',
    key: 'category',
    width: 100,
  },
  {
    title: '作者',
    dataIndex: 'author',
    key: 'author',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: '审核',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 80,
  },
  {
    title: '统计',
    key: 'stats',
    width: 80,
  },
  {
    title: '发布时间',
    dataIndex: 'publishTime',
    key: 'publishTime',
    width: 150,
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right' as const,
  },
]

// 加载分类
const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.data.code === 0 && res.data.data) {
      categories.value = res.data.data
    }
  } catch (error: any) {
    console.error('加载分类失败:', error)
  }
}

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      ...searchParams,
      pageNum: searchParams.current,
      pageSize: searchParams.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      articleList.value = res.data.data.records || []
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

// 搜索处理
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  loadArticles()
}

// 重置
const handleReset = () => {
  searchParams.keyword = ''
  searchParams.categoryId = undefined
  searchParams.status = ''
  searchParams.current = 1
  loadArticles()
}

// 表格变化处理
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  loadArticles()
}

// 新建文章
const handleAdd = () => {
  router.push('/admin/article/edit')
}

// 轮播图管理
const handleBannerManage = () => {
  router.push('/admin/banner')
}

// 查看文章
const handleView = (id: string) => {
  router.push(`/article/${id}`)
}

// 编辑文章
const handleEdit = (id: string) => {
  router.push(`/admin/article/edit/${id}`)
}

// 发布文章
const handlePublish = async (id: string) => {
  try {
    const res = await publishArticle({ id })
    if (res.data.code === 0) {
      message.success('发布成功')
      loadArticles()
    } else {
      message.error('发布失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('发布失败:', error)
    message.error('发布失败：' + error.message)
  }
}

// 下架文章
const handleOffline = async (id: string) => {
  try {
    const res = await offlineArticle({ id })
    if (res.data.code === 0) {
      message.success('下架成功')
      loadArticles()
    } else {
      message.error('下架失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('下架失败:', error)
    message.error('下架失败：' + error.message)
  }
}
// 上架文章
const handleListed = async (id: string) => {
  try {
    const res = await listedArticle({ id })
    if (res.data.code === 0) {
      message.success('上架成功')
      loadArticles()
    } else {
      message.error('上架失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('上架失败:', error)
    message.error('上架失败：' + error.message)
  }
}

// 删除文章
const handleDelete = async (id: string) => {
  try {
    const res = await deleteArticle({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      loadArticles()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('删除失败:', error)
    message.error('删除失败：' + error.message)
  }
}

// 显示分类管理对话框
const showCategoryModal = () => {
  categoryModalVisible.value = true
  loadCategories()
}

// 新增分类 - 图片上传前处理
const handleNewCategoryUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt1M = file.size / 1024 / 1024 < 5
  if (!isLt1M) {
    message.error('图片大小不能超过 5MB！')
    return false
  }

  // 转换为base64
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    newCategory.icon = reader.result as string
    newCategoryFileList.value = [
      {
        uid: Date.now().toString(),
        name: file.name,
        status: 'done',
        url: reader.result,
      },
    ]
  }

  return false // 阻止自动上传
}

// 新增分类 - 移除图片
const handleNewCategoryRemove = () => {
  newCategory.icon = ''
  newCategoryFileList.value = []
}

// 编辑分类 - 图片上传前处理
const handleEditCategoryUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt1M = file.size / 1024 / 1024 < 5
  if (!isLt1M) {
    message.error('图片大小不能超过 5MB！')
    return false
  }

  // 转换为base64
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    editingCategory.icon = reader.result as string
    editCategoryFileList.value = [
      {
        uid: Date.now().toString(),
        name: file.name,
        status: 'done',
        url: reader.result,
      },
    ]
  }

  return false // 阻止自动上传
}

// 编辑分类 - 移除图片
const handleEditCategoryRemove = () => {
  editingCategory.icon = ''
  editCategoryFileList.value = []
}

// 添加分类
const handleAddCategory = async () => {
  if (!newCategory.categoryName) {
    message.warning('请输入分类名称')
    return
  }

  try {
    const res = await addCategory({
      categoryName: newCategory.categoryName,
      categoryDesc: newCategory.categoryDesc,
      icon: newCategory.icon,
      sortOrder: newCategory.sortOrder || 0,
      status: 1,
    } as API.ArticleCategory)

    if (res.data.code === 0) {
      message.success('添加成功')
      // 重置表单
      newCategory.categoryName = ''
      newCategory.categoryDesc = ''
      newCategory.icon = ''
      newCategory.sortOrder = 0
      newCategoryFileList.value = []
      // 重新加载分类列表
      loadCategories()
    } else {
      message.error('添加失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('添加分类失败:', error)
    message.error('添加失败：' + error.message)
  }
}

// 编辑分类
const handleEditCategory = (category: API.ArticleCategory) => {
  editingCategory.id = category.id
  editingCategory.categoryName = category.categoryName || ''
  editingCategory.categoryDesc = category.categoryDesc || ''
  editingCategory.icon = category.icon || ''
  editingCategory.sortOrder = category.sortOrder || 0
  editingCategory.status = category.status || 1

  // 设置图片列表
  if (category.icon) {
    editCategoryFileList.value = [
      {
        uid: '-1',
        name: 'icon.jpg',
        status: 'done',
        url: category.icon,
      },
    ]
  } else {
    editCategoryFileList.value = []
  }

  editCategoryModalVisible.value = true
}

// 更新分类
const handleUpdateCategory = async () => {
  if (!editingCategory.categoryName) {
    message.warning('请输入分类名称')
    return
  }

  try {
    const res = await updateCategory({
      id: editingCategory.id,
      categoryName: editingCategory.categoryName,
      categoryDesc: editingCategory.categoryDesc,
      icon: editingCategory.icon,
      sortOrder: editingCategory.sortOrder,
      status: editingCategory.status,
    } as API.ArticleCategory)

    if (res.data.code === 0) {
      message.success('更新成功')
      editCategoryModalVisible.value = false
      loadCategories()
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('更新分类失败:', error)
    message.error('更新失败：' + error.message)
  }
}

// 删除分类
const handleDeleteCategory = async (id: string) => {
  try {
    const res = await deleteCategory({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      loadCategories()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('删除分类失败:', error)
    message.error('删除失败：' + error.message)
  }
}

// 初始化
onMounted(() => {
  loadCategories()
  loadArticles()
})
</script>

<style scoped lang="scss">
.article-manage-page {
  padding: 24px;
  min-height: 100vh;
  background: #f0f2f5;

  .search-bar {
    margin-bottom: 16px;
    padding: 16px;
    background: #fafafa;
    border-radius: 4px;
  }

  .title-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;

    .title-text {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .text-gray {
    color: #999;
  }

  :deep(.ant-table) {
    .ant-table-cell {
      vertical-align: middle;
    }
  }
}

.category-manage {
  .mb-16 {
    margin-bottom: 16px;
  }

  .upload-tip {
    margin-top: 8px;
    color: #999;
    font-size: 12px;
  }
}

/* 响应式适配 */
@media (max-width: 992px) {
  .article-manage-page {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .article-manage-page {
    padding: 16px;

    :deep(.ant-card-head) {
      padding: 12px 16px;
    }

    :deep(.ant-card-head-title) {
      font-size: 16px;
    }

    :deep(.ant-card-extra) {
      width: 100%;
      margin-top: 12px;
    }

    :deep(.ant-card-head-wrapper) {
      flex-direction: column;
      align-items: flex-start;
    }

    :deep(.ant-card-extra .ant-space) {
      width: 100%;
      flex-wrap: wrap;
    }

    :deep(.ant-card-extra .ant-btn) {
      flex: 1;
      min-width: 80px;
    }

    .search-bar {
      padding: 12px;

      :deep(.ant-form-inline .ant-form-item) {
        display: block;
        margin-right: 0;
        margin-bottom: 10px;
        width: 100%;
      }

      :deep(.ant-form-item-control) {
        width: 100%;
      }

      :deep(.ant-input),
      :deep(.ant-select) {
        width: 100% !important;
      }

      :deep(.ant-space) {
        width: 100%;
      }

      :deep(.ant-btn) {
        flex: 1;
      }
    }

    .article-table {
      :deep(.ant-table) {
        font-size: 13px;
      }

      :deep(.ant-table-thead > tr > th) {
        padding: 10px 8px;
        font-size: 12px;
      }

      :deep(.ant-table-tbody > tr > td) {
        padding: 10px 8px;
      }

      :deep(.ant-image) {
        width: 60px !important;
        height: 45px !important;
      }

      :deep(.ant-tag) {
        font-size: 10px;
        padding: 0 4px;
        margin: 2px;
      }

      :deep(.ant-btn-link) {
        padding: 2px 4px;
        font-size: 12px;
      }
    }

    :deep(.ant-pagination) {
      flex-wrap: wrap;
      justify-content: center;
      gap: 8px;
    }

    :deep(.ant-pagination-total-text) {
      width: 100%;
      text-align: center;
    }
  }

  .category-manage {
    :deep(.ant-row) {
      flex-direction: column;
    }

    :deep(.ant-col) {
      max-width: 100% !important;
      flex: 0 0 100% !important;
    }

    :deep(.ant-form-item) {
      margin-bottom: 12px;
    }
  }
}

@media (max-width: 576px) {
  .article-manage-page {
    padding: 12px;

    :deep(.ant-card) {
      border-radius: 8px;
    }

    :deep(.ant-card-body) {
      padding: 12px;
    }

    :deep(.ant-card-head) {
      padding: 10px 12px;
      min-height: auto;
    }

    :deep(.ant-card-head-title) {
      font-size: 15px;
    }

    :deep(.ant-card-extra .ant-btn) {
      height: 34px;
      font-size: 12px;
      padding: 0 10px;
    }

    .search-bar {
      padding: 10px;
      border-radius: 6px;

      :deep(.ant-form-item-label) {
        font-size: 13px;
      }

      :deep(.ant-input),
      :deep(.ant-select-selector) {
        height: 34px !important;
        font-size: 13px;
      }

      :deep(.ant-btn) {
        height: 34px;
        font-size: 13px;
      }
    }

    .article-table {
      :deep(.ant-table) {
        font-size: 12px;
      }

      :deep(.ant-table-thead > tr > th) {
        padding: 8px 6px;
        font-size: 11px;
      }

      :deep(.ant-table-tbody > tr > td) {
        padding: 8px 6px;
      }

      :deep(.ant-image) {
        width: 50px !important;
        height: 38px !important;
      }

      .title-cell {
        gap: 4px;

        .title-text {
          font-size: 12px;
        }
      }

      :deep(.ant-tag) {
        font-size: 9px;
        padding: 0 3px;
        line-height: 16px;
      }

      :deep(.ant-space-vertical) {
        gap: 2px !important;

        span {
          font-size: 11px;
        }
      }

      :deep(.ant-btn-link) {
        padding: 1px 3px;
        font-size: 11px;
        height: auto;
      }
    }

    :deep(.ant-pagination-item),
    :deep(.ant-pagination-prev),
    :deep(.ant-pagination-next) {
      min-width: 28px;
      height: 28px;
      line-height: 28px;
    }
  }

  .category-manage {
    :deep(.ant-card) {
      margin-bottom: 12px;
    }

    :deep(.ant-card-head) {
      padding: 8px 12px;
      min-height: auto;
    }

    :deep(.ant-card-head-title) {
      font-size: 14px;
    }

    :deep(.ant-card-body) {
      padding: 12px;
    }

    :deep(.ant-input),
    :deep(.ant-input-number),
    :deep(.ant-select-selector) {
      height: 34px !important;
      font-size: 13px;
    }

    :deep(.ant-btn) {
      height: 34px;
      font-size: 13px;
    }

    :deep(.ant-table) {
      font-size: 12px;
    }

    :deep(.ant-table-thead > tr > th),
    :deep(.ant-table-tbody > tr > td) {
      padding: 8px 6px;
      font-size: 11px;
    }

    :deep(.ant-image) {
      width: 30px !important;
      height: 30px !important;
    }

    :deep(.ant-btn-link) {
      padding: 1px 4px;
      font-size: 11px;
    }

    .upload-tip {
      font-size: 11px;
    }
  }

  :deep(.category-modal .ant-modal-body) {
    padding: 12px;
  }

  :deep(.ant-modal-content) {
    border-radius: 10px;
  }

  :deep(.ant-modal-header) {
    padding: 12px 16px;
  }

  :deep(.ant-modal-title) {
    font-size: 16px;
  }
}

@media (max-width: 375px) {
  .article-manage-page {
    padding: 10px;

    :deep(.ant-card-head) {
      padding: 8px 10px;
    }

    :deep(.ant-card-head-title) {
      font-size: 14px;
    }

    :deep(.ant-card-body) {
      padding: 10px;
    }

    :deep(.ant-card-extra .ant-btn) {
      height: 32px;
      font-size: 11px;
      padding: 0 8px;
    }

    .search-bar {
      padding: 8px;

      :deep(.ant-input),
      :deep(.ant-select-selector) {
        height: 32px !important;
        font-size: 12px;
      }

      :deep(.ant-btn) {
        height: 32px;
        font-size: 12px;
      }
    }

    .article-table {
      :deep(.ant-table-thead > tr > th),
      :deep(.ant-table-tbody > tr > td) {
        padding: 6px 4px;
      }

      :deep(.ant-image) {
        width: 40px !important;
        height: 30px !important;
      }

      :deep(.ant-tag) {
        font-size: 8px;
        padding: 0 2px;
      }

      :deep(.ant-btn-link) {
        padding: 0 2px;
        font-size: 10px;
      }
    }
  }

  .category-manage {
    :deep(.ant-input),
    :deep(.ant-input-number),
    :deep(.ant-select-selector) {
      height: 32px !important;
      font-size: 12px;
    }

    :deep(.ant-btn) {
      height: 32px;
      font-size: 12px;
    }

    :deep(.ant-table-thead > tr > th),
    :deep(.ant-table-tbody > tr > td) {
      padding: 6px 4px;
      font-size: 10px;
    }
  }
}
</style>
