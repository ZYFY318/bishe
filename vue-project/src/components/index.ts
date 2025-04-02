import SvgIcon from "./Svgicon/index.vue";
import ItemCard from "./ItemCard/index.vue"
import threeModel from "./three/threeModel.vue";
import AddModelCard from "./AddModelCard/index.vue";
import ModelUploadDialog from "./ModelUploadDialog/index.vue";

const allGlobalComponent: any = { 
  SvgIcon,
  ItemCard,
  threeModel,
  AddModelCard,
  ModelUploadDialog
};

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
