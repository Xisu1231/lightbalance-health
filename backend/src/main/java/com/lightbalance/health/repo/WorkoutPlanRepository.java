package com.lightbalance.health.repo;

import com.lightbalance.health.domain.UserProfile;
import com.lightbalance.health.domain.WorkoutPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findAllByOwner(UserProfile owner);

    Optional<WorkoutPlan> findByIdAndOwner(Long id, UserProfile owner);
}
