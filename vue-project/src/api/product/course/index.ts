// 统一管理课程相关接口
import request from "@/utils/request";
import type {
  CourseListResponse,
  CourseResponse,
  CourseCreateData,
  CourseCreateResponse,
  CourseUpdateData,
  // 假设存在发布课程响应类型，若不存在需添加
  CoursePublishResponse,
} from "./type";

// API 枚举
enum API {
  GET_COURSE_LIST = "/courses",
  GET_COURSE_DETAIL = "/courses",
  CREATE_COURSE = "/courses/create",
  UPDATE_COURSE = "/courses/update",
  DELETE_COURSE = "/courses/delete",
  // 添加发布课程 API
  PUBLISH_COURSE = "/courses/publish",
}

// 获取课程列表
export const fetchCourseList = () =>
  request.get<any, CourseListResponse>(API.GET_COURSE_LIST);

// 获取课程详情
export const fetchCourseDetail = (id: number) =>
  request.get<any, CourseResponse>(`${API.GET_COURSE_DETAIL}${id}`);

// 创建课程
export const createCourse = (data: CourseCreateData) => {
  const url = `${API.CREATE_COURSE}?title=${encodeURIComponent(
    data.title
  )}&duration=${data.duration}&creatorId=${data.creatorId}`;

  // 如果有封面文件，使用 FormData
  if (data.cover) {
    const formData = new FormData();
    formData.append("cover", data.cover);

    return request.post<any, CourseCreateResponse>(url, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  }

  // 没有封面文件时直接发送
  return request.post<any, CourseCreateResponse>(url);
};

// 更新课程信息
export const updateCourse = (id: number, data: CourseUpdateData) => {
  const formData = new FormData();
  if (data.title) formData.append("title", data.title);
  if (data.duration) formData.append("duration", data.duration.toString());
  if (data.cover) formData.append("cover", data.cover);

  return request.put<any, CourseResponse>(
    `${API.UPDATE_COURSE}${id}`,
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );
};

// 删除课程
export const deleteCourse = (id: number) =>
  request.delete<any, CourseResponse>(`${API.DELETE_COURSE}${id}`);

// 发布/取消发布课程
export const publishCourse = (id: number, isPublished: boolean) =>
  request.post<any, CoursePublishResponse>(`${API.PUBLISH_COURSE}${id}`, {
    isPublished,
  });
