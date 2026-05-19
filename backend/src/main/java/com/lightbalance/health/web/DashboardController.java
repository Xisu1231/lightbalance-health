package com.lightbalance.health.web;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public AppDtos.DashboardResponse getDashboard() {
        return dashboardService.getDashboard();
    }

    @PostMapping("/meals/{mealId}/consume")
    public AppDtos.DashboardResponse consumeMeal(@PathVariable Long mealId) {
        return dashboardService.consumeMeal(mealId);
    }

    @PostMapping("/water")
    public AppDtos.DashboardResponse addWater(@Valid @RequestBody AppDtos.WaterRequest request) {
        return dashboardService.addWater(request.amount());
    }

    @PostMapping("/profile")
    public AppDtos.DashboardResponse updateProfile(@Valid @RequestBody AppDtos.ProfileUpdateRequest request) {
        return dashboardService.updateProfile(request);
    }

    @PostMapping("/checkin")
    public AppDtos.DashboardResponse updateCheckin(@Valid @RequestBody AppDtos.DailyCheckinRequest request) {
        return dashboardService.updateCheckin(request);
    }

    @PostMapping("/meals")
    public AppDtos.DashboardResponse createMeal(@Valid @RequestBody AppDtos.MealCreateRequest request) {
        return dashboardService.createMeal(request);
    }

    @PostMapping("/workouts/{workoutId}/toggle")
    public AppDtos.DashboardResponse toggleWorkout(@PathVariable Long workoutId) {
        return dashboardService.toggleWorkout(workoutId);
    }

    @PostMapping("/workouts")
    public AppDtos.DashboardResponse createWorkout(@Valid @RequestBody AppDtos.WorkoutCreateRequest request) {
        return dashboardService.createWorkout(request);
    }

    @PostMapping("/trends")
    public AppDtos.DashboardResponse createTrend(@Valid @RequestBody AppDtos.TrendCreateRequest request) {
        return dashboardService.createTrend(request);
    }
}
