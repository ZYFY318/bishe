<template>
  <div class="card-container">
    <!-- 展示已发布的试卷卡片 -->
    <el-card
      v-for="exam in examList"
      :key="exam.id"
      class="base-card exam-card"
      :class="{ 'completed-exam': isExamCompleted(exam.id) }"
      @click="goToExam(exam.id)"
    >
      <div class="exam-status" v-if="isExamCompleted(exam.id)">
        <el-tag type="success" effect="dark">已完成</el-tag>
      </div>
      <div class="exam-status" v-else>
        <el-tag type="info" effect="dark">未完成</el-tag>
      </div>
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
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Timer, User, Check } from '@element-plus/icons-vue';
import { fetchPublishedExams } from '@/api/product/exam';
import { reqUserExamResults } from '@/api/examResult';
import type { ExamItem } from '@/api/product/exam/type';
import { ElMessageBox } from 'element-plus';
import useUserStore from '@/stores/modules/user';

const router = useRouter();
const userStore = useUserStore();

// 定义一个响应式变量来存储从后端获取的试卷数据
const examList = ref<ExamItem[]>([]);
// 用户已完成的考试记录
const completedExams = ref<number[]>([]);

// 检查试卷是否已完成
const isExamCompleted = (examId: number) => {
  return completedExams.value.includes(examId);
};

// 获取用户已完成的考试记录
const getUserCompletedExams = async () => {
  try {
    const result = await reqUserExamResults(userStore.userId) as any;
    console.log("here",result)
    if (result.code === 200 && result.data && result.data.length > 0) {
      // 提取已完成试卷的ID
      completedExams.value = result.data.map((record: any) => record.examId);
    }
  } catch (error) {
    console.error('获取考试记录出错:', error);
  }
};

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
  // 如果试卷已完成，直接跳转到查看模式
  if (isExamCompleted(examId)) {
    router.push({
      path: '/examview/take',
      query: {
        examId: examId.toString(),
        examTitle: examList.value.find(exam => exam.id === examId)?.title || '未命名试卷'
      }
    });
    return;
  }
  
  // 未完成的试卷需要确认是否开始
  ElMessageBox.confirm(
    '确定要开始考试吗？开始后计时将不能暂停。',
    '进入考试确认',
    {
      confirmButtonText: '确认进入',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      router.push({
        path: '/examview/take',
        query: {
          examId: examId.toString(),
          examTitle: examList.value.find(exam => exam.id === examId)?.title || '未命名试卷'
        }
      });
    })
    .catch(() => {
      // 用户取消，不做任何操作
    });
};

// 在组件挂载时请求数据
onMounted(async () => {
  await getUserCompletedExams();
  await getExamList();
});
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

// 可以添加特定于学生试卷列表的额外样式
.exam-card {
  position: relative;
  transition: all 0.3s ease;
  
  &.completed-exam {
    border: 1px solid rgba(103, 194, 58, 0.5);
    box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(103, 194, 58, 0.3);
    }
  }
  
  &:not(.completed-exam):hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(64, 158, 255, 0.3);
  }
  
  .exam-status {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 2;
  }
  
  .card-creator {
    margin-top: 20px;
    p {
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