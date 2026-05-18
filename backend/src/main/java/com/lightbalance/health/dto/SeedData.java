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
        DatasetSummarySeed dataset,
        List<ModelMetricSeed> models,
        String selectedModel,
        List<BmiBandSeed> bmiBands,
        List<String> narrative
    ) {
    }

    public record DatasetSummarySeed(
        int sample_count,
        int high_risk_count,
        double avg_sleep_hours,
        int avg_steps,
        double avg_bmi,
        double avg_stress_score
    ) {
    }

    public record ModelMetricSeed(
        String model,
        double accuracy,
        double precision,
        double recall,
        double f1,
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
