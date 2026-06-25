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
          <span>管理员可查看用户列表、账号状态，并在运维场景下重置用户密码。</span>
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
        <strong>测试账号</strong>
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
        <div class="workspace-hero-copy">
          <p class="kicker">健康生活分析</p>
          <h2 class="workspace-headline">
            <span class="workspace-headline__name">{{ dashboard.profile.name }}，</span>
            <span>{{ headlineSuffix }}</span>
          </h2>
          <p class="subcopy">把健康记录、趋势分析、数据处理、账号安全和智能建议收拢到同一个持续运行的健康工作台里。</p>
        </div>

        <div class="profile-chip">
          <div class="avatar">{{ dashboard.profile.name.slice(0, 1) }}</div>
          <div class="profile-meta">
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

        <div class="section-grid section-grid--wide body-shell">
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

        <div class="section-grid section-grid--single">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">开始路径</p>
                <h3>建议先补样本，再看分析，再定个人计划</h3>
              </div>
            </div>

            <div class="summary-strip summary-strip--three">
              <article v-for="item in gettingStartedCards" :key="item.title" class="summary-chip summary-chip--large">
                <span>{{ item.step }}</span>
                <strong>{{ item.title }}</strong>
                <small>{{ item.detail }}</small>
              </article>
            </div>
          </section>

          <section v-if="false" class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">风险散点</p>
                <h3>睡眠与压力分布</h3>
              </div>
            </div>
            <BaseChart :option="scatterOption" />

            <ul class="narrative-list chart-note-list">
              <li v-for="item in scatterInsights" :key="item">{{ item }}</li>
            </ul>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">数据资产中心</p>
                <h3>样本池与来源登记</h3>
              </div>
            </div>

            <div class="dataset-grid">
              <article class="summary-chip">
                <span>样本量</span>
                <strong>{{ samplePoolSummary.sampleCount }}</strong>
              </article>
              <article class="summary-chip">
                <span>高风险样本</span>
                <strong>{{ samplePoolSummary.highRiskCount }}</strong>
              </article>
              <article class="summary-chip">
                <span>高风险占比</span>
                <strong>{{ formatPercent(samplePoolSummary.highRiskRatio) }}</strong>
              </article>
              <article class="summary-chip">
                <span>特征数</span>
                <strong>{{ analytics.datasetSummary.featureCount }}</strong>
              </article>
              <article class="summary-chip">
                <span>训练 / 测试</span>
                <strong>{{ analytics.datasetSummary.trainCount }} / {{ analytics.datasetSummary.testCount }}</strong>
              </article>
              <article class="summary-chip">
                <span>平均 BMI</span>
                <strong>{{ samplePoolSummary.avgBmi }}</strong>
              </article>
            </div>

            <div class="section-grid section-grid--wide sample-workspace">
              <section class="panel panel--subtle">
                <div class="panel-head compact">
                  <div>
                    <p class="kicker">样本补充</p>
                    <h3>新增观察样本</h3>
                  </div>
                </div>

                <form class="editor-grid" @submit.prevent="addSample">
                  <label class="form-field">
                    <span>样本标签</span>
                    <input v-model.trim="sampleForm.label" type="text" placeholder="例如：久坐办公人群 A" />
                  </label>
                  <label class="form-field">
                    <span>体态分层</span>
                    <select v-model="sampleForm.bodyType">
                      <option v-for="item in bodyTypeOptions" :key="item" :value="item">{{ item }}</option>
                    </select>
                  </label>
                  <label class="form-field">
                    <span>BMI</span>
                    <input v-model.number="sampleForm.bmi" type="number" min="15" max="40" step="0.1" />
                  </label>
                  <label class="form-field">
                    <span>睡眠时长</span>
                    <input v-model.number="sampleForm.sleepHours" type="number" min="3" max="12" step="0.1" />
                  </label>
                  <label class="form-field">
                    <span>日均步数</span>
                    <input v-model.number="sampleForm.steps" type="number" min="1000" max="25000" step="100" />
                  </label>
                  <label class="form-field">
                    <span>日均饮水</span>
                    <input v-model.number="sampleForm.waterMl" type="number" min="500" max="5000" step="50" />
                  </label>
                  <label class="form-field">
                    <span>压力评分</span>
                    <input v-model.number="sampleForm.stressScore" type="number" min="0" max="100" step="1" />
                  </label>
                  <label class="form-field">
                    <span>空腹血糖</span>
                    <input v-model.number="sampleForm.glucose" type="number" min="3" max="12" step="0.1" />
                  </label>
                  <label class="form-field">
                    <span>收缩压</span>
                    <input v-model.number="sampleForm.systolic" type="number" min="80" max="180" step="1" />
                  </label>
                  <label class="form-field">
                    <span>舒张压</span>
                    <input v-model.number="sampleForm.diastolic" type="number" min="50" max="120" step="1" />
                  </label>

                  <div class="form-actions form-actions--full">
                    <button class="primary-button" type="submit">
                      <Save :size="16" />
                      <span>加入样本池</span>
                    </button>
                  </div>
                </form>
              </section>

              <section class="panel panel--subtle">
                <div class="panel-head compact">
                  <div>
                    <p class="kicker">来源登记</p>
                    <h3>样本资产与补充记录</h3>
                  </div>
                </div>

                <div class="dataset-grid">
                  <article v-for="item in descriptiveStatsExtended" :key="item.label" class="summary-chip">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                    <small>{{ item.detail }}</small>
                  </article>
                </div>
              </section>
            </div>

            <div class="source-grid">
              <article v-for="source in mergedSourceCards" :key="source.name" class="source-card">
                <div class="source-meta">
                  <strong>{{ source.name }}</strong>
                  <span>{{ source.type }} · {{ source.sampleCount }} 条</span>
                </div>
                <small>资产路径 {{ source.location }}</small>
                <p>{{ source.note }}</p>
              </article>
            </div>

            <div class="section-grid section-grid--charts">
              <section class="panel panel--subtle">
                <div class="panel-head compact">
                  <div>
                    <p class="kicker">群体结构</p>
                    <h3>各体态人群占比</h3>
                  </div>
                </div>
                <BaseChart :option="bodyTypeDistributionOption" />
              </section>

              <section class="panel panel--subtle">
                <div class="panel-head compact">
                  <div>
                    <p class="kicker">行为影响</p>
                    <h3>行为对血糖、血压、压力的影响比重</h3>
                  </div>
                </div>
                <BaseChart :option="behaviorImpactOption" />
              </section>
            </div>

            <div class="panel panel--embedded">
              <div class="panel-head compact">
                <div>
                  <p class="kicker">指标关系</p>
                  <h3>各指标之间的相互关系</h3>
                </div>
              </div>
              <BaseChart :option="metricHeatmapOption" />
            </div>

            <ul class="narrative-list">
              <li v-for="line in bodyNarrative" :key="line">{{ line }}</li>
            </ul>

            <div class="dataset-grid">
              <article v-for="item in descriptiveStatsExtended" :key="item.label" class="summary-chip">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.detail }}</small>
              </article>
            </div>

            <ul class="narrative-list chart-note-list">
              <li v-for="item in relationshipInsights" :key="item">{{ item }}</li>
            </ul>
          </section>
        </div>

        <div class="section-grid section-grid--charts">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">个人趋势</p>
                <h3>最近一周恢复变化</h3>
              </div>
            </div>

            <BaseChart :option="trendOption" />

            <ul class="narrative-list chart-note-list">
              <li v-for="item in trendChartInsights" :key="item">{{ item }}</li>
            </ul>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">分层分布</p>
                <h3>BMI 风险区间</h3>
              </div>
            </div>

            <BaseChart :option="bmiBandOption" />

            <ul class="narrative-list chart-note-list">
              <li v-for="item in bmiInsights" :key="item">{{ item }}</li>
            </ul>
          </section>
        </div>

        <div v-if="false" class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">数据工程</p>
                <h3>清洗、抽样与标准化</h3>
              </div>
            </div>

            <p class="section-note">
              {{ analytics.preprocessingAudit.tooling.runtime }} · {{ analytics.preprocessingAudit.tooling.library }}
              {{ analytics.preprocessingAudit.tooling.libraryVersion }} · {{ analytics.preprocessingAudit.tooling.scriptPath }}
            </p>

            <div class="pipeline-grid">
              <article v-for="item in processingFlow" :key="item.title" class="pipeline-card">
                <span>{{ item.stage }}</span>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </article>
            </div>

            <ul class="narrative-list">
              <li v-for="step in analytics.preprocessing.steps" :key="step">{{ step }}</li>
            </ul>

            <div class="missing-grid">
              <article v-for="item in analytics.preprocessing.missingSummary" :key="item.dataset" class="summary-chip">
                <span>{{ item.dataset }}</span>
                <strong>{{ item.rowsAfter }} / {{ item.rowsBefore }}</strong>
                <small>缺失值 {{ item.missingValuesFound }} · 删除样本 {{ item.missingRowsRemoved }}</small>
              </article>
            </div>

            <div class="quality-grid">
              <article class="summary-chip">
                <span>主样本重复检查</span>
                <strong>{{ analytics.preprocessingAudit.lifestyle.duplicateRowsFound }}</strong>
                <small>删除重复 {{ analytics.preprocessingAudit.lifestyle.duplicateRowsRemoved }} 条</small>
              </article>
              <article class="summary-chip">
                <span>主样本范围超界</span>
                <strong>{{ sumCounts(analytics.preprocessingAudit.lifestyle.rangeChecks, 'invalidCount') }}</strong>
                <small>依据业务阈值扫描无效字段</small>
              </article>
              <article class="summary-chip">
                <span>临床库缺失值</span>
                <strong>{{ analytics.preprocessingAudit.benchmark.missingValuesFound }}</strong>
                <small>删除缺失记录 {{ analytics.preprocessingAudit.benchmark.missingRowsRemoved }} 条</small>
              </article>
              <article class="summary-chip">
                <span>临床库统计异常</span>
                <strong>{{ sumCounts(analytics.preprocessingAudit.benchmark.outlierChecks, 'outlierCount') }}</strong>
                <small>保留极端样本并单独标记</small>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">特征工程</p>
                <h3>主推断引擎关键因素</h3>
              </div>
            </div>

            <BaseChart :option="featureImpactOption" />

            <div class="impact-caption" v-if="analytics.featureImportance.length">
              <strong>{{ analytics.featureImportance[0].label }}</strong>
              <p>{{ analytics.featureImportance[0].interpretation }}</p>
            </div>

            <ul class="narrative-list chart-note-list">
              <li v-for="item in featureInsights" :key="item">{{ item }}</li>
            </ul>
          </section>
        </div>

        <div v-if="false" class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">样本预览</p>
                <h3>入模记录切片</h3>
              </div>
            </div>

            <div class="sample-table-wrap">
              <table class="sample-table">
                <thead>
                  <tr>
                    <th>样本</th>
                    <th>睡眠</th>
                    <th>压力</th>
                    <th>BMI</th>
                    <th>步数</th>
                    <th>饮水</th>
                    <th>风险</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in samplePreviewRows" :key="row.id">
                    <td>{{ row.id }}</td>
                    <td>{{ row.sleepHours }} h</td>
                    <td>{{ row.stressScore }}</td>
                    <td>{{ row.bmi }}</td>
                    <td>{{ row.steps }}</td>
                    <td>{{ row.waterMl }} ml</td>
                    <td>
                      <span class="risk-dot" :class="row.riskLabel === 'HIGH' ? 'risk-dot--high' : 'risk-dot--low'">
                        {{ row.riskLabel === 'HIGH' ? '高风险' : '低风险' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">字段字典</p>
                <h3>主样本字段释义</h3>
              </div>
            </div>

            <div class="dictionary-table-wrap">
              <table class="dictionary-table">
                <thead>
                  <tr>
                    <th>字段</th>
                    <th>类型</th>
                    <th>单位</th>
                    <th>含义</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in analytics.preprocessingAudit.lifestyle.fieldDictionary" :key="item.key">
                    <td>{{ item.label }}</td>
                    <td>{{ item.dtype }}</td>
                    <td>{{ item.unit }}</td>
                    <td>{{ item.meaning }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <div v-if="false" class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">字段覆盖</p>
                <h3>临床基准库特征范围</h3>
              </div>
            </div>

            <div class="field-tag-grid">
              <span v-for="label in analytics.benchmark.featureLabels" :key="label" class="field-tag">
                {{ label }}
              </span>
            </div>

            <ul class="narrative-list benchmark-note-list">
              <li v-for="note in analytics.benchmark.notes" :key="note">{{ note }}</li>
            </ul>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">清洗动作</p>
                <h3>缺失值、异常值与无效字段处理</h3>
              </div>
            </div>

            <div class="audit-block">
              <strong>{{ analytics.preprocessingAudit.lifestyle.dataset }}</strong>
              <ul class="narrative-list">
                <li v-for="item in analytics.preprocessingAudit.lifestyle.actions" :key="item">{{ item }}</li>
              </ul>
            </div>

            <div class="audit-block">
              <strong>{{ analytics.preprocessingAudit.benchmark.dataset }}</strong>
              <ul class="narrative-list">
                <li v-for="item in analytics.preprocessingAudit.benchmark.actions" :key="item">{{ item }}</li>
              </ul>
            </div>
          </section>
        </div>

        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">习惯模拟</p>
                <h3>先看保持当前习惯时，身体指标会怎么变化</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent>
              <label class="form-field">
                <span>当前体重</span>
                <input v-model.number="scenarioForm.weight" type="number" min="35" max="180" step="0.1" />
              </label>
              <label class="form-field">
                <span>体脂率</span>
                <input v-model.number="scenarioForm.bodyFat" type="number" min="5" max="50" step="0.1" />
              </label>
              <label class="form-field">
                <span>日均热量</span>
                <input v-model.number="scenarioForm.calories" type="number" min="1000" max="4000" step="10" />
              </label>
              <label class="form-field">
                <span>日均步数</span>
                <input v-model.number="scenarioForm.steps" type="number" min="1000" max="30000" step="100" />
              </label>
              <label class="form-field">
                <span>睡眠时长</span>
                <input v-model.number="scenarioForm.sleepHours" type="number" min="3" max="12" step="0.1" />
              </label>
              <label class="form-field">
                <span>压力评分</span>
                <input v-model.number="scenarioForm.stressScore" type="number" min="0" max="100" step="1" />
              </label>
              <label class="form-field">
                <span>每周训练分钟</span>
                <input v-model.number="scenarioForm.workoutMinutes" type="number" min="0" max="600" step="10" />
              </label>
              <label class="form-field">
                <span>每日饮水</span>
                <input v-model.number="scenarioForm.waterMl" type="number" min="500" max="5000" step="50" />
              </label>
            </form>

            <BaseChart :option="habitForecastOption" />

            <ul class="narrative-list chart-note-list">
              <li v-for="item in forecastInsights" :key="item">{{ item }}</li>
            </ul>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">目标反推</p>
                <h3>告诉系统你想变成什么状态</h3>
              </div>
            </div>

            <form class="editor-grid" @submit.prevent>
              <label class="form-field">
                <span>目标体重</span>
                <input v-model.number="goalScenario.targetWeight" type="number" min="35" max="180" step="0.1" />
              </label>
              <label class="form-field">
                <span>目标体脂率</span>
                <input v-model.number="goalScenario.targetBodyFat" type="number" min="5" max="45" step="0.1" />
              </label>
              <label class="form-field">
                <span>目标睡眠</span>
                <input v-model.number="goalScenario.targetSleepHours" type="number" min="5" max="10" step="0.1" />
              </label>
              <label class="form-field">
                <span>目标压力</span>
                <input v-model.number="goalScenario.targetStressScore" type="number" min="0" max="100" step="1" />
              </label>
              <label class="form-field">
                <span>目标步数</span>
                <input v-model.number="goalScenario.targetSteps" type="number" min="3000" max="20000" step="100" />
              </label>
              <label class="form-field">
                <span>计划周期</span>
                <input v-model.number="goalScenario.weeks" type="number" min="4" max="16" step="1" />
              </label>
            </form>

            <div class="plan-table-wrap">
              <table class="plan-table">
                <thead>
                  <tr>
                    <th>周次</th>
                    <th>饮食重点</th>
                    <th>训练安排</th>
                    <th>行为提醒</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in generatedPlanRows" :key="row.week">
                    <td>{{ row.week }}</td>
                    <td>{{ row.nutrition }}</td>
                    <td>{{ row.workout }}</td>
                    <td>{{ row.focus }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <ul class="narrative-list chart-note-list">
              <li v-for="item in planNarrative" :key="item">{{ item }}</li>
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
              <div class="nutrition-builder form-field--wide">
                <div class="nutrition-builder__head">
                  <div>
                    <span>智能营养估算</span>
                    <strong>输入菜名或食材后自动填充热量与三大营养素</strong>
                  </div>
                  <small>优先匹配常见蔬菜、菌菇、豆制品和基础主食，复杂菜品可在结果基础上微调。</small>
                </div>

                <div class="nutrition-toolbar">
                  <label class="form-field form-field--wide">
                    <span>菜品 / 食材名称</span>
                    <input
                      v-model.trim="nutritionForm.query"
                      type="text"
                      placeholder="例如：西兰花、番茄炒蛋、香菇豆腐、玉米"
                    />
                  </label>
                  <label class="form-field">
                    <span>估算克数</span>
                    <input v-model.number="nutritionForm.grams" type="number" min="50" step="10" />
                  </label>
                  <div class="nutrition-toolbar__actions">
                    <button
                      class="ghost-button"
                      type="button"
                      :disabled="busy.nutritionSearch"
                      @click="searchNutritionFoods"
                    >
                      <span>{{ busy.nutritionSearch ? '搜索中...' : '搜索食材' }}</span>
                    </button>
                    <button
                      class="primary-button"
                      type="button"
                      :disabled="busy.nutritionEstimate"
                      @click="estimateNutrition"
                    >
                      <Sparkles :size="16" />
                      <span>{{ busy.nutritionEstimate ? '估算中...' : '智能填充' }}</span>
                    </button>
                  </div>
                </div>

                <p v-if="nutritionSearch.sourceSummary" class="nutrition-source">{{ nutritionSearch.sourceSummary }}</p>

                <div v-if="nutritionSearch.items.length" class="nutrition-results">
                  <button
                    v-for="item in nutritionSearch.items"
                    :key="item.id"
                    type="button"
                    class="nutrition-result"
                    :class="{ active: nutritionEstimate.matchedName === item.name }"
                    @click="applySuggestion(item)"
                  >
                    <strong>{{ item.name }}</strong>
                    <span>{{ item.category }} · 每 100 g 约 {{ item.caloriesPer100g }} kcal</span>
                    <small>P {{ item.proteinPer100g }} · C {{ item.carbsPer100g }} · F {{ item.fatPer100g }}</small>
                  </button>
                </div>

                <div v-if="nutritionEstimate.matchedName" class="nutrition-estimate">
                  <div class="nutrition-estimate__summary">
                    <div>
                      <span>已匹配</span>
                      <strong>{{ nutritionEstimate.matchedName }}</strong>
                    </div>
                    <div>
                      <span>份量</span>
                      <strong>{{ nutritionEstimate.portion }}</strong>
                    </div>
                    <div>
                      <span>匹配可信度</span>
                      <strong>{{ nutritionEstimate.confidence }}</strong>
                    </div>
                  </div>
                  <div class="nutrition-estimate__metrics">
                    <article>
                      <span>热量</span>
                      <strong>{{ nutritionEstimate.calories }} kcal</strong>
                    </article>
                    <article>
                      <span>蛋白质</span>
                      <strong>{{ nutritionEstimate.protein }} g</strong>
                    </article>
                    <article>
                      <span>碳水</span>
                      <strong>{{ nutritionEstimate.carbs }} g</strong>
                    </article>
                    <article>
                      <span>脂肪</span>
                      <strong>{{ nutritionEstimate.fat }} g</strong>
                    </article>
                  </div>
                  <p>{{ nutritionEstimate.note }}</p>
                </div>

                <div v-if="nutritionEstimate.matchedName" class="nutrition-helper-tip">
                  当前保存会直接把这份估算写入餐食记录，你也可以继续修改下面的数值后再保存。
                </div>
              </div>
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

      <section v-if="false" class="band">
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
                <p class="kicker">推断引擎</p>
                <h3>模型性能矩阵</h3>
              </div>
            </div>

            <div class="model-grid">
              <article
                v-for="model in analytics.models"
                :key="model.model"
                class="model-card"
                :class="{ 'model-card--selected': model.model === analytics.selectedModel }"
              >
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
                <div class="metric-row">
                  <span>AUC</span>
                  <b>{{ formatPercent(model.auc) }}</b>
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

        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">混淆矩阵</p>
                <h3>{{ analytics.selectedModel }} 误差结构</h3>
              </div>
            </div>

            <div class="confusion-grid">
              <article v-for="item in analytics.confusionMatrix" :key="item.label" class="confusion-card">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">KNN 调参</p>
                <h3>K 值与性能变化</h3>
              </div>
            </div>

            <BaseChart :option="knnSweepOption" />
          </section>
        </div>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">跨源基准库</p>
                <h3>{{ analytics.benchmark.name }}</h3>
                <p class="section-note">{{ analytics.benchmark.source }} · 清洗产物 {{ analytics.benchmark.cleanedPath }}</p>
              </div>
            </div>

          <div class="dataset-grid">
            <article class="summary-chip">
              <span>原始样本</span>
              <strong>{{ analytics.benchmark.sampleCount }}</strong>
            </article>
            <article class="summary-chip">
              <span>清洗后样本</span>
              <strong>{{ analytics.benchmark.usableSampleCount }}</strong>
            </article>
            <article class="summary-chip">
              <span>阳性占比</span>
              <strong>{{ formatPercent(analytics.benchmark.positiveRate) }}</strong>
            </article>
            <article class="summary-chip">
              <span>公开特征数</span>
              <strong>{{ analytics.benchmark.featureCount }}</strong>
            </article>
          </div>

          <div class="section-grid">
            <section class="benchmark-shell">
              <div class="model-grid">
                <article
                  v-for="model in analytics.benchmark.models"
                  :key="model.model"
                  class="model-card"
                  :class="{ 'model-card--selected': model.model === analytics.benchmark.selectedModel }"
                >
                  <strong>{{ model.model }}</strong>
                  <div class="metric-row">
                    <span>准确率</span>
                    <b>{{ formatPercent(model.accuracy) }}</b>
                  </div>
                  <div class="metric-row">
                    <span>召回率</span>
                    <b>{{ formatPercent(model.recall) }}</b>
                  </div>
                  <div class="metric-row">
                    <span>F1 值</span>
                    <b>{{ formatPercent(model.f1) }}</b>
                  </div>
                  <div class="metric-row">
                    <span>AUC</span>
                    <b>{{ formatPercent(model.auc) }}</b>
                  </div>
                </article>
              </div>
            </section>

            <section class="benchmark-shell">
              <ul class="narrative-list">
                <li v-for="step in analytics.benchmark.preprocessing" :key="step">{{ step }}</li>
              </ul>
            </section>
          </div>
        </section>
      </section>

      <section v-show="currentSection === 'trend'" class="band">
        <div class="section-grid section-grid--wide">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">趋势记录</p>
                <h3>补录今天的身体状态</h3>
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

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">趋势教练</p>
                <h3>本周调整建议</h3>
              </div>
            </div>

            <div class="ai-advice-box">
              <p>{{ trendAdviceText }}</p>
              <small v-if="trendAdvice.generatedAt">最近更新 {{ trendAdvice.generatedAt }}</small>
            </div>

            <button class="primary-button" @click="generateTrendAdvice" :disabled="busy.trendAdvice">
              <Sparkles :size="16" />
              <span>{{ busy.trendAdvice ? '分析中...' : '刷新本周建议' }}</span>
            </button>

            <div class="panel-head panel-head--spaced">
              <div>
                <p class="kicker">立即可做</p>
                <h3>今天优先处理的事</h3>
              </div>
            </div>

            <div class="dataset-grid">
              <article v-for="item in trendPriorityCards" :key="item.label" class="summary-chip">
                <span>{{ item.label }}</span>
                <strong>{{ item.current }}</strong>
                <small>{{ item.detail }}</small>
                <p class="summary-chip__hint">{{ item.hint }}</p>
              </article>
            </div>
          </section>
        </div>

        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">恢复趋势</p>
                <h3>最近记录变化</h3>
              </div>
            </div>
            <BaseChart :option="trendOption" />
          </section>

          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">本周摘要</p>
                <h3>你最近 7 天的节奏</h3>
              </div>
            </div>

            <div class="dataset-grid">
              <article v-for="item in trendWeeklyDigest" :key="item.label" class="summary-chip">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.detail }}</small>
              </article>
            </div>

            <ul class="narrative-list">
              <li v-for="item in trendRiskNotes" :key="item">{{ item }}</li>
            </ul>
          </section>
        </div>

        <div class="section-grid">
          <section class="panel">
            <div class="panel-head">
              <div>
                <p class="kicker">可执行建议</p>
                <h3>把建议落实到今天</h3>
              </div>
            </div>

            <div class="insight-stack">
              <article v-for="item in trendActionInsights" :key="item.title" class="insight-card">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </article>
            </div>
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
  { key: 'trend', label: '趋势追踪', desc: '补录每日趋势并查看风险变化', icon: ChartColumnBig },
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
  nutritionSearch: false,
  nutritionEstimate: false,
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
const nutritionForm = reactive({
  query: '',
  grams: 150,
});
const nutritionSearch = reactive({
  sourceSummary: '',
  items: [],
});
const nutritionEstimate = reactive(createNutritionEstimateDefaults());
const workoutForm = reactive(createWorkoutDefaults());

const trendForm = reactive({
  recordDate: '',
  weight: 0,
  sleepHours: 0,
  steps: 0,
  calories: 0,
  stressScore: 0,
});

const bodyTypeOptions = ['偏瘦', '标准', '超重', '肥胖'];

const sampleForm = reactive(createSampleDefaults());
const customSamples = ref([]);

const scenarioForm = reactive({
  weight: 0,
  bodyFat: 0,
  calories: 1800,
  steps: 8000,
  sleepHours: 7.2,
  stressScore: 40,
  workoutMinutes: 180,
  waterMl: 2000,
});

const goalScenario = reactive({
  targetWeight: 0,
  targetBodyFat: 0,
  targetSleepHours: 7.8,
  targetStressScore: 32,
  targetSteps: 10000,
  weeks: 8,
});

resetMealForm();

const isAdmin = computed(() => Boolean(authUser.value?.admin));

const navItems = computed(() => (isAdmin.value ? [...baseNavItems, adminNavItem] : baseNavItems));

const headlineSuffix = computed(() => {
  if (!dashboard.value) return '';
  const profileName = dashboard.value.profile.name || '';
  const headline = dashboard.value.recoverySignal.headline || '';
  const prefix = `${profileName}，`;
  if (headline.startsWith(prefix)) {
    return headline.slice(prefix.length);
  }
  return headline;
});

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

const recentTrendWindow = computed(() => (dashboard.value?.trends || []).slice(-7));

const trendAdviceText = computed(() => {
  const advice = trendAdvice.advice?.trim();
  if (!advice) {
    return '还没有生成建议。补录今天的数据后，可以让系统结合近 7 天节奏给出更具体的调整方向。';
  }
  if (/Permission denied|getsockopt|请求失败|错误信息|暂时不可用/i.test(advice)) {
    return '智能建议暂时未返回结果。你可以先按照下面的优先事项调整，稍后再重新生成。';
  }
  return advice;
});

const trendPriorityCards = computed(() => {
  if (!dashboard.value) return [];

  const summary = dashboard.value.summary;
  const sleepGap = Math.max(0, 8 - summary.sleepHours);
  const stepGap = Math.max(0, summary.stepTarget - summary.steps);
  const waterGap = Math.max(0, summary.waterTarget - summary.water);
  const workoutGap = Math.max(0, summary.workoutTarget - summary.workoutMinutes);

  return [
    {
      label: '睡眠',
      current: `${summary.sleepHours.toFixed(1)} h`,
      detail: sleepGap > 0 ? `距离 8 h 恢复线还差 ${sleepGap.toFixed(1)} h` : '已达到恢复睡眠时长',
      hint: sleepGap > 0 ? '今晚尽量提前入睡，先补睡眠再追训练强度。' : '保持当前作息，避免熬夜反弹。',
    },
    {
      label: '步数',
      current: `${summary.steps}`,
      detail: stepGap > 0 ? `距离今日目标还差 ${stepGap} 步` : '今天的活动量已经达标',
      hint: stepGap > 0 ? '优先补一段 15 到 20 分钟快走，比临时高强度更稳。' : '可以用轻松散步收尾，帮助恢复。',
    },
    {
      label: '饮水',
      current: `${summary.water} ml`,
      detail: waterGap > 0 ? `距离今日目标还差 ${waterGap} ml` : '今日饮水已达标',
      hint: waterGap > 0 ? '把剩余水量分成 2 到 3 次喝完，不要集中猛灌。' : '继续少量多次补水，维持状态。',
    },
    {
      label: '活动时长',
      current: `${summary.workoutMinutes} min`,
      detail: workoutGap > 0 ? `距离今日目标还差 ${workoutGap} min` : '今天的训练时长已经够了',
      hint: workoutGap > 0 ? '如果精神一般，先补低强度活动，不必硬撑大强度。' : '晚上以拉伸或放松为主，避免过度训练。',
    },
  ];
});

const trendWeeklyDigest = computed(() => {
  const trends = recentTrendWindow.value;
  if (!trends.length) return [];

  const first = trends[0];
  const last = trends[trends.length - 1];
  const avgSleep = trends.reduce((sum, item) => sum + item.sleepHours, 0) / trends.length;
  const avgSteps = Math.round(trends.reduce((sum, item) => sum + item.steps, 0) / trends.length);
  const avgStress = Math.round(trends.reduce((sum, item) => sum + item.stressScore, 0) / trends.length);
  const weightDelta = last.weight - first.weight;
  const stressDelta = last.stressScore - first.stressScore;

  return [
    {
      label: '体重变化',
      value: `${weightDelta > 0 ? '+' : ''}${weightDelta.toFixed(1)} kg`,
      detail: `从 ${first.weight} kg 到 ${last.weight} kg`,
    },
    {
      label: '平均睡眠',
      value: `${avgSleep.toFixed(1)} h`,
      detail: avgSleep >= 7.5 ? '恢复节奏比较稳定' : '睡眠仍偏少，值得优先修复',
    },
    {
      label: '平均步数',
      value: `${avgSteps}`,
      detail: `最近 ${trends.length} 天的日均活动量`,
    },
    {
      label: '压力变化',
      value: `${stressDelta > 0 ? '+' : ''}${stressDelta}`,
      detail: `最近平均压力 ${avgStress} 分`,
    },
  ];
});

const trendRiskNotes = computed(() => {
  if (!dashboard.value) return [];

  const summary = dashboard.value.summary;
  const notes = [];

  if (summary.sleepHours < 7) {
    notes.push(`睡眠只有 ${summary.sleepHours.toFixed(1)} 小时，恢复不足会直接影响食欲、训练质量和第二天压力感。`);
  }
  if (summary.stressScore >= 70) {
    notes.push(`压力分达到 ${summary.stressScore}，今天更适合降强度、早点结束刺激性活动。`);
  }
  if (summary.steps < summary.stepTarget * 0.6) {
    notes.push(`当前步数只完成了约 ${Math.round((summary.steps / summary.stepTarget) * 100)}%，建议补一次轻快步行。`);
  }
  if (summary.water < summary.waterTarget * 0.7) {
    notes.push(`饮水量还偏低，补足水分通常比额外喝咖啡更能改善疲劳感。`);
  }

  if (!notes.length) {
    notes.push('最近状态总体平稳，接下来重点是保持规律作息，不要因为状态好就突然加量。');
  }

  return notes;
});

const trendActionInsights = computed(() => {
  if (!dashboard.value) return [];

  const summary = dashboard.value.summary;
  const actions = [];

  actions.push(
    {
      title: '把晚上节奏收下来',
      description:
        summary.sleepHours < 7
          ? '今晚尽量把睡前 1 小时留给洗漱、拉伸和放松，先把睡眠补回来。'
          : '今晚继续维持当前作息，把稳定节奏当作第一目标。',
    },
    {
      title: '给身体补一次轻活动',
      description:
        summary.steps < summary.stepTarget
          ? '饭后加一段 15 到 20 分钟步行，通常就能明显缩小今天的活动差距。'
          : '今天活动量已经不错，后半天以放松活动和拉伸收尾更合适。',
    },
    {
      title: '把水分分段补齐',
      description:
        summary.water < summary.waterTarget
          ? '把剩余水量拆成几次喝完，搭配正常进餐，避免临睡前一次性补太多。'
          : '饮水已经达标，继续少量多次维持就够了。',
    },
  );

  return actions;
});

const processingFlow = computed(() => {
  if (!analytics.value) return [];

  const sources = analytics.value.datasetSummary?.sources || [];
  const missingSummary = analytics.value.preprocessing?.missingSummary || [];
  const benchmark = analytics.value.benchmark;

  return [
    {
      stage: 'Stage 01',
      title: '数据接入',
      description: `整合 ${sources.length} 路数据资产，当前覆盖 ${analytics.value.datasetSummary.sampleCount} 条行为样本与 ${benchmark?.sampleCount || 0} 条外部基准记录。`,
    },
    {
      stage: 'Stage 02',
      title: '质量筛查',
      description: `缺失值扫描共覆盖 ${missingSummary.length} 个数据集，外部基准库最终保留 ${benchmark?.usableSampleCount || 0} 条可用记录。`,
    },
    {
      stage: 'Stage 03',
      title: '特征对齐',
      description: `统一整理 ${analytics.value.datasetSummary.featureCount} 个主特征，并保持训练集 ${analytics.value.datasetSummary.trainCount} / 测试集 ${analytics.value.datasetSummary.testCount} 的分层抽样结构。`,
    },
    {
      stage: 'Stage 04',
      title: '推断输出',
      description: `主推断引擎当前选择 ${analytics.value.selectedModel}，并同步产出混淆矩阵、AUC 与特征影响强度。`,
    },
  ];
});

const samplePreviewRows = computed(() => {
  if (!analytics.value?.scatterPoints?.length) return [];
  return analytics.value.scatterPoints.slice(0, 8).map((row) => ({
    id: row.id,
    sleepHours: row.sleepHours,
    stressScore: row.stressScore,
    bmi: row.bmi,
    steps: row.steps,
    waterMl: row.waterMl,
    riskLabel: row.riskLabel,
  }));
});

const descriptiveStats = computed(() => {
  if (!analytics.value?.scatterPoints?.length) return [];

  const rows = analytics.value.scatterPoints;
  const sleepValues = rows.map((item) => item.sleepHours);
  const stressValues = rows.map((item) => item.stressScore);
  const stepValues = rows.map((item) => item.steps);
  const waterValues = rows.map((item) => item.waterMl);

  return [
    {
      label: '平均睡眠',
      value: `${analytics.value.datasetSummary.avgSleepHours.toFixed(1)} h`,
      detail: `样本范围 ${formatNumber(Math.min(...sleepValues), 1)} 到 ${formatNumber(Math.max(...sleepValues), 1)} h`,
    },
    {
      label: '平均压力',
      value: `${formatNumber(analytics.value.datasetSummary.avgStressScore, 1)} 分`,
      detail: `样本范围 ${Math.min(...stressValues)} 到 ${Math.max(...stressValues)} 分`,
    },
    {
      label: '平均步数',
      value: `${analytics.value.datasetSummary.avgSteps}`,
      detail: `样本范围 ${Math.min(...stepValues)} 到 ${Math.max(...stepValues)} 步`,
    },
    {
      label: '平均饮水',
      value: `${Math.round(waterValues.reduce((sum, value) => sum + value, 0) / waterValues.length)} ml`,
      detail: `样本范围 ${Math.min(...waterValues)} 到 ${Math.max(...waterValues)} ml`,
    },
  ];
});

const correlationInsights = computed(() => {
  if (!analytics.value?.scatterPoints?.length) return [];

  const rows = analytics.value.scatterPoints;
  const sleepValues = rows.map((item) => item.sleepHours);
  const stressValues = rows.map((item) => item.stressScore);
  const stepValues = rows.map((item) => item.steps);
  const bmiValues = rows.map((item) => item.bmi);
  const highRiskRows = rows.filter((item) => item.riskLabel === 'HIGH');
  const lowRiskRows = rows.filter((item) => item.riskLabel !== 'HIGH');

  const sleepStressCorr = correlation(sleepValues, stressValues);
  const stepsBmiCorr = correlation(stepValues, bmiValues);
  const highRiskSleep = average(highRiskRows.map((item) => item.sleepHours));
  const lowRiskSleep = average(lowRiskRows.map((item) => item.sleepHours));

  return [
    `睡眠与压力的相关系数为 ${formatNumber(sleepStressCorr, 2)}，说明睡眠越少的样本通常压力越高。`,
    `步数与 BMI 的相关系数为 ${formatNumber(stepsBmiCorr, 2)}，反映活动量更高的样本 BMI 往往更稳定。`,
    `高风险样本平均睡眠 ${formatNumber(highRiskSleep, 1)} 小时，低风险样本为 ${formatNumber(lowRiskSleep, 1)} 小时，二者存在明显差距。`,
  ];
});

const scatterInsights = computed(() => {
  if (!analytics.value?.scatterPoints?.length) return [];

  const rows = analytics.value.scatterPoints;
  const highStressLowSleep = rows.filter((item) => item.sleepHours < 6.5 && item.stressScore >= 65).length;
  const lowStressEnoughSleep = rows.filter((item) => item.sleepHours >= 7.5 && item.stressScore < 50).length;

  return [
    `散点图横轴为睡眠时长、纵轴为压力评分，点越靠左上代表“睡得少但压力高”的风险组合。`,
    `当前样本中有 ${highStressLowSleep} 条记录集中在高压低睡眠区域，而处于“睡眠充足且压力较低”区域的记录有 ${lowStressEnoughSleep} 条。`,
  ];
});

const featureInsights = computed(() => {
  if (!analytics.value?.featureImportance?.length) return [];

  const topFeatures = analytics.value.featureImportance.slice(0, 3);
  return [
    `特征重要性图展示的是各变量对风险判断的影响强度，横轴数值越大，说明该指标越值得重点关注。`,
    `当前影响最大的前三项分别是 ${topFeatures.map((item) => item.label).join('、')}，和生活方式干预最直接相关。`,
  ];
});

const trendChartInsights = computed(() => {
  if (!dashboard.value?.trends?.length) return [];

  const trends = dashboard.value.trends;
  const first = trends[0];
  const last = trends[trends.length - 1];
  const sleepDelta = last.sleepHours - first.sleepHours;
  const stressDelta = last.stressScore - first.stressScore;

  return [
    `这张图同时展示体重、睡眠和压力的变化，可以观察恢复状态是否在同步改善。`,
    `最近记录中，睡眠变化 ${sleepDelta >= 0 ? '+' : ''}${formatNumber(sleepDelta, 1)} 小时，压力变化 ${stressDelta >= 0 ? '+' : ''}${stressDelta} 分。`,
  ];
});

const bmiInsights = computed(() => {
  if (!analytics.value?.bmiBands?.length) return [];

  const bands = analytics.value.bmiBands;
  const highest = [...bands].sort((left, right) => right.highRiskRate - left.highRiskRate)[0];
  const lowest = [...bands].sort((left, right) => left.highRiskRate - right.highRiskRate)[0];

  return [
    `横轴是 BMI 区间，纵轴是该区间内的高风险占比，用来观察不同体型层级的风险差异。`,
    `当前高风险占比最高的是 ${highest.label} 区间，最低的是 ${lowest.label} 区间，说明风险并不是平均分布的。`,
  ];
});

const enrichedBaseSamples = computed(() =>
  (analytics.value?.scatterPoints || []).map((item) => enrichSample(item))
);

const sampleRows = computed(() => [...enrichedBaseSamples.value, ...customSamples.value]);

const samplePoolSummary = computed(() => {
  const rows = sampleRows.value;
  const highRiskCount = rows.filter((item) => item.riskLabel === 'HIGH').length;
  return {
    sampleCount: rows.length,
    highRiskCount,
    highRiskRatio: rows.length ? highRiskCount / rows.length : 0,
    avgBmi: formatNumber(average(rows.map((item) => item.bmi)), 1),
  };
});

const mergedSourceCards = computed(() => {
  const sources = analytics.value?.datasetSummary?.sources || [];
  return [
    ...sources,
    {
      name: '用户扩展样本池',
      type: '手工补充',
      location: 'workspace://custom-samples',
      sampleCount: customSamples.value.length,
      note: customSamples.value.length
        ? `本轮已新增 ${customSamples.value.length} 条样本，用于观察群体结构与关系变化。`
        : '可在此页直接补充典型人群样本，让分析视角更贴近你的项目场景。',
    },
  ];
});

const descriptiveStatsExtended = computed(() => {
  const rows = sampleRows.value;
  if (!rows.length) return [];
  return [
    {
      label: '平均睡眠',
      value: `${formatNumber(average(rows.map((item) => item.sleepHours)), 1)} h`,
      detail: `范围 ${formatNumber(Math.min(...rows.map((item) => item.sleepHours)), 1)} - ${formatNumber(Math.max(...rows.map((item) => item.sleepHours)), 1)} h`,
    },
    {
      label: '平均血糖',
      value: `${formatNumber(average(rows.map((item) => item.glucose)), 1)} mmol/L`,
      detail: `高于 6.1 的样本 ${rows.filter((item) => item.glucose >= 6.1).length} 条`,
    },
    {
      label: '平均收缩压',
      value: `${Math.round(average(rows.map((item) => item.systolic)))} mmHg`,
      detail: `舒张压均值 ${Math.round(average(rows.map((item) => item.diastolic)))} mmHg`,
    },
    {
      label: '平均压力',
      value: `${Math.round(average(rows.map((item) => item.stressScore)))}`,
      detail: `高压样本 ${rows.filter((item) => item.stressScore >= 65).length} 条`,
    },
  ];
});

const gettingStartedCards = computed(() => [
  {
    step: 'Step 1',
    title: '补几个典型样本',
    detail: '先把目标人群、极端生活方式或你想展示的案例加进样本池，图表会立刻跟着变化。',
  },
  {
    step: 'Step 2',
    title: '看群体分布和关系',
    detail: '重点看体态占比、行为对血糖血压压力的影响比重，以及指标相关矩阵。',
  },
  {
    step: 'Step 3',
    title: '再去做个人趋势模拟',
    detail: '把自己的习惯填进去，先看不改变时会怎么走，再根据目标反推出计划表。',
  },
]);

const bodyTypeDistributionOption = computed(() => {
  const counts = bodyTypeOptions.map((label) => ({
    name: label,
    value: sampleRows.value.filter((item) => item.bodyType === label).length,
  }));
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#52615a' } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        data: counts,
        label: { color: '#52615a', formatter: '{b}\n{d}%' },
        itemStyle: {
          color: (params) => ['#6ea88c', '#27465a', '#d9bc61', '#c57a55'][params.dataIndex % 4],
        },
      },
    ],
  };
});

