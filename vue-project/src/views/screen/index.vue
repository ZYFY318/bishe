<template>
    <div class="app">
        <div ref="chart" style="width: 100%; height: 100%;"></div>
        <div>
            <button @click="setExamType(1)">小测试</button>
            <button @click="setExamType(2)">中测试</button>
            <button @click="setExamType(3)">大测试</button>
            <button @click="setDataType('score')">分数</button>
            <button @click="setDataType('duration')">用时</button>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import * as echarts from 'echarts';
import useUserStore from '@/stores/modules/user';
import { reqUserExamResults } from '@/api/examResult';

const chart = ref(null);
const chartInstance = ref(null);
const examType = ref(1); // 默认小测试
const dataType = ref('score'); // 默认展示分数
const chartData = ref([]);

// 获取用户信息
const userStore = useUserStore();
const userId = userStore.userId; // 监听 userId 变化

const fetchData = async () => {
    // console.log(userId, "??");
    if (!userId) return; // 确保 userId 存在
    try {
        const response = await reqUserExamResults(userId);
        console.log(response);
        chartData.value = response.data.filter(item => item.examType === examType.value);
        updateChart();
    } catch (error) {
        console.error('获取数据失败', error);

    }
};

const updateChart = () => {
    if (!chartInstance.value) return;
    const dates = chartData.value.map(item => item.examDate);
    const values = chartData.value.map(item => item[dataType.value]);

    const option = {
        title: { text: `${dataType.value === 'score' ? '分数' : '用时'}随日期变化` },
        xAxis: { type: 'category', data: dates },
        yAxis: { type: 'value' },
        series: [{ type: 'line', data: values }]
    };
    chartInstance.value.setOption(option);
};

const setExamType = (type) => {
    examType.value = type;
};

const setDataType = (type) => {
    dataType.value = type;
};

onMounted(() => {
    chartInstance.value = echarts.init(chart.value);
    fetchData();
});

// 监听 examType、dataType 和 userId 变化
watch([examType, dataType, userId], fetchData);
</script>

<style>
app {
    width: 100%;
}

button {
    margin: 5px;
    padding: 8px;
    cursor: pointer;
}
</style>