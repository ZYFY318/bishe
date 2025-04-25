<template>
    
    <el-button size="small" icon="Refresh" circle @click="update"></el-button>
    <el-button size="small" icon="FullScreen" circle @click="fullScreen"></el-button>
    <el-button size="small" icon="Setting" circle></el-button>
    
    <!-- 主题切换开关 -->
    <el-tooltip
      :content="LayOutSettingStore.theme === 'dark' ? '切换到亮色模式' : '切换到暗色模式'"
      placement="bottom"
    >
      <div class="theme-switch">
        <el-switch
          v-model="isDarkTheme"
          inline-prompt
          :active-action-icon="Moon"
          :inactive-action-icon="Sunny"
          @change="changeTheme"
        />
      </div>
    </el-tooltip>
    <img :src="getImgUrl(userStore.avatar)" class="user-avatar">
    <el-dropdown>
        <span class="el-dropdown-link">
            <span class="username">{{ userStore.username }}</span>
            <el-icon class="el-icon--right">
                <arrow-down />
            </el-icon>
        </span>
        <template #dropdown>
            <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
        </template>
    </el-dropdown>
</template>
<script setup lang="ts">
import useLayOutSettingStore from '@/stores/modules/setting';
import useUserStore from '@/stores/modules/user';
import { useRoute, useRouter } from 'vue-router';
import { ref, computed } from 'vue';
import { Moon, Sunny } from '@element-plus/icons-vue';

const $router = useRouter();
const $route = useRoute();
const LayOutSettingStore = useLayOutSettingStore();
const userStore = useUserStore();

// 计算属性用于主题切换开关
const isDarkTheme = computed({
  get: () => LayOutSettingStore.theme === 'dark',
  set: (val) => {
    // 这里无需处理，通过 change 事件处理
  }
});

// 切换主题
const changeTheme = () => {
  LayOutSettingStore.toggleTheme();
};

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

const update = () => {
    LayOutSettingStore.refresh = !LayOutSettingStore.refresh;
}
console.log(userStore.username);
const fullScreen = () => {
    let full = document.fullscreenElement;
    if (!full) {
        document.documentElement.requestFullscreen();
    } else {
        document.exitFullscreen();
    }
}
const logout = () => {
    //1.需要向服务器发送请求[退出登录接口]

    //2.仓库关于用户相关数据得清空掉
    userStore.userLogout();
    //3.跳转到登陆页面 - 删除redirect参数，直接跳转到登录页  
    $router.push({ path: '/login' })
}
</script>
<style lang="scss" scoped>
button {
    background-color: transparent;
    font-size: 18px;
    color: var(--text-color);
    border-color: var(--border-color);
    
    &:hover {
        color: var(--primary-color);
        border-color: var(--primary-color);
        background-color: rgba(64, 158, 255, 0.1);
    }
}

.theme-switch {
  display: inline-block;
  margin: 0 10px;
  cursor: pointer;
}

.user-avatar {
    width: 30px;
    height: 30px;
    margin: 0px 10px;
    border-radius: 50%;
    border: 2px solid var(--border-color);
}

.username {
    font-size: 16px;
    font-weight: bold;
    color: var(--text-color);
}

.el-icon--right {
    font-size: 16px;
    color: var(--text-color);
}
</style>