<template>
  <div class="model-display-container">
    <!-- 顶部导航栏 -->
    <div class="model-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="model-title">3D模型展示平台</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
          全屏
        </el-button>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧控制面板 -->
      <div class="control-panel">
        <el-card class="control-card">
          <template #header>
            <div class="card-header">
              <span>模型控制</span>
            </div>
          </template>
          
          <div class="control-item">
            <span>模型长度</span>
            <el-slider v-model="modelLength" :min="10" :max="200" />
          </div>
          
          <div class="control-item">
            <span>模型宽度</span>
            <el-slider v-model="modelWidth" :min="10" :max="200" />
          </div>
          
          <div class="control-item">
            <span>模型高度</span>
            <el-slider v-model="modelHeight" :min="10" :max="200" />
          </div>
          
          <div class="control-item reset-item" @click="handleReset">
            <span>重置尺寸</span>
            <el-icon><Refresh /></el-icon>
          </div>
          
          <div class="control-item">
            <span>自动旋转</span>
            <el-switch v-model="autoRotate" />
          </div>
          
          <div class="control-item">
            <span>模型颜色</span>
            <div class="color-selector">
              <div 
                v-for="(color, index) in colors" 
                :key="index" 
                class="color-item"
                :class="{ active: selectedColor === color }"
                :style="{ backgroundColor: color }"
                @click="selectColor(color)"
              ></div>
            </div>
          </div>

          <div class="control-item">
            <span>交互指南</span>
            <div class="interaction-guide">
              <el-alert
                title="点击灭火器上的安全销(绿色高亮部分)，将其拔出"
                type="info"
                :closable="false"
                show-icon
              />
              <el-button 
                v-if="isPinPulled"
                size="small" 
                type="primary" 
                @click="resetPin" 
                class="reset-button"
              >
                重置安全销
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 右侧模型展示区 -->
      <div class="model-container">
        <div class="canvas-container">
          <!-- 加载进度条 -->
          <div v-if="loading" class="loading-container">
            <el-progress 
              type="circle"
              :percentage="loadingProgress"
              :stroke-width="6"
              :width="120"
              :show-text="true"
              :status="loadingProgress === 100 ? 'success' : ''"
            >
              <template #default>
                <span class="progress-text">{{ loadingProgress === 100 ? '加载完成' : '加载中...' }}</span>
              </template>
            </el-progress>
          </div>
          <!-- 模型容器 -->
          <div v-show="!loading" ref="canvasContainer" class="model-view"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, FullScreen, Refresh } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import useLayOutSettingStore from '@/stores/modules/setting';

const router = useRouter();

// 返回上一页
const goBack = () => {
  router.back();
};

// 模型名称
const modelName = "本地GLB模型"; 

// 模型路径 - 直接使用public目录下的模型
const modelPath = '/fire_extinguisher.glb'; // 确保public文件夹中有此文件

// 模型尺寸控制参数
const modelLength = ref(50);
const modelWidth = ref(50);
const modelHeight = ref(50);

// 重置尺寸函数
const handleReset = () => {
  modelLength.value = 50;
  modelWidth.value = 50;
  modelHeight.value = 50;
};

// 将三个维度组合成一个对象
const scale = computed(() => ({
  x: modelLength.value / 50,
  y: modelHeight.value / 50,
  z: modelWidth.value / 50
}));

// 自动旋转控制
const autoRotate = ref(false);

// 颜色控制
const selectedColor = ref('#ff0000');

// 颜色选项
const colors = [
  '#ff0000', '#00ff00', '#0000ff', 
  '#ffff00', '#ff00ff', '#00ffff',
  '#000000', '#ffffff', '#808080'
];

// Three.js 相关变量
const canvasContainer = ref(null);
let scene, camera, renderer, controls;
const modelRef = ref(null);

// 交互相关变量
const pinObject = ref(null); // 存储消防栓的引用
const isPinPulled = ref(false);
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
const originalPinPosition = new THREE.Vector3();
const pinPullDistance = 1.0; // 大幅增加拔出距离，使效果更明显

// 加载状态和进度
const loading = ref(true);
const loadingProgress = ref(0);

// 获取主题设置
const layoutSettingStore = useLayOutSettingStore();
// 根据当前主题计算背景颜色
const sceneBackgroundColor = computed(() => {
  return layoutSettingStore.theme === 'dark' ? 0x1e1e2e : 0xffffff;
});

