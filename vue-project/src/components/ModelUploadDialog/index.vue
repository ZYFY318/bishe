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
      
      <el-form-item label="封面图片" prop="image">
        <el-upload
          class="image-uploader"
          action="#"
          :auto-upload="false"
          :on-change="handleImageChange"
          :limit="1"
          accept="image/jpeg,image/png,image/jpg"
        >
          <img v-if="imageUrl" :src="imageUrl" class="preview-image" />
          <el-icon v-else class="image-uploader-icon"><plus /></el-icon>
          <template #tip>
            <div class="el-upload__tip">
              请上传 JPG/PNG 格式的图片作为模型封面
            </div>
          </template>
        </el-upload>
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
import useUserStore from '@/stores/modules/user';
import { Plus, UploadFilled } from '@element-plus/icons-vue';

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

// 图片预览URL
const imageUrl = ref('');

// 表单数据
const form = reactive<{
  name: string;
  description: string;
  file: File | null;
  cover: File | null;
}>({
  name: '',
  description: '',
  file: null,
  cover: null
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
  ],
  image: [
    { required: false, message: '请上传封面图片', trigger: 'change' }
  ]
});

// 上传状态
const uploading = ref(false);

// 图片变更处理
const handleImageChange = (file: any) => {
  if (file && file.raw) {
    // 检查文件类型
    const isImage = file.raw.type === 'image/jpeg' || 
                   file.raw.type === 'image/png' || 
                   file.raw.type === 'image/jpg';
    
    if (!isImage) {
      ElMessage.error('请上传JPG/PNG格式的图片!');
      return false;
    }
    
    // 检查文件大小（限制为5MB）
    const isLt5M = file.raw.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      ElMessage.error('图片大小不能超过 5MB!');
      return false;
    }
    
    // 保存文件引用
    form.cover = file.raw;
    
    // 创建预览URL
    imageUrl.value = URL.createObjectURL(file.raw);
  }
};

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
    
    // 显示文件大小信息
    const fileSizeMB = (file.raw.size / 1024 / 1024).toFixed(2);
    ElMessage.info(`已选择文件: ${file.raw.name} (${fileSizeMB}MB)`);
    
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
        // 获取用户信息
        const userStore = useUserStore();
        console.log("当前用户ID:", userStore.userId);
        
        // 创建FormData对象来处理文件上传
        const formData = new FormData();
        formData.append('file', form.file);
        formData.append('name', form.name);
        formData.append('description', form.description);
        
        // 添加封面图片（如果有）
        if (form.cover) {
          formData.append('cover', form.cover);
        }
        
        // 确保userId是数字并正确传递
        if (userStore.userId) {
          formData.append('creatorId', userStore.userId.toString());
          console.log("添加creatorId到FormData:", userStore.userId);
        }
        
        // 调用上传API
        const res = await uploadModel(formData);
        
        if (res.code === 200) {
          console.log("上传成功，返回数据:", res.data);
          ElMessage.success('模型上传成功');
          emit('uploaded');
          handleClose();
        } else {
          ElMessage.error('上传失败');
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
  form.image = null;
  imageUrl.value = '';
};
</script>

<style scoped lang="scss">
.model-upload-dialog {
  .image-uploader {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  .image-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .preview-image {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
  }
}
</style> 