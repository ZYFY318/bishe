// 试卷项的接口
export interface ExamItem {
  id: number;
  title: string;
  coverUrl: string;
  duration: number;
  createdAt: string;
  published?: boolean; // 发布状态，true为已发布，false或undefined为未发布
  creatorId?: number; // 创建者ID
  creatorName?: string; // 创建者用户名
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
  creatorId: number;
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

// 试卷更新请求参数
export interface ExamUpdateData {
  title: string;
  duration: number;
  cover?: File;
}

// 题目项接口
export interface QuestionItem {
  id: number;
  title: string;
  options: string[];
  answer: string;
}

// 试卷中题目的响应数据接口
export interface ExamQuestionsResponse {
  code: number;
  data: {
    questions: QuestionItem[];
    total: number;
  };
  message?: string;
}

// 添加题目到试卷的请求参数
export interface AddQuestionsToExamRequest {
  questionIds: number[];
} 