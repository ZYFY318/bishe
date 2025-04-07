//统一管理模型相关接口
import request from "@/utils/request";
import type { ModelListResponse, ModelQueryParams, ModelResponse, ModelUploadData, ModelUploadResponse } from "./type";

// API 枚举
enum API {
  GET_MODEL_LIST = "/model",
  GET_MODEL_DATA = "/model",  // /{id}/data 获取模型二进制数据
  UPLOAD_MODEL = "/model/upload"
}

// 获取模型列表
export const reqModelList = (params?: ModelQueryParams) =>
  request.get<any, ModelListResponse>(API.GET_MODEL_LIST, { params });

// 获取单个模型详情
export const reqModelDetail = (id: number) =>
  request.get<any, ModelResponse>(`${API.GET_MODEL_DATA}/${id}`);

// 获取模型二进制数据
export const reqModelData = (id: number) =>
  request.get<any, Blob>(`${API.GET_MODEL_DATA}/${id}`);

// 上传模型
export const uploadModel = (data: ModelUploadData | FormData) => {
  let formData: FormData;
  
  if (!(data instanceof FormData)) {
    // 如果是ModelUploadData类型，转换为FormData
    formData = new FormData();
    formData.append('name', data.name);
    formData.append('description', data.description || '');
    formData.append('file', data.file);
    
    // 可选字段
    if (data.creatorId !== undefined) {
      formData.append('creatorId', data.creatorId.toString());
    }
    if (data.image) {
      formData.append('image', data.image);
    }
  } else {
    // 如果已经是FormData，直接使用
    formData = data;
  }
  
  return request.post<any, ModelUploadResponse>(API.UPLOAD_MODEL, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};


