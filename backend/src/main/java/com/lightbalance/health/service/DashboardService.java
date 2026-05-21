package com.lightbalance.health.service;

import com.lightbalance.health.domain.DailySummary;
import com.lightbalance.health.domain.MealEntry;
import com.lightbalance.health.domain.TrendRecord;
import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.domain.WorkoutPlan;
import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.repo.DailySummaryRepository;
import com.lightbalance.health.repo.MealEntryRepository;
import com.lightbalance.health.repo.TrendRecordRepository;
import com.lightbalance.health.repo.UserProfileRepository;
import com.lightbalance.health.repo.WorkoutPlanRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {

    private final UserProfileRepository userProfileRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final TrendRecordRepository trendRecordRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ResourceDataLoader resourceDataLoader;

    public DashboardService(
        UserProfileRepository userProfileRepository,
        DailySummaryRepository dailySummaryRepository,
        TrendRecordRepository trendRecordRepository,
        MealEntryRepository mealEntryRepository,
        WorkoutPlanRepository workoutPlanRepository,
        ResourceDataLoader resourceDataLoader
    ) {
        this.userProfileRepository = userProfileRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.trendRecordRepository = trendRecordRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.resourceDataLoader = resourceDataLoader;
    }

    @Transactional(readOnly = true)
    public AppDtos.DashboardResponse getDashboard(UserProfile user) {
        UserProfile profile = getProfile(user);
        DailySummary summary = getLatestSummary(user);
        List<TrendRecord> trends = trendRecordRepository.findAllByOwnerOrderByRecordDateAsc(user);
        List<MealEntry> meals = mealEntryRepository.findAllByOwnerOrderByRecommendedTimeAsc(user);
        List<WorkoutPlan> workouts = workoutPlanRepository.findAllByOwner(user)
            .stream()
            .sorted(Comparator.comparing(WorkoutPlan::isCompleted))
            .toList();

        return new AppDtos.DashboardResponse(
            new AppDtos.ProfileCard(
                profile.getName(),
                profile.getHandleName(),
                profile.getGoal(),
                profile.getWeight(),
                profile.getTargetWeight(),
                profile.getBmi(),
                profile.getBodyFat(),
                profile.getVisceralFat(),
                profile.getMuscleRate(),
                profile.getBasalMetabolism(),
                profile.getHealthScore(),
                profile.getRiskLevel()
            ),
            new AppDtos.SummaryCard(
                summary.getRecordDate().toString(),
                summary.getCalories(),
                summary.getCalorieTarget(),
                summary.getWater(),
                summary.getWaterTarget(),
                summary.getSleepScore(),
                summary.getSleepHours(),
                summary.getSteps(),
                summary.getStepTarget(),
                summary.getWorkoutMinutes(),
                summary.getWorkoutTarget(),
                summary.getMoodScore(),
                summary.getStressScore()
            ),
            List.of(
                macro("Protein", summary.getProtein(), summary.getProteinTarget(), "g"),
                macro("Carbs", summary.getCarbs(), summary.getCarbsTarget(), "g"),
                macro("Fat", summary.getFat(), summary.getFatTarget(), "g")
            ),
            meals.stream()
                .map(meal -> new AppDtos.MealCard(
                    meal.getId(),
                    meal.getName(),
                    meal.getMealType(),
                    meal.getPortion(),
                    meal.getCalories(),
                    meal.getProtein(),
                    meal.getCarbs(),
                    meal.getFat(),
                    meal.isEaten(),
                    meal.getRecommendedTime().toString()
                ))
                .toList(),
            workouts.stream()
                .map(workout -> new AppDtos.WorkoutCard(
                    workout.getId(),
                    workout.getTitle(),
                    workout.getCategory(),
                    workout.getDuration(),
                    workout.getCaloriesBurned(),
                    workout.getIntensity(),
                    workout.isCompleted()
                ))
                .toList(),
            buildInsights(summary, trends),
            trends.stream()
                .map(item -> new AppDtos.TrendPoint(
                    item.getRecordDate().toString().substring(5),
                    item.getWeight(),
                    item.getSleepHours(),
                    item.getSteps(),
                    item.getCalories(),
                    item.getStressScore()
                ))
                .toList(),
            resourceDataLoader.getAppSeed().coachPrompts(),
            buildSignal(summary, trends, profile)
        );
    }

    @Transactional
    public AppDtos.DashboardResponse consumeMeal(UserProfile user, Long mealId) {
        MealEntry meal = mealEntryRepository.findByIdAndOwner(mealId, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal not found"));
        if (!meal.isEaten()) {
            meal.setEaten(true);
            mealEntryRepository.save(meal);

            DailySummary summary = getLatestSummary(user);
            applyMeal(summary, meal);
            dailySummaryRepository.save(summary);
            refreshProfileSignals(user, summary);
        }
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse deleteMeal(UserProfile user, Long mealId) {
        MealEntry meal = mealEntryRepository.findByIdAndOwner(mealId, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal not found"));
        if (meal.isEaten()) {
            DailySummary summary = getLatestSummary(user);
            removeMeal(summary, meal);
            dailySummaryRepository.save(summary);
            refreshProfileSignals(user, summary);
        }
        mealEntryRepository.delete(meal);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse addWater(UserProfile user, int amount) {
        DailySummary summary = getLatestSummary(user);
        summary.setWater(Math.max(0, summary.getWater() + Math.max(0, amount)));
        dailySummaryRepository.save(summary);
        refreshProfileSignals(user, summary);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse toggleWorkout(UserProfile user, Long workoutId) {
        WorkoutPlan workout = workoutPlanRepository.findByIdAndOwner(workoutId, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found"));
        DailySummary summary = getLatestSummary(user);
        if (workout.isCompleted()) {
            workout.setCompleted(false);
            summary.setWorkoutMinutes(Math.max(0, summary.getWorkoutMinutes() - workout.getDuration()));
        } else {
            workout.setCompleted(true);
            summary.setWorkoutMinutes(summary.getWorkoutMinutes() + workout.getDuration());
        }
        workoutPlanRepository.save(workout);
        dailySummaryRepository.save(summary);
        refreshProfileSignals(user, summary);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse deleteWorkout(UserProfile user, Long workoutId) {
        WorkoutPlan workout = workoutPlanRepository.findByIdAndOwner(workoutId, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found"));
        if (workout.isCompleted()) {
            DailySummary summary = getLatestSummary(user);
            summary.setWorkoutMinutes(Math.max(0, summary.getWorkoutMinutes() - workout.getDuration()));
            dailySummaryRepository.save(summary);
            refreshProfileSignals(user, summary);
        }
        workoutPlanRepository.delete(workout);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse updateProfile(UserProfile user, AppDtos.ProfileUpdateRequest request) {
        UserProfile profile = getProfile(user);
        DailySummary summary = getLatestSummary(user);

        profile.setName(textOrDefault(request.name(), profile.getName()));
        profile.setHandleName(textOrDefault(request.handle(), profile.getHandleName()));
        profile.setGoal(textOrDefault(request.goal(), profile.getGoal()));
        profile.setWeight(positiveOrDefault(request.weight(), profile.getWeight()));
        profile.setTargetWeight(positiveOrDefault(request.targetWeight(), profile.getTargetWeight()));
        profile.setBodyFat(positiveOrDefault(request.bodyFat(), profile.getBodyFat()));
        profile.setVisceralFat(positiveOrDefault(request.visceralFat(), profile.getVisceralFat()));
        profile.setMuscleRate(positiveOrDefault(request.muscleRate(), profile.getMuscleRate()));

        recalculateProfile(profile, summary);
        userProfileRepository.save(profile);
        syncTrend(profile, summary);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse updateCheckin(UserProfile user, AppDtos.DailyCheckinRequest request) {
        DailySummary summary = getLatestSummary(user);

        summary.setCalorieTarget(positiveOrDefault(request.calorieTarget(), summary.getCalorieTarget()));
        summary.setWaterTarget(positiveOrDefault(request.waterTarget(), summary.getWaterTarget()));
        summary.setStepTarget(positiveOrDefault(request.stepTarget(), summary.getStepTarget()));
        summary.setWorkoutTarget(positiveOrDefault(request.workoutTarget(), summary.getWorkoutTarget()));
        summary.setSleepHours(positiveOrDefault(request.sleepHours(), summary.getSleepHours()));
        summary.setSteps(nonNegativeOrDefault(request.steps(), summary.getSteps()));
        summary.setMoodScore(clamp(nonNegativeOrDefault(request.moodScore(), summary.getMoodScore()), 0, 100));
        summary.setStressScore(clamp(nonNegativeOrDefault(request.stressScore(), summary.getStressScore()), 0, 100));
        summary.setSleepScore(calculateSleepScore(summary.getSleepHours(), summary.getStressScore()));

        dailySummaryRepository.save(summary);
        refreshProfileSignals(user, summary);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse createMeal(UserProfile user, AppDtos.MealCreateRequest request) {
        MealEntry meal = new MealEntry();
        meal.setOwner(user);
        meal.setName(textOrDefault(request.name(), "Custom meal"));
        meal.setMealType(textOrDefault(request.mealType(), "Snack"));
        meal.setPortion(textOrDefault(request.portion(), "1 serving"));
        meal.setCalories(Math.max(0, request.calories()));
        meal.setProtein(Math.max(0, request.protein()));
        meal.setCarbs(Math.max(0, request.carbs()));
        meal.setFat(Math.max(0, request.fat()));
        meal.setRecommendedTime(parseTime(request.recommendedTime()));
        meal.setEaten(request.eaten());
        mealEntryRepository.save(meal);

        if (request.eaten()) {
            DailySummary summary = getLatestSummary(user);
            applyMeal(summary, meal);
            dailySummaryRepository.save(summary);
            refreshProfileSignals(user, summary);
        }

        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse createWorkout(UserProfile user, AppDtos.WorkoutCreateRequest request) {
        WorkoutPlan workout = new WorkoutPlan();
        workout.setOwner(user);
        workout.setTitle(textOrDefault(request.title(), "Custom workout"));
        workout.setCategory(textOrDefault(request.category(), "Functional training"));
        workout.setDuration(Math.max(5, request.duration()));
        workout.setCaloriesBurned(Math.max(0, request.caloriesBurned()));
        workout.setIntensity(textOrDefault(request.intensity(), "Moderate"));
        workout.setCompleted(false);
        workoutPlanRepository.save(workout);
        return getDashboard(user);
    }

    @Transactional
    public AppDtos.DashboardResponse createTrend(UserProfile user, AppDtos.TrendCreateRequest request) {
        LocalDate recordDate = parseDate(user, request.recordDate());
        TrendRecord trend = trendRecordRepository.findByOwnerAndRecordDate(user, recordDate).orElseGet(TrendRecord::new);
        trend.setOwner(user);
        trend.setRecordDate(recordDate);
        trend.setWeight(positiveOrDefault(request.weight(), getProfile(user).getWeight()));
        trend.setSleepHours(positiveOrDefault(request.sleepHours(), getLatestSummary(user).getSleepHours()));
        trend.setSteps(Math.max(0, request.steps()));
        trend.setCalories(Math.max(0, request.calories()));
        trend.setStressScore(clamp(Math.max(0, request.stressScore()), 0, 100));
        trendRecordRepository.save(trend);

        UserProfile profile = getProfile(user);
        DailySummary summary = getLatestSummary(user);
        profile.setWeight(trend.getWeight());
        if (summary.getRecordDate().equals(recordDate)) {
            summary.setSleepHours(trend.getSleepHours());
            summary.setSteps(trend.getSteps());
            summary.setCalories(trend.getCalories());
            summary.setStressScore(trend.getStressScore());
            summary.setSleepScore(calculateSleepScore(summary.getSleepHours(), summary.getStressScore()));
            dailySummaryRepository.save(summary);
        }
        recalculateProfile(profile, summary);
        userProfileRepository.save(profile);
        return getDashboard(user);
    }

    private DailySummary getLatestSummary(UserProfile user) {
        return dailySummaryRepository.findFirstByOwnerOrderByRecordDateDesc(user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Daily summary not found"));
    }

    private UserProfile getProfile(UserProfile user) {
        return userProfileRepository.findById(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AppDtos.MacroProgress macro(String label, int value, int target, String unit) {
        String tone = value >= target ? "good" : value >= target * 0.75 ? "focus" : "calm";
        return new AppDtos.MacroProgress(label, value, target, unit, tone);
    }

    private void applyMeal(DailySummary summary, MealEntry meal) {
        summary.setCalories(summary.getCalories() + meal.getCalories());
        summary.setProtein(summary.getProtein() + meal.getProtein());
        summary.setCarbs(summary.getCarbs() + meal.getCarbs());
        summary.setFat(summary.getFat() + meal.getFat());
    }

    private void removeMeal(DailySummary summary, MealEntry meal) {
        summary.setCalories(Math.max(0, summary.getCalories() - meal.getCalories()));
        summary.setProtein(Math.max(0, summary.getProtein() - meal.getProtein()));
        summary.setCarbs(Math.max(0, summary.getCarbs() - meal.getCarbs()));
        summary.setFat(Math.max(0, summary.getFat() - meal.getFat()));
    }

    private void refreshProfileSignals(UserProfile user, DailySummary summary) {
        UserProfile profile = getProfile(user);
        recalculateProfile(profile, summary);
        userProfileRepository.save(profile);
        syncTrend(profile, summary);
    }

    private void syncTrend(UserProfile user, DailySummary summary) {
        TrendRecord trend = trendRecordRepository.findByOwnerAndRecordDate(user, summary.getRecordDate()).orElseGet(TrendRecord::new);
        trend.setOwner(user);
        trend.setRecordDate(summary.getRecordDate());
        trend.setWeight(user.getWeight());
        trend.setSleepHours(summary.getSleepHours());
        trend.setSteps(summary.getSteps());
        trend.setCalories(summary.getCalories());
        trend.setStressScore(summary.getStressScore());
        trendRecordRepository.save(trend);
    }

    private void recalculateProfile(UserProfile profile, DailySummary summary) {
        double heightMeters = inferHeightMeters(profile);
        double bmi = profile.getWeight() / (heightMeters * heightMeters);
        profile.setBmi(round1(bmi));

        double leanMass = profile.getWeight() * (1 - profile.getBodyFat() / 100.0);
        profile.setBasalMetabolism(Math.round(370 + (21.6 * leanMass)));
        profile.setRiskLevel(computeRiskLevel(profile, summary));
        profile.setHealthScore(computeHealthScore(profile, summary));
    }

    private double inferHeightMeters(UserProfile profile) {
        if (profile.getBmi() > 0 && profile.getWeight() > 0) {
            return Math.sqrt(profile.getWeight() / profile.getBmi());
        }
        return 1.7;
    }

    private int computeHealthScore(UserProfile profile, DailySummary summary) {
        double bmiScore = Math.max(45, 100 - Math.abs(profile.getBmi() - 22) * 12);
        double waterScore = ratioScore(summary.getWater(), summary.getWaterTarget());
        double stepScore = ratioScore(summary.getSteps(), summary.getStepTarget());
        double workoutScore = ratioScore(summary.getWorkoutMinutes(), summary.getWorkoutTarget());
        double stressScore = 100 - clamp(summary.getStressScore(), 0, 100);
        double score = (bmiScore * 0.22)
            + (summary.getSleepScore() * 0.2)
            + (waterScore * 0.14)
            + (stepScore * 0.14)
            + (workoutScore * 0.12)
            + (summary.getMoodScore() * 0.1)
            + (stressScore * 0.08);
        return clamp((int) Math.round(score), 45, 98);
    }

    private String computeRiskLevel(UserProfile profile, DailySummary summary) {
        if (profile.getBmi() >= 28 || summary.getStressScore() >= 75 || summary.getSleepHours() < 5.5) {
            return "High attention";
        }
        if (profile.getBmi() >= 24 || summary.getStressScore() >= 60 || summary.getSleepHours() < 6.5) {
            return "Moderate risk";
        }
        return "Stable";
    }

    private int calculateSleepScore(double sleepHours, int stressScore) {
        double durationPenalty = Math.abs(sleepHours - 8.0) * 12;
        double stressPenalty = stressScore * 0.28;
        return clamp((int) Math.round(96 - durationPenalty - stressPenalty), 45, 98);
    }

    private double ratioScore(int value, int target) {
        if (target <= 0) {
            return 100;
        }
        return Math.min(100, (value * 100.0) / target);
    }

    private String textOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private int positiveOrDefault(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    private double positiveOrDefault(double value, double fallback) {
        return value > 0 ? value : fallback;
    }

    private int nonNegativeOrDefault(int value, int fallback) {
        return value >= 0 ? value : fallback;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalTime.of(12, 0);
        }
        return LocalTime.parse(value.trim());
    }

    private LocalDate parseDate(UserProfile user, String value) {
        if (value == null || value.isBlank()) {
            return getLatestSummary(user).getRecordDate();
        }
        return LocalDate.parse(value.trim());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private List<AppDtos.InsightCard> buildInsights(DailySummary summary, List<TrendRecord> trends) {
        List<AppDtos.InsightCard> cards = new ArrayList<>();
        int proteinGap = summary.getProteinTarget() - summary.getProtein();
        int waterGap = summary.getWaterTarget() - summary.getWater();
        int stepGap = summary.getStepTarget() - summary.getSteps();
        double averageSleep = trends.stream()
            .skip(Math.max(0, trends.size() - 7))
            .mapToDouble(TrendRecord::getSleepHours)
            .average()
            .orElse(summary.getSleepHours());

        cards.add(new AppDtos.InsightCard(
            proteinGap <= 0 ? "Protein target met" : "Protein is close to target",
            proteinGap <= 0
                ? "Recovery basics are stable today. Tonight can focus on hydration and light stretching."
                : String.format(Locale.ROOT, "%d g short of target. A high-protein evening snack would fit well.", proteinGap),
            proteinGap <= 0 ? "good" : "focus"
        ));
        cards.add(new AppDtos.InsightCard(
            waterGap <= 0 ? "Water target completed" : "More water still needed",
            waterGap <= 0
                ? "Hydration rhythm has been steady, no need to force extra water tonight."
                : String.format(Locale.ROOT, "%d ml remains. Splitting it into 2 or 3 rounds will be easier.", waterGap),
            waterGap <= 0 ? "good" : "calm"
        ));
        cards.add(new AppDtos.InsightCard(
            stepGap <= 0 ? "Activity target met" : "A little more activity would help",
            stepGap <= 0
                ? "You've already crossed today's step target, so recovery-focused movement is enough."
                : String.format(Locale.ROOT, "%d steps remain. A 10 to 15 minute walk after dinner is the easiest finish.", stepGap),
            stepGap <= 0 ? "good" : "focus"
        ));
        cards.add(new AppDtos.InsightCard(
            "Sleep rhythm is stable",
            String.format(Locale.ROOT, "Average sleep over the last 7 days is %.2f hours. Keep stress in the comfortable zone.", averageSleep),
            "calm"
        ));
        return cards;
    }

    private AppDtos.RecoverySignal buildSignal(DailySummary summary, List<TrendRecord> trends, UserProfile profile) {
        double weekWeightDelta = trends.size() >= 7
            ? trends.get(trends.size() - 1).getWeight() - trends.get(trends.size() - 7).getWeight()
            : 0;
        String badge = summary.getStressScore() <= 45 ? "Synced" : "Reduce stress";
        String subline = weekWeightDelta <= 0
            ? "Weight trend is moving down steadily. Today is a good day to keep a light and stable rhythm."
            : "Weight has rebounded slightly, but recovery markers are still in range. Prioritize sleep and hydration.";
        String headline = profile.getName() + ", start from today's state and move toward a lighter, steadier self.";
        return new AppDtos.RecoverySignal(headline, subline, badge);
    }
}
