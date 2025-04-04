import component from "element-plus/es/components/tree-select/src/tree-select-option.mjs";

export const constantRoute = [
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    name: "login",
    meta: {
      title: "登录",
      hidden: true,
      icon: "Promotion",
    },
  },
  {
    //登陆成功后展示数据的路由
    path: "/",
    component: () => import("@/layout/index.vue"),
    name: "layout",
    meta: {
      title: "",
      hidden: false,
      icon: "",
      requiresAuth: true,
    },
    redirect: "/home",
    children: [
      {
        path: "/home",
        component: () => import("@/views/home/index.vue"),
        meta: {
          title: "首页",
          hidden: false,
          icon: "HomeFilled",
        },
      },
    ],
  },
  {
    path: "/404",
    component: () => import("@/views/404/index.vue"),
    name: "404",
    meta: {
      title: "404",
      hidden: true,
    },
  },
  {
    path: "/:pathMath(.*)*",
    redirect: "/404",
    name: "Any",
    meta: {
      title: "any",
      hidden: true,
    },
  },
  {
    path: "/screen",
    component: () => import("@/views/screen/index.vue"),
    name: "Screen",
    meta: {
      hidden: false,
      title: "个人中心",
      icon: "Platform",
    },
  },
  {
    path: "/acl",
    component: () => import("@/layout/index.vue"),
    name: "Acl",
    meta: {
      hidden: false,
      title: "权限管理",
      icon: "Lock",
    },
    redirect: "/acl/role",
    children: [
      {
        path: "/acl/role",
        component: () => import("@/views/acl/role/index.vue"),
        name: "Role",
        meta: {
          hidden: false,
          title: "角色管理",
          icon: "UserFilled",
        },
      },
    ],
  },
  {
    path: "/product",
    component: () => import("@/layout/index.vue"),
    name: "Product",
    meta: {
      hidden: false,
      title: "教学管理",
      icon: "Goods",
    },
    redirect: "/product/trademark",
    children: [
      {
        path: "/product/trademark",
        component: () => import("@/views/product/trademark/index.vue"),
        name: "Trademark",
        meta: {
          hidden: false,
          title: "题目管理",
          icon: "ShoppingCartFull",
        },
      },
      {
        path: "/product/exam",
        component: () => import("@/views/product/exam/index.vue"),
        name: "Exam",
        meta: {
          hidden: false,
          title: "试卷管理",
          icon: "Files",
        },
      },
      {
        path: "/product/examview",
        component: () => import("@/views/product/examview/index.vue"),
        name: "ExamView",
        meta: {
          hidden: true,
          title: "试卷预览",
        },
      },
      {
        path: "/product/sku",
        component: () => import("@/views/product/sku/index.vue"),
        name: "Sku",
        meta: {
          hidden: false,
          title: "测试",
          icon: "Orange",
        },
      },
    ],
  },
  {
    path: "/showModel",
    component: () => import("@/views/showModel/index.vue"),
    name: "ShowModel",
    meta: {
      hidden: true,
      title: "模型展示",
      icon: "Monitor",
    },
  },
];