// 直接加载模型文件
const loadModel = () => {
  loading.value = true;
  loadingProgress.value = 0;
  
  const gltfLoader = new GLTFLoader();
  gltfLoader.load(
    modelPath,
    (gltf) => {
      const model = gltf.scene;
      
      model.traverse((child) => {
        if (child.isMesh) {
          // 存储原始材质，以便我们可以更改颜色
          child.originalMaterial = child.material.clone();
          
          // 寻找消防栓部分 (这里需要根据实际模型结构调整)
          // 注意：这里使用了名称匹配，可能需要根据实际模型的命名来调整
          if (child.name.toLowerCase().includes('pin') || 
              child.name.toLowerCase().includes('safety') || 
              child.name.toLowerCase().includes('lever')) {
            // 优先选择安全销(Pin__FE_0)
            if (child.name === 'Pin__FE_0') {
              pinObject.value = child;
              // 存储原始位置
              originalPinPosition.copy(child.position.clone());
              
              // 高亮显示安全销部分
              const highlightMaterial = child.material.clone();
              highlightMaterial.emissive = new THREE.Color(0x00ff00);
              highlightMaterial.emissiveIntensity = 0.5;
              child.material = highlightMaterial;
            } 
            // 如果我们还没有找到Pin对象，则考虑Lever部件
            else if (!pinObject.value && child.name.includes('Lever')) {
              // 存储杠杆部件，但不设为主要交互对象
              if (child.name === 'Lever_Top__FE_0') {
                // 可以在这里存储顶部杠杆引用，以后扩展功能
              }
            }
          }
        }
      });

      // 如果没找到明确的消防栓部件，尝试查找可能的子对象
      if (!pinObject.value) {
        // 直接搜索名字匹配的部件
        model.traverse((searchChild) => {
          if (searchChild.name === 'Pin__FE_0') {
            console.log('通过全局搜索找到安全销');
            pinObject.value = searchChild;
            originalPinPosition.copy(searchChild.position.clone());
            
            // 高亮显示安全销
            if (searchChild.material) {
              const highlightMaterial = searchChild.material.clone();
              highlightMaterial.emissive = new THREE.Color(0x00ff00);
              highlightMaterial.emissiveIntensity = 0.5;
              searchChild.material = highlightMaterial;
            }
          }
        });
      }

      // 计算模型的包围盒
      const box = new THREE.Box3().setFromObject(model);
      const size = box.getSize(new THREE.Vector3());
      
      // 设置模型位置
      model.position.set(0, 0, 0);
      
      // 设置初始缩放
      model.scale.set(
        scale.value.x * 2,
        scale.value.y * 2,
        scale.value.z * 2
      );
      
      model.castShadow = true;
      scene.add(model);
      
      // 保存模型引用
      modelRef.value = model;

      // 调整相机位置以适应模型
      const center = box.getCenter(new THREE.Vector3());
      const maxDim = Math.max(size.x, size.y, size.z);
      camera.position.set(
        maxDim * 2,
        maxDim * 2,
        maxDim * 2
      );
      camera.lookAt(center);
      
      // 应用当前选择的颜色
      applyColor(selectedColor.value);
      
      // 查找安全销
      findPinInScene();
      
      // 如果找到了安全销，给它特殊高亮
      if (pinObject.value) {
        // 高亮显示安全销
        if (pinObject.value.material) {
          const highlightMaterial = pinObject.value.material.clone();
          highlightMaterial.emissive = new THREE.Color(0x00ff00);
          highlightMaterial.emissiveIntensity = 0.8;
          pinObject.value.material = highlightMaterial;
          // 保存原始位置
          originalPinPosition.copy(pinObject.value.position.clone());
        }
      }
      
      // 在模型加载完成后，设置进度为100%并隐藏进度条
      loadingProgress.value = 100;
      setTimeout(() => {
        loading.value = false;
      }, 500);
    },
    (progress) => {
      // 计算加载进度
      const progressPercentage = (progress.loaded / progress.total) * 100;
      loadingProgress.value = progressPercentage;
    },
    (error) => {
      console.error('模型加载失败:', error);
      ElMessage.error('模型加载失败，请检查模型文件路径是否正确');
      loading.value = false;
    }
  );
};

// 应用颜色到模型
const applyColor = (color) => {
  if (!modelRef.value) return;
  
  modelRef.value.traverse((child) => {
    if (child.isMesh && child.originalMaterial && child !== pinObject.value) {
      // 创建新材质并应用选择的颜色
      const newMaterial = child.originalMaterial.clone();
      newMaterial.color.set(color);
      child.material = newMaterial;
    }
  });
};

// 选择颜色
const selectColor = (color) => {
  selectedColor.value = color;
  applyColor(color);
};

