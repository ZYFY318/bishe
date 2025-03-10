import type { FormRules } from "element-plus";

export const questionRules: FormRules = {
  title: [
    { required: true, message: "请输入题目名称", trigger: "blur" },
    { min: 2, max: 50, message: "长度在2到50个字符", trigger: "blur" },
  ],
  options: [
    {
      validator: (_, value: string[], callback) => {
        if (value.some((opt) => !opt?.trim())) {
          callback(new Error("所有选项必须填写"));
        } else if (new Set(value).size !== value.length) {
          callback(new Error("选项内容不能重复"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
  answer: [{ required: true, message: "请选择正确答案", trigger: "change" }],
};
