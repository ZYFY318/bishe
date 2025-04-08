<template>
  <div class="profile-container">
    <!-- 顶部导航栏 -->
    <div class="profile-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="profile-title">个人中心</h1>
      </div>
    </div>
    
    <!-- 个人信息板块 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" plain size="small">编辑</el-button>
        </div>
      </template>
      <div class="profile-info">
        <!-- 个人信息内容将在此处添加 -->
        <div class="profile-placeholder">个人信息内容将在此处显示</div>
      </div>
    </el-card>
    
    <!-- 我的模型板块 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>我的模型</span>
        </div>
      </template>
      <el-scrollbar class="horizontal-scrollbar">
        <div class="models-container">
          <!-- 模拟的模型项目 -->
          <div class="model-item">
            <div class="model-placeholder">模型1</div>
          </div>
          <div class="model-item">
            <div class="model-placeholder">模型2</div>
          </div>
          <div class="model-item">
            <div class="model-placeholder">模型3</div>
          </div>
          <div class="model-item">
            <div class="model-placeholder">模型4</div>
          </div>
          <div class="model-item">
            <div class="model-placeholder">模型5</div>
          </div>
          <div class="model-item">
            <div class="model-placeholder">模型5</div>
          </div>
        </div>
      </el-scrollbar>
    </el-card>
    
    <!-- 我的作业板块 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>我的作业</span>
        </div>
      </template>
      <el-scrollbar class="horizontal-scrollbar">
        <div class="assignments-container">
          <!-- 模拟的作业项目 -->
          <div class="assignment-item">
            <div class="assignment-placeholder">作业1</div>
          </div>
          <div class="assignment-item">
            <div class="assignment-placeholder">作业2</div>
          </div>
          <div class="assignment-item">
            <div class="assignment-placeholder">作业3</div>
          </div>
          <div class="assignment-item">
            <div class="assignment-placeholder">作业4</div>
          </div>
          <div class="assignment-item">
            <div class="assignment-placeholder">作业5</div>
          </div>
        </div>
      </el-scrollbar>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import useUserStore from '@/stores/modules/user';

const router = useRouter();
const userStore = useUserStore();

// 返回上一页
const goBack = () => {
  router.back();
};

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 清除用户信息
    await userStore.userLogout();
    // 跳转到登录页，不带任何参数
    router.push('/login');
  }).catch(() => {
    // 用户取消操作
  });
};
</script>

<style scoped lang="scss">
.profile-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
//   background-color: transparent;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
  
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
  
  .profile-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color);
  }
}

.profile-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: var(--shadow);
  background-color: transparent;
  border: 1px solid var(--border-color);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
    color: var(--text-color);
  }
}

.profile-info,
.models-container,
.assignments-container {
  padding: 15px 0;
}

.profile-placeholder {
  padding: 30px;
  text-align: center;
  background-color: var(--bg-color);
  border-radius: 6px;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.models-container,
.assignments-container {
  display: flex;
  flex-direction: row;
  gap: 20px;
  padding: 15px 5px;
  min-width: max-content;
}

.horizontal-scrollbar {
  max-width: 100%;
  height: auto;
}

.model-item,
.assignment-item {
  flex: 0 0 auto;
  width: 200px;
  height: 180px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.model-placeholder,
.assignment-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: transparent;
  color: var(--text-color-secondary);
  font-size: 16px;
  border: 1px dashed var(--border-color);
}
</style>