<template>
  <div class="progress-rail">
    <div class="progress-rail__meta">
      <span>{{ label }}</span>
      <strong>{{ value }} / {{ target }} {{ unit }}</strong>
    </div>
    <div class="progress-rail__track">
      <div
        class="progress-rail__fill"
        :class="tone"
        :style="{ width: `${Math.min(100, Math.round((value / target) * 100))}%` }"
      ></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: Number,
  target: Number,
  unit: String,
  tone: String,
});
</script>
