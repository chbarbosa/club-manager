package com.clubmanager.repository;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.TeamCategory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUuid(UUID uuid);

    Page<Player> findAllByActiveTrue(Pageable pageable);

    Page<Player> findAllByTeamCategory(TeamCategory teamCategory, Pageable pageable);

    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByRegistrationNumber(String registrationNumber);
}
