import { ref } from "vue";
import { ElMessage } from "element-plus";
import type { Question } from "@/api/product/trademark/type";
import { addQuestion } from "@/api/product/trademark";
export const useAddQuestionForm = (refreshList: () => Promise<void>) => {
  const dialogVisible = ref(false);
  const formRef = ref();
  const formData = ref<Question>({
    title: "",
    options: ["", "", "", ""],
    answer: "",
  });

  const open = () => {
    dialogVisible.value = true;
    reset();
  };

  const reset = () => {
    formData.value = {
      title: "",
      options: ["", "", "", ""],
      answer: "",
    };
    formRef.value?.resetFields();
  };

  const submit = async () => {
    try {
      await formRef.value.validate();

      if (!formData.value.options.includes(formData.value.answer)) {
        ElMessage.error("答案必须存在于选项中");
        return;
      }
      await addQuestion(formData.value);
      dialogVisible.value = false;
      await refreshList();

      ElMessage.success("题目添加成功");
    } catch (error) {
      console.error("题目添加失败:", error);
    }
  };

  return {
    dialogVisible,
    formRef,
    formData,
    open,
    submit,
  };
};
