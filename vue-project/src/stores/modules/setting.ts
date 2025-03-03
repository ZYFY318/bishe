//layout相关配置仓库
import { Fold } from '@element-plus/icons-vue';
import { defineStore } from 'pinia';
const useLayOutSettingStore = defineStore('SettingStore', {
  state: () => {
    return {
      fold: false,
    };
  },
});

export default useLayOutSettingStore;
