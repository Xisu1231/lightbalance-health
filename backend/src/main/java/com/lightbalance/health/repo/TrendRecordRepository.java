package com.lightbalance.health.repo;

import com.lightbalance.health.domain.TrendRecord;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendRecordRepository extends JpaRepository<TrendRecord, Long> {

    List<TrendRecord> findAllByOrderByRecordDateAsc();

    Optional<TrendRecord> findByRecordDate(LocalDate recordDate);
}