const behaviorImpactOption = computed(() => {
  const behaviors = [
    { key: 'sleepHours', label: '睡眠' },
    { key: 'steps', label: '步数' },
    { key: 'waterMl', label: '饮水' },
    { key: 'bmi', label: 'BMI' },
  ];
  const outcomes = [
    { key: 'glucose', label: '血糖', color: '#c57a55' },
    { key: 'systolic', label: '血压', color: '#27465a' },
    { key: 'stressScore', label: '压力', color: '#6ea88c' },
  ];
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0, textStyle: { color: '#52615a' } },
    grid: { left: 28, right: 18, top: 48, bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: behaviors.map((item) => item.label),
      axisLabel: { color: '#69766f' },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#69766f', formatter: (value) => `${Math.round(value * 100)}%` },
      splitLine: { lineStyle: { color: '#ece7d8' } },
      max: 1,
    },
    series: outcomes.map((outcome) => ({
      name: outcome.label,
      type: 'bar',
      stack: outcome.label,
      barMaxWidth: 26,
      itemStyle: { color: outcome.color, borderRadius: [8, 8, 0, 0] },
      data: normalizeImpactSeries(
        behaviors.map((behavior) =>
          Math.abs(correlation(sampleRows.value.map((item) => Number(item[behavior.key])), sampleRows.value.map((item) => Number(item[outcome.key]))))
        )
      ),
    })),
  };
});

