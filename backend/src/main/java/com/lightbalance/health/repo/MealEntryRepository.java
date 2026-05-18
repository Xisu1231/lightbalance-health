package com.lightbalance.health.repo;

import com.lightbalance.health.domain.MealEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    List<MealEntry> findAllByOrderByRecommendedTimeAsc();
}
