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
import com.lightbalance.health.repo.WorkoutPlanRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountBootstrapService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    private final ResourceDataLoader resourceDataLoader;
    private final DailySummaryRepository dailySummaryRepository;
    private final TrendRecordRepository trendRecordRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    public AccountBootstrapService(
        ResourceDataLoader resourceDataLoader,
        DailySummaryRepository dailySummaryRepository,
        TrendRecordRepository trendRecordRepository,
        MealEntryRepository mealEntryRepository,
        WorkoutPlanRepository workoutPlanRepository
    ) {
        this.resourceDataLoader = resourceDataLoader;
        this.dailySummaryRepository = dailySummaryRepository;
        this.trendRecordRepository = trendRecordRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    @Transactional
    public void seedForUser(UserProfile profile, String displayName) {
        SeedData.AppSeed seed = resourceDataLoader.getAppSeed();
        LocalDate today = LocalDate.now(APP_ZONE);

        profile.setName(displayName);
        profile.setHandleName("@" + profile.getUsername());
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
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now(APP_ZONE));
        }

        DailySummary summary = new DailySummary();
        summary.setOwner(profile);
        summary.setRecordDate(today);
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
            record.setOwner(profile);
            record.setRecordDate(today.minusDays(seed.trends().size() - 1L - seed.trends().indexOf(trend)));
            record.setWeight(trend.weight());
            record.setSleepHours(trend.sleepHours());
            record.setSteps(trend.steps());
            record.setCalories(trend.calories());
            record.setStressScore(trend.stressScore());
            trendRecordRepository.save(record);
        }

        for (SeedData.MealSeed meal : seed.meals()) {
            MealEntry entry = new MealEntry();
            entry.setOwner(profile);
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
            plan.setOwner(profile);
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
