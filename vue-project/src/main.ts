// import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import pinia from './stores'
import router from './router'
import './permission'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//引入全局样式
import '@/styles/index.scss'
import 'element-plus/theme-chalk/display.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
// 引入自定义暗色主题样式
import '@/styles/dark/css-vars.css'
import useLayOutSettingStore from './stores/modules/setting'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化主题
const settingStore = useLayOutSettingStore(pinia);
settingStore.initTheme();

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