const metricHeatmapOption = computed(() => {
  const metrics = [
    { key: 'bmi', label: 'BMI' },
    { key: 'sleepHours', label: '睡眠' },
    { key: 'steps', label: '步数' },
    { key: 'waterMl', label: '饮水' },
    { key: 'stressScore', label: '压力' },
    { key: 'glucose', label: '血糖' },
    { key: 'systolic', label: '收缩压' },
  ];
  const rows = [];
  metrics.forEach((left, rowIndex) => {
    metrics.forEach((right, colIndex) => {
      rows.push([
        colIndex,
        rowIndex,
        Number(formatNumber(correlation(sampleRows.value.map((item) => Number(item[left.key])), sampleRows.value.map((item) => Number(item[right.key]))), 2)),
      ]);
    });
  });
  return {
    tooltip: {
      formatter: (params) => `${metrics[params.data[1]].label} / ${metrics[params.data[0]].label}: ${params.data[2]}`,
    },
    grid: { left: 70, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: metrics.map((item) => item.label), axisLabel: { color: '#69766f' } },
    yAxis: { type: 'category', data: metrics.map((item) => item.label), axisLabel: { color: '#69766f' } },
    visualMap: {
      min: -1,
      max: 1,
      calculable: false,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: { color: ['#4d7a93', '#f7f3e8', '#c67c58'] },
    },
    series: [
      {
        type: 'heatmap',
        data: rows,
        label: { show: true, color: '#24332f', formatter: ({ data }) => data[2] },
      },
    ],
  };
});

