import "./assets/main.css";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import { createApp } from "vue";
import { createPinia } from "pinia";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import App from "./App.vue";
import router from "./router";
import "virtual:svg-icons-register";
import globalComponents from "@/components";
import "@/styles/index.scss";
const app = createApp(App);

app.use(ElementPlus, {
  locale: zhCn,
});
app.use(createPinia());
app.use(router);
app.use(globalComponents);

app.mount("#app");
