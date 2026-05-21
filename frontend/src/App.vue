<template>
  <div v-if="!authReady" class="loading-state">正在检查登录状态...</div>

  <div v-else-if="!authUser" class="auth-shell">
    <section class="auth-hero">
      <div class="auth-brand">
        <span class="brand-kicker">LIGHTBALANCE</span>
        <h1>轻享健康</h1>
        <p>把健康分析、趋势追踪、账号安全和 DeepSeek 智能建议放进一个真正可持续使用的个人健康工作台。</p>
      </div>

      <div class="auth-points">
        <article>
          <strong>多账号隔离</strong>
          <span>每位用户都有独立的身体画像、饮食、训练、趋势和 AI 对话记录。</span>
        </article>
        <article>
          <strong>管理员后台</strong>
          <span>管理员可查看用户列表、账号状态，并为演示场景重置用户密码。</span>
        </article>
        <article>
          <strong>账号安全</strong>
          <span>支持注册、登录、找回密码和登录后修改密码，软件更像真实产品。</span>
        </article>
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-tabs auth-tabs--triple">
        <button :class="{ active: authMode === 'login' }" @click="authMode = 'login'">登录</button>
        <button :class="{ active: authMode === 'register' }" @click="authMode = 'register'">注册</button>
        <button :class="{ active: authMode === 'recover' }" @click="authMode = 'recover'">找回密码</button>
      </div>

      <div v-if="notice.text" class="notice-banner" :class="`notice-banner--${notice.type}`">
        <span>{{ notice.text }}</span>
        <button class="ghost-button" @click="clearNotice">知道了</button>
      </div>

      <form class="auth-form" @submit.prevent="submitAuth">
        <label class="form-field">
          <span>账号</span>
          <input v-model.trim="authForm.username" type="text" placeholder="4-24 位，小写字母 / 数字 / 下划线" />
        </label>

        <label class="form-field" v-if="authMode !== 'login'">
          <span>{{ authMode === 'register' ? '显示名称' : '显示名称校验' }}</span>
          <input
            v-model.trim="authForm.name"
            type="text"
            :placeholder="authMode === 'register' ? '例如：思明老师' : '请输入该账号当前显示名称'"
          />
        </label>

        <label class="form-field">
          <span>{{ authMode === 'recover' ? '新密码' : '密码' }}</span>
          <input
            v-model="authForm.password"
            type="password"
            :placeholder="authMode === 'recover' ? '设置新的登录密码' : '至少 6 位'"
          />
        </label>

        <button class="primary-button auth-submit" type="submit" :disabled="authBusy">
          <component :is="authMode === 'login' ? ShieldCheck : authMode === 'register' ? UserPlus : RefreshCcw" :size="16" />
          <span>
            {{
              authBusy
                ? '提交中...'
                : authMode === 'login'
                  ? '登录并进入工作台'
                  : authMode === 'register'
                    ? '注册并创建健康档案'
                    : '校验并重置密码'
            }}
          </span>
        </button>
      </form>

      <div class="auth-hint">
        <strong>演示账号</strong>
        <span>账号：admin</span>
        <span>密码：admin123</span>
      </div>
    </section>
  </div>

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <span class="brand-kicker">LIGHTBALANCE</span>
        <h1>轻享健康</h1>
        <p>健康生活分析与智能建议平台</p>
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
          <p class="kicker">健康生活分析</p>
          <h2>{{ dashboard.recoverySignal.headline }}</h2>
          <p class="subcopy">把健康记录、趋势分析、账号安全和管理员能力放进同一个工作台里，既适合演示，也更接近真实软件。</p>
        </div>

        <div class="profile-chip">
          <div class="avatar">{{ dashboard.profile.name.slice(0, 1) }}</div>
          <div>
            <strong>{{ dashboard.profile.name }}</strong>
            <span>{{ authUser.username }}</span>
          </div>
          <span class="sync-badge">{{ authUser.admin ? '管理员' : dashboard.recoverySignal.badge }}</span>
          <button class="ghost-button profile-logout" @click="logout">
            <LogOut :size="15" />
            <span>退出</span>
          </button>
        </div>
      </header>

      <div v-if="notice.text" class="notice-banner" :class="`notice-banner--${notice.type}`">
        <span>{{ notice.text }}</span>
        <button class="ghost-button" @click="clearNotice">知道了</button>
      </div>

      <section v-show="currentSection === 'overview'" class="band">
        <div class="hero-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">实时总览</p>
                <h3>今天的状态总览</h3>
              </div>
              <div class="chip-row">
                <span class="score-pill">健康评分 {{ dashboard.profile.healthScore }}</span>
                <span class="score-pill score-pill--soft">{{ dashboard.profile.riskLevel }}</span>
              </div>
            </div>

            <div class="stats-grid stats-grid--four">
              <article class="stat-tile">
                <span>今日摄入</span>
                <strong>{{ dashboard.summary.calories }} kcal</strong>
                <small>目标 {{ dashboard.summary.calorieTarget }} kcal</small>
              </article>
              <article class="stat-tile">
                <span>热量余量</span>
                <strong>{{ calorieBalanceText }}</strong>
                <small>{{ calorieBalanceHint }}</small>
              </article>
              <article class="stat-tile">
                <span>饮水进度</span>
                <strong>{{ dashboard.summary.water }} / {{ dashboard.summary.waterTarget }} ml</strong>
                <div class="mini-actions">
                  <button @click="addWater(250)" :disabled="busy.water">+250 ml</button>
                  <button @click="addWater(500)" :disabled="busy.water">+500 ml</button>
                </div>
              </article>
              <article class="stat-tile stat-tile--dark">
                <span>恢复面板</span>
                <strong>{{ dashboard.summary.sleepHours }} h / 睡眠分 {{ dashboard.summary.sleepScore }}</strong>
                <small>步数 {{ dashboard.summary.steps }} / {{ dashboard.summary.stepTarget }}</small>
              </article>
            </div>
          </section>

          <section class="panel macro-panel">
            <div class="panel-head compact">
              <div>
                <p class="kicker">营养结构</p>
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
          </section>
        </div>

        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">每日回填</p>
                <h3>今日数据回填</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent="saveCheckin">
              <label class="form-field">
                <span>睡眠时长 (h)</span>
                <input v-model.number="checkinForm.sleepHours" type="number" min="0" max="12" step="0.1" />
              </label>
              <label class="form-field">
                <span>今日步数</span>
                <input v-model.number="checkinForm.steps" type="number" min="0" step="100" />
              </label>
              <label class="form-field">
                <span>心情评分</span>
                <input v-model.number="checkinForm.moodScore" type="range" min="0" max="100" />
                <small>{{ checkinForm.moodScore }} / 100</small>
              </label>
              <label class="form-field">
                <span>压力评分</span>
                <input v-model.number="checkinForm.stressScore" type="range" min="0" max="100" />
                <small>{{ checkinForm.stressScore }} / 100</small>
              </label>
              <label class="form-field">
                <span>热量目标</span>
                <input v-model.number="checkinForm.calorieTarget" type="number" min="1200" step="50" />
              </label>
              <label class="form-field">
                <span>饮水目标</span>
                <input v-model.number="checkinForm.waterTarget" type="number" min="500" step="100" />
              </label>
              <label class="form-field">
                <span>步数目标</span>
                <input v-model.number="checkinForm.stepTarget" type="number" min="1000" step="500" />
              </label>
              <label class="form-field">
                <span>训练目标</span>
                <input v-model.number="checkinForm.workoutTarget" type="number" min="0" step="5" />
              </label>

              <div class="form-actions form-actions--full">
                <button class="primary-button" type="submit" :disabled="busy.checkin">
                  <Save :size="16" />
                  <span>{{ busy.checkin ? '保存中...' : '保存今日数据' }}</span>
                </button>
              </div>
            </form>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">行动提醒</p>
                <h3>今日行动提示</h3>
              </div>
            </div>

            <div class="insight-stack">
              <article v-for="item in dashboard.insights" :key="item.title" class="insight-card">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </article>
            </div>

            <div class="water-inline">
              <label class="form-field form-field--inline">
                <span>自定义补水</span>
                <input v-model.number="customWaterAmount" type="number" min="50" step="50" />
              </label>
              <button class="secondary-button" @click="addWater(customWaterAmount)" :disabled="busy.water">
                <Droplets :size="16" />
                <span>记一笔饮水</span>
              </button>
            </div>
          </section>
        </div>

        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="kicker">每周节律</p>
              <h3>恢复趋势</h3>
            </div>
          </div>
          <BaseChart :option="trendOption" />
        </section>
      </section>

      <section v-show="currentSection === 'body'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">身体画像</p>
                <h3>身体画像</h3>
              </div>
            </div>

            <div class="portrait-grid portrait-grid--four">
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
                <p class="kicker">画像编辑</p>
                <h3>目标与身体参数</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent="saveProfile">
              <label class="form-field">
                <span>显示名称</span>
                <input v-model="profileForm.name" type="text" />
              </label>
              <label class="form-field">
                <span>账号标识</span>
                <input v-model="profileForm.handle" type="text" />
              </label>
              <label class="form-field form-field--wide">
                <span>当前目标</span>
                <select v-model="profileForm.goal">
                  <option v-for="goal in goalTemplates" :key="goal" :value="goal">{{ goal }}</option>
                </select>
              </label>
              <label class="form-field">
                <span>当前体重</span>
                <input v-model.number="profileForm.weight" type="number" min="30" max="180" step="0.1" />
              </label>
              <label class="form-field">
                <span>目标体重</span>
                <input v-model.number="profileForm.targetWeight" type="number" min="30" max="180" step="0.1" />
              </label>
              <label class="form-field">
                <span>体脂率</span>
                <input v-model.number="profileForm.bodyFat" type="number" min="5" max="60" step="0.1" />
              </label>
              <label class="form-field">
                <span>内脏脂肪</span>
                <input v-model.number="profileForm.visceralFat" type="number" min="1" max="25" step="0.1" />
              </label>
              <label class="form-field">
                <span>肌肉率</span>
                <input v-model.number="profileForm.muscleRate" type="number" min="10" max="70" step="0.1" />
              </label>

              <div class="form-actions form-actions--full">
                <button class="primary-button" type="submit" :disabled="busy.profile">
                  <Target :size="16" />
                  <span>{{ busy.profile ? '更新中...' : '更新身体画像' }}</span>
                </button>
              </div>
            </form>
          </section>
        </div>

        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">风险散点</p>
                <h3>睡眠与压力分布</h3>
              </div>
            </div>
            <BaseChart :option="scatterOption" />
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">样本概览</p>
                <h3>建模样本摘要</h3>
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
      </section>

      <section v-show="currentSection === 'nutrition'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">快捷录入</p>
                <h3>餐食记录</h3>
              </div>
            </div>

            <div class="meal-grid">
              <article v-for="meal in dashboard.meals" :key="meal.id" class="meal-card">
                <div>
                  <strong>{{ meal.name }}</strong>
                  <span>{{ meal.mealType }} · {{ meal.portion }} · {{ meal.recommendedTime }}</span>
                </div>
                <small>{{ meal.calories }} kcal · P{{ meal.protein }} C{{ meal.carbs }} F{{ meal.fat }}</small>
                <div class="card-actions">
                  <button :disabled="meal.eaten || busy.mealConsume" @click="consumeMeal(meal.id)">
                    {{ meal.eaten ? '已记入今日' : '记录到今天' }}
                  </button>
                  <button class="danger-button" :disabled="busy.mealDelete" @click="deleteMeal(meal.id)">
                    <Trash2 :size="15" />
                    <span>删除</span>
                  </button>
                </div>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">自定义餐食</p>
                <h3>自定义餐食</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent="saveMeal">
              <label class="form-field form-field--wide">
                <span>餐食名称</span>
                <input v-model="mealForm.name" type="text" />
              </label>
              <label class="form-field">
                <span>类别</span>
                <select v-model="mealForm.mealType">
                  <option v-for="item in mealTypes" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label class="form-field">
                <span>份量</span>
                <input v-model="mealForm.portion" type="text" />
              </label>
              <label class="form-field">
                <span>热量</span>
                <input v-model.number="mealForm.calories" type="number" min="0" step="10" />
              </label>
              <label class="form-field">
                <span>蛋白质</span>
                <input v-model.number="mealForm.protein" type="number" min="0" step="1" />
              </label>
              <label class="form-field">
                <span>碳水</span>
                <input v-model.number="mealForm.carbs" type="number" min="0" step="1" />
              </label>
              <label class="form-field">
                <span>脂肪</span>
                <input v-model.number="mealForm.fat" type="number" min="0" step="1" />
              </label>
              <label class="form-field">
                <span>推荐时间</span>
                <input v-model="mealForm.recommendedTime" type="time" />
              </label>
              <label class="form-check form-check--wide">
                <input v-model="mealForm.eaten" type="checkbox" />
                <span>同时记入今日摄入</span>
              </label>

              <div class="form-actions form-actions--full">
                <button class="primary-button" type="submit" :disabled="busy.meal">
                  <Apple :size="16" />
                  <span>{{ busy.meal ? '保存中...' : '新增餐食' }}</span>
                </button>
              </div>
            </form>
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'workout'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">训练流程</p>
                <h3>训练计划</h3>
              </div>
            </div>

            <div class="workout-stack">
              <article v-for="plan in dashboard.workouts" :key="plan.id" class="workout-card">
                <div>
                  <strong>{{ plan.title }}</strong>
                  <span>{{ plan.category }} · {{ plan.intensity }}</span>
                </div>
                <small>{{ plan.duration }} min · {{ plan.caloriesBurned }} kcal</small>
                <div class="card-actions">
                  <button :class="{ done: plan.completed }" @click="toggleWorkout(plan.id)" :disabled="busy.workoutToggle">
                    {{ plan.completed ? '已完成，点击撤销' : '标记完成' }}
                  </button>
                  <button class="danger-button" :disabled="busy.workoutDelete" @click="deleteWorkout(plan.id)">
                    <Trash2 :size="15" />
                    <span>删除</span>
                  </button>
                </div>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">自定义训练</p>
                <h3>自定义训练</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent="saveWorkout">
              <label class="form-field form-field--wide">
                <span>训练标题</span>
                <input v-model="workoutForm.title" type="text" />
              </label>
              <label class="form-field">
                <span>类别</span>
                <input v-model="workoutForm.category" type="text" />
              </label>
              <label class="form-field">
                <span>强度</span>
                <select v-model="workoutForm.intensity">
                  <option v-for="item in workoutIntensities" :key="item" :value="item">{{ item }}</option>
                </select>
              </label>
              <label class="form-field">
                <span>时长</span>
                <input v-model.number="workoutForm.duration" type="number" min="5" step="5" />
              </label>
              <label class="form-field">
                <span>预计消耗</span>
                <input v-model.number="workoutForm.caloriesBurned" type="number" min="0" step="10" />
              </label>

              <div class="form-actions form-actions--full">
                <button class="primary-button" type="submit" :disabled="busy.workout">
                  <Dumbbell :size="16" />
                  <span>{{ busy.workout ? '保存中...' : '新增训练任务' }}</span>
                </button>
              </div>
            </form>
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'trend'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">趋势追踪</p>
                <h3>趋势追踪</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent="saveTrend">
              <label class="form-field">
                <span>日期</span>
                <input v-model="trendForm.recordDate" type="date" />
              </label>
              <label class="form-field">
                <span>体重</span>
                <input v-model.number="trendForm.weight" type="number" min="30" step="0.1" />
              </label>
              <label class="form-field">
                <span>睡眠</span>
                <input v-model.number="trendForm.sleepHours" type="number" min="0" max="12" step="0.1" />
              </label>
              <label class="form-field">
                <span>步数</span>
                <input v-model.number="trendForm.steps" type="number" min="0" step="100" />
              </label>
              <label class="form-field">
                <span>热量</span>
                <input v-model.number="trendForm.calories" type="number" min="0" step="10" />
              </label>
              <label class="form-field">
                <span>压力分</span>
                <input v-model.number="trendForm.stressScore" type="number" min="0" max="100" step="1" />
              </label>

              <div class="form-actions form-actions--full">
                <button class="primary-button" type="submit" :disabled="busy.trend">
                  <ChartColumnBig :size="16" />
                  <span>{{ busy.trend ? '保存中...' : '写入趋势数据' }}</span>
                </button>
              </div>
            </form>

            <div class="trend-list">
              <article v-for="item in latestTrends" :key="item.date" class="trend-item">
                <strong>{{ item.date }}</strong>
                <span>{{ item.weight }} kg · {{ item.sleepHours }} h · 压力 {{ item.stressScore }}</span>
              </article>
            </div>
          </section>
        </div>

        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">DeepSeek 趋势教练</p>
                <h3>趋势智能建议</h3>
              </div>
            </div>

            <div class="ai-advice-box">
              <p>{{ trendAdvice.advice || '还没有生成趋势建议。你可以先补录今天的数据，再让 DeepSeek 分析接下来怎么调整。' }}</p>
              <small v-if="trendAdvice.generatedAt">
                {{ trendAdvice.runtime.provider }} · {{ trendAdvice.runtime.model }} · {{ trendAdvice.generatedAt }}
              </small>
            </div>

            <button class="primary-button" @click="generateTrendAdvice" :disabled="busy.trendAdvice">
              <Sparkles :size="16" />
              <span>{{ busy.trendAdvice ? '分析中...' : '生成趋势建议' }}</span>
            </button>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">模型对比</p>
                <h3>模型评估结果</h3>
              </div>
            </div>

            <div class="model-grid">
              <article v-for="model in analytics.models" :key="model.model" class="model-card">
                <strong>{{ model.model }}</strong>
                <div class="metric-row">
                  <span>准确率</span>
                  <b>{{ formatPercent(model.accuracy) }}</b>
                </div>
                <div class="metric-row">
                  <span>精确率</span>
                  <b>{{ formatPercent(model.precision) }}</b>
                </div>
                <div class="metric-row">
                  <span>召回率</span>
                  <b>{{ formatPercent(model.recall) }}</b>
                </div>
                <div class="metric-row">
                  <span>F1 值</span>
                  <b>{{ formatPercent(model.f1) }}</b>
                </div>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">BMI 风险区间</p>
                <h3>BMI 风险区间</h3>
              </div>
            </div>
            <BaseChart :option="bmiBandOption" />
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'assistant'" class="band">
        <div class="assistant-shell">
          <section class="panel panel--chat">
            <div class="panel-head">
              <div>
                <p class="kicker">Astra 助手</p>
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
              <textarea
                v-model="draftMessage"
                rows="3"
                placeholder="告诉我今天的饮食、训练、睡眠和情绪变化，或者直接问 DeepSeek 下一步怎么调。"
              />
              <button class="primary-button" @click="sendMessage" :disabled="sending || !draftMessage.trim()">
                <Sparkles :size="16" />
                <span>{{ sending ? '发送中...' : '发送给 Astra' }}</span>
              </button>
            </div>
          </section>
        </div>
      </section>

      <section v-show="currentSection === 'account'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">账号安全</p>
                <h3>修改密码</h3>
                <p class="section-note">修改后会刷新登录令牌，当前设备会自动保持登录。</p>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent="changePassword">
              <label class="form-field form-field--wide">
                <span>当前密码</span>
                <input v-model="securityForm.currentPassword" type="password" />
              </label>
              <label class="form-field form-field--wide">
                <span>新密码</span>
                <input v-model="securityForm.newPassword" type="password" />
              </label>
              <div class="form-actions form-actions--full">
                <button class="primary-button" type="submit" :disabled="busy.security">
                  <KeyRound :size="16" />
                  <span>{{ busy.security ? '提交中...' : '更新登录密码' }}</span>
                </button>
              </div>
            </form>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">账号快照</p>
                <h3>当前账号信息</h3>
              </div>
            </div>

            <div class="dataset-grid">
              <article class="summary-chip">
                <span>用户名</span>
                <strong>{{ authUser.username }}</strong>
              </article>
              <article class="summary-chip">
                <span>显示名称</span>
                <strong>{{ dashboard.profile.name }}</strong>
              </article>
              <article class="summary-chip">
                <span>角色</span>
                <strong>{{ authUser.admin ? '管理员' : '普通用户' }}</strong>
              </article>
              <article class="summary-chip">
                <span>当前目标</span>
                <strong>{{ dashboard.profile.goal }}</strong>
              </article>
            </div>

            <div class="ai-advice-box">
              <p>找回密码入口已经放在登录页。如果用户忘记密码，可以用“账号 + 当前显示名称 + 新密码”完成重置。</p>
            </div>
          </section>
        </div>
      </section>

      <section v-if="authUser.admin" v-show="currentSection === 'admin'" class="band">
        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">管理概览</p>
                <h3>管理员后台</h3>
              </div>
              <button class="secondary-button" @click="loadAdminUsers" :disabled="busy.adminLoad">
                <RefreshCcw :size="16" />
                <span>{{ busy.adminLoad ? '刷新中...' : '刷新用户列表' }}</span>
              </button>
            </div>

            <div class="dataset-grid" v-if="adminUsers">
              <article class="summary-chip">
                <span>总用户数</span>
                <strong>{{ adminUsers.summary.totalUsers }}</strong>
              </article>
              <article class="summary-chip">
                <span>管理员</span>
                <strong>{{ adminUsers.summary.adminUsers }}</strong>
              </article>
              <article class="summary-chip">
                <span>今日活跃</span>
                <strong>{{ adminUsers.summary.activeTodayUsers }}</strong>
              </article>
            </div>
          </section>
        </div>

        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="kicker">用户列表</p>
              <h3>用户列表</h3>
            </div>
          </div>

          <div class="admin-table-wrap" v-if="adminUsers">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>账号</th>
                  <th>名称</th>
                  <th>角色</th>
                  <th>目标</th>
                  <th>评分</th>
                  <th>风险</th>
                  <th>创建时间</th>
                  <th>最近记录</th>
                  <th>餐食</th>
                  <th>训练</th>
                  <th>趋势</th>
                  <th>重置密码</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in adminUsers.users" :key="user.id">
                  <td>{{ user.username }}</td>
                  <td>{{ user.name }}</td>
                  <td>{{ user.admin ? '管理员' : '用户' }}</td>
                  <td>{{ user.goal }}</td>
                  <td>{{ user.healthScore }}</td>
                  <td>{{ user.riskLevel }}</td>
                  <td>{{ user.createdAt }}</td>
                  <td>{{ user.latestRecordDate }}</td>
                  <td>{{ user.mealCount }}</td>
                  <td>{{ user.workoutCount }}</td>
                  <td>{{ user.trendCount }}</td>
                  <td>
                    <div class="admin-reset">
                      <input v-model="adminResetDrafts[user.id]" type="password" placeholder="新密码" />
                      <button
                        class="ghost-button"
                        :disabled="busy.adminReset"
                        @click="resetUserPassword(user.id)"
                      >
                        重置
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
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
  Droplets,
  Dumbbell,
  HeartPulse,
  KeyRound,
  LayoutDashboard,
  LogOut,
  RefreshCcw,
  Save,
  ShieldCheck,
  Sparkles,
  Target,
  Trash2,
  UserPlus,
  Users,
} from 'lucide-vue-next';
import { computed, onMounted, reactive, ref } from 'vue';
import BaseChart from './components/BaseChart.vue';
import ProgressRail from './components/ProgressRail.vue';
import { useApi } from './composables/useApi';

