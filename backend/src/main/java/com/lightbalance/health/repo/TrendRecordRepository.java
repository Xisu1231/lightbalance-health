package com.lightbalance.health.repo;

import com.lightbalance.health.domain.TrendRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendRecordRepository extends JpaRepository<TrendRecord, Long> {

    List<TrendRecord> findAllByOrderByRecordDateAsc();
}
