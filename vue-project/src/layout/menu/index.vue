<template>
    <template v-for="(item, index) in menuList" :key="item.path">
        <!-- no children -->
        <template v-if="!item.children">
            <el-menu-item 
                v-if="!item.meta.hidden && hasPermission(item)" 
                :index="item.path" 
                @click="goRoute"
            >
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
            <el-menu-item 
                v-if="!item.children[0].meta.hidden && hasPermission(item.children[0])" 
                :index="item.children[0].path" 
                @click="goRoute"
            >
                <el-icon>
                    <component :is="item.children[0].meta.icon"></component>
                </el-icon>
                <template #title>
                    <span>{{ item.children[0].meta.title }}</span>
                </template>
            </el-menu-item>
        </template>

        <!-- have two more child -->
        <el-sub-menu 
            :index="item.path" 
            v-if="item.children && item.children.length > 1 && !item.meta.hidden && hasPermission(item)"
        >
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
import useUserStore from '@/stores/modules/user';

// 获取父组件传递过来的全部路由
defineProps(['menuList']);

// 获取用户仓库
const userStore = useUserStore();

// 检查用户是否有权限查看该菜单项
const hasPermission = (route: any) => {
    // 如果路由没有配置roles或者roles是空数组，则所有用户可见
    if (!route.meta || !route.meta.roles || route.meta.roles.length === 0) {
        return true;
    }
    
    // 检查当前用户类型是否在允许的角色列表中

    return route.meta.roles.includes(userStore.userType);
};

// 获取路由器对象
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
    background-color: var(--menu-bg) !important;
    border-right: none;
    
    .el-menu-item {
        height: 60px;
        line-height: 60px;
        color: var(--menu-text) !important;
        
        &.is-active {
            background-color: rgba(64, 158, 255, 0.2) !important;
            color: var(--primary-color) !important;
            
            &::before {
                content: "";
                position: absolute;
                left: 0;
                top: 15px;
                bottom: 15px;
                width: 3px;
                background-color: var(--primary-color);
                border-radius: 0 3px 3px 0;
            }
        }
        
        &:hover {
            background-color: rgba(64, 158, 255, 0.1) !important;
        }
        
        .el-icon {
            font-size: 18px;
            margin-right: 8px;
            color: var(--menu-text);
        }
    }

    .el-sub-menu {
        .el-sub-menu__title {
            height: 60px;
            line-height: 60px;
            color: var(--menu-text) !important;
            
            &:hover {
                background-color: rgba(64, 158, 255, 0.1) !important;
            }

            .el-icon {
                font-size: 18px;
                margin-right: 8px;
                color: var(--menu-text);
            }
        }
    }
}

.sub-menu {
    background-color: var(--menu-bg) !important;
}
</style>
