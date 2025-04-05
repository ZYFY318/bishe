export interface ExamResult {
  id?: number;
  userId: number;
  examDate: string; // ISO 格式字符串
  score: number;
  duration: number;
}

// 统一的 API 响应格式
export interface ResponseMessage<T> {
  code: number;
  message: string;
  data: T;
}
