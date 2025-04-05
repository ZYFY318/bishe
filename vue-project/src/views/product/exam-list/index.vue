<template>
  <div class="card-container">
    <!-- 展示已发布的试卷卡片 -->
    <el-card
      v-for="exam in examList"
      :key="exam.id"
      class="base-card exam-card"
      @click="goToExam(exam.id)"
    >
      <div class="card-content">
        <div class="card-image">
          <img :src="getImgUrl(exam.coverUrl)" alt="试卷封面" />
        </div>
        <div class="card-info">
          <h3 class="card-title">{{ exam.title }}</h3>
          <div class="card-details">
            <p><el-icon><Timer /></el-icon> {{ exam.duration }} 分钟</p>
            <p><el-icon><Calendar /></el-icon> {{ formatDate(exam.createdAt) }}</p>
           
          </div>
          <div class="card-creator">
            <p v-if="exam.creatorName"><el-icon><User /></el-icon> {{ exam.creatorName }}</p>
          </div>
          
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Timer, User } from '@element-plus/icons-vue';
import { fetchPublishedExams } from '@/api/product/exam';
import type { ExamItem } from '@/api/product/exam/type';

const router = useRouter();

// 定义一个响应式变量来存储从后端获取的试卷数据
const examList = ref<ExamItem[]>([]);

// 获取试卷列表的函数
const getExamList = async () => {
  try {
    const response = await fetchPublishedExams();
    if (response.code === 200) {
      examList.value = response.data;
      console.log(examList.value)
    } else {
      console.error('Failed to fetch published exams:', response);
    }
  } catch (error) {
    console.error('Failed to fetch published exams:', error);
    examList.value = [];
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

// 获取图片URL，处理相对路径
const getImgUrl = (url: string) => {
  console.log(url)
  if (!url) return '';
  
  // 如果是完整URL，直接返回
  if (url.startsWith('http')) {
    return url;
  }
  
  // 如果是相对路径，拼接API基础URL
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090';
  console.log(`${baseUrl}${url}`)
  return `${baseUrl}${url}`;
};

// 跳转到考试页面
const goToExam = (examId: number) => {
  router.push({
    path: '/examview/take',
    query: {
      examId: examId.toString(),
      examTitle: examList.value.find(exam => exam.id === examId)?.title || '未命名试卷'
    }
  });
};

// 在组件挂载时请求数据
onMounted(() => {
  getExamList();
});
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

// 可以添加特定于学生试卷列表的额外样式
.exam-card {
  .card-creator {
    margin-top: 20px;
    p {
    // display: flex;
    align-items: center;
    margin: 0;
    font-size: 20px;
    font-weight: 300;
    color: rgba(255, 255, 255, 0.8);
    
    .el-icon {
      margin-right: 5px;
    }
  }
  }
}
</style> 