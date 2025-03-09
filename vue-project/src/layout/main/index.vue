<template>
    <div id="app">
        <router-view v-slot="{ Component }">
            <transition name="fade">
                <component :is="Component" v-if="flag" />
            </transition>
        </router-view>
    </div>
</template>

<script setup lang="ts">
import useLayOutSettingStore from '@/stores/modules/setting';
import { ref, watch, nextTick } from 'vue';
//控制当前组件是否销毁
let flag = ref(true)
const LayOutSettingStore = useLayOutSettingStore();
//监听仓库refresh是否变化
watch(() => LayOutSettingStore.refresh as any, () => {
    flag.value = false;
    nextTick(() => {
        flag.value = true;
    })
})
</script>

<style scoped lang="scss">
/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s ease;
}

.fade-enter,
.fade-leave-to {
    opacity: 0;
}

.fade-enter-from {
    opacity: 0;
    transform: scale(0);
}

.fade-enter-to {
    opacity: 1;
    transform: scale(1);
}

#app {
    display: flex;
    justify-content: center;
}
</style>