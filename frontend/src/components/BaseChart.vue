<template>
  <div ref="chartRef" class="chart-host"></div>
</template>

<script setup>
import * as echarts from 'echarts';
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps({
  option: {
    type: Object,
    required: true,
  },
});

const chartRef = ref(null);
let chartInstance;
let observer;

function render() {
  if (!chartRef.value) {
    return;
  }
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }
  chartInstance.setOption(props.option, true);
}

onMounted(() => {
  render();
  observer = new ResizeObserver(() => {
    chartInstance?.resize();
  });
  observer.observe(chartRef.value);
});

watch(
  () => props.option,
  () => {
    render();
  },
  { deep: true },
);

onBeforeUnmount(() => {
  observer?.disconnect();
  chartInstance?.dispose();
});
</script>
