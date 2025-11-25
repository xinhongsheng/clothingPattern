// @ts-ignore
/* eslint-disable */
import request from '@/request'

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

/** 此处后端没有提供注释 GET /home/statistics */
export async function getHomeStatistics(options?: { [key: string]: any }) {
  return request<API.BaseResponseHomeStatisticsVO>('/home/statistics', {
    method: 'GET',
    ...(options || {}),
  })
}
