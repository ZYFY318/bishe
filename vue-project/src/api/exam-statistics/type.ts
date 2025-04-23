// 定义考试统计API相关的类型

// 考试统计数据响应类型
export interface ExamStatisticsResponse {
  id: number;
  examId: number;
  participantCount: number;
  avgScore: number;
  passingScore: number;
  passingCount: number;
  highestScore: number;
  lowestScore: number;
  totalTime: number;
  avgTime: number;
  createdAt: string;
  updatedAt: string;
}

// 分数分布响应类型
export interface ScoreDistributionResponse {
  examId: number;
  totalParticipants: number;
  sections: number[];     // 分数段划分: [0, 10, 20, 30, 40, ...]
  counts: number[];       // 每个分数段的人数
  percentages: number[];  // 每个分数段的百分比
}

// 题目分析响应类型
export interface QuestionAnalysisResponse {
  id: number;
  examId: number;
  questionId: number;
  description: string;
  correctRate: number;
  attemptCount: number;
  correctCount: number;
  createdAt: string;
  updatedAt: string;
} 