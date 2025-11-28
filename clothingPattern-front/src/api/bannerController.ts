// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /banner/add */
export async function addBanner(body: API.Banner, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/banner/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /banner/delete/${param0} */
export async function deleteBanner(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteBannerParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/banner/delete/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /banner/list */
export async function getBannerList(options?: { [key: string]: any }) {
  return request<API.BaseResponseListBanner>('/banner/list', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /banner/update */
export async function updateBanner(body: API.Banner, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/banner/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /banner/upload */
export async function uploadBanner(body: {}, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/banner/upload', {
    method: 'POST',
    // headers: {
    //   'Content-Type': 'application/json',
    // },
    data: body,
    ...(options || {}),
  })
}
