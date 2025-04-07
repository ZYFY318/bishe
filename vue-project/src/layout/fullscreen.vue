<template>
  <div class="fullscreen-layout">
    <router-view></router-view>
  </div>
</template>

<script setup lang="ts">
// 全屏布局组件，没有头部和侧边栏
</script>

<style scoped lang="scss">
.fullscreen-layout {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background-color: var(--bg-color);
  transition: background-color 0.3s ease;
}
</style> 