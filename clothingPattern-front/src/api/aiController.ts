// @ts-ignore
/* eslint-disable */
import request from '@/request'

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
