<template>
    <template v-for="(item, index) in menuList" :key="item.path">
        <!-- no children -->
        <template v-if="!item.children">
            <el-menu-item v-if="!item.meta.hidden" :index="item.path" @click="goRoute">
                <el-icon>
                    <component :is="item.meta.icon"></component>
                </el-icon>
                <template #title>
                    <span>{{ item.meta.title }}</span>
                </template>
            </el-menu-item>
        </template>

        <!-- have child but only one -->
        <template v-if="item.children && item.children.length == 1">
            <el-menu-item v-if="!item.children[0].meta.hidden" :index="item.children[0].path" @click="goRoute">
                <el-icon>
                    <component :is="item.children[0].meta.icon"></component>
                </el-icon>
                <template #title>
                    <span>{{ item.children[0].meta.title }}</span>
                </template>
            </el-menu-item>
        </template>

        <!-- have two more child -->
        <el-sub-menu :index="item.path" v-if="item.children && item.children.length > 1">
            <template #title>
                <el-icon>
                    <component :is="item.meta.icon"></component>
                </el-icon>
                <span>{{ item.meta.title }}</span>
            </template>
            <el-menu class="sub-menu">
                <Menu :menuList="item.children"></Menu>
            </el-menu>

        </el-sub-menu>
    </template>
</template>
<script setup lang="ts">
import router from '@/router';

import { useRouter } from 'vue-router';
//获取父组件传递过来的全部路由
defineProps(['menuList'])
//获取路由器对象
let $router = useRouter();
const goRoute = (vc: any) => {
    $router.push(vc.index);

}
</script>
<script lang="ts">
export default {
    name: 'Menu'
}
</script>
<style lang="scss">
.el-menu {
    .el-menu-item {
        height: 60px;
        line-height: 60px;
        
        .el-icon {
            font-size: 18px;
            margin-right: 8px;
        }
    }

    .el-sub-menu {
        .el-sub-menu__title {
            height: 60px;
            line-height: 60px;

            .el-icon {
                font-size: 18px;
                margin-right: 8px;
            }
        }
    }
}

.sub-menu {
    background-color: #1f2d3d;
}

</style>
