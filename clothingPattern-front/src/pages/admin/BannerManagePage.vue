<template>
  <div class="banner-manage-page">
    <a-card title="轮播图管理" :bordered="false">
      <!-- 操作栏 -->
      <template #extra>
        <a-button type="primary" @click="showAddModal">
          <PlusOutlined />
          添加轮播图
        </a-button>
      </template>

      <!-- 轮播图列表 -->
      <a-table
        :columns="columns"
        :data-source="bannerList"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <!-- 使用新的 bodyCell 插槽 -->
        <template #bodyCell="{ column, record }">
          <!-- ͼƬ -->
          <template v-if="column.key === 'imageUrl'">
            <a-image
              v-if="record?.imageUrl"
              :src="record.imageUrl"
              :width="120"
              :height="80"
              style="object-fit: cover"
            />
            <span v-else class="text-gray">无图片</span>
          </template>

          <!-- ״̬ -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record?.status === 1 ? 'success' : 'default'">
              {{ record?.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)"> 编辑 </a-button>
              <a-popconfirm
                title="确定要删除这个轮播图吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger> 删除 </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 轮播图详情对话框 -->
    <a-modal
      :open="bannerModalVisible"
      :title="isEditModal ? '编辑轮播图' : '添加轮播图'"
      @ok="handleOk"
      @cancel="handleCancel"
      ok-text="确定"
      cancel-text="取消"
      width="800px"
    >
      <a-form :model="form" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" placeholder="请输入轮播图标题" />
        </a-form-item>
        <a-form-item label="ͼƬ" required>
          <a-upload
            list-type="picture-card"
            :file-list="fileList"
            :before-upload="handleUpload"
            @remove="handleRemove"
            :max-count="1"
            accept="image/*"
          >
            <div v-if="fileList.length < 1">
              <plus-outlined />
              <div style="margin-top: 8px">上传图片</div>
            </div>
          </a-upload>
          <div class="upload-tip">建议尺寸：1920x400，支持jpg、png格式，大小不超过5MB</div>
        </a-form-item>
        <a-form-item label="链接地址">
          <a-input v-model:value="form.linkUrl" placeholder="请输入跳转链接" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number
            v-model:value="form.sortOrder"
            :min="0"
            placeholder="排序"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="״̬">
          <a-select v-model:value="form.status" style="width: 100%">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, PictureOutlined } from '@ant-design/icons-vue'
import {
  getBannerList,
  addBanner,
  updateBanner,
  deleteBanner,
  uploadBanner,
} from '@/api/bannerController'

// 数据
const bannerList = ref<any[]>([])
const loading = ref(false)

// 对话框
const bannerModalVisible = ref(false)
const isEditModal = ref(false)

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1,
})

// 图片上传
const fileList = ref<any[]>([])

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
  },
  {
    title: 'ͼƬ',
    dataIndex: 'imageUrl',
    key: 'imageUrl',
    width: 150,
  },
  {
    title: '链接地址',
    dataIndex: 'linkUrl',
    key: 'linkUrl',
  },
  {
    title: '排序',
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    width: 80,
  },
  {
    title: '״̬',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right' as const,
  },
]

// 加载轮播图列表
const loadBanners = async () => {
  loading.value = true
  try {
    const res = await getBannerList()
    if (res.data.code === 0 && res.data.data) {
      bannerList.value = res.data.data
    } else {
      message.error('加载轮播图失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('加载轮播图失败:', error)
    message.error('加载轮播图失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddModal = () => {
  isEditModal.value = false
  resetForm()
  bannerModalVisible.value = true
}

// 显示编辑对话框
const handleEdit = (record: any) => {
  isEditModal.value = true
  form.id = record.id
  form.title = record.title
  form.imageUrl = record.imageUrl
  form.linkUrl = record.linkUrl
  form.sortOrder = record.sortOrder
  form.status = record.status

  // 设置图片列表
  if (record.imageUrl) {
    fileList.value = [
      {
        uid: '-1',
        name: 'banner.jpg',
        status: 'done',
        url: record.imageUrl,
      },
    ]
  } else {
    fileList.value = []
  }

  bannerModalVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.title = ''
  form.imageUrl = ''
  form.linkUrl = ''
  form.sortOrder = 0
  form.status = 1
  fileList.value = []
}

// 处理图片上传
const handleUpload = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB！')
    return false
  }

  try {
    const res = await uploadBanner({}, file) as any
    if (res.data.code === 0 && res.data.data) {
      const imageUrl = res.data.data
      form.imageUrl = imageUrl
      fileList.value = [
        {
          uid: Date.now().toString(),
          name: file.name,
          status: 'done',
          url: imageUrl,
        },
      ]
      message.success('图片上传成功')
    } else {
      message.error('图片上传失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('图片上传失败:', error)
    message.error('图片上传失败：' + error.message)
  }

  return false // 阻止自动上传
}

// 处理图片移除
const handleRemove = () => {
  form.imageUrl = ''
  fileList.value = []
}

// 确定按钮
const handleOk = async () => {
  // 表单验证
  if (!form.title) {
    message.warning('请输入轮播图标题')
    return
  }
  if (!form.imageUrl) {
    message.warning('请上传轮播图图片')
    return
  }

  try {
    let res
    if (isEditModal.value) {
      // 编辑轮播图
      res = await updateBanner(form)
    } else {
      // 添加轮播图
      res = await addBanner(form)
    }

    if (res.data.code === 0) {
      message.success(isEditModal.value ? '编辑成功' : '添加成功')
      bannerModalVisible.value = false
      loadBanners()
    } else {
      message.error((isEditModal.value ? '编辑失败' : '添加失败') + '：' + res.data.message)
    }
  } catch (error: any) {
    console.error((isEditModal.value ? '编辑轮播图' : '添加轮播图') + '失败:', error)
    message.error((isEditModal.value ? '编辑失败' : '添加失败') + '：' + error.message)
  }
}

// 取消按钮
const handleCancel = () => {
  bannerModalVisible.value = false
  resetForm()
}

// 删除轮播图
const handleDelete = async (id: number) => {
  try {
    const res = await deleteBanner({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      loadBanners()
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('删除轮播图失败:', error)
    message.error('删除失败：' + error.message)
  }
}

// 初始化
onMounted(() => {
  loadBanners()
})
</script>

<style scoped lang="scss">
.banner-manage-page {
  padding: 24px;

  .text-gray {
    color: #999;
  }

  :deep(.ant-table) {
    .ant-table-cell {
      vertical-align: middle;
    }
  }

  .upload-tip {
    margin-top: 8px;
    color: #999;
    font-size: 12px;
  }
}
</style>
