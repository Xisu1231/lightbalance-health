package com.lightbalance.health.service;

import com.lightbalance.health.domain.DailySummary;
import com.lightbalance.health.domain.MealEntry;
import com.lightbalance.health.domain.TrendRecord;
import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.domain.WorkoutPlan;
import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.dto.SeedData;
import com.lightbalance.health.repo.DailySummaryRepository;
import com.lightbalance.health.repo.MealEntryRepository;
import com.lightbalance.health.repo.TrendRecordRepository;
import com.lightbalance.health.repo.UserProfileRepository;
import com.lightbalance.health.repo.WorkoutPlanRepository;
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
        UserProfile profile = userProfileRepository.findAll().stream().findFirst().orElseThrow();
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
            summary.setCalories(summary.getCalories() + meal.getCalories());
            summary.setProtein(summary.getProtein() + meal.getProtein());
            summary.setCarbs(summary.getCarbs() + meal.getCarbs());
            summary.setFat(summary.getFat() + meal.getFat());
            dailySummaryRepository.save(summary);
        }
        return getDashboard();
    }

    @Transactional
    public AppDtos.DashboardResponse addWater(int amount) {
        DailySummary summary = getLatestSummary();
        summary.setWater(summary.getWater() + amount);
        dailySummaryRepository.save(summary);
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
        return getDashboard();
    }

    private DailySummary getLatestSummary() {
        return dailySummaryRepository.findFirstByOrderByRecordDateDesc().orElseThrow();
    }

    private AppDtos.MacroProgress macro(String label, int value, int target, String unit) {
        String tone = value >= target ? "good" : value >= target * 0.75 ? "focus" : "calm";
        return new AppDtos.MacroProgress(label, value, target, unit, tone);
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
