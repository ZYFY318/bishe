<template>
  <div class="exam-edit-container">
    <div class="exam-header">
      <el-button text @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="exam-title">{{ examTitle }} - 编辑试卷</h1>
      <el-button type="primary" @click="saveExam" :loading="saving">保存试卷</el-button>
    </div>
    
    <div class="exam-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="已选题目" name="selected">
          <div v-if="selectedQuestions.length === 0" class="empty-questions">
            <el-empty description="当前试卷还没有题目，请从题库中选择题目" :image-size="150">
              <template #image>
                <el-icon class="empty-icon"><DocumentCopy /></el-icon>
              </template>
              <el-button type="primary" @click="activeTab = 'question-bank'">
                前往题库选择
              </el-button>
            </el-empty>
          </div>
          <el-scrollbar v-else height="70vh" class="custom-scrollbar">
            <div class="question-list">
              <el-card v-for="(question, index) in selectedQuestions" :key="question.id" class="question-card">
                <div class="question-header">
                  <div class="question-number">第 {{ index + 1 }} 题</div>
                  <el-button type="danger" size="small" text @click="removeQuestion(question.id)">
                    <el-icon><Delete /></el-icon>
                    移除
                  </el-button>
                </div>
                <div class="question-title">{{ question.title }}</div>
                <el-scrollbar height="150px" class="options-scrollbar">
                  <div class="question-options">
                    <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option">
                     
                      <span class="option-text" :class="{ 'is-correct': question.answer === String.fromCharCode(65 + optIndex) }">
                        {{ option }}
                      </span>
                    </div>
                  </div>
                </el-scrollbar>
              </el-card>
            </div>
          </el-scrollbar>
        </el-tab-pane>
        <el-tab-pane label="题库" name="question-bank">
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题目"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleBatchAdd" :disabled="selectedQuestionIds.length === 0">
              添加选中题目 ({{ selectedQuestionIds.length }})
            </el-button>
          </div>
          <el-scrollbar height="60vh" class="table-scrollbar">
            <el-table
              v-loading="loading"
              :data="questions"
              style="width: 100%"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column label="题目内容" prop="title" show-overflow-tooltip />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button
                    v-if="!isQuestionSelected(row.id)"
                    type="primary"
                    size="small"
                    @click="addQuestion(row)"
                  >
                    添加
                  </el-button>
                  <el-button
                    v-else
                    type="info"
                    size="small"
                    disabled
                  >
                    已添加
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-scrollbar>
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, DocumentCopy, Delete, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { fetchExamDetail, updateExam, getExamQuestions, addQuestionsToExam } from '@/api/product/exam';
import { reqQuestion, searchQuestion } from '@/api/product/trademark';
import type { QuestionItem } from '@/api/product/exam/type';

const route = useRoute();
const router = useRouter();

// 从路由参数中获取试卷ID和标题
const examId = ref(Number(route.query.examId) || 0);
const examTitle = ref(route.query.examTitle as string || '未命名试卷');

// 试卷内容
const loading = ref(false);
const saving = ref(false);
const activeTab = ref('selected');
const selectedQuestions = ref<QuestionItem[]>([]);
const searchKeyword = ref('');

