import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';

const outDir = resolve('analysis/generated');
mkdirSync(outDir, { recursive: true });

function createRng(seed = 20260518) {
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

function generateSample(index) {
  const age = int(19, 52);
  const gender = rand() > 0.5 ? 1 : 0;
  const heightCm = round(gender ? range(168, 188) : range(154, 175), 1);
  const baselineWeight = gender ? range(60, 95) : range(47, 78);
  const lifestyleShift = range(-7, 10);
  const weightKg = round(baselineWeight + lifestyleShift, 1);
  const bmiValue = round(bmi(weightKg, heightCm), 2);
  const sleepHours = round(range(4.7, 8.9), 1);
  const steps = int(1800, 15800);
  const exerciseMinutes = int(0, 110);
  const waterMl = int(600, 3000);
  const caloriesKcal = int(1350, 3200);
  const proteinG = int(35, 165);
  const stressScore = int(18, 92);
  const restingHeartRate = int(55, 102);
  const waistCm = round(range(64, 110), 1);
  const screenHours = round(range(2.1, 10.3), 1);
  const smokingFlag = rand() > 0.86 ? 1 : 0;
  const lateNightSnack = rand() > 0.58 ? 1 : 0;

  const riskScore =
    (bmiValue - 22.5) * 0.18 +
    (6.8 - sleepHours) * 0.42 +
    (7000 - steps) / 2500 +
    (25 - exerciseMinutes) / 18 +
    (1700 - waterMl) / 520 +
    (stressScore - 58) / 14 +
    (restingHeartRate - 74) / 11 +
    (waistCm - 84) / 10 +
    (screenHours - 5.2) / 2.6 +
    smokingFlag * 1.4 +
    lateNightSnack * 0.75 +
    (caloriesKcal > 2550 ? 0.9 : 0) +
    (proteinG < 65 ? 0.5 : 0) +
    range(-0.95, 0.95);

  const riskLabel = riskScore > 1.55 ? 'HIGH' : 'LOW';

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

function standardize(samples, featureNames) {
  const means = {};
  const stds = {};
  featureNames.forEach((name) => {
    const values = samples.map((sample) => Number(sample[name]));
    const mean = values.reduce((sum, value) => sum + value, 0) / values.length;
    const variance =
      values.reduce((sum, value) => sum + (value - mean) ** 2, 0) / values.length;
    means[name] = mean;
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

function trainLogisticRegression(trainSamples, featureNames, iterations = 2400, lr = 0.09) {
  const scaler = standardize(trainSamples, featureNames);
  const xs = trainSamples.map((sample) => vectorize(sample, featureNames, scaler));
  const ys = trainSamples.map((sample) => (sample.risk_label === 'HIGH' ? 1 : 0));

  const weights = new Array(featureNames.length).fill(0);
  let bias = 0;

  for (let step = 0; step < iterations; step += 1) {
    const gradW = new Array(weights.length).fill(0);
    let gradB = 0;

    for (let i = 0; i < xs.length; i += 1) {
      const z = xs[i].reduce((sum, value, index) => sum + value * weights[index], bias);
      const prediction = sigmoid(z);
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

function predictLogistic(model, sample, featureNames) {
  const features = vectorize(sample, featureNames, model.scaler);
  const score = features.reduce(
    (sum, value, index) => sum + value * model.weights[index],
    model.bias,
  );
  const probability = sigmoid(score);
  return {
    probability: round(probability, 4),
    label: probability >= 0.5 ? 'HIGH' : 'LOW',
  };
}

function predictKnn(trainSamples, featureNames, sample, k = 7) {
  const scaler = standardize(trainSamples, featureNames);
  const target = vectorize(sample, featureNames, scaler);
  const ranked = trainSamples
    .map((trainSample) => {
      const current = vectorize(trainSample, featureNames, scaler);
      const distance = Math.sqrt(
        current.reduce((sum, value, index) => sum + (value - target[index]) ** 2, 0),
      );
      return { label: trainSample.risk_label, distance };
    })
    .sort((a, b) => a.distance - b.distance)
    .slice(0, k);

  const highVotes = ranked.filter((item) => item.label === 'HIGH').length;
  const probability = highVotes / k;
  return {
    probability: round(probability, 4),
    label: probability >= 0.5 ? 'HIGH' : 'LOW',
  };
}

function evaluateModel(name, predictFn, testSamples) {
  let tp = 0;
  let fp = 0;
  let tn = 0;
  let fn = 0;

  const scored = testSamples.map((sample) => {
    const result = predictFn(sample);
    const actualHigh = sample.risk_label === 'HIGH';
    const predictedHigh = result.label === 'HIGH';
    if (actualHigh && predictedHigh) tp += 1;
    if (!actualHigh && predictedHigh) fp += 1;
    if (!actualHigh && !predictedHigh) tn += 1;
    if (actualHigh && !predictedHigh) fn += 1;
    return {
      id: sample.id,
      actual: sample.risk_label,
      predicted: result.label,
      probability: result.probability,
    };
  });

  const accuracy = (tp + tn) / testSamples.length;
  const precision = tp / Math.max(tp + fp, 1);
  const recall = tp / Math.max(tp + fn, 1);
  const f1 = (2 * precision * recall) / Math.max(precision + recall, 1e-9);

  return {
    model: name,
    accuracy: round(accuracy, 4),
    precision: round(precision, 4),
    recall: round(recall, 4),
    f1: round(f1, 4),
    confusionMatrix: {
      tp,
      fp,
      tn,
      fn,
    },
    predictions: scored,
  };
}

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

const dataset = Array.from({ length: 320 }, (_, index) => generateSample(index));
const shuffled = shuffle(dataset);
const split = Math.floor(shuffled.length * 0.8);
const trainSamples = shuffled.slice(0, split);
const testSamples = shuffled.slice(split);

const logisticModel = trainLogisticRegression(trainSamples, featureNames);
const logisticReport = evaluateModel(
  'Logistic Regression',
  (sample) => predictLogistic(logisticModel, sample, featureNames),
  testSamples,
);
const knnReport = evaluateModel(
  'KNN (k=7)',
  (sample) => predictKnn(trainSamples, featureNames, sample),
  testSamples,
);

const overallStats = {
  sample_count: dataset.length,
  high_risk_count: dataset.filter((sample) => sample.risk_label === 'HIGH').length,
  avg_sleep_hours: round(dataset.reduce((sum, item) => sum + item.sleep_hours, 0) / dataset.length, 2),
  avg_steps: Math.round(dataset.reduce((sum, item) => sum + item.steps, 0) / dataset.length),
  avg_bmi: round(dataset.reduce((sum, item) => sum + item.bmi, 0) / dataset.length, 2),
  avg_stress_score: round(
    dataset.reduce((sum, item) => sum + item.stress_score, 0) / dataset.length,
    2,
  ),
};

const riskByBmiBand = [
  { label: 'BMI<20', highRiskRate: 0, count: 0 },
  { label: '20-24', highRiskRate: 0, count: 0 },
  { label: '24-28', highRiskRate: 0, count: 0 },
  { label: 'BMI>=28', highRiskRate: 0, count: 0 },
];

dataset.forEach((sample) => {
  const bandIndex = sample.bmi < 20 ? 0 : sample.bmi < 24 ? 1 : sample.bmi < 28 ? 2 : 3;
  riskByBmiBand[bandIndex].count += 1;
  if (sample.risk_label === 'HIGH') riskByBmiBand[bandIndex].highRiskRate += 1;
});

riskByBmiBand.forEach((band) => {
  band.highRiskRate = round((band.highRiskRate / Math.max(band.count, 1)) * 100, 2);
});

const appSeed = {
  profile: {
    name: '小明老师',
    handle: '@admin',
    goal: '恢复代谢节律，稳定体重与精力',
    weight: 64.6,
    targetWeight: 61.5,
    bmi: 22.8,
    bodyFat: 28.2,
    visceralFat: 7,
    muscleRate: 41.6,
    basalMetabolism: 1364,
    healthScore: 84,
    riskLevel: '低风险',
  },
  todaySummary: {
    date: '2026-05-18',
    calories: 1480,
    calorieTarget: 1650,
    water: 1450,
    waterTarget: 2200,
    protein: 92,
    proteinTarget: 96,
    carbs: 156,
    carbsTarget: 205,
    fat: 39,
    fatTarget: 48,
    sleepHours: 7.3,
    sleepScore: 86,
    steps: 8630,
    stepTarget: 10000,
    workoutMinutes: 48,
    workoutTarget: 60,
    moodScore: 78,
    stressScore: 42,
  },
  trends: [
    ['05-05', 65.8, 6.4, 6200, 1700, 48],
    ['05-06', 65.5, 6.8, 7100, 1620, 46],
    ['05-07', 65.3, 7.1, 8600, 1580, 44],
    ['05-08', 65.2, 7.4, 9200, 1510, 41],
    ['05-09', 65.0, 7.0, 8300, 1490, 43],
    ['05-10', 64.9, 7.2, 9800, 1540, 40],
    ['05-11', 64.8, 7.0, 10120, 1610, 38],
    ['05-12', 64.9, 6.7, 7600, 1660, 51],
    ['05-13', 64.7, 7.5, 10800, 1520, 39],
    ['05-14', 64.7, 7.2, 8800, 1480, 42],
    ['05-15', 64.6, 7.1, 9100, 1500, 40],
    ['05-16', 64.5, 7.4, 11200, 1460, 37],
    ['05-17', 64.5, 7.0, 8450, 1530, 43],
    ['05-18', 64.6, 7.3, 8630, 1480, 42],
  ].map(([date, weight, sleep, stepsValue, caloriesValue, stress]) => ({
    date,
    weight,
    sleepHours: sleep,
    steps: stepsValue,
    calories: caloriesValue,
    stressScore: stress,
  })),
  meals: [
    {
      name: '高蛋白酸奶燕麦杯',
      mealType: '早餐',
      portion: '1 份',
      calories: 320,
      protein: 24,
      carbs: 36,
      fat: 8,
      eaten: true,
      recommendedTime: '08:10',
    },
    {
      name: '鸡胸糙米能量碗',
      mealType: '午餐',
      portion: '1 盒',
      calories: 510,
      protein: 38,
      carbs: 54,
      fat: 12,
      eaten: true,
      recommendedTime: '12:20',
    },
    {
      name: '香煎鳕鱼时蔬盘',
      mealType: '晚餐',
      portion: '1 盘',
      calories: 420,
      protein: 32,
      carbs: 22,
      fat: 15,
      eaten: true,
      recommendedTime: '18:40',
    },
    {
      name: '香蕉花生酱吐司',
      mealType: '加餐',
      portion: '1 份',
      calories: 230,
      protein: 8,
      carbs: 30,
      fat: 9,
      eaten: false,
      recommendedTime: '16:30',
    },
  ],
  workouts: [
    {
      title: '代谢唤醒快走',
      category: '低冲击有氧',
      duration: 25,
      caloriesBurned: 170,
      intensity: '轻中强度',
      completed: true,
    },
    {
      title: '核心稳定训练',
      category: '力量耐力',
      duration: 18,
      caloriesBurned: 110,
      intensity: '中强度',
      completed: true,
    },
    {
      title: '晚间拉伸放松',
      category: '恢复训练',
      duration: 15,
      caloriesBurned: 40,
      intensity: '舒缓',
      completed: false,
    },
  ],
  assistantConversation: [
    {
      speaker: 'assistant',
      tag: 'Astra',
      title: '在线健康助理',
      content:
        '我已经同步了你今天的饮食、训练和恢复节奏。继续告诉我新的状态变化，我会给你更贴近当天目标的建议。',
      time: '08:15',
    },
    {
      speaker: 'user',
      tag: '你',
      title: '综合平衡',
      content: '午饭后有点困，下午还要开会，怎么调整更稳？',
      time: '13:20',
    },
    {
      speaker: 'assistant',
      tag: 'Astra',
      title: '恢复建议',
      content:
        '优先补 300 到 400 ml 水，接着做 5 分钟站立伸展。你今天碳水摄入已经够用，下午加餐选无糖酸奶或一小把坚果，比再喝甜饮更稳。',
      time: '13:23',
    },
  ],
  insights: [
    {
      title: '蛋白质接近达标',
      description: '距离目标还差 4 g，晚间加一杯无糖豆浆就能补齐。',
      tone: 'good',
    },
    {
      title: '饮水还需补充',
      description: '今天还差 750 ml，建议拆成 3 次喝完，避免睡前一次性补水。',
      tone: 'focus',
    },
    {
      title: '睡眠节律维持稳定',
      description: '最近 7 天平均睡眠 7.17 小时，恢复分数维持在较好水平。',
      tone: 'calm',
    },
  ],
  coachPrompts: [
    '我刚吃了一份水果酸奶，要不要补录？',
    '今天加班，只能做 15 分钟训练，怎么安排？',
    '我想把体脂降一点，晚餐该怎么换？',
  ],
};

const report = {
  dataset: overallStats,
  models: [logisticReport, knnReport],
  selectedModel: logisticReport.accuracy >= knnReport.accuracy ? logisticReport.model : knnReport.model,
  bmiBands: riskByBmiBand,
  narrative: [
    '睡眠不足、步数偏低、压力偏高与高风险标签呈显著同向关系。',
    '逻辑回归在模拟健康数据上表现更稳定，便于解释各项特征如何影响风险概率。',
    'KNN 对局部样本模式较敏感，但在高压力与高 BMI 交叉区域更容易受邻居分布波动影响。',
  ],
};

const csvHeaders = Object.keys(dataset[0]);
const csvContent = [
  csvHeaders.join(','),
  ...dataset.map((row) => csvHeaders.map((header) => row[header]).join(',')),
].join('\n');

writeFileSync(resolve(outDir, 'lifestyle_risk_dataset.csv'), csvContent, 'utf8');
writeFileSync(resolve(outDir, 'model_report.json'), JSON.stringify(report, null, 2), 'utf8');
writeFileSync(resolve(outDir, 'app_seed.json'), JSON.stringify(appSeed, null, 2), 'utf8');

console.log(`Generated files in ${dirname(resolve(outDir, 'app_seed.json'))}`);
