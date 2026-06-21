package com.clubmanager.repository;

import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamMatch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMatchRepository extends JpaRepository<TeamMatch, Long> {

    @EntityGraph(attributePaths = {"team", "championship"})
    Optional<TeamMatch> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"team", "championship"})
    List<TeamMatch> findByTeamOrderByMatchDateTimeDesc(Team team);

    long countByChampionship(com.clubmanager.domain.Championship championship);
}
