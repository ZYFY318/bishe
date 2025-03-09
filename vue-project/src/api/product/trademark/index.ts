import request from "@/utils/request";

//题目模块管理接口
enum API {
  GET_QUESTION_URL = "/question/page/",
  ADD_QUESTION_URL = "/question",
}

export const reqQuestion = (page: number, limit: number) =>
  request.get<any, any>(API.GET_QUESTION_URL + `${page}/${limit}`);

export const addQuestion = (questionData: any) => {
  request.post(API.ADD_QUESTION_URL, questionData);
};
