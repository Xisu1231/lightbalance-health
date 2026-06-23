# 模型部分评分点对应说明

本文档用于对应课程评分标准中的：

- `4. 模型设计与实现（20分）`
- `5. 模型评价与结果分析（15分）`

## 4. 模型设计与实现

### 4.1 模型选择合理

本项目是一个二分类任务，目标标签为 `HIGH / LOW`，因此选择了三类适合分类任务的模型进行比较：

- `Logistic Regression`
  适合二分类，训练稳定，可直接解释特征权重，适合作为主推断模型。
- `KNN`
  适合做邻域型对比模型，便于展示参数变化对结果的影响。
- `Gaussian Naive Bayes`
  训练速度快，对小样本和外部公开医疗数据表现稳定，可作为另一类建模思路的补充。

对应代码位置：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:571)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:682)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:119)

结论：

- 主任务数据上，`Logistic Regression` 的综合表现最好，因此被选为主推断引擎。
- 外部 UCI 基准数据上，`Gaussian Naive Bayes` 最稳定，说明当前流程具有跨源可迁移性。

### 4.2 代码实现正确

项目中模型训练、预测和评价并不是口头描述，而是有完整脚本实现：

- `stratifiedSplit(...)`：实现分层训练测试划分
- `trainLogisticRegression(...)`：实现逻辑回归训练
- `trainGaussianNb(...)`：实现高斯朴素贝叶斯训练
- `predictKnn(...)`：实现 KNN 分类
- `evaluateModel(...)`：统一计算 Accuracy / Precision / Recall / F1 / AUC / 混淆矩阵

对应代码位置：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:208)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:233)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:275)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:325)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:372)

生成产物：

- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:1)
- [analysis/generated/model_report.json](/E:/WORK_2/AI/Final/analysis/generated/model_report.json:1)

这说明模型不是“写在报告里”，而是能真实训练、真实输出结果。

### 4.3 训练测试划分规范

本项目采用：

- 分层 `8:2` 划分训练集和测试集
- 主任务样本：`335 / 85`
- UCI 基准样本也沿用同样的训练测试策略

这样做的原因是：

- 高风险样本占比不是 50%，如果随机切分，测试集类别比例可能偏移；
- 分层抽样能保证训练集和测试集的类别分布更稳定。

对应证据：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:208)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:38)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:68)

### 4.4 参数设置清楚

主任务参数并不是含糊带过，而是可以明确说明：

- Logistic Regression
  - 训练轮数 `iterations = 2600`
  - 学习率 `lr = 0.085`
  - 判定阈值 `0.5`
- KNN
  - 主任务展示模型使用 `k = 7`
  - 额外比较 `k = 3, 5, 7, 9, 11`
- Gaussian Naive Bayes
  - 使用按类别统计均值与方差的高斯分布建模
- UCI 基准数据上的 Logistic Regression
  - 训练轮数 `3200`
  - 学习率 `0.075`
- 标准化策略
  - Logistic Regression 与 KNN 使用训练集统计量做标准化
  - Gaussian Naive Bayes 直接基于原始数值分布建模

对应代码位置：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:233)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:325)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:457)
- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:686)

### 4.5 有拓展尝试

项目不只是“做了一个模型”，而是做了多层拓展：

- 三模型对比：Logistic Regression / KNN / Gaussian Naive Bayes
- KNN 参数扫描：`k = 3, 5, 7, 9, 11`
- 加入外部公开数据做跨源验证
- 保留特征重要性与混淆矩阵做进一步解释

对应证据：

- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:61)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:191)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:219)

结论：这一部分可以按 `20/20` 进行说明。

## 5. 模型评价与结果分析

### 5.1 评价指标正确

本项目对分类任务同时保留了以下指标：

- `Accuracy`
- `Precision`
- `Recall`
- `F1`
- `AUC`
- `Confusion Matrix`

这样做是因为当前数据里高风险样本占比约为 `38.33%`，仅看 Accuracy 不够，必须同时关注 Recall 和 F1。

对应证据：

- [analysis/generate-dataset.mjs](/E:/WORK_2/AI/Final/analysis/generate-dataset.mjs:396)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:314)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:150)

### 5.2 结果表达清楚

结果同时通过三种形式展示：

- 表格：报告中的模型对比表
- 图表：KNN 调参曲线、BMI 风险区间图、特征重要性图、趋势图
- 文字：主任务分析、跨源验证分析、局限性分析

对应证据：

- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:150)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:168)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:288)

### 5.3 分析较深入

当前分析已经不只是“谁分数高”，还解释了：

- 为什么主任务里逻辑回归最好
- 为什么 KNN 会随着 `k` 的变化而明显波动
- 为什么需要同时看 Recall 和 F1
- 为什么公开数据上的最优模型与主任务不同
- 哪些特征是风险的关键驱动项

对应证据：

- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:170)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:176)
- [data/model_report.json](/E:/WORK_2/AI/Final/data/model_report.json:310)

### 5.4 认识模型局限

本项目已经明确写出局限性：

- 主任务数据仍然是模拟数据，与真实世界有差距
- UCI Cleveland 数据规模有限，不能代表更复杂的临床环境
- 当前模型适合课程展示和辅助分析，不能直接用于医学诊断

对应证据：

- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:189)
- [docs/report-outline.md](/E:/WORK_2/AI/Final/docs/report-outline.md:213)

结论：这一部分也可以按 `15/15` 进行说明。

## 答辩时建议这样说

可以直接用下面这句话概括：

“模型部分不是只放了一个结果截图，而是完成了从分层划分、三模型训练、KNN 调参、统一指标评价，到公开数据跨源验证和局限性分析的完整闭环，因此模型设计与实现、模型评价与结果分析这两部分都具备拿满分的证据。”
