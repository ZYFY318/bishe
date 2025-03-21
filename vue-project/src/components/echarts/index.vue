<template>
    <div ref="chartRef" :style="{ width, height }"></div>
    <!-- <h1>你好</h1> -->
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
    options: {
        type: Object,
        required: true,
    },
    width: {
        type: String,
        default: '600px',
    },
    height: {
        type: String,
        default: '300px',
    },
    autoResize: {
        type: Boolean,
        default: true,
    },
});

const chartRef = ref(null);
let chartInstance = null;

const initChart = () => {
    if (chartRef.value) {
        chartInstance = echarts.init(chartRef.value);
        chartInstance.setOption(props.options);
    }
};

onMounted(() => {
    initChart();
    if (props.autoResize) {
        window.addEventListener('resize', resizeChart);
    }
});

onBeforeUnmount(() => {
    if (props.autoResize) {
        window.removeEventListener('resize', resizeChart);
    }
    if (chartInstance) {
        chartInstance.dispose();
    }
});

watch(
    () => props.options,
    (newOptions) => {
        if (chartInstance && newOptions) {
            chartInstance.setOption(newOptions);
        }
    },
    { deep: true }
);

const resizeChart = () => {
    if (chartInstance) {
        chartInstance.resize();
    }
};
</script>