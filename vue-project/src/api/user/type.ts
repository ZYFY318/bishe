//登录接口参数ts类型

export interface loginForm {
  username: string;
  password: string;
}

//登录接口返回的数据类型
interface dataType {
  token?: string;
  message?: string;
}

//获取用户信息接口返回的数据类型
interface userInfo {
  userId: number;
  avatar: string;
  username: string;
  userType: string;
  email?: string;
  roles: string[];
  buttons: string[];
  routes: string[];
  message?: string;
}

export interface loginResponseData {
  code: number;
  data: dataType;
}

export interface userResponseData {
  code: number;
  data: userInfo;
}

// 注册接口参数类型
export interface registerForm {
  username: string;
  password: string;
  userType: string;
  email?: string;
}

// 更新用户信息接口参数类型
export interface updateUserForm {
  userId?: number;
  username?: string;
  avatar?: string;
  email?: string;
  avatarFile?: File;
}
