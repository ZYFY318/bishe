<template>
  <div class="exam-analysis-container">
    <!-- 顶部导航栏 -->
    <div class="analysis-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h1 class="analysis-title">{{ examInfo.title || '未命名考试' }} - 考试分析</h1>
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
      <!-- 考试基本信息和统计数据 -->
      <el-card class="analysis-card info-card">
        <template #header>
          <div class="card-header">
            <span>考试基本信息</span>
          </div>
        </template>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">考试名称</span>
            <span class="value">{{ examInfo.title || '未命名考试' }}</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ formatDate(examInfo.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">考试时长</span>
            <span class="value">{{ examInfo.duration || 0 }}分钟</span>
          </div>
          <div class="info-item">
            <span class="label">题目数量</span>
            <span class="value">{{ examInfo.questionCount || 0 }}题</span>
          </div>
          <div class="info-item">
            <span class="label">总分值</span>
            <span class="value">{{ examInfo.totalScore || 0 }}分</span>
          </div>
          <div class="info-item">
            <span class="label">参与人数</span>
            <span class="value">{{ statistics ? statistics.participantCount : 0 }}人</span>
          </div>
          <div class="info-item">
            <span class="label">平均分数</span>
            <span class="value">{{ statistics ? (statistics.avgScore || 0).toFixed(1) : '0.0' }}分</span>
          </div>
          <div class="info-item">
            <span class="label">及格人数</span>
            <span class="value">{{ statistics ? statistics.passingCount : 0 }}人 ({{ calcPassingRate() }}%)</span>
          </div>
        </div>
      </el-card>

      <!-- 详细统计数据 -->
      <el-card class="analysis-card">
        <template #header>
          <div class="card-header">
            <span>考试统计详情</span>
          </div>
        </template>
        <div v-if="!statistics" class="empty-data">
          <el-empty description="暂无统计数据" />
        </div>
        <div v-else class="statistics-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="及格分数线">{{ statistics.passingScore || 60 }}分</el-descriptions-item>
            <el-descriptions-item label="及格率">{{ calcPassingRate() }}%</el-descriptions-item>
            <el-descriptions-item label="最高分">{{ statistics.highestScore || 0 }}分</el-descriptions-item>
            <el-descriptions-item label="最低分">{{ statistics.lowestScore || 0 }}分</el-descriptions-item>
            <el-descriptions-item label="平均用时">{{ formatTime(statistics.avgTime || 0) }}</el-descriptions-item>
            <el-descriptions-item label="总用时">{{ formatTime(statistics.totalTime || 0) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 成绩分布图表 -->
      <el-card class="analysis-card">
        <template #header>
          <div class="card-header">
            <span>分数分布</span>
          </div>
        </template>
        <div v-if="!scoreDistribution || !scoreDistribution.totalParticipants" class="empty-data">
          <el-empty description="暂无分数分布数据" />
        </div>
        <div v-else class="chart-container">
          <div ref="scoreDistributionChartRef" class="chart"></div>
        </div>
      </el-card>

      <!-- 题目难度分析 -->
      <el-card class="analysis-card">
        <template #header>
          <div class="card-header">
            <span>题目难度分析</span>
          </div>
        </template>
        <div v-if="!questionAnalysisData || questionAnalysisData.length === 0" class="empty-data">
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
        <div v-if="!questionAnalysisData || questionAnalysisData.length === 0" class="empty-data">
          <el-empty description="暂无题目分析数据" />
        </div>
        <el-table v-else :data="paginatedQuestionData" border stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="description" label="题目描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="attemptCount" label="答题人数" width="100" sortable />
          <el-table-column prop="correctCount" label="正确人数" width="100" sortable />
          <el-table-column label="正确率" width="120" sortable-by="correctRate">
            <template #default="scope">
              <el-progress
                :percentage="Number((scope.row.correctRate * 100).toFixed(1))"
                :color="getDifficultyColor(scope.row.correctRate)"
                :format="(percentage: number) => `${percentage.toFixed(1)}%`"
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
        <div v-if="questionAnalysisData && questionAnalysisData.length > 0" class="pagination-container">
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
import { ArrowLeft, Refresh, Search, PieChart } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { reqExamStatistics, reqScoreDistribution, updateExamStatistics, reqQuestionAnalysis, updateQuestionAnalysis } from '@/api/exam-statistics';
import type { ExamStatisticsResponse, ScoreDistributionResponse, QuestionAnalysisResponse } from '@/api/exam-statistics/type';
import { fetchExamDetail } from '@/api/product/exam';

// 开启调试日志
console.log("开始执行考试分析组件");

const route = useRoute();
const router = useRouter();
const examId = computed(() => Number(route.params.id) || 0);

// 图表引用
const scoreDistributionChartRef = ref<HTMLElement | null>(null);
let scoreDistributionChart: echarts.ECharts | null = null;
const questionDifficultyChartRef = ref<HTMLElement | null>(null);
let questionDifficultyChart: echarts.ECharts | null = null;

// 数据状态
const loading = ref(true);
const statistics = ref<ExamStatisticsResponse | null>(null);
const scoreDistribution = ref<ScoreDistributionResponse | null>(null);
const examInfo = ref<any>({});
const questionAnalysisData = ref<QuestionAnalysisResponse[]>([]);

// 表格分页和搜索
const currentPage = ref(1);
const pageSize = ref(10);
const searchText = ref('');

// 计算属性：筛选后的题目数据
const filteredQuestionData = computed(() => {
  if (!searchText.value || !questionAnalysisData.value) {
    return questionAnalysisData.value || [];
  }
  return questionAnalysisData.value.filter(question => 
    question.description.toLowerCase().includes(searchText.value.toLowerCase())
  );
});

// 计算当前页的题目数据
const paginatedQuestionData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredQuestionData.value.slice(start, end);
});

// 题目难度相关方法
const getDifficultyLabel = (correctRate: number): string => {
  if (correctRate < 0.4) return '困难';
  if (correctRate < 0.7) return '中等';
  return '简单';
};

const getDifficultyType = (correctRate: number): string => {
  if (correctRate < 0.4) return 'danger';
  if (correctRate < 0.7) return 'warning';
  return 'success';
};

const getDifficultyColor = (correctRate: number): string => {
  if (correctRate < 0.4) return '#F56C6C';
  if (correctRate < 0.7) return '#E6A23C';
  return '#67C23A';
};

// 计算及格率
const calcPassingRate = () => {
  if (!statistics.value || !statistics.value.participantCount || statistics.value.participantCount === 0) {
    return '0.0';
  }
  
  const rate = (statistics.value.passingCount / statistics.value.participantCount) * 100;
  return rate.toFixed(1);
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
};

// 格式化时间（秒转为分:秒）
const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = Math.floor(seconds % 60);
  return `${minutes}分${remainingSeconds}秒`;
};

