package com.lightbalance.health.repo;

import com.lightbalance.health.domain.MealEntry;
import com.lightbalance.health.domain.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    List<MealEntry> findAllByOrderByRecommendedTimeAsc();

    List<MealEntry> findAllByOwnerOrderByRecommendedTimeAsc(UserProfile owner);

    Optional<MealEntry> findByIdAndOwner(Long id, UserProfile owner);
}
