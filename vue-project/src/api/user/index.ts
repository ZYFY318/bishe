//统一管理用户相关接口
import request from "@/utils/request";
import type { loginForm, loginResponseData, userResponseData, registerForm } from "./type";
import { GET_TOKEN } from "@/utils/token";
enum API {
  LOGIN_URL = "/user/login",
  USERINFO_URL = "/user/info",
  LOGOUT_URL = "/user/logout",
  REGISTER_URL = "/user/register",
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
