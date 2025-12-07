declare namespace API {
  type ActiveUserVO = {
    user?: UserVO
    patternCount?: number
  }

  type AdminCommentVO = {
    id?: number
    userId?: number
    patternId?: number
    content?: string
    createTime?: string
    likeCount?: number
    replyCount?: number
    userName?: string
    patternName?: string
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
    id?: number
    categoryId?: number
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
    categoryId: number
    title: string
    coverImage?: string
    summary: string
    content: string
    author?: string
    source?: string
    tags?: string[]
    isTop?: number
    isRecommend?: number
  }

  type ArticleCategory = {
    id?: number
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
    id?: number
    categoryName?: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
    createTime?: string
    updateTime?: string
  }

  type ArticleQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    categoryId?: number
    keyword?: string
    tags?: string[]
    status?: string
    auditStatus?: string
    isTop?: number
    isHot?: number
    isRecommend?: number
  }

  type ArticleVO = {
    id?: number
    categoryId?: number
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

  type Banner = {
    id?: number
    title?: string
    imageUrl?: string
    linkUrl?: string
    sortOrder?: number
    status?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
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

  type BaseResponseCollectResult = {
    code?: number
    data?: CollectResult
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

  type BaseResponseImageFusionTask = {
    code?: number
    data?: ImageFusionTask
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

  type BaseResponseListBanner = {
    code?: number
    data?: Banner[]
    message?: string
  }

  type BaseResponseListCommentVO = {
    code?: number
    data?: CommentVO[]
    message?: string
  }

  type BaseResponseListMapStringObject = {
    code?: number
    data?: Record<string, any>[]
    message?: string
  }

  type BaseResponseListQueryTaskHistoryResultVO = {
    code?: number
    data?: QueryTaskHistoryResultVO[]
    message?: string
  }

  type BaseResponseListString = {
    code?: number
    data?: string[]
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

  type BaseResponseMJImagineVO = {
    code?: number
    data?: MJImagineVO
    message?: string
  }

  type BaseResponsePageAdminCommentVO = {
    code?: number
    data?: PageAdminCommentVO
    message?: string
  }

  type BaseResponsePageArticleVO = {
    code?: number
    data?: PageArticleVO
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

  type BaseResponsePageWanQueryVO = {
    code?: number
    data?: PageWanQueryVO
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
    articleId: number
  }

  type CategoryAddRequest = {
    categoryName: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
  }

  type CategoryUpdateRequest = {
    id: number
    categoryName: string
    categoryDesc?: string
    icon?: string
    sortOrder?: number
    status?: number
  }

  type clearCacheParams = {
    patternId?: number
  }

  type collectArticleParams = {
    articleId: number
  }

  type CollectResult = {
    collected?: boolean
    collectCount?: number
  }

  type CommentAddRequest = {
    patternId: number
    content: string
    parentId?: number
  }

  type CommentQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    patternId?: number
    parentId?: number
    userName?: string
    patternName?: string
  }

  type CommentStatisticsVO = {
    totalComments?: number
    mainComments?: number
    totalLikes?: number
  }

  type CommentVO = {
    id?: number
    userId?: number
    patternId?: number
    content?: string
    parentId?: number
    rootId?: number
    replyToUserId?: number
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
    id: number
  }

  type deleteBannerParams = {
    id: number
  }

  type deleteCategoryParams = {
    id: number
  }

  type deleteComment1Params = {
    commentId: number
  }

  type DeleteRequest = {
    id?: number
  }

  type getArticleDetailParams = {
    id: number
  }

  type getBatchLikeStatusParams = {
    patternIds: number[]
  }

  type getCollectStatusParams = {
    articleId: number
  }

  type getCommentDetailParams = {
    commentId: number
  }

  type getCommentRepliesParams = {
    commentId: number
  }

  type getCommentStatisticsParams = {
    patternId: number
  }

  type getHotArticlesParams = {
    limit?: number
  }

  type getLikeStatus1Params = {
    articleId: number
  }

  type getLikeStatusParams = {
    patternId: number
  }

  type getPatternByIdParams = {
    id: number
  }

  type getPatternVOByIdParams = {
    id: number
  }

  type getRecommendArticlesParams = {
    limit?: number
  }

  type getResultsParams = {
    taskId: string
  }

  type getStatusParams = {
    taskId: string
  }

  type getTryOnHistoryParams = {
    userId: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type HomeStatisticsVO = {
    styleDistribution?: Record<string, any>
    activeUsers?: ActiveUserVO[]
    trendData?: TrendDataVO[]
    totalPatterns?: number
    totalUsers?: number
  }

  type ImageFusionTask = {
    id?: number
    userId?: number
    dashscopeTaskId?: string
    prompt?: string
    negativePrompt?: string
    imageUrls?: string
    parameters?: string
    taskStatus?: string
    submitTime?: string
    scheduledTime?: string
    endTime?: string
    errorCode?: string
    errorMessage?: string
    origPrompts?: string
    tempImageUrls?: string
    localImageUrls?: string
    sorts?: string
    createTime?: string
    updateTime?: string
    tempImageUrlList?: string[]
    localImageUrlList?: string[]
    origPromptList?: string[]
    sortList?: number[]
  }

  type likeArticleParams = {
    articleId: number
  }

  type LikeResultVO = {
    isLiked?: boolean
    likeCount?: number
  }

  type listedArticleParams = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
  }

  type MJActionRequest = {
    taskId?: string
    imageId?: string
    action?: string
  }

  type MJBlendRequest = {
    imageUrls?: string[]
    action?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type MJImagineRequest = {
    prompt?: string
    action?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type MJImagineVO = {
    imageUrl?: string
    imageWidth?: number
    imageHeight?: number
    actions?: string[]
    rawImageUrl?: string
    rawImageWidth?: number
    rawImageHeight?: number
    subImageUrls?: string[]
    progress?: number
    imageId?: string
    taskId?: string
    success?: boolean
    traceId?: string
    patternName?: string
    prompt?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type offlineArticleParams = {
    id: number
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type Output = {
    taskId?: string
    taskStatus?: string
    submitTime?: string
    scheduledTime?: string
    endTime?: string
    results?: Result[]
    code?: string
    message?: string
    taskMetrics?: TaskMetrics
  }

  type PageAdminCommentVO = {
    records?: AdminCommentVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageAdminCommentVO
    searchCount?: PageAdminCommentVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type PageArticleVO = {
    records?: ArticleVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageArticleVO
    searchCount?: PageArticleVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
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

  type PageWanQueryVO = {
    records?: WanQueryVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageWanQueryVO
    searchCount?: PageWanQueryVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type Pattern = {
    id?: number
    userId?: number
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
    auditorId?: number
    rejectReason?: string
    likeCount?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type PatternAuditRequest = {
    id?: number
    auditStatus?: string
    rejectReason?: string
  }

  type PatternEditRequest = {
    id?: number
    patternName?: string
    description?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type PatternGenerateRequest = {
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
    id?: number
    userId?: number
    patternName?: string
    generationType?: string
    style?: string
    season?: string
    targetAudience?: string
    auditStatus?: string
  }

  type PatternUpdateRequest = {
    id?: number
    patternName?: string
    description?: string
    style?: string
    season?: string
    targetAudience?: string
  }

  type PatternVO = {
    id?: number
    userId?: number
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
    id: number
  }

  type queryStatusParams = {
    taskId: string
  }

  type QueryTaskHistoryResultVO = {
    id?: number
    localImageUrl?: string
    submitTime?: string
    endTime?: string
  }

  type Result = {
    origPrompt?: string
    url?: string
    code?: string
    message?: string
  }

  type saveSelectedImageParams = {
    taskId: string
    imageUrl: string
  }

  type searchArticlesParams = {
    keyword: string
    pageNum?: number
    pageSize?: number
  }

  type SseEmitter = {
    timeout?: number
  }

  type submitParams = {
    personImageUrl: string
    topGarmentUrl?: string
    bottomGarmentUrl?: string
  }

  type submitTaskParams = {
    userId: number
    imageUrls: string
    parameters?: string
  }

  type TaskMetrics = {
    total?: number
    succeeded?: number
    failed?: number
  }

  type toggleCommentLikeParams = {
    commentId: number
  }

  type toggleLikeParams = {
    patternId: number
  }

  type TrendDataVO = {
    date?: string
    count?: number
  }

  type TryOnTask = {
    id?: number
    userId?: number
    personImageUrl?: string
    topGarmentUrl?: string
    bottomGarmentUrl?: string
    dashscopeTaskId?: string
    taskStatus?: string
    resultImageUrl?: string
    createTime?: string
    updateTime?: string
    submitTime?: string
    scheduledTime?: string
    endTime?: string
    errorCode?: string
    errorMessage?: string
    localResultUrl?: string
  }

  type Usage = {
    imageCount?: number
  }

  type User = {
    id?: number
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
    id?: number
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

  type UserUpdateMyRequest = {
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }

  type WanQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userId?: number
    taskStatus?: string
    dashscopeTaskId?: string
    promptKeyword?: string
    startSubmitTime?: string
    endSubmitTime?: string
    startEndTime?: string
    endEndTime?: string
    errorCode?: string
  }

  type WanQueryVO = {
    requestId?: string
    output?: Output
    usage?: Usage
  }
}
