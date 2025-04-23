// 统一管理考试统计相关接口
import request from "@/utils/request";
import type { ExamStatisticsResponse, ScoreDistributionResponse, QuestionAnalysisResponse } from "./type";

// 通用响应包装类型
interface ResponseWrapper<T> {
  code: number;
  success: boolean;
  message: string;
  data: T;
}

// API 枚举
enum API {
  GET_ALL_STATISTICS = "/exam-statistics",
  GET_EXAM_STATISTICS = "/exam-statistics/exam", // /{examId} 获取特定考试的统计数据
  GET_SCORE_DISTRIBUTION = "/exam-statistics/score-distribution", // /{examId} 获取考试分数分布
  UPDATE_STATISTICS = "/exam-statistics/update", // /{examId} 更新考试统计数据
  DELETE_STATISTICS = "/exam-statistics", // /{id} 删除统计数据
  
  GET_QUESTION_ANALYSIS = "/question-analysis/exam", // /{examId} 获取考试的题目分析
  GET_SPECIFIC_QUESTION_ANALYSIS = "/question-analysis/exam", // /{examId}/question/{questionId} 获取特定题目分析
  UPDATE_QUESTION_ANALYSIS = "/question-analysis/update", // /{examId} 更新题目分析
  DELETE_QUESTION_ANALYSIS = "/question-analysis", // /{id} 删除题目分析
  DELETE_EXAM_QUESTION_ANALYSIS = "/question-analysis/exam" // /{examId} 删除考试的所有题目分析
}

// 获取所有考试统计数据
export const reqAllStatistics = () =>
  request.get<any, ResponseWrapper<ExamStatisticsResponse[]>>(API.GET_ALL_STATISTICS);

// 获取特定考试的统计数据
export const reqExamStatistics = (examId: number) =>
  request.get<any, ResponseWrapper<ExamStatisticsResponse>>(`${API.GET_EXAM_STATISTICS}/${examId}`);

// 获取考试分数分布
export const reqScoreDistribution = (examId: number) =>
  request.get<any, ResponseWrapper<ScoreDistributionResponse>>(`${API.GET_SCORE_DISTRIBUTION}/${examId}`);

// 更新考试统计数据
export const updateExamStatistics = (examId: number) =>
  request.post<any, ResponseWrapper<ExamStatisticsResponse>>(`${API.UPDATE_STATISTICS}/${examId}`);

// 删除考试统计数据
export const deleteStatistics = (id: number) =>
  request.delete<any, ResponseWrapper<void>>(`${API.DELETE_STATISTICS}/${id}`);
  
// 获取考试的题目分析
export const reqQuestionAnalysis = (examId: number) =>
  request.get<any, ResponseWrapper<QuestionAnalysisResponse[]>>(`${API.GET_QUESTION_ANALYSIS}/${examId}`);

// 获取特定题目的分析
export const reqSpecificQuestionAnalysis = (examId: number, questionId: number) =>
  request.get<any, ResponseWrapper<QuestionAnalysisResponse>>(
    `${API.GET_SPECIFIC_QUESTION_ANALYSIS}/${examId}/question/${questionId}`
  );

// 更新题目分析
export const updateQuestionAnalysis = (examId: number) =>
  request.post<any, ResponseWrapper<QuestionAnalysisResponse[]>>(`${API.UPDATE_QUESTION_ANALYSIS}/${examId}`);

// 删除题目分析
export const deleteQuestionAnalysis = (id: number) =>
  request.delete<any, ResponseWrapper<void>>(`${API.DELETE_QUESTION_ANALYSIS}/${id}`);

// 删除考试的所有题目分析
export const deleteExamQuestionAnalysis = (examId: number) =>
  request.delete<any, ResponseWrapper<void>>(`${API.DELETE_EXAM_QUESTION_ANALYSIS}/${examId}`); 