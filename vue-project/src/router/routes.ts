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
  //   {
  //     path: '/screen',
  //     component: () => import('@/views/screen/index.vue'),
  //     name: 'Screen',
  //     meta: {
  //       hidden: false,
  //       title: '数据大屏',
  //       icon: 'Platform',
  //     },
  //   },
  //   {
  //     path: '/acl',
  //     component: () => import('@/layout/index.vue'),
  //     name: 'Acl',
  //     meta: {
  //       hidden: false,
  //       title: '权限管理',
  //       icon: 'Lock',
  //     },
  //     redirect: '/acl/user',
  //     children: [
  //       {
  //         path: '/acl/user',
  //         component: () => import('@/views/acl/user/index.vue'),
  //         name: 'User',
  //         meta: {
  //           hidden: false,
  //           title: '用户管理',
  //           icon: 'User',
  //         },
  //       },
  //       {
  //         path: '/acl/role',
  //         component: () => import('@/views/acl/role/index.vue'),
  //         name: 'Role',
  //         meta: {
  //           hidden: false,
  //           title: '角色管理',
  //           icon: 'UserFilled',
  //         },
  //       },
  //       {
  //         path: '/acl/permission',
  //         component: () => import('@/views/acl/permission/index.vue'),
  //         name: 'Permission',
  //         meta: {
  //           hidden: false,
  //           title: '菜单管理',
  //           icon: 'Monitor',
  //         },
  //       },
  //     ],
  //   },
  //   {
  //     path: '/product',
  //     component: () => import('@/layout/index.vue'),
  //     name: 'Product',
  //     meta: {
  //       hidden: false,
  //       title: '商品管理',
  //       icon: 'Goods',
  //     },
  //     redirect: '/product/trademark',
  //     children: [
  //       {
  //         path: '/product/trademark',
  //         component: () => import('@/views/product/trademark/index.vue'),
  //         name: 'Trademark',
  //         meta: {
  //           hidden: false,
  //           title: '品牌管理',
  //           icon: 'ShoppingCartFull',
  //         },
  //       },
  //       {
  //         path: '/product/attr',
  //         component: () => import('@/views/product/attr/index.vue'),
  //         name: 'Attr',
  //         meta: {
  //           hidden: false,
  //           title: '属性管理',
  //           icon: 'ChromeFilled',
  //         },
  //       },
  //       {
  //         path: '/product/spu',
  //         component: () => import('@/views/product/spu/index.vue'),
  //         name: 'Spu',
  //         meta: {
  //           hidden: false,
  //           title: 'SPU管理',
  //           icon: 'Calendar',
  //         },
  //       },
  //       {
  //         path: '/product/sku',
  //         component: () => import('@/views/product/sku/index.vue'),
  //         name: 'Sku',
  //         meta: {
  //           hidden: false,
  //           title: 'SKU管理',
  //           icon: 'Orange',
  //         },
  //       },
  //     ],
  //   },
];
