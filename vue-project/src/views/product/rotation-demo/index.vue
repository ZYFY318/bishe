<template>
  <div class="rotation-demo-container">
    <!-- 顶部导航栏 -->
    <div class="demo-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="demo-title">Three.js 旋转演示</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="rotateInYZPlane">在YZ平面旋转90度</el-button>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 渲染区域 -->
      <div class="render-container">
        <!-- Three.js 将在这里渲染 -->
        <div ref="canvasContainer" class="canvas-wrapper"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const router = useRouter();

// 返回上一页
const goBack = () => {
  router.back();
};

// Three.js 相关变量
const canvasContainer = ref(null);
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let renderer: THREE.WebGLRenderer;
let controls: OrbitControls;
let axesHelper: THREE.AxesHelper;
let cylinder: THREE.Mesh;
let boundingBox: THREE.Box3;
let boxHelper: THREE.BoxHelper;

// 在YZ平面旋转90度
const rotateInYZPlane = () => {
  if (!cylinder) return;
  
  // 将角度转换为弧度
  const angleRad = THREE.MathUtils.degToRad(90);
  
  // 计算圆柱体顶部位置（因为初始位置是在原点上方1个单位，高度为2，所以顶部在y=2处）
  const pivotPoint = new THREE.Vector3(0, 4, -2);
  
  // 第一步：将物体位置减去旋转中心点，相当于把旋转中心移到原点
  cylinder.position.sub(pivotPoint);
  console.log(cylinder.position)
  // 第二步：创建一个四元数，设置为绕X轴旋转90度
  const quaternion = new THREE.Quaternion();
  quaternion.setFromAxisAngle(new THREE.Vector3(1, 0, 0), angleRad);
  
  // 第三步：应用旋转到位置
  cylinder.position.applyQuaternion(quaternion);
  
  // 第四步：应用旋转到物体的方向
  cylinder.quaternion.premultiply(quaternion);
  
  // 第五步：将旋转中心点加回来，恢复到世界坐标系中的正确位置
  cylinder.position.add(pivotPoint);
  
  // 更新包围盒
  if (boxHelper) {
    boxHelper.update();
  }
};

// 初始化Three.js场景
const initThreeJs = () => {
  const container = canvasContainer.value;
  if (!container) return;
  
  // 获取容器尺寸
  const width = container.clientWidth;
  const height = container.clientHeight;
  
  // 创建场景
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0xf0f0f0);
  
  // 创建相机
  camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);
  camera.position.set(5, 5, 5);
  camera.lookAt(0, 0, 0);
  
  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(width, height);
  renderer.setPixelRatio(window.devicePixelRatio);
  container.appendChild(renderer.domElement);
  
  // 添加轨道控制器
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  
  // 添加坐标轴辅助
  axesHelper = new THREE.AxesHelper(3);
  scene.add(axesHelper);
  
  // 添加坐标轴标签
  addAxisLabels();
  
  // 添加网格辅助
  const gridHelper = new THREE.GridHelper(10, 10, 0x888888, 0xcccccc);
  scene.add(gridHelper);
  
  // 创建圆柱体
  createCylinder();
  
  // 添加灯光
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
  scene.add(ambientLight);
  
  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
  directionalLight.position.set(5, 10, 5);
  scene.add(directionalLight);
  
  // 开始渲染循环
  animate();
  
  // 监听窗口大小变化
  window.addEventListener('resize', onWindowResize);
};

// 创建圆柱体
const createCylinder = () => {
  // 创建圆柱体几何体
  const geometry = new THREE.CylinderGeometry(0.5, 0.5, 2, 32);
  
  // 创建材质
  const material = new THREE.MeshStandardMaterial({ 
    color: 0x1a73e8,
    metalness: 0.3,
    roughness: 0.4
  });
  
  // 创建圆柱体
  cylinder = new THREE.Mesh(geometry, material);
  cylinder.position.set(0, 2, -2); // 修改位置到(0,2,-2)
  scene.add(cylinder);
  
  // 创建和添加包围盒
  boundingBox = new THREE.Box3().setFromObject(cylinder);
  boxHelper = new THREE.BoxHelper(cylinder, 0xff0000);
  scene.add(boxHelper);
  
  // 添加一个小球标记顶部旋转点
  const pivotMarker = new THREE.Mesh(
    new THREE.SphereGeometry(0.1, 16, 16),
    new THREE.MeshBasicMaterial({ color: 0xff9900 })
  );
  pivotMarker.position.set(0, 3, -2); // 调整位置到圆柱体顶部
  scene.add(pivotMarker);
};

// 添加坐标轴标签
const addAxisLabels = () => {
  // 创建X轴标签
  const xMarker = new THREE.Mesh(
    new THREE.SphereGeometry(0.15, 16, 16),
    new THREE.MeshBasicMaterial({ color: 0xff0000 })
  );
  xMarker.position.set(3.5, 0, 0);
  scene.add(xMarker);
  
  // 创建Y轴标签
  const yMarker = new THREE.Mesh(
    new THREE.SphereGeometry(0.15, 16, 16),
    new THREE.MeshBasicMaterial({ color: 0x00ff00 })
  );
  yMarker.position.set(0, 3.5, 0);
  scene.add(yMarker);
  
  // 创建Z轴标签
  const zMarker = new THREE.Mesh(
    new THREE.SphereGeometry(0.15, 16, 16),
    new THREE.MeshBasicMaterial({ color: 0x0000ff })
  );
  zMarker.position.set(0, 0, 3.5);
  scene.add(zMarker);
};

// 响应窗口大小变化
const onWindowResize = () => {
  if (!canvasContainer.value || !camera || !renderer) return;
  
  const width = canvasContainer.value.clientWidth;
  const height = canvasContainer.value.clientHeight;
  
  camera.aspect = width / height;
  camera.updateProjectionMatrix();
  renderer.setSize(width, height);
};

// 动画循环
const animate = () => {
  requestAnimationFrame(animate);
  
  if (controls) {
    controls.update();
  }
  
  // 更新包围盒
  if (cylinder && boxHelper) {
    boxHelper.update();
  }
  
  if (renderer && scene && camera) {
    renderer.render(scene, camera);
  }
};

// 生命周期钩子
onMounted(() => {
  initThreeJs();
});

onUnmounted(() => {
  // 清理资源
  window.removeEventListener('resize', onWindowResize);
  
  if (renderer && canvasContainer.value) {
    canvasContainer.value.removeChild(renderer.domElement);
    renderer.dispose();
  }
  
  if (controls) {
    controls.dispose();
  }
});
</script>

<style scoped lang="scss">
.rotation-demo-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color, #ffffff);
}

.demo-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-color, #e4e7ed);
  
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
    color: var(--text-color, #303133);
  }
  
  .demo-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color, #303133);
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
      background-color: var(--primary-color, #409eff);
      border-radius: 2px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
  }
}

.main-content {
  display: flex;
  flex: 1;
  height: calc(100vh - 60px);
  overflow: hidden;
}

.render-container {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
}

.canvas-wrapper {
  flex: 1;
  width: 100%;
  position: relative;
}
</style> 