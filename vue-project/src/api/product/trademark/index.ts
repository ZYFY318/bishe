import request from "@/utils/request";
import type { Question } from "./type";
//题目模块管理接口
enum API {
  GET_QUESTION_URL = "/question/page/",
  ADD_QUESTION_URL = "/question",
  UPDATE_QUESTION_URL = "/question",
  DELETE_QUESTION_URL = "/question",
  GET_RANDOM_URL = "/question/random/",
}

export const reqAllQuestion = () => request.get(API.GET_QUESTION_URL);

export const reqRandomQuestion = (num: number) =>
  request.get<any>(API.GET_RANDOM_URL + `${num}`);

export const reqQuestion = (page: number, limit: number) =>
  request.get<any, any>(API.GET_QUESTION_URL + `${page}/${limit}`);

export const addQuestion = (questionData: Question) =>
  request.post(API.ADD_QUESTION_URL, questionData);

export const updateQuestion = (questionData: Question, id: number) =>
  request.put(API.UPDATE_QUESTION_URL + `/${id}`, questionData);

export const deleteQuestion = (id: number) =>
  request.delete(API.UPDATE_QUESTION_URL + `/${id}`);
