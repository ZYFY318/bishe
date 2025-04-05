<template>
  <el-dialog
    title="添加新试卷"
    v-model="dialogVisible"
    width="500px"
    :before-close="handleClose"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    class="exam-upload-dialog"
  >
    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="试卷名称" prop="title">
        <el-input v-model="form.title" placeholder="请输入试卷名称"></el-input>
      </el-form-item>
      
      <el-form-item label="考试时长" prop="duration">
        <el-input-number 
          v-model="form.duration" 
          :min="5" 
          :max="180" 
          :step="5"
          controls-position="right"
        />
        <span class="duration-unit">分钟</span>
      </el-form-item>
      
      <el-form-item label="试卷封面" prop="cover">
        <el-upload
          class="cover-uploader"
          action="#"
          :auto-upload="false"
          :on-change="handleCoverChange"
          :limit="1"
          accept="image/jpeg,image/png,image/gif"
        >
          <div v-if="coverUrl" class="cover-preview">
            <img :src="coverUrl" class="cover-img">
            <div class="cover-actions">
              <el-icon @click.stop="removeCover"><Delete /></el-icon>
            </div>
          </div>
          <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="el-upload__tip">请上传试卷封面图片（JPEG/PNG/GIF）</div>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          创建
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { Delete, Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { createExam } from '@/api/product/exam'; // 引入创建试卷API
import type { ExamCreateData } from '@/api/product/exam/type'; // 引入试卷创建参数类型
import useUserStore from '@/stores/modules/user';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'uploaded']);

// 对话框可见性
const dialogVisible = ref(props.visible);

// 监听visible变化
watch(() => props.visible, (newVal: boolean) => {
  dialogVisible.value = newVal;
});

// 监听dialogVisible变化
watch(() => dialogVisible.value, (newVal: boolean) => {
  emit('update:visible', newVal);
});

// 表单引用
const formRef = ref<FormInstance>();

// 封面预览URL
const coverUrl = ref('');

// 提交状态
const submitting = ref(false);

// 表单数据
const form = reactive({
  title: '',
  duration: 60,
  cover: null as File | null
});

// 表单验证规则
const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入试卷名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请设置考试时长', trigger: 'change' },
    { type: 'number', min: 5, max: 180, message: '时长在 5 到 180 分钟之间', trigger: 'change' }
  ],
  cover: [
    { required: true, message: '请上传试卷封面', trigger: 'change' }
  ]
});

// 封面变更处理
const handleCoverChange = (file: any) => {
  if (!file) return;
  
  // 检查文件类型
  const isImage = file.raw.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('请上传图片文件!');
    return;
  }
  
  // 检查文件大小（限制为5MB）
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
    return;
  }
  
  form.cover = file.raw;
  coverUrl.value = URL.createObjectURL(file.raw);
};

// 移除封面
const removeCover = () => {
  form.cover = null;
  if (coverUrl.value) {
    URL.revokeObjectURL(coverUrl.value);
    coverUrl.value = '';
  }
};

// 提交处理
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      
      try {
        // 导入用户仓库以获取用户ID
        const userStore = useUserStore();
        
        // 创建试卷数据对象，确保类型正确
        const examData: ExamCreateData = {
          title: form.title,
          duration: form.duration,
          creatorId: userStore.userId // 添加创建者ID
        };
        
        // 只有当 cover 存在时才添加到对象中
        if (form.cover) {
          examData.cover = form.cover;
        }
        
        // 调用API创建试卷
        const res = await createExam(examData);
        
        if (res.code === 200) {
          ElMessage.success(res.message || '试卷创建成功');
          emit('uploaded');
          handleClose();
        } else {
          ElMessage.error(res.message || '创建失败');
        }
      } catch (error) {
        console.error('创建试卷失败:', error);
        ElMessage.error('创建失败，请重试');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false;
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields();
  }
  form.cover = null;
  if (coverUrl.value) {
    URL.revokeObjectURL(coverUrl.value);
    coverUrl.value = '';
  }
};
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.duration-unit {
  margin-left: 10px;
  color: #909399;
}

.cover-uploader {
  text-align: center;
  
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
  }
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cover-preview {
  position: relative;
  width: 178px;
  height: 178px;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-actions {
  position: absolute;
  top: 0;
  right: 0;
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  border-bottom-left-radius: 6px;
  opacity: 0;
  transition: opacity 0.3s;
  
  .cover-preview:hover & {
    opacity: 1;
  }
  
  .el-icon {
    color: #fff;
    font-size: 18px;
    cursor: pointer;
    
    &:hover {
      color: #f56c6c;
    }
  }
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}



</style> 