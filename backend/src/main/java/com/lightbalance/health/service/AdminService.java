package com.lightbalance.health.service;

import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.repo.DailySummaryRepository;
import com.lightbalance.health.repo.MealEntryRepository;
import com.lightbalance.health.repo.TrendRecordRepository;
import com.lightbalance.health.repo.UserProfileRepository;
import com.lightbalance.health.repo.WorkoutPlanRepository;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserProfileRepository userProfileRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final TrendRecordRepository trendRecordRepository;
    private final AuthService authService;

    public AdminService(
        UserProfileRepository userProfileRepository,
        DailySummaryRepository dailySummaryRepository,
        MealEntryRepository mealEntryRepository,
        WorkoutPlanRepository workoutPlanRepository,
        TrendRecordRepository trendRecordRepository,
        AuthService authService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.trendRecordRepository = trendRecordRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public AppDtos.AdminUsersResponse getUsers(UserProfile currentUser) {
        authService.requireAdmin(currentUser);

        List<UserProfile> users = userProfileRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(UserProfile::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        List<AppDtos.AdminUserRow> rows = users.stream()
            .map(this::toRow)
            .toList();

        int adminUsers = (int) users.stream().filter(authService::isAdmin).count();
        int activeTodayUsers = (int) users.stream()
            .filter(user -> dailySummaryRepository.findFirstByOwnerOrderByRecordDateDesc(user)
                .map(summary -> summary.getRecordDate().equals(java.time.LocalDate.now(APP_ZONE)))
                .orElse(false))
            .count();

        return new AppDtos.AdminUsersResponse(
            new AppDtos.AdminSummary(users.size(), adminUsers, activeTodayUsers),
            rows
        );
    }

    @Transactional
    public void resetUserPassword(UserProfile currentUser, Long userId, AppDtos.AdminResetPasswordRequest request) {
        authService.requireAdmin(currentUser);
        UserProfile target = userProfileRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "未找到该用户"));
        target.setPasswordHash(authService.encodePassword(request.newPassword()));
        target.setSessionToken(java.util.UUID.randomUUID().toString().replace("-", ""));
        userProfileRepository.save(target);
    }

    private AppDtos.AdminUserRow toRow(UserProfile user) {
        String latestRecordDate = dailySummaryRepository.findFirstByOwnerOrderByRecordDateDesc(user)
            .map(summary -> summary.getRecordDate().toString())
            .orElse("-");
        return new AppDtos.AdminUserRow(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getHandleName(),
            user.getGoal(),
            user.getHealthScore(),
            user.getRiskLevel(),
            user.getCreatedAt() == null ? "-" : user.getCreatedAt().format(TIME_FORMATTER),
            latestRecordDate,
            mealEntryRepository.findAllByOwnerOrderByRecommendedTimeAsc(user).size(),
            workoutPlanRepository.findAllByOwner(user).size(),
            trendRecordRepository.findAllByOwnerOrderByRecordDateAsc(user).size(),
            authService.isAdmin(user)
        );
    }

}
