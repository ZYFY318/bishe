// 统一管理试卷相关接口
import request from "@/utils/request";
import type { 
  ExamListResponse, 
  ExamResponse, 
  ExamCreateData, 
  ExamCreateResponse,
  ExamUpdateData,
  ExamQuestionsResponse,
  AddQuestionsToExamRequest
} from "./type";

// API 枚举
enum API {
  GET_EXAM_LIST = "/exams",
  GET_TEACHER_EXAMS = "/exams/teacher",
  GET_PUBLISHED_EXAMS = "/exams/published",
  GET_EXAM_DETAIL = "/exams",
  CREATE_EXAM = "/exams/create",
  UPDATE_EXAM = "/exams",
  GET_EXAM_QUESTIONS = "/exams/questions",
  ADD_QUESTIONS_TO_EXAM = "/exams/questions",
  PUBLISH_EXAM = "/exams/publish",
  DELETE_EXAM = "/exams"
}

// 获取试卷列表
export const fetchExams = () =>
  request.get<any, ExamListResponse>(API.GET_EXAM_LIST);

// 获取特定教师创建的试卷
export const fetchTeacherExams = (teacherId: number) =>
  request.get<any, ExamListResponse>(`${API.GET_TEACHER_EXAMS}/${teacherId}`);

// 获取试卷详情
export const fetchExamDetail = (id: number) =>
  request.get<any, ExamResponse>(`${API.GET_EXAM_DETAIL}/${id}`);

// 创建试卷
export const createExam = (data: ExamCreateData) => {
  const url = `${API.CREATE_EXAM}?title=${encodeURIComponent(data.title)}&duration=${data.duration}&creatorId=${data.creatorId}`;
  
  // 如果有封面文件，使用FormData
  if (data.cover) {
    const formData = new FormData();
    formData.append('cover', data.cover);
    
    return request.post<any, ExamCreateResponse>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
  
  // 没有封面文件时直接发送
  return request.post<any, ExamCreateResponse>(url);
};

// 更新试卷信息
export const updateExam = (id: number, data: ExamUpdateData) => {
  const formData = new FormData();
  formData.append('title', data.title);
  formData.append('duration', data.duration.toString());
  
  if (data.cover) {
    formData.append('cover', data.cover);
  }
  
  return request.put<any, ExamResponse>(`${API.UPDATE_EXAM}/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

// 获取试卷中的题目
export const getExamQuestions = (examId: number) =>
  request.get<any, ExamQuestionsResponse>(`${API.GET_EXAM_QUESTIONS}/${examId}`);

// 添加题目到试卷
export const addQuestionsToExam = (examId: number, data: AddQuestionsToExamRequest) =>
  request.post<any, ExamResponse>(`${API.ADD_QUESTIONS_TO_EXAM}/${examId}`, data);

// 发布试卷
export const publishExam = (examId: number, published: boolean = true) =>
  request.put<any, ExamResponse>(`${API.PUBLISH_EXAM}/${examId}`, { published });

// 获取已发布的试卷（适用于学生）
export const fetchPublishedExams = () =>
  request.get<any, ExamListResponse>(API.GET_PUBLISHED_EXAMS);

// 删除试卷
export const deleteExam = (examId: number) =>
  request.delete<any, ExamResponse>(`${API.DELETE_EXAM}/${examId}`); 