import request from '@/utils/request';

// 类型定义
// 考试基本信息类型
export interface ExamBasicInfo {
  id: number;
  title: string;
  createdAt: string;
  duration: number;
  questionCount: number;
  totalScore: number;
  participantCount: number;
  avgScore: number;
  passingCount: number;
  creatorId: string;
  creatorName: string;
}

// 成绩区间分布
export interface ScoreDistribution {
  range: string;
  count: number;
  percentage: number;
}

// 题目难度分析
export interface QuestionDifficulty {
  questionId: number;
  questionType: string;
  description: string;
  correctRate: number;
  averageTime: number; // 平均用时(秒)
}

// 学生考试结果
export interface StudentResult {
  studentId: string;
  studentName: string;
  score: number;
  completeTime: number; // 完成时间(秒)
  submitTime: string;
  startTime: string;
  isPassed: boolean;
  answeredCount: number;
  correctCount: number;
}

// 学生答题详情
export interface StudentAnswerDetail {
  questionId: number;
  questionType: string;
  questionContent: string;
  studentAnswer: string;
  correctAnswer: string;
  isCorrect: boolean;
  score: number;
  timeSpent: number; // 花费时间(秒)
}

// 考试分析数据响应
export interface ExamAnalysisResponse {
  code: number;
  message: string;
  data: {
    examInfo: ExamBasicInfo;
    scoreDistribution: ScoreDistribution[];
  };
}

// 学生成绩列表响应
export interface StudentResultsResponse {
  code: number;
  message: string;
  data: StudentResult[];
}

// 题目难度分析响应
export interface QuestionDifficultyResponse {
  code: number;
  message: string;
  data: QuestionDifficulty[];
}

// 学生答题详情响应
export interface StudentDetailResponse {
  code: number;
  message: string;
  data: {
    basicInfo: StudentResult;
    answerDetails: StudentAnswerDetail[];
  };
}

// API路径枚举
enum API {
  GET_EXAM_ANALYSIS = "/exam/analysis/",
  GET_STUDENT_RESULTS = "/exam/student-results/",
  GET_QUESTION_DIFFICULTY = "/exam/question-difficulty/",
  GET_STUDENT_EXAM_DETAIL = "/exam/student-detail/",
  EXPORT_EXAM_RESULTS = "/exam/export/"
}

// 获取考试基本信息和分析数据
export const getExamAnalysisData = (examId: number | string) => {
  return request.get<any, ExamAnalysisResponse>(`${API.GET_EXAM_ANALYSIS}${examId}`);
};

// 获取参与该考试的学生成绩列表
export const getStudentResults = (examId: number | string) => {
  return request.get<any, StudentResultsResponse>(`${API.GET_STUDENT_RESULTS}${examId}`);
};

// 获取题目难度分析
export const getQuestionDifficulty = (examId: number | string) => {
  return request.get<any, QuestionDifficultyResponse>(`${API.GET_QUESTION_DIFFICULTY}${examId}`);
};

// 获取单个学生的详细考试情况
export const getStudentExamDetail = (examId: number | string, studentId: string) => {
  return request.get<any, StudentDetailResponse>(`${API.GET_STUDENT_EXAM_DETAIL}${examId}/${studentId}`);
};

// 导出考试成绩Excel
export const exportExamResults = (examId: number | string) => {
  return request.get<any, Blob>(`${API.EXPORT_EXAM_RESULTS}${examId}`, {
    responseType: 'blob'
  });
}; 