package com.lightbalance.health.repo;

import com.lightbalance.health.domain.DailySummary;
import com.lightbalance.health.domain.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {

    Optional<DailySummary> findFirstByOrderByRecordDateDesc();

    Optional<DailySummary> findFirstByOwnerOrderByRecordDateDesc(UserProfile owner);
}
