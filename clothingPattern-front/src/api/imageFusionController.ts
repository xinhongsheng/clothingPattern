// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /api/image-fusion/results/${param0} */
export async function getResults(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getResultsParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.BaseResponseListString>(`/image-fusion/results/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /api/image-fusion/status/${param0} */
export async function queryStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryStatusParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.BaseResponseImageFusionTask>(`/image-fusion/status/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /api/image-fusion/submit */
export async function submitTask(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.submitTaskParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/image-fusion/submit', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /api/image-fusion/upload */
export async function uploadImage(formData: FormData, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/image-fusion/upload', {
    method: 'POST',
    data: formData,
    ...(options || {}),
  })
}
