<template>
  <el-card style="max-width: 100%">
    <!-- 顶部添加题目按钮 -->
    <!-- 添加题目按钮 -->
    <el-button type="primary" size="default" icon="Plus" @click="openAddDialog" style="padding: 5px;">
      添加题目
    </el-button>

    <!-- 题目添加对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加题目" width="500px">
      <el-form :model="addForm">
        <el-form-item label="题目名称" :rules="[{ required: true, message: '请输入题目名称', trigger: 'blur' }]">
          <el-input v-model="addForm.title" placeholder="请输入题目名称" />
        </el-form-item>

        <el-form-item label="题目选项" :rules="[{ required: true, message: '请输入选项', trigger: 'blur' }]">
          <el-input v-for="(option, index) in addForm.options" :key="index" v-model="addForm.options[index]"
            placeholder="请输入选项" />
        </el-form-item>

        <el-form-item label="正确答案" :rules="[{ required: true, message: '请选择正确答案', trigger: 'blur' }]">
          <el-select v-model="addForm.answer" placeholder="请选择正确答案">
            <el-option v-for="(option, index) in addForm.options" :key="index" :label="option" :value="option" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 对话框底部 -->
      <template #footer>
        <el-button @click="addDialogVisible = false" style="padding: 5px;">取消</el-button>
        <el-button type="primary" @click="saveQuestion" style="padding: 5px;">保存</el-button>
      </template>
    </el-dialog>
    <!-- 题目表格 -->
    <el-table style="margin: 10px 0px" :data="questionArr">
      <el-table-column prop="id" label="序号" width="60" align="center" type="index" />
      <el-table-column prop="title" label="题目名称" width="200" />
      <el-table-column prop="options" label="题目选项" width="250">
        <template #default="{ row }">
          <ul>
            <li v-for="(option, index) in row.options" :key="index">
              {{ option }}
            </li>
          </ul>
        </template>
      </el-table-column>
      <el-table-column prop="answer" label="题目答案" width="120" align="center" />

      <!-- 操作列 -->
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row, $index }">
          <el-button type="primary" size="small" icon="Edit" @click="editQuestion(row)"
            style="font-size: 16px; padding: 5px 8px;">
          </el-button>
          <el-button type="danger" size="small" icon="Delete" @click="deleteQuestion(row.id)"
            style="font-size: 16px; padding: 5px 8px;">
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <el-pagination v-model:current-page="pageNo" v-model:page-size="limit" :total="total" :page-sizes="[5, 10, 15]"
      layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
      @current-change="handleCurrentChange" style="margin-top: 15px; text-align: right" />

    <!-- 编辑题目对话框 -->
    <!-- <el-dialog v-model="editDialogVisible" title="编辑题目" width="500px">
      <el-form :model="editForm">
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
    </el-dialog> -->
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { reqQuestion, addQuestion } from "@/api/product/trademark";

// 题目列表数据


let pageNo = ref<number>(1);
let limit = ref<number>(3);
let total = ref<number>(0);
let questionArr = ref<any>([])
const addDialogVisible = ref(false);
const getQuestion = async () => {
  let result = await reqQuestion(pageNo.value, limit.value);
  console.log(result);
  if (result.code === 200) {
    total.value = result.data.totalElements;
    questionArr.value = result.data.content;
  }
}

onMounted(() => {
  getQuestion();
})
// // 处理分页变化
const handleSizeChange = (newSize: number) => {
  limit.value = newSize;
  pageNo.value = 1; // 改变页大小后，重置到第一页
  getQuestion()
};
const handleCurrentChange = (newPage: number) => {
  getQuestion()
};

let addForm = ref({
  title: "",
  options: ["", "", "", ""],  // 4个选项
  answer: ""
});

// 打开添加题目的对话框
const openAddDialog = () => {
  addDialogVisible.value = true;
  addForm.value = { title: "", options: ["", "", "", ""], answer: "" };  // 重置表单
};

// 保存添加的题目
const saveQuestion = () => {
  console.log("保存题目:", addForm.value);
  addQuestion(addForm.value)
  addDialogVisible.value = false;
  getQuestion();
};

</script>

<style scoped lang="scss">
.el-card {
  padding: 20px;
  overflow: visible;
  // display: none;
  // opacity: 0;
  // visibility: hidden;
  // position: absolute;
}
</style>
