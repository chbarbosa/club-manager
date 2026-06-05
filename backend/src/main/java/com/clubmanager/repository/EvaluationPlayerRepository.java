package com.clubmanager.repository;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationPlayer;
import com.clubmanager.domain.Player;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationPlayerRepository extends JpaRepository<EvaluationPlayer, Long> {

    @EntityGraph(attributePaths = {"evaluation", "player"})
    Optional<EvaluationPlayer> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"evaluation", "player"})
    List<EvaluationPlayer> findByEvaluationAndActiveTrueOrderByPlayerNameAsc(Evaluation evaluation);

    boolean existsByEvaluationAndPlayerAndActiveTrue(Evaluation evaluation, Player player);
}
