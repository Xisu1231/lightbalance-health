<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <span class="brand-kicker">LIGHTBALANCE</span>
        <h1>轻享健康</h1>
        <p>科学减重健康平台</p>
      </div>

      <nav class="nav-list">
        <button
          v-for="item in navItems"
          :key="item.key"
          class="nav-item"
          :class="{ active: currentSection === item.key }"
          @click="currentSection = item.key"
        >
          <component :is="item.icon" :size="18" />
          <div>
            <strong>{{ item.label }}</strong>
            <span>{{ item.desc }}</span>
          </div>
        </button>
      </nav>
    </aside>

    <main class="workspace" v-if="dashboard && analytics && assistant">
      <header class="workspace-header">
        <div>
          <p class="kicker">健康生活分析软件</p>
          <h2>{{ dashboard.recoverySignal.headline }}</h2>
          <p class="subcopy">{{ dashboard.recoverySignal.subline }}</p>
        </div>
        <div class="profile-chip">
          <div class="avatar">{{ dashboard.profile.name.slice(0, 1) }}</div>
          <div>
            <strong>{{ dashboard.profile.name }}</strong>
            <span>{{ dashboard.profile.handle }}</span>
          </div>
          <span class="sync-badge">{{ dashboard.recoverySignal.badge }}</span>
        </div>
      </header>

      <section v-show="currentSection === 'overview'" class="band">
        <div class="hero-grid">
          <div class="hero-main panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Fuel Dashboard</p>
                <h3>今天的状态总览</h3>
              </div>
              <span class="score-pill">健康评分 {{ dashboard.profile.healthScore }}</span>
            </div>
            <div class="stats-grid">
              <article class="stat-tile">
                <span>今日摄入</span>
                <strong>{{ dashboard.summary.calories }} kcal</strong>
                <small>目标 {{ dashboard.summary.calorieTarget }} kcal</small>
              </article>
              <article class="stat-tile">
                <span>饮水完成</span>
                <strong>{{ dashboard.summary.water }} / {{ dashboard.summary.waterTarget }} ml</strong>
                <div class="mini-actions">
                  <button @click="addWater(250)">+250 ml</button>
                  <button @click="addWater(500)">+500 ml</button>
                </div>
              </article>
              <article class="stat-tile stat-tile--dark">
                <span>恢复面板</span>
                <strong>{{ dashboard.summary.sleepHours }} h / 睡眠分 {{ dashboard.summary.sleepScore }}</strong>
                <small>步数 {{ dashboard.summary.steps }} / {{ dashboard.summary.stepTarget }}</small>
              </article>
            </div>
          </div>

          <div class="panel macro-panel">
            <div class="panel-head compact">
              <div>
                <p class="kicker">Macro Balance</p>
                <h3>营养结构</h3>
              </div>
            </div>
            <ProgressRail
              v-for="macro in dashboard.macros"
              :key="macro.label"
              :label="macro.label"
              :value="macro.value"
              :target="macro.target"
              :unit="macro.unit"
              :tone="macro.tone"
            />
          </div>
        </div>

        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Weekly Rhythm</p>
                <h3>恢复趋势</h3>
              </div>
            </div>
            <BaseChart :option="trendOption" />
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Signals</p>
                <h3>今天的行动提示</h3>
              </div>
            </div>
            <div class="insight-stack">
              <article v-for="item in dashboard.insights.slice(0, 3)" :key="item.title" class="insight-card">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </article>
            </div>
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'body'" class="band">
        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Body Portrait</p>
                <h3>身体画像</h3>
              </div>
            </div>
            <div class="portrait-grid">
              <article class="body-kpi">
                <span>当前体重</span>
                <strong>{{ dashboard.profile.weight }} kg</strong>
                <small>目标 {{ dashboard.profile.targetWeight }} kg</small>
              </article>
              <article class="body-kpi">
                <span>BMI</span>
                <strong>{{ dashboard.profile.bmi }}</strong>
                <small>{{ dashboard.profile.riskLevel }}</small>
              </article>
              <article class="body-kpi">
                <span>体脂率</span>
                <strong>{{ dashboard.profile.bodyFat }}%</strong>
                <small>内脏脂肪 {{ dashboard.profile.visceralFat }}</small>
              </article>
              <article class="body-kpi">
                <span>肌肉率</span>
                <strong>{{ dashboard.profile.muscleRate }}%</strong>
                <small>基础代谢 {{ dashboard.profile.basalMetabolism }} kcal</small>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Dataset Summary</p>
                <h3>建模样本说明</h3>
              </div>
            </div>
            <div class="dataset-grid">
              <article class="summary-chip">
                <span>样本量</span>
                <strong>{{ analytics.datasetSummary.sampleCount }}</strong>
              </article>
              <article class="summary-chip">
                <span>高风险样本</span>
                <strong>{{ analytics.datasetSummary.highRiskCount }}</strong>
              </article>
              <article class="summary-chip">
                <span>平均睡眠</span>
                <strong>{{ analytics.datasetSummary.avgSleepHours }} h</strong>
              </article>
              <article class="summary-chip">
                <span>平均步数</span>
                <strong>{{ analytics.datasetSummary.avgSteps }}</strong>
              </article>
            </div>
            <ul class="narrative-list">
              <li v-for="line in analytics.narrative" :key="line">{{ line }}</li>
            </ul>
          </section>
        </div>

        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Risk Scatter</p>
                <h3>睡眠与压力分布</h3>
              </div>
            </div>
            <BaseChart :option="scatterOption" />
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">BMI Risk Bands</p>
                <h3>体型区间风险率</h3>
              </div>
            </div>
            <BaseChart :option="bmiBandOption" />
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'nutrition'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Quick Add</p>
                <h3>常用餐食快捷录入</h3>
              </div>
            </div>
            <div class="meal-grid">
              <article v-for="meal in dashboard.meals" :key="meal.id" class="meal-card">
                <div>
                  <strong>{{ meal.name }}</strong>
                  <span>{{ meal.mealType }} · {{ meal.portion }}</span>
                </div>
                <small>{{ meal.calories }} kcal · P{{ meal.protein }} C{{ meal.carbs }} F{{ meal.fat }}</small>
                <button :disabled="meal.eaten" @click="consumeMeal(meal.id)">
                  {{ meal.eaten ? '已入库' : '记录到今天' }}
                </button>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Macro Review</p>
                <h3>饮食节奏提示</h3>
              </div>
            </div>
            <ProgressRail
              v-for="macro in dashboard.macros"
              :key="macro.label"
              :label="macro.label"
              :value="macro.value"
              :target="macro.target"
              :unit="macro.unit"
              :tone="macro.tone"
            />
            <div class="insight-stack">
              <article v-for="item in dashboard.insights.slice(0, 2)" :key="item.title" class="insight-card">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </article>
            </div>
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'workout'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Training Flow</p>
                <h3>今日训练计划</h3>
              </div>
            </div>
            <div class="workout-stack">
              <article v-for="plan in dashboard.workouts" :key="plan.id" class="workout-card">
                <div>
                  <strong>{{ plan.title }}</strong>
                  <span>{{ plan.category }} · {{ plan.intensity }}</span>
                </div>
                <small>{{ plan.duration }} min · 约 {{ plan.caloriesBurned }} kcal</small>
                <button :class="{ done: plan.completed }" @click="toggleWorkout(plan.id)">
                  {{ plan.completed ? '已完成' : '标记完成' }}
                </button>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Completion</p>
                <h3>执行状态</h3>
              </div>
            </div>
            <article class="summary-chip summary-chip--large">
              <span>训练时长</span>
              <strong>{{ dashboard.summary.workoutMinutes }} / {{ dashboard.summary.workoutTarget }} min</strong>
            </article>
            <article class="summary-chip summary-chip--large">
              <span>今日步数</span>
              <strong>{{ dashboard.summary.steps }} / {{ dashboard.summary.stepTarget }}</strong>
            </article>
            <article class="summary-chip summary-chip--large">
              <span>情绪分</span>
              <strong>{{ dashboard.summary.moodScore }} / 100</strong>
            </article>
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'trend'" class="band">
        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Model Compare</p>
                <h3>模型评价结果</h3>
              </div>
            </div>
            <div class="model-grid">
              <article v-for="model in analytics.models" :key="model.model" class="model-card">
                <strong>{{ model.model }}</strong>
                <div class="metric-row">
                  <span>Accuracy</span>
                  <b>{{ formatPercent(model.accuracy) }}</b>
                </div>
                <div class="metric-row">
                  <span>Precision</span>
                  <b>{{ formatPercent(model.precision) }}</b>
                </div>
                <div class="metric-row">
                  <span>Recall</span>
                  <b>{{ formatPercent(model.recall) }}</b>
                </div>
                <div class="metric-row">
                  <span>F1</span>
                  <b>{{ formatPercent(model.f1) }}</b>
                </div>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">Confusion Matrix</p>
                <h3>{{ analytics.selectedModel }}</h3>
              </div>
            </div>
            <div class="confusion-grid">
              <article v-for="cell in analytics.confusionMatrix" :key="cell.label" class="confusion-card">
                <span>{{ cell.label }}</span>
                <strong>{{ cell.value }}</strong>
              </article>
            </div>
          </section>
        </div>

        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="kicker">Trend Dashboard</p>
              <h3>体重、睡眠与压力联动</h3>
            </div>
          </div>
          <BaseChart :option="trendOption" />
        </section>
      </section>

      <section v-show="currentSection === 'assistant'" class="band">
        <div class="assistant-shell">
          <section class="panel panel--chat">
            <div class="panel-head">
              <div>
                <p class="kicker">Astra Coach</p>
                <h3>智能建议</h3>
                <p class="assistant-runtime">
                  {{ assistant.runtime.provider }} · {{ assistant.runtime.model }} · {{ assistant.runtime.status }}
                </p>
              </div>
            </div>

            <div class="message-stack">
              <article
                v-for="message in assistant.conversation"
                :key="`${message.time}-${message.content}`"
                class="message-card"
                :class="{ 'message-card--user': message.speaker === 'user' }"
              >
                <div class="message-meta">
                  <strong>{{ message.tag }}</strong>
                  <span>{{ message.title }} · {{ message.time }}</span>
                </div>
                <p>{{ message.content }}</p>
              </article>
            </div>

            <div class="prompt-row">
              <button
                v-for="prompt in assistant.quickPrompts"
                :key="prompt"
                class="prompt-button"
                @click="draftMessage = prompt"
              >
                {{ prompt }}
              </button>
            </div>

            <div class="composer">
              <textarea v-model="draftMessage" rows="3" placeholder="告诉我今天的饮食、训练或状态变化..." />
              <button @click="sendMessage" :disabled="sending || !draftMessage.trim()">发送</button>
            </div>
          </section>
        </div>
      </section>
    </main>

    <div v-else class="loading-state">正在同步健康数据...</div>
  </div>
