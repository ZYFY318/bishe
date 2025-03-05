import SvgIcon from "./Svgicon/index.vue";
const allGlobalComponent: any = { SvgIcon };
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
export default {
  install(app: any) {
    Object.keys(allGlobalComponent).forEach((key) => {
      app.component(key, allGlobalComponent[key]);
    });
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
      app.component(key, component);
    }
  },
};