const api = useApi();

const baseNavItems = [
  { key: 'overview', label: '今日概览', desc: '查看恢复、目标和今天的节奏', icon: LayoutDashboard },
  { key: 'body', label: '身体画像', desc: '维护体重、体脂和目标参数', icon: HeartPulse },
  { key: 'nutrition', label: '饮食规划', desc: '记录餐食、营养和饮水', icon: Apple },
  { key: 'workout', label: '训练计划', desc: '安排训练任务并追踪完成情况', icon: Activity },
  { key: 'trend', label: '趋势追踪', desc: '补录每日趋势并查看模型表现', icon: ChartColumnBig },
  { key: 'assistant', label: '智能建议', desc: '把数据交给 DeepSeek 帮你判断', icon: BrainCircuit },
  { key: 'account', label: '账号安全', desc: '修改密码并管理当前账号', icon: KeyRound },
];

const adminNavItem = { key: 'admin', label: '管理员后台', desc: '查看用户列表并重置密码', icon: Users };

const goalTemplates = ['减脂塑形', '稳定体重与作息', '提升睡眠质量', '增强体能与恢复'];
const mealTypes = ['早餐', '午餐', '晚餐', '加餐', '饮品'];
const workoutIntensities = ['轻松', '中等', '偏高', '高强度'];

