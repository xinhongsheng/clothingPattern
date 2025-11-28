// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 执行动作 POST /mj/action */
export async function executeAction(body: API.MJActionRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseMJImagineResponse>('/mj/action', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 生成图片并保存 POST /mj/generate */
export async function generateAndSave(
  body: API.MJImagineRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/mj/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 生成图片（不保存） POST /mj/imagine */
export async function imagine(body: API.MJImagineRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseMJImagineResponse>('/mj/imagine', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 保存图片到数据库 POST /mj/save */
export async function savePattern(body: API.MJImagineResponse, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/mj/save', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 测试接口 GET /mj/test */
export async function test(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/mj/test', {
    method: 'GET',
    ...(options || {}),
  })
}