const bodyNarrative = computed(() => {
  const rows = sampleRows.value;
  if (!rows.length) return [];
  const highGlucose = rows.filter((item) => item.glucose >= 6.1).length;
  const highBp = rows.filter((item) => item.systolic >= 135).length;
  return [
    `当前样本池共有 ${rows.length} 条记录，其中高风险样本 ${samplePoolSummary.value.highRiskCount} 条，占比 ${formatPercent(samplePoolSummary.value.highRiskRatio)}。`,
    `补充样本后可以直观看到体态结构与行为关系的变化，适合在答辩时演示“新增案例如何影响分析结论”。`,
    `目前空腹血糖偏高样本 ${highGlucose} 条，收缩压偏高样本 ${highBp} 条，可结合热力图解释哪些行为变量最值得优先干预。`,
  ];
});

const relationshipInsights = computed(() => [
  `睡眠、步数、饮水与压力/血糖/血压的相关方向可以直接从热力图读出：接近 -1 代表负相关，接近 1 代表正相关。`,
  `如果你补充了久坐高压或高 BMI 样本，通常会看到 BMI 与血压、血糖的正相关增强，而睡眠与压力的负相关更明显。`,
  `这部分可以直接支撑“各指标之间的相互关系”与“行为影响解释”两个展示点。`,
]);

