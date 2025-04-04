<template>
  <div class="card-container">
    <!-- 添加试卷卡片放在最前面 -->
    <AddExamCard @exam-added="refreshExamList" />
    
    <!-- 展示现有试卷的卡片 -->
    <ExamCard
      v-for="exam in examList"
      :key="exam.id"
      :exam="exam"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ExamCard from './components/ExamCard.vue';
import AddExamCard from './components/AddExamCard.vue';
import { fetchExams } from '@/api/product/exam';
import type { ExamItem } from '@/api/product/exam/type';

// 定义一个响应式变量来存储从后端获取的试卷数据
const examList = ref<ExamItem[]>([]);

// 获取试卷列表的函数
const getExamList = async () => {
  try {
    const response = await fetchExams();
    if (response.code === 200) {
      examList.value = response.data;
      console.log(response.data)
    } else {
      console.error('Failed to fetch exams:', response);
    }
  } catch (error) {
    console.error('Failed to fetch exams:', error);
    // 临时数据，实际项目中可删除
    examList.value = [
      {
        id: 1,
        title: '期中考试',
        coverUrl: 'https://i.yuki.sh/img-master/img/2022/05/21/23/03/44/98506476_p0_master1200.jpg',
        duration: 60,
        createdAt: '2023-05-01'
      },
      {
        id: 2,
        title: '期末考试',
        coverUrl: 'https://element-plus.org/images/element-plus-logo.svg',
        duration: 120,
        createdAt: '2023-06-15'
      },
      {
        id: 3,
        title: '期末考试',
        coverUrl: 'https://element-plus.org/images/element-plus-logo.svg',
        duration: 120,
        createdAt: '2023-06-15'
      },
      {
        id: 4,
        title: '期末考试',
        coverUrl: 'https://element-plus.org/images/element-plus-logo.svg',
        duration: 120,
        createdAt: '2023-06-15'
      },
      
    ];
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
.card-container {
  display: flex;
  flex-wrap: wrap; // 允许卡片换行
  gap: 20px; // 卡片之间的间距
  padding: 20px; // 容器内边距
  justify-content: flex-start; // 从左向右排列
  min-height: calc(100vh - 200px);
  margin: 20px;
}
</style> 