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

    // const index = questionArr.value.findIndex(q => q.id === id);
    // if (index !== -1) {
    //   questionArr.value.splice(index, 1); // 触发响应式更新
    // }
    getQuestion();
    questionArr.value = questionArr.value.filter(q => q.id !== id);
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
  // height: 600px; /* 固定高度 */
  margin: 10px auto;
  padding: 20px;
  overflow: hidden; /* 隐藏溢出内容 */
}

.table-container {
  height: calc(100% - 60px); /* 减去按钮和分页器的高度 */
  overflow: auto; /* 显示滚动条以处理溢出内容 */
}

.button-container {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 10px;
}

.el-table {
  width: 100%;
}

.el-table-column {
  /* 可以根据需要调整每列的宽度 */
  //  width: 25%; 
}
</style>


