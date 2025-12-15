// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /image-fusion/list/page/vo */
export async function listImageFusionVoByPage(
  body: API.WanQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageWanQueryVO>('/image-fusion/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /image-fusion/results/${param0} */
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

/** 此处后端没有提供注释 POST /image-fusion/save-selected */
export async function saveSelectedImage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.saveSelectedImageParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/image-fusion/save-selected', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /image-fusion/status/${param0} */
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

/** 此处后端没有提供注释 POST /image-fusion/submit */
export async function submitTask(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.submitTaskParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/image-fusion/submit', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /image-fusion/upload */
export async function uploadImage(body: {}, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/image-fusion/upload', {
    method: 'POST',
    // headers: {
    //   'Content-Type': 'application/json',
    // },
    data: body,
    ...(options || {}),
  })
}
