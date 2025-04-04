<template>
  <el-card class="exam-card" @click="goToExam">
    <div class="card-content">
      <div class="cover-image">
        <img :src="getImgUrl(exam.coverUrl)" alt="试卷封面" />
      </div>
      <div class="exam-info">
        <h3 class="exam-title">{{ exam.title }}</h3>
        <div class="exam-details">
          <p><el-icon><Timer /></el-icon> {{ exam.duration }} 分钟</p>
          <p><el-icon><Calendar /></el-icon> {{ formatDate(exam.createdAt) }}</p>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Calendar, Timer } from '@element-plus/icons-vue';

// 定义Props类型
interface ExamItem {
  id: number;
  title: string;
  coverUrl: string;
  duration: number;
  createdAt: string;
}

// 定义props
const props = defineProps<{
  exam: ExamItem;
}>();

const router = useRouter();

// 获取完整的图片URL
const getImgUrl = (url: string) => {
  if (!url) return '';
  
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 如果是相对路径，添加API基础URL
  const baseURL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:9090';
  
  // 确保路径正确
  if (url.startsWith('/')) {
    return `${baseURL}${url}`;
  } else {
    return `${baseURL}/${url}`;
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

// 跳转到试卷页面
const goToExam = () => {
  router.push({
    path: '/product/examview',
    query: {
      examId: props.exam.id.toString(),
      examTitle: props.exam.title
    }
  });
};
</script>

<style scoped lang="scss">
.exam-card {
  width: 280px;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  }
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-image {
  height: 160px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.exam-info {
  padding: 15px;
}

.exam-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px;
  color: white;
}

.exam-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  p {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    color: #e0e0e0;
    font-size: 14px;
  }

  .el-icon {
    color: #e0e0e0;
  }
}
</style> 