// 返回上一页
const goBack = () => {
  router.back();
};

// 加载考试信息
const loadExamInfo = async () => {
  if (!examId.value) return;
  
  try {
    console.log("正在加载考试信息, examId:", examId.value);
    const res = await fetchExamDetail(examId.value);
    
    console.log("考试信息加载结果:", res);
    
    if (res && res.code === 200) {
      examInfo.value = res.data;
    } else {
      ElMessage.warning('获取考试信息失败');
    }
  } catch (error) {
    console.error('Failed to load exam info:', error);
    ElMessage.error('获取考试信息失败');
  }
};

// 加载考试统计数据
const loadStatistics = async () => {
  if (!examId.value) return;
  
  try {
    console.log("正在加载考试统计数据, examId:", examId.value);
    const res = await reqExamStatistics(examId.value);
    
    console.log("考试统计数据加载结果:", res);
    
    if (res && res.code === 200 && res.data) {
      statistics.value = res.data;
    } else {
      console.warn("获取统计数据失败:", res);
      ElMessage.warning(res?.message || '获取统计数据失败');
    }
  } catch (error) {
    console.error('Failed to load exam statistics:', error);
    ElMessage.error('加载考试统计数据失败');
  }
};

// 加载分数分布数据
const loadScoreDistribution = async () => {
  if (!examId.value) return;
  
  try {
    console.log("正在加载分数分布数据, examId:", examId.value);
    const res = await reqScoreDistribution(examId.value);
    
    console.log("分数分布数据加载结果:", res);
    
    if (res && res.code === 200 && res.data) {
      scoreDistribution.value = res.data;
      
      // 加载完分数分布数据后初始化图表
      if (scoreDistribution.value && scoreDistribution.value.totalParticipants > 0) {
        setTimeout(() => {
          initScoreDistributionChart();
        }, 100);
      }
    } else {
      console.warn("获取分数分布数据失败:", res);
      ElMessage.warning(res?.message || '获取分数分布数据失败');
    }
  } catch (error) {
    console.error('Failed to load score distribution:', error);
    ElMessage.error('加载分数分布数据失败');
  }
};

