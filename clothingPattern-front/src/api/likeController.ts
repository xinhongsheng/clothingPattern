// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /like/check */
export async function checkLiked(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.checkLikedParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/like/check', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /like/count */
export async function getLikeCount(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLikeCountParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/like/count', {
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
