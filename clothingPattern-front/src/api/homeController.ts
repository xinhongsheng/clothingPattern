// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /home/article/topOne */
export async function getArticleTopOne(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/article/topOne', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /home/data/export */
export async function exportDataReport(
  body: API.DataExportRequest,
  options?: { [key: string]: any }
) {
  return request<any>('/home/data/export', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/interaction */
export async function getInteraction(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/interaction', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/pattern/count */
export async function getPatternCount(options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/home/pattern/count', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/province/userCount */
export async function getProvinceUserCount(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/province/userCount', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/statistics */
export async function getHomeStatistics(options?: { [key: string]: any }) {
  return request<API.BaseResponseHomeStatisticsVO>('/home/statistics', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/style/hot */
export async function getHotStyleTopFive(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/style/hot', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/style/preference */
export async function getStylePreference(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/style/preference', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/target/topFive */
export async function getTargetAudienceTopFive(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/target/topFive', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/user/count */
export async function getUserCount(options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/home/user/count', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /home/user/growth */
export async function getUserGrowth(options?: { [key: string]: any }) {
  return request<API.BaseResponseListMapStringObject>('/home/user/growth', {
    method: 'GET',
    ...(options || {}),
  })
}
