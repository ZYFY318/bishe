<template>
  <div class="exam-take-container">
    <!-- 考试头部信息 -->
    <div class="exam-header">
      <div class="exit-button" @click="confirmExit">
        <el-icon class="back-icon"><ArrowLeft /></el-icon>
      </div>
      <div class="exam-info">
        <h1 class="exam-title">{{ examTitle }}</h1>
        <div class="exam-meta">
          <span class="duration"><el-icon><Timer /></el-icon> 时长: {{ exam?.duration || 0 }} 分钟</span>
          <span class="creator" v-if="exam?.creatorName"><el-icon><User /></el-icon> 出题人: {{ exam?.creatorName }}</span>
        </div>
      </div>
      
      <!-- 未完成试卷显示剩余时间 -->
      <div class="exam-timer" v-if="!isCompleted">
        <div class="time-left">
          <span class="time-value">{{ formatTime(timeLeft) }}</span>
          <span class="time-label">剩余时间</span>
        </div>
      </div>
      
      <!-- 已完成试卷显示分数 -->
     
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 考试内容 -->
    <div v-else-if="questions.length > 0" class="exam-content">
      <div class="questions-container">
        <!-- 问题列表 -->
        <div class="questions-wrapper">
          <el-card v-for="(question, index) in questions" 
                  :key="question.id" 
                  class="question-card"
                  :class="{ 
                    'is-answered': isQuestionAnswered(index),
                    'is-correct': isCompleted && isAnswerCorrect(index),
                    'is-incorrect': isCompleted && !isAnswerCorrect(index) && isQuestionAnswered(index)
                  }">
            <div class="question-header">
              <div class="question-number">第 {{ index + 1 }} 题</div>
              <div v-if="isCompleted" class="question-result">
                <span v-if="isAnswerCorrect(index)" class="correct">正确</span>
                <span v-else-if="isQuestionAnswered(index)" class="incorrect">错误</span>
                <span v-else class="unanswered">未作答</span>
              </div>
            </div>
            <div class="question-title">{{ question.title }}</div>
            <div class="question-options">
              <el-radio-group v-model="answers[String(question.id)]" @change="() => handleAnswerChange(String(question.id))" :disabled="isCompleted">
                <div 
                  v-for="(option, optIndex) in question.options" 
                  :key="optIndex" 
                  class="option-item"
                  :class="{
                    'correct-option': isCompleted && option === question.answer,
                    'incorrect-option': isCompleted && answers[String(question.id)] === optIndex && option !== question.answer
                  }"
                >
                  <el-radio :label="optIndex">
                        <span class="option-text">{{ option }}</span>
                  </el-radio>
                </div>
              </el-radio-group>
            </div>
            <div v-if="isCompleted && (isQuestionAnswered(index) || Number(question.answer) !== undefined)" class="answer-explanation">
              <div v-if="isQuestionAnswered(index)" class="user-answer">
                您的答案: {{ question.options[answers[String(question.id)]] }}
              </div>
              <div class="correct-answer">
                正确答案: {{ question.answer}}
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 右侧答题卡，仅在未完成时显示 -->
        <div v-if="!isCompleted" class="answer-card">
          <div class="card-title">答题卡</div>
          <div class="progress-info">
            <div class="progress-text">
              已答 {{ answeredCount }}/{{ questions.length }}
            </div>
            <el-progress 
              :percentage="(answeredCount / questions.length) * 100" 
              :stroke-width="12" 
              status="success" />
          </div>
          <div class="question-buttons">
            <el-button 
              v-for="(_, index) in questions" 
              :key="index"
              size="small"
              :type="isQuestionAnswered(index) ? 'primary' : 'default'"
              @click="scrollToQuestion(index)"
            >
              {{ index + 1 }}
            </el-button>
          </div>
          <div class="submit-area">
            <el-button type="danger" size="large" @click="confirmSubmit" :disabled="submitting">
              {{ submitting ? '提交中...' : '交卷' }}
            </el-button>
          </div>
        </div>
        
        <!-- 已完成试卷时的侧边栏信息 -->
        <div v-else class="result-sidebar">
          <div class="card-title">考试结果</div>
          <div class="result-info">
            <div class="score-display">
              <div class="score-circle">{{ examResult.score }}</div>
              <div class="score-text">得分</div>
            </div>
            <div class="stats-summary">
              <div class="stat-item">
                <div class="stat-label">用时</div>
                <div class="stat-value">{{ formatDuration(examResult.duration) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">正确题数</div>
                <div class="stat-value">{{ correctCount }}/{{ questions.length }}</div>
              </div>
            </div>
          </div>
          <div class="legend">
            <div class="legend-item">
              <span class="legend-color correct"></span>
              <span class="legend-text">正确答案</span>
            </div>
            <div class="legend-item">
              <span class="legend-color incorrect"></span>
              <span class="legend-text">错误答案</span>
            </div>
            <div class="legend-item">
              <span class="legend-color unanswered"></span>
              <span class="legend-text">未作答</span>
            </div>
          </div>
          <div class="exit-area">
            <el-button type="primary" size="large" @click="goBack">
              返回试卷列表
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 无题目提示 -->
    <div v-else class="empty-container">
      <el-empty description="该试卷暂无题目" :image-size="200">
        <template #image>
          <el-icon class="empty-icon"><DocumentCopy /></el-icon>
        </template>
        <el-button type="primary" @click="goBack">返回试卷列表</el-button>
      </el-empty>
    </div>
    
    <!-- 交卷确认对话框 -->
    <el-dialog
      v-model="showConfirmDialog"
      title="确认交卷"
      width="30%"
      center
    >
      <div class="confirm-content">
        <p v-if="answeredCount < questions.length" class="warning-text">
          <el-icon><Warning /></el-icon>
          您还有 {{ questions.length - answeredCount }} 道题目未作答，确定要交卷吗？
        </p>
        <p v-else>
          确定要交卷吗？交卷后将无法修改答案。
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showConfirmDialog = false">取消</el-button>
          <el-button type="primary" @click="submitExam" :loading="submitting">
            确认交卷
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 交卷结果对话框 -->
    <el-dialog
      v-model="showResultDialog"
      title="考试结果"
      width="40%"
      center
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="result-content">
        <div class="score-display">
          <div class="score-circle">{{ examResult.score }}</div>
          <div class="score-text">您的得分</div>
        </div>
        <div class="result-stats">
          <div class="stat-item">
            <span class="stat-label">用时</span>
            <span class="stat-value">{{ formatDuration(examResult.duration) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">正确题数</span>
            <span class="stat-value">{{ correctCount }}/{{ questions.length }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="goBack">返回试卷列表</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Timer, User, Warning, DocumentCopy, ArrowLeft } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getExamQuestions, fetchExamDetail } from '@/api/product/exam';
import { reqSaveExamResult, reqUserExamResults } from '@/api/examResult';
import type { QuestionItem, ExamItem } from '@/api/product/exam/type';
import type { ExamResult } from '@/api/examResult/type';
import useUserStore from '@/stores/modules/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 如果不是学生，跳转回列表页
if (userStore.userType !== 'STUDENT') {
  ElMessage.warning('只有学生用户可以参加考试');
  router.push('/study/exam-list');
}

// 从路由参数中获取试卷ID和标题
const examId = ref(Number(route.query.examId) || 0);
const examTitle = ref(route.query.examTitle as string || '未命名试卷');

// 试卷数据
const loading = ref(true);
const exam = ref<ExamItem | null>(null);
const questions = ref<QuestionItem[]>([]);
const answers = ref<Record<string, number>>({});
const startTime = ref(Date.now());
const timeLeft = ref(0);
const timer = ref<any>(null);
const submitting = ref(false);
const showConfirmDialog = ref(false);
const showResultDialog = ref(false);
const isCompleted = ref(false); // 是否已完成该试卷

// 考试结果
const examResult = ref<{score: number, duration: number}>({
  score: 0,
  duration: 0
});

// 计算已答题数量
const answeredCount = computed(() => {
  return Object.keys(answers.value).filter(key => answers.value[key] !== undefined).length;
});

// 计算正确题数
const correctCount = computed(() => {
  return calculateCorrectAnswers(questions.value, answers.value);
});

// 封装计算正确答案数量的函数
const calculateCorrectAnswers = (questions: QuestionItem[], answers: Record<string, number>) => {
  let count = 0;
  questions.forEach((question) => {
    const questionId = String(question.id);
    // 获取用户选择的选项索引
    const selectedIndex = answers[questionId];
    
    // 只有当用户已选择答案时才进行比较
    if (selectedIndex !== undefined) {
      // 获取用户选择的选项内容
      const userAnswer = question.options[selectedIndex];
      // 获取正确答案的选项内容
      const correctAnswer = question.answer;
      
      // 比较用户选择的选项内容和正确答案
      if (userAnswer === correctAnswer) {
        count++;
      }
    }
  });
  return count;
};

// 检查题目是否已回答
const isQuestionAnswered = (index: number) => {
  const question = questions.value[index];
  return answers.value[String(question.id)] !== undefined;
};

// 加载试卷详情
const loadExamDetail = async () => {
  try {
    const result = await fetchExamDetail(examId.value);
    if (result.code === 200) {
      exam.value = result.data;
      // 设置倒计时
      timeLeft.value = exam.value.duration * 60; // 转换为秒
      startTimer();
    } else {
      ElMessage.error(result.message || '获取试卷详情失败');
    }
  } catch (error) {
    console.error('获取试卷详情出错:', error);
    ElMessage.error('获取试卷详情失败');
  }
};

// 加载试卷题目
const loadExamQuestions = async () => {
  if (!examId.value) {
    ElMessage.error('试卷ID无效');
    return;
  }

  try {
    loading.value = true;
    const result = await getExamQuestions(examId.value);
    if (result.code === 200) {
      questions.value = result.data.questions;
      // 初始化答案对象
      answers.value = {};
    } else {
      ElMessage.error(result.message || '获取试卷题目失败');
    }
  } catch (error) {
    console.error('获取试卷题目出错:', error);
    ElMessage.error('获取试卷题目失败');
  } finally {
    loading.value = false;
  }
};

// 启动倒计时
const startTimer = () => {
  timer.value = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      // 时间到，自动提交
      clearInterval(timer.value);
      ElMessage.warning('考试时间已到，系统将自动提交您的答案');
      submitExam();
    }
  }, 1000);
};

