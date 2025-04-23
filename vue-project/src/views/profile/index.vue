<template>
  <div class="profile-container">
    <!-- 顶部导航栏 -->
    <div class="profile-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="profile-title">个人中心</h1>
      </div>
    </div>
    
    <!-- 个人信息板块 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" plain size="small" @click="handleEdit">编辑</el-button>
        </div>
      </template>
      <div class="profile-info">
        <div class="user-avatar">
          <el-avatar :size="100" :src="getImgUrl(userStore.avatar)" />
        </div>
        <div class="user-details">
          <div class="detail-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userStore.username }}</span>
          </div>
          <div class="detail-item">
            <span class="label">用户ID：</span>
            <span class="value">{{ userStore.userId }}</span>
          </div>
          <div class="detail-item">
            <span class="label">用户类型：</span>
            <span class="value">{{ userTypeText }}</span>
          </div>
          <div class="detail-item">
            <span class="label">邮箱：</span>
            <span class="value">{{ userStore.email || '未设置' }}</span>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 我的模型板块 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>我的模型</span>
        </div>
      </template>
      <el-scrollbar class="horizontal-scrollbar">
        <div class="models-container">
          <template v-if="isLoadingModels">
            <div v-for="i in 3" :key="i" class="model-item">
              <el-skeleton :rows="3" animated />
            </div>
          </template>
          <template v-else-if="userModels.length > 0">
            <div v-for="model in userModels" :key="model.id" class="model-item">
              <div class="model-content" @click="goToModelDetails(model)">
                <img 
                  :src="getImgUrl(model.coverUrl || '')" 
                  class="model-image" 
                  alt="模型图片"
                />
                <div class="model-info">
                  <div class="model-name">{{ model.name }}</div>
                  <div class="model-description">{{ model.description }}</div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="empty-message">暂无模型</div>
          </template>
        </div>
      </el-scrollbar>
    </el-card>
    
    <!-- 我的作业/发布的考试板块，根据用户类型显示不同标题 -->
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>{{ isTeacher ? '发布的考试' : '我的作业' }}</span>
        </div>
      </template>
      <el-scrollbar class="horizontal-scrollbar">
        <div class="assignments-container">
          <template v-if="isLoadingExams">
            <div v-for="i in 3" :key="i" class="assignment-item">
              <el-skeleton :rows="2" animated />
            </div>
          </template>
          <template v-else-if="publishedExams.length > 0">
            <div 
              v-for="exam in publishedExams" 
              :key="exam.id" 
              class="assignment-item exam-card"
              @click="goToExamDetail(exam.id)"
            >
              <div class="card-content">
                <div class="card-image">
                  <img :src="getImgUrl(exam.coverUrl)" alt="试卷封面" />
                </div>
                <div class="card-info">
                  <h3 class="card-title">{{ exam.title }}</h3>
                  <div class="card-details">
                    <p><el-icon><Timer /></el-icon> {{ exam.duration }} 分钟</p>
                    <p><el-icon><Calendar /></el-icon> {{ new Date(exam.createdAt).toLocaleDateString() }}</p>
                  </div>
                  <div class="card-creator" v-if="exam.creatorName">
                    <p><el-icon><User /></el-icon> {{ exam.creatorName }}</p>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="empty-message">{{ isTeacher ? '暂无发布的考试' : '暂无考试记录' }}</div>
          </template>
        </div>
      </el-scrollbar>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑个人信息"
      width="500px"
    >
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="头像">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="avatarPreview || getImgUrl(userStore.avatar)" class="avatar" />
            <div class="avatar-upload-btn">
              <el-button type="primary" size="small" @click="triggerUpload">
                <el-icon><Upload /></el-icon>
                上传头像
              </el-button>
              <input 
                type="file" 
                ref="uploadInput" 
                style="display: none;" 
                accept="image/jpeg,image/png"
                @change="handleFileChange"
              >
            </div>
          </div>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="isSubmitting" @click="submitEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 历史考试情况 - 仅对学生显示 -->
    <el-card v-if="!isTeacher" class="profile-card">
      <template #header>
        <div class="card-header">
          <span>历史考试情况</span>
        </div>
      </template>
      <div class="chart-container">
        <div ref="examChartRef" class="exam-chart"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Upload, Timer, Calendar, User } from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import useUserStore from '@/stores/modules/user';
