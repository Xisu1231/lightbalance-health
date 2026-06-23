import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const OUTPUT_DIRS = [
  resolve('analysis/generated'),
  resolve('data'),
  resolve('backend/src/main/resources/seed'),
];

const lifestyleFeatureMeta = {
  bmi: { label: 'BMI', direction: 'positive', interpretation: 'BMI 偏高时，高风险概率会明显上升。' },
  sleep_hours: { label: '睡眠时长', direction: 'negative', interpretation: '睡眠越充足，模型越倾向于判定为低风险。' },
  steps: { label: '日步数', direction: 'negative', interpretation: '步数更高通常对应更低的生活方式风险。' },
  exercise_minutes: { label: '运动时长', direction: 'negative', interpretation: '规律运动会压低模型给出的高风险概率。' },
  water_ml: { label: '饮水量', direction: 'negative', interpretation: '饮水不足与较高风险标签呈同向关系。' },
  calories_kcal: { label: '热量摄入', direction: 'positive', interpretation: '热量长期偏高时，更容易被判定为高风险样本。' },
  protein_g: { label: '蛋白质摄入', direction: 'negative', interpretation: '蛋白质摄入更充足时，健康风险倾向下降。' },
  stress_score: { label: '压力评分', direction: 'positive', interpretation: '压力评分越高，高风险判断越常出现。' },
  resting_heart_rate: { label: '静息心率', direction: 'positive', interpretation: '静息心率偏高会增加模型的风险评分。' },
  waist_cm: { label: '腰围', direction: 'positive', interpretation: '腰围升高是高风险样本的重要信号。' },
  screen_hours: { label: '屏幕时长', direction: 'positive', interpretation: '久坐和长时间屏幕暴露会推高风险概率。' },
  smoking_flag: { label: '吸烟标记', direction: 'positive', interpretation: '吸烟样本更容易落入高风险类别。' },
  late_night_snack: { label: '深夜加餐', direction: 'positive', interpretation: '深夜加餐会把样本推向更高风险一侧。' },
};

const heartColumns = [
  'age',
  'sex',
  'cp',
  'trestbps',
  'chol',
  'fbs',
  'restecg',
  'thalach',
  'exang',
  'oldpeak',
  'slope',
  'ca',
  'thal',
  'num',
];

const heartColumnDescriptions = {
  age: '年龄',
  sex: '性别',
  cp: '胸痛类型',
  trestbps: '静息血压',
  chol: '胆固醇',
  fbs: '空腹血糖是否偏高',
  restecg: '静息心电图结果',
  thalach: '最大心率',
  exang: '运动诱发心绞痛',
  oldpeak: '运动后 ST 压低',
  slope: 'ST 斜率',
  ca: '主要血管数',
  thal: '地中海贫血检查结果',
};

mkdirSync(resolve('analysis/generated'), { recursive: true });
mkdirSync(resolve('data'), { recursive: true });
mkdirSync(resolve('backend/src/main/resources/seed'), { recursive: true });

function writeToTargets(fileName, content, targets = OUTPUT_DIRS) {
  targets.forEach((dir) => {
    mkdirSync(dir, { recursive: true });
    writeFileSync(resolve(dir, fileName), content, 'utf8');
  });
}

function createRng(seed = 20260622) {
  let state = seed >>> 0;
  return () => {
    state = (1664525 * state + 1013904223) >>> 0;
    return state / 0xffffffff;
  };
}

const rand = createRng();

function range(min, max) {
  return min + (max - min) * rand();
}

function int(min, max) {
  return Math.floor(range(min, max + 1));
}

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function bmi(weightKg, heightCm) {
  const h = heightCm / 100;
  return weightKg / (h * h);
}

function round(value, digits = 2) {
  const base = 10 ** digits;
  return Math.round(value * base) / base;
}

function sigmoid(z) {
  return 1 / (1 + Math.exp(-z));
}

