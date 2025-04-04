<template>
  <div class="exam-view-container">
    <div class="exam-header">
      <el-button text @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="exam-title">{{ examTitle }}</h1>
    </div>
    
    <div class="exam-content">
      <div class="placeholder-text">
        <el-empty description="试卷内容开发中，敬请期待..." :image-size="200">
          <template #image>
            <el-icon class="empty-icon"><DocumentCopy /></el-icon>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, DocumentCopy } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

// 从路由参数中获取试卷ID和标题
const examId = ref(route.query.examId as string);
const examTitle = ref(route.query.examTitle as string || '未命名试卷');

// 返回上一页
const goBack = () => {
  router.back();
};

onMounted(() => {
  // 如果需要从后端获取试卷详情，可以在这里添加相关代码
  console.log('Loading exam with ID:', examId.value);
});
</script>

<style scoped lang="scss">
.exam-view-container {
  width: 90%;
  margin: 20px auto;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 140px);
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.exam-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background-color: rgba(255, 255, 255, 0.05);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: white;
}

.exam-title {
  margin: 0 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: white;
}

.exam-content {
  flex: 1;
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.placeholder-text {
  text-align: center;
  color: white;
}

:deep(.el-empty__description) {
  color: white;
  font-size: 18px;
}

.empty-icon {
  font-size: 100px;
  color: rgba(255, 255, 255, 0.5);
}
</style> 