// 处理鼠标点击事件
const handleClick = (event) => {
  if (loading.value || !modelRef.value || !scene || !camera) return;
  
  // 计算鼠标在归一化设备坐标中的位置
  const canvas = renderer.domElement;
  const rect = canvas.getBoundingClientRect();
  
  mouse.x = ((event.clientX - rect.left) / canvas.width) * 2 - 1;
  mouse.y = -((event.clientY - rect.top) / canvas.height) * 2 + 1;
  
  // 更新射线
  raycaster.setFromCamera(mouse, camera);
  
  // 检查与消防栓的交叉
  const intersects = raycaster.intersectObjects(scene.children, true);
  
  if (intersects.length > 0) {
    // 直接检查是否点击到了Pin部分
    const clickedPin = intersects.find(item => 
      item.object.name && (item.object.name.includes("Pin") || item.object.name === "Pin__FE_0")
    );
    
    if (clickedPin) {
      // 临时设置点击对象为安全销
      pinObject.value = clickedPin.object;
      // 如果还没有保存原始位置，保存它
      if (!originalPinPosition.x && !originalPinPosition.y && !originalPinPosition.z) {
        originalPinPosition.copy(clickedPin.object.position.clone());
      }
      pullPin();
      return;
    }
    
    // 如果直接方法找不到，再尝试使用祖先方法
    const clickedObject = findAncestor(intersects[0].object, pinObject.value);
    
    if (clickedObject && clickedObject === pinObject.value) {
      pullPin();
    } else {
      // 备用方法：尝试在整个场景中找Pin对象
      if (!pinObject.value && modelRef.value) {
        findPinInScene();
        // 如果找到了，再尝试触发pullPin
        if (pinObject.value) {
          pullPin();
        }
      }
    }
  }
};

// 查找祖先对象（用于检查点击的是否是消防栓的一部分）
const findAncestor = (obj, target) => {
  let current = obj;
  
  while (current) {
    if (current === target) return target;
    current = current.parent;
  }
  
  return null;
};

// 拔出消防栓
const pullPin = () => {
  if (!pinObject.value || isPinPulled.value) return;
  
  // 拔出消防栓动画
  const startPosition = pinObject.value.position.clone();
  const endPosition = startPosition.clone();
  
  // 完全水平拔出方向，向左方向拔出
  endPosition.z -= pinPullDistance * 102.2; // 向左拔出（负X方向）
  // 不再向上或向前移动
  
  // 简单动画
  let progress = 0;
  const duration = 1200; // 延长动画时间，使拔出效果更明显
  const startTime = Date.now();
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数使动画更自然
    const easedProgress = easeOutBack(progress);
    
    // 计算当前位置
    pinObject.value.position.lerpVectors(startPosition, endPosition, easedProgress);
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    } else {
      isPinPulled.value = true;
      ElMessage.success('安全销已拔出！灭火器已准备就绪');
    }
  }
  
  animate();
};

// 缓动函数，使动画更加自然，并有弹回效果
const easeOutBack = (t) => {
  const c1 = 1.70158;
  const c3 = c1 + 1;
  return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
};

// 重置消防栓位置
const resetPin = () => {
  if (!pinObject.value || !isPinPulled.value) return;
  
  // 重置到原始位置的动画
  const startPosition = pinObject.value.position.clone();
  const endPosition = originalPinPosition.clone();
  
  // 简单动画
  let progress = 0;
  const duration = 800; // 毫秒
  const startTime = Date.now();
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数
    const easedProgress = easeInOutQuad(progress);
    
    // 计算当前位置
    pinObject.value.position.lerpVectors(startPosition, endPosition, easedProgress);
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    } else {
      isPinPulled.value = false;
      ElMessage.info('安全销已重置，灭火器已锁定');
    }
  }
  
  animate();
};

// 监听 scale 变化
watch(() => scale.value, (newScale) => {
  if (modelRef.value) {
    modelRef.value.scale.set(
      newScale.x * 2, 
      newScale.y * 2,
      newScale.z * 2
    );
  }
}, { deep: true });

// 监听 autoRotate 变化
watch(() => autoRotate.value, (newValue) => {
  if (controls) {
    controls.autoRotate = newValue;
    controls.autoRotateSpeed = 8.0;
  }
}, { immediate: true });

// 监听主题变化，更新场景背景颜色
watch(() => layoutSettingStore.theme, (newTheme) => {
  if (scene) {
    scene.background = new THREE.Color(sceneBackgroundColor.value);
  }
});

// 切换全屏
const toggleFullscreen = () => {
  const modelContainer = document.querySelector('.model-container') as HTMLElement;
  
  if (!document.fullscreenElement && modelContainer) {
    modelContainer.requestFullscreen().catch(err => {
      ElMessage.error(`全屏错误: ${err.message}`);
    });
  } else {
    document.exitFullscreen();
  }
};

