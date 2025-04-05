<template>
  <el-card class="base-card model-card" @click="handleClick">
    <div class="card-content">
      <div class="card-image">
        <img :src="modelImage" alt="Model preview" />
      </div>
      <div class="card-info">
        <h3 class="card-title">{{ props.modelItem.name }}</h3>
        <p class="card-description">{{ props.modelItem.description }}</p>
        <div class="card-details">
          <p class="created-time"><el-icon><Calendar /></el-icon> {{ formatDate(props.modelItem.created_at) }}</p>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, computed } from 'vue';
import { useRouter } from 'vue-router';
import type { PropType } from 'vue';
import type { ModelItem } from '@/api/model/type';
import { Calendar } from '@element-plus/icons-vue';

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
</script>

<style scoped lang="scss">
@import '@/styles/card.scss';

.model-card {
  // 特定于模型卡片的额外样式

}
</style>