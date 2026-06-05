package com.clubmanager.repository;

import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamCategory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = "trainer")
    Optional<Team> findByUuid(UUID uuid);

    @Override
    @EntityGraph(attributePaths = "trainer")
    Page<Team> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "trainer")
    Page<Team> findByAgeGroupContainingIgnoreCase(String ageGroup, Pageable pageable);

    @EntityGraph(attributePaths = "trainer")
    Page<Team> findByTeamCategory(TeamCategory teamCategory, Pageable pageable);

    @EntityGraph(attributePaths = "trainer")
    Page<Team> findByAgeGroupContainingIgnoreCaseAndTeamCategory(String ageGroup, TeamCategory teamCategory, Pageable pageable);
}
