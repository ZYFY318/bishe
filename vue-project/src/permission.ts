import router from "./router";
import nprogress from "nprogress";
import "nprogress/nprogress.css";
import setting from "./setting";
//获取用户相关的小仓库内部token数据，去判断用户是否登陆成功
import useUserStore from "./stores/modules/user";
import { fr } from "element-plus/es/locales.mjs";

//全局前置守卫
router.beforeEach(async (to, from, next) => {
  document.title = setting.title + "-" + to.meta.title;
  const userStore = useUserStore();
  //to:你将要访问的那个路由对象
  //from:你从哪个路由而来
  //next:路由的放行函数
  //访问某一个路由之前会执行
  nprogress.start();
  const token = userStore.token;
  const username = userStore.username;
  if (token) {
    // 已登录用户不能访问登录和注册页面
    if (to.path == "/login" || to.path == "/register") {
      next({ path: "/" });
    } else {
      if (username) {
        next();
      } else {
        //没有用户信息，在守卫这里发请求获取到了用户信息再放行
        try {
          await userStore.userInfo();
          next();
        } catch (error) {
          //token过期
          userStore.userLogout();
          next({ path: "/login" });
        }
      }
    }
  } else {
    // 未登录用户可以访问的路径列表
    const whiteList = ['/login', '/register'];
    if (whiteList.includes(to.path)) {
      next();
    } else {
      // 不再传递redirect参数，直接跳转到登录页
      next({ path: "/login" });
    }
  }
});

// 全局后置守卫
router.afterEach((to, from) => {
  nprogress.done();
});

//用户未登录：可以访问login和register,其余路由不能访问
//用户登录成功：不可以访问login和register，其余的路由可以访问
