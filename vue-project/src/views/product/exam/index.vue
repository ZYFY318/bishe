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
      draggable="true"
      @dragstart="handleDragStart($event, exam)"
      @dragend="handleDragEnd"
    />

 
  </div>
     <!-- 删除区域 -->
     <div 
      class="delete-zone" 
      :class="{ 'delete-zone-active': isDragging, 'delete-zone-hover': isDragOver }"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      v-show="isDragging"
    >
      <el-icon class="delete-icon"><Delete /></el-icon>
      <span class="delete-text">拖动至此删除试卷</span>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ExamCard from './components/ExamCard.vue';
import AddExamCard from './components/AddExamCard.vue';
import { fetchTeacherExams, deleteExam } from '@/api/product/exam';
import type { ExamItem } from '@/api/product/exam/type';
import useUserStore from '@/stores/modules/user';
import { Delete } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 获取用户仓库以获取用户ID
const userStore = useUserStore();

// 定义一个响应式变量来存储从后端获取的试卷数据
const examList = ref<ExamItem[]>([]);

// 拖拽相关状态
const isDragging = ref(false);
const isDragOver = ref(false);
const currentDragExam = ref<ExamItem | null>(null);

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

// 处理拖拽开始
const handleDragStart = (event: DragEvent, exam: ExamItem) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('examId', exam.id.toString());
    isDragging.value = true;
    currentDragExam.value = exam;
  }
};

// 处理拖拽结束
const handleDragEnd = () => {
  isDragging.value = false;
  isDragOver.value = false;
  currentDragExam.value = null;
};

// 处理拖拽悬停
const handleDragOver = () => {
  isDragOver.value = true;
};

// 处理拖拽离开
const handleDragLeave = () => {
  isDragOver.value = false;
};

// 处理拖拽放置
const handleDrop = async (event: DragEvent) => {
  if (event.dataTransfer) {
    const examId = parseInt(event.dataTransfer.getData('examId'));
    // 确认删除
    ElMessageBox.confirm(
      '确定要删除这个试卷吗？此操作不可恢复',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(async () => {
        try {
          const response = await deleteExam(examId);
          if (response.code === 200) {
            ElMessage.success('试卷删除成功');
            refreshExamList();
          } else {
            ElMessage.error(response.message || '删除失败');
          }
        } catch (error) {
          console.error('删除试卷失败:', error);
          ElMessage.error('删除失败，请稍后重试');
        }
      })
      .catch(() => {
        ElMessage.info('已取消删除');
      });
  }
  isDragging.value = false;
  isDragOver.value = false;
  currentDragExam.value = null;
};

// 在组件挂载时请求数据
onMounted(() => {
  getExamList();
});
</script>

<style scoped lang="scss">

.delete-zone {
  position: fixed;
  bottom: 0px;
  right: 20px;
  width: 200px;
  height: 200px;
  border-radius: 10%;
  background-color: rgba(255, 73, 73, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
  border: 2px dashed #ff4949;
  z-index: 100;
  opacity: 0.7;
  
  .delete-icon {
    font-size: 40px;
    color: #ff4949;
    margin-bottom: 10px;
  }
  
  .delete-text {
    color: #ff4949;
    font-size: 16px;
    text-align: center;
  }
  
  &-active {
    background-color: rgba(255, 73, 73, 0.3);
    opacity: 1;
  }
  
  &-hover {
    background-color: rgba(255, 73, 73, 0.5);
    transform: scale(1.1);
    border: 2px solid #ff4949;
    box-shadow: 0 0 15px rgba(255, 73, 73, 0.5);
    opacity: 1;
  }
}
</style> 