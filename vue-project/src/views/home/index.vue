<template>
    <div class="card-container">
      <ItemCard
        imageUrl="hakasei.png"
        name="Item Name"
        description="This is a brief description of the item."
        glbPath="old_fire_axe.glb"
      />
      <ItemCard
        imageUrl="hakasei.png"
        name="Item Name 2"
        description="This is a brief description of the item."
        glbPath="kitty_character.glb"
      />
      <ItemCard
        imageUrl="hakasei.png"
        name="Item Name 3"
        description="This is a brief description of the item."
        glbPath="among_us_character.glb"
      />
      <ItemCard
        imageUrl="hakasei.png"
        name="Item Name 4"
        description="This is a brief description of the item."
        glbPath="path/to/model4.glb"
      />
    </div>
  </template>
  
  <script setup lang="ts">
 
  </script>
  
  <style scoped lang="scss">
  .card-container {
    display: flex;
    flex-wrap: wrap; // 允许卡片换行
    gap: 20px; // 卡片之间的间距
    padding: 10px; // 容器内边距
    justify-content: flex-start; // 从左向右排列
  }
  </style>