const currentSection = ref('overview');
const authReady = ref(false);
const authUser = ref(null);
const authMode = ref('login');
const authBusy = ref(false);
const dashboard = ref(null);
const analytics = ref(null);
const assistant = ref(null);
const adminUsers = ref(null);
const draftMessage = ref('');
const sending = ref(false);
const customWaterAmount = ref(300);

const notice = reactive({
  type: 'success',
  text: '',
});

const authForm = reactive({
  username: '',
  password: '',
  name: '',
});

const securityForm = reactive({
  currentPassword: '',
  newPassword: '',
});

const adminResetDrafts = reactive({});

const busy = reactive({
  profile: false,
  checkin: false,
  meal: false,
  mealConsume: false,
  mealDelete: false,
  workout: false,
  workoutToggle: false,
  workoutDelete: false,
  trend: false,
  trendAdvice: false,
  water: false,
  security: false,
  adminLoad: false,
  adminReset: false,
});

const trendAdvice = reactive({
  advice: '',
  generatedAt: '',
  runtime: {
    provider: 'DeepSeek',
    model: '',
    status: '',
  },
});

const profileForm = reactive({
  name: '',
  handle: '',
  goal: goalTemplates[0],
  weight: 0,
  targetWeight: 0,
  bodyFat: 0,
  visceralFat: 0,
  muscleRate: 0,
});

