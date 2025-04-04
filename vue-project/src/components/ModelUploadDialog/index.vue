<template>
  <el-dialog
    title="上传新模型"
    v-model="dialogVisible"
    width="500px"
    :before-close="handleClose"
    append-to-body
    destroy-on-close
    class="model-upload-dialog"
  >
    <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
      <el-form-item label="模型名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入模型名称"></el-input>
      </el-form-item>
      
      <el-form-item label="模型描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入模型描述"
          :rows="3"
        ></el-input>
      </el-form-item>
      
      <el-form-item label="模型文件" prop="file">
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".glb"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              请上传 .glb 格式的3D模型文件
            </div>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading" style="width: 3rem;">
          上传
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { uploadModel } from '@/api/model';
import type { ModelUploadData } from '@/api/model/type';

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

// 表单数据
const form = reactive<{
  name: string;
  description: string;
  file: File | null;
}>({
  name: '',
  description: '',
  file: null
});

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入模型名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入模型描述', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请上传模型文件', trigger: 'change' }
  ]
});

// 上传状态
const uploading = ref(false);

// 文件变更处理
const handleFileChange = (file: any) => {
  if (file && file.raw) {
    // 检查文件类型
    if (file.raw.type !== 'model/gltf-binary' && !file.raw.name.endsWith('.glb')) {
      ElMessage.error('请上传GLB格式的模型文件!');
      return false;
    }
    
    // 检查文件大小（限制为50MB）
    const isLt50M = file.raw.size / 1024 / 1024 < 50;
    if (!isLt50M) {
      ElMessage.error('模型文件大小不能超过 50MB!');
      return false;
    }
    
    form.file = file.raw;
  }
};

// 上传处理
const handleUpload = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid && form.file) {
      uploading.value = true;
      try {
        const uploadData: ModelUploadData = {
          name: form.name,
          description: form.description,
          file: form.file
        };
        
        const res = await uploadModel(uploadData);
        if (res.code === 200) {
          ElMessage.success('模型上传成功');
          emit('uploaded');
          handleClose();
        } else {
          ElMessage.error(res.data.message || '上传失败');
        }
      } catch (error) {
        console.error('上传模型失败:', error);
        ElMessage.error('上传失败，请重试');
      } finally {
        uploading.value = false;
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
  form.file = null;
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.upload-demo {
  width: 100%;
}


</style> 