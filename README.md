# LightBalance 轻享健康

一个面向人工智能课程大作业的健康生活分析软件，采用 `Vue 3 + JavaScript + Spring Boot + MySQL(H2 兼容直跑)` 方案实现，兼顾可运行的软件展示和完整的数据分析/机器学习流程。

## 项目亮点

- 高保真桌面式 UI：今日概览、身体画像、饮食规划、训练计划、趋势追踪、智能建议 6 大模块
- 完整课程流程：问题定义、数据来源说明、数据预处理、可视化分析、模型训练、模型评价、结果分析、系统展示
- 双数据链路：主项目使用生活方式模拟数据，同时引入 `UCI Cleveland Heart Disease` 公开医疗数据做外部验证
- 内置机器学习对比：`Logistic Regression`、`KNN`、`Gaussian Naive Bayes` 三模型比较，并补充 `AUC` 与 `KNN` 调参曲线
- 已接入真正的大模型助手：支持 DeepSeek 聊天建议，可结合当天健康数据生成个性化回复
- 默认可直接运行：后端默认使用 `H2 MySQL 模式`，无需本机先安装 MySQL
- MySQL 可切换：提供 `database/health_balance_mysql.sql` 与 `application-mysql.yml`

## 作业题目

`基于生活方式数据的健康风险分析与智能建议系统`

## 技术结构

- 前端：Vue 3、Vite、ECharts、Lucide Icons
- 后端：Spring Boot 3、Spring Web、Spring Data JPA
- 数据层：H2（默认演示）/ MySQL（课程提交可切换）
- 数据分析：Node 脚本生成模拟数据、清洗公开数据、训练多模型、输出评价结果

## 目录结构

```text
Final/
├── analysis/                  # 数据集生成与模型评价脚本
├── backend/                   # Spring Boot 后端
├── frontend/                  # Vue 前端
├── data/                      # 提交用数据文件
├── database/                  # MySQL 建表脚本
├── docs/                      # 报告骨架与说明
├── scripts/                   # 启动与初始化脚本
└── README.md
```

## 数据与模型说明

- 主任务数据：`data/lifestyle_risk_dataset.csv`
- 主任务样本量：420
- 主任务标签：`HIGH / LOW`
- 主任务特征：
  - BMI
  - 睡眠时长
  - 日步数
  - 运动时长
  - 饮水量
  - 热量摄入
  - 蛋白质摄入
  - 压力评分
  - 静息心率
  - 腰围
  - 屏幕时长
  - 是否吸烟
  - 是否深夜加餐

- 外部公开数据：`data/external/processed.cleveland.data`
- 清洗后公开数据：`data/heart_disease_cleveland_cleaned.csv`
- 公开数据来源：`UCI Machine Learning Repository - Heart Disease`
- 公开数据主页：`https://archive.ics.uci.edu/dataset/45/heart+disease`

当前主任务模型结果：

- Logistic Regression：Accuracy `94.12%`，F1 `92.75%`，AUC `99.13%`
- Gaussian Naive Bayes：Accuracy `85.88%`，F1 `81.25%`
- KNN (k=7)：Accuracy `76.47%`，F1 `65.52%`

公开数据基准结果：

- Gaussian Naive Bayes：Accuracy `91.67%`，F1 `90.91%`
- Logistic Regression：Accuracy `91.67%`，F1 `90.57%`
- KNN (k=9)：Accuracy `86.67%`，F1 `85.19%`

完整指标、预处理摘要、特征重要性、混淆矩阵和 KNN 调参结果见 `data/model_report.json` 与系统分析界面。
如果需要逐项对应课程评分点，参考 `docs/model-rubric-mapping.md`。
如果需要单独说明“加分项”是如何落实的，参考 `docs/bonus-mapping.md`。

## 运行方式

### 方式一：单服务直跑（推荐）

前端静态页面已经打包进 Spring Boot，默认只需要启动后端：

1. 执行 `scripts/bootstrap.ps1`
2. 执行 `scripts/run-backend.ps1`
3. 浏览器访问 `http://localhost:8088`

### DeepSeek 配置

如果你希望“智能建议”模块真正调用 DeepSeek，而不是只显示接入提示，请先配置环境变量：

```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek API Key"
```

可选配置：