const checkinForm = reactive({
  calorieTarget: 1600,
  waterTarget: 2000,
  stepTarget: 8000,
  workoutTarget: 45,
  sleepHours: 7.5,
  steps: 0,
  moodScore: 80,
  stressScore: 35,
});

const mealForm = reactive(createMealDefaults());
const workoutForm = reactive(createWorkoutDefaults());

const trendForm = reactive({
  recordDate: '',
  weight: 0,
  sleepHours: 0,
  steps: 0,
  calories: 0,
  stressScore: 0,
});

const isAdmin = computed(() => Boolean(authUser.value?.admin));

const navItems = computed(() => (isAdmin.value ? [...baseNavItems, adminNavItem] : baseNavItems));

const calorieBalance = computed(() => {
  if (!dashboard.value) return 0;
  return dashboard.value.summary.calorieTarget - dashboard.value.summary.calories;
});

const calorieBalanceText = computed(() => {
  if (calorieBalance.value > 0) return `剩余 ${calorieBalance.value} kcal`;
  if (calorieBalance.value < 0) return `超出 ${Math.abs(calorieBalance.value)} kcal`;
  return '刚好达标';
});

const calorieBalanceHint = computed(() => {
  if (calorieBalance.value > 250) return '还可以安排一顿轻食或恢复加餐';
  if (calorieBalance.value >= 0) return '今天的总量已经很接近目标';
  return '晚上建议以补水和轻活动为主';
});