// 格式化剩余时间
const formatTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 格式化考试用时
const formatDuration = (seconds: number) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  let result = '';
  if (hours > 0) {
    result += `${hours}小时`;
  }
  if (minutes > 0 || hours > 0) {
    result += `${minutes}分钟`;
  }
  result += `${secs}秒`;
  
  return result;
};

// 答案变更处理
const handleAnswerChange = (questionId: string) => {
  // 记录答题状态
  console.log(`Question ${questionId} answered with: ${answers.value[questionId]}`);
};

// 滚动到指定题目
const scrollToQuestion = (index: number) => {
  const questionCards = document.querySelectorAll('.question-card');
  if (questionCards[index]) {
    nextTick(() => {
      questionCards[index].scrollIntoView({ behavior: 'smooth' });
    });
  }
};

// 确认交卷
const confirmSubmit = () => {
  showConfirmDialog.value = true;
};

// 检查用户历史考试记录，判断是否已完成该试卷
const checkExamCompletion = async () => {
  try {
    // 获取当前用户的考试记录
    const result = await reqUserExamResults(userStore.userId) as any;
    if (result.code === 200 && result.data && result.data.length > 0) {
      // 筛选当前试卷的考试记录
      const examResults = result.data.filter((record: any) => record.examId === examId.value);
      if (examResults.length > 0) {
        // 已完成该试卷，设置状态
        isCompleted.value = true;
        // 获取最近的一次考试记录
        const latestResult = examResults[examResults.length - 1];
        examResult.value = {
          score: latestResult.score,
          duration: latestResult.duration
        };
        
        // 如果有用户答案记录，则加载答案
        if (latestResult.userAnswers) {
          answers.value = JSON.parse(latestResult.userAnswers);
        }
      }
    }
  } catch (error) {
    console.error('获取考试记录出错:', error);
  }
};

