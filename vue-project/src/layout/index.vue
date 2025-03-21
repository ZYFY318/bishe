<template>
    <div class="layout_container">
        <!-- 左侧菜单 -->
        <div class="layout_slider" :class="{ fold: LayOutSettingStore.fold }">
            <Logo></Logo>
            <!-- 展示菜单 -->
            <!-- 滚动组件 -->
            <el-scrollbar class="scrollbar">
                <!-- 根据路由动态生成路由 -->
                <el-menu :collapse="LayOutSettingStore.fold" :default-active="$route.path" background-color='#e64980'
                    text-color="white" active-text-color="#fa5252">
                    <Menu :menuList='userStore.menuRoutes'></Menu>
                </el-menu>
            </el-scrollbar>
        </div>

        <!-- 主内容区域 -->
        <div class="layout_content">
            <!-- 顶部导航 -->
            <div class="layout_tabbar">
                <tabbar></tabbar>
            </div>
            <!-- 内容 -->
            <div class="layout_main">
                <Main></Main>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">

import { useRoute } from 'vue-router';
import Logo from './logo/index.vue'
import Menu from './menu/index.vue'
//获取用户相关的小仓库
import useUserStore from '@/stores/modules/user';
import Main from './main/index.vue'
import Tabbar from './tabbar/index.vue'
import useLayOutSettingStore from '@/stores/modules/setting';
const LayOutSettingStore = useLayOutSettingStore();
//获取路由对象
let $router = useRoute();
const userStore = useUserStore();
</script>

<style lang="scss">
.layout_container {
    display: flex;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background-color: red;
}

.layout_slider {

    width: $base-menu-width; // 左侧菜单宽度
    background-color: $base-menu-background;
    transition: all 0.3s;

    .scrollbar {
        width: 100%;
        height: calc(100vh - $base-menu-logo-height);
    }

    &.fold {
        width: $base-menu-min-width;
    }
}

.layout_content {
    display: flex;
    flex-direction: column;
    width: 100%;
}

.layout_tabbar {
    height: $base-tabbar-height; // 顶部导航高度
}

.layout_main {
    flex: 1; // 占用剩余空间
    background-color: greenyellow;
    padding: 20px;
    overflow: auto;
    vertical-align: middle;
    align-items: center;
    justify-content: center;
    height: 100%;
}

.el-menu {
    border-right: none;
    /* 去除右侧边框 */
}
</style>
