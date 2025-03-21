<template>
    <div class="ap21">
        <EChart :options="chartOptions" width="600px" height="300px" />
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
import { ref, watch } from 'vue';
import EChart from '@/components/echarts'
import useUserStore from '@/stores/modules/user';
import { reqUserExamResults } from '@/api/examResult';

const examType = ref(1);
const dataType = ref('score');
const chartData = ref([]);
const chartOptions = ref({});

const userStore = useUserStore();
const userId = userStore.userId;

const fetchData = async () => {
    if (!userId) return;
    try {
        const response = await reqUserExamResults(userId);
        // 过滤出符合当前考试类型的数据
        chartData.value = response.data.filter(item => item.examType === examType.value);
        updateChart();
    } catch (error) {
        console.error('获取数据失败', error);
    }
};

const updateChart = () => {
    const dates = chartData.value.map(item => item.examDate);
    const values = chartData.value.map(item => item[dataType.value]);
    chartOptions.value = {
        title: { text: `${dataType.value === 'score' ? '分数' : '用时'}随日期变化` },
        xAxis: { type: 'category', data: dates },
        yAxis: { type: 'value' },
        series: [{ type: 'line', data: values }],
    };
};

const setExamType = (type) => {
    examType.value = type;
};

const setDataType = (type) => {
    dataType.value = type;
};

// 监听 examType、dataType 或 userId 变化时重新获取数据
watch([examType, dataType, () => userStore.userId], fetchData);

fetchData();
</script>

<style scoped>
.ap21 {
    flex: 1;
    height: 400px;
}

button {
    margin: 5px;
    padding: 8px;
    cursor: pointer;
}
</style>