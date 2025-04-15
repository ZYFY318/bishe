//统一管理模型相关接口
import request from "@/utils/request";
import type { ModelListResponse, ModelQueryParams, ModelResponse, ModelUploadData, ModelUploadResponse } from "./type";

// API 枚举
enum API {
  GET_MODEL_LIST = "/model",
  GET_MODEL_DATA = "/model",  // /{id}/data 获取模型二进制数据
  UPLOAD_MODEL = "/model/upload",
  DELETE_MODEL = "/model",  // 添加删除模型的API路径
  GET_USER_MODELS = "/model/user" // 获取用户模型列表
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
      console.log("设置creatorId:", data.creatorId.toString());
    }
    
    if (data.cover) {
      formData.append('cover', data.cover);
    }
  } else {
    // 如果已经是FormData，直接使用
    formData = data;
    // 输出FormData的内容进行调试
    console.log("FormData内容:");
    for (const pair of formData.entries()) {
      console.log(pair[0], pair[1]);
    }
  }
  
  return request.post<any, ModelUploadResponse>(API.UPLOAD_MODEL, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

// 删除模型
export const deleteModel = (id: number) =>
  request.delete<any, ModelResponse>(`${API.DELETE_MODEL}/${id}`);

// 获取用户模型列表
export const reqUserModels = (creatorId: number) =>
  request.get<any, ModelListResponse>(`${API.GET_USER_MODELS}/${creatorId}`);


