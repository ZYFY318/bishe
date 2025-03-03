import SvgIcon from "./Svgicon/index.vue";

const allGlobalComponents = { SvgIcon };
export default {
  install(app) {
    Object.keys(allGlobalComponents).forEach((item) => {
      app.component(item, allGlobalComponents[item]);
    });
  },
};
