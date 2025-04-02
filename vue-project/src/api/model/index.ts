//统一管理模型相关接口
import request from "@/utils/request";
import type { ModelListResponse, ModelQueryParams, ModelResponse, ModelUploadData, ModelUploadResponse } from "./type";

// API 枚举
enum API {
  GET_MODEL_LIST = "/model",
  GET_MODEL_DETAIL = "/model/detail",
  UPLOAD_MODEL = "/model/upload"
}

// 获取模型列表
export const reqModelList = (params?: ModelQueryParams) =>
  request.get<any, ModelListResponse>(API.GET_MODEL_LIST, { params });

// 获取单个模型详情
export const reqModelDetail = (id: string) =>
  request.get<any, ModelResponse>(`${API.GET_MODEL_DETAIL}/${id}`);

// 上传模型
export const uploadModel = (data: ModelUploadData) => {
  const formData = new FormData();
  formData.append('name', data.name);
  formData.append('description', data.description);
  formData.append('file', data.file);
  
  return request.post<any, ModelUploadResponse>(API.UPLOAD_MODEL, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};


