<template>
  <div class="item-card" @click="handleClick">
    <div class="card-content">
      <img :src="modelImage" alt="Model preview" class="model-preview" />
      <div class="model-info">
        <h3>{{ props.modelItem.name }}</h3>
        <p>{{ props.modelItem.description }}</p>
        <p class="created-time">创建时间: {{ new Date(props.modelItem.created_at).toLocaleString() }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, computed } from 'vue';
import { useRouter } from 'vue-router';
import type { PropType } from 'vue';
import type { ModelItem } from '@/api/model/type';

const router = useRouter();

// 定义组件的 props
const props = defineProps({
  modelItem: {
    type: Object as PropType<ModelItem>,
    required: true
  }
});

// 计算属性：如果没有 imageUrl 则使用默认图片
const modelImage = computed(() => {
  return props.modelItem.imageUrl || '/hakasei.png';
});

const handleClick = () => {
  router.push({
    path: '/showModel',
    query: {
      modelId: props.modelItem.id,
      modelName: props.modelItem.name
    }
  });
};
</script>

<style scoped lang="scss">
.item-card {
  // background: #ffffff;
  // border-radius: 8px;
  // box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 10px ;
    backdrop-filter: blur(10px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
  }
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.model-info {
  h3 {
    margin: 0 0 8px;
    font-size: 18px;
    color: #333;
  }
  
  p {
    margin: 0;
    color: #666;
    font-size: 14px;
    
    &.created-time {
      margin-top: 8px;
      color: #999;
      font-size: 12px;
    }
  }
}

.model-preview {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 12px;
}
</style>