// 判断答案是否正确
const isAnswerCorrect = (index: number) => {
  const question = questions.value[index];
  if (!question) return false;
  
  const questionId = String(question.id);
  const selectedIndex = answers.value[questionId];
  
  if (selectedIndex === undefined) return false;
  
  // 比较选择的选项内容和正确答案
  return question.options[selectedIndex] === question.answer;
};

// 提交考试
const submitExam = async () => {
  try {
    submitting.value = true;
    
    // 计算得分和用时
    const totalQuestions = questions.value.length;
    
    const correctAnswers = calculateCorrectAnswers(questions.value, answers.value);
    
    const endTime = Date.now();
    const duration = Math.round((endTime - startTime.value) / 1000); // 考试用时（秒）
    // 计算得分（满分100）
    const score = Math.round((correctAnswers / totalQuestions) * 100);
    
    // 构建考试结果
    const examResultData = {
      userId: userStore.userId,
      examId: examId.value, // 添加考试ID关联
      examDate: new Date().toISOString(),
      score,
      duration,
      userAnswers: JSON.stringify(answers.value), // 保存用户答案
      questionCount: totalQuestions // 添加题目数量字段
    };
    
    // 保存到本地状态，用于显示结果
    examResult.value = {
      score,
      duration
    };
    
    // 提交到服务器
    const result = await reqSaveExamResult(examResultData) as any;
    // 正确获取响应数据
    if (result.code === 200) {
      // 设置已完成状态
      isCompleted.value = true;
      // 显示结果对话框
      showConfirmDialog.value = false;
      showResultDialog.value = true;
    } else {
      const errorMsg = result.message || '提交失败';
      throw new Error(errorMsg);
    }
  } catch (error: any) {
    console.error('提交考试出错:', error);
    ElMessage.error(`提交失败: ${error.message || '未知错误'}`);
  } finally {
    submitting.value = false;
  }
};