</template>

<script setup>
import {
  Activity,
  Apple,
  BrainCircuit,
  ChartColumnBig,
  HeartPulse,
  LayoutDashboard,
} from 'lucide-vue-next';
import { computed, onMounted, ref } from 'vue';
import BaseChart from './components/BaseChart.vue';
import ProgressRail from './components/ProgressRail.vue';
import { useApi } from './composables/useApi';

const api = useApi();

const navItems = [
  { key: 'overview', label: '今日概览', desc: '看见当下状态与恢复节律', icon: LayoutDashboard },
  { key: 'body', label: '身体画像', desc: '沉淀基础数据与目标轮廓', icon: HeartPulse },
  { key: 'nutrition', label: '饮食规划', desc: '平衡摄入结构与饮水节奏', icon: Apple },
  { key: 'workout', label: '训练计划', desc: '安排训练任务与消耗反馈', icon: Activity },
  { key: 'trend', label: '追踪趋势', desc: '观察长期变化与模型表现', icon: ChartColumnBig },
  { key: 'assistant', label: '智能建议', desc: '听取适合当下的行动指引', icon: BrainCircuit },
];

const currentSection = ref('overview');
const dashboard = ref(null);
const analytics = ref(null);
const assistant = ref(null);
const draftMessage = ref('');
const sending = ref(false);

const trendOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'axis' },
  legend: {
    textStyle: { color: '#52615a' },
    top: 0,
  },
  grid: { left: 24, right: 18, top: 48, bottom: 24, containLabel: true },
  xAxis: {
    type: 'category',
    data: dashboard.value?.trends.map((item) => item.date) || [],
    axisLine: { lineStyle: { color: '#d8d6ca' } },
    axisLabel: { color: '#69766f' },
  },
  yAxis: [
    {
      type: 'value',
      name: '体重',
      axisLabel: { color: '#69766f' },
      splitLine: { lineStyle: { color: '#ece7d8' } },
    },
    {
      type: 'value',
      name: '睡眠/压力',
      axisLabel: { color: '#69766f' },
      splitLine: { show: false },
    },
  ],
  series: [
    {
      name: '体重 kg',
      type: 'line',
      smooth: true,
      data: dashboard.value?.trends.map((item) => item.weight) || [],
      lineStyle: { color: '#27465a', width: 3 },
      itemStyle: { color: '#27465a' },
    },
    {
      name: '睡眠 h',
      type: 'line',
      yAxisIndex: 1,
      smooth: true,
      data: dashboard.value?.trends.map((item) => item.sleepHours) || [],
      lineStyle: { color: '#6ea88c', width: 3 },
      itemStyle: { color: '#6ea88c' },
    },
    {
      name: '压力分',
      type: 'bar',
      yAxisIndex: 1,
      data: dashboard.value?.trends.map((item) => item.stressScore) || [],
      itemStyle: { color: '#d9bc61', borderRadius: [6, 6, 0, 0] },
      barMaxWidth: 16,
    },
  ],
}));