const habitForecastSeries = computed(() => {
  const weeks = Math.max(4, goalScenario.weeks);
  const labels = Array.from({ length: weeks }, (_, index) => `第${index + 1}周`);
  const points = labels.map((label, index) => simulateWeek(index + 1));
  return { labels, points };
});

const habitForecastOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { top: 0, textStyle: { color: '#52615a' } },
  grid: { left: 28, right: 18, top: 48, bottom: 24, containLabel: true },
  xAxis: { type: 'category', data: habitForecastSeries.value.labels, axisLabel: { color: '#69766f' } },
  yAxis: [
    { type: 'value', name: '体重/体脂', axisLabel: { color: '#69766f' }, splitLine: { lineStyle: { color: '#ece7d8' } } },
    { type: 'value', name: '压力/血糖', axisLabel: { color: '#69766f' }, splitLine: { show: false } },
  ],
  series: [
    {
      name: '体重 kg',
      type: 'line',
      smooth: true,
      data: habitForecastSeries.value.points.map((item) => item.weight),
      lineStyle: { color: '#27465a', width: 3 },
      itemStyle: { color: '#27465a' },
    },
    {
      name: '体脂率 %',
      type: 'line',
      smooth: true,
      data: habitForecastSeries.value.points.map((item) => item.bodyFat),
      lineStyle: { color: '#d9bc61', width: 3 },
      itemStyle: { color: '#d9bc61' },
    },
    {
      name: '压力评分',
      type: 'line',
      yAxisIndex: 1,
      smooth: true,
      data: habitForecastSeries.value.points.map((item) => item.stressScore),
      lineStyle: { color: '#6ea88c', width: 3 },
      itemStyle: { color: '#6ea88c' },
    },
    {
      name: '血糖 mmol/L',
      type: 'line',
      yAxisIndex: 1,
      smooth: true,
      data: habitForecastSeries.value.points.map((item) => item.glucose),
      lineStyle: { color: '#c57a55', width: 3 },
      itemStyle: { color: '#c57a55' },
    },
  ],
}));

