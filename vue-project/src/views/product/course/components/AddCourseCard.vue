<template>
    <el-card class="add-card" @click="openAddDialog">
        <div class="card-content">
            <div class="add-icon">
                <el-icon :size="40">
                    <Plus />
                </el-icon>
            </div>
            <div class="add-text">
                <h3>添加课程</h3>
                <p>创建新的课程</p>
            </div>
        </div>
    </el-card>

    <CourseUploadDialog v-model:visible="dialogVisible" @uploaded="handleUploaded" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import CourseUploadDialog from './CourseUploadDialog.vue';
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
    emit('course-added');
};

const emit = defineEmits(['course-added']);
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

// 可以添加特定于AddCourseCard的额外样式</style>