import request from "@/utils/request";
import type { ExamResult, ResponseMessage } from "@/types";

// 定义 API 枚举
enum API {
  GET_USER_EXAM_RESULTS = "/exam-results/user/",
  SAVE_EXAM_RESULT = "/exam-results/save",
}

/**
 * 获取指定用户的考试成绩
 * @param userId 用户 ID
 */
export const reqUserExamResults = (userId: number) =>
  request.get<ResponseMessage<ExamResult[]>>(
    API.GET_USER_EXAM_RESULTS + `${userId}`
  );

/**
 * 保存考试成绩
 * @param examResult 考试成绩对象
 */
export const reqSaveExamResult = (examResult: ExamResult) =>
  request.post<ResponseMessage<ExamResult>>(API.SAVE_EXAM_RESULT, examResult);
