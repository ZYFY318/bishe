<template>
  <el-card class="add-model-card" @click="openUploadDialog">
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
.add-model-card {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.3s;
  border: 2px dashed rgba(255, 255, 255, 0.4);
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;

  &:hover {
    transform: scale(1.02);
    border-color: rgba(255, 255, 255, 0.8);
    background-color: rgba(255, 255, 255, 0.2);
  }
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.add-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: white;
}

.add-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

h3 {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: white;
}

p {
  font-size: 14px;
  color: #e0e0e0;
  margin-top: 5px;
}
</style> 