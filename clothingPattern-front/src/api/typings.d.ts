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

  type BaseResponseAiAnswerVO = {
    code?: number
    data?: AiAnswerVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseHomeStatisticsVO = {
    code?: number
    data?: HomeStatisticsVO
    message?: string
  }

  type BaseResponseLikeResultVO = {
    code?: number
    data?: LikeResultVO
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

  type checkLikedParams = {
    patternId: number
  }

  type DataExportRequest = {
    format?: string
    startDate?: string
    endDate?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type getLikeCountParams = {
    patternId: number
  }

  type getPatternByIdParams = {
    id: number
  }

  type getPatternVOByIdParams = {
    id: number
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

  type LikeResultVO = {
    isLiked?: boolean
    likeCount?: number
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

  type SseEmitter = {
    timeout?: number
  }

  type toggleLikeParams = {
    patternId: number
  }

  type TrendDataVO = {
    date?: string
    count?: number
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
}