const latestTrends = computed(() => (dashboard.value?.trends || []).slice(-5).reverse());

const trendOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: { trigger: 'axis' },
  legend: { textStyle: { color: '#52615a' }, top: 0 },
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
      itemStyle: { color: '#27465a', borderRadius: [6, 6, 0, 0] },
    },
  ],
}));

function createMealDefaults() {
  return {
    name: '',
    mealType: mealTypes[0],
    portion: '1 份',
    calories: 320,
    protein: 20,
    carbs: 30,
    fat: 10,
    recommendedTime: '12:30',
    eaten: true,
  };
}

function createWorkoutDefaults() {
  return {
    title: '',
    category: '功能训练',
    duration: 30,
    caloriesBurned: 180,
    intensity: workoutIntensities[1],
  };
}

function formatPercent(value) {
  return `${(value * 100).toFixed(2)}%`;
}

function showNotice(text, type = 'success') {
  notice.text = text;
  notice.type = type;
}

function clearNotice() {
  notice.text = '';
}

function resetMealForm() {
  Object.assign(mealForm, createMealDefaults());
}

function resetWorkoutForm() {
  Object.assign(workoutForm, createWorkoutDefaults());
}

function syncFormsFromDashboard() {
  if (!dashboard.value) return;

  Object.assign(profileForm, {
    name: dashboard.value.profile.name,
    handle: dashboard.value.profile.handle,
    goal: dashboard.value.profile.goal,
    weight: dashboard.value.profile.weight,
    targetWeight: dashboard.value.profile.targetWeight,
    bodyFat: dashboard.value.profile.bodyFat,
    visceralFat: dashboard.value.profile.visceralFat,
    muscleRate: dashboard.value.profile.muscleRate,
  });

  Object.assign(checkinForm, {
    calorieTarget: dashboard.value.summary.calorieTarget,
    waterTarget: dashboard.value.summary.waterTarget,
    stepTarget: dashboard.value.summary.stepTarget,
    workoutTarget: dashboard.value.summary.workoutTarget,
    sleepHours: dashboard.value.summary.sleepHours,
    steps: dashboard.value.summary.steps,
    moodScore: dashboard.value.summary.moodScore,
    stressScore: dashboard.value.summary.stressScore,
  });

  Object.assign(trendForm, {
    recordDate: dashboard.value.summary.date,
    weight: dashboard.value.profile.weight,
    sleepHours: dashboard.value.summary.sleepHours,
    steps: dashboard.value.summary.steps,
    calories: dashboard.value.summary.calories,
    stressScore: dashboard.value.summary.stressScore,
  });
}

