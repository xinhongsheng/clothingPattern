declare namespace API {
  type ActiveUserVO = {
    user?: UserVO
    patternCount?: number
  }

  type AiAnswerVO = {
    question?: string
    answer?: string
    imageUrl?: string
  }

  type AiQuestionRequest = {
    question?: string
    imageUrl?: string
  }

  type Article = {
    id?: string
    categoryId?: string
    title?: string
    coverImage?: string
    summary?: string
    content?: string
    author?: string
    source?: string
    tags?: string
    viewCount?: number
    likeCount?: number
    commentCount?: number
    shareCount?: number
    collectCount?: number
    isTop?: number
    isHot?: number
    isRecommend?: number
    status?: string
    auditStatus?: string
    publishTime?: string
    createTime?: string
    updateTime?: string
    categoryName?: string
    liked?: boolean
    collected?: boolean
    isDelete?: number
  }

  type ArticleAddRequest = {
    categoryId: string
    title: string
    coverImage?: string
    summary: string
    content: string
    author?: string
    source?: string
    tags?: string
    isTop?: number
    isRecommend?: number
  }

  type ArticleCategory = {
    id?: string
    categoryName?: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ArticleCategoryVO = {
    id?: string
    categoryName?: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
    createTime?: string
    updateTime?: string
  }

  type ArticleQueryRequest = {
    categoryId?: string
    keyword?: string
    tag?: string[]
    status?: string
    auditStatus?: string
    isTop?: number
    isHot?: number
    isRecommend?: number
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
  }

  type ArticleVO = {
    id?: string
    categoryId?: string
    categoryName?: string
    title?: string
    coverImage?: string
    summary?: string
    content?: string
    author?: string
    source?: string
    tags?: string
    viewCount?: number
    likeCount?: number
    commentCount?: number
    shareCount?: number
    collectCount?: number
    isTop?: number
    isHot?: number
    isRecommend?: number
    status?: string
    auditStatus?: string
    publishTime?: string
    createTime?: string
    updateTime?: string
    liked?: boolean
    collected?: boolean
  }

  type BaseResponseAiAnswerVO = {
    code?: number
    data?: AiAnswerVO
    message?: string
  }

  type BaseResponseArticleVO = {
    code?: number
    data?: ArticleVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseCommentStatisticsVO = {
    code?: number
    data?: CommentStatisticsVO
    message?: string
  }

  type BaseResponseCommentVO = {
    code?: number
    data?: CommentVO
    message?: string
  }

  type BaseResponseHomeStatisticsVO = {
    code?: number
    data?: HomeStatisticsVO
    message?: string
  }

  type BaseResponseLikeResult = {
    code?: number
    data?: LikeResult
    message?: string
  }

  type BaseResponseLikeResultVO = {
    code?: number
    data?: LikeResultVO
    message?: string
  }

  type BaseResponseListArticleCategory = {
    code?: number
    data?: ArticleCategory[]
    message?: string
  }

  type BaseResponseListArticleCategoryVO = {
    code?: number
    data?: ArticleCategoryVO[]
    message?: string
  }

  type BaseResponseListArticleVO = {
    code?: number
    data?: ArticleVO[]
    message?: string
  }

  type BaseResponseListCommentVO = {
    code?: number
    data?: CommentVO[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseMapLongBoolean = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponsePagePattern = {
    code?: number
    data?: PagePattern
    message?: string
  }

  type BaseResponsePagePatternVO = {
    code?: number
    data?: PagePatternVO
    message?: string
  }

  type BaseResponsePageResultArticleVO = {
    code?: number
    data?: PageResultArticleVO
    message?: string
  }

  type BaseResponsePageResultCommentVO = {
    code?: number
    data?: PageResultCommentVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponsePattern = {
    code?: number
    data?: Pattern
    message?: string
  }

  type BaseResponsePatternVO = {
    code?: number
    data?: PatternVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseStringArray = {
    code?: number
    data?: string[]
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type cancelCollectArticleParams = {
    articleId: string
  }

  type CategoryAddRequest = {
    categoryName: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
  }

  type CategoryUpdateRequest = {
    id: string
    categoryName: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
  }

  type clearCacheParams = {
    patternId?: string
  }

  type collectArticleParams = {
    articleId: string
  }

  type CommentAddRequest = {
    patternId: string
    content: string
    parentId?: string
  }

  type CommentQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    patternId: string
    parentId?: string
  }

  type CommentStatisticsVO = {
    totalComments?: number
    mainComments?: number
    totalLikes?: number
  }

  type CommentVO = {
    id?: string
    userId?: string
    patternId?: string
    content?: string
    parentId?: string
    rootId?: string
    replyToUserId?: string
    likeCount?: number
    replyCount?: number
    topStatus?: number
    auditStatus?: string
    createTime?: string
    userName?: string
    userAvatar?: string
    replyToUserName?: string
    liked?: boolean
  }

  type DataExportRequest = {
    format?: string
    startDate?: string
    endDate?: string
  }

  type deleteArticleParams = {
    id: string
  }

  type deleteCategoryParams = {
    id: string
  }

  type deleteCommentParams = {
    commentId: string
  }

  type DeleteRequest = {
    id?: string
  }

  type getArticleDetailParams = {
    id: string
  }

  type getBatchLikeStatusParams = {
    patternIds: string[]
  }

  type getCollectStatusParams = {
    articleId: string
  }

  type getCommentDetailParams = {
    commentId: string
  }

  type getCommentRepliesParams = {
    commentId: string
  }

  type getCommentStatisticsParams = {
    patternId: string
  }

  type getHotArticlesParams = {
    limit?: number
  }

  type getLikeStatus1Params = {
    articleId: string
  }

  type getLikeStatusParams = {
    patternId: string
  }

  type getPatternByIdParams = {
    id: string
  }

  type getPatternVOByIdParams = {
    id: string
  }

  type getRecommendArticlesParams = {
    limit?: number
  }

  type getUserByIdParams = {
    id: string
  }

  type getUserVOByIdParams = {
    id: string
  }

  type HomeStatisticsVO = {
    styleDistribution?: Record<string, any>
    activeUsers?: ActiveUserVO[]
    trendData?: TrendDataVO[]
    totalPatterns?: number
    totalUsers?: number
  }

  type likeArticleParams = {
    articleId: string
  }

  type LikeResult = {
    liked?: boolean
    likeCount?: number
  }

  type LikeResultVO = {
    isLiked?: boolean
    likeCount?: number
  }

  type listedArticleParams = {
    id: string
  }

  type LoginUserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
  }

  type offlineArticleParams = {
    id: string
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type PagePattern = {
    records?: Pattern[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePattern
    searchCount?: PagePattern
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PagePatternVO = {
    records?: PatternVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PagePatternVO
    searchCount?: PagePatternVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageResultArticleVO = {
    list?: ArticleVO[]
    total?: number
  }

  type PageResultCommentVO = {
    list?: CommentVO[]
    total?: number
  }

  type PageUserVO = {
    records?: UserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUserVO
    searchCount?: PageUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type Pattern = {
    id?: string
    userId?: string
    patternName?: string
    description?: string
    generationType?: string
    referenceImageUrl?: string
    patternUrl?: string
    thumbUrl?: string
    fileSize?: number
    fileType?: string
    style?: string
    season?: string
    targetAudience?: string
    generationParams?: Record<string, any>
    auditStatus?: string
    auditTime?: string
    auditorId?: string
    rejectReason?: string
    likeCount?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type PatternAuditRequest = {
    id?: string
    auditStatus?: string
    rejectReason?: string
  }

  type PatternEditRequest = {
    id?: string
    patternName?: string
    description?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type PatternGenerateRequest = {
    serviceType?: string
    doubaoMode?: string
    referenceImageUrls?: string[]
    maxImages?: number
    patternName?: string
    description?: string
    generationType?: string
    referenceImageUrl?: string
    style?: string
    season?: string
    targetAudience?: string
    size?: string
    negativePrompt?: string
    promptExtend?: boolean
  }

  type PatternQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: string
    userId?: string
    patternName?: string
    generationType?: string
    style?: string
    season?: string
    targetAudience?: string
    auditStatus?: string
  }

  type PatternUpdateRequest = {
    id?: string
    patternName?: string
    description?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type PatternVO = {
    id?: string
    userId?: string
    patternName?: string
    description?: string
    generationType?: string
    referenceImageUrl?: string
    patternUrl?: string
    thumbUrl?: string
    fileSize?: number
    fileType?: string
    style?: string
    season?: string
    targetAudience?: string
    auditStatus?: string
    auditTime?: string
    rejectReason?: string
    createTime?: string
    updateTime?: string
    user?: UserVO
    likeCount?: number
    isLiked?: boolean
  }

  type publishArticleParams = {
    id: string
  }

  type searchArticlesParams = {
    keyword: string
    pageNum?: number
    pageSize?: number
  }

  type SseEmitter = {
    timeout?: number
  }

  type toggleCommentLikeParams = {
    commentId: string
  }

  type toggleLikeParams = {
    patternId: string
  }

  type TrendDataVO = {
    date?: string
    count?: number
  }

  type User = {
    id?: string
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: string
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
