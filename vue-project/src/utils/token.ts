//封装本地存储存储数据和读取数据方法

export const SET_TOKEN = (token: string) => {
  localStorage.setItem("TOKEN", token as string);
};

export const GET_TOKEN = () => {
  return localStorage.getItem("TOKEN"); //用户唯一标识token
};

//本地存储删除数据方法
export const REMOVE_TOKEN = () => {
  localStorage.removeItem("TOKEN");
};
