// 统一管理试卷相关接口
import request from "@/utils/request";
import type { 
  ExamListResponse, 
  ExamResponse, 
  ExamCreateData, 
  ExamCreateResponse 
} from "./type";

// API 枚举
enum API {
  GET_EXAM_LIST = "/exams",
  GET_EXAM_DETAIL = "/exams",
  CREATE_EXAM = "/exams/create"
}

// 获取试卷列表
export const fetchExams = () =>
  request.get<any, ExamListResponse>(API.GET_EXAM_LIST);

// 获取试卷详情
export const fetchExamDetail = (id: number) =>
  request.get<any, ExamResponse>(`${API.GET_EXAM_DETAIL}/${id}`);

// 创建试卷
export const createExam = (data: ExamCreateData) => {
  const formData = new FormData();
  formData.append('title', data.title);
  formData.append('duration', data.duration.toString());
  
  if (data.cover) {
    formData.append('cover', data.cover);
  }
  
  return request.post<any, ExamCreateResponse>(API.CREATE_EXAM, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}; 