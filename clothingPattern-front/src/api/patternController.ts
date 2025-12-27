// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /pattern/audit */
export async function auditPattern(
  body: API.PatternAuditRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/pattern/audit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/behavior/view */
export async function recordViewBehavior(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.recordViewBehaviorParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/pattern/behavior/view', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/delete */
export async function deletePattern(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/pattern/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/edit */
export async function editPattern(body: API.PatternEditRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/pattern/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/generate */
export async function generatePattern(
  body: API.PatternGenerateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePatternGenerateTaskVO>('/pattern/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /pattern/generate/status/${param0} */
export async function getGenerateStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getGenerateStatusParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.BaseResponsePatternGenerateTaskVO>(`/pattern/generate/status/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /pattern/get */
export async function getPatternById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPatternByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePattern>('/pattern/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /pattern/get/vo */
export async function getPatternVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPatternVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePatternVO>('/pattern/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/list/page */
export async function listPatternByPage(
  body: API.PatternQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePattern>('/pattern/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/list/page/vo */
export async function listPatternVoByPage(
  body: API.PatternQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePatternVO>('/pattern/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/my/list/page/vo */
export async function listMyPatternVoByPage(
  body: API.PatternQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePatternVO>('/pattern/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /pattern/recommend */
export async function getRecommendations(options?: { [key: string]: any }) {
  return request<API.BaseResponseListPatternVO>('/pattern/recommend', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/recommend/refresh */
export async function refreshRecommendations(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/pattern/recommend/refresh', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /pattern/update */
export async function updatePattern(
  body: API.PatternUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/pattern/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
