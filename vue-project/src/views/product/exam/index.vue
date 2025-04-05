<template>
  <div class="card-container">
    <!-- 添加试卷卡片放在最前面 -->
    <AddExamCard @exam-added="refreshExamList" />
    
    <!-- 展示现有试卷的卡片 -->
    <ExamCard
      v-for="exam in examList"
      :key="exam.id"
      :exam="exam"
      @exam-updated="refreshExamList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ExamCard from './components/ExamCard.vue';
import AddExamCard from './components/AddExamCard.vue';
import { fetchTeacherExams } from '@/api/product/exam';
import type { ExamItem } from '@/api/product/exam/type';
import useUserStore from '@/stores/modules/user';

// 获取用户仓库以获取用户ID
const userStore = useUserStore();

// 定义一个响应式变量来存储从后端获取的试卷数据
const examList = ref<ExamItem[]>([]);

// 获取试卷列表的函数
const getExamList = async () => {
  try {
    // 使用当前登录用户的ID获取该教师创建的试卷
    const response = await fetchTeacherExams(userStore.userId);
    if (response.code === 200) {
      examList.value = response.data;
      console.log(response.data)
    } else {
      console.error('Failed to fetch exams:', response);
    }
  } catch (error) {
    console.error('Failed to fetch exams:', error);
    // 临时数据，实际项目中可删除
    examList.value = [];
  }
};

// 刷新试卷列表
const refreshExamList = () => {
  getExamList();
};

// 在组件挂载时请求数据
onMounted(() => {
  getExamList();
});
</script>

<style scoped lang="scss">

</style> 