import { ref, computed, onMounted, reactive, onBeforeUnmount } from 'vue';
import { reqUserInfo, updateUserInfo } from '@/api/user';
import { reqUserModels } from '@/api/model';
import { reqUserExamResults } from '@/api/examResult';
import { fetchPublishedExams } from '@/api/product/exam';
import type { updateUserForm } from '@/api/user/type';
import type { ModelListResponse } from '@/api/model/type';
import type { ExamResult } from '@/api/examResult/type';
import type { ExamItem } from '@/api/product/exam/type';
import * as echarts from 'echarts';

const router = useRouter();
const userStore = useUserStore();
const defaultAvatar = '/default-avatar.png'; // 默认头像路径

// 判断是否是教师用户
const isTeacher = computed(() => userStore.userType === 'TEACHER');

// 编辑表单
const dialogVisible = ref(false);
const isSubmitting = ref(false);
const uploadInput = ref<HTMLInputElement | null>(null);
const editForm = ref({
  username: '',
  email: '',
});

// 头像文件和预览
const avatarFile = ref<File | null>(null);
const avatarPreview = ref<string>('');

// 用户类型文本
const userTypeText = computed(() => {
  const typeMap: Record<string, string> = {
    'admin': '管理员',
    'teacher': '教师',
    'student': '学生'
  };
  return typeMap[userStore.userType] || userStore.userType;
});

// 用户模型列表
const userModels = ref<ModelListResponse['data']>([]);
const isLoadingModels = ref(false);

// 用户考试结果（用于图表）
const userExamResults = ref<ExamResult[]>([]);

// 发布的试卷列表（用于我的作业板块）
const publishedExams = ref<ExamItem[]>([]);
const isLoadingExams = ref(false);

// 折线图的DOM引用
const examChartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 图表数据，将从API更新
const examChartData = reactive({
  dates: [] as string[],
  scores: [] as number[],
  questionCounts: [] as number[]
});

// 处理考试数据以用于图表显示
const processExamData = (examResults: ExamResult[]) => {
  // 按日期排序
  const sortedResults = [...examResults].sort((a, b) => 
    new Date(a.examDate).getTime() - new Date(b.examDate).getTime()
  );
  
  // 提取日期、分数和题目数量
  const dates = sortedResults.map(result => new Date(result.examDate).toLocaleDateString());
  const scores = sortedResults.map(result => result.score);
  const questionCounts = sortedResults.map(result => result.questionCount);
  
  return {
    dates,
    scores,
    questionCounts
  };
};

// 获取用户考试结果，用于图表显示 - 仅针对学生用户
const fetchUserExamResults = async () => {
  if (!userStore.userId || isTeacher.value) return;
  
  try {
    const response = await reqUserExamResults(userStore.userId);
    // TypeScript解析有问题，使用any类型临时解决
    const data = response as any;
    if (data.code === 200 && Array.isArray(data.data)) {
      userExamResults.value = data.data.map((exam: any, index: number) => ({
        ...exam,
        id: exam.id || index
      }));
      
      // 更新图表数据
      updateChartData(userExamResults.value);
    } else {
      ElMessage.error('获取考试记录失败');
    }
  } catch (error) {
    console.error('获取考试记录失败:', error);
    ElMessage.error('获取考试记录失败');
  }
};

// 获取已发布的试卷列表，用于我的作业板块
const fetchPublishedExamsList = async () => {
  isLoadingExams.value = true;
  try {
    const response = await fetchPublishedExams();
    if (response.code === 200) {
      publishedExams.value = response.data;
    } else {
      ElMessage.error('获取试卷列表失败');
    }
  } catch (error) {
    console.error('获取试卷列表失败:', error);
    ElMessage.error('获取试卷列表失败');
  } finally {
    isLoadingExams.value = false;
  }
};

