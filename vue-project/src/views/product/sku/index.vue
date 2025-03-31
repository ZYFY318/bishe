<template>
  <div class="main">
    <!-- 选择题目数量页面 -->
    <div v-if="!isTesting" class="selection-page">
      <div class="selection-card">
        <el-button type="primary" style="margin-top: 1rem; width: 50%;" @click="startTest">开始测试</el-button>
        <div>
          <el-radio-group v-model="selectedNum" size="medium" class="el-group">
            <el-radio-button label="3">3题</el-radio-button>
            <el-radio-button label="5">5题</el-radio-button>
            <el-radio-button label="10">10题</el-radio-button>
          </el-radio-group>
        </div>
      </div>

    </div>

    <!-- 试卷内容页面 -->
    <el-header v-if="isTesting" class="header">
      <el-button type="danger" @click="exitTest">退出答题</el-button>
      <div class="header-right">
        <el-button type="success" style="width: 60px;" @click="submitTest" :disabled="isSubmitted">
          交卷
        </el-button>
      </div>
    </el-header>

    <!-- 交卷后显示成绩信息 -->
    <div v-if="isSubmitted" class="score-info">
      <el-text type="success">得分: {{ score }}</el-text>
      <el-text type="primary">答对: {{ correctCount }} / {{ questions.length }}</el-text>
      <el-text type="warning">用时: {{ timeUsed }} 秒</el-text>
    </div>

    <el-main v-if="isTesting" class="main-content">
      <el-card class="exam-card">
        <el-scrollbar height="70vh">
          <div v-for="(question, index) in questions" :key="question.id" class="question">
            <el-text class="question-text">{{ index + 1 }}. {{ question.title }}</el-text>
            <div class="options">
              <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-card"
                @click="selectOption(index, optIndex)" :class="{
                  selected: selectedAnswers[index] === optIndex && !isSubmitted,
                  correct: isSubmitted && optIndex === question.correctIndex,
                  incorrect: isSubmitted && selectedAnswers[index] === optIndex && optIndex !== question.correctIndex
                }">
                {{ option }}
              </div>
            </div>
            <!-- 交卷后显示正确答案 -->
            <div v-if="isSubmitted" class="answer-info">
              正确答案：{{ question.answer }}
            </div>
          </div>
        </el-scrollbar>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { reqRandomQuestion } from '@/api/product/trademark';
import { ref } from 'vue';
import { reqSaveExamResult } from '@/api/examResult';
import { ElMessage } from 'element-plus';
import useUserStore from '@/stores/modules/user';
const isTesting = ref(false); // 是否在答题页面
const isSubmitted = ref(false); // 是否已交卷
const selectedNum = ref(3); // 用户选择的题目数量
const questions = ref([]); // 题目数组
const selectedAnswers = ref({}); // 用户选择的答案
const score = ref(0); // 分数
const correctCount = ref(0); // 答对题目数
const timeUsed = ref(0); // 用时
let startTime = 0;
let useStore = useUserStore();
// 开始测试
const startTest = async () => {
  try {
    const response = await reqRandomQuestion(selectedNum.value);
    console.log(response);

    questions.value = response.data.map(q => ({
      id: q.id,
      title: q.title,
      options: q.options,
      answer: q.answer,
      correctIndex: q.options.indexOf(q.answer) // 获取正确答案的索引
    })) || [];

    selectedAnswers.value = {}; // 清空选项
    isTesting.value = true; // 进入答题页面
    isSubmitted.value = false; // 取消交卷状态
    startTime = Date.now(); // 记录开始时间
  } catch (error) {
    console.error("获取题目失败:", error);
  }
};

// 退出答题，返回题目选择页面
const exitTest = () => {
  isTesting.value = false;
  isSubmitted.value = false;
};

// 选择答案
const selectOption = (questionIndex, optionIndex) => {
  if (!isSubmitted.value) {
    selectedAnswers.value[questionIndex] = optionIndex;
  }
};

// 交卷
const submitTest = async () => {
  const examTypeMap = { 3: 1, 5: 2, 10: 3 };
  const examType = examTypeMap[selectedNum.value] || 1; // 默认值为 1
  isSubmitted.value = true;
  let correct = 0;

  Object.keys(selectedAnswers.value).forEach(index => {
    if (selectedAnswers.value[index] === questions.value[index].correctIndex) {
      correct++;
    }
  });

  correctCount.value = correct;
  score.value = Math.round((correct / questions.value.length) * 100); // 计算分数
  timeUsed.value = Math.round((Date.now() - startTime) / 1000); // 计算用时

  // 构造考试成绩对象
  const examResult = {
    userId: useStore.userId, // 这里应该是当前用户的ID，你可以根据你的项目逻辑获取
    examDate: new Date().toISOString(), // 当前时间
    score: score.value,
    duration: timeUsed.value,
    examType: examType,
  };

  try {
    const response = await reqSaveExamResult(examResult);
    if (response.code === 200) {
      ElMessage.success('考试成绩已保存！');
    } else {
      ElMessage.error('保存成绩失败！');
    }
  } catch (error) {
    console.error("提交考试成绩失败:", error);
    ElMessage.error('提交失败，请稍后重试');
  }
};
</script>

<style scoped>
/* 选择题目数量页面 */
.main {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
  align-items: center;
}

.selection-page {
  display: flex;
  flex-direction: column;
  background-color: aliceblue;
  text-align: center;
  /* align-items: center; */
  justify-content: center;
  /* width: 60%; */

}

.selection-card {
  text-align: center;
  justify-content: center;
  align-items: center;
  display: flex;
  flex-direction: column;
  height: 10pc;
}

.el-group {
  width: 100%;
  margin-top: 1rem;
}

/* 头部导航栏样式 */
.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #f3f4f6;
  border-bottom: 1px solid #ddd;
  border-radius: 10px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* 成绩信息 */
.score-info {
  display: flex;
  justify-content: center;
  gap: 2rem;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 10px;
  margin-top: 10px;
}

/* 主要内容区域样式 */
.main-content {
  flex: 1;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-radius: 10px;
}

.exam-card {
  width: 100%;
  max-width: 900px;
  padding: 1.5rem;
}

.question {
  margin-bottom: 1.5rem;
}

.question-text {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 1rem;
  display: block;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.option-card {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
  text-align: center;
}

/* 用户点击选项时 */
.option-card.selected {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
  transform: scale(1.05);
}

/* 交卷后，正确答案绿色 */
.option-card.correct {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

/* 交卷后，错误答案红色 */
.option-card.incorrect {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

/* 正确答案信息 */
.answer-info {
  margin-top: 5px;
  font-size: 0.9rem;
  color: #333;
}
</style>
