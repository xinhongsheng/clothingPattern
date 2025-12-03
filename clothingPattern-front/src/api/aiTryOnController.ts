// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /try-on/status/${param0} */
export async function getStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getStatusParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.TryOnTask>(`/try-on/status/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /try-on/submit */
export async function submit(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.submitParams,
  options?: { [key: string]: any }
) {
  return request<string>('/try-on/submit', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /try-on/upload */
export async function upload(body: FormData, options?: { [key: string]: any }) {
  return request<string>('/try-on/upload', {
    method: 'POST',
    // 交给浏览器自动设置 multipart/form-data 边界，不要强制指定 Content-Type
    data: body,
    ...(options || {}),
  })
}
