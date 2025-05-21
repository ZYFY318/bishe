// 课程项接口
export interface CourseItem {
  id: number;
  title: string;
  duration: number;
  coverUrl?: string;
  creatorId: number;
}

// 课程列表响应数据接口
export interface CourseListResponse {
  code: number;
  data: CourseItem[];
  message?: string;
}

// 单个课程响应数据接口
export interface CourseResponse {
  code: number;
  data: CourseItem;
  message?: string;
}

// 课程创建请求参数
export interface CourseCreateData {
  title: string;
  duration: number;
  creatorId: number;
  cover?: File;
}

// 课程创建响应
export interface CourseCreateResponse {
  code: number;
  data: {
    message?: string;
    course?: CourseItem;
  };
  message?: string;
}

// 课程更新请求参数
export interface CourseUpdateData {
  title?: string;
  duration?: number;
  cover?: File;
}
