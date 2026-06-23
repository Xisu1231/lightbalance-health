<template>
  <div ref="chartRef" class="chart-host"></div>
</template>

<script setup>
import * as echarts from 'echarts';
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps({
  option: {
    type: Object,
    required: true,
  },
});

const chartRef = ref(null);
let chartInstance;
let observer;
let resizeTimer;

function scheduleResize(attempt = 0) {
  clearTimeout(resizeTimer);
  resizeTimer = setTimeout(() => {
    if (!chartRef.value || !chartInstance) {
      return;
    }

    const { clientWidth, clientHeight } = chartRef.value;
    if ((clientWidth < 80 || clientHeight < 80) && attempt < 6) {
      scheduleResize(attempt + 1);
      return;
    }

    chartInstance.resize();
  }, attempt === 0 ? 16 : 120);
}

function render() {
  if (!chartRef.value) {
    return;
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  chartInstance.setOption(props.option, true);
  scheduleResize();
}

onMounted(() => {
  render();
  observer = new ResizeObserver(() => {
    scheduleResize();
  });
  observer.observe(chartRef.value);
  window.addEventListener('resize', scheduleResize);
});

watch(
  () => props.option,
  () => {
    nextTick(() => {
      render();
    });
  },
  { deep: true },
);

onBeforeUnmount(() => {
  clearTimeout(resizeTimer);
  observer?.disconnect();
  window.removeEventListener('resize', scheduleResize);
  chartInstance?.dispose();
});
</script>
