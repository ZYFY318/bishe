<template>
    <div class="card-container">
      <!-- 添加模型卡片放在最前面 -->
      <AddModelCard @model-uploaded="refreshModelList" />
      
      <!-- 展示现有模型的卡片 -->
      <ItemCard
        v-for="item in items"
        :key="item.id"
        :model-item="item"
        @drag-start="handleModelDragStart"
      />
      
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
        <span class="delete-text">拖动至此删除模型</span>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue';
  import type { ModelItem } from '@/api/model/type';
  import { reqModelList, deleteModel } from '@/api/model';
  import { Delete } from '@element-plus/icons-vue';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import useUserStore from '@/stores/modules/user';

  // 获取用户仓库以获取用户ID
  const userStore = useUserStore();

  // 定义一个响应式变量来存储从后端获取的数据
  const items = ref<ModelItem[]>([]);
  
  // 拖拽相关状态
  const isDragging = ref(false);
  const isDragOver = ref(false);
  const currentDragModel = ref<ModelItem | null>(null);
  
  // 获取模型列表的函数
  const fetchModelList = async () => {
    try {
      const response = await reqModelList();
      console.log("获取到模型列表:", response.data);
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
  
  // 处理模型拖拽开始
  const handleModelDragStart = (model: ModelItem) => {
    isDragging.value = true;
    currentDragModel.value = model;
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
      const modelId = parseInt(event.dataTransfer.getData('modelId'));
      const creatorId = event.dataTransfer.getData('creatorId');
      
      // 检查是否是自己创建的模型
      if (creatorId && parseInt(creatorId) !== userStore.userId) {
        ElMessage.error('无法删除不是由您创建的模型');
      } else {
        // 确认删除
        ElMessageBox.confirm(
          '确定要删除这个模型吗？此操作不可恢复',
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
          .then(async () => {
            try {
              const response = await deleteModel(modelId);
              if (response.code === 200) {
                ElMessage.success('模型删除成功');
                refreshModelList();
              } else {
                ElMessage.error(response.message || '删除失败');
              }
            } catch (error) {
              console.error('删除模型失败:', error);
              ElMessage.error('删除失败，请稍后重试');
            }
          })
          .catch(() => {
            ElMessage.info('已取消删除');
          });
      }
    }
    isDragging.value = false;
    isDragOver.value = false;
    currentDragModel.value = null;
  };
  
  // 在组件挂载时请求数据
  onMounted(() => {
    fetchModelList();
  });
  </script>
  
  <style scoped lang="scss">


  .delete-zone {
    position: fixed;
    bottom: 30px;
    right: 30px;
    width: 200px;
    height: 200px;
    border-radius: 50%;
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