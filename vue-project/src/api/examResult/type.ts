export interface ExamResult {
  id?: number;
  userId: number;
  examId: number;
  examDate: string; // ISO 格式字符串
  score: number;
  duration: number;
  userAnswers?: string; // JSON 字符串
  questionCount: number; // 试卷题目数量
}

// 统一的 API 响应格式
export interface ResponseMessage<T> {
  code: number; 
  message: string;
  data: T;
}
