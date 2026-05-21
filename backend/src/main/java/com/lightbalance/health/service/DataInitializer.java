package com.lightbalance.health.service;

import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.repo.DailySummaryRepository;
import com.lightbalance.health.repo.MealEntryRepository;
import com.lightbalance.health.repo.TrendRecordRepository;
import com.lightbalance.health.repo.UserProfileRepository;
import com.lightbalance.health.repo.WorkoutPlanRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String NEW_NAME = "\u601D\u660E\u8001\u5E08";
    private static final String OLD_NAME = "\u5C0F\u660E\u8001\u5E08";

    private final UserProfileRepository userProfileRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final TrendRecordRepository trendRecordRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final AccountBootstrapService accountBootstrapService;

    public DataInitializer(
        UserProfileRepository userProfileRepository,
        DailySummaryRepository dailySummaryRepository,
        TrendRecordRepository trendRecordRepository,
        MealEntryRepository mealEntryRepository,
        WorkoutPlanRepository workoutPlanRepository,
        AccountBootstrapService accountBootstrapService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.dailySummaryRepository = dailySummaryRepository;
        this.trendRecordRepository = trendRecordRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.accountBootstrapService = accountBootstrapService;
    }

    @Override
    public void run(String... args) {
        if (userProfileRepository.count() == 0) {
            createDemoUser();
        } else {
            migrateExistingData();
        }
    }

    private void createDemoUser() {
        UserProfile profile = new UserProfile();
        profile.setUsername("admin");
        profile.setPasswordHash(hashPassword("admin123"));
        profile.setSessionToken(generateToken());
        profile.setCreatedAt(LocalDateTime.now(APP_ZONE));
        profile = userProfileRepository.save(profile);
        accountBootstrapService.seedForUser(profile, NEW_NAME);
        userProfileRepository.save(profile);
    }

    private void migrateExistingData() {
        List<UserProfile> profiles = userProfileRepository.findAll();
        if (profiles.isEmpty()) {
            return;
        }

        UserProfile fallbackOwner = profiles.get(0);
        LocalDate today = LocalDate.now(APP_ZONE);

        for (int index = 0; index < profiles.size(); index++) {
            UserProfile profile = profiles.get(index);
            if (OLD_NAME.equals(profile.getName())) {
                profile.setName(NEW_NAME);
            }
            if (profile.getUsername() == null || profile.getUsername().isBlank()) {
                profile.setUsername(index == 0 ? "admin" : "user" + profile.getId());
            }
            if (profile.getPasswordHash() == null || profile.getPasswordHash().isBlank()) {
                profile.setPasswordHash(hashPassword(index == 0 ? "admin123" : "health123"));
            }
            if (profile.getSessionToken() == null || profile.getSessionToken().isBlank()) {
                profile.setSessionToken(generateToken());
            }
            if (profile.getCreatedAt() == null) {
                profile.setCreatedAt(LocalDateTime.now(APP_ZONE));
            }
            if (profile.getHandleName() == null || profile.getHandleName().isBlank()) {
                profile.setHandleName("@" + profile.getUsername());
            }
            userProfileRepository.save(profile);
        }

        dailySummaryRepository.findAll().forEach(summary -> {
            if (summary.getOwner() == null) {
                summary.setOwner(fallbackOwner);
            }
            if (summary.getRecordDate() != null && summary.getRecordDate().isBefore(today)) {
                summary.setRecordDate(today);
            }
            dailySummaryRepository.save(summary);
        });

        trendRecordRepository.findAll().forEach(trend -> {
            if (trend.getOwner() == null) {
                trend.setOwner(fallbackOwner);
            }
            trendRecordRepository.save(trend);
        });

        mealEntryRepository.findAll().forEach(meal -> {
            if (meal.getOwner() == null) {
                meal.setOwner(fallbackOwner);
            }
            mealEntryRepository.save(meal);
        });

        workoutPlanRepository.findAll().forEach(workout -> {
            if (workout.getOwner() == null) {
                workout.setOwner(fallbackOwner);
            }
            workoutPlanRepository.save(workout);
        });
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
