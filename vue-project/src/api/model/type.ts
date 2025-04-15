// 模型项的接口
export interface ModelItem {
  id: number;
  name: string;
  description: string;
  created_at: string;
  coverUrl?: string;
  creatorId?: number;
  creatorName?: string;
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
  message?: string;
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
  cover?: File;
  creatorId?: number;
}

// 模型上传响应
export interface ModelUploadResponse {
  code: number;
  data: ModelItem;
} 