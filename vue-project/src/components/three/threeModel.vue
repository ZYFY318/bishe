<template>
    <div ref="canvasContainer" class="canvas-container"></div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  import * as THREE from 'three'
  import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
  import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
  
  const props = defineProps({
    modelPath: {
      type: String,
      required: true,
    },
  })
  
  const canvasContainer = ref(null)
  let scene, camera, renderer, controls
  
  onMounted(() => {
    // 创建场景
    scene = new THREE.Scene()
    scene.background = new THREE.Color(0x808080) // 设置背景色为灰色
  
    // 创建相机
    camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000)
    camera.position.set(0, 1.6, 5)
  
    // 创建渲染器并启用阴影
    renderer = new THREE.WebGLRenderer({ antialias: true })
    renderer.setSize(window.innerWidth, window.innerHeight)
    renderer.shadowMap.enabled = true // 启用阴影映射
    canvasContainer.value.appendChild(renderer.domElement)
  
    // 创建白色地面
    const groundGeometry = new THREE.PlaneGeometry(10, 10) // 创建一个 10x10 的平面
    const groundMaterial = new THREE.MeshStandardMaterial({ color: 0xffffff }) // 使用标准材质并设置为白色
    const ground = new THREE.Mesh(groundGeometry, groundMaterial)
    ground.rotation.x = -Math.PI / 2 // 使地面水平
    ground.receiveShadow = true // 启用地面接收阴影
    scene.add(ground) // 将地面添加到场景中
  
    // 增强环境光
    const ambientLight = new THREE.AmbientLight(0xffffff, 1.5) // 增强环境光强度
    scene.add(ambientLight)
  
    // 增强点光源并启用阴影
    const pointLight = new THREE.PointLight(0xffffff, 2, 100) // 增强点光源强度
    pointLight.position.set(5, 5, 5) // 设置点光源的位置
    pointLight.castShadow = true // 启用点光源的阴影
    scene.add(pointLight)
  
    // 初始化 OrbitControls
    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true // 启用阻尼
    controls.dampingFactor = 0.5 // 调整阻尼因子
    controls.rotateSpeed = 0.4; // 调整旋转速度
    controls.screenSpacePanning = false // 禁用平移
  
    // 加载 GLB 模型
    const gltfLoader = new GLTFLoader()
    gltfLoader.load(
      props.modelPath, // 使用传入的模型路径
      (gltf) => {
        console.log('模型加载成功:', gltf); // 打印加载的对象
        const model = gltf.scene; // 保存加载的模型
        scene.add(model); // 将加载的对象添加到场景中
        model.position.set(0, 0, -1); // 将模型向相机前方移动
        model.scale.set(1, 1, 1); // 尝试使用原始大小
        model.castShadow = true; // 启用模型的阴影
        console.log('模型已添加到场景中，位置:', model.position, '缩放:', model.scale); // 打印模型位置和缩放
  
        // 打印模型的边界框
        const box = new THREE.Box3().setFromObject(model);
        console.log('模型边界框:', box);
  
        // 创建边界框的可视化
        const boxHelper = new THREE.Box3Helper(box, new THREE.Color(1, 0, 0)); // 红色边界框
        scene.add(boxHelper); // 将边界框添加到场景中
  
        // 在渲染循环中更新模型的旋转
        function animate() {
          requestAnimationFrame(animate);
          model.rotation.y += 0.01; // 使模型绕 Y 轴旋转
          controls.update(); // 更新控制器
          renderer.render(scene, camera);
        }
        animate();
      },
      (xhr) => {
        console.log(`加载进度: ${(xhr.loaded / xhr.total) * 100}%`);
      },
      (error) => {
        console.error('模型加载失败:', error);
      }
    );
  
    // 处理窗口大小调整
    window.addEventListener('resize', onWindowResize, false)
    function onWindowResize() {
      camera.aspect = window.innerWidth / window.innerHeight
      camera.updateProjectionMatrix()
      renderer.setSize(window.innerWidth, window.innerHeight)
    }
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', onWindowResize)
  })
  </script>
  
  <style scoped>
  .canvas-container {
    width: 100vw;
    height: 100vh;
    overflow: hidden;
  }
  </style>