```powershell
$env:DEEPSEEK_MODEL="deepseek-v4-flash"
```

然后重新执行：

```powershell
scripts/run-backend.ps1
```

默认配置项：

- `DEEPSEEK_API_KEY`：必填
- `DEEPSEEK_MODEL`：默认 `deepseek-v4-flash`
- `DEEPSEEK_BASE_URL`：默认 `https://api.deepseek.com`
- `DEEPSEEK_THINKING_ENABLED`：默认 `false`

## 公网部署与微信分享

项目已经补齐了适合公网部署的文件：

- `Dockerfile`
- `render.yaml`
- `backend/src/main/resources/application-prod.yml`

这意味着它已经具备：

- 单服务部署
- HTTPS 公网访问
- 手机浏览器可读
- 微信内直接打开网页链接

### 推荐部署方式：Render

1. 把项目上传到 GitHub 仓库
2. 登录 Render
3. 选择 `New +` -> `Blueprint`
4. 连接你的 GitHub 仓库
5. Render 会自动识别根目录下的 `render.yaml`
6. 在环境变量里填入：
   - `DEEPSEEK_API_KEY`
7. 部署完成后，会得到一个公网 `https://...onrender.com` 链接
8. 把这个 HTTPS 链接直接发到微信即可

### 为什么现在适合微信分享

- 前端接口默认改成了同源访问，不再写死 `localhost`
- 页面补了移动端响应式布局
- 部署后会走 HTTPS，微信里可直接点开
- 页面补了分享标题、描述和封面图

### 当前限制

我已经把项目改成“可部署到公网”的状态，但真正生成公网链接这一步，仍然需要你自己的：

- GitHub 仓库
- Render 账号
- DeepSeek API Key

如果你想继续，我下一步最合适的是帮你把这个项目整理成“上传 GitHub 后直接点部署”的最终提交状态。

### 方式二：前后端分离开发

1. 执行 `scripts/bootstrap.ps1`
2. 执行 `scripts/run-backend.ps1`
3. 执行 `scripts/run-frontend.ps1`
4. 浏览器访问 `http://localhost:5173`

### 方式二：切换 MySQL

1. 本机启动 MySQL
2. 执行 `database/health_balance_mysql.sql`
3. 修改 `backend/src/main/resources/application-mysql.yml` 中用户名密码
4. 使用：

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

## 课程作业要求映射

- 明确问题：健康生活方式数据驱动的风险判断与日常建议
- 数据来源：自建模拟数据 + UCI 公开医疗数据，来源、字段和清洗逻辑都可说明
- 数据预处理：完整展示缺失值检查、公开数据清洗、分层 8:2 划分、标准化处理
- 至少 3 张图表：趋势图、睡眠压力散点图、BMI 风险柱状图、特征重要性条形图、KNN 调参曲线
- 至少 1 个模型：已实现 3 个分类模型对比
- 模型评价：Accuracy / Precision / Recall / F1 / AUC / 混淆矩阵
- 结果分析：在系统分析区、`docs/report-outline.md` 和 `data/model_report.json` 中给出

## 高分建议

- 展示时优先打开“趋势追踪”和“身体画像”两页，老师能直接看到预处理、模型评价、混淆矩阵、公开数据验证和图表分析。
- 报告里明确写出为什么默认展示逻辑回归：它在主任务上兼顾 `F1`、`Recall` 和可解释性。
- 答辩时强调“不是只做了模拟数据”，而是额外引入了公开医疗数据验证模型流程的迁移能力。
- 如果时间允许，再补一页 PPT 专门展示 `UCI Cleveland Heart Disease` 的清洗流程和结果对比，会很加分。

## 交付建议

提交压缩包时建议保留：

- `frontend/`
- `backend/`
- `data/`
- `database/`
- `docs/`
- `README.md`

## 开发补充

- 如果你修改了前端页面，执行 `scripts/sync-frontend.ps1` 可以重新打包并同步到 Spring Boot 的静态资源目录。
- 后端默认走 H2 的 MySQL 兼容模式；如果老师明确要求必须连接真实 MySQL，切到 `mysql` 配置即可。
- DeepSeek 已经完成代码接入；如果界面显示 “DeepSeek not configured”，说明代码没问题，只是当前启动环境里还没有 API Key。
