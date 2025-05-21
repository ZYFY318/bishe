<template>
    <div class="card-container">
        <!-- 添加课程卡片放在最前面 -->
        <AddCourseCard @course-added="refreshCourseList" />

        <!-- 展示现有课程的卡片 -->
        <CourseCard v-for="course in courseList" :key="course.id" :course="course" @course-updated="refreshCourseList"
            draggable="true" @dragstart="handleDragStart($event, course)" @dragend="handleDragEnd" />

        <!-- 删除区域 -->
        <div class="delete-zone" :class="{ 'delete-zone-active': isDragging, 'delete-zone-hover': isDragOver }"
            @dragover.prevent="handleDragOver" @dragleave="handleDragLeave" @drop="handleDrop" v-show="isDragging">
            <el-icon class="delete-icon">
                <Delete />
            </el-icon>
            <span class="delete-text">拖动至此删除课程</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import CourseCard from './components/CourseCard.vue';
import AddCourseCard from './components/AddCourseCard.vue';
// 修改导入的接口
import { fetchCourseList, deleteCourse } from '@/api/product/course';
import type { CourseItem } from '@/api/product/course/type';
import useUserStore from '@/stores/modules/user';
import { Delete } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 获取用户仓库以获取用户ID
const userStore = useUserStore();

// 定义一个响应式变量来存储从后端获取的课程数据
const courseList = ref<CourseItem[]>([]);

// 拖拽相关状态
const isDragging = ref(false);
const isDragOver = ref(false);
const currentDragCourse = ref<CourseItem | null>(null);

// 获取课程列表的函数
const getCourseList = async () => {
    try {
        // 修改为使用 fetchCourseList 接口，并添加用户 ID 参数
        const response = await fetchCourseList();
        if (response.code === 200) {
            // 假设后端返回的数据包含当前教师的课程
            courseList.value = response.data.filter((course: CourseItem) => course.creatorId === userStore.userId);
            console.log(response.data)
        } else {
            console.error('Failed to fetch courses:', response);
        }
    } catch (error) {
        console.error('Failed to fetch courses:', error);
        // 临时数据，实际项目中可删除
        courseList.value = [];
    }
};

// 刷新课程列表
const refreshCourseList = () => {
    getCourseList();
};

// 处理拖拽开始
const handleDragStart = (event: DragEvent, course: CourseItem) => {
    if (event.dataTransfer) {
        event.dataTransfer.setData('courseId', course.id.toString());
        isDragging.value = true;
        currentDragCourse.value = course;
    }
};

// 处理拖拽结束
const handleDragEnd = () => {
    isDragging.value = false;
    isDragOver.value = false;
    currentDragCourse.value = null;
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
        const courseId = parseInt(event.dataTransfer.getData('courseId'));
        // 确认删除
        ElMessageBox.confirm(
            '确定要删除这个课程吗？此操作不可恢复',
            '警告',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
            .then(async () => {
                try {
                    const response = await deleteCourse(courseId);
                    if (response.code === 200) {
                        ElMessage.success('课程删除成功');
                        refreshCourseList();
                    } else {
                        ElMessage.error(response.message || '删除失败');
                    }
                } catch (error) {
                    console.error('删除课程失败:', error);
                    ElMessage.error('删除失败，请稍后重试');
                }
            })
            .catch(() => {
                ElMessage.info('已取消删除');
            });
    }
    isDragging.value = false;
    isDragOver.value = false;
    currentDragCourse.value = null;
};

// 在组件挂载时请求数据
onMounted(() => {
    getCourseList();
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