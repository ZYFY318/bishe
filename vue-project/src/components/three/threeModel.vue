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
      modelRef.value.scale.set(
        newScale.x * 2, // 乘以2是因为我们之前基础缩放是2
        newScale.y * 2,
        newScale.z * 2
      )
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
            console.log(size)
            // 设置模型位置，z轴位置为模型高度的一半
            model.position.set(0, -size.y / 2, 0);
            
            // 设置初始缩放
            model.scale.set(
                props.scale.x * 2,
                props.scale.y * 2,
                props.scale.z * 2
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
    scene.add(axesHelper);
  })
  </script>
  
  <style scoped>
  .canvas-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
  }
  </style>