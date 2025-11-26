// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /like/batch-status */
export async function getBatchLikeStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getBatchLikeStatusParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseMapLongBoolean>('/like/batch-status', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /like/status */
export async function getLikeStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLikeStatusParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/like/status', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /like/toggle */
export async function toggleLike(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.toggleLikeParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLikeResultVO>('/like/toggle', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
