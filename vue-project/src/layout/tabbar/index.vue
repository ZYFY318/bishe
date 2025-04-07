<template>
    <div class="tabbar">
        <div class="tabbar_left">
            <Breadcrumb></Breadcrumb>
        </div>
        <div class="tabbar_right">
            <Setting></Setting>
        </div>
    </div>
</template>
<script setup lang="ts">
import Breadcrumb from './breadcrumb/index.vue'
import Setting from './setting/index.vue';
</script>
<style lang="scss">
.tabbar {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: space-between;
    background-color: var(--card-bg);
    border-radius: 10px;
    box-shadow: var(--shadow);
    transition: all 0.3s ease;

    .tabbar_left,
    .tabbar_right {
        display: flex;
        align-items: center;
        width: 50%;
    }

    .tabbar_left {
        margin-left: 20px;
    }

    .tabbar_right {
        justify-content: flex-end;
        padding-right: 20px;
    }
}
</style>