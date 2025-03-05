//创建用户相关仓库
import { defineStore } from "pinia";
//引入接口
import { reqLogin, reqUserInfo } from "@/api/user";
//引入数据类型
import type { loginForm, loginResponseData } from "@/api/user/type";
import type { UserState } from "./types/type";
//引入操作本地存储的工具方法
import { SET_TOKEN, GET_TOKEN, REMOVE_TOKEN } from "@/utils/token";
//引入路由
import { constantRoute } from "@/router/routes";
let useUserStore = defineStore("User", {
  //小仓库存储数据地方
  state: (): UserState => {
    return {
      token: GET_TOKEN(),
      menuRoutes: constantRoute,
      username: "",
      avatar: "",
    };
  },
  //异步|逻辑
  actions: {
    //用户登录
    async userLogin(data: loginForm) {
      let result: loginResponseData = await reqLogin(data);
      if (result.code === 200) {
        //pinia仓库存储token
        this.token = result.data.token as string;
        //本地存储持久化存储一份
        SET_TOKEN(result.data.token as string);
        //返回一个成功promise
        return "ok";
      } else {
        return Promise.reject(new Error(result.data.message));
      }
    },
    //获取用户信息
    async userInfo() {
      const result = await reqUserInfo();
      if (result.code === 200) {
        this.username = result.data.username;
        this.avatar = result.data.avatar;
        console.log(this.avatar);
        return "ok";
      } else {
        return Promise.reject("获取用户信息失败");
      }
    },
    //退出登录
    userLogout() {
      this.token = "";
      this.username = "";
      this.avatar = "";
      REMOVE_TOKEN();
    },
  },
  getters: {},
});

//对外暴露或取消仓库的方法
export default useUserStore;
