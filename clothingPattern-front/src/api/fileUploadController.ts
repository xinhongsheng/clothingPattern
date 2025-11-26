
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /file/upload/article-cover */
export async function uploadArticleCover(body: {}, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/file/upload/article-cover', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
