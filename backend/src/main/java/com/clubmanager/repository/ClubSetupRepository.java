package com.clubmanager.repository;

import com.clubmanager.domain.ClubSetup;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubSetupRepository extends JpaRepository<ClubSetup, Long> {

    Optional<ClubSetup> findByType(String type);

    Optional<ClubSetup> findByUuid(UUID uuid);
}

