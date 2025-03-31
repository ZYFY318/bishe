<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// 获取从 ItemCard 传递过来的模型路径和名称
const modelPath = route.query.modelPath;
const modelName = ref(route.query.modelName || '未命名模型'); // 添加模型名称


const goBack = () => {
  router.back();
};

// 修改控制参数，添加 watch
const modelLength = ref(50);
const modelWidth = ref(50);
const modelHeight = ref(50);

// 将三个维度组合成一个对象传递给子组件
const modelScale = computed(() => ({
  x: modelLength.value / 50, // 将 0-100 的值映射到 0-2 的比例
  y: modelHeight.value / 50,
  z: modelWidth.value / 50
}));

// 颜色选项
const colors = [
  '#ff0000', '#00ff00', '#0000ff', 
  '#ffff00', '#ff00ff', '#00ffff',
  '#000000', '#ffffff', '#808080'
];

const selectColor = (color) => {
  // 处理颜色选择
  console.log('Selected color:', color);
};

// 在其他 ref 变量后添加
const autoRotate = ref(false);
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
            <el-slider v-model="modelLength" :min="0" :max="100" />
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item">
            <span>模型宽度</span>
            <el-slider v-model="modelWidth" :min="0" :max="100" />
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item">
            <span>模型高度</span>
            <el-slider v-model="modelHeight" :min="0" :max="100" />
          </div>
        </el-card>

        <el-card class="edit-card">
          <div class="edit-item">
            <span>模型颜色</span>
            <div class="color-blocks">
              <div v-for="color in colors" 
                   :key="color" 
                   :style="{ backgroundColor: color }"
                   class="color-block"
                   @click="selectColor(color)">
              </div>
            </div>
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
        <threeModel 
          :modelPath="modelPath"
          :modelName="modelName"
          :scale="modelScale"
          :autoRotate="autoRotate"
        ></threeModel>
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
}

.color-blocks {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.color-block {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ddd;

  &:hover {
    transform: scale(1.1);
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
