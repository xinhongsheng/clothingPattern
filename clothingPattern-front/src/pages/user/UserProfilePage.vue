<template>
  <div class="user-profile-page">
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <!-- 个人信息 -->
        <a-tab-pane key="info" tab="个人信息">
          <div class="profile-section">
            <a-form
              :model="userForm"
              :label-col="{ span: 4 }"
              :wrapper-col="{ span: 16 }"
              @finish="handleUpdateProfile"
            >
              <!-- 头像 -->
              <a-form-item label="头像">
                <div class="avatar-upload">
                  <a-avatar :size="100" :src="userForm.userAvatar || defaultAvatar">
                    <template #icon>
                      <user-outlined />
                    </template>
                  </a-avatar>
                  <a-upload
                    :show-upload-list="false"
                    :before-upload="handleAvatarUpload"
                    accept="image/*"
                  >
                    <a-button type="link" style="margin-top: 10px">
                      <camera-outlined /> 更换头像
                    </a-button>
                  </a-upload>
                </div>
              </a-form-item>

              <!-- 昵称 -->
              <a-form-item label="昵称" name="userName">
                <a-input
                  v-model:value="userForm.userName"
                  placeholder="请输入昵称"
                  :maxlength="20"
                />
              </a-form-item>

              <!-- 账号 -->
              <a-form-item label="账号">
                <a-input :value="loginUser?.userAccount" disabled />
              </a-form-item>

              <!-- 个人简介 -->
              <a-form-item label="个人简介" name="userProfile">
                <a-textarea
                  v-model:value="userForm.userProfile"
                  placeholder="请输入个人简介"
                  :rows="4"
                  :maxlength="200"
                  show-count
                />
              </a-form-item>

              <!-- 所在省份 -->
              <a-form-item label="所在省份" name="province">
                <a-select
                  v-model:value="userForm.province"
                  placeholder="请选择所在省份"
                  show-search
                  allow-clear
                  :filter-option="filterOption"
                >
                  <a-select-option v-for="province in provinceList" :key="province" :value="province">
                    {{ province }}
                  </a-select-option>
                </a-select>
              </a-form-item>

              <!-- 按钮 -->
              <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
                <a-space>
                  <a-button type="primary" html-type="submit" :loading="saving">
                    保存修改
                  </a-button>
                  <a-button @click="loadUserInfo">重置</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <!-- 我的收藏 -->
        <a-tab-pane key="collect" tab="我的收藏">
          <div class="collect-section">
            <a-spin :spinning="collectLoading">
              <a-empty v-if="collectList.length === 0" description="暂无收藏" />
              <a-list
                v-else
                :grid="{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 3, xxl: 4 }"
                :data-source="collectList"
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-card hoverable @click="goToArticleDetail(item.id)">
                      <template #cover>
                        <img
                          v-if="item.coverImage"
                          :src="item.coverImage"
                          :alt="item.title"
                          class="cover-image"
                        />
                        <div v-else class="no-cover">
                          <file-image-outlined style="font-size: 48px; color: #ccc" />
                        </div>
                      </template>
                      <a-card-meta :title="item.title">
                        <template #description>
                          <div class="article-summary">{{ item.summary }}</div>
                          <div class="article-stats">
                            <span><eye-outlined /> {{ item.viewCount || 0 }}</span>
                            <span><like-outlined /> {{ item.likeCount || 0 }}</span>
                            <span><star-outlined /> {{ item.collectCount || 0 }}</span>
                          </div>
                        </template>
                      </a-card-meta>
                    </a-card>
                  </a-list-item>
                </template>
              </a-list>
            </a-spin>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  CameraOutlined,
  EyeOutlined,
  LikeOutlined,
  StarOutlined,
  FileImageOutlined,
} from '@ant-design/icons-vue'
import { getLoginUser, updateMyUser, uploadAvatar } from '@/api/userController'
import { getMyCollectArticles } from '@/api/articleController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const router = useRouter()
const userStore = useLoginUserStore()

// 当前标签页
const activeTab = ref('info')

// 登录用户信息
const loginUser = computed(() => userStore.loginUser)

// 默认头像
const defaultAvatar = 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png'

// 用户表单
const userForm = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
  province: '',
})

// 省份列表
const provinceList = [
  '北京市', '天津市', '上海市', '重庆市',
  '河北省', '山西省', '辽宁省', '吉林省', '黑龙江省',
  '江苏省', '浙江省', '安徽省', '福建省', '江西省', '山东省',
  '河南省', '湖北省', '湖南省', '广东省', '海南省',
  '四川省', '贵州省', '云南省', '陕西省', '甘肃省', '青海省',
  '台湾省',
  '内蒙古自治区', '广西壮族自治区', '西藏自治区', '宁夏回族自治区', '新疆维吾尔自治区',
  '香港特别行政区', '澳门特别行政区',
]

// 省份搜索过滤
const filterOption = (input: string, option: any) => {
  return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 保存状态
const saving = ref(false)

// 收藏列表
const collectList = ref<API.ArticleVO[]>([])
const collectLoading = ref(false)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getLoginUser()
    if (res.data.code === 0 && res.data.data) {
      const user = res.data.data
      userForm.userName = user.userName || ''
      userForm.userAvatar = user.userAvatar || ''
      userForm.userProfile = user.userProfile || ''
      userForm.province = user.province || ''
    }
  } catch (error: any) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载收藏列表
const loadCollectList = async () => {
  collectLoading.value = true
  try {
    const res = await getMyCollectArticles()
    if (res.data.code === 0 && res.data.data) {
      collectList.value = res.data.data
    } else {
      message.error('加载收藏列表失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('加载收藏列表失败:', error)
    message.error('加载收藏列表失败')
  } finally {
    collectLoading.value = false
  }
}

// 更新个人信息
const handleUpdateProfile = async () => {
  saving.value = true
  try {
    const res = await updateMyUser({
      userName: userForm.userName,
      userAvatar: userForm.userAvatar,
      userProfile: userForm.userProfile,
      province: userForm.province,
    })
    if (res.data.code === 0) {
      message.success('保存成功')
      // 刷新用户信息
      await userStore.fetchLoginUser()
    } else {
      message.error('保存失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 上传头像
const handleAvatarUpload = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }

  try {
    // 调用后端上传接口
    const res = await uploadAvatar({}, file) as any
    if (res.data.code === 0 && res.data.data) {
      const cosUrl = res.data.data
      userForm.userAvatar = cosUrl
      message.success('头像已更新，请点击保存修改')
    } else {
      message.error('上传失败：' + res.data.message)
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    message.error('上传失败：' + error.message)
  }

  return false // 阻止自动上传
}

// 跳转到文章详情
const goToArticleDetail = (id: number) => {
  router.push(`/article/${id}`)
}

// 初始化
onMounted(() => {
  loadUserInfo()
  loadCollectList()
})
</script>

<style scoped lang="scss">
.user-profile-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;

  .profile-section {
    padding: 24px 0;

    .avatar-upload {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
  }

  .collect-section {
    min-height: 400px;

    .cover-image {
      width: 100%;
      height: 200px;
      object-fit: cover;
    }

    .no-cover {
      width: 100%;
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
    }

    .article-summary {
      margin-bottom: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      color: #666;
      font-size: 14px;
    }

    .article-stats {
      display: flex;
      gap: 16px;
      color: #999;
      font-size: 13px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    :deep(.ant-card) {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
    }
  }
}
</style>
