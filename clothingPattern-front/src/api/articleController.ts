// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /article/${param0} */
export async function getArticleDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getArticleDetailParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseArticleVO>(`/article/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/add */
export async function addArticle(body: API.ArticleAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/article/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/collect */
export async function collectArticle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.collectArticleParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/article/collect', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/collect/cancel */
export async function cancelCollectArticle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.cancelCollectArticleParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/article/collect/cancel', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /article/collect/status */
export async function getCollectStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCollectStatusParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/article/collect/status', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/delete/${param0} */
export async function deleteArticle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteArticleParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/article/delete/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /article/hot */
export async function getHotArticles(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getHotArticlesParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListArticleVO>('/article/hot', {
    method: 'GET',
    params: {
      // limit has a default value: 10
      limit: '10',
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/like */
export async function likeArticle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.likeArticleParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLikeResult>('/article/like', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /article/like/status */
export async function getLikeStatus1(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLikeStatus1Params,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/article/like/status', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/list */
export async function getArticleList(
  body: API.ArticleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageResultArticleVO>('/article/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/offline/${param0} */
export async function offlineArticle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.offlineArticleParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/article/offline/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/publish/${param0} */
export async function publishArticle(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.publishArticleParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/article/publish/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /article/recommend */
export async function getRecommendArticles(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getRecommendArticlesParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListArticleVO>('/article/recommend', {
    method: 'GET',
    params: {
      // limit has a default value: 10
      limit: '10',
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /article/search */
export async function searchArticles(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchArticlesParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageResultArticleVO>('/article/search', {
    method: 'GET',
    params: {
      // pageNum has a default value: 1
      pageNum: '1',
      // pageSize has a default value: 10
      pageSize: '10',
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /article/update */
export async function updateArticle(body: API.Article, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/article/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 上传文章封面图片 POST /article/upload/cover */
export async function uploadArticleCover(file: File, options?: { [key: string]: any }) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request<API.BaseResponseString>('/article/upload/cover', {
    method: 'POST',
    data: formData,
    ...(options || {}),
  })
}
