<template>
  <el-card class="base-card model-card" :class="{ 'is-dragging': isDragging }" @click="handleClick" draggable="true" @dragstart="handleDragStart" @dragend="handleDragEnd">
    <div class="card-content">
      <div class="card-image">
        <img :src="getImgUrl(props.modelItem.coverUrl)" alt="Model preview" />
      </div>
      <div class="card-info">
        <h3 class="card-title">{{ props.modelItem.name }}</h3>
        <p class="card-description">{{ props.modelItem.description }}</p>
        <div class="card-details">
          <p class="created-time"><el-icon><Calendar /></el-icon> {{ formatDate(props.modelItem.created_at) }}</p>
          <p class="creator" v-if="props.modelItem.creatorName">
            <el-icon><User /></el-icon> {{ props.modelItem.creatorName }}
          </p>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, defineEmits, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { PropType } from 'vue';
import type { ModelItem } from '@/api/model/type';
import { Calendar, User } from '@element-plus/icons-vue';

const router = useRouter();
const emit = defineEmits(['drag-start']);

// 定义组件的 props
const props = defineProps({
  modelItem: {
    type: Object as PropType<ModelItem>,
    required: true
  }
});

// 拖拽状态
const isDragging = ref(false);



// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
};

const handleClick = () => {
  router.push({
    path: '/showModel',
    query: {
      modelId: props.modelItem.id,
      modelName: props.modelItem.name
    }
  });
};
// 获取完整的图片URL
const getImgUrl = (url: string) => {
  if (!url) return '/hakasei.png';
  
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
// 拖拽开始
const handleDragStart = (event: DragEvent) => {
  event.stopPropagation(); // 阻止点击事件触发
  if (event.dataTransfer) {
    event.dataTransfer.setData('modelId', props.modelItem.id.toString());
    if (props.modelItem.creatorId) {
      event.dataTransfer.setData('creatorId', props.modelItem.creatorId.toString());
    }
    isDragging.value = true;
    emit('drag-start', props.modelItem);
  }
};

// 拖拽结束
const handleDragEnd = () => {
  isDragging.value = false;
};
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

.model-card {
  // 特定于模型卡片的额外样式
  .card-title {
    color: var(--text-color);
  }
  
  .card-details, .card-info p {
    color: var(--text-color);
    
    .el-icon {
      margin-right: 5px;
    }
  }
  
  .card-details {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 10px;
    
    p {
      display: flex;
      align-items: center;
      margin: 0;
    }
    
    .creator {
      font-weight: 500;
    }
  }
  
  .card-description {
    color: var(--text-color);
    opacity: 0.8;
    margin: 10px 0;
  }
  
  &.is-dragging {
    opacity: 0.7;
    transform: scale(0.98);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  }
}
</style>