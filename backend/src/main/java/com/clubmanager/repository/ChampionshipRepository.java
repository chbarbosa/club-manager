package com.clubmanager.repository;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.Team;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChampionshipRepository extends JpaRepository<Championship, Long> {

    @EntityGraph(attributePaths = {"team", "team.trainer"})
    Optional<Championship> findByUuid(UUID uuid);

    @Override
    @EntityGraph(attributePaths = {"team", "team.trainer"})
    Page<Championship> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"team", "team.trainer"})
    Page<Championship> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "team.trainer"})
    Page<Championship> findByTeam(Team team, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "team.trainer"})
    Page<Championship> findByNameContainingIgnoreCaseAndTeam(String name, Team team, Pageable pageable);

    @Query("""
            select count(distinct championship)
            from Championship championship, PlayerTeam assignment
            where assignment.player.uuid = :playerUuid
              and assignment.team = championship.team
            """)
    long countDistinctByPlayerUuid(@Param("playerUuid") UUID playerUuid);
}
