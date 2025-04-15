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
          <el-button type="primary" plain size="small" @click="handleEdit">编辑</el-button>
        </div>
      </template>
      <div class="profile-info">
        <div class="user-avatar">
          <el-avatar :size="100" :src="getImgUrl(userStore.avatar)" />
        </div>
        <div class="user-details">
          <div class="detail-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userStore.username }}</span>
          </div>
          <div class="detail-item">
            <span class="label">用户ID：</span>
            <span class="value">{{ userStore.userId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">用户类型：</span>
            <span class="value">{{ userTypeText }}</span>
          </div>
          <div class="detail-item">
            <span class="label">邮箱：</span>
            <span class="value">{{ userStore.email || '未设置' }}</span>
          </div>
        </div>
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
          <template v-if="isLoadingModels">
            <div v-for="i in 3" :key="i" class="model-item">
              <el-skeleton :rows="3" animated />
            </div>
          </template>
          <template v-else-if="userModels.length > 0">
            <div v-for="model in userModels" :key="model.id" class="model-item">
              <div class="model-content">
                <img 
                  :src="getImgUrl(model.coverUrl)" 
                  class="model-image" 
                  alt="模型图片"
                />
                <div class="model-info">
                  <div class="model-name">{{ model.name }}</div>
                  <div class="model-description">{{ model.description }}</div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="empty-message">暂无模型</div>
          </template>
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

    <!-- 编辑个人信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑个人信息"
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="avatarPreview || getImgUrl(userStore.avatar)" class="avatar" />
            <div class="avatar-upload-btn">
              <el-button type="primary" size="small" @click="triggerUpload">
                <el-icon><Upload /></el-icon>
                上传头像
              </el-button>
              <input 
                type="file" 
                ref="uploadInput" 
                style="display: none;" 
                accept="image/jpeg,image/png"
                @change="handleFileChange"
              >
            </div>
          </div>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="isSubmitting" @click="submitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Upload } from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import useUserStore from '@/stores/modules/user';
import { ref, computed, onMounted } from 'vue';
import { reqUserInfo, updateUserInfo } from '@/api/user';
import { reqUserModels } from '@/api/model';
import type { updateUserForm } from '@/api/user/type';
import type { ModelListResponse } from '@/api/model/type';

const router = useRouter();
const userStore = useUserStore();
const defaultAvatar = '/default-avatar.png'; // 默认头像路径

// 编辑表单
const dialogVisible = ref(false);
const isSubmitting = ref(false);
const uploadInput = ref<HTMLInputElement | null>(null);
const editForm = ref({
  username: '',
  email: '',
});

// 头像文件和预览
const avatarFile = ref<File | null>(null);
const avatarPreview = ref<string>('');

// 用户类型文本
const userTypeText = computed(() => {
  const typeMap: Record<string, string> = {
    'admin': '管理员',
    'teacher': '教师',
    'student': '学生'
  };
  return typeMap[userStore.userType] || userStore.userType;
});

// 用户模型列表
const userModels = ref<ModelListResponse['data']>([]);
const isLoadingModels = ref(false);

// 获取完整的图片URL
const getImgUrl = (url: string) => {
  if (!url) return '/hakasei.png';
  
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 如果是相对路径，添加API基础URL
  const baseURL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:9090';
  
  // 确保路径正确
  if (url.startsWith('/')) {
    return `${baseURL}${url}`;
  } else {
    return `${baseURL}/${url}`;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 获取用户信息
const fetchUserInfo = () => {
  // 直接使用Pinia仓库中的用户信息，不再发送请求
  editForm.value.username = userStore.username;
  editForm.value.email = userStore.email;
};

// 获取用户模型列表
const fetchUserModels = async () => {
  if (!userStore.userId) return;
  
  isLoadingModels.value = true;
  try {
    const response = await reqUserModels(userStore.userId);
    if (response.code === 200) {
      userModels.value = response.data;
    } else {
      ElMessage.error('获取模型列表失败');
    }
  } catch (error) {
    console.error('获取模型列表失败:', error);
    ElMessage.error('获取模型列表失败');
  } finally {
    isLoadingModels.value = false;
  }
};

// 处理编辑按钮点击
const handleEdit = () => {
  editForm.value.username = userStore.username;
  avatarPreview.value = '';
  avatarFile.value = null;
  dialogVisible.value = true;
};

// 触发文件上传
const triggerUpload = () => {
  if (uploadInput.value) {
    uploadInput.value.click();
  }
};

// 处理文件选择变化
const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];
    
    // 验证文件类型和大小
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt2M = file.size / 1024 / 1024 < 2;
    
    if (!isJPG) {
      ElMessage.error('上传头像图片只能是 JPG/PNG 格式!');
      return;
    }
    
    if (!isLt2M) {
      ElMessage.error('上传头像图片大小不能超过 2MB!');
      return;
    }
    
    // 保存文件引用
    avatarFile.value = file;
    
    // 创建本地预览
    const reader = new FileReader();
    reader.onload = (e) => {
      if (e.target) {
        avatarPreview.value = e.target.result as string;
      }
    };
    reader.readAsDataURL(file);
  }
};

// 取消编辑
const cancelEdit = () => {
  dialogVisible.value = false;
  // 重置表单和头像状态
  avatarFile.value = null;
  avatarPreview.value = '';
};

// 提交编辑
const submitEdit = async () => {
  isSubmitting.value = true;
  
  try {
    // 准备更新数据
    const updateData: updateUserForm = {
      userId: userStore.userId,
      username: editForm.value.username,
      email: editForm.value.email,
    };
    
    // 如果有新上传的头像文件，添加到更新数据
    if (avatarFile.value) {
      updateData.avatarFile = avatarFile.value;
    }
    
    // 调用更新用户信息的API
    const response = await updateUserInfo(updateData);
    console.log(response)
    if (response.code === 200) {
      // 更新本地状态
      userStore.username = response.data.username;
      
      if (response.data.avatar) {
        userStore.avatar = response.data.avatar;
      }
      
      ElMessage.success('个人信息更新成功');
      dialogVisible.value = false;
      
      // 重置头像状态
      avatarFile.value = null;
      avatarPreview.value = '';
    } else {
      ElMessage.error(response.data.message || '更新失败');
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    ElMessage.error('更新用户信息失败');
  } finally {
    isSubmitting.value = false;
  }
};

// 组件挂载时初始化用户信息和模型列表
onMounted(() => {
  // 检查pinia中是否已有数据
  if (userStore.username) {
    // 已有数据，直接使用
    fetchUserInfo();
    fetchUserModels();
  } else {
    // 没有数据，可能是直接访问页面或刷新，需要重新请求
    userStore.userInfo().then(() => {
      fetchUserInfo();
      fetchUserModels();
    }).catch(error => {
      console.error('获取用户信息失败:', error);
      ElMessage.error('获取用户信息失败');
    });
  }
});
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

.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.user-avatar {
  margin-bottom: 10px;
}

.user-details {
  width: 100%;
  max-width: 500px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  
  .label {
    width: 100px;
    font-weight: 500;
    color: var(--text-color-secondary);
  }
  
  .value {
    flex: 1;
    color: var(--text-color);
  }
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

// 头像上传样式
.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  
  .avatar {
    border-radius: 50%;
    object-fit: cover;
  }
  
  .avatar-upload-btn {
    margin-top: 10px;
  }
}

.model-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.model-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.model-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.model-name {
  font-weight: 500;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-description {
  font-size: 12px;
  color: var(--text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.empty-message {
  width: 100%;
  text-align: center;
  color: var(--text-color-secondary);
  padding: 20px;
}
</style>