async function loadAll() {
  try {
    const [dashboardData, analyticsData, assistantData] = await Promise.all([
      api.get('/api/dashboard'),
      api.get('/api/analytics'),
      api.get('/api/assistant'),
    ]);
    dashboard.value = dashboardData;
    analytics.value = analyticsData;
    assistant.value = assistantData;
    syncFormsFromDashboard();

    if (isAdmin.value) {
      await loadAdminUsers();
    } else {
      adminUsers.value = null;
    }
  } catch (error) {
    if (error.status === 401) {
      api.clearToken();
      authUser.value = null;
      authReady.value = true;
      showNotice('登录状态已失效，请重新登录。', 'error');
      return;
    }
    showNotice(error.message || '初始化数据失败，请刷新页面重试。', 'error');
  }
}

async function loadAdminUsers() {
  if (!isAdmin.value) return;
  busy.adminLoad = true;
  try {
    adminUsers.value = await api.get('/api/admin/users');
  } catch (error) {
    showNotice(error.message || '用户列表加载失败，请稍后再试。', 'error');
  } finally {
    busy.adminLoad = false;
  }
}

async function runDashboardAction(flag, request, successText, afterSuccess) {
  busy[flag] = true;
  try {
    dashboard.value = await request();
    syncFormsFromDashboard();
    if (dashboard.value?.profile && authUser.value) {
      authUser.value = {
        ...authUser.value,
        name: dashboard.value.profile.name,
        handle: dashboard.value.profile.handle,
        goal: dashboard.value.profile.goal,
      };
    }
    if (afterSuccess) afterSuccess();
    showNotice(successText, 'success');
  } catch (error) {
    showNotice(error.message || '操作失败，请稍后再试。', 'error');
  } finally {
    busy[flag] = false;
  }
}

