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
import useUserStore from '@/stores/modules/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 从路由参数中获取试卷ID和标题
const examId = ref(Number(route.query.examId) || 0);
const examTitle = ref(route.query.examTitle as string || '未命名试卷');
const canEdit = ref(userStore.userType === 'TEACHER'); // 只有教师可以编辑试卷

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

// 返回上一页，根据用户角色决定返回路径
const goBack = () => {
  // 根据用户角色决定返回的页面
  if (userStore.userType === 'TEACHER') {
    // 教师返回试卷管理页面
    router.push('/product/exam');
  } else {
    // 学生返回试卷列表页面
    router.push('/study/exam-list');
  }
};

// 前往编辑页面
const goToEdit = () => {
  router.push({
    path: '/examview/edit',
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
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.exam-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--card-bg);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: var(--text-color);
}

.exam-title {
  margin: 0 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
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
  color: var(--text-color);
}

.empty-icon {
  font-size: 100px;
  color: var(--text-color);
  opacity: 0.5;
}

.questions-list {
  margin-top: 20px;
}

.question-card {
  margin-bottom: 20px;
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-number {
  font-weight: bold;
  color: var(--text-color);
}

.question-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--text-color);
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option {
  padding: 10px;
  border-radius: 5px;
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  
  .option-text {
    color: var(--text-color);
  }
}
</style> 