// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /comment/add */
export async function addComment(body: API.CommentAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseCommentVO>('/comment/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /comment/admin/delete */
export async function deleteComment(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/comment/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /comment/delete/${param0} */
export async function deleteCommentByUser(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteCommentByUserParams,
  options?: { [key: string]: any }
) {
  const { commentId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/comment/delete/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /comment/get/${param0} */
export async function getCommentDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCommentDetailParams,
  options?: { [key: string]: any }
) {
  const { commentId: param0, ...queryParams } = params
  return request<API.BaseResponseCommentVO>(`/comment/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /comment/like/${param0} */
export async function toggleCommentLike(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.toggleCommentLikeParams,
  options?: { [key: string]: any }
) {
  const { commentId: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/comment/like/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /comment/list */
export async function getPatternComments(
  body: API.CommentQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageResultCommentVO>('/comment/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /comment/list/page/vo */
export async function listAdminCommentVoByPage(
  body: API.CommentQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAdminCommentVO>('/comment/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /comment/replies/${param0} */
export async function getCommentReplies(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCommentRepliesParams,
  options?: { [key: string]: any }
) {
  const { commentId: param0, ...queryParams } = params
  return request<API.BaseResponseListCommentVO>(`/comment/replies/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /comment/statistics/${param0} */
export async function getCommentStatistics(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getCommentStatisticsParams,
  options?: { [key: string]: any }
) {
  const { patternId: param0, ...queryParams } = params
  return request<API.BaseResponseCommentStatisticsVO>(`/comment/statistics/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