const forecastInsights = computed(() => {
  const points = habitForecastSeries.value.points;
  if (!points.length) return [];
  const last = points[points.length - 1];
  return [
    `如果保持当前习惯 ${goalScenario.weeks} 周，体重大约会走到 ${formatNumber(last.weight, 1)} kg，体脂率约 ${formatNumber(last.bodyFat, 1)}%。`,
    `当前习惯下预测血糖约 ${formatNumber(last.glucose, 1)} mmol/L，压力评分约 ${Math.round(last.stressScore)}，适合先判断“如果不调整会怎样”。`,
    `这里的趋势是基于当前摄入、步数、睡眠、饮水和训练量做的近似模拟，后续每天记录后，这条曲线会继续动态变化。`,
  ];
});

const generatedPlanRows = computed(() => {
  const weeks = Math.max(4, goalScenario.weeks);
  const calorieGap = scenarioForm.calories - idealCalories();
  const stepGap = goalScenario.targetSteps - scenarioForm.steps;
  return Array.from({ length: weeks }, (_, index) => ({
    week: `第 ${index + 1} 周`,
    nutrition: calorieGap > 0
      ? `把日均热量下调 ${Math.min(220, Math.max(80, Math.round(calorieGap / weeks)))} kcal，优先减夜宵与精制碳水。`
      : `维持当前热量结构，蛋白质占比再提高一点，保证恢复与饱腹感。`,
    workout: stepGap > 0
      ? `把周训练保持在 ${Math.max(180, scenarioForm.workoutMinutes)} 分钟，并把日均步数逐步补到 ${goalScenario.targetSteps}。`
      : `保持现有训练量，增加 1 次低强度恢复训练，避免体能和压力同时上升。`,
    focus: index < 2
      ? '先稳定睡眠和补水，再逐步提步数。'
      : index < weeks - 2
        ? '让训练和饮食节奏稳定，不要忽高忽低。'
        : '观察体重、体脂和压力是否贴近目标，再做微调。',
  }));
});

