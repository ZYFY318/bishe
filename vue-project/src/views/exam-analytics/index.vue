<template>
  <div class="exam-analytics-container">
    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <h2>考试统计数据</h2>
          <el-button type="primary" @click="updateStatistics">刷新统计数据</el-button>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="statistics" class="statistics-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="参与人数">{{ statistics.participantCount || 0 }}人</el-descriptions-item>
          <el-descriptions-item label="平均分数">{{ statistics.avgScore ? statistics.avgScore.toFixed(2) : '0.00' }}分</el-descriptions-item>
          <el-descriptions-item label="及格分数线">{{ statistics.passingScore || 60 }}分</el-descriptions-item>
          <el-descriptions-item label="及格人数">{{ statistics.passingCount || 0 }}人 ({{ getPassRate() }}%)</el-descriptions-item>
          <el-descriptions-item label="最高分">{{ statistics.highestScore || 0 }}分</el-descriptions-item>
          <el-descriptions-item label="最低分">{{ statistics.lowestScore || 0 }}分</el-descriptions-item>
          <el-descriptions-item label="平均用时">{{ formatTime(statistics.avgTime || 0) }}</el-descriptions-item>
          <el-descriptions-item label="总用时">{{ formatTime(statistics.totalTime || 0) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="empty-data">
        <el-empty description="暂无统计数据" />
      </div>
    </el-card>

    <el-card class="distribution-card">
      <template #header>
        <div class="card-header">
          <div class="title-section">
            <span>分数分布</span>
            <el-button type="primary" size="small" @click="goToQuestionAnalysis">
              <el-icon><PieChart /></el-icon>
              查看题目分析
            </el-button>
          </div>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="scoreDistribution && scoreDistribution.totalParticipants > 0" class="chart-container">
        <div ref="scoreChartRef" class="score-chart"></div>
      </div>
      <div v-else class="empty-data">
        <el-empty description="暂无分数分布数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { reqExamStatistics, reqScoreDistribution, updateExamStatistics } from '@/api/exam-statistics';
import type { ExamStatisticsResponse, ScoreDistributionResponse } from '@/api/exam-statistics/type';
import { ArrowLeft, Download, Search, PieChart } from '@element-plus/icons-vue';

console.log("组件脚本开始执行");

const route = useRoute();
const router = useRouter();
const examId = computed(() => Number(route.params.id));

const loading = ref(true);
const statistics = ref<ExamStatisticsResponse | null>(null);
const scoreDistribution = ref<ScoreDistributionResponse | null>(null);
const scoreChartRef = ref<HTMLElement | null>(null);
let scoreChart: echarts.ECharts | null = null;

// 获取及格率
const getPassRate = () => {
  if (!statistics.value || statistics.value.participantCount === 0) return '0.00';
  return ((statistics.value.passingCount / statistics.value.participantCount) * 100).toFixed(2);
};

// 格式化时间（秒转为分:秒）
const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = Math.floor(seconds % 60);
  return `${minutes}分${remainingSeconds}秒`;
};

// 初始化图表
const initScoreChart = () => {
  if (!scoreChartRef.value || !scoreDistribution.value) return;
  
  if (scoreChart) {
    scoreChart.dispose();
  }
  
  scoreChart = echarts.init(scoreChartRef.value);
  
  const { sections, counts, percentages } = scoreDistribution.value;
  const xAxisLabels: string[] = [];
  
  // 生成X轴标签: 0-10, 11-20, ...
  for (let i = 0; i < sections.length - 1; i++) {
    xAxisLabels.push(`${sections[i]}-${sections[i+1] - 1}`);
  }
  
  const option = {
    title: {
      text: '考试分数分布',
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
  
  scoreChart.setOption(option);
  
  // 响应窗口大小变化
  window.addEventListener('resize', () => {
    scoreChart?.resize();
  });
};

// 加载考试统计数据
const loadStatistics = async () => {
  if (!examId.value) return;
  
  loading.value = true;
  try {
    const res = await reqExamStatistics(examId.value);
    console.log('Statistics response:', res);
    // 如果后端返回的是ResponseMessage格式，则需要从data字段获取数据
    if (res && res.data) {
      statistics.value = res.data;
    } else if (res) {
      statistics.value = res;
    } else {
      ElMessage.warning('获取到的统计数据为空');
    }
  } catch (error) {
    console.error('Failed to load exam statistics:', error);
    ElMessage.error('加载考试统计数据失败');
  } finally {
    loading.value = false;
  }
};

// 加载分数分布数据
const loadScoreDistribution = async () => {
  if (!examId.value) return;
  
  try {
    const res = await reqScoreDistribution(examId.value);
    console.log('Distribution response:', res);
    // 如果后端返回的是ResponseMessage格式，则需要从data字段获取数据
    if (res && res.data) {
      scoreDistribution.value = res.data;
    } else if (res) {
      scoreDistribution.value = res;
    }
    
    // 加载完分数分布数据后初始化图表
    if (scoreDistribution.value) {
      setTimeout(() => {
        initScoreChart();
      }, 100);
    }
  } catch (error) {
    console.error('Failed to load score distribution:', error);
    ElMessage.error('加载分数分布数据失败');
  }
};

// 更新统计数据
const updateStatistics = async () => {
  if (!examId.value) return;
  
  try {
    loading.value = true;
    await updateExamStatistics(examId.value);
    ElMessage.success('统计数据更新成功');
    // 刷新数据
    await loadStatistics();
    await loadScoreDistribution();
  } catch (error) {
    console.error('Failed to update statistics:', error);
    ElMessage.error('更新统计数据失败');
  } finally {
    loading.value = false;
  }
};

// 跳转到题目分析页面
const goToQuestionAnalysis = () => {
  router.push(`/question-analysis/${examId.value}`);
};

// 监听examId变化，重新加载数据
watch(() => examId.value, () => {
  loadStatistics();
  loadScoreDistribution();
});

onMounted(() => {
  loadStatistics();
  loadScoreDistribution();
  console.log("组件定义完成");
});
</script>

<style scoped>
.exam-analytics-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-card,
.distribution-card {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.empty-data {
  padding: 40px 0;
}

.statistics-content {
  padding: 10px;
}

.chart-container {
  width: 100%;
  height: 400px;
}

.score-chart {
  width: 100%;
  height: 100%;
}
</style> 