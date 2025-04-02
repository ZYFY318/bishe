<template>
    <div ref="canvasContainer" class="canvas-container"></div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted, watch } from 'vue'
  import * as THREE from 'three'
  import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
  import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
  
  const props = defineProps({
    modelPath: {
      type: String,
      required: true,
    },
    modelName: {
      type: String,
      default: '未命名模型'
    },
    scale: {
      type: Object,
      default: () => ({ x: 1, y: 1, z: 1 })
    },
    autoRotate: {
      type: Boolean,
      default: false
    }
  })
  
  const canvasContainer = ref(null)
  let scene, camera, renderer, controls
  
  // 添加一个 ref 来存储模型引用
  const modelRef = ref(null)
  
  // 监听 scale 变化
  watch(() => props.scale, (newScale) => {
    if (modelRef.value) {
      // 移除这段代码，因为我们已经在模型加载时设置了缩放因子
      // modelRef.value.scale.set(
      //   newScale.x * 3, 
      //   newScale.y * 3,
      //   newScale.z * 3
      // )
    }
  }, { deep: true })
  
  // 监听 autoRotate 变化
  watch(() => props.autoRotate, (newValue) => {
    if (controls) {
      controls.autoRotate = newValue;
      controls.autoRotateSpeed = 8.0; // 可以调整旋转速度
    }
  }, { immediate: true })
  
  onMounted(() => {
    // 创建场景
    scene = new THREE.Scene()
    scene.background = new THREE.Color(0xffffff) // 将背景色改为白色
  
    // 获取父容器的尺寸
    const container = canvasContainer.value
    const containerWidth = container.clientWidth
    const containerHeight = container.clientHeight
    // 使用父容器的尺寸创建相机
    camera = new THREE.PerspectiveCamera(75, containerWidth / containerHeight, 0.1, 1000)
    camera.position.set(0, 0.6, 0.5)
  
    // 使用父容器的尺寸创建渲染器
    renderer = new THREE.WebGLRenderer({ antialias: true })
    renderer.setSize(containerWidth, containerHeight)
    renderer.shadowMap.enabled = true // 启用阴影映射
    container.appendChild(renderer.domElement)
  
    // 增强环境光
    const ambientLight = new THREE.AmbientLight(0xffffff, 3.5) // 增强环境光强度
    scene.add(ambientLight)
  
    // 增强点光源并启用阴影
    const pointLight = new THREE.PointLight(0xffffff, 2, 100) // 增强点光源强度
    pointLight.position.set(5, 5, 5) // 设置点光源的位置
    pointLight.castShadow = true // 启用点光源的阴影
    scene.add(pointLight)
  
    // 初始化 OrbitControls
    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true
    controls.dampingFactor = 0.05
    controls.rotateSpeed = 0.4
    controls.autoRotate = props.autoRotate
    controls.autoRotateSpeed = 8.0
    controls.enableZoom = true
    controls.enablePan = false
    controls.minDistance = 2
    controls.maxDistance = 10
    controls.minPolarAngle = Math.PI / 4
    controls.maxPolarAngle = Math.PI / 1.5
  
    // 创建渲染循环函数
    function animate() {
        requestAnimationFrame(animate);
        if (controls) {
            controls.update(); // 这会处理自动旋转
        }
        if (renderer && scene && camera) {
            renderer.render(scene, camera);
        }
    }
    
    // 立即开始渲染循环
    animate();
  console.log("heew",props.modelPath)
    // 加载 GLB 模型
    const gltfLoader = new GLTFLoader()
    gltfLoader.load(
        props.modelPath,
        (gltf) => {
            console.log(`模型 "${props.modelName}" 加载成功`);
            const model = gltf.scene;
            
            // 计算模型的包围盒
            const box = new THREE.Box3().setFromObject(model);
            const size = box.getSize(new THREE.Vector3());
            const center = box.getCenter(new THREE.Vector3());
            
            console.log('模型原始尺寸:', size);
            
            // 自动标准化模型大小
            // 计算当前模型的最大尺寸
            const maxDimension = Math.max(size.x, size.y, size.z);
            
            // 定义目标标准尺寸（将模型缩放到这个基准大小）
            const targetSize = 2; 
            
            // 计算缩放比例
            const scaleFactor = targetSize / maxDimension;
            
            // 创建一个函数来计算映射后的缩放值
            const mapScale = (value) => {
              // 将滑块值 (10-200) 映射到合理的缩放范围 (0.2-4.0)
              return 0.2 + (value - 10) * (3.8 / 190);
            };
            
            // 应用缩放
            model.scale.set(
              scaleFactor * mapScale(props.scale.x * 100), 
              scaleFactor * mapScale(props.scale.y * 100), 
              scaleFactor * mapScale(props.scale.z * 100)
            );
            
            // 重置模型位置到中心
            model.position.set(0, 0, 0);
            
            // 由于我们已经缩放了模型，需要重新计算包围盒
            box.setFromObject(model);
            const newSize = box.getSize(new THREE.Vector3());
            const newCenter = box.getCenter(new THREE.Vector3());
            
            console.log('标准化后的模型尺寸:', newSize);
            
            // 调整模型位置，使底部对齐
            model.position.y = -newSize.y / 2;
            
            model.castShadow = true;
            scene.add(model);
            
            // 添加边框辅助器
            const boxHelper = new THREE.BoxHelper(model, 0x0066ff); // 改为蓝色边框更醒目
            // scene.add(boxHelper);
            
            // 在 watch 函数中也使用相同的映射方法
            watch(() => props.scale, (newScale) => {
              if (modelRef.value) {
                modelRef.value.scale.set(
                  scaleFactor * mapScale(newScale.x * 100),
                  scaleFactor * mapScale(newScale.y * 100),
                  scaleFactor * mapScale(newScale.z * 100)
                );
                // 如果有边框辅助器，更新它
                if (boxHelper) {
                  boxHelper.update();
                }
              }
            }, { deep: true });
            
            // 保存模型引用
            modelRef.value = model;

            // 设置相机位置
            // 计算合适的相机距离
            const cameraDistance = Math.max(newSize.x, newSize.y, newSize.z) * 2.5;
            
            // 设置相机位置
            camera.position.set(
                cameraDistance, 
                cameraDistance * 0.8, 
                cameraDistance
            );
            
            // 让相机看向模型中心，但略微上移，以便更好地观察模型
            camera.lookAt(0, newSize.y * 0.25, 0);
            
            // 更新控制器目标点
            controls.target.set(0, newSize.y * 0.25, 0);
            controls.update();
        },
        (xhr) => {
            console.log(`${props.modelName} 加载进度: ${(xhr.loaded / xhr.total * 100)}%`);
        },
        (error) => {
            console.error(`模型 "${props.modelName}" 加载失败:`, error);
            console.error('尝试加载的模型路径:', props.modelPath);
        }
    );
  
    // 修改 resize 处理函数，使用 ResizeObserver 监听父容器大小变化
    const resizeObserver = new ResizeObserver(entries => {
      for (const entry of entries) {
        const { width, height } = entry.contentRect
        camera.aspect = width / height
        camera.updateProjectionMatrix()
        renderer.setSize(width, height)
      }
    })
  
    // 观察父容器的大小变化
    resizeObserver.observe(container)
  
    // 在组件卸载时停止观察
    onUnmounted(() => {
      resizeObserver.disconnect()
      if (controls) {
        controls.dispose();
      }
    })
  
    // 添加坐标轴辅助器
    const axesHelper = new THREE.AxesHelper(5);
    // scene.add(axesHelper);
  })
  </script>
  
  <style scoped>
  .canvas-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
  }
  </style>