//统一管理用户相关接口
import request from "@/utils/request";
import type { loginForm, loginResponseData, userResponseData, registerForm, updateUserForm } from "./type";
import { GET_TOKEN } from "@/utils/token";
enum API {
  LOGIN_URL = "/user/login",
  USERINFO_URL = "/user/info",
  LOGOUT_URL = "/user/logout",
  REGISTER_URL = "/user/register",
  UPDATE_USER_URL = "/user/update",
  UPDATE_USER_AVATAR_URL = "/user/update/avatar"
}

export const reqLogin = (data: loginForm) =>
  request.post<any, loginResponseData>(API.LOGIN_URL, data);

export const reqUserInfo = () =>
  request.get<any, userResponseData>(API.USERINFO_URL, {
    headers: {
      token: GET_TOKEN(),
    },
  });

export const reqLogout = () => request.post<any, any>(API.LOGOUT_URL);

// 注册请求
export const reqRegister = (data: registerForm) =>
  request.post<any, any>(API.REGISTER_URL, data);

// 更新用户信息
export const updateUserInfo = (data: updateUserForm) => {
  // 如果包含文件，使用FormData和专用的头像上传接口
  if (data.avatarFile) {
    const formData = new FormData();
    
    if (data.username) {
      formData.append('username', data.username);
    }
    
    if (data.email) {
      formData.append('email', data.email);
    }
    
    // 添加用户ID
    if (data.userId) {
      formData.append('userId', data.userId.toString());
    }
    
    formData.append('avatar', data.avatarFile);
    
    return request.post<any, userResponseData>(API.UPDATE_USER_AVATAR_URL, formData, {
      headers: {
        token: GET_TOKEN(),
        'Content-Type': 'multipart/form-data'
      },
    });
  }
  
  // 否则使用普通JSON请求和常规更新接口
  return request.post<any, userResponseData>(API.UPDATE_USER_URL, data, {
    headers: {
      token: GET_TOKEN(),
    },
  });
}
