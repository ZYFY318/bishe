<template>
    <div class="card-container">
      <!-- 添加模型卡片放在最前面 -->
      <AddModelCard @model-uploaded="refreshModelList" />
      
      <!-- 展示现有模型的卡片 -->
      <ItemCard
        v-for="item in items"
        :key="item.id"
        :model-item="item"
      />
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import type { ModelItem } from '@/api/model/type';
  import { reqModelList } from '@/api/model';
  import ItemCard from '@/components/ItemCard/index.vue';
  import AddModelCard from '@/components/AddModelCard/index.vue';
  
  // 定义一个响应式变量来存储从后端获取的数据
  const items = ref<ModelItem[]>([]);
  
  // 获取模型列表的函数
  const fetchModelList = async () => {
    try {
      const response = await reqModelList();
      console.log("here",response.data)
      if (response.code === 200) {
        items.value = response.data;
      } else {
        console.error('Failed to fetch items:', response);
      }
    } catch (error) {
      console.error('Failed to fetch items:', error);
    }
  };
  
  // 刷新模型列表
  const refreshModelList = () => {
    fetchModelList();
  };
  
  // 在组件挂载时请求数据
  onMounted(() => {
    fetchModelList();
  });
  </script>
  
  <style scoped lang="scss">
 
  </style>