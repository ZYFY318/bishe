<template>
  <div class="a">
    <div ref="sceneContainer" class="scene-container"></div>
    <a-button>primary</a-button>
  </div>

</template>

<script>
import { ref, onMounted } from 'vue';
import * as THREE from 'three';

export default {
  name: 'ThreeDScene',
  setup() {
    const sceneContainer = ref(null);
    let scene, camera, renderer, cube;

    const initScene = () => {
      // 创建场景
      scene = new THREE.Scene();

      // 创建相机
      camera = new THREE.PerspectiveCamera(
        75,
        sceneContainer.value.clientWidth / sceneContainer.value.clientHeight,
        0.1,
        1000
      );
      camera.position.z = 5;

      // 创建渲染器
      renderer = new THREE.WebGLRenderer();
      renderer.setSize(sceneContainer.value.clientWidth, sceneContainer.value.clientHeight);
      sceneContainer.value.appendChild(renderer.domElement);

      // 创建立方体
      const geometry = new THREE.BoxGeometry();
      const material = new THREE.MeshBasicMaterial({ color: 0x00ff00 });
      cube = new THREE.Mesh(geometry, material);
      scene.add(cube);
    };

    const animate = () => {
      renderer.render(scene, camera);
      requestAnimationFrame(animate);
      cube.rotation.x += 0.01;
      cube.rotation.y += 0.01;
    };

    onMounted(() => {
      initScene();
      animate();
    });

    return {
      sceneContainer,
    };
  },
};
</script>

<style scoped>
.scene-container {
  width: 100%;
  height: 100vh;
}
.a {
  
}
</style>