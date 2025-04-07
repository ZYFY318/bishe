//layout相关配置仓库
import { Fold } from '@element-plus/icons-vue';
import { defineStore } from 'pinia';

// 定义主题类型
export type ThemeType = 'light' | 'dark';

const useLayOutSettingStore = defineStore('SettingStore', {
  state: () => {
    return {
      fold: false,
      refresh: false,
      // 主题设置，默认为dark
      theme: localStorage.getItem('theme') as ThemeType || 'dark',
    };
  },
  actions: {
    // 切换主题
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light';
      // 保存到localStorage
      localStorage.setItem('theme', this.theme);
      // 更新文档根元素的data-theme属性
      document.documentElement.setAttribute('data-theme', this.theme);
      
      // 添加或移除Element Plus暗色模式类
      if (this.theme === 'dark') {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }
    },
    // 初始化主题
    initTheme() {
      document.documentElement.setAttribute('data-theme', this.theme);
      
      // 初始化时应用Element Plus暗色模式类
      if (this.theme === 'dark') {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }
    },
  },
});

export default useLayOutSettingStore;
