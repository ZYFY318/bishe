<template>
  <el-card class="add-card" @click="openUploadDialog">
    <div class="card-content">
      <div class="add-icon">
        <el-icon :size="40"><plus /></el-icon>
      </div>
      <div class="add-text">
        <h3>添加模型</h3>
        <p>上传新的3D模型文件</p>
      </div>
    </div>
  </el-card>
  
  <ModelUploadDialog v-model:visible="dialogVisible" @uploaded="handleUploaded" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import ModelUploadDialog from '../ModelUploadDialog/index.vue';

// 对话框可见性
const dialogVisible = ref(false);

// 打开上传对话框
const openUploadDialog = () => {
  dialogVisible.value = true;
};

// 处理上传成功事件
const handleUploaded = () => {
  // 发出上传成功的事件，通知父组件刷新数据
  emit('model-uploaded');
};

const emit = defineEmits(['model-uploaded']);
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';
</style> 