<template>
    <el-card class="item-card" @click="navigateToShowModel">
      <div class="card-content">
        <img :src="imageUrl" alt="Item Image" class="item-image" />
        <div class="item-details">
          <h3 class="item-name">{{ name }}</h3>
          <p class="item-description">{{ description }}</p>
        </div>
      </div>
    </el-card>
  </template>
  
  <script setup lang="ts">
  import { defineProps } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  // 定义组件的 props
  const props = defineProps({
    imageUrl: {
      type: String,
      required: true
    },
    name: {
      type: String,
      required: true
    },
    description: {
      type: String,
      required: true
    },
    glbPath: {
      type: String,
      required: true
    }
  });
  
  const navigateToShowModel = () => {
    router.push({
      path: '/showModel',
      query: { modelPath: props.glbPath }
    });
  };
  </script>
  
  <style scoped lang="scss">
  .item-card {
    width: 300px;
    display: flex;
    align-items: center;
    padding: 20px;
    cursor: pointer;
    transition: transform 0.2s;
  
    &:hover {
      transform: scale(1.02);
    }
  }
  
  .card-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .item-image {
    width: 100px;
    height: 100px;
    object-fit: cover;
    margin-right: 20px;
  }
  
  .item-details {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    
  }
  
  .item-name {
    font-size: 18px;
    font-weight: bold;
    margin: 0 0 10px 0;
  }
  
  .item-description {
    font-size: 14px;
    color: #666;
    white-space: normal;
    word-wrap: break-word;
    overflow: hidden;
    text-overflow: ellipsis;
    text-align: left;
  }
  </style>