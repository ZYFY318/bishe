<template>
    <div class="layout_container">
        <!-- 左侧菜单 -->
        <div class="layout_slider animate-left" :class="{ fold: LayOutSettingStore.fold }">
            <div class="logo-container">
                <Logo></Logo>
            </div>
            <!-- 展示菜单 -->
            <!-- 滚动组件 -->
            <el-scrollbar class="scrollbar">
                <!-- 根据路由动态生成路由 -->
                <el-menu 
                    :collapse="LayOutSettingStore.fold" 
                    :default-active="activeMenu" 
                    :background-color="'var(--menu-bg)'"
                    :text-color="'var(--menu-text)'" 
                    :active-text-color="'var(--primary-color)'"
                >
                    <Menu :menuList='userStore.menuRoutes'></Menu>
                </el-menu>
            </el-scrollbar>
        </div>

        <!-- 主内容区域 -->
        <div class="layout_content">
            <!-- 顶部导航 -->
            <div class="layout_tabbar animate-top">
                <tabbar></tabbar>
            </div>
            <!-- 内容 -->
            <div class="layout_main animate-right">
                <Main></Main>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { ref, watch } from 'vue';
import Logo from './logo/index.vue'
import Menu from './menu/index.vue'
//获取用户相关的小仓库
import useUserStore from '@/stores/modules/user';
import Main from './main/index.vue'
import Tabbar from './tabbar/index.vue'
import useLayOutSettingStore from '@/stores/modules/setting';

const LayOutSettingStore = useLayOutSettingStore();
//获取路由对象
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 当前激活的菜单项
const activeMenu = ref(route.path);

// 监听路由变化
watch(() => route.path, (newPath) => {
  // 更新当前激活的菜单项
  activeMenu.value = newPath;
}, { immediate: true });
</script>

<style lang="scss" scoped>
.layout_container {
    display: flex;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background: var(--bg-color);
    background-size: cover;
}

.layout_slider {
    justify-content: center; /* 水平居中 */
    width: 18vw; // Set the width to 20% of the viewport width
    background-color: var(--menu-bg);
    border-radius: 10px; /* Adjust the value for desired roundness */
    height: calc(100vh - 40px); /* Adjust the value for desired top and bottom spacing */
    margin: 20px 0 20px 20px; /* Adjust the values for desired spacing */

    .scrollbar {
        width: 100%;
        height: calc(100vh - $base-menu-logo-height);
    }

    &.fold {
        width: 5vw; // Adjust the width when the menu is folded
    }
}

.layout_content {
    width: 100%; // Adjust width to account for menu width and left margin
    height: calc(100vh - 20px); // Adjust height to fit the screen with some margin
    margin: 20px 0 20px 0; // Align top with menu
    padding: 0px; // Add padding if needed
    // overflow: auto; // Ensure content is scrollable if it overflows
    // background-color: aqua;
}

.layout_tabbar {
    height: $base-tabbar-height; // 顶部导航高度
}

.layout_main {
    flex: 1; // 占用剩余空间
    padding: 20px;
    // overflow: auto
    vertical-align: middle;
    justify-content: center;
}

.el-menu {
    border-right: none;
    /* 去除右侧边框 */
}

/* 添加动画关键帧 */
@keyframes layoutSlideFromLeft {
    from {
        transform: translateX(-50px);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@keyframes layoutSlideFromTop {
    from {
        transform: translateY(-30px);
        opacity: 0;
    }
    to {
        transform: translateY(0);
        opacity: 1;
    }
}

@keyframes layoutSlideFromRight {
    from {
        transform: translateX(50px);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

/* 应用动画的类 - 更具体的选择器 */
.layout_container .animate-left {
    animation: layoutSlideFromLeft 0.8s ease forwards;
    opacity: 0;
}

.layout_container .animate-top {
    animation: layoutSlideFromTop 0.8s ease forwards;
    animation-delay: 0.3s;
    opacity: 0;
}

.layout_container .animate-right {
    animation: layoutSlideFromRight 0.8s ease forwards;
    animation-delay: 0.6s;
    opacity: 0;
}

/* 确保动画结束后保持状态 */
.layout_container .animate-left, 
.layout_container .animate-top, 
.layout_container .animate-right {
    animation-fill-mode: forwards;
}

/* 添加过渡效果 */
.layout_slider {
    transition: width 0.3s ease;
    /* 其他现有样式保持不变 */
}

.layout_tabbar {
    transition: all 0.3s ease;
    /* 其他现有样式保持不变 */
}

.layout_main {
    transition: all 0.3s ease;
    /* 其他现有样式保持不变 */
}

</style>
