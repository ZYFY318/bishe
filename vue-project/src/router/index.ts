import { createRouter, createWebHashHistory } from "vue-router";
import { constantRoute } from "./routes";
import useUserStore from "@/stores/modules/user";
import { GET_TOKEN } from "@/utils/token";
let router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoute,
  scrollBehavior() {
    return {
      left: 0,
      top: 0,
    };
  },
});

// 添加全局前置守卫
router.beforeEach(async (to, from, next) => {
  const isAuthenticated = !!GET_TOKEN();
  const userStore = useUserStore();
  if (to.meta.requiresAuth && !isAuthenticated) {
    next("/login");
  } else {
    next();
  }
});

export default router;
