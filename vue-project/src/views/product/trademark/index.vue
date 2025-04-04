<template>
  <el-card class="question-card">
    <!-- 顶部添加题目按钮 -->
    <!-- 添加题目按钮 -->
    <div class="button-container">
      <el-button type="primary" size="default" icon="Plus" @click="openAddDialog" style="padding: 5px;">
        添加题目
      </el-button>
    </div>
    <!-- 题目添加对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加题目" width="500px">
      <el-form ref="addFormRef" :model="addForm" :rules="questionRules">
        <el-form-item label="题目名称" prop="title">
          <el-input v-model="addForm.title" placeholder="请输入题目名称" />
        </el-form-item>

        <el-form-item label="题目选项" prop="options">
          <div v-for="(option, index) in addForm.options" :key="index" style="margin-bottom: 8px;">
            <el-input v-model="addForm.options[index]" :placeholder="`选项 ${index + 1}`"
              @blur="() => addFormRef.validateField('options')" style="padding: 5px;" />
          </div>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-select v-model="addForm.answer" placeholder="请选择正确答案">
            <el-option v-for="(option, index) in addForm.options" :key="index" :label="option || `选项 ${index + 1}`"
              :value="option" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
    <!-- 题目表格 -->
    <div class="table-container">
      <el-table :data="questionArr">
        <el-table-column type="index" label="序号" align="center" width="60" />
        <el-table-column prop="title" label="题目名称" />
        <el-table-column prop="options" label="题目选项">
          <template #default="{ row }">
            <ul>
              <li v-for="(option, index) in row.options" :key="index">
                {{ option }}
              </li>
            </ul>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="题目答案" align="center" />
        <el-table-column label="操作" align="center">
          <template #default="{ row, $index }">
            <el-button type="primary" size="small" icon="Edit" @click="openEditDialog(row)"
              style="font-size: 16px; padding: 5px 8px;">
            </el-button>
            <el-button type="danger" size="small" icon="Delete" @click="handleDeleteQuestion(row.id)"
              style="font-size: 16px; padding: 5px 8px;">
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页器 -->
    <el-pagination v-model:current-page="pageNo" v-model:page-size="limit" :total="total" :page-sizes="[5, 10, 15]"
      layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
      @current-change="handleCurrentChange" style="margin-top: 15px; text-align: right" />

    <!-- 编辑题目对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑题目">
      <el-form ref="editFormRef" :model="editForm">
        <el-form-item label="题目名称">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="题目选项">
          <el-input v-for="(option, index) in editForm.options" :key="index" v-model="editForm.options[index]" />
        </el-form-item>
        <el-form-item label="正确答案">
          <el-select v-model="editForm.answer" placeholder="请选择正确答案">
            <el-option v-for="(option, index) in editForm.options" :key="index" :label="option" :value="option" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

  </el-card>
</template>
<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useQuestion } from '@/views/product/trademark/hook/useQuestion'
import { questionRules } from '@/views/product/trademark/utils/questionValidators'
import { addQuestion, deleteQuestion, reqAllQuestion, reqQuestion } from '@/api/product/trademark'
import { useAddQuestionForm } from './hook/useAddQuestionForm'
import { useEditQuestion } from './hook/useEditQuetion'

const {
  pageNo,
  limit,
  total,
  questionArr,
  getQuestion,
  handleSizeChange,
  handleCurrentChange
} = useQuestion()

const {
  dialogVisible: addDialogVisible,
  formRef: addFormRef,
  formData: addForm,
  open: openAddDialog,
  submit: saveQuestion
} = useAddQuestionForm(getQuestion)


const {
  dialogVisible: editDialogVisible,
  formRef: editFormRef,
  formData: editForm,
  open: openEditDialog,
  submit: saveEdit
} = useEditQuestion(getQuestion);

// 修复TypeScript类型错误
interface Question {
  id: number;
  title: string;
  options: string[];
  answer: string;
}