// 返回试卷列表
const goBack = () => {
  router.back();
};

// 确认退出
const confirmExit = () => {
  // 已完成试卷直接返回，不需确认
  if (isCompleted.value) {
    goBack();
    return;
  }
  
  // 未完成试卷需要确认
  ElMessageBox.confirm(
    '确定要退出考试吗？退出后所有作答将丢失。',
    '退出确认',
    {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      goBack();
    })
    .catch(() => {
      // 用户取消，不做任何操作
    });
};

// 离开页面前提示
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  // 已完成试卷或已显示结果对话框时无需警告
  if (isCompleted.value || showResultDialog.value) {
    return;
  }
  
  // 未完成试卷需要警告
  const message = '离开页面将导致考试中断，确定要离开吗？';
  e.returnValue = message;
  return message;
};

// 组件挂载
onMounted(async () => {
  window.addEventListener('beforeunload', beforeUnloadHandler);
  
  await loadExamDetail();
  await loadExamQuestions();
  await checkExamCompletion();
  
  // 如果是未完成的试卷，才开始计时
  if (!isCompleted.value) {
    // 记录开始时间
    startTime.value = Date.now();
  } else {
    // 已完成试卷无需计时
    if (timer.value) {
      clearInterval(timer.value);
    }
  }
});

// 组件卸载前清理
onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler);
  if (timer.value) {
    clearInterval(timer.value);
  }
});
</script>

