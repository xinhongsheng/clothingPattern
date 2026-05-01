  <template>
    <div class="article-page">
      <div class="page-container">
        <!-- 顶部轮播图 -->
        <section class="banner-section">
          <a-carousel autoplay effect="fade" :dots="true" :autoplay-speed="4000">
            <div v-for="banner in bannerList" :key="banner.id" class="carousel-item">
              <a href="#" target="_blank" rel="noopener noreferrer">
                <div class="image-wrapper">
                  <img :src="banner.imageUrl" :alt="banner.title" class="carousel-image" />
                  <div class="banner-content">
                    <div class="banner-pill">精选推荐</div>
                    <h2 class="banner-title">{{ banner.title }}</h2>
                  </div>
                </div>
              </a>
            </div>
          </a-carousel>
        </section>

        <!-- 主要内容区 -->
        <div class="main-content-wrapper">
          <!-- 左侧文章列表 -->
          <main class="left-column">
            <!-- 分类和筛选栏 -->
            <div class="filter-bar card-glass">
              <div class="category-scroll">
                <a-button
                  v-for="category in [{ id: null, categoryName: '全部' }, ...categories]"
                  :key="category.id || 'all'"
                  :type="selectedCategory === category.id ? 'primary' : 'text'"
                  class="category-btn"
                  @click="handleCategoryChange(category.id)"
                >
                  {{ category.categoryName }}
                </a-button>
              </div>

              <div class="sort-actions">
                <a-input-search
                  v-model:value="searchKeyword"
                  placeholder="搜索精彩文章..."
                  class="custom-search"
                  @search="handleSearch"
                />
                <a-radio-group
                  v-model:value="sortField"
                  button-style="solid"
                  @change="handleSortChange"
                >
                  <a-radio-button value="publishTime">最新</a-radio-button>
                  <a-radio-button value="viewCount">最热</a-radio-button>
                </a-radio-group>
              </div>
            </div>

            <!-- 文章列表 -->
            <a-spin :spinning="loading">
              <div v-if="articleList.length > 0" class="article-list">
                <div
                  v-for="article in articleList"
                  :key="article.id"
                  class="article-card card-glass"
                  @click="goToDetail(article.id)"
                >
                  <div class="card-cover">
                    <a-image
                      :src="article.coverImage || '/default-article-cover.jpg'"
                      :alt="article.title"
                      :preview="false"
                      class="cover-img"
                    />
                    <div class="badges">
                      <span v-if="article.isTop === 1" class="badge top">置顶</span>
                      <span v-if="article.isHot === 1" class="badge hot">热门</span>
                    </div>
                    <div class="cover-fade"></div>
                  </div>

                  <div class="card-content">
                    <div class="meta-top">
                      <a-tag color="blue" class="category-tag">{{ article.categoryName }}</a-tag>
                      <span class="publish-time">{{ formatTime(article.publishTime) }}</span>
                    </div>

                    <h3 class="title" :title="article.title">{{ article.title }}</h3>
                    <p class="summary">{{ article.summary }}</p>

                    <div class="meta-footer">
                      <div class="author-info">
                        <a-avatar size="small" :src="article.userAvatar" class="avatar">
                          {{ article.author?.[0]?.toUpperCase() }}
                        </a-avatar>
                        <span class="author-name">{{ article.author }}</span>
                      </div>

                      <div class="stats">
                        <span title="阅读"
                          ><EyeOutlined /> {{ formatNumber(article.viewCount) }}</span
                        >
                        <span title="点赞"
                          ><LikeOutlined /> {{ formatNumber(article.likeCount) }}</span
                        >
                        <span title="评论"
                          ><MessageOutlined /> {{ formatNumber(article.commentCount) }}</span
                        >
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <a-empty
                v-else
                description="暂无相关文章"
                :image="Empty.PRESENTED_IMAGE_SIMPLE"
                class="empty-state"
              />
            </a-spin>

            <!-- 分页 -->
            <div v-if="total > 0" class="pagination-wrapper">
              <a-pagination
                v-model:current="currentPage"
                v-model:pageSize="pageSize"
                :total="total"
                :show-size-changer="false"
                @change="handlePageChange"
              />
            </div>
          </main>

          <!-- 右侧边栏 -->
          <aside class="right-column">
            <!-- 热门推荐 -->
            <div class="sidebar-card hot-articles card-glass">
              <h3 class="sidebar-title"><FireOutlined class="icon-fire" /> 热门推荐</h3>

              <a-spin :spinning="hotLoading">
                <ul class="hot-list">
                  <li
                    v-for="(article, index) in hotArticles"
                    :key="article.id"
                    class="hot-item"
                    @click="goToDetail(article.id)"
                  >
                    <span class="rank-num" :class="{ 'top-3': index < 3 }">{{ index + 1 }}</span>
                    <div class="hot-info">
                      <div class="hot-title">{{ article.title }}</div>
                      <div class="hot-view">{{ formatNumber(article.viewCount) }} 阅读</div>
                    </div>
                  </li>
                </ul>
              </a-spin>
            </div>

            <!-- 推广/广告位 - 仅管理员可见 -->
            <div v-if="isAdmin" class="sidebar-card promotion-card card-glass">
              <div class="promo-orb orb-a"></div>
              <div class="promo-orb orb-b"></div>

              <div class="promo-content">
                <div class="promo-eyebrow">Community</div>
                <h4>分享你的灵感</h4>
                <p>加入我们的社区，发布你的第一篇设计文章。</p>
                <a-button type="primary" block class="promo-btn" @click="goToPublishArticle">立即发布</a-button>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  </template>

  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Empty } from 'ant-design-vue'
  import { EyeOutlined, LikeOutlined, MessageOutlined, FireOutlined } from '@ant-design/icons-vue'
  import { getArticleList } from '@/api/articleController'
  import { getBannerList } from '@/api/bannerController'
  import { getCategories } from '@/api/articleCategoryController'
  import { useLoginUserStore } from '@/stores/useLoginUserStore'
  import dayjs from 'dayjs'
  import relativeTime from 'dayjs/plugin/relativeTime'
  import 'dayjs/locale/zh-cn'

  dayjs.extend(relativeTime)
  dayjs.locale('zh-cn')

  const router = useRouter()
  const loginUserStore = useLoginUserStore()

  // 管理员判断
  const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')

  // 跳转到发布文章页面
  const goToPublishArticle = () => {
    router.push('/admin/article/edit')
  }

  // 数据定义
  const categories = ref<API.ArticleCategory[]>([])
  const articleList = ref<API.ArticleVO[]>([])
  const hotArticles = ref<API.ArticleVO[]>([])
  const bannerList = ref<any[]>([])
  const selectedCategory = ref<string | null>(null)
  const searchKeyword = ref('')
  const sortField = ref('publishTime')
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const loading = ref(false)
  const hotLoading = ref(false)

  // 辅助函数
  const formatTime = (time: string) => (time ? dayjs(time).fromNow() : '')
  const formatNumber = (num: number) => {
    return num > 999 ? (num / 1000).toFixed(1) + 'k' : num
  }

  // 加载数据
  const loadCategories = async () => {
    try {
      const res = await getCategories()
      if (res.data.code === 0 && res.data.data) {
        categories.value = res.data.data
      }
    } catch (error) {
      console.error('加载分类失败', error)
    }
  }

  const loadArticles = async () => {
    loading.value = true
    try {
      const res = await getArticleList({
        categoryId: selectedCategory.value || undefined,
        keyword: searchKeyword.value || undefined,
        sortField: sortField.value,
        sortOrder: 'desc',
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        status: 'PUBLISHED',
        auditStatus: 'APPROVED',
      })
      if (res.data.code === 0) {
        articleList.value = res.data.data.records || []
        total.value = res.data.data.total || 0
      }
    } finally {
      loading.value = false
    }
  }

  const loadHotArticles = async () => {
    hotLoading.value = true
    try {
      const res = await getArticleList({
        sortField: 'viewCount',
        sortOrder: 'desc',
        pageNum: 1,
        pageSize: 5,
        status: 'PUBLISHED',
        auditStatus: 'APPROVED',
      })
      if (res.data.code === 0) {
        hotArticles.value = res.data.data.records || []
      }
    } finally {
      hotLoading.value = false
    }
  }

  const loadBanners = async () => {
    try {
      const res = await getBannerList()
      if (res.data.code === 0) {
        bannerList.value = res.data.data
      }
    } catch (error) {
      console.error(error)
    }
  }

  // 事件处理
  const handleCategoryChange = (val: string | null) => {
    if (selectedCategory.value === val) return
    selectedCategory.value = val
    currentPage.value = 1
    loadArticles()
  }

  const handleSearch = () => {
    currentPage.value = 1
    loadArticles()
  }

  const handleSortChange = () => {
    currentPage.value = 1
    loadArticles()
  }

  const handlePageChange = (page: number) => {
    currentPage.value = page
    loadArticles()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const goToDetail = (id?: string) => {
    if (id) router.push(`/article/${id}`)
  }

  onMounted(() => {
    loadCategories()
    loadArticles()
    loadHotArticles()
    loadBanners()
  })
  </script>

  <style scoped lang="scss">
  @import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700&family=Newsreader:wght@400;600;700&display=swap');

  /* =========================
    Design Tokens（与首页一致）
    ========================= */
  .article-page {
    --ink: #1f1a15;
    --muted: #7a6f66;
    --accent: #d45b2d;
    --accent-2: #2a9d8f;
    --surface: #fffdf8;
    --surface-2: #f6efe6;
    --stroke: rgba(31, 26, 21, 0.08);
    --shadow: 0 22px 60px rgba(31, 26, 21, 0.12);
    --transition-duration: 0.35s;
    --transition-easing: cubic-bezier(0.22, 1, 0.36, 1);
    --font-body: 'Manrope', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;
    --font-display: 'Newsreader', 'Noto Serif SC', 'Songti SC', serif;

    min-height: 100vh;
    padding: 24px 0 44px;
    font-family: var(--font-body);
    color: var(--ink);
    background:
      radial-gradient(900px 420px at 8% -10%, rgba(240, 181, 128, 0.35), transparent 65%),
      radial-gradient(800px 380px at 92% 5%, rgba(122, 210, 196, 0.28), transparent 60%),
      linear-gradient(180deg, #fbf7f1 0%, #ffffff 55%, #f6efe6 100%);
    position: relative;
    isolation: isolate;
    overflow-x: hidden;
  }

  .article-page::after {
    content: '';
    position: absolute;
    inset: 0;
    background-image: radial-gradient(rgba(31, 26, 21, 0.06) 1px, transparent 1px);
    background-size: 22px 22px;
    opacity: 0.32;
    z-index: 0;
    pointer-events: none;
  }

  .page-container {
    max-width: 1440px;
    margin: 0 auto;
    padding: 0 16px;
    position: relative;
    z-index: 1;
  }

  /* =========================
    Glass Card Helper
    ========================= */
  .card-glass {
    background: rgba(255, 255, 255, 0.78);
    border: 1px solid var(--stroke);
    border-radius: 26px;
    box-shadow: 0 18px 36px rgba(31, 26, 21, 0.08);
    backdrop-filter: blur(10px);
  }

  /* =========================
    Banner
    ========================= */
  .banner-section {
    margin-bottom: 22px;
    border-radius: 28px;
    overflow: hidden;
    border: 1px solid var(--stroke);
    box-shadow: var(--shadow);
    position: relative;
    z-index: 1;
  }

  :deep(.ant-carousel .slick-dots) {
    bottom: 18px;
  }

  :deep(.ant-carousel .slick-dots li button) {
    width: 18px;
    height: 6px;
    border-radius: 999px;
    opacity: 0.35;
  }

  :deep(.ant-carousel .slick-dots li.slick-active button) {
    opacity: 1;
  }

  .carousel-item {
    height: 380px;
    position: relative;
  }

  .image-wrapper {
    width: 100%;
    height: 100%;
    position: relative;
  }

  .carousel-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transform: scale(1.02);
    transition: transform 0.8s var(--transition-easing);
  }

  .carousel-item:hover .carousel-image {
    transform: scale(1.06);
  }

  .banner-content {
    position: absolute;
    inset: auto 0 0 0;
    padding: 56px 26px 26px;
    color: #fff;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent 70%);
  }

  .banner-pill {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.18);
    border: 1px solid rgba(255, 255, 255, 0.26);
    font-size: 12px;
    letter-spacing: 0.18em;
    text-transform: uppercase;
    font-weight: 700;
    margin-bottom: 12px;
    backdrop-filter: blur(8px);
  }

  .banner-title {
    font-family: var(--font-display);
    color: #fff;
    font-size: 28px;
    font-weight: 700;
    margin: 0;
    letter-spacing: -0.6px;
    text-shadow: 0 2px 14px rgba(0, 0, 0, 0.28);
  }

  /* =========================
    Main Layout
    ========================= */
  .main-content-wrapper {
    display: flex;
    gap: 22px;
    align-items: flex-start;
  }

  .left-column {
    flex: 1;
    min-width: 0;
  }

  .right-column {
    width: 330px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 18px;
  }

  /* =========================
    Filter Bar
    ========================= */
  .filter-bar {
    padding: 18px 20px;
    margin-bottom: 18px;
    display: flex;
    flex-direction: column;
    gap: 14px;
    transition: all var(--transition-duration) var(--transition-easing);
  }

  .filter-bar:hover {
    transform: translateY(-2px);
    box-shadow: 0 24px 48px rgba(31, 26, 21, 0.12);
  }

  .category-scroll {
    display: flex;
    overflow-x: auto;
    padding-bottom: 6px;
    gap: 8px;

    &::-webkit-scrollbar {
      height: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(31, 26, 21, 0.12);
      border-radius: 999px;
    }
  }

  .category-btn {
    flex-shrink: 0;
    border-radius: 999px;
    font-weight: 700;
    padding: 0 14px;
    height: 34px;
    transition: all 0.25s var(--transition-easing);
  }

  :deep(.category-btn.ant-btn-text) {
    color: var(--muted);
  }

  :deep(.category-btn.ant-btn-text:hover) {
    color: var(--accent);
    background: rgba(212, 91, 45, 0.08);
    transform: translateY(-1px);
  }

  :deep(.category-btn.ant-btn-primary) {
    border: none;
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
    box-shadow: 0 12px 22px rgba(212, 91, 45, 0.25);
  }

  .sort-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 14px;
  }

  .custom-search {
    max-width: 320px;
    width: 100%;
  }

  :deep(.custom-search .ant-input) {
    height: 40px;
    border-radius: 999px;
    border: 1px solid rgba(31, 26, 21, 0.12);
    background: rgba(255, 255, 255, 0.9);
    transition: all 0.25s var(--transition-easing);
    padding-left: 16px;
  }

  :deep(.custom-search .ant-input:focus) {
    border-color: rgba(42, 157, 143, 0.55);
    box-shadow: 0 0 0 3px rgba(42, 157, 143, 0.16);
  }

  :deep(.custom-search .ant-input-search-button) {
    border-radius: 0 999px 999px 0 !important;
    border: none;
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
    box-shadow: 0 12px 22px rgba(212, 91, 45, 0.22);
  }

  :deep(.ant-radio-group) {
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.65);
    border: 1px solid rgba(31, 26, 21, 0.1);
    padding: 4px;
  }

  :deep(.ant-radio-button-wrapper) {
    border: none !important;
    border-radius: 999px !important;
    color: var(--muted);
    font-weight: 800;
    height: 32px;
    line-height: 32px;
    padding: 0 14px;
    background: transparent;
    transition: all 0.25s var(--transition-easing);
  }

  :deep(.ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked):hover) {
    color: var(--accent);
    background: rgba(212, 91, 45, 0.08);
  }

  :deep(.ant-radio-button-wrapper-checked) {
    color: #fff !important;
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%) !important;
    box-shadow: 0 12px 22px rgba(212, 91, 45, 0.22);
  }

  /* =========================
    Article List & Cards
    ========================= */
  .article-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .article-card {
    display: flex;
    overflow: hidden;
    cursor: pointer;
    border-radius: 26px;
    transition: all var(--transition-duration) var(--transition-easing);
  }

  .article-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 26px 46px rgba(31, 26, 21, 0.12);
    border-color: rgba(212, 91, 45, 0.22);
  }

  .card-cover {
    width: 300px;
    position: relative;
    overflow: hidden;
    flex-shrink: 0;
  }

  .cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.7s var(--transition-easing);
  }

  .article-card:hover .cover-img {
    transform: scale(1.06);
  }

  .cover-fade {
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, rgba(0, 0, 0, 0.18), transparent 55%);
    pointer-events: none;
  }

  .badges {
    position: absolute;
    top: 14px;
    left: 14px;
    display: flex;
    gap: 8px;
    z-index: 2;
  }

  .badge {
    padding: 4px 10px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 900;
    color: #fff;
    border: 1px solid rgba(255, 255, 255, 0.24);
    backdrop-filter: blur(10px);
  }

  .badge.top {
    background: rgba(255, 77, 79, 0.92);
  }
  .badge.hot {
    background: rgba(255, 122, 69, 0.92);
  }

  .card-content {
    flex: 1;
    padding: 18px 20px;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .meta-top {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 8px;
  }

  :deep(.category-tag) {
    border-radius: 999px;
    padding: 2px 10px;
    font-weight: 800;
  }

  .publish-time {
    color: rgba(122, 111, 102, 0.9);
    font-size: 13px;
    font-weight: 600;
  }

  .title {
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 700;
    color: var(--ink);
    margin: 0 0 6px;
    letter-spacing: -0.4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .summary {
    color: var(--muted);
    font-size: 14px;
    line-height: 1.7;
    margin: 0 0 14px;
    flex: 1;

    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .meta-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid rgba(31, 26, 21, 0.08);
    padding-top: 12px;
  }

  .author-info {
    display: flex;
    align-items: center;
    gap: 8px;
    color: var(--ink);
    font-weight: 700;
    font-size: 13px;
  }

  :deep(.avatar) {
    box-shadow: 0 12px 22px rgba(31, 26, 21, 0.08);
  }

  .author-name {
    white-space: nowrap;
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .stats {
    display: flex;
    gap: 14px;
    color: rgba(122, 111, 102, 0.9);
    font-size: 13px;
    font-weight: 700;
  }

  .stats span {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: color 0.25s var(--transition-easing);
  }

  .stats span:hover {
    color: var(--accent);
  }

  /* =========================
    Sidebar
    ========================= */
  .sidebar-card {
    padding: 18px;
    border-radius: 26px;
    transition: all var(--transition-duration) var(--transition-easing);
  }

  .sidebar-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 24px 48px rgba(31, 26, 21, 0.12);
  }

  .sidebar-title {
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 700;
    margin: 0 0 14px;
    display: flex;
    align-items: center;
    gap: 10px;
    letter-spacing: -0.3px;

    .icon-fire {
      color: #ff4d4f;
      filter: drop-shadow(0 10px 16px rgba(255, 77, 79, 0.2));
    }
  }

  .hot-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }

  .hot-item {
    display: flex;
    gap: 12px;
    padding: 10px 4px;
    cursor: pointer;
    border-bottom: 1px dashed rgba(31, 26, 21, 0.12);
    transition: all 0.25s var(--transition-easing);
  }

  .hot-item:last-child {
    border-bottom: none;
  }

  .hot-item:hover {
    transform: translateY(-1px);
  }

  .rank-num {
    width: 22px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    background: rgba(31, 26, 21, 0.06);
    color: rgba(122, 111, 102, 0.95);
    font-size: 12px;
    font-weight: 900;
    border-radius: 8px;
    margin-top: 2px;
    flex-shrink: 0;
  }

  .rank-num.top-3 {
    background: rgba(255, 77, 79, 0.92);
    color: #fff;
  }

  .hot-info {
    flex: 1;
    overflow: hidden;
  }

  .hot-title {
    font-size: 14px;
    color: var(--ink);
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-weight: 800;
    transition: color 0.25s var(--transition-easing);
  }

  .hot-item:hover .hot-title {
    color: var(--accent);
  }

  .hot-view {
    font-size: 12px;
    color: rgba(122, 111, 102, 0.92);
    font-weight: 700;
  }

  /* =========================
    Promotion (match homepage vibe, no "cheap gradient")
    ========================= */
  .promotion-card {
    position: relative;
    overflow: hidden;
    padding: 18px;
  }

  .promo-orb {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
    opacity: 0.65;
  }

  .promo-orb.orb-a {
    width: 220px;
    height: 220px;
    right: -90px;
    top: -120px;
    background: radial-gradient(circle at 30% 30%, rgba(244, 182, 124, 0.8), transparent 70%);
    animation: float 10s ease-in-out infinite;
  }

  .promo-orb.orb-b {
    width: 180px;
    height: 180px;
    left: -80px;
    bottom: -120px;
    background: radial-gradient(circle at 30% 30%, rgba(92, 184, 167, 0.75), transparent 70%);
    animation: float 12s ease-in-out infinite reverse;
  }

  .promo-content {
    position: relative;
    z-index: 1;
    text-align: left;
  }

  .promo-eyebrow {
    font-size: 11px;
    letter-spacing: 0.22em;
    text-transform: uppercase;
    color: var(--accent-2);
    font-weight: 800;
    margin-bottom: 10px;
  }

  .promo-content h4 {
    font-family: var(--font-display);
    margin: 0 0 8px;
    font-size: 20px;
    letter-spacing: -0.4px;
    color: var(--ink);
  }

  .promo-content p {
    margin: 0 0 14px;
    color: var(--muted);
    font-size: 13px;
    line-height: 1.7;
  }

  .promo-btn {
    border-radius: 999px;
    border: none;
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%);
    box-shadow: 0 14px 24px rgba(212, 91, 45, 0.22);
  }

  /* =========================
    Pagination & Empty
    ========================= */
  .pagination-wrapper {
    margin-top: 26px;
    display: flex;
    justify-content: center;
  }

  :deep(.ant-pagination-item) {
    border-radius: 12px !important;
    border: 1px solid transparent;
    background: rgba(255, 255, 255, 0.9);
    transition: all 0.2s var(--transition-easing);
  }

  :deep(.ant-pagination-item:hover) {
    border-color: rgba(212, 91, 45, 0.4);
    color: var(--accent);
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(212, 91, 45, 0.14);
  }

  :deep(.ant-pagination-item-active) {
    background: linear-gradient(135deg, var(--accent) 0%, #f08a5d 100%) !important;
    border: none !important;
    box-shadow: 0 12px 22px rgba(212, 91, 45, 0.25);
  }

  :deep(.ant-pagination-item-active a) {
    color: #ffffff !important;
  }

  .empty-state {
    padding: 48px 0;
  }

  :deep(.ant-empty-description) {
    color: rgba(122, 111, 102, 0.9);
    font-weight: 700;
  }

  /* =========================
    Responsive
    ========================= */
  @media (max-width: 992px) {
    .main-content-wrapper {
      flex-direction: column;
    }

    .right-column {
      width: 100%;
      order: 2;
    }

    .article-card {
      flex-direction: column;
    }

    .card-cover {
      width: 100%;
      height: 200px;
    }

    .card-content {
      padding: 16px 16px 18px;
    }
  }

  @media (max-width: 576px) {
    .carousel-item {
      height: 220px;
    }

    .banner-title {
      font-size: 20px;
    }

    .sort-actions {
      flex-direction: column;
      align-items: stretch;
    }

    .custom-search {
      max-width: none;
    }

    :deep(.ant-radio-group) {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }

    :deep(.ant-radio-button-wrapper) {
      flex: 1;
      text-align: center;
    }
  }

  /* =========================
    Animations
    ========================= */
  @keyframes float {
    0%,
    100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(12px);
    }
  }
  </style>