// 同时获取两种数据 - 基于用户类型选择性获取
const fetchAllData = async () => {
  const fetchPromises = [fetchPublishedExamsList()];
  
  // 只有学生才需要获取考试结果
  if (!isTeacher.value) {
    fetchPromises.push(fetchUserExamResults());
  }
  
  await Promise.all(fetchPromises);
};

// 更新图表数据
const updateChartData = (examResults: ExamResult[]) => {
  // 如果没有考试记录，使用空数据
  if (!examResults || examResults.length === 0) {
    examChartData.dates = [];
    examChartData.scores = [];
    examChartData.questionCounts = [];
    return;
  }
  
  // 处理考试数据
  const processedData = processExamData(examResults);
  examChartData.dates = processedData.dates;
  examChartData.scores = processedData.scores;
  examChartData.questionCounts = processedData.questionCounts;
  
  // 如果图表已初始化，则更新图表
  if (chartInstance) {
    initExamChart();
  }
};

// 获取完整的图片URL
const getImgUrl = (url: string) => {
  if (!url) return '/hakasei.png';
  
  // 如果是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  
  // 如果是相对路径，添加API基础URL
  const baseURL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:9090';
  
  // 确保路径正确
  if (url.startsWith('/')) {
    return `${baseURL}${url}`;
  } else {
    return `${baseURL}/${url}`;
  }
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 获取用户信息
const fetchUserInfo = () => {
  // 直接使用Pinia仓库中的用户信息，不再发送请求
  editForm.value.username = userStore.username;
  editForm.value.email = userStore.email;
};

// 获取用户模型列表
const fetchUserModels = async () => {
  if (!userStore.userId) return;
  
  isLoadingModels.value = true;
  try {
    const response = await reqUserModels(userStore.userId);
    if (response.code === 200) {
      userModels.value = response.data;
    } else {
      ElMessage.error('获取模型列表失败');
    }
  } catch (error) {
    console.error('获取模型列表失败:', error);
    ElMessage.error('获取模型列表失败');
  } finally {
    isLoadingModels.value = false;
  }
};

// 导航函数定义，放在goBack函数下面
const goToModelDetails = (model: ModelListResponse['data'][0]) => {
  router.push({
    path: '/showModel',
    query: {
      modelId: model.id,
      modelName: model.name
    }
  });
};

// 跳转到考试详情
const goToExamDetail = (examId: number) => {
  // 教师跳转到考试分析页面，学生跳转到考试界面
  if (isTeacher.value) {
    router.push({
      path: `/exam-analysis/${examId}`,
      query: { 
        examId,
        examTitle: publishedExams.value.find(exam => exam.id === examId)?.title || '未命名试卷'
      }
    });
  } else {
    router.push({
      path: '/examview/take',
      query: { 
        examId,
        examTitle: publishedExams.value.find(exam => exam.id === examId)?.title || '未命名试卷'
      }
    });
  }
};

// 格式化考试用时
const formatDuration = (seconds: number): string => {
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

// 组件挂载时初始化用户信息和模型列表
onMounted(() => {
  // 检查pinia中是否已有数据
  if (userStore.username) {
    // 已有数据，直接使用
    fetchUserInfo();
    fetchUserModels();
    fetchAllData(); // 获取考试记录和已发布试卷
    
    // 仅对学生用户初始化图表
    if (!isTeacher.value) {
      // 初始化图表需要等DOM渲染完成
      setTimeout(() => {
        initExamChart();
      }, 100);
    }
  } else {
    // 没有数据，可能是直接访问页面或刷新，需要重新请求
    userStore.userInfo().then(() => {
      fetchUserInfo();
      fetchUserModels();
      fetchAllData(); // 获取考试记录和已发布试卷
      
      // 仅对学生用户初始化图表
      if (!isTeacher.value) {
        // 初始化图表需要等DOM渲染完成
        setTimeout(() => {
          initExamChart();
        }, 100);
      }
    }).catch(error => {
      console.error('获取用户信息失败:', error);
      ElMessage.error('获取用户信息失败');
    });
  }
});

onBeforeUnmount(() => {
  // 清理图表实例
  if (chartInstance) {
    chartInstance.dispose();
  }
});

// 初始化图表
const initExamChart = () => {
  // 如果是教师用户，不初始化图表
  if (isTeacher.value) return;
  
  if (examChartRef.value) {
    // 确保销毁旧实例
    if (chartInstance) {
      chartInstance.dispose();
    }
    
    // 创建新实例
    chartInstance = echarts.init(examChartRef.value);
    
    // 如果没有数据，显示无数据提示
    if (examChartData.dates.length === 0) {
      chartInstance.setOption({
        title: {
          text: '暂无考试记录',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#909399',
            fontSize: 16
          }
        }
      });
      return;
    }
    
    // 配置图表选项
    const option = {
      title: {
        text: '历史考试情况',
        left: 'center',
        textStyle: {
          color: '#606266'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      legend: {
        data: ['考试分数', '试卷题量'],
        top: 30,
        textStyle: {
          color: '#606266'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%', // 为datazoom留出空间
        containLabel: true
      },
      toolbox: {
        feature: {
          saveAsImage: {}
        }
      },
      dataZoom: [
        {
          type: 'slider',
          show: true,
          xAxisIndex: [0],
          start: 0,
          end: 100,
          bottom: 10
        },
        {
          type: 'inside', // 启用内部缩放
          xAxisIndex: [0],
          start: 0,
          end: 100
        }
      ],
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: examChartData.dates,
          axisLabel: {
            color: '#606266'
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '分数',
          position: 'left',
          min: 0,
          max: 100,
          interval: 20,
          axisLabel: {
            formatter: '{value}',
            color: '#606266'
          }
        },
        {
          type: 'value',
          name: '试卷题量',
          position: 'right',
          min: 0,
          max: Math.max(...examChartData.questionCounts, 30) + 10, // 动态设置最大值
          interval: 10,
          axisLabel: {
            formatter: '{value}',
            color: '#606266'
          }
        }
      ],
      series: [
        {
          name: '考试分数',
          type: 'line',
          smooth: true,
          emphasis: {
            focus: 'series'
          },
          yAxisIndex: 0,
          data: examChartData.scores,
          itemStyle: {
            color: '#409EFF'
          },
          lineStyle: {
            width: 3
          }
        },
        {
          name: '试卷题量',
          type: 'line',
          smooth: true,
          emphasis: {
            focus: 'series'
          },
          yAxisIndex: 1,
          data: examChartData.questionCounts,
          itemStyle: {
            color: '#67C23A'
          },
          lineStyle: {
            width: 3
          }
        }
      ]
    };
    
    // 应用配置
    if (chartInstance) {
      chartInstance.setOption(option);
      
      // 添加窗口大小变化监听，自适应大小
      window.addEventListener('resize', () => {
        if (chartInstance) {
          chartInstance.resize();
        }
      });
    }
  }
};

// 处理编辑按钮点击
const handleEdit = () => {
  editForm.value.username = userStore.username;
  avatarPreview.value = '';
  avatarFile.value = null;
  dialogVisible.value = true;
};

// 触发文件上传
const triggerUpload = () => {
  if (uploadInput.value) {
    uploadInput.value.click();
  }
};

// 处理文件选择变化
const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    const file = input.files[0];
    
    // 验证文件类型和大小
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
    const isLt2M = file.size / 1024 / 1024 < 2;
    
    if (!isJPG) {
      ElMessage.error('上传头像图片只能是 JPG/PNG 格式!');
      return;
    }
    
    if (!isLt2M) {
      ElMessage.error('上传头像图片大小不能超过 2MB!');
      return;
    }
    
    // 保存文件引用
    avatarFile.value = file;
    
    // 创建本地预览
    const reader = new FileReader();
    reader.onload = (e) => {
      if (e.target) {
        avatarPreview.value = e.target.result as string;
      }
    };
    reader.readAsDataURL(file);
  }
};

// 取消编辑
const cancelEdit = () => {
  dialogVisible.value = false;
  // 重置表单和头像状态
  avatarFile.value = null;
  avatarPreview.value = '';
};

// 提交编辑
const submitEdit = async () => {
  isSubmitting.value = true;
  
  try {
    // 准备更新数据
    const updateData: updateUserForm = {
      userId: userStore.userId,
      username: editForm.value.username,
      email: editForm.value.email,
    };
    
    // 如果有新上传的头像文件，添加到更新数据
    if (avatarFile.value) {
      updateData.avatarFile = avatarFile.value;
    }
    
    // 调用更新用户信息的API
    const response = await updateUserInfo(updateData);
    console.log(response)
    if (response.code === 200) {
      // 更新本地状态
      userStore.username = response.data.username;
      
      if (response.data.avatar) {
        userStore.avatar = response.data.avatar;
      }
      
      ElMessage.success('个人信息更新成功');
      dialogVisible.value = false;
      
      // 重置头像状态
      avatarFile.value = null;
      avatarPreview.value = '';
    } else {
      ElMessage.error(response.data.message || '更新失败');
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    ElMessage.error('更新用户信息失败');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped lang="scss">
.profile-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
//   background-color: transparent;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;
  }
  
  .back-button {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 14px;
    color: var(--text-color);
  }
  
  .profile-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color);
  }
}

