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
    path: "/register",
    component: () => import("@/views/register/index.vue"),
    name: "register",
    meta: {
      title: "注册",
      hidden: true,
      icon: "UserFilled",
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
          roles: ["STUDENT", "TEACHER"],
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
    component: () => import("@/views/profile/index.vue"),
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
      roles: ["TEACHER"],
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
          roles: ["TEACHER"],
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
      roles: ["TEACHER"],
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
          roles: ["TEACHER"],
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
          roles: ["TEACHER"],
        },
      },
    ],
  },
  // 学生的课程学习菜单
  {
    path: "/study",
    component: () => import("@/layout/index.vue"),
    name: "Study",
    meta: {
      hidden: false,
      title: "课程学习",
      icon: "Reading",
      roles: ["STUDENT"],
    },
    children: [
      {
        path: "/study/exam-list",
        component: () => import("@/views/product/exam-list/index.vue"),
        name: "ExamList",
        meta: {
          hidden: false,
          title: "试卷列表",
          icon: "Document",
          roles: ["STUDENT"],
        },
      },
      // 可以添加其他学习相关页面，如模型库等
    ],
  },
  // 将试卷预览和编辑移到这里，作为独立的顶级路由
  {
    path: "/examview",
    component: () => import("@/layout/fullscreen.vue"), // 使用全屏布局
    name: "ExamViewLayout",
    meta: {
      hidden: true,
      title: "nihaoa"
    },
    children: [
      {
        path: "",
        component: () => import("@/views/product/examview/index.vue"),
        name: "ExamView",
        meta: {
          hidden: true,
          title: "试卷预览",
          fullscreen: true,
        },
      },
      {
        path: "edit",
        component: () => import("@/views/product/examview/edit.vue"),
        name: "ExamEdit",
        meta: {
          hidden: true,
          title: "编辑试卷",
          fullscreen: true,
        },
      },
      {
        path: "take",
        component: () => import("@/views/product/exam-take/index.vue"),
        name: "ExamTake",
        meta: {
          hidden: true,
          title: "考试页面",
          fullscreen: true,
          roles: ["STUDENT"],
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
  {
    path: "/product/sku",
    component: () => import("@/views/product/sku/index.vue"),
    name: "Sku",
    meta: {
      hidden: false,
      title: "测试",
      icon: "Orange",
      roles: ["STUDENT"],
    },
  },
];
