<template>
    <el-card class="base-card course-card" :class="{ 'is-dragging': isDragging }" @dragstart="handleDragStart"
        @dragend="handleDragEnd">
        <div class="card-content">
            <div class="card-image" @click="goToCourse">
                <img :src="getImgUrl(course.coverUrl)" alt="课程封面" />
            </div>
            <div class="card-info" @click="goToCourse">
                <h3 class="card-title">{{ course.title }}</h3>
                <div class="card-details">
                    <p><el-icon>
                            <Timer />
                        </el-icon> {{ course.duration }} 分钟</p>
                    <p><el-icon>
                            <Calendar />
                        </el-icon> {{ formatDate(course.createdAt) }}</p>
                </div>
            </div>
            <div class="card-actions" @click.stop>
                <el-button :type="isPublished ? 'success' : 'primary'" :icon="isPublished ? Check : Upload" size="small"
                    :loading="publishing" @click="handlePublish">
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
// 只保留 publishCourse 引用
import { publishCourse } from '@/api/product/course';

// 定义Props类型
interface CourseItem {
    id: number;
    title: string;
    coverUrl: string;
    duration: number;
    createdAt: string;
    published?: boolean;
}

// 定义props
const props = defineProps<{
    course: CourseItem;
}>();

// 定义emit事件
const emit = defineEmits<{
    (e: 'course-updated', courseId: number): void;
}>();

const router = useRouter();
const publishing = ref(false);

// 计算属性：是否已发布
const isPublished = computed(() => !!props.course.published);

// 拖拽状态
const isDragging = ref(false);

// 拖拽开始
const handleDragStart = () => {
    isDragging.value = true;
};

// 拖拽结束
const handleDragEnd = () => {
    isDragging.value = false;
};

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

// 跳转到课程页面
const goToCourse = () => {
    router.push({
        path: '/courseview',
        query: {
            courseId: props.course.id.toString(),
            courseTitle: props.course.title
        }
    });
};

// 处理发布/取消发布课程
const handlePublish = async () => {
    try {
        publishing.value = true;
        // 切换发布状态
        const newPublishState = !isPublished.value;

        // 调用API更新课程发布状态
        const result = await publishCourse(props.course.id, newPublishState);

        if (result.code === 200) {
            ElMessage.success(newPublishState ? '课程已成功发布' : '课程已取消发布');
            // 通知父组件课程已更新
            emit('course-updated', props.course.id);
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

.course-card {

    // 这里可以添加特定于CourseCard的额外样式
    .card-actions {
        justify-content: flex-end;
    }

    .card-details,
    .card-info p {
        color: var(--text-color);
    }

    .card-title {
        color: var(--text-color);
    }

    &.is-dragging {
        opacity: 0.7;
        transform: scale(0.98);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
    }
}
</style>