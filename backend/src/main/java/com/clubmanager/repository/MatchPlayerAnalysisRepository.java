package com.clubmanager.repository;

import com.clubmanager.domain.MatchPlayerAnalysis;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.TeamMatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPlayerAnalysisRepository extends JpaRepository<MatchPlayerAnalysis, Long> {

    @EntityGraph(attributePaths = {"player", "player.positions", "match"})
    List<MatchPlayerAnalysis> findByMatch(TeamMatch match);

    @EntityGraph(attributePaths = {"player", "match"})
    Optional<MatchPlayerAnalysis> findByMatchAndPlayer(TeamMatch match, Player player);
}
