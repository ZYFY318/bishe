<template>
  <div class="exam-take-container">
    <!-- 考试头部信息 -->
    <div class="exam-header">
      <div class="exam-info">
        <h1 class="exam-title">{{ examTitle }}</h1>
        <div class="exam-meta">
          <span class="duration"><el-icon><Timer /></el-icon> 时长: {{ exam?.duration || 0 }} 分钟</span>
          <span class="creator" v-if="exam?.creatorName"><el-icon><User /></el-icon> 出题人: {{ exam?.creatorName }}</span>
        </div>
      </div>
      <div class="exam-timer">
        <div class="time-left">
          <span class="time-value">{{ formatTime(timeLeft) }}</span>
          <span class="time-label">剩余时间</span>
        </div>
      </div>
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
                  :class="{ 'is-answered': isQuestionAnswered(index) }">
            <div class="question-header">
              <div class="question-number">第 {{ index + 1 }} 题</div>
            </div>
            <div class="question-title">{{ question.title }}</div>
            <div class="question-options">
              <el-radio-group v-model="answers[String(question.id)]" @change="() => handleAnswerChange(String(question.id))">
                <div 
                  v-for="(option, index) in question.options" 
                  :key="index" 
                  class="option-item"
                >
                  <el-radio :label="index">
                        <span class="option-text">{{ option }}</span>
                  </el-radio>
                </div>
              </el-radio-group>
            </div>
          </el-card>
        </div>
        
        <!-- 右侧答题卡 -->
        <div class="answer-card">
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
import { Timer, User, Warning, DocumentCopy } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getExamQuestions, fetchExamDetail } from '@/api/product/exam';
import { reqSaveExamResult } from '@/api/examResult';
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
      // 将用户选择的选项索引转换为选项内容
      const userAnswer = question.options[selectedIndex];
      // 获取正确答案的选项内容
      const correctAnswer = question.options[Number(question.answer)];
      
      // 比较用户选择的选项内容和正确答案的选项内容
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
      examDate: new Date().toISOString(),
      score,
      duration,
    };
    
    // 保存到本地状态，用于显示结果
    examResult.value = {
      score,
      duration
    };
    
    // 提交到服务器
    const result = await reqSaveExamResult(examResultData) as any;
    console.log(result.code)
    // 正确获取响应数据
    if (result.code === 200) {
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
  router.push('/study/exam-list');
};

// 离开页面前提示
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (!showResultDialog.value) {
    const message = '离开页面将导致考试中断，确定要离开吗？';
    e.returnValue = message;
    return message;
  }
};

// 组件挂载
onMounted(async () => {
  window.addEventListener('beforeunload', beforeUnloadHandler);
  
  await loadExamDetail();
  await loadExamQuestions();
  
  // 记录开始时间
  startTime.value = Date.now();
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
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  margin-bottom: 20px;
  
  .exam-info {
    .exam-title {
      font-size: 24px;
      margin: 0 0 10px;
      color: white;
    }
    
    .exam-meta {
      display: flex;
      gap: 15px;
      color: rgba(255, 255, 255, 0.8);
      
      span {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }
  
  .exam-timer {
    background-color: rgba(0, 0, 0, 0.2);
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
        color: #ff6b6b;
      }
      
      .time-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.7);
        margin-top: 4px;
      }
    }
  }
}

.loading-container {
  flex: 1;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.empty-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 40px;
  
  .empty-icon {
    font-size: 100px;
    color: rgba(255, 255, 255, 0.3);
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
        background-color: rgba(255, 255, 255, 0.1);
        border-radius: 10px;
        transition: all 0.3s ease;
        padding: 20px;
        
        &.is-answered {
          border: 1px solid rgba(64, 158, 255, 0.5);
          box-shadow: 0 0 10px rgba(64, 158, 255, 0.2);
        }
        
        .question-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 15px;
          
          .question-number {
            font-weight: bold;
            color: white;
            font-size: 16px;
            background-color: rgba(64, 158, 255, 0.2);
            padding: 4px 10px;
            border-radius: 4px;
          }
        }
        
        .question-title {
          font-size: 18px !important;
          font-weight: 600 !important;
          margin-bottom: 20px;
          color: white;
          line-height: 1.5;
        }
        
        .question-options {
          .option-item {
            display: flex;
            align-items: flex-start;
            margin-right: 0;
            margin-bottom: 10px;
            color: white;
            padding: 8px 12px;
            border-radius: 5px;
            transition: background-color 0.3s;
            
            &:hover {
              background-color: rgba(255, 255, 255, 0.1);
            }
            
            .option-label {
              margin-right: 10px;
              font-weight: bold;
              font-size: 16px;
            }
            
            .option-text {
              word-break: break-word;
              font-size: 16px;
              line-height: 1.5;
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
            color: #e5eaf3;
            padding-left: 8px;
            display: flex;
            font-size: 16px;
            font-weight: 500;
          }
          
          :deep(.el-radio__inner) {
            background-color: transparent;
            border-color: rgba(255, 255, 255, 0.5);
          }
        }
      }
    }
    
    .answer-card {
      width: 250px;
      background-color: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(10px);
      border-radius: 10px;
      padding: 20px;
      height: fit-content;
      position: sticky;
      top: 20px;
      
      .card-title {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 15px;
        color: white;
        text-align: center;
      }
      
      .progress-info {
        margin-bottom: 20px;
        
        .progress-text {
          display: flex;
          justify-content: space-between;
          color: white;
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
    color: #e6a23c;
    
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
      background-color: #409eff;
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
      color: #606266;
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
        color: #909399;
        margin-bottom: 5px;
      }
      
      .stat-value {
        font-size: 18px;
        font-weight: bold;
        color: #606266;
      }
    }
  }
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: white;
}

/* 自定义滚动条 */
.questions-wrapper {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.3) transparent;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(255, 255, 255, 0.3);
    border-radius: 3px;
  }
}
</style> 