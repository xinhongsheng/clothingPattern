// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 执行动作 POST /mj/action */
export async function executeAction(body: API.MJActionRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseMJImagineVO>('/mj/action', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** Blend垫图/混合 POST /mj/blend */
export async function blend(body: API.MJBlendRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseMJImagineVO>('/mj/blend', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** AI扩写提示词 GET /mj/expand */
export async function expandPrompt(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.expandPromptParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/mj/expand', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 生成图片（不保存） POST /mj/imagine */
export async function imagine(body: API.MJImagineRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseMJImagineVO>('/mj/imagine', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}


/** 保存图片到数据库 POST /mj/save */
export async function savePattern(body: API.MJImagineVO, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/mj/save', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