// 加载题目分析数据
const loadQuestionAnalysis = async () => {
  if (!examId.value) return;
  
  try {
    console.log("正在加载题目分析数据, examId:", examId.value);
    const res = await reqQuestionAnalysis(examId.value);
    
    console.log("题目分析数据加载结果:", res);
    
    if (res && res.code === 200 && res.data) {
      questionAnalysisData.value = res.data;
      
      // 加载完题目分析数据后初始化图表
      if (questionAnalysisData.value.length > 0) {
        setTimeout(() => {
          initQuestionDifficultyChart();
        }, 100);
      }
    } else {
      console.warn("获取题目分析数据失败:", res);
      ElMessage.warning(res?.message || '获取题目分析数据失败');
    }
  } catch (error) {
    console.error('Failed to load question analysis:', error);
    ElMessage.error('加载题目分析数据失败');
  }
};

// 刷新所有数据
const refreshData = async () => {
  if (!examId.value) return;
  
  loading.value = true;
  try {
    console.log("开始刷新数据, examId:", examId.value);
    
    // 更新统计数据
    const statsUpdateRes = await updateExamStatistics(examId.value);
    console.log("统计数据更新结果:", statsUpdateRes);
    
    // 更新题目分析数据
    const questionUpdateRes = await updateQuestionAnalysis(examId.value);
    console.log("题目分析数据更新结果:", questionUpdateRes);
    
    ElMessage.success('数据更新成功');
    
    // 重新加载所有数据
    await Promise.all([
      loadExamInfo(),
      loadStatistics(),
      loadScoreDistribution(),
      loadQuestionAnalysis()
    ]);
  } catch (error) {
    console.error('Failed to update data:', error);
    ElMessage.error('更新数据失败');
  } finally {
    loading.value = false;
  }
};

// 初始化成绩分布图表
const initScoreDistributionChart = () => {
  if (!scoreDistributionChartRef.value || !scoreDistribution.value) return;
  
  console.log("初始化分数分布图表");
  
  if (scoreDistributionChart) {
    scoreDistributionChart.dispose();
  }
  
  scoreDistributionChart = echarts.init(scoreDistributionChartRef.value);

  const { sections, counts, percentages } = scoreDistribution.value;
  const xAxisLabels: string[] = [];
  
  // 生成X轴标签: 0-10, 11-20, ...
  for (let i = 0; i < sections.length - 1; i++) {
    xAxisLabels.push(`${sections[i]}-${sections[i+1] - 1}`);
  }
  
  const option = {
    title: {
      text: '成绩分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params: any) => {
        const dataIndex = params[0].dataIndex;
        return `分数段: ${xAxisLabels[dataIndex]}<br/>人数: ${counts[dataIndex]}<br/>百分比: ${percentages[dataIndex].toFixed(2)}%`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisLabels,
      name: '分数段'
    },
    yAxis: [
      {
        type: 'value',
        name: '人数',
        position: 'left',
      },
      {
        type: 'value',
        name: '百分比(%)',
        position: 'right',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '人数',
        type: 'bar',
        data: counts,
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      },
      {
        name: '百分比',
        type: 'line',
        yAxisIndex: 1,
        data: percentages,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#ff9f7f'
        },
        lineStyle: {
          width: 2,
          color: '#ff9f7f'
        }
      }
    ]
  };
  
  scoreDistributionChart.setOption(option);
  
  // 响应窗口大小变化
  window.addEventListener('resize', () => {
    scoreDistributionChart?.resize();
  });
};

// 初始化题目难度分析图表
const initQuestionDifficultyChart = () => {
  if (!questionDifficultyChartRef.value || !questionAnalysisData.value || questionAnalysisData.value.length === 0) return;
  
  console.log("初始化题目难度分析图表");
  
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

// 加载所有数据
const loadAllData = async () => {
  if (!examId.value) return;
  
  console.log("开始加载全部数据, examId:", examId.value);
  loading.value = true;
  
  try {
    await Promise.all([
      loadExamInfo(),
      loadStatistics(),
      loadScoreDistribution(),
      loadQuestionAnalysis()
    ]);
    console.log("全部数据加载完成");
  } catch (error) {
    console.error('Failed to load data:', error);
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

// 监听examId变化，重新加载数据
watch(() => examId.value, (newId) => {
  console.log("examId变更为:", newId);
  if (newId) {
    loadAllData();
  }
});

// 组件挂载后初始化
onMounted(() => {
  console.log("组件已挂载, examId:", examId.value);
  if (examId.value) {
    loadAllData();
  }
});
</script>

<style scoped lang="scss">
.exam-analysis-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

.analysis-card {
  margin-bottom: 20px;
  border-radius: 8px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
    
    .title-section {
      display: flex;
      align-items: center;
      gap: 15px;
    }
    
    .search-input {
      width: 250px;
    }
  }
}

.info-card {
  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    grid-gap: 20px;
  }
  
  .info-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .label {
      font-size: 14px;
      color: #909399;
    }
    
    .value {
      font-size: 16px;
      font-weight: 500;
    }
  }
}

.statistics-content {
  padding: 10px;
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