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
        <div class="statistics-content">
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
        <div class="chart-container">
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
        <div class="chart-container">
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
        <el-table :data="paginatedQuestionData" border stripe>
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
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  import { ArrowLeft, Refresh, Search } from '@element-plus/icons-vue';
  import * as echarts from 'echarts';
  
  const router = useRouter();
  
  // 图表引用
  const scoreDistributionChartRef = ref<HTMLElement | null>(null);
  let scoreDistributionChart: echarts.ECharts | null = null;
  const questionDifficultyChartRef = ref<HTMLElement | null>(null);
  let questionDifficultyChart: echarts.ECharts | null = null;
  
  // 静态考试信息
  const staticExamInfo = {
    title: '测试试卷',
    createdAt: '2023-06-15T09:30:00',
    duration: 120,
    questionCount: 25,
    totalScore: 150
  };
  
  // 静态统计信息
  const staticStatistics = {
    participantCount: 85,
    avgScore: 78.5,
    passingCount: 62,
    passingScore: 60,
    highestScore: 148,
    lowestScore: 42,
    avgTime: 6348,
    totalTime: 539580
  };
  
  // 静态分数分布数据
  const staticScoreDistribution = {
    sections: [0, 60, 70, 80, 90, 100, 120, 150],
    counts: [5, 12, 18, 25, 20, 15, 5],
    percentages: [5.88, 14.12, 21.18, 29.41, 23.53, 17.65, 5.88],
    totalParticipants: 85
  };
  
  // 静态题目分析数据
  const generateQuestionAnalysis = () => {
    const questionTypes = ['函数', '几何', '代数', '统计', '概率'];
    return Array.from({ length: 25 }, (_, i) => ({
      id: i + 1,
      description: `测试 ${i + 1}`,
      attemptCount: 85 - Math.floor(Math.random() * 10),
      correctCount: Math.floor(85 * (0.3 + Math.random() * 0.6)),
      correctRate: parseFloat((0.3 + Math.random() * 0.6).toFixed(2))
    }));
  };
  
  // 数据状态
  const examInfo = ref(staticExamInfo);
  const statistics = ref(staticStatistics);
  const scoreDistribution = ref(staticScoreDistribution);
  const questionAnalysisData = ref(generateQuestionAnalysis());
  
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
    if (!statistics.value || !statistics.value.participantCount) {
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
  
  // 刷新数据
  const refreshData = () => {
    ElMessage.success('演示数据已刷新');
    questionAnalysisData.value = generateQuestionAnalysis();
    setTimeout(() => {
      initQuestionDifficultyChart();
    }, 100);
  };
  
  // 初始化成绩分布图表
  const initScoreDistributionChart = () => {
    if (!scoreDistributionChartRef.value || !scoreDistribution.value) return;
    
    if (scoreDistributionChart) {
      scoreDistributionChart.dispose();
    }
    
    scoreDistributionChart = echarts.init(scoreDistributionChartRef.value);
  
    const { sections, counts, percentages } = scoreDistribution.value;
    const xAxisLabels: string[] = [];
    
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
    
    window.addEventListener('resize', () => {
      scoreDistributionChart?.resize();
    });
  };
  
  // 初始化题目难度分析图表
  const initQuestionDifficultyChart = () => {
    if (!questionDifficultyChartRef.value || !questionAnalysisData.value || questionAnalysisData.value.length === 0) return;
    
    if (questionDifficultyChart) {
      questionDifficultyChart.dispose();
    }
    
    questionDifficultyChart = echarts.init(questionDifficultyChartRef.value);
    
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
  
  // 初始化数据
  const initData = () => {
    // 数据已经通过静态变量初始化
    setTimeout(() => {
      initScoreDistributionChart();
      initQuestionDifficultyChart();
    }, 500);
  };
  
  onMounted(() => {
    initData();
  });
  </script>
  
  <style scoped lang="scss">
  .exam-analysis-container {
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
  
  .analysis-card {
    margin-bottom: 20px;
    border-radius: 8px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 18px;
      font-weight: 500;
      
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
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  </style>