// 分页数据
const questions = ref<QuestionItem[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 选中的题目
const selectedQuestionIds = ref<number[]>([]);

// 加载试卷数据
const loadExamData = async () => {
  if (!examId.value) {
    ElMessage.error('试卷ID无效');
    return;
  }

  try {
    loading.value = true;
    const result = await getExamQuestions(examId.value);
    if (result.code === 200) {
      selectedQuestions.value = result.data.questions;
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

// 加载题库数据
const loadQuestions = async () => {
  try {
    loading.value = true;
    let result;
    
    // 如果有搜索关键词，使用搜索API，否则使用普通分页API
    if (searchKeyword.value && searchKeyword.value.trim() !== '') {
      result = await searchQuestion(currentPage.value, pageSize.value, searchKeyword.value);
    } else {
      result = await reqQuestion(currentPage.value, pageSize.value);
    }
    
    if (result.code === 200) {
      questions.value = result.data.content;
      total.value = result.data.totalElements;
    } else {
      ElMessage.error(result.message || '获取题目列表失败');
    }
  } catch (error) {
    console.error('获取题目列表出错:', error);
    ElMessage.error('获取题目列表失败');
  } finally {
    loading.value = false;
  }
};

// 判断题目是否已选中
const isQuestionSelected = (questionId: number) => {
  return selectedQuestions.value.some(q => q.id === questionId);
};

// 添加单个题目
const addQuestion = (question: QuestionItem) => {
  if (!isQuestionSelected(question.id)) {
    selectedQuestions.value.push(question);
    ElMessage.success('题目已添加到试卷');
  }
};

// 移除题目
const removeQuestion = (questionId: number) => {
  ElMessageBox.confirm('确定要移除该题目吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    selectedQuestions.value = selectedQuestions.value.filter(q => q.id !== questionId);
    ElMessage.success('题目已从试卷中移除');
  }).catch(() => {});
};

// 处理表格选择变化
const handleSelectionChange = (selection: QuestionItem[]) => {
  selectedQuestionIds.value = selection.map(item => item.id);
};

// 批量添加题目
const handleBatchAdd = () => {
  if (selectedQuestionIds.value.length === 0) {
    ElMessage.warning('请先选择要添加的题目');
    return;
  }

  // 筛选出未添加的题目
  const newQuestions = questions.value.filter(q => 
    selectedQuestionIds.value.includes(q.id) && !isQuestionSelected(q.id)
  );

  if (newQuestions.length === 0) {
    ElMessage.warning('所选题目已全部添加到试卷中');
    return;
  }

  // 添加新题目
  selectedQuestions.value = [...selectedQuestions.value, ...newQuestions];
  ElMessage.success(`成功添加 ${newQuestions.length} 道题目到试卷`);
  
  // 切换到已选题目标签
  activeTab.value = 'selected';
};

// 保存试卷
const saveExam = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请至少添加一道题目到试卷中');
    return;
  }

  try {
    saving.value = true;
    const result = await addQuestionsToExam(examId.value, {
      questionIds: selectedQuestions.value.map(q => q.id)
    });
    
    if (result.code === 200) {
      ElMessage.success('试卷保存成功');
      goBack();
    } else {
      ElMessage.error(result.message || '保存试卷失败');
    }
  } catch (error) {
    console.error('保存试卷出错:', error);
    ElMessage.error('保存试卷失败');
  } finally {
    saving.value = false;
  }
};

// 返回上一页
const goBack = () => {
  // router.back();
  // 直接导航到试卷预览页面，确保返回到正确的路由
  router.push({
    path: '/examview',
    query: {
      examId: examId.value,
      examTitle: examTitle.value
    }
  });
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  loadQuestions();
};

// 处理页码变化
const handleCurrentChange = () => {
  loadQuestions();
};

// 处理每页数量变化
const handleSizeChange = () => {
  currentPage.value = 1;
  loadQuestions();
};

onMounted(() => {
  loadExamData();
  loadQuestions();
});
</script>

<style lang="scss">
.exam-edit-container {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg-color);
  padding: 20px;
  box-sizing: border-box;
  color: var(--text-color);
  transition: background-color 0.3s;

  .exam-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    background-color: var(--card-bg);
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: var(--shadow);
    border: 1px solid var(--border-color);

    .exam-title {
      font-size: 18px;
      margin: 0;
      color: var(--text-color);
    }

    .back-button {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }

  .exam-content {
    background-color: var(--card-bg);
    border-radius: 8px;
    padding: 20px;
    box-shadow: var(--shadow);
    border: 1px solid var(--border-color);

    .empty-questions {
      padding: 40px 0;
      
      .empty-icon {
        font-size: 60px;
        color: var(--text-color-secondary);
      }
    }

    .question-list {
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .question-card {
      margin-bottom: 16px;
      border: 1px solid var(--border-color);
      border-radius: 8px;
      background-color: var(--card-bg);
      box-shadow: var(--shadow);

      .question-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid var(--border-color);

        .question-number {
          font-weight: bold;
          color: var(--primary-color);
        }
      }

      .question-title {
        font-weight: 500;
        margin-bottom: 12px;
        color: var(--text-color);
      }

      .question-options {
        padding: 8px 0;

        .option {
          display: flex;
          align-items: flex-start;
          margin-bottom: 8px;
          padding: 8px 12px;
          border-radius: 4px;
          background-color: var(--bg-color);

          &:hover {
            background-color: var(--hover-bg);
          }

          .option-text {
            color: var(--text-color);

            &.is-correct {
              color: var(--success-color);
              font-weight: 500;
            }
          }
        }
      }
    }

    .search-bar {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
    }

    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .table-scrollbar, .custom-scrollbar {
    --el-scrollbar-bar-size: 6px;
    --el-scrollbar-color: var(--primary-color);
  }
}
</style> 