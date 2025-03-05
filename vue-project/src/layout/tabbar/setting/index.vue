<template>
    <el-button type="primary" size="small" icon="Refresh" circle @click="update"></el-button>
    <el-button type="primary" size="small" icon="FullScreen" circle @click="fullScreen"></el-button>
    <el-button type="primary" size="small" icon="Setting" circle></el-button>
    <img :src="userStore.avatar" style="width: 20px; height: 20px; margin: 0px 10px;">
    <el-dropdown>
        <span class="el-dropdown-link">
            {{ userStore.username }}
            <el-icon class="el-icon--right">
                <arrow-down />
            </el-icon>
        </span>
        <template #dropdown>
            <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
        </template>
    </el-dropdown>
</template>
<script setup lang="ts">
import useLayOutSettingStore from '@/stores/modules/setting';
import useUserStore from '@/stores/modules/user';
import { useRoute, useRouter } from 'vue-router';
const $router = useRouter();
const $route = useRoute();
const LayOutSettingStore = useLayOutSettingStore();
const userStore = useUserStore();
const update = () => {
    LayOutSettingStore.refresh = !LayOutSettingStore.refresh;
}
console.log(userStore.username);
const fullScreen = () => {
    let full = document.fullscreenElement;
    if (!full) {
        document.documentElement.requestFullscreen();
    } else {
        document.exitFullscreen();
    }
}
const logout = () => {
    //1.需要向服务器发送请求[退出登录接口]

    //2.仓库关于用户相关数据得清空掉
    userStore.userLogout();
    //3.跳转到登陆页面  
    $router.push({ path: '/login', query: { redirect: $route.path } })
}
</script>
<style lang="scss"></style>