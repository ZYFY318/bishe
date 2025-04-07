<template>
  <el-card class="base-card exam-card">
    <div class="card-content">
      <div class="card-image" @click="goToExam">
        <img :src="getImgUrl(exam.coverUrl)" alt="试卷封面" />
      </div>
      <div class="card-info" @click="goToExam">
        <h3 class="card-title">{{ exam.title }}</h3>
        <div class="card-details">
          <p><el-icon><Timer /></el-icon> {{ exam.duration }} 分钟</p>
          <p><el-icon><Calendar /></el-icon> {{ formatDate(exam.createdAt) }}</p>
        </div>
      </div>
      <div class="card-actions" @click.stop>
        <el-button 
          :type="isPublished ? 'success' : 'primary'"
          :icon="isPublished ? Check : Upload"
          size="small"
          :loading="publishing"
          @click="handlePublish"
        >
          {{ isPublished ? '已发布' : '发布' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Calendar, Timer, Upload, Check } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { publishExam, getExamQuestions } from '@/api/product/exam';

// 定义Props类型
interface ExamItem {
  id: number;
  title: string;
  coverUrl: string;
  duration: number;
  createdAt: string;
  published?: boolean;
}

// 定义props
const props = defineProps<{
  exam: ExamItem;
}>();

// 定义emit事件
const emit = defineEmits<{
  (e: 'exam-updated', examId: number): void;
}>();

const router = useRouter();
const publishing = ref(false);

// 计算属性：是否已发布
const isPublished = computed(() => !!props.exam.published);

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
    path: '/examview',
    query: {
      examId: props.exam.id.toString(),
      examTitle: props.exam.title
    }
  });
};

// 处理发布/取消发布试卷
const handlePublish = async () => {
  try {
    publishing.value = true;
    // 切换发布状态
    const newPublishState = !isPublished.value;
    
    // 如果是发布操作，先检查试卷是否有题目
    if (newPublishState) {
      // 获取试卷题目列表
      const questionsResult = await getExamQuestions(props.exam.id);
      
      // 检查试卷是否至少有一个题目
      if (questionsResult.code === 200 && (!questionsResult.data.questions || questionsResult.data.questions.length === 0)) {
        ElMessage.warning('试卷必须至少包含一个题目才能发布');
        publishing.value = false;
        return;
      }
    }
    
    // 调用API更新试卷发布状态
    const result = await publishExam(props.exam.id, newPublishState);
    
    if (result.code === 200) {
      ElMessage.success(newPublishState ? '试卷已成功发布' : '试卷已取消发布');
      // 通知父组件试卷已更新
      emit('exam-updated', props.exam.id);
    } else {
      ElMessage.error(result.message || (newPublishState ? '发布失败' : '取消发布失败'));
    }
  } catch (error) {
    console.error('发布操作出错:', error);
    ElMessage.error('操作失败，请稍后重试');
  } finally {
    publishing.value = false;
  }
};
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

.exam-card {
  // 这里可以添加特定于ExamCard的额外样式
  .card-actions {
    justify-content: flex-end;
  }
  
  .card-details, .card-info p {
    color: var(--text-color);
  }
  
  .card-title {
    color: var(--text-color);
  }
}
</style> 