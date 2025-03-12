import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { Question } from "@/api/product/trademark/type";
import { updateQuestion } from "@/api/product/trademark";
import { useQuestion } from "@/views/product/trademark/hook/useQuestion"; // 导入 questionArr

export const useEditQuestion = (refreshList: () => Promise<void>) => {
  const { questionArr } = useQuestion(); // 获取 questionArr
  const dialogVisible = ref(false);
  const formRef = ref();
  const formData = ref<Question>({
    id: undefined,
    title: "",
    options: ["", "", "", ""],
    answer: "",
  });

  const open = (question: Question) => {
    dialogVisible.value = true;
    formData.value = { ...question };
  };

  const submit = async () => {
    try {
      await formRef.value.validate();

      if (!formData.value.options.includes(formData.value.answer)) {
        ElMessage.error("答案必须存在于选项中");
        return;
      }

      const updatedQuestion = await updateQuestion(
        formData.value,
        formData.value.id as number
      );
      // 手动更新 questionArr
      refreshList();
      const index = questionArr.value.findIndex(
        (q) => q.id === formData.value.id
      );
      if (index !== -1) {
        questionArr.value.splice(index, 1, { ...formData.value });
      }
      dialogVisible.value = false;
      ElMessage.success("编辑成功");
    } catch (error) {
      console.error("编辑失败:", error);
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
