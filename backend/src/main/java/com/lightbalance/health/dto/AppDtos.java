package com.lightbalance.health.dto;

import java.util.List;

public final class AppDtos {

    private AppDtos() {
    }

    public record DashboardResponse(
        ProfileCard profile,
        SummaryCard summary,
        List<MacroProgress> macros,
        List<MealCard> meals,
        List<WorkoutCard> workouts,
        List<InsightCard> insights,
        List<TrendPoint> trends,
        List<String> coachPrompts,
        RecoverySignal recoverySignal
    ) {
    }

    public record ProfileCard(
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

    public record SummaryCard(
        String date,
        int calories,
        int calorieTarget,
        int water,
        int waterTarget,
        int sleepScore,
        double sleepHours,
        int steps,
        int stepTarget,
        int workoutMinutes,
        int workoutTarget,
        int moodScore,
        int stressScore
    ) {
    }

    public record MacroProgress(
        String label,
        int value,
        int target,
        String unit,
        String tone
    ) {
    }

    public record MealCard(
        Long id,
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

    public record WorkoutCard(
        Long id,
        String title,
        String category,
        int duration,
        int caloriesBurned,
        String intensity,
        boolean completed
    ) {
    }

    public record InsightCard(
        String title,
        String description,
        String tone
    ) {
    }

    public record TrendPoint(
        String date,
        double weight,
        double sleepHours,
        int steps,
        int calories,
        int stressScore
    ) {
    }

    public record RecoverySignal(
        String headline,
        String subline,
        String badge
    ) {
    }

    public record WaterRequest(int amount) {
    }

    public record ProfileUpdateRequest(
        String name,
        String handle,
        String goal,
        double weight,
        double targetWeight,
        double bodyFat,
        double visceralFat,
        double muscleRate
    ) {
    }

    public record DailyCheckinRequest(
        int calorieTarget,
        int waterTarget,
        int stepTarget,
        int workoutTarget,
        double sleepHours,
        int steps,
        int moodScore,
        int stressScore
    ) {
    }

    public record MealCreateRequest(
        String name,
        String mealType,
        String portion,
        int calories,
        int protein,
        int carbs,
        int fat,
        String recommendedTime,
        boolean eaten
    ) {
    }

    public record NutritionSearchResponse(
        String query,
        int total,
        String sourceSummary,
        List<NutritionFoodOption> items
    ) {
    }

    public record NutritionFoodOption(
        String id,
        String name,
        String category,
        int caloriesPer100g,
        double proteinPer100g,
        double carbsPer100g,
        double fatPer100g,
        double fiberPer100g,
        String source
    ) {
    }

    public record NutritionEstimateRequest(
        String query,
        int grams
    ) {
    }

    public record NutritionEstimateResponse(
        String query,
        String matchedName,
        String category,
        int grams,
        String portion,
        int calories,
        double protein,
        double carbs,
        double fat,
        double fiber,
        String confidence,
        String source,
        String note,
        List<NutritionFoodOption> relatedFoods
    ) {
    }

    public record WorkoutCreateRequest(
        String title,
        String category,
        int duration,
        int caloriesBurned,
        String intensity
    ) {
    }

    public record TrendCreateRequest(
        String recordDate,
        double weight,
        double sleepHours,
        int steps,
        int calories,
        int stressScore
    ) {
    }

    public record AnalyticsResponse(
        ProjectSummary project,
        DatasetSummary datasetSummary,
        PreprocessingSummary preprocessing,
        PreprocessingAudit preprocessingAudit,
        List<ModelMetric> models,
        String selectedModel,
        List<BmiBand> bmiBands,
        List<ScatterPoint> scatterPoints,
        List<ConfusionCell> confusionMatrix,
        List<FeatureImpact> featureImportance,
        List<KnnSweepPoint> knnSweep,
        BenchmarkSummary benchmark,
        List<String> narrative,
        List<String> reportBullets
    ) {
    }

    public record PreprocessingAudit(
        ToolingInfo tooling,
        DatasetAudit lifestyle,
        DatasetAudit benchmark
    ) {
    }

    public record ToolingInfo(
        String scriptPath,
        String runtime,
        String library,
        String libraryVersion
    ) {
    }

    public record DatasetAudit(
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
        List<FieldDictionaryItem> fieldDictionary,
        List<RangeCheckItem> rangeChecks,
        List<OutlierCheckItem> outlierChecks,
        List<String> actions,
        List<String> notes
    ) {
    }

    public record FieldDictionaryItem(
        String key,
        String label,
        String dtype,
        String unit,
        String meaning
    ) {
    }

    public record RangeCheckItem(
        String field,
        String validRange,
        int invalidCount,
        String action
    ) {
    }

    public record OutlierCheckItem(
        String field,
        int outlierCount,
        String method,
        String action
    ) {
    }

    public record ProjectSummary(
        String title,
        String subtitle,
        String taskType
    ) {
    }

    public record DatasetSummary(
        int sampleCount,
        int highRiskCount,
        double highRiskRatio,
        double avgSleepHours,
        int avgSteps,
        double avgBmi,
        double avgStressScore,
        int featureCount,
        int trainCount,
        int testCount,
        List<DataSourceCard> sources
    ) {
    }

    public record DataSourceCard(
        String name,
        String type,
        String location,
        int sampleCount,
        String note
    ) {
    }

    public record PreprocessingSummary(
        List<String> steps,
        List<MissingValueCard> missingSummary
    ) {
    }

    public record MissingValueCard(
        String dataset,
        int rowsBefore,
        int rowsAfter,
        int missingValuesFound,
        int missingRowsRemoved
    ) {
    }

    public record ModelMetric(
        String model,
        double accuracy,
        double precision,
        double recall,
        double f1,
        double auc
    ) {
    }

    public record BmiBand(
        String label,
        double highRiskRate,
        int count
    ) {
    }

    public record ScatterPoint(
        String id,
        double sleepHours,
        int stressScore,
        double bmi,
        int steps,
        int waterMl,
        String riskLabel
    ) {
    }

    public record ConfusionCell(
        String label,
        int value
    ) {
    }

    public record FeatureImpact(
        String key,
        String label,
        double weight,
        double signedWeight,
        String direction,
        String interpretation
    ) {
    }

    public record KnnSweepPoint(
        int k,
        double accuracy,
        double f1
    ) {
    }

    public record BenchmarkSummary(
        String name,
        String source,
        String citation,
        String rawPath,
        String cleanedPath,
        int sampleCount,
        int usableSampleCount,
        int featureCount,
        double positiveRate,
        List<String> preprocessing,
        MissingValueCard missingSummary,
        List<ModelMetric> models,
        String selectedModel,
        List<String> notes,
        List<String> featureLabels
    ) {
    }

    public record AssistantRequest(String message) {
    }

    public record AssistantMessage(
        String speaker,
        String tag,
        String title,
        String content,
        String time
    ) {
    }

    public record AssistantResponse(
        List<AssistantMessage> conversation,
        List<String> quickPrompts,
        AssistantRuntime runtime
    ) {
    }

    public record AssistantRuntime(
        String provider,
        String model,
        boolean liveMode,
        String status
    ) {
    }

    public record TrendAdviceResponse(
        String advice,
        AssistantRuntime runtime,
        String generatedAt
    ) {
    }

    public record AuthRequest(
        String username,
        String password
    ) {
    }

    public record RegisterRequest(
        String username,
        String password,
        String name
    ) {
    }

    public record AuthUser(
        Long id,
        String username,
        String name,
        String handle,
        String goal,
        boolean admin
    ) {
    }

    public record AuthResponse(
        String token,
        AuthUser user
    ) {
    }

    public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
    ) {
    }

    public record RecoverPasswordRequest(
        String username,
        String name,
        String newPassword
    ) {
    }

    public record AdminUserRow(
        Long id,
        String username,
        String name,
        String handle,
        String goal,
        int healthScore,
        String riskLevel,
        String createdAt,
        String latestRecordDate,
        int mealCount,
        int workoutCount,
        int trendCount,
        boolean admin
    ) {
    }

    public record AdminSummary(
        int totalUsers,
        int adminUsers,
        int activeTodayUsers
    ) {
    }

    public record AdminUsersResponse(
        AdminSummary summary,
        List<AdminUserRow> users
    ) {
    }

    public record AdminResetPasswordRequest(
        String newPassword
    ) {
    }
}