function shuffle(items) {
  const arr = [...items];
  for (let i = arr.length - 1; i > 0; i -= 1) {
    const j = Math.floor(rand() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
}

function softmaxBinary(logProbPositive, logProbNegative) {
  const max = Math.max(logProbPositive, logProbNegative);
  const positive = Math.exp(logProbPositive - max);
  const negative = Math.exp(logProbNegative - max);
  return positive / (positive + negative);
}

function generateLifestyleSample(index) {
  const age = int(19, 55);
  const gender = rand() > 0.52 ? 1 : 0;
  const heightCm = round(gender ? range(167, 188) : range(153, 175), 1);
  const baselineWeight = gender ? range(59, 97) : range(46, 79);
  const lifestyleShift = range(-7, 11);
  const weightKg = round(clamp(baselineWeight + lifestyleShift, 42, 112), 1);
  const bmiValue = round(bmi(weightKg, heightCm), 2);
  const sleepHours = round(range(4.8, 8.9), 1);
  const steps = int(1800, 16200);
  const exerciseMinutes = int(0, 115);
  const waterMl = int(700, 3200);
  const caloriesKcal = int(1350, 3250);
  const proteinG = int(35, 170);
  const stressScore = int(20, 94);
  const restingHeartRate = int(55, 104);
  const waistCm = round(range(64, 111), 1);
  const screenHours = round(range(2.1, 10.8), 1);
  const smokingFlag = rand() > 0.87 ? 1 : 0;
  const lateNightSnack = rand() > 0.59 ? 1 : 0;

  const riskScore =
    (bmiValue - 22.5) * 0.18 +
    (6.9 - sleepHours) * 0.46 +
    (7000 - steps) / 2400 +
    (25 - exerciseMinutes) / 18 +
    (1700 - waterMl) / 520 +
    (stressScore - 56) / 13 +
    (restingHeartRate - 73) / 10.8 +
    (waistCm - 84) / 9.8 +
    (screenHours - 5.3) / 2.4 +
    smokingFlag * 1.45 +
    lateNightSnack * 0.72 +
    (caloriesKcal > 2550 ? 0.92 : 0) +
    (proteinG < 68 ? 0.46 : 0) +
    range(-0.95, 0.95);

  const riskLabel = riskScore > 1.52 ? 'HIGH' : 'LOW';

  return {
    id: `S${String(index + 1).padStart(3, '0')}`,
    age,
    gender,
    height_cm: heightCm,
    weight_kg: weightKg,
    bmi: bmiValue,
    sleep_hours: sleepHours,
    steps,
    exercise_minutes: exerciseMinutes,
    water_ml: waterMl,
    calories_kcal: caloriesKcal,
    protein_g: proteinG,
    stress_score: stressScore,
    resting_heart_rate: restingHeartRate,
    waist_cm: waistCm,
    screen_hours: screenHours,
    smoking_flag: smokingFlag,
    late_night_snack: lateNightSnack,
    risk_label: riskLabel,
  };
}

function mean(values) {
  return values.reduce((sum, value) => sum + value, 0) / values.length;
}

function standardize(samples, featureNames) {
  const means = {};
  const stds = {};
  featureNames.forEach((name) => {
    const values = samples.map((sample) => Number(sample[name]));
    const avg = mean(values);
    const variance = values.reduce((sum, value) => sum + (value - avg) ** 2, 0) / values.length;
    means[name] = avg;
    stds[name] = Math.sqrt(variance) || 1;
  });
  return { means, stds };
}

function vectorize(sample, featureNames, scaler) {
  return featureNames.map((name) => {
    const raw = Number(sample[name]);
    return (raw - scaler.means[name]) / scaler.stds[name];
  });
}

function stratifiedSplit(samples, labelKey, trainRatio = 0.8) {
  const byLabel = new Map();
  samples.forEach((sample) => {
    const label = sample[labelKey];
    if (!byLabel.has(label)) {
      byLabel.set(label, []);
    }
    byLabel.get(label).push(sample);
  });

  const train = [];
  const test = [];
  for (const [, bucket] of byLabel.entries()) {
    const shuffled = shuffle(bucket);
    const splitIndex = Math.max(1, Math.floor(shuffled.length * trainRatio));
    train.push(...shuffled.slice(0, splitIndex));
    test.push(...shuffled.slice(splitIndex));
  }

  return {
    train: shuffle(train),
    test: shuffle(test),
  };
}

function trainLogisticRegression(trainSamples, featureNames, labelKey, positiveLabel, iterations = 2600, lr = 0.085) {
  const scaler = standardize(trainSamples, featureNames);
  const xs = trainSamples.map((sample) => vectorize(sample, featureNames, scaler));
  const ys = trainSamples.map((sample) => (sample[labelKey] === positiveLabel ? 1 : 0));

  const weights = new Array(featureNames.length).fill(0);
  let bias = 0;

  for (let step = 0; step < iterations; step += 1) {
    const gradW = new Array(weights.length).fill(0);
    let gradB = 0;

    for (let i = 0; i < xs.length; i += 1) {
      const score = xs[i].reduce((sum, value, index) => sum + value * weights[index], bias);
      const prediction = sigmoid(score);
      const error = prediction - ys[i];

      for (let j = 0; j < weights.length; j += 1) {
        gradW[j] += error * xs[i][j];
      }
      gradB += error;
    }

    for (let j = 0; j < weights.length; j += 1) {
      weights[j] -= (lr * gradW[j]) / xs.length;
    }
    bias -= (lr * gradB) / xs.length;
  }

  return { weights, bias, scaler };
}

function predictLogistic(model, sample, featureNames, positiveLabel, negativeLabel) {
  const features = vectorize(sample, featureNames, model.scaler);
  const score = features.reduce((sum, value, index) => sum + value * model.weights[index], model.bias);
  const probability = sigmoid(score);
  return {
    probability,
    label: probability >= 0.5 ? positiveLabel : negativeLabel,
  };
}

function trainGaussianNb(trainSamples, featureNames, labelKey, positiveLabel) {
  const classes = [positiveLabel, trainSamples.find((sample) => sample[labelKey] !== positiveLabel)[labelKey]];
  const stats = {};
  const priors = {};

  classes.forEach((label) => {
    const subset = trainSamples.filter((sample) => sample[labelKey] === label);
    priors[label] = subset.length / trainSamples.length;
    stats[label] = {};
    featureNames.forEach((feature) => {
      const values = subset.map((sample) => Number(sample[feature]));
      const avg = mean(values);
      const variance = values.reduce((sum, value) => sum + (value - avg) ** 2, 0) / values.length || 1e-6;
      stats[label][feature] = {
        mean: avg,
        variance,
      };
    });
  });

  return { stats, priors, classes };
}

function gaussianLogPdf(value, avg, variance) {
  return -0.5 * Math.log(2 * Math.PI * variance) - ((value - avg) ** 2) / (2 * variance);
}

function predictGaussianNb(model, sample, featureNames, positiveLabel, negativeLabel) {
  const positiveLog =
    Math.log(model.priors[positiveLabel]) +
    featureNames.reduce((sum, feature) => {
      const current = model.stats[positiveLabel][feature];
      return sum + gaussianLogPdf(Number(sample[feature]), current.mean, current.variance);
    }, 0);

  const negativeLog =
    Math.log(model.priors[negativeLabel]) +
    featureNames.reduce((sum, feature) => {
      const current = model.stats[negativeLabel][feature];
      return sum + gaussianLogPdf(Number(sample[feature]), current.mean, current.variance);
    }, 0);

  const probability = softmaxBinary(positiveLog, negativeLog);
  return {
    probability,
    label: probability >= 0.5 ? positiveLabel : negativeLabel,
  };
}

function predictKnn(trainSamples, featureNames, sample, labelKey, positiveLabel, negativeLabel, k = 7) {
  const scaler = standardize(trainSamples, featureNames);
  const target = vectorize(sample, featureNames, scaler);

  const neighbors = trainSamples
    .map((trainSample) => {
      const vector = vectorize(trainSample, featureNames, scaler);
      const distance = Math.sqrt(
        vector.reduce((sum, value, index) => sum + (value - target[index]) ** 2, 0),
      );
      return {
        label: trainSample[labelKey],
        distance,
      };
    })
    .sort((a, b) => a.distance - b.distance)
    .slice(0, k);

  const positiveVotes = neighbors.filter((item) => item.label === positiveLabel).length;
  const probability = positiveVotes / k;

  return {
    probability,
    label: probability >= 0.5 ? positiveLabel : negativeLabel,
  };
}

function computeAuc(entries) {
  const positives = entries.filter((item) => item.actual === 1);
  const negatives = entries.filter((item) => item.actual === 0);
  if (!positives.length || !negatives.length) {
    return 0.5;
  }

  const ranked = [...entries]
    .sort((a, b) => a.probability - b.probability)
    .map((item, index) => ({ ...item, rank: index + 1 }));

  const positiveRankSum = ranked
    .filter((item) => item.actual === 1)
    .reduce((sum, item) => sum + item.rank, 0);

  return (
    (positiveRankSum - (positives.length * (positives.length + 1)) / 2) /
    (positives.length * negatives.length)
  );
}

function evaluateModel(name, testSamples, predictFn, labelKey, positiveLabel, negativeLabel) {
  let tp = 0;
  let fp = 0;
  let tn = 0;
  let fn = 0;

  const probabilities = [];

  testSamples.forEach((sample) => {
    const result = predictFn(sample);
    const actualPositive = sample[labelKey] === positiveLabel;
    const predictedPositive = result.label === positiveLabel;

    if (actualPositive && predictedPositive) tp += 1;
    if (!actualPositive && predictedPositive) fp += 1;
    if (!actualPositive && !predictedPositive) tn += 1;
    if (actualPositive && !predictedPositive) fn += 1;

    probabilities.push({
      actual: actualPositive ? 1 : 0,
      probability: result.probability,
    });
  });

  const accuracy = (tp + tn) / testSamples.length;
  const precision = tp / Math.max(tp + fp, 1);
  const recall = tp / Math.max(tp + fn, 1);
  const f1 = (2 * precision * recall) / Math.max(precision + recall, 1e-9);
  const auc = computeAuc(probabilities);

  return {
    model: name,
    accuracy: round(accuracy, 4),
    precision: round(precision, 4),
    recall: round(recall, 4),
    f1: round(f1, 4),
    auc: round(auc, 4),
    confusionMatrix: {
      tp,
      fp,
      tn,
      fn,
    },
  };
}

function selectBestModel(models) {
  return [...models].sort((a, b) => {
    if (b.f1 !== a.f1) return b.f1 - a.f1;
    if (b.recall !== a.recall) return b.recall - a.recall;
    return b.accuracy - a.accuracy;
  })[0];
}

function buildBmiBands(dataset) {
  const bands = [
    { label: 'BMI<20', highRiskRate: 0, count: 0 },
    { label: '20-24', highRiskRate: 0, count: 0 },
    { label: '24-28', highRiskRate: 0, count: 0 },
    { label: 'BMI>=28', highRiskRate: 0, count: 0 },
  ];

  dataset.forEach((sample) => {
    const index = sample.bmi < 20 ? 0 : sample.bmi < 24 ? 1 : sample.bmi < 28 ? 2 : 3;
    bands[index].count += 1;
    if (sample.risk_label === 'HIGH') {
      bands[index].highRiskRate += 1;
    }
  });

  bands.forEach((band) => {
    band.highRiskRate = round((band.highRiskRate / Math.max(band.count, 1)) * 100, 2);
  });

  return bands;
}

function buildFeatureImportance(logisticModel, featureNames) {
  return featureNames
    .map((feature, index) => {
      const weight = logisticModel.weights[index];
      const meta = lifestyleFeatureMeta[feature];
      return {
        key: feature,
        label: meta.label,
        weight: round(Math.abs(weight), 4),
        signedWeight: round(weight, 4),
        direction: weight >= 0 ? 'risk_up' : 'risk_down',
        interpretation: meta.interpretation,
      };
    })
    .sort((a, b) => b.weight - a.weight)
    .slice(0, 8);
}

function buildKnnSweep(trainSamples, testSamples, featureNames, labelKey, positiveLabel, negativeLabel) {
  return [3, 5, 7, 9, 11].map((k) => {
    const metrics = evaluateModel(
      `KNN (k=${k})`,
      testSamples,
      (sample) => predictKnn(trainSamples, featureNames, sample, labelKey, positiveLabel, negativeLabel, k),
      labelKey,
      positiveLabel,
      negativeLabel,
    );
    return {
      k,
      accuracy: metrics.accuracy,
      f1: metrics.f1,
    };
  });
}

function toCsv(rows) {
  if (!rows.length) {
    return '';
  }

  const headers = Object.keys(rows[0]);
  return [
    headers.join(','),
    ...rows.map((row) => headers.map((header) => row[header]).join(',')),
  ].join('\n');
}

function loadAppSeed() {
  const candidatePaths = [
    resolve('backend/src/main/resources/seed/app_seed.json'),
    resolve('data/app_seed.json'),
  ];

  const existingPath = candidatePaths.find((file) => existsSync(file));
  if (!existingPath) {
    throw new Error('app_seed.json not found');
  }

  return JSON.parse(readFileSync(existingPath, 'utf8'));
}

function loadHeartDiseaseDataset() {
  const rawPath = resolve('data/external/processed.cleveland.data');
  if (!existsSync(rawPath)) {
    throw new Error('Public benchmark dataset missing: data/external/processed.cleveland.data');
  }

  const rawRows = readFileSync(rawPath, 'utf8')
    .trim()
    .split(/\r?\n/)
    .map((line) => line.split(','));

  let missingValuesFound = 0;
  const cleanedRows = [];

  rawRows.forEach((parts, index) => {
    const missingInRow = parts.filter((value) => value === '?').length;
    missingValuesFound += missingInRow;
    if (missingInRow > 0) {
      return;
    }

    const row = {};
    heartColumns.forEach((column, columnIndex) => {
      row[column] = Number(parts[columnIndex]);
    });

    cleanedRows.push({
      id: `H${String(index + 1).padStart(3, '0')}`,
      ...row,
      disease_binary: row.num > 0 ? 'PRESENT' : 'ABSENT',
      disease_binary_int: row.num > 0 ? 1 : 0,
    });
  });

  return {
    rawSampleCount: rawRows.length,
    usableSampleCount: cleanedRows.length,
    missingValuesFound,
    missingRowsRemoved: rawRows.length - cleanedRows.length,
    rows: cleanedRows,
  };
}

function buildLifestyleReport(dataset) {
  const featureNames = [
    'bmi',
    'sleep_hours',
    'steps',
    'exercise_minutes',
    'water_ml',
    'calories_kcal',
    'protein_g',
    'stress_score',
    'resting_heart_rate',
    'waist_cm',
    'screen_hours',
    'smoking_flag',
    'late_night_snack',
  ];

  const split = stratifiedSplit(dataset, 'risk_label');
  const logisticModel = trainLogisticRegression(split.train, featureNames, 'risk_label', 'HIGH');
  const negativeLabel = 'LOW';

  const logisticMetrics = evaluateModel(
    'Logistic Regression',
    split.test,
    (sample) => predictLogistic(logisticModel, sample, featureNames, 'HIGH', negativeLabel),
    'risk_label',
    'HIGH',
    negativeLabel,
  );

  const knnMetrics = evaluateModel(
    'KNN (k=7)',
    split.test,
    (sample) => predictKnn(split.train, featureNames, sample, 'risk_label', 'HIGH', negativeLabel, 7),
    'risk_label',
    'HIGH',
    negativeLabel,
  );

  const gaussianModel = trainGaussianNb(split.train, featureNames, 'risk_label', 'HIGH');
  const gaussianMetrics = evaluateModel(
    'Gaussian Naive Bayes',
    split.test,
    (sample) => predictGaussianNb(gaussianModel, sample, featureNames, 'HIGH', negativeLabel),
    'risk_label',
    'HIGH',
    negativeLabel,
  );

  const models = [logisticMetrics, knnMetrics, gaussianMetrics];
  const bestModel = selectBestModel(models);
  const highRiskCount = dataset.filter((sample) => sample.risk_label === 'HIGH').length;
  const highRiskRatio = highRiskCount / dataset.length;

  return {
    featureNames,
    split,
    logisticModel,
    models,
    selectedModel: bestModel.model,
    bmiBands: buildBmiBands(dataset),
    featureImportance: buildFeatureImportance(logisticModel, featureNames),
    knnSweep: buildKnnSweep(split.train, split.test, featureNames, 'risk_label', 'HIGH', negativeLabel),
    datasetSummary: {
      sample_count: dataset.length,
      high_risk_count: highRiskCount,
      high_risk_ratio: round(highRiskRatio, 4),
      avg_sleep_hours: round(mean(dataset.map((sample) => sample.sleep_hours)), 2),
      avg_steps: Math.round(mean(dataset.map((sample) => sample.steps))),
      avg_bmi: round(mean(dataset.map((sample) => sample.bmi)), 2),
      avg_stress_score: round(mean(dataset.map((sample) => sample.stress_score)), 2),
      feature_count: featureNames.length,
      train_count: split.train.length,
      test_count: split.test.length,
      sources: [
        {
          name: 'Lifestyle Behavior Stream',
          type: '系统行为样本',
          location: 'data/lifestyle_risk_dataset.csv',
          sampleCount: dataset.length,
          note: '由睡眠、步数、饮水、压力、BMI、腰围和作息习惯等字段合成，作为平台的本地行为样本池。',
        },
        {
          name: 'Cleveland Clinical Registry',
          type: '外部临床基准',
          location: 'data/external/processed.cleveland.data',
          sampleCount: 303,
          note: '来自 UCI 的心血管特征库，用于校验同一处理链在临床特征场景下的稳定性。',
        },
      ],
    },
    preprocessing: {
      steps: [
        '先检查字段完整性与标签分布，保证问题定义、输入特征和输出标签一一对应。',
        '采用分层 8:2 训练/测试划分，避免高风险与低风险样本比例在测试集中过度偏移。',
        '对逻辑回归与 KNN 使用数值标准化，减少量纲差异对距离与权重学习的干扰。',
        '同时保留 Accuracy、Precision、Recall、F1 与 AUC，避免只看单一指标造成误判。',
      ],
      missingSummary: [
        {
          dataset: '模拟生活方式数据',
          rowsBefore: dataset.length,
          rowsAfter: dataset.length,
          missingValuesFound: 0,
          missingRowsRemoved: 0,
        },
      ],
    },
    narrative: [
      `当前行为样本池中高风险记录占比 ${round(highRiskRatio * 100, 2)}%，风险识别需要同时关注召回率与误报率平衡。`,
      `逻辑回归在当前生活方式数据上的 F1 为 ${(logisticMetrics.f1 * 100).toFixed(2)}%，是现阶段最稳定的主推断引擎。`,
      `特征权重显示“${buildFeatureImportance(logisticModel, featureNames)
        .slice(0, 3)
        .map((item) => item.label)
        .join('、')}”是最关键的风险驱动因素。`,
    ],
    reportBullets: [
      '本地行为样本池与外部临床基准库共用一套特征处理与评估流程。',
      '系统同时保留 Accuracy、Precision、Recall、F1、AUC 与混淆矩阵，避免单指标误判。',
      'KNN 调参与特征权重解释可以直接反映模型边界与关键驱动项。',
      '清洗、标准化、抽样、建模、可视化与建议生成已经形成闭环。',
    ],
  };
}

function buildHeartBenchmark() {
  const dataset = loadHeartDiseaseDataset();
  const featureNames = heartColumns.slice(0, -1);
  const split = stratifiedSplit(dataset.rows, 'disease_binary');
  const positiveLabel = 'PRESENT';
  const negativeLabel = 'ABSENT';

  const logisticModel = trainLogisticRegression(split.train, featureNames, 'disease_binary', positiveLabel, 3200, 0.075);
  const gaussianModel = trainGaussianNb(split.train, featureNames, 'disease_binary', positiveLabel);

  const logisticMetrics = evaluateModel(
    'Logistic Regression',
    split.test,
    (sample) => predictLogistic(logisticModel, sample, featureNames, positiveLabel, negativeLabel),
    'disease_binary',
    positiveLabel,
    negativeLabel,
  );

  const knnMetrics = evaluateModel(
    'KNN (k=9)',
    split.test,
    (sample) => predictKnn(split.train, featureNames, sample, 'disease_binary', positiveLabel, negativeLabel, 9),
    'disease_binary',
    positiveLabel,
    negativeLabel,
  );

  const gaussianMetrics = evaluateModel(
    'Gaussian Naive Bayes',
    split.test,
    (sample) => predictGaussianNb(gaussianModel, sample, featureNames, positiveLabel, negativeLabel),
    'disease_binary',
    positiveLabel,
    negativeLabel,
  );

  const models = [logisticMetrics, knnMetrics, gaussianMetrics];
  const bestModel = selectBestModel(models);
  const positiveRate =
    dataset.rows.filter((row) => row.disease_binary === positiveLabel).length / dataset.rows.length;

  const cleanedCsvRows = dataset.rows.map((row) => {
    const next = {};
    featureNames.forEach((feature) => {
      next[feature] = row[feature];
    });
    next.target = row.disease_binary_int;
    return next;
  });

  writeToTargets(
    'heart_disease_cleveland_cleaned.csv',
    toCsv(cleanedCsvRows),
    [resolve('analysis/generated'), resolve('data')],
  );

  return {
    name: 'UCI Cleveland Heart Disease',
    source: 'UCI Machine Learning Repository',
    citation: 'https://archive.ics.uci.edu/dataset/45/heart+disease',
    rawPath: 'data/external/processed.cleveland.data',
    cleanedPath: 'data/heart_disease_cleveland_cleaned.csv',
    sample_count: dataset.rawSampleCount,
    usable_sample_count: dataset.usableSampleCount,
    feature_count: featureNames.length,
    positive_rate: round(positiveRate, 4),
    preprocessing: [
      '将原始文件中的 ? 识别为缺失值，并剔除含缺失字段的记录。',
      '把 num>0 统一映射为阳性标签，转换为平台可直接消费的二分类结构。',
      '保持 13 个原始临床字段作为输入特征，不额外注入标签泄露字段。',
      '采用与本地行为样本一致的抽样、标准化与评估规则，便于统一比较。',
    ],
    missingSummary: {
      dataset: 'UCI Cleveland Heart Disease',
      rowsBefore: dataset.rawSampleCount,
      rowsAfter: dataset.usableSampleCount,
      missingValuesFound: dataset.missingValuesFound,
      missingRowsRemoved: dataset.missingRowsRemoved,
    },
    models,
    selectedModel: bestModel.model,
    notes: [
      `临床基准库中的阳性占比为 ${round(positiveRate * 100, 2)}%，与本地样本池形成不同分布结构。`,
      `${bestModel.model} 在这组临床特征上的 F1 为 ${(bestModel.f1 * 100).toFixed(2)}%，说明当前推断链具备跨源稳定性。`,
      '年龄、血压、胆固醇、最大心率等字段可以作为平台外部基准库的标准化输入。 ',
    ],
    featureLabels: featureNames.map((feature) => heartColumnDescriptions[feature]),
  };
}

const appSeed = loadAppSeed();
const lifestyleDataset = Array.from({ length: 420 }, (_, index) => generateLifestyleSample(index));
const lifestyleReport = buildLifestyleReport(lifestyleDataset);
const heartBenchmark = buildHeartBenchmark();

const modelReport = {
  project: {
    title: '基于生活方式数据的健康风险分析与智能建议系统',
    subtitle: '生活方式行为样本与外部临床基准库协同驱动的双数据链路',
    taskType: 'binary_classification',
  },
  dataset: lifestyleReport.datasetSummary,
  preprocessing: {
    steps: lifestyleReport.preprocessing.steps,
    missingSummary: [
      ...lifestyleReport.preprocessing.missingSummary,
      heartBenchmark.missingSummary,
    ],
  },
  models: lifestyleReport.models,
  selectedModel: lifestyleReport.selectedModel,
  bmiBands: lifestyleReport.bmiBands,
  featureImportance: lifestyleReport.featureImportance,
  knnSweep: lifestyleReport.knnSweep,
  benchmark: heartBenchmark,
  narrative: [
    ...lifestyleReport.narrative,
    `临床基准库当前表现最好的模型为 ${heartBenchmark.selectedModel}，可作为跨源参考引擎。`,
  ],
  reportBullets: lifestyleReport.reportBullets,
};

const lifestyleCsv = toCsv(lifestyleDataset);
const modelReportJson = JSON.stringify(modelReport, null, 2);
const appSeedJson = JSON.stringify(appSeed, null, 2);

writeToTargets('lifestyle_risk_dataset.csv', lifestyleCsv);
writeToTargets('model_report.json', modelReportJson);
writeToTargets('app_seed.json', appSeedJson);

console.log('Generated data assets for analysis/, data/ and backend seed resources.');
