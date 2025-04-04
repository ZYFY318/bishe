<template>
  <div class="exam-view-container">
    <div class="exam-header">
      <el-button text @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="exam-title">{{ examTitle }}</h1>
      <el-button type="primary" @click="goToEdit" v-if="canEdit">
        <el-icon><Edit /></el-icon>
        编辑试卷
      </el-button>
    </div>
    
    <div class="exam-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else-if="questions.length === 0" class="placeholder-text">
        <el-empty description="试卷中暂无题目，请点击编辑试卷添加题目" :image-size="200">
          <template #image>
            <el-icon class="empty-icon"><DocumentCopy /></el-icon>
          </template>
          <el-button type="primary" @click="goToEdit" v-if="canEdit">
            去添加题目
          </el-button>
        </el-empty>
      </div>
      <div v-else class="questions-list">
        <el-card v-for="(question, index) in questions" :key="question.id" class="question-card">
          <div class="question-header">
            <div class="question-number">第 {{ index + 1 }} 题</div>
          </div>
          <div class="question-title">{{ question.title }}</div>
          <div class="question-options">
            <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option">
              <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
              <span class="option-text">{{ option }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, DocumentCopy, Edit } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getExamQuestions } from '@/api/product/exam';
import type { QuestionItem } from '@/api/product/exam/type';

const route = useRoute();
const router = useRouter();

// 从路由参数中获取试卷ID和标题
const examId = ref(Number(route.query.examId) || 0);
const examTitle = ref(route.query.examTitle as string || '未命名试卷');
const canEdit = ref(true); // 这里可以根据用户权限判断是否可以编辑

// 试卷内容
const loading = ref(false);
const questions = ref<QuestionItem[]>([]);

// 加载试卷数据
const loadExamData = async () => {
  if (!examId.value) {
    ElMessage.error('试卷ID无效');
    return;
  }

  try {
    loading.value = true;
    const result = await getExamQuestions(examId.value);
    if (result.code === 200) {
      questions.value = result.data.questions;
    } else {
      ElMessage.error(result.message || '获取试卷题目失败');
    }
  } catch (error) {
    console.error('获取试卷题目出错:', error);
    ElMessage.error('获取试卷题目失败');
  } finally {
    loading.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 前往编辑页面
const goToEdit = () => {
  router.push({
    path: '/product/examview/edit',
    query: {
      examId: examId.value,
      examTitle: examTitle.value
    }
  });
};

onMounted(() => {
  loadExamData();
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
  flex: 1;
}

.exam-content {
  flex: 1;
  padding: 40px;
}

.loading-container {
  margin: 20px;
}

.placeholder-text {
  text-align: center;
  color: white;
}

.empty-icon {
  font-size: 100px;
  color: rgba(255, 255, 255, 0.5);
}

.questions-list {
  margin-top: 20px;
}

.question-card {
  margin-bottom: 20px;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-number {
  font-weight: bold;
  color: white;
}

.question-title {
  font-size: 16px;
  margin-bottom: 15px;
  color: white;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: white;
}

.option-label {
  min-width: 24px;
}

.option-text {
  flex: 1;
}

:deep(.el-empty__description) {
  color: white;
  font-size: 18px;
}

:deep(.el-card__body) {
  padding: 15px;
}
</style> 