<style scoped lang="scss">
.exam-take-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
  overflow: hidden;
  color: var(--text-color);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: var(--card-bg);
  border-radius: 10px;
  margin-bottom: 20px;
  position: relative;
  box-shadow: var(--shadow);
  
  .exit-button {
    position: absolute;
    left: 20px;
    top: 50%;
    transform: translateY(-50%);
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background-color: var(--border-color);
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background-color: var(--primary-color);
    }
    
    .back-icon {
      font-size: 18px;
      color: var(--text-color);
    }
  }
  
  .exam-info {
    margin-left: 50px; /* 为退出按钮留出空间 */
    .exam-title {
      font-size: 24px;
      margin: 0 0 10px;
      color: var(--text-color);
    }
    
    .exam-meta {
      display: flex;
      gap: 15px;
      color: var(--text-color);
      opacity: 0.8;
      
      span {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }
  
  .exam-timer {
    background-color: var(--card-bg);
    border: 1px solid var(--border-color);
    padding: 15px 20px;
    border-radius: 8px;
    text-align: center;
    
    .time-left {
      display: flex;
      flex-direction: column;
      
      .time-value {
        font-size: 22px;
        font-weight: bold;
        font-family: monospace;
        color: var(--danger-color);
      }
      
      .time-label {
        font-size: 12px;
        color: var(--text-color);
        opacity: 0.7;
        margin-top: 4px;
      }
    }
  }
}

.loading-container {
  flex: 1;
  padding: 40px;
  background-color: var(--card-bg);
  border-radius: 10px;
  box-shadow: var(--shadow);
}

.empty-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--card-bg);
  border-radius: 10px;
  padding: 40px;
  box-shadow: var(--shadow);
  
  .empty-icon {
    font-size: 100px;
    color: var(--text-color);
    opacity: 0.3;
  }
}

.exam-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
 
  .questions-container {
    display: flex;
    gap: 20px;
    flex: 1;
    height: 100%;
    overflow: hidden;
 
    .questions-wrapper {
      flex: 1;
      overflow-y: auto;
      padding-right: 10px;
      height: calc(100vh - 250px);
      
      .question-card {
        margin-bottom: 20px;
        background-color: var(--card-bg);
        border-radius: 10px;
        transition: all 0.3s ease;
        padding: 20px;
        border: 1px solid var(--border-color);
        box-shadow: var(--shadow);
        
        &.is-answered {
          border: 1px solid var(--primary-color);
          box-shadow: 0 0 10px rgba(64, 158, 255, 0.2);
        }
        
        &.is-correct {
          border: 1px solid var(--success-color);
          box-shadow: 0 0 10px rgba(103, 194, 58, 0.2);
        }
        
        &.is-incorrect {
          border: 1px solid var(--danger-color);
          box-shadow: 0 0 10px rgba(245, 108, 108, 0.2);
        }
        
        .question-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 15px;
          
          .question-number {
            font-weight: bold;
            color: var(--text-color);
            font-size: 16px;
            background-color: var(--primary-color);
            padding: 4px 10px;
            border-radius: 4px;
            color: white;
          }
          
          .question-result {
            padding: 4px 10px;
            border-radius: 4px;
            font-weight: bold;
            
            .correct {
              color: var(--success-color);
            }
            
            .incorrect {
              color: var(--danger-color);
            }
            
            .unanswered {
              color: var(--text-color);
              opacity: 0.6;
            }
          }
        }
        
        .question-title {
          font-size: 18px !important;
          font-weight: 600 !important;
          margin-bottom: 20px;
          color: var(--text-color);
          line-height: 1.5;
        }
        
        .question-options {
          .option-item {
            display: flex;
            align-items: flex-start;
            margin-right: 0;
            margin-bottom: 10px;
            color: var(--text-color);
            padding: 8px 12px;
            border-radius: 5px;
            transition: background-color 0.3s;
            
            &.correct-option {
              background-color: rgba(103, 194, 58, 0.1);
              border: 1px solid rgba(103, 194, 58, 0.5);
            }
            
            &.incorrect-option {
              background-color: rgba(245, 108, 108, 0.1);
              border: 1px solid rgba(245, 108, 108, 0.5);
            }
            
            &:hover {
              background-color: var(--border-color);
            }
          }
          
          :deep(.el-radio-group) {
            display: flex;
            flex-direction: column;
            gap: 5px;
            width: 100%;
          }
          
          :deep(.el-radio) {
            margin-right: 0;
            width: 100%;
          }
          
          :deep(.el-radio__label) {
            color: var(--text-color);
            padding-left: 8px;
            display: flex;
            font-size: 16px;
            font-weight: 500;
          }
          
          :deep(.el-radio__inner) {
            background-color: var(--card-bg);
            border-color: var(--border-color);
          }
        }
        
        .answer-explanation {
          margin-top: 15px;
          padding-top: 15px;
          border-top: 1px dashed var(--border-color);
          
          .user-answer, .correct-answer {
            display: flex;
            align-items: center;
            margin-bottom: 5px;
            font-size: 14px;
          }
          
          .user-answer {
            color: var(--text-color);
          }
          
          .correct-answer {
            color: var(--success-color);
          }
        }
      }
    }
    
    .answer-card {
      width: 250px;
      background-color: var(--card-bg);
      border-radius: 10px;
      padding: 20px;
      height: fit-content;
      position: sticky;
      top: 20px;
      box-shadow: var(--shadow);
      border: 1px solid var(--border-color);
      
      .card-title {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 15px;
        color: var(--text-color);
        text-align: center;
      }
      
      .progress-info {
        margin-bottom: 20px;
        
        .progress-text {
          display: flex;
          justify-content: space-between;
          color: var(--text-color);
          margin-bottom: 8px;
        }
      }
      
      .question-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-bottom: 30px;
        
        .el-button {
          min-width: 40px;
          height: 40px;
          border-radius: 50%;
          padding: 0;
          font-weight: bold;
        }
      }
      
      .submit-area {
        display: flex;
        justify-content: center;
        
        .el-button {
          width: 100%;
        }
      }
    }
  }
}

