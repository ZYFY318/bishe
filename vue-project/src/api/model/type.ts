// 模型项的接口
export interface ModelItem {
  imageUrl: string;
  name: string;
  description: string;
  glbPath: string;
}

// 模型列表的响应数据接口
export interface ModelListResponse {
  code: number;
  data: ModelItem[];
}

// 单个模型的响应数据接口
export interface ModelResponse {
  code: number;
  data: ModelItem;
}

// 模型请求参数接口
export interface ModelQueryParams {
  page?: number;
  limit?: number;
  keyword?: string;
}

// 模型上传请求参数
export interface ModelUploadData {
  name: string;
  description: string;
  file: File;
}

// 模型上传响应
export interface ModelUploadResponse {
  code: number;
  data: {
    message: string;
    modelPath: string;
  }
} 