async function consumeMeal(id) {
  await runDashboardAction('mealConsume', () => api.post(`/api/dashboard/meals/${id}/consume`), '餐食已经记入今日摄入。');
}

async function deleteMeal(id) {
  await runDashboardAction('mealDelete', () => api.delete(`/api/dashboard/meals/${id}`), '餐食已删除，今日营养数据已同步更新。');
}

async function addWater(amount) {
  const safeAmount = Number(amount) || 0;
  if (safeAmount <= 0) {
    showNotice('请输入大于 0 的饮水量。', 'error');
    return;
  }
  await runDashboardAction('water', () => api.post('/api/dashboard/water', { amount: safeAmount }), '补水记录已更新。');
}

async function toggleWorkout(id) {
  await runDashboardAction('workoutToggle', () => api.post(`/api/dashboard/workouts/${id}/toggle`), '训练完成状态已更新。');
}

async function deleteWorkout(id) {
  await runDashboardAction('workoutDelete', () => api.delete(`/api/dashboard/workouts/${id}`), '训练计划已删除，完成时长已同步更新。');
}

async function saveProfile() {
  await runDashboardAction('profile', () => api.post('/api/dashboard/profile', profileForm), '身体画像已更新。');
}

async function saveCheckin() {
  await runDashboardAction('checkin', () => api.post('/api/dashboard/checkin', checkinForm), '今日数据已保存。');
}

