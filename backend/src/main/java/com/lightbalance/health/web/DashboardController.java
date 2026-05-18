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

    @PostMapping("/workouts/{workoutId}/toggle")
    public AppDtos.DashboardResponse toggleWorkout(@PathVariable Long workoutId) {
        return dashboardService.toggleWorkout(workoutId);
    }
}
