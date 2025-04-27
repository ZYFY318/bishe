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
      <!-- 右侧模型展示区 - 现在占据全部空间 -->
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
          
          <!-- 拿起灭火器提示框 - 只在未拿起灭火器时显示 -->
          <div v-show="!loading && !isExtinguisherPickedUp" class="pick-up-hint">
            <div class="hint-content">
              <el-icon><Pointer /></el-icon>
              <span>点击灭火器将其拿起</span>
              <el-button type="primary" size="small" @click="pickUpExtinguisher">
                拿起灭火器
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 操作指南对话框 -->
    <el-dialog
      v-model="guideDialogVisible"
      title="灭火器操作指南"
      width="500px"
      center
    >
      <div class="guide-content">
        <p class="guide-title">请按照以下步骤操作灭火器：</p>
        <ol class="guide-steps">
          <li>点击<strong>灭火器</strong>将其拿起</li>
          <li>点击<strong>安全销</strong>将其拔出，解除安全锁定</li>
          <li>点击<strong>喷嘴</strong>准备好喷射位置</li>
          <li>点击<strong>压把手柄</strong>开始喷射灭火剂</li>
        </ol>
        <p class="guide-note">
          <el-icon><Warning /></el-icon>
          注意：必须按照正确顺序操作才能成功使用灭火器
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="guideDialogVisible = false">
            开始操作
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 游戏完成对话框 -->
    <el-dialog
      v-model="gameCompletedDialogVisible"
      title="游戏完成"
      width="500px"
      center
    >
      <div class="guide-content">
        <p class="guide-title">恭喜你成功灭火！</p>
        <p class="guide-note">
          <el-icon><Check /></el-icon>
          你已经成功扑灭了火源，游戏完成！
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="gameCompletedDialogVisible = false">
            继续游戏
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 重置按钮 - 悬浮在右下角 -->
    <div class="floating-button reset-button">
      <el-button
        type="danger"
        size="large"
        circle
        @click="resetAll"
        :icon="Refresh"
        title="重置"
      ></el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, FullScreen, Refresh, Warning, Pointer, Check } from '@element-plus/icons-vue';
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

// 操作指南对话框显示控制
const guideDialogVisible = ref(true);

// 游戏完成对话框显示控制
const gameCompletedDialogVisible = ref(false);

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
let globalAxesHelper = null; // 全局坐标轴辅助对象

// 添加粒子系统相关变量
let particleSystem = null;
let particles = null;
const particleCount = 2000; // 粒子数量
let particleGeometry = null;
let particleMaterial = null;
let isSprayingFoam = false; // 是否正在喷射泡沫
let sprayDirection = new THREE.Vector3(0, 0, -1); // 喷射方向
let clock = null; // 用于动画计时

// 交互相关变量
const pinObject = ref(null); // 存储安全销的引用
const leverTopObject = ref(null); // 存储压把顶部的引用
const leverBottomObject = ref(null); // 存储压把底部的引用
const nozzleObject = ref(null); // 存储喷管的引用
let nozzlePivotPoint = new THREE.Vector3(); // 存储喷嘴旋转轴心点
const nozzlePivotHelper = ref(null); // 存储喷管轴心点辅助对象
const nozzleBoxHelper = ref(null); // 存储喷管包围盒辅助对象
const pivotPointSphere = ref(null); // 用于显示轴心点的绿色球体
const isPinPulled = ref(false); // 安全销是否已拔出
const isLeverPressed = ref(false); // 压把是否已按下
const isNozzleRaised = ref(false); // 喷管是否已立起
const raycaster = new THREE.Raycaster();
const mouse = new THREE.Vector2();
const originalPinPosition = new THREE.Vector3();
const originalLeverTopPosition = new THREE.Vector3(); // 存储压把顶部原始位置
const originalLeverTopRotation = new THREE.Euler(); // 存储压把顶部原始旋转
const originalNozzleRotation = new THREE.Euler(); // 存储喷管原始旋转
const pinPullDistance = 1.0; // 增加拔出距离，使效果更明显
const currentHoveredObject = ref(null); // 当前鼠标悬停的对象
const originalMaterials = new Map(); // 存储原始材质

// 火焰相关变量
let fireParticleSystem = null;
let fireGeometry = null;
let fireMaterial = null;
const fireParticleCount = 1000;
const isFireActive = ref(true); // 火焰是否激活
const firePosition = new THREE.Vector3(0, 0, -2.5); // 修改火焰位置到正前方较低的位置
const fireExtinguishDuration = 3000; // 灭火持续时间(毫秒)
let fireExtinguishTimeout = null; // 灭火定时器
let fireExtinguishProgress = 0; // 灭火进度 (0-1)
let isExtinguishing = false; // 是否正在灭火

// 添加火源半球相关变量
let fireDomeMesh = null;
const fireDomeRadius = 1.2; // 半球半径
const fireDomeColor = 0x00ff00; // 绿色
const fireDomeOpacity = 0.2; // 透明度
let particlesInDomeCount = 0; // 进入半球的粒子数量
const requiredParticlesInDome = 10000; // 需要多少粒子进入半球才开始灭火
let totalParticlesHitCount = 0; // 总计进入半球的粒子数量
const fireScaleReductionRate = 0.05; // 每批粒子减少火焰的比例（5%）

// 加载状态和进度
const loading = ref(true);
const loadingProgress = ref(0);

// 获取主题设置
const layoutSettingStore = useLayOutSettingStore();
// 根据当前主题计算背景颜色
const sceneBackgroundColor = computed(() => {
  return layoutSettingStore.theme === 'dark' ? 0x1e1e2e : 0xffffff;
});

// 添加状态变量
const isExtinguisherPickedUp = ref(false); // 是否已拿起灭火器

// 键盘控制相关变量
const keysPressed = ref({
  w: false,
  a: false,
  s: false,
  d: false
});
const rotationSpeed = 0.05; // 旋转速度

