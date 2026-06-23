package com.lightbalance.health.dto;

import java.util.List;

public final class SeedData {

    private SeedData() {
    }

    public record AppSeed(
        ProfileSeed profile,
        SummarySeed todaySummary,
        List<TrendSeed> trends,
        List<MealSeed> meals,
        List<WorkoutSeed> workouts,
        List<ConversationSeed> assistantConversation,
        List<InsightSeed> insights,
        List<String> coachPrompts
    ) {
    }

    public record ProfileSeed(
        String name,
        String handle,
        String goal,
        double weight,
        double targetWeight,
        double bmi,
        double bodyFat,
        double visceralFat,
        double muscleRate,
        double basalMetabolism,
        int healthScore,
        String riskLevel
    ) {
    }

    public record SummarySeed(
        String date,
        int calories,
        int calorieTarget,
        int water,
        int waterTarget,
        int protein,
        int proteinTarget,
        int carbs,
        int carbsTarget,
        int fat,
        int fatTarget,
        double sleepHours,
        int sleepScore,
        int steps,
        int stepTarget,
        int workoutMinutes,
        int workoutTarget,
        int moodScore,
        int stressScore
    ) {
    }

    public record TrendSeed(
        String date,
        double weight,
        double sleepHours,
        int steps,
        int calories,
        int stressScore
    ) {
    }

    public record MealSeed(
        String name,
        String mealType,
        String portion,
        int calories,
        int protein,
        int carbs,
        int fat,
        boolean eaten,
        String recommendedTime
    ) {
    }

    public record WorkoutSeed(
        String title,
        String category,
        int duration,
        int caloriesBurned,
        String intensity,
        boolean completed
    ) {
    }

    public record ConversationSeed(
        String speaker,
        String tag,
        String title,
        String content,
        String time
    ) {
    }

    public record InsightSeed(
        String title,
        String description,
        String tone
    ) {
    }

    public record ModelReport(
        ProjectSeed project,
        DatasetSummarySeed dataset,
        PreprocessingSeed preprocessing,
        List<ModelMetricSeed> models,
        String selectedModel,
        List<BmiBandSeed> bmiBands,
        List<FeatureImpactSeed> featureImportance,
        List<KnnSweepSeed> knnSweep,
        BenchmarkSeed benchmark,
        List<String> narrative,
        List<String> reportBullets
    ) {
    }

    public record PreprocessingAuditSeed(
        ToolingSeed tooling,
        DatasetAuditSeed lifestyle,
        DatasetAuditSeed benchmark
    ) {
    }

    public record ToolingSeed(
        String scriptPath,
        String runtime,
        String library,
        String libraryVersion
    ) {
    }

    public record DatasetAuditSeed(
        String dataset,
        String path,
        int rows,
        int columns,
        String labelColumn,
        java.util.Map<String, Integer> labelDistribution,
        int missingValuesFound,
        int missingRowsRemoved,
        int duplicateRowsFound,
        int duplicateRowsRemoved,
        int invalidRowsFound,
        int invalidRowsRemoved,
        List<FieldDictionarySeed> fieldDictionary,
        List<RangeCheckSeed> rangeChecks,
        List<OutlierCheckSeed> outlierChecks,
        List<String> actions,
        List<String> notes
    ) {
    }

    public record FieldDictionarySeed(
        String key,
        String label,
        String dtype,
        String unit,
        String meaning
    ) {
    }

    public record RangeCheckSeed(
        String field,
        String validRange,
        int invalidCount,
        String action
    ) {
    }

    public record OutlierCheckSeed(
        String field,
        int outlierCount,
        String method,
        String action
    ) {
    }

    public record ProjectSeed(
        String title,
        String subtitle,
        String taskType
    ) {
    }

    public record DatasetSummarySeed(
        int sample_count,
        int high_risk_count,
        double high_risk_ratio,
        double avg_sleep_hours,
        int avg_steps,
        double avg_bmi,
        double avg_stress_score,
        int feature_count,
        int train_count,
        int test_count,
        List<DataSourceSeed> sources
    ) {
    }

    public record DataSourceSeed(
        String name,
        String type,
        String location,
        int sampleCount,
        String note
    ) {
    }

    public record PreprocessingSeed(
        List<String> steps,
        List<MissingValueSeed> missingSummary
    ) {
    }

    public record MissingValueSeed(
        String dataset,
        int rowsBefore,
        int rowsAfter,
        int missingValuesFound,
        int missingRowsRemoved
    ) {
    }

    public record ModelMetricSeed(
        String model,
        double accuracy,
        double precision,
        double recall,
        double f1,
        double auc,
        ConfusionMatrixSeed confusionMatrix
    ) {
    }

    public record ConfusionMatrixSeed(
        int tp,
        int fp,
        int tn,
        int fn
    ) {
    }

    public record BmiBandSeed(
        String label,
        double highRiskRate,
        int count
    ) {
    }

    public record FeatureImpactSeed(
        String key,
        String label,
        double weight,
        double signedWeight,
        String direction,
        String interpretation
    ) {
    }

    public record KnnSweepSeed(
        int k,
        double accuracy,
        double f1
    ) {
    }

    public record BenchmarkSeed(
        String name,
        String source,
        String citation,
        String rawPath,
        String cleanedPath,
        int sample_count,
        int usable_sample_count,
        int feature_count,
        double positive_rate,
        List<String> preprocessing,
        MissingValueSeed missingSummary,
        List<ModelMetricSeed> models,
        String selectedModel,
        List<String> notes,
        List<String> featureLabels
    ) {
    }

    public record DatasetRow(
        String id,
        double bmi,
        double sleepHours,
        int stressScore,
        int steps,
        int waterMl,
        String riskLabel
    ) {
    }
}