.profile-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: var(--shadow);
  background-color: transparent;
  border: 1px solid var(--border-color);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
    color: var(--text-color);
  }
}

.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.user-avatar {
  margin-bottom: 10px;
}

.user-details {
  width: 100%;
  max-width: 500px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
  
  .label {
    width: 100px;
    font-weight: 500;
    color: var(--text-color-secondary);
  }
  
  .value {
    flex: 1;
    color: var(--text-color);
  }
}

.profile-placeholder {
  padding: 30px;
  text-align: center;
  background-color: var(--bg-color);
  border-radius: 6px;
  color: var(--text-color-secondary);
  font-size: 14px;
}

.models-container,
.assignments-container {
  display: flex;
  flex-direction: row;
  gap: 20px;
  padding: 15px 5px;
  min-width: max-content;
}

.horizontal-scrollbar {
  max-width: 100%;
  height: auto;
}

.model-item,
.assignment-item {
  flex: 0 0 auto;
  width: 200px;
  height: 180px;
  border-radius: 6px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.model-placeholder,
.assignment-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: transparent;
  color: var(--text-color-secondary);
  font-size: 16px;
  border: 1px dashed var(--border-color);
}

// 头像上传样式
.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  
  .avatar {
    border-radius: 50%;
    object-fit: cover;
  }
  
  .avatar-upload-btn {
    margin-top: 10px;
  }
}