// 直接加载模型文件
const loadModel = () => {
  loading.value = true;
  loadingProgress.value = 0;
  
  const gltfLoader = new GLTFLoader();
  gltfLoader.load(
    modelPath,
    (gltf) => {
      const model = gltf.scene;
      
      // 保存模型引用以便后续找到压把部件
      // 注意：我们会寻找名称包含"lever"、"handle"或相关词的部件
      let leverTop = null;
      let leverBottom = null;

      model.traverse((child) => {
        if (child.isMesh) {
          // 存储原始材质
          child.originalMaterial = child.material.clone();
          
          // 查找关键部件
          if (child.name.toLowerCase().includes('pin') || 
              child.name.toLowerCase().includes('safety')) {
            // 识别安全销
            if (child.name === 'Pin__FE_0' || child.name.toLowerCase().includes('pin')) {
              pinObject.value = child;
              originalPinPosition.copy(child.position.clone());
              
              // 删除安全销初始高亮
            } 
          } else if (child.name.toLowerCase().includes('lever') || 
                    child.name.toLowerCase().includes('handle') ||
                    child.name.toLowerCase().includes('trigger')) {
            // 识别压把部件
            if (child.name.toLowerCase().includes('top') || 
                child.name === 'Lever_Top__FE_0') {
              leverTop = child;
            } else if (child.name.toLowerCase().includes('bottom') || 
                      child.name === 'Lever_Bottom__FE_0') {
              leverBottom = child;
            }
          } else if (child.name.toLowerCase().includes('nozzle') || 
                    child.name.toLowerCase().includes('hose') ||
                    child.name.toLowerCase().includes('tube')) {
            // 识别喷管部件
            nozzleObject.value = child;
            console.log(nozzleObject.value.position)
            // 保存当前旋转状态
            if (child.rotation) {
              originalNozzleRotation.copy(child.rotation.clone());
            }
            
            // 计算喷管的包围盒
            const box = new THREE.Box3().setFromObject(child);
            const size = box.getSize(new THREE.Vector3());
            const boxCenter = box.getCenter(new THREE.Vector3()); // 获取包围盒中心
            
            // 设置喷嘴的旋转轴心点 - 在喷管的顶部（根据模型朝向可能需要调整）
            // 假设喷管是沿着Y轴向上的，我们取喷管顶部为轴心点
            nozzlePivotPoint = new THREE.Vector3(0, size.y, -size.z);
          }
        }
      });

      // 将找到的压把部件存储到Vue引用中
      leverTopObject.value = leverTop;
      leverBottomObject.value = leverBottom;

      // 计算模型的包围盒
      const box = new THREE.Box3().setFromObject(model);
      const size = box.getSize(new THREE.Vector3());
      
      // 设置模型位置
      model.position.set(0, 0, 0);
      
      // 设置初始缩放
      model.scale.set(
        scale.value.x * 0.8, // 减小初始模型尺寸，原本是 1.2
        scale.value.y * 0.8, // 减小初始模型尺寸，原本是 1.2
        scale.value.z * 0.8  // 减小初始模型尺寸，原本是 1.2
      );
      
      model.castShadow = true;
      scene.add(model);
      
      // 保存模型引用
      modelRef.value = model;

      // 调整相机位置以适应模型
      const center = box.getCenter(new THREE.Vector3());
      const maxDim = Math.max(size.x, size.y, size.z);
      camera.position.set(
        maxDim * 3.5, // 增加相机距离，原本是 maxDim * 2
        maxDim * 3.5, // 增加相机距离，原本是 maxDim * 2
        maxDim * 3.5  // 增加相机距离，原本是 maxDim * 2
      );
      camera.lookAt(center);
      
      // 应用当前选择的颜色
      applyColor(selectedColor.value);
      
      // 查找安全销
      findPinInScene();
      
      // 如果找到了安全销，保存原始位置
      if (pinObject.value) {
        // 保存原始位置
        originalPinPosition.copy(pinObject.value.position.clone());
      }
      
      // 如果找到了顶部压把，存储其原始位置和旋转
      if (leverTop) {
        originalLeverTopPosition.copy(leverTop.position.clone());
        if (leverTop.rotation) {
          originalLeverTopRotation.copy(leverTop.rotation.clone());
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

// 处理鼠标移动事件
const handleMouseMove = (event) => {
  if (loading.value || !modelRef.value || !scene || !camera) return;
  
  // 计算鼠标在归一化设备坐标中的位置
  const canvas = renderer.domElement;
  const rect = canvas.getBoundingClientRect();
  
  mouse.x = ((event.clientX - rect.left) / canvas.width) * 2 - 1;
  mouse.y = -((event.clientY - rect.top) / canvas.height) * 2 + 1;
  
  // 更新射线
  raycaster.setFromCamera(mouse, camera);
  
  // 检查射线与模型的交叉
  const intersects = raycaster.intersectObjects(scene.children, true);
  
  // 先恢复之前高亮的对象
  resetHighlight();
  
  if (intersects.length > 0) {
    // 无论顺序，查找所有可交互部件
    let interactiveObject = null;
    let interactiveType = "";
    
    // 1. 先检查是否是安全销
    const pin = findInteractiveObjectByType(intersects, "Pin");
    if (pin) {
      interactiveObject = pin;
      interactiveType = "Pin";
    }
    
    // 2. 喷管
    if (!interactiveObject) {
      const nozzle = findInteractiveObjectByType(intersects, "Nozzle");
      if (nozzle) {
        interactiveObject = nozzle;
        interactiveType = "Nozzle";
      }
    }
    
    // 3. 压把
    if (!interactiveObject) {
      const lever = findInteractiveObjectByType(intersects, "Lever");
      if (lever) {
        interactiveObject = lever;
        interactiveType = "Lever";
      }
    }
    
    // 如果找到了交互对象，高亮它
    if (interactiveObject) {
      highlightObject(interactiveObject, interactiveType);
    }
  }
};

// 根据类型查找交互对象
const findInteractiveObjectByType = (intersects, type) => {
  // 查找直接匹配的对象
  const found = intersects.find(item => 
    item.object.name && (
      item.object.name.includes(type) || 
      (type === "Pin" && item.object.name === "Pin__FE_0") ||
      (type === "Nozzle" && (item.object.name.includes("Hose") || item.object.name.includes("Tube"))) ||
      (type === "Lever" && (item.object.name.includes("Handle") || item.object.name.includes("Trigger")))
    )
  );
  
  if (found) return found.object;
  
  // 查找匹配的祖先对象
  const targetObject = type === "Pin" ? pinObject.value : 
                      type === "Nozzle" ? nozzleObject.value : 
                      leverTopObject.value || leverBottomObject.value;
  
  if (!targetObject) return null;
  
  const ancestor = findAncestor(intersects[0].object, targetObject);
  return ancestor;
};

// 高亮对象
const highlightObject = (obj, type) => {
  if (!obj) return;
  
  // 保存当前悬停的对象
  currentHoveredObject.value = { object: obj, type };
  
  // 如果是网格对象，更改材质
  if (obj.isMesh && obj.material) {
    // 保存原始材质
    if (!originalMaterials.has(obj.uuid)) {
      originalMaterials.set(obj.uuid, obj.material.clone());
    }
    
    // 创建高亮材质 (复制当前材质并添加发光效果)
    const highlightMaterial = obj.material.clone();
    highlightMaterial.emissive = new THREE.Color(0x00ff00); // 绿色高亮
    highlightMaterial.emissiveIntensity = 0.6;
    obj.material = highlightMaterial;
    
    // 更新鼠标样式
    renderer.domElement.style.cursor = 'pointer';
  }
};

// 重置高亮
const resetHighlight = () => {
  if (currentHoveredObject.value) {
    const obj = currentHoveredObject.value.object;
    
    // 恢复原始材质
    if (obj.isMesh && originalMaterials.has(obj.uuid)) {
      obj.material = originalMaterials.get(obj.uuid);
    }
    
    // 重置鼠标样式
    renderer.domElement.style.cursor = 'auto';
    
    // 清除当前悬停对象
    currentHoveredObject.value = null;
  }
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
  
  // 检查射线与模型的交叉
  const intersects = raycaster.intersectObjects(scene.children, true);
  
  console.log("handleClick - 找到的交叉对象数量:", intersects.length);
  
  if (intersects.length > 0) {
    // 如果灭火器尚未拿起
    if (!isExtinguisherPickedUp.value) {
      // console.log("灭火器未拿起状态");
      
      // // 检查是否点击了灭火器，打印所有相交对象的名称，以便调试
      // intersects.forEach((item, index) => {
      //   console.log(`相交对象 ${index}:`, item.object.name || "未命名对象", item.object);
      // });
      
      // 只有点击灭火器模型或其组件时才拿起灭火器
      // 过滤掉地面和其他非灭火器物体
      const clickedFireExtinguisher = intersects.some(item => {
        // 排除明确知道的地面对象（通常名称中包含ground或plane，或者是大的平面几何体）
        const isGround = item.object.name && 
          (item.object.name.toLowerCase().includes('ground') || 
           item.object.name.toLowerCase().includes('plane') ||
           item.object.name.toLowerCase().includes('floor'));
        
        // 通过判断物体的几何体类型来排除地面（通常是平面几何体）
        const isPlaneGeometry = item.object.geometry && 
          (item.object.geometry.type === 'PlaneGeometry' || 
           item.object.geometry.type === 'PlaneBufferGeometry');
        
        // 检查是否是灭火器的一部分，通过名称判断
        const isFireExtinguisherPart = item.object.name && 
          (item.object.name.includes('FE_0') || 
           item.object.name.toLowerCase().includes('canister') ||
           item.object.name.toLowerCase().includes('nozzle') ||
           item.object.name.toLowerCase().includes('lever') ||
           item.object.name.toLowerCase().includes('pin') ||
           item.object.name.toLowerCase().includes('handle'));
           
        // 如果不是地面，并且是灭火器的一部分或者是模型的子对象
        return !isGround && !isPlaneGeometry && 
          (isFireExtinguisherPart || (modelRef.value && (item.object === modelRef.value || isPartOfModel(item.object, modelRef.value))));
      });
      
      console.log("是否点击了灭火器:", clickedFireExtinguisher);
      
      if (clickedFireExtinguisher) {
        pickUpExtinguisher();
        return;
      }
    } else {
      // 已拿起灭火器，检查点击的是什么部件
      const clickedPin = findInteractiveObjectByType(intersects, "Pin");
      const clickedNozzle = findInteractiveObjectByType(intersects, "Nozzle");
      const clickedLever = findInteractiveObjectByType(intersects, "Lever");
      
      // 执行原有的交互逻辑...
      // 先检查是否是正确的交互顺序
      if (!isPinPulled.value) {
        // 第一步：应该拔出安全销
        if (clickedPin) {
          if (!originalPinPosition.x && !originalPinPosition.y && !originalPinPosition.z) {
            originalPinPosition.copy(clickedPin.position.clone());
          }
          pullPin();
          return;
        } else if (clickedNozzle) {
          // 点击了喷管，但还没拔出安全销
          ElMessage.warning('请先拔出安全销！');
          return;
        } else if (clickedLever) {
          // 点击了压把，但还没拔出安全销
          ElMessage.warning('请先拔出安全销！');
          return;
        }
      } 
      else if (isPinPulled.value && !isNozzleRaised.value) {
        // 第二步：应该立起喷管
        if (clickedNozzle) {
          raiseNozzle();
          return;
        } else if (clickedLever) {
          // 点击了压把，但还没准备喷管
          ElMessage.warning('请先点击喷管，准备好喷嘴后再按压把手！');
          return;
        } else if (clickedPin) {
          // 点击了已拔出的安全销
          ElMessage.info('安全销已拔出，请继续操作喷管！');
          return;
        }
      }
      else if (isPinPulled.value && isNozzleRaised.value && !isLeverPressed.value) {
        // 第三步：应该按压把手
        if (clickedLever) {
          pressLever(clickedLever);
          return;
        } else if (clickedPin) {
          // 点击了已拔出的安全销
          ElMessage.info('安全销已拔出，请按压把手开始喷射！');
          return;
        } else if (clickedNozzle) {
          // 点击了已立起的喷管
          ElMessage.info('喷嘴已就位，请按压把手开始喷射！');
          return;
        }
      }
      else if (isPinPulled.value && isNozzleRaised.value && isLeverPressed.value) {
        // 灭火器已完全操作完毕
        if (clickedPin || clickedNozzle || clickedLever) {
          ElMessage.success('灭火器操作完成，正在喷射灭火剂！');
          return;
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
  endPosition.x += pinPullDistance * 2.2; // 向左拔出（负z方向）
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

// 重置灭火器（安全销和压把）
const reset = () => {
  console.log("重置灭火器和火焰状态");
  
  // 重置灭火器状态
  isPinPulled.value = false;
  isLeverPressed.value = false;
  isNozzleRaised.value = false;
  isExtinguisherPickedUp.value = false;
  
  // 停止喷射泡沫
  stopSprayingFoam();
  
  // 清除所有残余的泡沫粒子
  if (particleSystem) {
    scene.remove(particleSystem);
    if (particleGeometry) particleGeometry.dispose();
    if (particleMaterial) particleMaterial.dispose();
    particleSystem = null;
    particleGeometry = null;
    particleMaterial = null;
  }
  
  // 重置火焰状态
  isFireActive.value = true;
  isExtinguishing = false;
  fireExtinguishProgress = 0;
  particlesInDomeCount = 0;
  totalParticlesHitCount = 0; // 重置总计粒子数
  
  // 清除定时器
  if (fireExtinguishTimeout) {
    clearTimeout(fireExtinguishTimeout);
    fireExtinguishTimeout = null;
  }
  
  // 重建火焰系统
  if (fireParticleSystem) {
    scene.remove(fireParticleSystem);
    fireParticleSystem = null;
  }
  if (fireGeometry) {
    fireGeometry.dispose();
    fireGeometry = null;
  }
  if (fireMaterial) {
    fireMaterial.dispose();
    fireMaterial = null;
  }
  createFireParticles();
  
  // 注意：不移除半球，让它保持存在作为开发辅助工具
  
  // 重置安全销位置
  resetPinPosition();
  
  // 重置压把位置
  resetLeverPosition();
  
  // 重置喷管位置
  resetNozzlePosition();
  
  // 重置灭火器位置
  resetExtinguisherPosition();
  
  // 重置完成后给用户提示
  ElMessage.success('已重置灭火器和火焰状态');
};

// 重置安全销位置的函数
const resetPinPosition = () => {
  if (!pinObject.value || !originalPinPosition) return;
  
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
    }
  }
  
  animate();
};

// 重置压把位置的函数
const resetLeverPosition = () => {
  if (!leverTopObject.value || !originalLeverTopRotation) return;
  
  const leverToAnimate = leverTopObject.value;
  const currentRotation = leverToAnimate.rotation.clone();
  
  // 简单动画
  let progress = 0;
  const duration = 600; // 毫秒
  const startTime = Date.now();
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数
    const easedProgress = easeInOutQuad(progress);
    
    // 恢复到原始旋转
    leverToAnimate.rotation.x = currentRotation.x + (originalLeverTopRotation.x - currentRotation.x) * easedProgress;
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    }
  }
  
  animate();
};

// 喷管立起动画函数
const raiseNozzle = () => {
  if (!nozzleObject.value || isNozzleRaised.value) return;
  
  // 获取喷管对象
  const nozzle = nozzleObject.value;
  // 设置喷管的新位置
  nozzle.position.set(0, 9.9, -2.0);

  // 计算喷管的包围盒
  const box = new THREE.Box3().setFromObject(nozzle);
  const center = box.getCenter(new THREE.Vector3());
  const size = box.getSize(new THREE.Vector3());
  
  // // 计算喷嘴顶部位置作为旋转中心点
  // const pivotPoint = nozzle.position.clone();
  
  // 将物体位置减去旋转中心点，相当于把旋转中心移到原点

  nozzle.position.sub(nozzlePivotPoint);

  // 创建一个四元数，设置为绕X轴旋转90度
  const angleRad = THREE.MathUtils.degToRad(90);
  const quaternion = new THREE.Quaternion();
  quaternion.setFromAxisAngle(new THREE.Vector3(1, 0, 0), angleRad);
  
  nozzle.position.applyQuaternion(quaternion);
  nozzle.quaternion.premultiply(quaternion);

  nozzle.position.add(nozzlePivotPoint);

  
  // 动画完成后设置状态
  isNozzleRaised.value = true;
  
  // 显示成功消息
  ElMessage.success('喷管已就位，正在喷射灭火剂！');
};

// 监听 scale 变化
watch(() => scale.value, (newScale) => {
  if (modelRef.value) {
    modelRef.value.scale.set(
      newScale.x * 1.0, // 减小缩放系数，原本是 1.5
      newScale.y * 1.0, // 减小缩放系数，原本是 1.5
      newScale.z * 1.0  // 减小缩放系数，原本是 1.5
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

// 按压灭火器压把的动画
const pressLever = (leverObject) => {
  if (!leverTopObject.value || isLeverPressed.value) return;
  
  isLeverPressed.value = true;
  
  // 使用顶部压把对象进行动画
  const leverToAnimate = leverTopObject.value;
  
  // 保存初始状态
  const startPosition = leverToAnimate.position.clone();
  const startRotation = leverToAnimate.rotation.clone();
  
  // 设置目标旋转(向下旋转约30度)
  const endRotation = startRotation.clone();
  endRotation.x += Math.PI / 6; // 绕X轴旋转30度
  
  // 简单动画
  let progress = 0;
  const duration = 1000; // 按压动画时长
  const startTime = Date.now();
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数
    const easedProgress = easeOutQuad(progress);
    
    // 旋转压把
    leverToAnimate.rotation.x = startRotation.x - (endRotation.x - startRotation.x) * easedProgress;
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    } else {
      // 动画完成，显示成功消息
      ElMessage.success('灭火器已激活，开始喷射灭火剂！');
      
      // 开始喷射泡沫效果
      startSprayingFoam();
      
      // 开始灭火效果
      startExtinguishingFire();
    }
  }
  
  animate();
};

// 平滑缓动函数
const easeOutQuad = (t) => {
  return 1 - (1 - t) * (1 - t);
};

// 弹性缓动函数
const easeOutElastic = (t) => {
  const p = 0.5; // 增加p值，使弹性更加平缓（原值为0.3）
  return Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1;
};

// 添加重置喷嘴位置的函数
const resetNozzlePosition = () => {
  if (!nozzleObject.value || !originalNozzleRotation) return;
  
  const nozzle = nozzleObject.value;
  
  // 重置喷管位置动画
  let progress = 0;
  const duration = 800; // 毫秒
  const startTime = Date.now();
  const startPosition = nozzle.position.clone();
  const startQuaternion = nozzle.quaternion.clone();
  
  // 创建一个四元数，设置为原始旋转
  const targetQuaternion = new THREE.Quaternion();
  targetQuaternion.setFromEuler(originalNozzleRotation);
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数
    const easedProgress = easeInOutQuad(progress);
    
    // 恢复到原始位置和旋转
    nozzle.position.set(0, 0, 0); // 重置到原点
    nozzle.quaternion.slerp(targetQuaternion, easedProgress);
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    }
  }
  
  animate();
};

// 添加拿起灭火器的函数
const pickUpExtinguisher = () => {
  console.log("pickUpExtinguisher被调用");
  if (isExtinguisherPickedUp.value) {
    console.log("灭火器已经被拿起，函数退出");
    return;
  }
  
  isExtinguisherPickedUp.value = true;
  console.log("设置isExtinguisherPickedUp=true");
  
  // 获取当前模型位置
  const currentPosition = new THREE.Vector3();
  modelRef.value.getWorldPosition(currentPosition);
  
  // 计算新位置：向相机方向移动
  const cameraDirection = new THREE.Vector3();
  camera.getWorldDirection(cameraDirection);
  cameraDirection.negate(); // 相机的反方向就是"朝向观察者"的方向
  
  // 新位置：相机位置 + 朝向观察者的方向 * 距离
  const newPosition = new THREE.Vector3();
  camera.getWorldPosition(newPosition);
  
  // 向相机方向移动，但不要太近，保持一定距离
  newPosition.sub(cameraDirection.multiplyScalar(3));
  
  // 设置模型的y坐标为视野中心稍微偏下的位置
  newPosition.y -= 0.5;
  
  // 动画移动模型到新位置
  const startPosition = currentPosition.clone();
  const endPosition = newPosition.clone();
  let progress = 0;
  const duration = 1000; // 移动动画时长1秒
  const startTime = Date.now();
  
  // 保存初始旋转
  const startRotation = modelRef.value.quaternion.clone();
  
  // 创建新的四元数，将模型正面朝向相机
  const targetRotation = new THREE.Quaternion();
  const upVector = new THREE.Vector3(0, 1, 0); // 保持模型的上方向
  
  // 创建一个朝向相机的方向向量
  const lookAtCamera = new THREE.Vector3().subVectors(camera.position, newPosition).normalize();
  
  // 创建一个旋转矩阵，正确朝向相机
  const rotationMatrix = new THREE.Matrix4();
  // 修改此处，确保模型正面朝向相机（将lookAtCamera取反，使模型正面而非背面朝向相机）
  rotationMatrix.lookAt(new THREE.Vector3(0, 0, 0), lookAtCamera.negate(), upVector);
  targetRotation.setFromRotationMatrix(rotationMatrix);
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数
    const easedProgress = easeOutBack(progress);
    
    // 移动模型
    modelRef.value.position.lerpVectors(startPosition, endPosition, easedProgress);
    
    // 旋转模型
    modelRef.value.quaternion.slerp(targetRotation, easedProgress);
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    } else {
      // 显示提示
      console.log("灭火器拿起动画完成");
      ElMessage.success('已拿起灭火器！现在可以拔出安全销。');
    }
  }
  
  animate();
};

// 检查一个对象是否是模型的一部分
const isPartOfModel = (object, model) => {
  let current = object;
  while (current) {
    if (current === model) return true;
    current = current.parent;
  }
  return false;
};

// 添加重置灭火器位置的函数
const resetExtinguisherPosition = () => {
  if (!modelRef.value) return;
  
  // 重置到原始位置
  const startPosition = modelRef.value.position.clone();
  const endPosition = new THREE.Vector3(0, 0, 0);
  
  // 重置到原始旋转
  const startRotation = modelRef.value.quaternion.clone();
  const endRotation = new THREE.Quaternion();
  
  // 动画回到原位
  let progress = 0;
  const duration = 1000;
  const startTime = Date.now();
  
  function animate() {
    const elapsed = Date.now() - startTime;
    progress = Math.min(elapsed / duration, 1);
    
    // 使用缓动函数
    const easedProgress = easeInOutQuad(progress);
    
    // 移动和旋转模型
    modelRef.value.position.lerpVectors(startPosition, endPosition, easedProgress);
    modelRef.value.quaternion.slerp(endRotation, easedProgress);
    
    if (progress < 1) {
      requestAnimationFrame(animate);
    }
  }
  
  animate();
};

// 添加创建粒子系统的函数
const createParticleSystem = () => {
  // 如果已经存在粒子系统，先移除它
  if (particleSystem) {
    scene.remove(particleSystem);
    if (particleGeometry) particleGeometry.dispose();
    if (particleMaterial) particleMaterial.dispose();
  }
  
  // 创建粒子几何体
  particleGeometry = new THREE.BufferGeometry();
  const positions = new Float32Array(particleCount * 3);
  const colors = new Float32Array(particleCount * 3);
  const sizes = new Float32Array(particleCount);
  const velocities = new Float32Array(particleCount * 3);
  
  // 初始化粒子位置和速度
  const nozzleWorldPos = new THREE.Vector3();
  if (nozzleObject.value) {
    nozzleObject.value.getWorldPosition(nozzleWorldPos);
  } else {
    // 如果找不到喷嘴，使用默认位置
    nozzleWorldPos.set(0, 1, 0);
  }
  
  // 计算喷射方向
  if (nozzleObject.value && isNozzleRaised.value) {
    // 如果喷嘴已抬起，更新喷射方向
    const nozzleDirection = new THREE.Vector3(0, 0, 1); // 修改为正向Z轴
    nozzleObject.value.getWorldDirection(nozzleDirection);
    // 反转方向使泡沫向前喷
    sprayDirection.copy(nozzleDirection.negate());
  }
  
  // 粒子颜色
  const whiteColor = new THREE.Color(0xffffff);
  const foamColor = new THREE.Color(0xf0f0f0);
  
  // 初始化粒子
  for (let i = 0; i < particleCount; i++) {
    // 随机值，用于散射效果
    const randomX = (Math.random() - 0.5) * 0.5;
    const randomY = (Math.random() - 0.5) * 0.5;
    const randomZ = (Math.random() - 0.5) * 0.5;
    
    // 设置粒子初始位置（喷嘴位置）
    positions[i * 3] = nozzleWorldPos.x;
    positions[i * 3 + 1] = nozzleWorldPos.y + 1.3; // 向上调整初始位置
    positions[i * 3 + 2] = nozzleWorldPos.z;
    
    // 设置粒子速度（方向 + 随机扩散）
    velocities[i * 3] = sprayDirection.x * 2 + randomX;
    velocities[i * 3 + 1] = sprayDirection.y * 2 + randomY;
    velocities[i * 3 + 2] = sprayDirection.z * 2 + randomZ;
    
    // 设置粒子颜色（白色到浅灰色的随机值，模拟泡沫）
    const mixedColor = whiteColor.clone().lerp(foamColor, Math.random() * 0.3);
    colors[i * 3] = mixedColor.r;
    colors[i * 3 + 1] = mixedColor.g;
    colors[i * 3 + 2] = mixedColor.b;
    
    // 设置粒子大小（随机大小，模拟不同大小的泡沫）
    sizes[i] = Math.random() * 0.2 + 0.05;
  }
  
  particleGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  particleGeometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
  particleGeometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
  
  // 改用更简单的材质，避免着色器编译错误
  // 使用内置的PointsMaterial而不是ShaderMaterial
  particleMaterial = new THREE.PointsMaterial({
    size: 0.1,
    vertexColors: true,
    transparent: true,
    opacity: 0.8,
    blending: THREE.AdditiveBlending,
    sizeAttenuation: true, // 粒子大小会随距离衰减
  });
  
  // 创建粒子系统
  particleSystem = new THREE.Points(particleGeometry, particleMaterial);
  particleSystem.userData = { velocities: velocities };
  
  // 添加到场景
  scene.add(particleSystem);
  
  // 初始化时钟，用于动画
  if (!clock) clock = new THREE.Clock();
};

// 添加开始喷射泡沫的函数
const startSprayingFoam = () => {
  if (isSprayingFoam) return;
  
  isSprayingFoam = true;
  createParticleSystem();
  
  // 开始自动更新粒子系统
  clock.start();
};

// 添加停止喷射泡沫的函数
const stopSprayingFoam = () => {
  isSprayingFoam = false;
  
  if (particleSystem) {
    scene.remove(particleSystem);
    particleSystem = null;
  }
  
  if (particleGeometry) {
    particleGeometry.dispose();
    particleGeometry = null;
  }
  
  if (particleMaterial) {
    particleMaterial.dispose();
    particleMaterial = null;
  }
};

// 添加更新粒子系统的函数
const updateParticles = (deltaTime) => {
  if (!particleSystem || !isSprayingFoam) return;
  
  const positions = particleSystem.geometry.attributes.position.array;
  const velocities = particleSystem.userData.velocities;
  const sizes = particleSystem.geometry.attributes.size.array;
  
  // 重置进入半球的粒子计数
  particlesInDomeCount = 0;
  
  // 获取喷嘴的世界坐标
  const nozzleWorldPos = new THREE.Vector3();
  if (nozzleObject.value) {
    nozzleObject.value.getWorldPosition(nozzleWorldPos);
  }
  
  for (let i = 0; i < particleCount; i++) {
    // 更新粒子位置
    positions[i * 3] += velocities[i * 3] * deltaTime;
    positions[i * 3 + 1] += velocities[i * 3 + 1] * deltaTime;
    positions[i * 3 + 2] += velocities[i * 3 + 2] * deltaTime;
    
    // 加入重力影响
    velocities[i * 3 + 1] -= 1.0 * deltaTime; // 模拟重力
    
    // 创建当前粒子位置的向量
    const particlePosition = new THREE.Vector3(
      positions[i * 3],
      positions[i * 3 + 1],
      positions[i * 3 + 2]
    );
    
    // 检查粒子是否进入火源半球
    if (isFireActive.value && isParticleInFireDome(particlePosition)) {
      particlesInDomeCount++;
    }
    
    // 粒子寿命和重置逻辑
    // 如果粒子离开视野太远，重新放回喷嘴位置
    const distanceFromNozzle = Math.sqrt(
      Math.pow(positions[i * 3] - nozzleWorldPos.x, 2) + 
      Math.pow(positions[i * 3 + 1] - nozzleWorldPos.y, 2) + 
      Math.pow(positions[i * 3 + 2] - nozzleWorldPos.z, 2)
    );
    
    if (distanceFromNozzle > 10 || positions[i * 3 + 1] < -2) {
      // 重置粒子到喷嘴位置
      positions[i * 3] = nozzleWorldPos.x;
      positions[i * 3 + 1] = nozzleWorldPos.y + 1.3; // 应用与初始化相同的Y轴偏移
      positions[i * 3 + 2] = nozzleWorldPos.z;
      
      // 重新计算粒子速度
      const randomX = (Math.random() - 0.5) * 0.5;
      const randomY = (Math.random() - 0.5) * 0.5;
      const randomZ = (Math.random() - 0.5) * 0.5;
      
      velocities[i * 3] = sprayDirection.x * 2 + randomX;
      velocities[i * 3 + 1] = sprayDirection.y * 2 + randomY;
      velocities[i * 3 + 2] = sprayDirection.z * 2 + randomZ;
      
      // 随机调整粒子大小
      sizes[i] = Math.random() * 0.2 + 0.05;
    }
  }
  
  // 更新位置属性
  particleSystem.geometry.attributes.position.needsUpdate = true;
  particleSystem.geometry.attributes.size.needsUpdate = true;
  
  // 输出进入半球的粒子数量（每秒更新一次，避免过多日志）
  if (Math.random() < deltaTime) {
    console.log(`当前有 ${particlesInDomeCount} 个泡沫粒子在半球内，累计 ${totalParticlesHitCount} 个粒子`);
  }
  
  // 检查是否累计了足够多粒子击中火源，每达到阈值进行一次火焰规模缩减
  if (isFireActive.value && particlesInDomeCount > 0) {
    totalParticlesHitCount += particlesInDomeCount;
    
    // 计算累计了多少批100个粒子
    const batchesCount = Math.floor(totalParticlesHitCount / requiredParticlesInDome);
    
    // 如果有新的完整批次，更新火焰规模
    if (batchesCount > Math.floor((totalParticlesHitCount - particlesInDomeCount) / requiredParticlesInDome)) {
      reduceFireScale();
    }
  }
};

// 创建火焰粒子系统
const createFireParticles = () => {
  // 如果已存在火焰系统，先移除
  if (fireParticleSystem) {
    scene.remove(fireParticleSystem);
    if (fireGeometry) fireGeometry.dispose();
    if (fireMaterial) fireMaterial.dispose();
  }
  
  // 创建火焰几何体
  fireGeometry = new THREE.BufferGeometry();
  const positions = new Float32Array(fireParticleCount * 3);
  const colors = new Float32Array(fireParticleCount * 3);
  const sizes = new Float32Array(fireParticleCount);
  const velocities = new Float32Array(fireParticleCount * 3);
  
  // 火焰颜色
  const redColor = new THREE.Color(0xff4500); // 红色
  const orangeColor = new THREE.Color(0xff8c00); // 橙色
  const yellowColor = new THREE.Color(0xffff00); // 黄色
  
  // 初始化火焰粒子
  for (let i = 0; i < fireParticleCount; i++) {
    // 在火焰位置周围随机分布粒子
    const radius = Math.random() * 1.2; // 增大基底半径，从0.8改为1.2
    const angle = Math.random() * Math.PI * 2;
    const height = Math.random() * 2.5; // 增加高度，从1.8改为2.5
    
    positions[i * 3] = firePosition.x + radius * Math.cos(angle);
    positions[i * 3 + 1] = firePosition.y + height;
    positions[i * 3 + 2] = firePosition.z + radius * Math.sin(angle);
    
    // 设置粒子向上的速度（火焰上升效果）
    velocities[i * 3] = (Math.random() - 0.5) * 0.5; // 增加横向扩散，从0.3改为0.5
    velocities[i * 3 + 1] = Math.random() * 1.5 + 0.8; // 增加上升速度，从1.2+0.5改为1.5+0.8
    velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.5; // 增加横向扩散，从0.3改为0.5
    
    // 根据粒子高度设置颜色（底部红色，中间橙色，顶部黄色）
    let mixColor;
    if (height < 0.8) { // 增加红色区域，从0.5改为0.8
      mixColor = redColor.clone();
    } else if (height < 1.6) { // 增加橙色区域，从1.0改为1.6
      const t = (height - 0.8) * 1.25;
      mixColor = redColor.clone().lerp(orangeColor, t);
    } else {
      const t = (height - 1.6) * 1.1;
      mixColor = orangeColor.clone().lerp(yellowColor, t);
    }
    
    colors[i * 3] = mixColor.r;
    colors[i * 3 + 1] = mixColor.g;
    colors[i * 3 + 2] = mixColor.b;
    
    // 设置粒子大小
    sizes[i] = Math.random() * 0.15 + 0.08; // 增加粒子大小，从0.1+0.05改为0.15+0.08
  }
  
  fireGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
  fireGeometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
  fireGeometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
  
  // 创建火焰材质
  fireMaterial = new THREE.PointsMaterial({
    size: 0.1,
    vertexColors: true,
    transparent: true,
    opacity: 0.8,
    blending: THREE.AdditiveBlending,
    sizeAttenuation: true,
  });
  
  // 创建火焰粒子系统
  fireParticleSystem = new THREE.Points(fireGeometry, fireMaterial);
  fireParticleSystem.userData = { velocities: velocities };
  
  // 添加到场景
  scene.add(fireParticleSystem);
  
  // 创建或更新火源半球
  createFireDome();
};

// 创建透明绿色半球包围火源
const createFireDome = () => {
  // 如果已存在半球，先移除
  if (fireDomeMesh) {
    scene.remove(fireDomeMesh);
    fireDomeMesh.geometry.dispose();
    fireDomeMesh.material.dispose();
  }
  
  // 创建半球几何体
  const geometry = new THREE.SphereGeometry(fireDomeRadius, 32, 32, 0, Math.PI * 2, 0, Math.PI / 2);
  
  // 创建半透明材质
  const material = new THREE.MeshBasicMaterial({
    color: fireDomeColor,
    transparent: true,
    opacity: fireDomeOpacity,
    side: THREE.DoubleSide,
    depthWrite: false // 确保半透明物体正确渲染
  });
  
  // 创建半球网格
  fireDomeMesh = new THREE.Mesh(geometry, material);
  
  // 设置半球位置在火焰位置
  fireDomeMesh.position.copy(firePosition);
  
  // 添加到场景
  scene.add(fireDomeMesh);
  
  // 确保半球可见，辅助开发
  console.log("半球已创建，作为开发辅助工具始终可见");
};

// 判断粒子是否在火源半球内部
const isParticleInFireDome = (particlePosition) => {
  if (!fireDomeMesh) return false;
  
  // 计算粒子到半球中心的距离
  const dx = particlePosition.x - firePosition.x;
  const dy = particlePosition.y - firePosition.y;
  const dz = particlePosition.z - firePosition.z;
  
  // 计算平方距离
  const distanceSquared = dx * dx + dy * dy + dz * dz;
  
  // 如果距离小于半球半径且y坐标大于火源的y坐标(半球只有上半部分)
  return distanceSquared < fireDomeRadius * fireDomeRadius && particlePosition.y >= firePosition.y;
};

// 更新火焰粒子
const updateFireParticles = (deltaTime) => {
  if (!fireParticleSystem || !isFireActive.value) return;

  const positions = fireParticleSystem.geometry.attributes.position.array;
  const velocities = fireParticleSystem.userData.velocities;
  const colors = fireParticleSystem.geometry.attributes.color.array;
  const sizes = fireParticleSystem.geometry.attributes.size.array;
  
  // 随机高度和半径基准值
  const baseHeight = 2.0 - fireExtinguishProgress * 1.8; // 随着灭火进度降低高度，从1.0改为2.0
  const baseRadius = 1.0 - fireExtinguishProgress * 0.8; // 随着灭火进度降低半径，从0.5改为1.0
  
  for (let i = 0; i < fireParticleCount; i++) {
    // 根据灭火进度随机熄灭一些粒子(尤其是高处的粒子)
    if (isExtinguishing && Math.random() < fireExtinguishProgress * 0.1) {
      // 模拟粒子熄灭
      positions[i * 3] = firePosition.x + (Math.random() - 0.5) * 0.1;
      positions[i * 3 + 1] = firePosition.y;
      positions[i * 3 + 2] = firePosition.z + (Math.random() - 0.5) * 0.1;
      velocities[i * 3] = 0;
      velocities[i * 3 + 1] = 0;
      velocities[i * 3 + 2] = 0;
      sizes[i] = 0.05; // 设置为很小的尺寸
      continue;
    }
    
    // 更新粒子位置
    positions[i * 3] += velocities[i * 3] * deltaTime;
    positions[i * 3 + 1] += velocities[i * 3 + 1] * deltaTime * (1 - fireExtinguishProgress * 0.8); // 随灭火进度减慢上升速度
    positions[i * 3 + 2] += velocities[i * 3 + 2] * deltaTime;
    
    // 高度限制和距离检查
    const heightFromBase = positions[i * 3 + 1] - firePosition.y;
    const distanceFromCenter = Math.sqrt(
      Math.pow(positions[i * 3] - firePosition.x, 2) +
      Math.pow(positions[i * 3 + 2] - firePosition.z, 2)
    );
    
    // 如果粒子超出最大高度或者距离太远，则重置粒子
    if (heightFromBase > baseHeight || distanceFromCenter > baseRadius) {
      // 重置粒子位置到火焰底部随机位置
      const theta = Math.random() * Math.PI * 2;
      const r = Math.random() * baseRadius;
      
      positions[i * 3] = firePosition.x + r * Math.cos(theta);
      positions[i * 3 + 1] = firePosition.y;
      positions[i * 3 + 2] = firePosition.z + r * Math.sin(theta);
      
      // 重置粒子速度
      velocities[i * 3] = (Math.random() - 0.5) * 0.3 * (1 - fireExtinguishProgress * 0.6); // 随灭火进度减小水平速度
      velocities[i * 3 + 1] = 0.5 + Math.random() * 0.7 * (1 - fireExtinguishProgress * 0.8); // 随灭火进度减小垂直速度
      velocities[i * 3 + 2] = (Math.random() - 0.5) * 0.3 * (1 - fireExtinguishProgress * 0.6);
      
      // 重置粒子大小
      sizes[i] = 0.1 + Math.random() * 0.05 * (1 - fireExtinguishProgress * 0.7); // 随灭火进度变小
    }
  }
  
  // 更新粒子属性
  fireParticleSystem.geometry.attributes.position.needsUpdate = true;
  fireParticleSystem.geometry.attributes.size.needsUpdate = true;
  fireParticleSystem.geometry.attributes.color.needsUpdate = true;
};

// 降低火焰规模的函数
const reduceFireScale = () => {
  // 计算当前应减少的比例 (使用线性计算方式)
  const batchCount = Math.floor(totalParticlesHitCount / requiredParticlesInDome);
  const currentReduction = 1 - (batchCount * fireScaleReductionRate);
  
  // 修改终止阈值从0.1改为0.3
  const minScale = 0.3;
  
  // 如果火焰几乎被熄灭，则完全熄灭
  if (currentReduction <= minScale) {
    isFireActive.value = false;
    isExtinguishing = false;
    ElMessage.success('成功灭火！');
    
    // 弹出游戏完成对话框
    gameCompletedDialogVisible.value = true;
    
    // 停止喷射泡沫
    stopSprayingFoam();
    
    return;
  }
  
  // 输出调试信息
  console.log(`降低火焰规模: 总计进入 ${totalParticlesHitCount} 个粒子，批次 ${batchCount}，当前规模: ${currentReduction.toFixed(2)}`);
  
  // 直接设置灭火进度为线性值，确保与规模减小同步
  fireExtinguishProgress = 1 - currentReduction;
};

// 开始灭火过程
const startExtinguishingFire = () => {
  if (isExtinguishing) return;
  
  isExtinguishing = true;
  console.log(`开始灭火过程! 有${particlesInDomeCount}个粒子在火源区域`);
  
  // 清除可能存在的之前的定时器
  if (fireExtinguishTimeout) {
    clearTimeout(fireExtinguishTimeout);
  }
};

// 重置火焰
const resetFire = () => {
  isFireActive.value = true;
  isExtinguishing = false;
  fireExtinguishProgress = 0;
  
  // 清除定时器
  if (fireExtinguishTimeout) {
    clearTimeout(fireExtinguishTimeout);
    fireExtinguishTimeout = null;
  }
  
  // 重新创建火焰粒子系统
  createFireParticles();
};

// 添加键盘按下和释放的事件监听器
const handleKeyDown = (event) => {
  if (!isExtinguisherPickedUp.value) return; // 只有拿起灭火器后才能控制

  const key = event.key.toLowerCase();
  if (['w', 'a', 's', 'd'].includes(key)) {
    keysPressed.value[key] = true;
    event.preventDefault(); // 防止按键引起页面滚动
  }
};

const handleKeyUp = (event) => {
  const key = event.key.toLowerCase();
  if (['w', 'a', 's', 'd'].includes(key)) {
    keysPressed.value[key] = false;
  }
};

// 更新灭火器朝向的函数
const updateExtinguisherRotation = () => {
  if (!isExtinguisherPickedUp.value || !modelRef.value) return;

  // 创建一个Euler对象来表示旋转
  const rotation = new THREE.Euler().copy(modelRef.value.rotation);

  // 基于按键状态调整旋转
  if (keysPressed.value.w) {
    rotation.x -= rotationSpeed; // 向上旋转
  }
  if (keysPressed.value.s) {
    rotation.x += rotationSpeed; // 向下旋转
  }
  if (keysPressed.value.a) {
    rotation.y += rotationSpeed; // 向左旋转
  }
  if (keysPressed.value.d) {
    rotation.y -= rotationSpeed; // 向右旋转
  }

  // 移除旋转限制，允许360°旋转
  // 保持角度在[-π, π]范围内以避免值过大
  rotation.x = (rotation.x + Math.PI * 2) % (Math.PI * 2);
  if (rotation.x > Math.PI) rotation.x -= Math.PI * 2;

  // 应用旋转
  modelRef.value.rotation.copy(rotation);

  // 更新喷射方向
  if (isSprayingFoam && nozzleObject.value) {
    const nozzleDirection = new THREE.Vector3(0, 0, 1);
    nozzleObject.value.getWorldDirection(nozzleDirection);
    sprayDirection.copy(nozzleDirection.negate());
  }
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
  camera.position.set(10, 10, 10); // 将相机初始位置设置得更远

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

  // 添加地面
  const groundGeometry = new THREE.PlaneGeometry(50, 50);
  const groundMaterial = new THREE.MeshStandardMaterial({ 
    color: 0x999999,
    roughness: 0.8,
    metalness: 0.2,
    side: THREE.DoubleSide
  });
  const ground = new THREE.Mesh(groundGeometry, groundMaterial);
  ground.rotation.x = Math.PI / 2;
  ground.position.y = 0; // 位置下移，让灭火器位于地面上方
  ground.receiveShadow = true;
  scene.add(ground);

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
  
  // 初始化时钟
  clock = new THREE.Clock();
  
  // 创建渲染循环函数
  function animate() {
    // 更新灭火器朝向
    updateExtinguisherRotation();
    
    requestAnimationFrame(animate);
    if (controls) {
      controls.update();
    }
    
    // 更新粒子系统
    if (isSprayingFoam && particleSystem) {
      const deltaTime = clock.getDelta();
      updateParticles(deltaTime);
    }
    
    // 更新火焰粒子
    if (fireParticleSystem) {
      const fireDeltaTime = clock ? clock.getDelta() : 0.016;
      updateFireParticles(fireDeltaTime);
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
  
  // 添加鼠标移动事件监听器
  renderer.domElement.addEventListener('mousemove', handleMouseMove);

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

  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyDown);
  window.addEventListener('keyup', handleKeyUp);

  // 在组件卸载时清理资源
  onUnmounted(() => {
    resizeObserver.disconnect();
    if (renderer && renderer.domElement) {
      renderer.domElement.removeEventListener('click', handleClick);
      renderer.domElement.removeEventListener('mousemove', handleMouseMove);
    }
    if (controls) controls.dispose();
    
    // 清理粒子系统资源
    if (particleSystem) {
      scene.remove(particleSystem);
    }
    if (particleGeometry) {
      particleGeometry.dispose();
    }
    if (particleMaterial) {
      particleMaterial.dispose();
    }
    
    if (renderer) {
      renderer.dispose();
      if (container.contains(renderer.domElement)) {
        container.removeChild(renderer.domElement);
      }
    }

    // 移除键盘事件监听
    window.removeEventListener('keydown', handleKeyDown);
    window.removeEventListener('keyup', handleKeyUp);
  });

  // 创建火焰效果
  createFireParticles();
});

// 重置整个场景
const resetAll = () => {
  // 重置火焰状态
  isFireActive.value = true;
  isExtinguishing = false;
  fireExtinguishProgress = 0;
  particlesInDomeCount = 0;
  totalParticlesHitCount = 0;
  
  // 重置灭火器状态
  isExtinguisherPickedUp.value = false;
  isPinPulled.value = false;
  isLeverPressed.value = false;
  isNozzleRaised.value = false;
  
  // 重置游戏完成对话框
  gameCompletedDialogVisible.value = false;
  
  // 调用已有的重置函数
  reset();
  
  ElMessage.success('场景已重置，可以重新开始！');
};
</script>

<style scoped lang="scss">
.model-display-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
  position: relative;
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

.reset-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 100;
}

.guide-content {
  padding: 10px;
  
  .guide-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    color: var(--text-color);
  }
  
  .guide-steps {
    margin-bottom: 20px;
    padding-left: 20px;
    
    li {
      margin-bottom: 12px;
      color: var(--text-color);
      line-height: 1.5;
      
      strong {
        color: var(--primary-color);
      }
    }
  }
  
  .guide-note {
    display: flex;
    align-items: center;
    background-color: rgba(var(--warning-color-rgb), 0.1);
    padding: 10px 15px;
    border-radius: 4px;
    font-size: 14px;
    color: var(--warning-color);
    
    .el-icon {
      margin-right: 8px;
      font-size: 18px;
    }
  }
}

:deep(.el-progress__text) {
  color: var(--text-color) !important;
}

.pick-up-hint {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.7);
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  z-index: 10;
  transition: opacity 0.3s ease;
  
  .hint-content {
    display: flex;
    align-items: center;
    gap: 10px;
    color: white;
    
    .el-icon {
      font-size: 20px;
      color: #409EFF;
      animation: pulse 1.5s infinite;
    }
    
    span {
      font-size: 16px;
      white-space: nowrap;
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.floating-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 100;
}
</style> 