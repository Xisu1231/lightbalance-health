# 加分项对应说明

本文档用于对应课程说明中的“加分项”。

目标不是泛泛而谈“我们也有”，而是把每一条加分点落实到：

- 实际功能
- 代码/数据证据
- 答辩时可直接使用的话术

## 1. 已完成可交互展示界面

本项目不是静态报告，也不是单个 Notebook，而是完整的交互式 Web 应用：

- 今日概览
- 身体画像
- 饮食规划
- 训练计划
- 趋势追踪
- 智能建议

用户可以真实录入、修改、查看数据，系统会联动刷新健康画像、趋势图和建议内容。

对应证据：

- [frontend/src/App.vue](/E:/WORK_2/AI/Final/frontend/src/App.vue:1)
- [backend/src/main/java/com/lightbalance/health/web/DashboardController.java](/E:/WORK_2/AI/Final/backend/src/main/java/com/lightbalance/health/web/DashboardController.java:18)
- [README.md](/E:/WORK_2/AI/Final/README.md:7)

答辩可直接说：

“我们完成的是一个真实可交互的分析系统，而不是只在报告里放图表截图。”

## 2. 比较了 3 种以上模型，并进行了结果分析

项目当前比较了 3 个分类模型：

- `Logistic Regression`
- `KNN`
- `Gaussian Naive Bayes`

而且不是只比较 Accuracy，而是同时比较：

- Accuracy
- Precision
- Recall
- F1
- AUC
- 混淆矩阵

还额外做了 `KNN` 的参数比较：`k = 3, 5, 7, 9, 11`。

对应证据：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:571)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:613)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:61)
- [docs/model-rubric-mapping.md](/E:/WORK_2/AI/Final/docs/model-rubric-mapping.md:11)

答辩可直接说：

“我们不是只跑了一个模型，而是做了三模型对比和 KNN 参数扫描，最后才确定主推断引擎。”

## 3. 自己采集或整理了真实数据

本项目的数据不是单一路径：

- 一条是自行构建并清洗的生活方式数据链路
- 另一条是引入并整理的真实公开医疗数据 `UCI Cleveland Heart Disease`

其中公开数据做了：

- 原始文件读取
- `?` 缺失值识别
- 删除缺失记录
- 标签重映射
- 清洗后导出为平台可直接复用的 CSV

对应证据：

- [analysis/preprocess_with_pandas.py](/E:/WORK_2/AI/Final/analysis/preprocess_with_pandas.py:1)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:533)
- [data/heart_disease_cleveland_cleaned.csv](/E:/WORK_2/AI/Final/data/heart_disease_cleveland_cleaned.csv:1)
- [data/preprocessing_audit.json](/E:/WORK_2/AI/Final/data/preprocessing_audit.json:1)

答辩可直接说：

“我们不仅做了模拟数据，还额外整理了 UCI 真实公开数据，用来验证当前建模流程的迁移能力。”

## 4. 项目主题与生活实际/社会问题结合紧密

主题不是抽象题，而是和日常健康管理直接相关：

- 睡眠
- 步数
- 饮水
- 压力
- BMI
- 饮食
- 训练

输出也不是只给一个“分类标签”，而是进一步转化成用户能理解的健康建议和行动提示。

对应证据：

- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:9)
- [frontend/src/App.vue](/E:/WORK_2/AI/Final/frontend/src/App.vue:140)
- [frontend/src/App.vue](/E:/WORK_2/AI/Final/frontend/src/App.vue:1179)

答辩可直接说：

“这个题目天然贴近生活，不是为了做模型而做模型，而是围绕健康风险识别和行为调整展开。”

## 5. 对模型进行了参数调优或特征工程处理

本项目两类都做了：

### 参数调优

- `KNN` 比较了多个 `k` 值
- Logistic Regression 明确设置训练轮数和学习率
- UCI 基准数据上使用了另一组 Logistic 参数

### 特征工程

- 对 Logistic Regression 与 KNN 做标准化
- 使用逻辑回归权重做特征重要性分析
- 保留 BMI、压力、运动、腰围、饮水等多维特征共同建模

对应证据：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:233)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:457)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:686)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:120)

答辩可直接说：

“我们既做了参数调优，也做了特征层面的分析解释，这部分不只是训练完就结束。”

## 6. 报告中对模型局限和改进方向分析较深入

当前报告已经明确写出：

- 主任务数据仍有模拟成分
- 公开数据规模有限
- 当前模型不能直接作为医学诊断依据
- 后续可接入真实可穿戴设备数据
- 后续可增加更复杂模型，如随机森林或 XGBoost
- 后续可增加 ROC 曲线、报告导出等能力

对应证据：

- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:189)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:213)
- [docs/model-rubric-mapping.md](/E:/WORK_2/AI/Final/docs/model-rubric-mapping.md:171)

答辩可直接说：

“我们不仅展示结果，也明确写了局限性和后续改进方向，避免把课程演示包装成医学结论。”

## 7. 展示效果较好，能清楚说明项目思路和结果

当前系统已经具备适合课堂演示的几个优势：

- 应用不是报告口吻，而是独立软件界面
- 用户侧页面强调健康记录、趋势和建议
- 报告侧材料单独保留数据、模型和预处理细节
- 有图表、有表格、有文字分析、有真实交互
- 已接入 DeepSeek，可展示“AI 助手 + 数据分析系统”的完整感

对应证据：

- [README.md](/E:/WORK_2/AI/Final/README.md:7)
- [frontend/src/App.vue](/E:/WORK_2/AI/Final/frontend/src/App.vue:1)
- [docs/model-rubric-mapping.md](/E:/WORK_2/AI/Final/docs/model-rubric-mapping.md:1)

答辩可直接说：

“展示时老师可以先看到可交互系统，再看到模型证据和报告材料，项目思路会比较完整，也更容易拿到展示加分。”

## 最终结论

如果按课程给出的 7 条加分项逐条对应，本项目目前已经稳定覆盖：

- 可交互系统
- 三模型比较
- 整理真实公开数据
- 题目贴近生活实际
- 参数调优与特征工程
- 局限性与改进方向
- 展示效果完整

也就是说，这部分已经不是“碰碰运气加分”，而是具备系统性争取 `+3 ~ +5` 的基础。
