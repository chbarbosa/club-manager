package com.clubmanager.repository;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ChampionshipRoster;
import com.clubmanager.domain.Player;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChampionshipRosterRepository extends JpaRepository<ChampionshipRoster, Long> {

    @EntityGraph(attributePaths = {"championship", "player", "player.positions", "trainer"})
    Optional<ChampionshipRoster> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"championship", "player", "player.positions", "trainer"})
    List<ChampionshipRoster> findByChampionshipAndActiveTrueOrderByPlayer_NameAsc(Championship championship);

    boolean existsByChampionshipAndPlayerAndActiveTrue(Championship championship, Player player);
}