const planNarrative = computed(() => [
  `系统会先根据你现在的习惯画出“自然演化趋势”，再根据目标反推出每周计划，这样用户更容易理解为什么要做这些调整。`,
  `如果目标体重、体脂和压力都要明显改善，最先优先的是睡眠稳定、热量控制和步数补齐，而不是一开始就上很高强度。`,
  `后续你每天记录饮食和训练时，可以把实际变化和这条预测曲线对比，形成“预测 - 干预 - 回看”的闭环。`,
]);

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

const featureImpactOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    formatter: (params) => {
      const item = params[0];
      return `${item.name}<br/>权重强度 ${item.value}`;
    },
  },
  grid: { left: 80, right: 20, top: 20, bottom: 20, containLabel: true },
  xAxis: {
    type: 'value',
    name: '影响强度',
    axisLabel: { color: '#69766f' },
    splitLine: { lineStyle: { color: '#ece7d8' } },
  },
  yAxis: {
    type: 'category',
    data: (analytics.value?.featureImportance || []).map((item) => item.label).reverse(),
    axisLabel: { color: '#69766f' },
    axisLine: { lineStyle: { color: '#d8d6ca' } },
  },
  series: [
    {
      type: 'bar',
      data: (analytics.value?.featureImportance || [])
        .map((item) => ({
          value: item.weight,
          itemStyle: {
            color: item.direction === 'risk_up' ? '#c57a55' : '#4f8f73',
            borderRadius: [0, 12, 12, 0],
          },
        }))
        .reverse(),
      barWidth: 18,
    },
  ],
}));

const knnSweepOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { textStyle: { color: '#52615a' }, top: 0 },
  grid: { left: 24, right: 18, top: 42, bottom: 24, containLabel: true },
  xAxis: {
    type: 'category',
    data: (analytics.value?.knnSweep || []).map((item) => `k=${item.k}`),
    axisLabel: { color: '#69766f' },
    axisLine: { lineStyle: { color: '#d8d6ca' } },
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      color: '#69766f',
      formatter: (value) => `${Math.round(value * 100)}%`,
    },
    splitLine: { lineStyle: { color: '#ece7d8' } },
  },
  series: [
    {
      name: 'Accuracy',
      type: 'line',
      smooth: true,
      data: (analytics.value?.knnSweep || []).map((item) => item.accuracy),
      lineStyle: { color: '#27465a', width: 3 },
      itemStyle: { color: '#27465a' },
    },
    {
      name: 'F1',
      type: 'line',
      smooth: true,
      data: (analytics.value?.knnSweep || []).map((item) => item.f1),
      lineStyle: { color: '#6ea88c', width: 3 },
      itemStyle: { color: '#6ea88c' },
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

function createNutritionEstimateDefaults() {
  return {
    matchedName: '',
    portion: '',
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
    confidence: '',
    note: '',
  };
}

function createSampleDefaults() {
  return {
    label: '',
    bodyType: bodyTypeOptions[1],
    bmi: 23.5,
    sleepHours: 7.0,
    steps: 7800,
    waterMl: 1900,
    stressScore: 42,
    glucose: 5.3,
    systolic: 118,
    diastolic: 76,
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

function formatNumber(value, digits = 1) {
  return Number(value || 0).toFixed(digits);
}

function average(values) {
  if (!values.length) return 0;
  return values.reduce((sum, value) => sum + Number(value || 0), 0) / values.length;
}

function correlation(left, right) {
  if (!left.length || left.length !== right.length) return 0;

  const meanLeft = average(left);
  const meanRight = average(right);

  let numerator = 0;
  let leftVariance = 0;
  let rightVariance = 0;

  for (let index = 0; index < left.length; index += 1) {
    const leftDelta = left[index] - meanLeft;
    const rightDelta = right[index] - meanRight;
    numerator += leftDelta * rightDelta;
    leftVariance += leftDelta * leftDelta;
    rightVariance += rightDelta * rightDelta;
  }

  const denominator = Math.sqrt(leftVariance * rightVariance);
  if (!denominator) return 0;
  return numerator / denominator;
}

function sumCounts(items, key) {
  return (items || []).reduce((sum, item) => sum + Number(item[key] || 0), 0);
}

function classifyBodyType(bmi) {
  if (bmi < 18.5) return '偏瘦';
  if (bmi < 24) return '标准';
  if (bmi < 28) return '超重';
  return '肥胖';
}

function enrichSample(item) {
  const bmi = Number(item.bmi || 0);
  const sleepHours = Number(item.sleepHours || 0);
  const steps = Number(item.steps || 0);
  const waterMl = Number(item.waterMl || 0);
  const stressScore = Number(item.stressScore || 0);
  const systolic = Math.round(94 + bmi * 1.2 + stressScore * 0.24 - sleepHours * 1.6 - steps / 2200 + Math.max(0, 2000 - waterMl) / 380);
  const diastolic = Math.round(62 + bmi * 0.6 + stressScore * 0.13 - sleepHours * 0.7);
  const glucose = Math.round((4.2 + bmi * 0.05 + stressScore * 0.01 - sleepHours * 0.06 - steps / 9000) * 10) / 10;
  return {
    id: item.id,
    bodyType: classifyBodyType(bmi),
    bmi,
    sleepHours,
    steps,
    waterMl,
    stressScore,
    systolic,
    diastolic,
    glucose,
    riskLabel: item.riskLabel || (stressScore >= 65 || bmi >= 28 ? 'HIGH' : 'LOW'),
  };
}

function normalizeImpactSeries(values) {
  const total = values.reduce((sum, value) => sum + value, 0) || 1;
  return values.map((value) => Number((value / total).toFixed(2)));
}

function idealCalories() {
  return Math.round(24 * scenarioForm.weight + Math.max(0, scenarioForm.workoutMinutes - 150) * 2.2);
}

function simulateWeek(week) {
  const calorieBalancePerDay = scenarioForm.calories - idealCalories();
  const weeklyWeightDelta = calorieBalancePerDay / 7700 * 7 - Math.max(0, scenarioForm.steps - 9000) / 25000;
  const sleepBenefit = (scenarioForm.sleepHours - 7.2) * 0.12;
  const stressPenalty = (scenarioForm.stressScore - 35) * 0.03;
  const workoutBenefit = (scenarioForm.workoutMinutes - 180) / 520;
  const bodyFatDelta = stressPenalty - sleepBenefit - workoutBenefit + Math.max(0, calorieBalancePerDay) / 2600;
  const glucoseDelta = Math.max(0, calorieBalancePerDay) / 2200 - (scenarioForm.steps - 8000) / 50000 - sleepBenefit * 0.6;

  return {
    weight: Number((scenarioForm.weight + weeklyWeightDelta * week).toFixed(1)),
    bodyFat: Number((scenarioForm.bodyFat + bodyFatDelta * week).toFixed(1)),
    stressScore: Math.max(18, Math.round(scenarioForm.stressScore + ((scenarioForm.sleepHours < 7 ? 1.6 : -0.5) - scenarioForm.workoutMinutes / 420) * week)),
    glucose: Number((Math.max(4.3, 5 + glucoseDelta * week)).toFixed(1)),
  };
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
  mealForm.portion = '150 g';
  mealForm.calories = 0;
  mealForm.protein = 0;
  mealForm.carbs = 0;
  mealForm.fat = 0;
  nutritionForm.query = '';
  nutritionForm.grams = 150;
  nutritionSearch.sourceSummary = '';
  nutritionSearch.items = [];
  Object.assign(nutritionEstimate, createNutritionEstimateDefaults());
}

function resetWorkoutForm() {
  Object.assign(workoutForm, createWorkoutDefaults());
}

function resetSampleForm() {
  Object.assign(sampleForm, createSampleDefaults());
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

  Object.assign(scenarioForm, {
    weight: dashboard.value.profile.weight,
    bodyFat: dashboard.value.profile.bodyFat,
    calories: dashboard.value.summary.calories || dashboard.value.summary.calorieTarget,
    steps: dashboard.value.summary.steps,
    sleepHours: dashboard.value.summary.sleepHours,
    stressScore: dashboard.value.summary.stressScore,
    workoutMinutes: dashboard.value.summary.workoutMinutes * 7,
    waterMl: dashboard.value.summary.water,
  });

  Object.assign(goalScenario, {
    targetWeight: dashboard.value.profile.targetWeight,
    targetBodyFat: Math.max(16, dashboard.value.profile.bodyFat - 3),
    targetSleepHours: 7.8,
    targetStressScore: Math.max(25, dashboard.value.summary.stressScore - 8),
    targetSteps: Math.max(9000, dashboard.value.summary.stepTarget),
    weeks: 8,
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

function addSample() {
  const payload = {
    id: `custom-${Date.now()}`,
    bodyType: sampleForm.bodyType || classifyBodyType(sampleForm.bmi),
    bmi: Number(sampleForm.bmi),
    sleepHours: Number(sampleForm.sleepHours),
    steps: Number(sampleForm.steps),
    waterMl: Number(sampleForm.waterMl),
    stressScore: Number(sampleForm.stressScore),
    glucose: Number(sampleForm.glucose),
    systolic: Number(sampleForm.systolic),
    diastolic: Number(sampleForm.diastolic),
    riskLabel:
      Number(sampleForm.stressScore) >= 65 || Number(sampleForm.bmi) >= 28 || Number(sampleForm.glucose) >= 6.1
        ? 'HIGH'
        : 'LOW',
    label: sampleForm.label || `补充样本 ${customSamples.value.length + 1}`,
  };

  customSamples.value = [...customSamples.value, payload];
  resetSampleForm();
  showNotice('新样本已加入分析池，下面的图表和比例已经同步更新。', 'success');
}

async function saveProfile() {
  await runDashboardAction('profile', () => api.post('/api/dashboard/profile', profileForm), '身体画像已更新。');
}

async function saveCheckin() {
  await runDashboardAction('checkin', () => api.post('/api/dashboard/checkin', checkinForm), '今日数据已保存。');
}

async function searchNutritionFoods() {
  if (!nutritionForm.query.trim()) {
    showNotice('请先输入菜品或食材名称。', 'error');
    return;
  }

  busy.nutritionSearch = true;
  try {
    const response = await api.get(
      `/api/dashboard/nutrition/search?q=${encodeURIComponent(nutritionForm.query.trim())}&limit=6`
    );
    nutritionSearch.sourceSummary = response.sourceSummary || '';
    nutritionSearch.items = response.items || [];
    if (!nutritionSearch.items.length) {
      showNotice('暂未找到匹配食材，请试试更基础的名称。', 'error');
      return;
    }
    showNotice(`已找到 ${nutritionSearch.items.length} 个候选食材。`, 'success');
  } catch (error) {
    showNotice(error.message || '食材搜索失败，请稍后重试。', 'error');
  } finally {
    busy.nutritionSearch = false;
  }
}

function syncMealFormFromEstimate(estimate, preferredName = '') {
  mealForm.name = preferredName || nutritionForm.query.trim() || estimate.matchedName;
  mealForm.portion = estimate.portion;
  mealForm.calories = Math.round(Number(estimate.calories || 0));
  mealForm.protein = Math.round(Number(estimate.protein || 0));
  mealForm.carbs = Math.round(Number(estimate.carbs || 0));
  mealForm.fat = Math.round(Number(estimate.fat || 0));
}

async function estimateNutrition(queryOverride = '') {
  const query = (queryOverride || nutritionForm.query).trim();
  if (!query) {
    showNotice('请先输入菜品或食材名称。', 'error');
    return;
  }

  busy.nutritionEstimate = true;
  try {
    const response = await api.post('/api/dashboard/nutrition/estimate', {
      query,
      grams: Number(nutritionForm.grams) || 150,
    });
    Object.assign(nutritionEstimate, response);
    syncMealFormFromEstimate(response, query);
    nutritionSearch.sourceSummary = response.source || nutritionSearch.sourceSummary;
    nutritionSearch.items = response.relatedFoods || nutritionSearch.items;
    showNotice(`已根据 ${response.matchedName} 自动填充营养数据。`, 'success');
  } catch (error) {
    showNotice(error.message || '营养估算失败，请稍后重试。', 'error');
  } finally {
    busy.nutritionEstimate = false;
  }
}

function applySuggestion(item) {
  nutritionForm.query = item.name;
  estimateNutrition(item.name);
}

async function saveMeal() {
  if (!mealForm.name.trim() && nutritionEstimate.matchedName) {
    mealForm.name = nutritionEstimate.matchedName;
  }
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
