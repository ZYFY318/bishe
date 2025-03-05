<template>
    <el-icon style="margin-right: 10px;" @click="changeIcon">
        <component :is="LayoutSettingStore.fold ? 'Fold' : 'Expand'"></component>
    </el-icon>
    <el-breadcrumb separator-icon="ArrowRight">
        <el-breadcrumb-item v-for="(item, index) in $route.matched" :key="index" v-show="item.meta.title"
            :to="item.path">
            <!-- 图标 -->
            <el-icon style="margin: 0px 2px;">
                <component :is="item.meta.icon"></component>
            </el-icon>
            <!-- 面包屑展示匹配路由的标题 -->
            <span>{{ item.meta.title }}</span>
        </el-breadcrumb-item>

    </el-breadcrumb>
</template>
<script setup lang="ts">
import { useRoute } from 'vue-router';
import { ref } from 'vue';
//获取仓库
import useLayOutSettingStore from '@/stores/modules/setting';
const LayoutSettingStore = useLayOutSettingStore();
const $route = useRoute();
let fold = ref(false);
const changeIcon = () => {
    LayoutSettingStore.fold = !LayoutSettingStore.fold
}
</script>
<style lang="scss"></style>