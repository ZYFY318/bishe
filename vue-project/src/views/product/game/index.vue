<template>
  <div class="webgl-game-container">
    <!-- 顶部导航栏 -->
    <div class="game-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="game-title">3D互动学习平台</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
          全屏
        </el-button>
      </div>
    </div>
    
    <!-- 游戏展示区域 -->
    <div class="game-container">
      <div v-if="!gameLoaded" class="game-placeholder">
        <el-empty description="3D内容加载中...">
          <template #image>
            <el-icon class="game-loading-icon"><Loading /></el-icon>
          </template>
          <el-button type="primary" @click="loadGame">加载3D内容</el-button>
        </el-empty>
      </div>
      <div v-else class="unity-container" ref="unityContainer">
        <!-- Unity WebGL内容将通过JavaScript动态加载到这里 -->
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, FullScreen, Loading } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();

// 返回上一页
const goBack = () => {
  router.back();
};

// 游戏状态
const gameLoaded = ref(false);

// Unity实例引用
const unityInstance = ref(null);
const unityContainer = ref(null);

// 加载游戏
const loadGame = () => {
  ElMessage.info('正在加载3D内容,请稍候...');
  
  // 模拟加载过程
  setTimeout(() => {
    gameLoaded.value = true;
    ElMessage.success('3D内容加载完成!');
    
    // 这里在实际项目中会调用Unity WebGL的加载函数
    // 例如: loadUnityGame(unityContainer.value);
  }, 2000);
};

// 切换全屏
const toggleFullscreen = () => {
  if (!gameLoaded.value) {
    loadGame();
    return;
  }
  
  const gameContainer = document.querySelector('.game-container') as HTMLElement;
  
  if (!document.fullscreenElement && gameContainer) {
    gameContainer.requestFullscreen().catch(err => {
      ElMessage.error(`全屏错误: ${err.message}`);
    });
  } else {
    document.exitFullscreen();
  }
};

// 在组件挂载时初始化Unity WebGL加载器
onMounted(() => {
  // 这里通常会加载Unity的加载器脚本
  // 例如:
  // const script = document.createElement('script');
  // script.src = '/Build/UnityLoader.js';
  // script.onload = () => { 
  //   unityInstance.value = UnityLoader.instantiate(
  //     unityContainer.value, 
  //     '/Build/WebGLBuild.json',
  //     { onProgress: progress => console.log(progress) }
  //   );
  // };
  // document.body.appendChild(script);
});

// 在组件卸载时清理Unity实例
onUnmounted(() => {
  // 如果Unity实例存在，则进行清理
  // 例如:
  // if (unityInstance.value) {
  //   unityInstance.value.Quit();
  // }
});
</script>

<style scoped lang="scss">
.webgl-game-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-color);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .back-button {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 14px;
    color: var(--text-color);
  }
  
  .game-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color);
    position: relative;
    padding-left: 12px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 20px;
      background-color: var(--primary-color);
      border-radius: 2px;
    }
  }
}

.game-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #222;
}

.game-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .game-loading-icon {
    font-size: 48px;
    color: var(--primary-color);
    animation: rotate 2s linear infinite;
  }
}

.unity-container {
  width: 100%;
  height: 100%;
  background-color: #000;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style> 