const scatterOption = computed(() => ({
  tooltip: {
    formatter: (params) =>
      `样本 ${params.data[3]}<br/>睡眠 ${params.data[0]} h<br/>压力 ${params.data[1]}<br/>风险 ${params.data[4]}`,
  },
  grid: { left: 20, right: 16, top: 24, bottom: 24, containLabel: true },
  xAxis: {
    type: 'value',
    name: '睡眠时长(h)',
    axisLabel: { color: '#69766f' },
    splitLine: { lineStyle: { color: '#ece7d8' } },
  },
  yAxis: {
    type: 'value',
    name: '压力分',
    axisLabel: { color: '#69766f' },
    splitLine: { lineStyle: { color: '#ece7d8' } },
  },
  series: [
    {
      type: 'scatter',
      symbolSize: (value) => Math.max(10, value[2] * 0.55),
      data:
        analytics.value?.scatterPoints.map((item) => [
          item.sleepHours,
          item.stressScore,
          item.bmi,
          item.id,
          item.riskLabel,
        ]) || [],
      itemStyle: {
        color: (params) => (params.data[4] === 'HIGH' ? '#ce8756' : '#5c8a78'),
        opacity: 0.78,
      },
    },
  ],
}));

const bmiBandOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 24, right: 16, top: 24, bottom: 24, containLabel: true },
  xAxis: {
    type: 'category',
    data: analytics.value?.bmiBands.map((item) => item.label) || [],
    axisLabel: { color: '#69766f' },
    axisLine: { lineStyle: { color: '#d8d6ca' } },
  },
  yAxis: {
    type: 'value',
    name: '高风险率 %',
    axisLabel: { color: '#69766f' },
    splitLine: { lineStyle: { color: '#ece7d8' } },
  },
  series: [
    {
      type: 'bar',
      data: analytics.value?.bmiBands.map((item) => item.highRiskRate) || [],
      barWidth: 24,
      itemStyle: {
        color: '#27465a',
        borderRadius: [6, 6, 0, 0],
      },
    },
  ],
}));

function formatPercent(value) {
  return `${(value * 100).toFixed(2)}%`;
}

async function loadAll() {
  const [dashboardData, analyticsData, assistantData] = await Promise.all([
    api.get('/api/dashboard'),
    api.get('/api/analytics'),
    api.get('/api/assistant'),
  ]);
  dashboard.value = dashboardData;
  analytics.value = analyticsData;
  assistant.value = assistantData;
}

async function consumeMeal(id) {
  dashboard.value = await api.post(`/api/dashboard/meals/${id}/consume`);
}

async function addWater(amount) {
  dashboard.value = await api.post('/api/dashboard/water', { amount });
}

async function toggleWorkout(id) {
  dashboard.value = await api.post(`/api/dashboard/workouts/${id}/toggle`);
}

async function sendMessage() {
  if (!draftMessage.value.trim()) {
    return;
  }
  sending.value = true;
  assistant.value = await api.post('/api/assistant/message', { message: draftMessage.value.trim() });
  draftMessage.value = '';
  sending.value = false;
}

onMounted(loadAll);
</script>