const handleDeleteQuestion = async (id: number) => {
  try {
    // 确认框
    await ElMessageBox.confirm("确定要删除该题目吗？", "警告", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });

    // 调用后端删除 API
    console.log("here", questionArr.value.length, id);
    console.log(questionArr.value);
    await deleteQuestion(id);

    getQuestion();
    questionArr.value = questionArr.value.filter((q: Question) => q.id !== id);
    console.log("after", questionArr.value.length);
    console.log(questionArr.value);
    
    // 成功后刷新数据
    ElMessage.success("删除成功");

  } catch (error) {
    console.error("删除失败:", error);
    ElMessage.error("删除失败");
  }
};


// 打开对话框时加载初始数据
onMounted(() => {
  getQuestion();

})




</script>

<style scoped lang="scss">
.question-card {
  width: 90%; /* 固定宽度 */
  margin: 10px auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px); /* 调整为视口高度减去一定的空间 */
  background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 10px ;
    backdrop-filter: blur(10px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.button-container {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
  flex-shrink: 0; /* 防止按钮区域压缩 */
}

.table-container {
  flex: 1; /* 使表格容器占据剩余空间 */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止整体溢出 */
  margin-bottom: 15px;
  min-height: 200px; /* 确保表格区域有最小高度 */
  max-height: calc(100vh - 240px); /* 留出足够空间给分页器 */
  
}

.el-table {
  width: 100%;
  flex: 1; /* 表格占满容器 */
  overflow: auto; /* 内容溢出时显示滚动条 */
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto !important; /* 强制显示垂直滚动条 */
  max-height: calc(100vh - 350px); /* 减小最大高度，为分页器留出空间 */
}

.el-pagination {
  margin-top: 15px;
  padding: 10px 0;
  flex-shrink: 0; /* 防止分页器被压缩 */
  position: relative; /* 确保分页器位于正确的层叠顺序 */
  z-index: 10; /* 提高层叠顺序，防止被其他元素遮挡 */
  background-color: transparent !important;
}

/* 分页器按钮和输入框样式 */
:deep(.el-pagination button), 
:deep(.el-pagination .el-input__inner),
:deep(.el-pagination .el-select .el-input .el-input__inner) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #333 !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
}

/* 分页器按钮悬停效果 */
:deep(.el-pagination button:hover),
:deep(.el-pagination .el-input__inner:hover) {
  background-color: rgba(255, 255, 255, 0.3) !important;
}

/* 当前页码的样式 */
:deep(.el-pagination .el-pager li.is-active) {
  background-color: rgba(255, 255, 255, 0.3) !important;
  color: #333 !important;
  font-weight: bold;
}

/* 普通页码的样式 */
:deep(.el-pagination .el-pager li) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #333 !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
}

/* 页码悬停效果 */
:deep(.el-pagination .el-pager li:hover:not(.is-active)) {
  background-color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-table) {
  background-color: transparent !important;
  color: white !important;  /* 输入文字颜色 */
  font-size: 1.2rem !important;  /* 输入文字大小 */
}

:deep(.el-table tr), 
:deep(.el-table th), 
:deep(.el-table td) {
  background-color: transparent !important;    color: white !important;  /* 输入文字颜色 */
  font-size: 1.2rem !important;  /* 输入文字大小 */
}

:deep(.el-table--border), 
:deep(.el-table--group) {
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-table td), 
:deep(.el-table th.is-leaf) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-table--border .el-table__cell) {
  border-right: 1px solid rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-table__header-wrapper), 
:deep(.el-table__footer-wrapper) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-table__header-wrapper th) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  font-weight: bold;
}

:deep(.el-table__row:hover > td) {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

/* 分页器文本样式 - 包括"共xx条"、"前往"等文字 */
:deep(.el-pagination .el-pagination__total),
:deep(.el-pagination .el-pagination__jump),
:deep(.el-pagination .el-pagination__sizes),
:deep(.el-pagination span:not([class])),
:deep(.el-pagination button span) {
  color: rgb(126, 119, 119) !important;
  font-size: 1.2rem !important;
}


</style>