// 在场景中查找Pin对象
const findPinInScene = () => {
  // 递归遍历场景中的所有对象
  const searchInObject = (object) => {
    // 检查当前对象是否为Pin
    if (object.name && (object.name === 'Pin__FE_0' || object.name.includes('Pin'))) {
      pinObject.value = object;
      originalPinPosition.copy(object.position.clone());
      return true;
    }
    
    // 遍历子对象
    if (object.children && object.children.length > 0) {
      for (const child of object.children) {
        if (searchInObject(child)) {
          return true;
        }
      }
    }
    
    return false;
  };
  
  // 从场景开始搜索
  if (scene && scene.children) {
    for (const child of scene.children) {
      if (searchInObject(child)) {
        return true;
      }
    }
  }
  
  return false;
};

// 另一种缓动函数用于重置动画
const easeInOutQuad = (t) => {
  return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
};

onMounted(() => {
  // 获取模型容器
  const container = canvasContainer.value;
  if (!container) return;

  // 创建场景
  scene = new THREE.Scene();
  
  // 根据主题设置背景颜色
  scene.background = new THREE.Color(sceneBackgroundColor.value);

  // 获取容器的尺寸
  const containerWidth = container.clientWidth || 500;
  const containerHeight = container.clientHeight || 500;
  
  // 创建相机和渲染器
  camera = new THREE.PerspectiveCamera(75, containerWidth / containerHeight, 0.1, 1000);
  camera.position.set(5, 5, 5);

  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(containerWidth, containerHeight);
  renderer.shadowMap.enabled = true;
  container.appendChild(renderer.domElement);

  // 添加灯光
  const ambientLight = new THREE.AmbientLight(0xffffff, 3.5);
  scene.add(ambientLight);

  const pointLight = new THREE.PointLight(0xffffff, 2, 100);
  pointLight.position.set(5, 5, 5);
  pointLight.castShadow = true;
  scene.add(pointLight);

  // 初始化控制器
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.rotateSpeed = 0.4;
  controls.autoRotate = autoRotate.value;
  controls.autoRotateSpeed = 8.0;
  controls.enableZoom = true;
  controls.enablePan = false;
  controls.minDistance = 2;
  controls.maxDistance = 10;
  
  // 创建渲染循环函数
  function animate() {
    requestAnimationFrame(animate);
    if (controls) {
      controls.update();
    }
    if (renderer && scene && camera) {
      renderer.render(scene, camera);
    }
  }
  
  // 开始渲染循环
  animate();

  // 加载模型
  loadModel();

  // 添加鼠标点击事件监听器
  renderer.domElement.addEventListener('click', handleClick);

  // 监听容器大小变化
  const resizeObserver = new ResizeObserver(entries => {
    for (const entry of entries) {
      const { width, height } = entry.contentRect;
      if (camera && renderer) {
        camera.aspect = width / height;
        camera.updateProjectionMatrix();
        renderer.setSize(width, height);
      }
    }
  });

  resizeObserver.observe(container);

  // 在组件卸载时清理资源
  onUnmounted(() => {
    resizeObserver.disconnect();
    if (renderer && renderer.domElement) {
      renderer.domElement.removeEventListener('click', handleClick);
    }
    if (controls) controls.dispose();
    if (renderer) {
      renderer.dispose();
      if (container.contains(renderer.domElement)) {
        container.removeChild(renderer.domElement);
      }
    }
  });
});
</script>

<style scoped lang="scss">
.model-display-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
}

.model-header {
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
  
  .model-title {
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

.main-content {
  display: flex;
  flex: 1;
  height: calc(100vh - 60px);
  overflow: hidden;
}

.control-panel {
  width: 300px;
  padding: 16px;
  overflow-y: auto;
  border-right: 1px solid var(--border-color);
}

.control-card {
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  
  .card-header {
    color: var(--text-color);
    font-weight: 600;
  }
}

.control-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  color: var(--text-color);
  
  &.reset-item {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    padding: 5px 0;
    
    &:hover {
      color: var(--primary-color);
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

.interaction-guide {
  display: flex;
  flex-direction: column;
  gap: 10px;
  
  .reset-button {
    margin-top: 10px;
  }
}

.color-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 5px;
}

.color-item {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: transform 0.2s ease;
  
  &.active {
    border-color: var(--primary-color);
    transform: scale(1.1);
  }
  
  &:hover {
    transform: scale(1.1);
  }
}

.model-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--card-bg);
}

.canvas-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.model-view {
  width: 100%;
  height: 100%;
}

.loading-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  padding: 20px;
  background-color: rgba(var(--bg-color-rgb), 0.7);
  border-radius: 8px;
}

.progress-text {
  font-size: 14px;
  color: var(--text-color);
}

:deep(.el-progress__text) {
  color: var(--text-color) !important;
}
</style> 