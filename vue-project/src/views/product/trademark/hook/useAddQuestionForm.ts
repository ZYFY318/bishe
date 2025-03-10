// src/features/question/hooks/useQuestionForm.ts
import { ref } from "vue";
import { ElMessage } from "element-plus";
import type { Question } from "@/api/product/trademark/type";
// import { createQuestion } from '../api/questionApi'
import { addQuestion } from "@/api/product/trademark";
import { useQuestion } from "./useQuestion";
export const useAddQuestionForm = (refreshList: () => Promise<void>) => {
  const { pageNo, total } = useQuestion();
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
      pageNo.value = 1; // 新增此行
      console.log("here", total.value);
      await addQuestion(formData.value);
      dialogVisible.value = false;
      await refreshList();
      ElMessage.success("操作成功");
    } catch (error) {
      console.error("提交失败:", error);
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
