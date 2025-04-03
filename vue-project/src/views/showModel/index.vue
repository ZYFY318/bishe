<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import ThreeModel from '@/components/three/threeModel.vue';
import { reqModelDetail } from '@/api/model';
import { Refresh } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const modelId = Number(route.query.modelId);
const modelName = route.query.modelName as string;

const goBack = () => {
  router.back();
};

// 修改控制参数
const modelLength = ref(50);
const modelWidth = ref(50);
const modelHeight = ref(50);

// 添加重置函数
const handleReset = () => {
  modelLength.value = 50;
  modelWidth.value = 50;
  modelHeight.value = 50;
};

// 将三个维度组合成一个对象传递给子组件
const scale = computed(() => ({
  x: modelLength.value / 50,
  y: modelHeight.value / 50,
  z: modelWidth.value / 50
}));

// 自动旋转控制
const autoRotate = ref(false);

// 颜色选项
const colors = [
  '#ff0000', '#00ff00', '#0000ff', 
  '#ffff00', '#ff00ff', '#00ffff',
  '#000000', '#ffffff', '#808080'
];

const selectColor = (color: string) => {
  console.log('Selected color:', color);
};
</script>

<template>
  <div class="container">
    <!-- 顶部区域：退出键和模型名称 -->
    <div class="header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="model-title">{{ modelName }}</h2>
    </div>

    <!-- 主要内容区域：编辑区和模型展示区 -->
    <div class="main-content">
      <!-- 左侧编辑区 -->
      <div class="edit_container">
        <el-card class="edit-card">
          <div class="edit-item">
            <span>模型长度</span>
            <el-slider v-model="modelLength" :min="10" :max="200" />
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item">
            <span>模型宽度</span>
            <el-slider v-model="modelWidth" :min="10" :max="200" />
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item">
            <span>模型高度</span>
            <el-slider v-model="modelHeight" :min="10" :max="200" />
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item reset-item" @click="handleReset">
            <span>重置</span>
            <el-icon><Refresh /></el-icon>
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item">
            <span>自动旋转</span>
            <el-switch v-model="autoRotate" />
          </div>
        </el-card>
      </div>

      <!-- 右侧模型展示区 -->
      <div class="model-container">
        <ThreeModel 
          :model-id="modelId"
          :model-name="modelName"
          :scale="scale"
          :auto-rotate="autoRotate"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 0;
  margin: 0;
  width: 100%;
  flex: 1;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 0;
  padding: 20px;
  background-color: #f5f5f5;

  .el-button {
    padding: 0;
    font-size: 20px; /* 调整图标大小 */
  }

}

.model-title {
  margin: 0;
  font-size: 24px;
}

.main-content {
  display: flex;
  flex: 1;
  gap: 20px;
  padding: 20px;
  height: calc(100vh - 80px);
  overflow: hidden;
  background-color: #f5f5f5;
}

.edit_container {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.edit-card {
  padding: 15px;
}

.edit-item {
  display: flex;
  flex-direction: column;
  gap: 10px;

  &.reset-item {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    padding: 5px 0;
    
    &:hover {
      color: #409EFF;
      .el-icon {
        transform: rotate(180deg);
      }
    }
    
    .el-icon {
      font-size: 18px;
      transition: transform 0.3s ease;
    }
  }
}

.model-container {
  flex: 1;
  background-color: white;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

</style>
