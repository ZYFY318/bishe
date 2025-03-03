//统一管理用户相关接口
import request from "@/utils/request";
import type { loginForm, loginResponseData, userResponseData } from "./type";
import { GET_TOKEN } from "@/utils/token";
enum API {
  LOGIN_URL = "/user/login",
  USERINFO_URL = "/user/info",
}

export const reqLogin = (data: loginForm) =>
  request.post<any, loginResponseData>(API.LOGIN_URL, data);

export const reqUserInfo = () =>
  request.get<any, userResponseData>(API.USERINFO_URL, {
    headers: {
      token: GET_TOKEN(),
    },
  });
