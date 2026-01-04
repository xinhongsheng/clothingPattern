// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /ai/analyze-image */
export async function analyzeImage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.analyzeImageParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/ai/analyze-image', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/ask */
export async function askQuestion(body: API.AiQuestionRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseAiAnswerVO>('/ai/ask', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/ask/stream */
export async function askQuestionStream(
  body: API.AiQuestionRequest,
  options?: { [key: string]: any }
) {
  return request<API.SseEmitter>('/ai/ask/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /ai/questions */
export async function getCommonQuestions(options?: { [key: string]: any }) {
  return request<API.BaseResponseStringArray>('/ai/questions', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /ai/upload-image */
export async function uploadReferenceImage(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString>('/ai/upload-image', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}
