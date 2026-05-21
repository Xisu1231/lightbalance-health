package com.lightbalance.health.repo;

import com.lightbalance.health.domain.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUsername(String username);

    Optional<UserProfile> findBySessionToken(String sessionToken);
}
