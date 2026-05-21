package com.lightbalance.health.web;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.service.AuthService;
import com.lightbalance.health.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping
    public AppDtos.DashboardResponse getDashboard(HttpServletRequest request) {
        return dashboardService.getDashboard(authService.requireCurrentUser(request));
    }

    @PostMapping("/meals/{mealId}/consume")
    public AppDtos.DashboardResponse consumeMeal(HttpServletRequest request, @PathVariable Long mealId) {
        return dashboardService.consumeMeal(authService.requireCurrentUser(request), mealId);
    }

    @DeleteMapping("/meals/{mealId}")
    public AppDtos.DashboardResponse deleteMeal(HttpServletRequest request, @PathVariable Long mealId) {
        return dashboardService.deleteMeal(authService.requireCurrentUser(request), mealId);
    }

    @PostMapping("/water")
    public AppDtos.DashboardResponse addWater(HttpServletRequest servletRequest, @Valid @RequestBody AppDtos.WaterRequest request) {
        return dashboardService.addWater(authService.requireCurrentUser(servletRequest), request.amount());
    }

    @PostMapping("/profile")
    public AppDtos.DashboardResponse updateProfile(
        HttpServletRequest servletRequest,
        @Valid @RequestBody AppDtos.ProfileUpdateRequest request
    ) {
        return dashboardService.updateProfile(authService.requireCurrentUser(servletRequest), request);
    }

    @PostMapping("/checkin")
    public AppDtos.DashboardResponse updateCheckin(
        HttpServletRequest servletRequest,
        @Valid @RequestBody AppDtos.DailyCheckinRequest request
    ) {
        return dashboardService.updateCheckin(authService.requireCurrentUser(servletRequest), request);
    }

    @PostMapping("/meals")
    public AppDtos.DashboardResponse createMeal(
        HttpServletRequest servletRequest,
        @Valid @RequestBody AppDtos.MealCreateRequest request
    ) {
        return dashboardService.createMeal(authService.requireCurrentUser(servletRequest), request);
    }

    @PostMapping("/workouts/{workoutId}/toggle")
    public AppDtos.DashboardResponse toggleWorkout(HttpServletRequest request, @PathVariable Long workoutId) {
        return dashboardService.toggleWorkout(authService.requireCurrentUser(request), workoutId);
    }

    @DeleteMapping("/workouts/{workoutId}")
    public AppDtos.DashboardResponse deleteWorkout(HttpServletRequest request, @PathVariable Long workoutId) {
        return dashboardService.deleteWorkout(authService.requireCurrentUser(request), workoutId);
    }

    @PostMapping("/workouts")
    public AppDtos.DashboardResponse createWorkout(
        HttpServletRequest servletRequest,
        @Valid @RequestBody AppDtos.WorkoutCreateRequest request
    ) {
        return dashboardService.createWorkout(authService.requireCurrentUser(servletRequest), request);
    }

    @PostMapping("/trends")
    public AppDtos.DashboardResponse createTrend(
        HttpServletRequest servletRequest,
        @Valid @RequestBody AppDtos.TrendCreateRequest request
    ) {
        return dashboardService.createTrend(authService.requireCurrentUser(servletRequest), request);
    }
}