.model-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.model-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.model-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.model-name {
  font-weight: 500;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-description {
  font-size: 12px;
  color: var(--text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.empty-message {
  width: 100%;
  text-align: center;
  color: var(--text-color-secondary);
  padding: 20px;
}

.assignment-item.exam-card {
  width: 300px;
  height: auto;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 15px;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(64, 158, 255, 0.3);
  }
  
  .card-content {
    display: flex;
    padding: 15px;
    color: var(--text-color);
    
    .card-image {
      width: 100px;
      height: 140px;
      overflow: hidden;
      border-radius: 10px;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
      }
    }
    
    .card-info {
      flex: 1;
      padding-left: 15px;
      display: flex;
      flex-direction: column;
      
      .card-title {
        margin: 0 0 10px;
        font-size: 18px;
        font-weight: 600;
        color: var(--text-color);
      }
      
      .card-details {
        p {
          display: flex;
          align-items: center;
          margin: 5px 0;
          font-size: 14px;
          color: var(--text-color);
          opacity: 0.8;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
      
      .card-score {
        margin-top: 10px;
        p {
          display: flex;
          align-items: center;
          margin: 0;
          font-size: 14px;
          color: var(--text-color);
          
          strong {
            margin-right: 5px;
          }
        }
      }
    }
  }
}

.assignment-content {
  display: none;
}

.assignment-info {
  display: none;
}

.assignment-name, .assignment-date, .assignment-duration {
  display: none;
}

/* 图表样式 */
.chart-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 400px;
  padding: 20px 0;
}

.exam-chart {
  width: 100%;
  height: 100%;
}
</style>