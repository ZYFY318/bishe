// 试卷项的接口
export interface ExamItem {
  id: number;
  title: string;
  coverUrl: string;
  duration: number;
  createdAt: string;
}

// 试卷列表的响应数据接口
export interface ExamListResponse {
  code: number;
  data: ExamItem[];
  message?: string;
}

// 单个试卷的响应数据接口
export interface ExamResponse {
  code: number;
  data: ExamItem;
  message?: string;
}

// 试卷创建请求参数
export interface ExamCreateData {
  title: string;
  duration: number;
  cover?: File;
}

// 试卷创建响应
export interface ExamCreateResponse {
  code: number;
  data: {
    message?: string;
    exam?: ExamItem;
  };
  message?: string;
} 