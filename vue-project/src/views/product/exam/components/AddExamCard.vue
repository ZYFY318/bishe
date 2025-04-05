<template>
  <el-card class="add-card" @click="openAddDialog">
    <div class="card-content">
      <div class="add-icon">
        <el-icon :size="40"><Plus /></el-icon>
      </div>
      <div class="add-text">
        <h3>添加试卷</h3>
        <p>创建新的考试试卷</p>
      </div>
    </div>
  </el-card>
  
  <ExamUploadDialog v-model:visible="dialogVisible" @uploaded="handleUploaded" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import ExamUploadDialog from './ExamUploadDialog.vue';
import useUserStore from '@/stores/modules/user';
import { ElMessage } from 'element-plus';

// 对话框可见性
const dialogVisible = ref(false);

// 打开添加对话框
const openAddDialog = () => {
  dialogVisible.value = true;
};

// 处理上传成功事件
const handleUploaded = () => {
  // 发出上传成功的事件，通知父组件刷新数据
  emit('exam-added');
};

const emit = defineEmits(['exam-added']);

// 在创建试卷的方法中添加创建者ID
const createExam = async () => {
  // 验证表单
  await formRef.value?.validate();
  loading.value = true;
  
  try {
    // 获取用户信息
    const userStore = useUserStore();
    
    // 准备创建数据
    const data: ExamCreateData = {
      title: form.title,
      duration: form.duration,
      creatorId: userStore.userId, // 添加创建者ID
      cover: form.cover
    };
    
    // 调用创建接口
    const result = await createExamAPI(data);
    
    if (result.code === 200 && result.data.exam) {
      // 创建成功，显示成功消息
      ElMessage.success('试卷创建成功');
      // 通知父组件刷新列表
      emit('exam-added');
      // 重置表单
      resetForm();
    } else {
      throw new Error(result.data.message || '创建失败');
    }
  } catch (error) {
    console.error('创建试卷失败', error);
    ElMessage.error(`创建失败: ${(error as Error).message}`);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

// 可以添加特定于AddExamCard的额外样式
</style> 