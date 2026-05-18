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

    public record AnalyticsResponse(
        DatasetSummary datasetSummary,
        List<ModelMetric> models,
        String selectedModel,
        List<BmiBand> bmiBands,
        List<ScatterPoint> scatterPoints,
        List<ConfusionCell> confusionMatrix,
        List<String> narrative
    ) {
    }

    public record DatasetSummary(
        int sampleCount,
        int highRiskCount,
        double avgSleepHours,
        int avgSteps,
        double avgBmi,
        double avgStressScore
    ) {
    }

    public record ModelMetric(
        String model,
        double accuracy,
        double precision,
        double recall,
        double f1
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
}
