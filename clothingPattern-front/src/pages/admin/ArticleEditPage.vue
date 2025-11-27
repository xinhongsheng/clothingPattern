<template>
  <div class="article-edit-page">
    <a-card :title="isEdit ? '编辑文章' : '新建文章'" :bordered="false">
      <template #extra>
        <a-space>
          <a-button @click="handleBack">返回</a-button>
          <a-button @click="handleSaveDraft" :loading="saving">保存草稿</a-button>
          <a-button type="primary" @click="handlePublish" :loading="publishing">
            {{ isEdit ? '更新并发布' : '发布文章' }}
          </a-button>
        </a-space>
      </template>

      <a-form :model="formData" :label-col="{ span: 2 }" :wrapper-col="{ span: 22 }">
        <!-- 标题 -->
        <a-form-item label="文章标题" required>
          <a-input
            v-model:value="formData.title"
            placeholder="请输入文章标题"
            :maxlength="200"
            show-count
          />
        </a-form-item>

        <!-- 分类 -->
        <a-form-item label="文章分类" required>
          <a-select
            v-model:value="formData.categoryId"
            placeholder="请选择分类"
            style="width: 300px"
          >
            <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.categoryName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 封面图 -->
        <a-form-item label="封面图">
          <a-upload
            list-type="picture-card"
            :file-list="fileList"
            :before-upload="beforeUpload"
            @preview="handlePreview"
            @remove="handleRemove"
            :max-count="1"
            :disabled="uploading"
          >
            <div v-if="fileList.length < 1">
              <loading-outlined v-if="uploading" />
              <plus-outlined v-else />
              <div style="margin-top: 8px">{{ uploading ? '上传中...' : '上传封面' }}</div>
            </div>
          </a-upload>
          <div class="upload-tip">建议尺寸：800x600，支持jpg、png格式，大小不超过2MB</div>
        </a-form-item>

        <!-- 摘要 -->
        <a-form-item label="文章摘要" required>
          <a-textarea
            v-model:value="formData.summary"
            placeholder="请输入文章摘要"
            :rows="3"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <!-- 内容 -->
        <a-form-item label="文章内容" required>
          <a-textarea
            v-model:value="formData.content"
            placeholder="请输入文章内容（支持HTML）"
            :rows="15"
            style="font-family: monospace"
          />
          <div class="content-tip">
            提示：支持HTML标签，如 &lt;h2&gt;、&lt;p&gt;、&lt;strong&gt;、&lt;ul&gt;、&lt;li&gt; 等
          </div>
        </a-form-item>

        <!-- 作者 -->
        <a-form-item label="作者">
          <a-input
            v-model:value="formData.author"
            placeholder="请输入作者名称"
            style="width: 300px"
          />
        </a-form-item>

        <!-- 来源 -->
        <a-form-item label="来源">
          <a-input
            v-model:value="formData.source"
            placeholder="请输入文章来源"
            style="width: 300px"
          />
        </a-form-item>

        <!-- 标签 -->
        <a-form-item label="标签">
          <a-select
            v-model:value="formData.tags"
            mode="multiple"
            placeholder="请选择标签（可多选）"
            style="width: 500px"
            :options="presetTags.map((tag) => ({ label: tag, value: tag }))"
            allow-create
            :filter-option="filterOption"
            :get-popup-container="(triggerNode) => triggerNode.parentNode"
            @search="handleSearch"
            @blur="handleSelectBlur"
            @change="handleTagsChange"
          />
          <div style="margin-top: 8px; color: #999; font-size: 12px">
            提示：可从预设标签中选择，也可直接输入新标签
          </div>
        </a-form-item>

        <!-- 文章属性 -->
        <a-form-item label="文章属性">
          <a-space>
            <a-checkbox v-model:checked="formData.isTop">置顶</a-checkbox>
            <a-checkbox v-model:checked="formData.isRecommend">推荐</a-checkbox>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 图片预览 -->
    <a-modal :open="previewVisible" :footer="null" @cancel="previewVisible = false">
      <img :src="previewImage" style="width: 100%" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import {
  getArticleDetail,
  addArticle,
  updateArticle,
  publishArticle,
  uploadCoverImage,
} from '@/api/articleController'
import { getCategories } from '@/api/articleCategoryController'
import type { UploadProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

// 是否编辑模式
const isEdit = ref(false)
const articleId = ref<string>()

// 预设标签列表
const presetTags = ref<string[]>([
  '时尚趋势',
  '色彩搭配',
  '图案设计',
  '面料选择',
  '服装设计',
  '春夏趋势',
  '秋冬趋势',
  '复古风格',
  '现代简约',
  '民族风',
  '街头潮流',
  '高端定制',
])

// 表单数据
const formData = reactive({
  title: '',
  categoryId: undefined as string | undefined,
  coverImage: '',
  summary: '',
  content: '',
  author: '',
  source: '',
  tags: [] as string[],
  isTop: false,
  isRecommend: false,
})

// 过滤选项函数，允许创建新标签
const filterOption = (input: string, option: any) => {
  return option?.label.toLowerCase().includes(input.toLowerCase())
}

// 当前输入的标签文本
const currentTagInput = ref('')

// 处理搜索事件，保存当前输入的标签文本
const handleSearch = (value: string) => {
  currentTagInput.value = value
}

// 处理标签变化事件，清空当前输入的标签文本
const handleTagsChange = (value: string[]) => {
  currentTagInput.value = ''
}

// 处理失去焦点事件，添加当前输入的标签
const handleSelectBlur = () => {
  if (currentTagInput.value.trim()) {
    const tag = currentTagInput.value.trim()
    // 检查标签是否已存在
    if (!formData.tags.includes(tag)) {
      formData.tags.push(tag)
    }
    currentTagInput.value = ''
  }
}

// 分类列表
const categories = ref<API.ArticleCategory[]>([])

// 上传相关
const fileList = ref<any[]>([])
const previewVisible = ref(false)
const previewImage = ref('')
const uploading = ref(false)

// 保存状态
const saving = ref(false)
const publishing = ref(false)

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

// 加载文章详情（编辑模式）
const loadArticleDetail = async (id: string) => {
  try {
    const res = await getArticleDetail({ id })
    if (res.data.code === 0 && res.data.data) {
      const article = res.data.data
      formData.title = article.title || ''
      formData.categoryId = article.categoryId
      formData.coverImage = article.coverImage || ''
      formData.summary = article.summary || ''
      formData.content = article.content || ''
      formData.author = article.author || ''
      formData.source = article.source || ''
      formData.tags = Array.isArray(article.tags) ? article.tags : []
      formData.isTop = article.isTop === 1
      formData.isRecommend = article.isRecommend === 1

      // 设置封面图
      if (article.coverImage) {
        fileList.value = [
          {
            uid: '-1',
            name: 'cover.jpg',
            status: 'done',
            url: article.coverImage,
          },
        ]
      }
    } else {
      message.error('加载文章失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('加载文章失败:', error)
    message.error('加载文章失败：' + error.message)
  }
}

// 表单验证
const validateForm = () => {
  if (!formData.title) {
    message.warning('请输入文章标题')
    return false
  }
  if (!formData.categoryId) {
    message.warning('请选择文章分类')
    return false
  }
  if (!formData.summary) {
    message.warning('请输入文章摘要')
    return false
  }
  if (!formData.content) {
    message.warning('请输入文章内容')
    return false
  }
  return true
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!validateForm()) return

  saving.value = true
  try {
    const data = {
      ...formData,
      isTop: formData.isTop ? 1 : 0,
      isRecommend: formData.isRecommend ? 1 : 0,
    }

    if (isEdit.value && articleId.value) {
      // 更新 - 将tags数组转换为JSON字符串
      const updateData = { ...data }
      if (Array.isArray(updateData.tags)) {
        updateData.tags = JSON.stringify(updateData.tags)
      }
      const res = await updateArticle({ id: articleId.value, ...updateData } as any)
      if (res.data.code === 0) {
        message.success('保存成功')
      } else {
        message.error('保存失败：' + res.data.message)
      }
    } else {
      // 新建 - 保持tags数组不变，因为ArticleAddRequest期望List<String>
      const res = await addArticle(data as any)
      if (res.data.code === 0) {
        message.success('保存成功')
        router.push('/admin/article/manage')
      } else {
        message.error('保存失败：' + res.data.message)
      }
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    message.error('保存失败：' + error.message)
  } finally {
    saving.value = false
  }
}

// 发布文章
const handlePublish = async () => {
  if (!validateForm()) return

  publishing.value = true
  try {
    const data = {
      ...formData,
      isTop: formData.isTop ? 1 : 0,
      isRecommend: formData.isRecommend ? 1 : 0,
    }

    if (isEdit.value && articleId.value) {
      // 更新并发布 - 将tags数组转换为JSON字符串
      const updateData = { ...data }
      if (Array.isArray(updateData.tags)) {
        updateData.tags = JSON.stringify(updateData.tags)
      }
      const updateRes = await updateArticle({ id: articleId.value, ...updateData } as any)
      if (updateRes.data.code === 0) {
        // 发布
        const publishRes = await publishArticle({ id: articleId.value })
        if (publishRes.data.code === 0) {
          message.success('发布成功')
          router.push('/admin/article/manage')
        } else {
          message.error('发布失败：' + publishRes.data.message)
        }
      } else {
        message.error('更新失败：' + updateRes.data.message)
      }
    } else {
      // 新建并发布 - 保持tags数组不变，因为ArticleAddRequest期望List<String>
      const addRes = await addArticle(data as any)
      if (addRes.data.code === 0) {
        message.success('发布成功')
        router.push('/admin/article/manage')
      } else {
        message.error('发布失败：' + addRes.data.message)
      }
    }
  } catch (error: any) {
    console.error('发布失败:', error)
    message.error('发布失败：' + error.message)
  } finally {
    publishing.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 上传前检查
const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    message.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }

  // 创建FormData对象
  const uploadFormData = new FormData()
  uploadFormData.append('file', file)

  // 调用后端上传接口
  uploading.value = true
  try {
    const res = await uploadCoverImage(uploadFormData)
    if (res.data.code === 0 && res.data.data) {
      const cosUrl = res.data.data
      formData.coverImage = cosUrl
      fileList.value = [
        {
          uid: file.uid,
          name: file.name,
          status: 'done',
          url: cosUrl,
        },
      ]
      message.success('封面上传成功')
    } else {
      message.error('上传失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    message.error('上传失败：' + error.message)
  } finally {
    uploading.value = false
  }

  return false // 阻止自动上传
}

// 预览图片
const handlePreview = (file: any) => {
  previewImage.value = file.url || file.preview
  previewVisible.value = true
}

// 删除图片
const handleRemove = () => {
  formData.coverImage = ''
  fileList.value = []
}

// 初始化
onMounted(() => {
  loadCategories()

  // 检查是否编辑模式
  const id = route.params.id
  if (id) {
    isEdit.value = true
    articleId.value = id as string
    loadArticleDetail(articleId.value)
  }
})
</script>

<style scoped lang="scss">
.article-edit-page {
  padding: 24px;

  .upload-tip,
  .content-tip {
    margin-top: 8px;
    color: #999;
    font-size: 12px;
  }

  :deep(.ant-form-item-label) {
    font-weight: 500;
  }
}
</style>
