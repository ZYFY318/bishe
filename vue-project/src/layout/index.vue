<template>
    <div class="layout_container">
        <!-- 左侧菜单 -->
        <div class="layout_slider">
            <Logo></Logo>
        </div>
        <!-- 主内容区域 -->
        <div class="layout_content">
            <!-- 顶部导航 -->
            <div class="layout_tabbar">
                <!-- <tabbar></tabbar> -->456
            </div>
            <!-- 内容 -->
            <div class="layout_main">
                <!-- <Main></Main> -->789
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import Logo from './logo/index.vue'
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
}

.el-menu {
    border-right: none;
    /* 去除右侧边框 */
}
</style>