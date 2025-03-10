import { ref } from "vue";
import type { Question } from "@/api/product/trademark/type";
import { reqQuestion } from "@/api/product/trademark";

export const useQuestion = () => {
  const pageNo = ref(1);
  const limit = ref(3);
  const total = ref(0);
  const questionArr = ref<Question[]>([]);

  const getQuestion = async () => {
    let result = await reqQuestion(pageNo.value, limit.value);
    console.log(result);
    if (result.code === 200) {
      total.value = result.data.totalElements;
      questionArr.value = result.data.content;
      console.log("当前页码:", pageNo.value);
      console.log("总数据量:", total.value);
      console.log("当前页数据:", questionArr.value);
    }
  };

  const handleSizeChange = (newSize: number) => {
    limit.value = newSize;
    pageNo.value = 1;
    getQuestion();
  };

  const handleCurrentChange = () => {
    getQuestion();
  };

  return {
    pageNo,
    limit,
    total,
    questionArr,
    getQuestion,
    handleSizeChange,
    handleCurrentChange,
  };
};
