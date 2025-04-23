<template>
  <div class="question-analysis-container">
    <!-- 顶部导航栏 -->
    <div class="analysis-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="analysis-title">题目分析</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else>
      <!-- 题目难度分析 -->
      <el-card class="analysis-card">
        <template #header>
          <div class="card-header">
            <span>题目难度分析</span>
          </div>
        </template>
        <div v-if="questionAnalysisData.length === 0" class="empty-data">
          <el-empty description="暂无题目分析数据" />
        </div>
        <div v-else class="chart-container">
          <div ref="questionDifficultyChartRef" class="chart"></div>
        </div>
      </el-card>

      <!-- 题目列表 -->
      <el-card class="analysis-card">
        <template #header>
          <div class="card-header">
            <span>题目列表</span>
            <el-input
              v-model="searchText"
              placeholder="搜索题目内容"
              class="search-input"
              prefix-icon="Search"
              clearable
            />
          </div>
        </template>
        <div v-if="questionAnalysisData.length === 0" class="empty-data">
          <el-empty description="暂无题目分析数据" />
        </div>
        <el-table v-else :data="filteredQuestionData" border stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="description" label="题目描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="attemptCount" label="答题人数" width="100" sortable />
          <el-table-column prop="correctCount" label="正确人数" width="100" sortable />
          <el-table-column label="正确率" width="100" sortable-by="correctRate">
            <template #default="scope">
              <el-progress
                :percentage="Number((scope.row.correctRate * 100).toFixed(1))"
                :color="getDifficultyColor(scope.row.correctRate)"
                :format="(percentage) => `${percentage}%`"
              />
            </template>
          </el-table-column>
          <el-table-column label="难度" width="100">
            <template #default="scope">
              <el-tag :type="getDifficultyType(scope.row.correctRate)">
                {{ getDifficultyLabel(scope.row.correctRate) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredQuestionData.length"
          />
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Refresh, Search } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { reqQuestionAnalysis, updateQuestionAnalysis } from '@/api/exam-statistics';
import type { QuestionAnalysisResponse } from '@/api/exam-statistics/type';

const route = useRoute();
const router = useRouter();
const examId = ref<number>(Number(route.params.id) || 0);

// 图表引用
const questionDifficultyChartRef = ref<HTMLElement | null>(null);
let questionDifficultyChart: echarts.ECharts | null = null;

// 数据状态
const loading = ref(true);
const questionAnalysisData = ref<QuestionAnalysisResponse[]>([]);

// 表格分页和搜索
const currentPage = ref(1);
const pageSize = ref(10);
const searchText = ref('');

// 计算属性：筛选后的题目数据
const filteredQuestionData = computed(() => {
  if (!searchText.value) {
    return questionAnalysisData.value;
  }
  return questionAnalysisData.value.filter(question => 
    question.description.includes(searchText.value)
  );
});

// 根据正确率获取难度标签
const getDifficultyLabel = (correctRate: number): string => {
  if (correctRate < 0.4) return '困难';
  if (correctRate < 0.7) return '中等';
  return '简单';
};

// 根据正确率获取难度类型
const getDifficultyType = (correctRate: number): string => {
  if (correctRate < 0.4) return 'danger';
  if (correctRate < 0.7) return 'warning';
  return 'success';
};

// 根据正确率获取颜色
const getDifficultyColor = (correctRate: number): string => {
  if (correctRate < 0.4) return '#F56C6C';
  if (correctRate < 0.7) return '#E6A23C';
  return '#67C23A';
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 加载题目分析数据
const loadQuestionAnalysis = async () => {
  if (!examId.value) {
    ElMessage.error('考试ID无效');
    return;
  }
  
  loading.value = true;
  try {
    const res = await reqQuestionAnalysis(examId.value);
    console.log('Question analysis response:', res);
    
    if (res?.code === 200 && res.data) {
      questionAnalysisData.value = res.data;
      // 延迟一下初始化图表，确保DOM已渲染
      setTimeout(() => {
        initQuestionDifficultyChart();
      }, 100);
    } else {
      ElMessage.warning(res?.message || '获取题目分析数据失败');
    }
  } catch (error) {
    console.error('Failed to load question analysis:', error);
    ElMessage.error('获取题目分析数据失败');
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refreshData = async () => {
  if (!examId.value) {
    ElMessage.error('考试ID无效');
    return;
  }
  
  loading.value = true;
  try {
    // 调用更新分析数据的接口
    const res = await updateQuestionAnalysis(examId.value);
    
    if (res?.code === 200) {
      ElMessage.success('数据更新成功');
      // 重新加载数据
      await loadQuestionAnalysis();
    } else {
      ElMessage.error(res?.message || '更新分析数据失败');
      loading.value = false;
    }
  } catch (error) {
    console.error('Failed to update question analysis:', error);
    ElMessage.error('更新分析数据失败');
    loading.value = false;
  }
};

// 初始化题目难度分析图表
const initQuestionDifficultyChart = () => {
  if (!questionDifficultyChartRef.value || questionAnalysisData.value.length === 0) return;
  
  if (questionDifficultyChart) {
    questionDifficultyChart.dispose();
  }
  
  questionDifficultyChart = echarts.init(questionDifficultyChartRef.value);
  
  // 对题目按照正确率从低到高排序
  const sortedQuestions = [...questionAnalysisData.value].sort((a, b) => a.correctRate - b.correctRate);
  
  const option = {
    title: {
      text: '题目难度分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params: any) {
        const dataIndex = params[0].dataIndex;
        const question = sortedQuestions[dataIndex];
        return `${question.description}<br/>` +
               `正确率: ${(question.correctRate * 100).toFixed(1)}%<br/>` +
               `答题人数: ${question.attemptCount}<br/>` +
               `正确人数: ${question.correctCount}`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedQuestions.map((q, index) => `题${index + 1}`),
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '正确率(%)',
      min: 0,
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '正确率',
        type: 'bar',
        data: sortedQuestions.map(q => (q.correctRate * 100).toFixed(1)),
        itemStyle: {
          color: function(params: any) {
            const rate = sortedQuestions[params.dataIndex].correctRate;
            return getDifficultyColor(rate);
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        },
        barWidth: '40%'
      }
    ]
  };
  
  questionDifficultyChart.setOption(option);
  
  window.addEventListener('resize', () => {
    questionDifficultyChart?.resize();
  });
};

// 监听examId变化
watch(() => examId.value, () => {
  if (examId.value) {
    loadQuestionAnalysis();
  }
});

// 组件挂载后初始化
onMounted(() => {
  if (examId.value) {
    loadQuestionAnalysis();
  }
});
</script>

<style scoped lang="scss">
.question-analysis-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .back-button {
      display: flex;
      align-items: center;
      gap: 5px;
    }
    
    .analysis-title {
      font-size: 22px;
      font-weight: 600;
      margin: 0;
    }
  }
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.analysis-card {
  margin-bottom: 20px;
  border-radius: 8px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
  }
  
  .search-input {
    width: 250px;
  }
}

.chart-container {
  width: 100%;
  height: 400px;
  
  .chart {
    width: 100%;
    height: 100%;
  }
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 