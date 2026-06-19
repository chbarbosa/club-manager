package com.clubmanager.repository;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, Long> {

    @EntityGraph(attributePaths = {"player", "player.positions", "team"})
    Optional<PlayerTeam> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"player", "player.positions", "team"})
    List<PlayerTeam> findByTeamAndActiveTrueOrderByPlayer_NameAsc(Team team);

    Optional<PlayerTeam> findByPlayerAndActiveTrue(Player player);

    boolean existsByTeamAndPlayerAndActiveTrue(Team team, Player player);

    boolean existsByTeamAndJerseyNumberAndActiveTrue(Team team, Integer jerseyNumber);
}
