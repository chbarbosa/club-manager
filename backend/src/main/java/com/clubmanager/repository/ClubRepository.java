package com.clubmanager.repository;

import com.clubmanager.domain.Club;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByUuid(UUID uuid);
}