async function saveMeal() {
  await runDashboardAction(
    'meal',
    () => api.post('/api/dashboard/meals', mealForm),
    mealForm.eaten ? '餐食已新增并写入今日摄入。' : '餐食模板已新增。',
    resetMealForm
  );
}

async function saveWorkout() {
  await runDashboardAction(
    'workout',
    () => api.post('/api/dashboard/workouts', workoutForm),
    '新的训练任务已经加入计划。',
    resetWorkoutForm
  );
}

async function saveTrend() {
  await runDashboardAction('trend', () => api.post('/api/dashboard/trends', trendForm), '趋势数据已补录。');
}

async function generateTrendAdvice() {
  busy.trendAdvice = true;
  try {
    const response = await api.post('/api/assistant/trend-advice');
    Object.assign(trendAdvice, response);
    showNotice('趋势建议已生成。', 'success');
  } catch (error) {
    showNotice(error.message || '趋势建议生成失败，请稍后再试。', 'error');
  } finally {
    busy.trendAdvice = false;
  }
}

async function sendMessage() {
  if (!draftMessage.value.trim()) return;
  sending.value = true;
  try {
    assistant.value = await api.post('/api/assistant/message', { message: draftMessage.value.trim() });
    draftMessage.value = '';
  } catch (error) {
    showNotice(error.message || '智能建议发送失败，请稍后再试。', 'error');
  } finally {
    sending.value = false;
  }
}

async function changePassword() {
  busy.security = true;
  try {
    const response = await api.post('/api/auth/change-password', securityForm);
    persistAuth(response);
    securityForm.currentPassword = '';
    securityForm.newPassword = '';
    showNotice('密码已修改，新的登录令牌已经生效。', 'success');
  } catch (error) {
    showNotice(error.message || '修改密码失败，请稍后再试。', 'error');
  } finally {
    busy.security = false;
  }
}

async function resetUserPassword(userId) {
  const newPassword = adminResetDrafts[userId];
  if (!newPassword || newPassword.trim().length < 6) {
    showNotice('请先输入至少 6 位的新密码。', 'error');
    return;
  }
  busy.adminReset = true;
  try {
    await api.post(`/api/admin/users/${userId}/reset-password`, { newPassword });
    adminResetDrafts[userId] = '';
    showNotice('用户密码已由管理员重置。', 'success');
  } catch (error) {
    showNotice(error.message || '管理员重置密码失败。', 'error');
  } finally {
    busy.adminReset = false;
  }
}

function persistAuth(payload) {
  api.setToken(payload.token);
  authUser.value = payload.user;
}

async function submitAuth() {
  authBusy.value = true;
  try {
    if (authMode.value === 'recover') {
      await api.post('/api/auth/recover-password', {
        username: authForm.username,
        name: authForm.name,
        newPassword: authForm.password,
      });
      authMode.value = 'login';
      authForm.password = '';
      showNotice('密码已重置，请使用新密码登录。', 'success');
      return;
    }

    const path = authMode.value === 'login' ? '/api/auth/login' : '/api/auth/register';
    const payload = authMode.value === 'login'
      ? {
          username: authForm.username,
          password: authForm.password,
        }
      : {
          username: authForm.username,
          password: authForm.password,
          name: authForm.name,
        };
    const response = await api.post(path, payload);
    persistAuth(response);
    authReady.value = true;
    await loadAll();
    showNotice(authMode.value === 'login' ? '登录成功，欢迎回来。' : '注册成功，已为你创建初始健康档案。', 'success');
  } catch (error) {
    showNotice(error.message || '登录或注册失败，请稍后重试。', 'error');
  } finally {
    authBusy.value = false;
  }
}

async function logout() {
  try {
    await api.post('/api/auth/logout');
  } catch {
    // ignore logout failures
  } finally {
    api.clearToken();
    authUser.value = null;
    dashboard.value = null;
    analytics.value = null;
    assistant.value = null;
    adminUsers.value = null;
    authReady.value = true;
    currentSection.value = 'overview';
  }
}

async function bootstrap() {
  if (!api.getToken()) {
    authReady.value = true;
    return;
  }

  try {
    const response = await api.get('/api/auth/me');
    persistAuth(response);
    await loadAll();
  } catch {
    api.clearToken();
    authUser.value = null;
  } finally {
    authReady.value = true;
  }
}

onMounted(bootstrap);
</script>
