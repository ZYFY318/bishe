<template>
    <div class="logo" v-if="!setting.logoIsHidden">
        <img :src="setting.logo" alt="">
        <p>{{ setting.title }}</p>
    </div>
</template>
<script setup lang="ts">
//引入标题logo配置文件
import setting from '@/setting';

</script>
<style lang="scss">
.logo {
    width: 100%;
    height: $base-menu-logo-height;
    color: var(--menu-text);
    display: flex;
    align-items: center;
    padding: 8px;
    transition: all 0.3s ease;

    img {
        width: 40px;
        height: 40px;
        margin-right: 10px;
    }

    p {
        font-size: $base-menu-logo-title-fontSize;
        color: var(--menu-text);
        font-weight: 600;
        margin: 0;
        letter-spacing: 2px;
    }
}
</style>