.confirm-content {
  text-align: center;
  padding: 10px 0;
  
  .warning-text {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: var(--warning-color);
    
    .el-icon {
      font-size: 18px;
    }
  }
}

.result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  
  .score-display {
    text-align: center;
    margin-bottom: 30px;
    
    .score-circle {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      background-color: var(--primary-color);
      color: white;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 40px;
      font-weight: bold;
      margin: 0 auto 15px;
    }
    
    .score-text {
      font-size: 16px;
      color: var(--text-color);
    }
  }
  
  .result-stats {
    display: flex;
    justify-content: space-around;
    width: 100%;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .stat-label {
        font-size: 14px;
        color: var(--text-color);
        opacity: 0.7;
        margin-bottom: 5px;
      }
      
      .stat-value {
        font-size: 18px;
        font-weight: bold;
        color: var(--text-color);
      }
    }
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--border-color);
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--primary-color);
}

/* 自定义滚动条 */
.questions-wrapper {
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) transparent;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: var(--border-color);
    border-radius: 3px;
  }
}

// 已完成试卷的侧边栏样式
.result-sidebar {
  width: 250px;
  background-color: var(--card-bg);
  border-radius: 10px;
  padding: 20px;
  height: fit-content;
  position: sticky;
  top: 20px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border-color);
  
  .card-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 15px;
    color: var(--text-color);
    text-align: center;
  }
  
  .result-info {
    margin-bottom: 20px;
    
    .score-display {
      text-align: center;
      margin-bottom: 20px;
      
      .score-circle {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background-color: var(--primary-color);
        color: white;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 24px;
        font-weight: bold;
        margin: 0 auto 10px;
      }
      
      .score-text {
        font-size: 14px;
        color: var(--text-color);
        opacity: 0.7;
      }
    }
    
    .stats-summary {
      display: flex;
      flex-direction: column;
      gap: 10px;
      
      .stat-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .stat-label {
          color: var(--text-color);
          opacity: 0.7;
          font-size: 14px;
        }
        
        .stat-value {
          color: var(--text-color);
          font-weight: bold;
          font-size: 14px;
        }
      }
    }
  }
  
  .legend {
    margin-bottom: 20px;
    padding: 10px;
    background-color: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: 6px;
    
    .legend-item {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      
      .legend-color {
        width: 16px;
        height: 16px;
        border-radius: 4px;
        margin-right: 8px;
        
        &.correct {
          background-color: var(--success-color);
          opacity: 0.6;
        }
        
        &.incorrect {
          background-color: var(--danger-color);
          opacity: 0.6;
        }
        
        &.unanswered {
          background-color: var(--text-color);
          opacity: 0.3;
        }
      }
      
      .legend-text {
        color: var(--text-color);
        font-size: 12px;
      }
    }
  }
  
  .exit-area {
    margin-top: 20px;
    
    .el-button {
      width: 100%;
    }
  }
}
</style> 