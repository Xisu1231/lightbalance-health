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
