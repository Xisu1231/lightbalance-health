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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public AppDtos.DashboardResponse getDashboard() {
        UserProfile profile = getProfile();
        DailySummary summary = getLatestSummary();
        List<TrendRecord> trends = trendRecordRepository.findAllByOrderByRecordDateAsc();
        List<MealEntry> meals = mealEntryRepository.findAllByOrderByRecommendedTimeAsc();
        List<WorkoutPlan> workouts = workoutPlanRepository.findAll()
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
                macro("蛋白质", summary.getProtein(), summary.getProteinTarget(), "g"),
                macro("碳水", summary.getCarbs(), summary.getCarbsTarget(), "g"),
                macro("脂肪", summary.getFat(), summary.getFatTarget(), "g")
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
    public AppDtos.DashboardResponse consumeMeal(Long mealId) {
        MealEntry meal = mealEntryRepository.findById(mealId).orElseThrow();
        if (!meal.isEaten()) {
            meal.setEaten(true);
            mealEntryRepository.save(meal);

            DailySummary summary = getLatestSummary();
            applyMeal(summary, meal);
            dailySummaryRepository.save(summary);
            refreshProfileSignals(summary);
        }
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse deleteMeal(Long mealId) {
        MealEntry meal = mealEntryRepository.findById(mealId).orElseThrow();
        if (meal.isEaten()) {
            DailySummary summary = getLatestSummary();
            removeMeal(summary, meal);
            dailySummaryRepository.save(summary);
            refreshProfileSignals(summary);
        }
        mealEntryRepository.delete(meal);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse addWater(int amount) {
        DailySummary summary = getLatestSummary();
        summary.setWater(Math.max(0, summary.getWater() + Math.max(0, amount)));
        dailySummaryRepository.save(summary);
        refreshProfileSignals(summary);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse toggleWorkout(Long workoutId) {
        WorkoutPlan workout = workoutPlanRepository.findById(workoutId).orElseThrow();
        DailySummary summary = getLatestSummary();
        if (workout.isCompleted()) {
            workout.setCompleted(false);
            summary.setWorkoutMinutes(Math.max(0, summary.getWorkoutMinutes() - workout.getDuration()));
        } else {
            workout.setCompleted(true);
            summary.setWorkoutMinutes(summary.getWorkoutMinutes() + workout.getDuration());
        }
        workoutPlanRepository.save(workout);
        dailySummaryRepository.save(summary);
        refreshProfileSignals(summary);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse deleteWorkout(Long workoutId) {
        WorkoutPlan workout = workoutPlanRepository.findById(workoutId).orElseThrow();
        if (workout.isCompleted()) {
            DailySummary summary = getLatestSummary();
            summary.setWorkoutMinutes(Math.max(0, summary.getWorkoutMinutes() - workout.getDuration()));
            dailySummaryRepository.save(summary);
            refreshProfileSignals(summary);
        }
        workoutPlanRepository.delete(workout);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse updateProfile(AppDtos.ProfileUpdateRequest request) {
        UserProfile profile = getProfile();
        DailySummary summary = getLatestSummary();

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
        syncTrend(summary, profile);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse updateCheckin(AppDtos.DailyCheckinRequest request) {
        DailySummary summary = getLatestSummary();

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
        refreshProfileSignals(summary);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse createMeal(AppDtos.MealCreateRequest request) {
        MealEntry meal = new MealEntry();
        meal.setName(textOrDefault(request.name(), "自定义餐食"));
        meal.setMealType(textOrDefault(request.mealType(), "加餐"));
        meal.setPortion(textOrDefault(request.portion(), "1 份"));
        meal.setCalories(Math.max(0, request.calories()));
        meal.setProtein(Math.max(0, request.protein()));
        meal.setCarbs(Math.max(0, request.carbs()));
        meal.setFat(Math.max(0, request.fat()));
        meal.setRecommendedTime(parseTime(request.recommendedTime()));
        meal.setEaten(request.eaten());
        mealEntryRepository.save(meal);

        if (request.eaten()) {
            DailySummary summary = getLatestSummary();
            applyMeal(summary, meal);
            dailySummaryRepository.save(summary);
            refreshProfileSignals(summary);
        }

        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse createWorkout(AppDtos.WorkoutCreateRequest request) {
        WorkoutPlan workout = new WorkoutPlan();
        workout.setTitle(textOrDefault(request.title(), "自定义训练"));
        workout.setCategory(textOrDefault(request.category(), "功能训练"));
        workout.setDuration(Math.max(5, request.duration()));
        workout.setCaloriesBurned(Math.max(0, request.caloriesBurned()));
        workout.setIntensity(textOrDefault(request.intensity(), "中等"));
        workout.setCompleted(false);
        workoutPlanRepository.save(workout);
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse createTrend(AppDtos.TrendCreateRequest request) {
        LocalDate recordDate = parseDate(request.recordDate());
        TrendRecord trend = trendRecordRepository.findByRecordDate(recordDate).orElseGet(TrendRecord::new);
        trend.setRecordDate(recordDate);
        trend.setWeight(positiveOrDefault(request.weight(), getProfile().getWeight()));
        trend.setSleepHours(positiveOrDefault(request.sleepHours(), getLatestSummary().getSleepHours()));
        trend.setSteps(Math.max(0, request.steps()));
        trend.setCalories(Math.max(0, request.calories()));
        trend.setStressScore(clamp(Math.max(0, request.stressScore()), 0, 100));
        trendRecordRepository.save(trend);

        UserProfile profile = getProfile();
        DailySummary summary = getLatestSummary();
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
        return getDashboard();
    }

    private DailySummary getLatestSummary() {
        return dailySummaryRepository.findFirstByOrderByRecordDateDesc().orElseThrow();
    }

    private UserProfile getProfile() {
        return userProfileRepository.findAll().stream().findFirst().orElseThrow();
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

    private void refreshProfileSignals(DailySummary summary) {
        UserProfile profile = getProfile();
        recalculateProfile(profile, summary);
        userProfileRepository.save(profile);
        syncTrend(summary, profile);
    }

    private void syncTrend(DailySummary summary, UserProfile profile) {
        TrendRecord trend = trendRecordRepository.findByRecordDate(summary.getRecordDate()).orElseGet(TrendRecord::new);
        trend.setRecordDate(summary.getRecordDate());
        trend.setWeight(profile.getWeight());
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
            return "高关注";
        }
        if (profile.getBmi() >= 24 || summary.getStressScore() >= 60 || summary.getSleepHours() < 6.5) {
            return "中等风险";
        }
        return "稳定向好";
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

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return getLatestSummary().getRecordDate();
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
            proteinGap <= 0 ? "蛋白质已达标" : "蛋白质接近达标",
            proteinGap <= 0
                ? "今天的恢复与饱腹感基础已经稳住，可以把晚间重点放在补水和放松。"
                : String.format(Locale.ROOT, "距离目标还差 %d g，晚间加一杯无糖豆浆会更合适。", proteinGap),
            proteinGap <= 0 ? "good" : "focus"
        ));
        cards.add(new AppDtos.InsightCard(
            waterGap <= 0 ? "饮水目标完成" : "饮水还需补充",
            waterGap <= 0
                ? "全天补水节奏完整，今晚不需要再集中大量饮水。"
                : String.format(Locale.ROOT, "今天还差 %d ml，建议拆成 2 到 3 次喝完。", waterGap),
            waterGap <= 0 ? "good" : "calm"
        ));
        cards.add(new AppDtos.InsightCard(
            stepGap <= 0 ? "活动量达到目标" : "晚间还可补一点活动量",
            stepGap <= 0
                ? "步数已经越过今日目标，恢复性拉伸就足够。"
                : String.format(Locale.ROOT, "还差 %d 步，饭后快走 10 到 15 分钟最轻松。", stepGap),
            stepGap <= 0 ? "good" : "focus"
        ));
        cards.add(new AppDtos.InsightCard(
            "睡眠节律维持稳定",
            String.format(Locale.ROOT, "最近 7 天平均睡眠 %.2f 小时，恢复分数处于较好区间。", averageSleep),
            "calm"
        ));
        return cards;
    }

    private AppDtos.RecoverySignal buildSignal(DailySummary summary, List<TrendRecord> trends, UserProfile profile) {
        double weekWeightDelta = trends.size() >= 7
            ? trends.get(trends.size() - 1).getWeight() - trends.get(trends.size() - 7).getWeight()
            : 0;
        String badge = summary.getStressScore() <= 45 ? "已同步" : "需减压";
        String subline = weekWeightDelta <= 0
            ? "体重趋势平稳向下，今天适合继续走轻盈、稳定的节奏。"
            : "体重略有回弹，但恢复分数仍在线，优先守住睡眠和饮水。";
        String headline = profile.getName() + "，从今天的状态出发，走向更轻盈、更稳定的自己。";
        return new AppDtos.RecoverySignal(headline, subline, badge);
    }
}
