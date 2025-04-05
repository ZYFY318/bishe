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

<style scoped lang="scss">
.exam-edit-container {
  width: 90%;
  margin: 20px auto;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 160px);
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.exam-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background-color: rgba(255, 255, 255, 0.05);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: white;
}

.exam-title {
  margin: 0 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: white;
  flex: 1;
}

.exam-content {
  flex: 1;
  padding: 20px;
}

.empty-questions {
  text-align: center;
  padding: 40px 0;
}

.empty-icon {
  font-size: 100px;
  color: rgba(255, 255, 255, 0.5);
}

.question-list {
  margin-top: 20px;

}

.question-card {
  margin-bottom: 20px;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-number {
  font-weight: bold;
  color: white;
}

.question-title {
  font-size: 16px;
  margin-bottom: 15px;
  color: white;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  color: white;
}

.option-label {
  min-width: 24px;
}

.option-text {
  flex: 1;
}

.is-correct {
  color: #67c23a;
  font-weight: bold;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: end;
}

:deep(.el-empty__description) {
  color: white;
  font-size: 18px;
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-tabs__item.is-active) {
  color: white;
}

:deep(.el-tabs__active-bar) {
  background-color: white;
}

:deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(255, 255, 255, 0.2);
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-table) {
  background-color: transparent;
}

:deep(.el-table th.el-table__cell) {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-table tr) {
  background-color: rgba(255, 255, 255, 0.05);
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td.el-table__cell) {
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-table .cell) {
  color: white;
}

:deep(.el-pagination) {
  --el-pagination-text-color: white;
  --el-pagination-button-color: white;
  --el-pagination-hover-color: white;
}

/* el-scrollbar样式自定义 */
.custom-scrollbar {
  :deep(.el-scrollbar__bar) {
    opacity: 0.3;
    
    &.is-horizontal {
      height: 8px;
    }
    
    &.is-vertical {
      width: 8px;
    }
    
    &:hover {
      opacity: 0.8;
    }
  }
  
  :deep(.el-scrollbar__thumb) {
    background-color: rgba(255, 255, 255, 0.5);
    border-radius: 4px;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.7);
    }
  }
}

.options-scrollbar {
  :deep(.el-scrollbar__bar) {
    opacity: 0.2;
    
    &.is-horizontal {
      height: 6px;
    }
    
    &.is-vertical {
      width: 6px;
    }
    
    &:hover {
      opacity: 0.6;
    }
  }
  
  :deep(.el-scrollbar__thumb) {
    background-color: rgba(255, 255, 255, 0.3);
    border-radius: 3px;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.5);
    }
  }
}

.table-scrollbar {
  :deep(.el-scrollbar__bar) {
    opacity: 0.3;
    
    &.is-horizontal {
      height: 8px;
    }
    
    &.is-vertical {
      width: 8px;
    }
    
    &:hover {
      opacity: 0.8;
    }
  }
  
  :deep(.el-scrollbar__thumb) {
    background-color: rgba(255, 255, 255, 0.5);
    border-radius: 4px;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.7);
    }
  }
}
</style> 