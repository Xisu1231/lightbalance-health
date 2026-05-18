package com.lightbalance.health.service;

import com.lightbalance.health.domain.DailySummary;
import com.lightbalance.health.domain.MealEntry;
import com.lightbalance.health.domain.TrendRecord;
import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.domain.WorkoutPlan;
import com.lightbalance.health.dto.SeedData;
import com.lightbalance.health.repo.DailySummaryRepository;
import com.lightbalance.health.repo.MealEntryRepository;
import com.lightbalance.health.repo.TrendRecordRepository;
import com.lightbalance.health.repo.UserProfileRepository;
import com.lightbalance.health.repo.WorkoutPlanRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ResourceDataLoader resourceDataLoader;
    private final UserProfileRepository userProfileRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final TrendRecordRepository trendRecordRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public DataInitializer(
        ResourceDataLoader resourceDataLoader,
        UserProfileRepository userProfileRepository,
        DailySummaryRepository dailySummaryRepository,
        TrendRecordRepository trendRecordRepository,
        MealEntryRepository mealEntryRepository,
        WorkoutPlanRepository workoutPlanRepository
    ) {
        this.resourceDataLoader = resourceDataLoader;
        this.userProfileRepository = userProfileRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.trendRecordRepository = trendRecordRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    @Override
    public void run(String... args) {
        if (userProfileRepository.count() > 0) {
            return;
        }

        SeedData.AppSeed seed = resourceDataLoader.getAppSeed();

        UserProfile profile = new UserProfile();
        profile.setName(seed.profile().name());
        profile.setHandleName(seed.profile().handle());
        profile.setGoal(seed.profile().goal());
        profile.setWeight(seed.profile().weight());
        profile.setTargetWeight(seed.profile().targetWeight());
        profile.setBmi(seed.profile().bmi());
        profile.setBodyFat(seed.profile().bodyFat());
        profile.setVisceralFat(seed.profile().visceralFat());
        profile.setMuscleRate(seed.profile().muscleRate());
        profile.setBasalMetabolism(seed.profile().basalMetabolism());
        profile.setHealthScore(seed.profile().healthScore());
        profile.setRiskLevel(seed.profile().riskLevel());
        userProfileRepository.save(profile);

        DailySummary summary = new DailySummary();
        summary.setRecordDate(LocalDate.parse(seed.todaySummary().date()));
        summary.setCalories(seed.todaySummary().calories());
        summary.setCalorieTarget(seed.todaySummary().calorieTarget());
        summary.setWater(seed.todaySummary().water());
        summary.setWaterTarget(seed.todaySummary().waterTarget());
        summary.setProtein(seed.todaySummary().protein());
        summary.setProteinTarget(seed.todaySummary().proteinTarget());
        summary.setCarbs(seed.todaySummary().carbs());
        summary.setCarbsTarget(seed.todaySummary().carbsTarget());
        summary.setFat(seed.todaySummary().fat());
        summary.setFatTarget(seed.todaySummary().fatTarget());
        summary.setSleepHours(seed.todaySummary().sleepHours());
        summary.setSleepScore(seed.todaySummary().sleepScore());
        summary.setSteps(seed.todaySummary().steps());
        summary.setStepTarget(seed.todaySummary().stepTarget());
        summary.setWorkoutMinutes(seed.todaySummary().workoutMinutes());
        summary.setWorkoutTarget(seed.todaySummary().workoutTarget());
        summary.setMoodScore(seed.todaySummary().moodScore());
        summary.setStressScore(seed.todaySummary().stressScore());
        dailySummaryRepository.save(summary);

        for (SeedData.TrendSeed trend : seed.trends()) {
            TrendRecord record = new TrendRecord();
            record.setRecordDate(LocalDate.parse("2026-" + trend.date()));
            record.setWeight(trend.weight());
            record.setSleepHours(trend.sleepHours());
            record.setSteps(trend.steps());
            record.setCalories(trend.calories());
            record.setStressScore(trend.stressScore());
            trendRecordRepository.save(record);
        }

        for (SeedData.MealSeed meal : seed.meals()) {
            MealEntry entry = new MealEntry();
            entry.setName(meal.name());
            entry.setMealType(meal.mealType());
            entry.setPortion(meal.portion());
            entry.setCalories(meal.calories());
            entry.setProtein(meal.protein());
            entry.setCarbs(meal.carbs());
            entry.setFat(meal.fat());
            entry.setEaten(meal.eaten());
            entry.setRecommendedTime(LocalTime.parse(meal.recommendedTime()));
            mealEntryRepository.save(entry);
        }

        for (SeedData.WorkoutSeed workout : seed.workouts()) {
            WorkoutPlan plan = new WorkoutPlan();
            plan.setTitle(workout.title());
            plan.setCategory(workout.category());
            plan.setDuration(workout.duration());
            plan.setCaloriesBurned(workout.caloriesBurned());
            plan.setIntensity(workout.intensity());
            plan.setCompleted(workout.completed());
            workoutPlanRepository.save(plan);
        }
    }
}
