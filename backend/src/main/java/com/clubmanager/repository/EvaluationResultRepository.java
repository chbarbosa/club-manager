package com.clubmanager.repository;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationResult;
import com.clubmanager.domain.Player;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {

    @EntityGraph(attributePaths = {"evaluation", "player", "sourceEvent"})
    List<EvaluationResult> findByEvaluationOrderByPlayerNameAsc(Evaluation evaluation);

    @EntityGraph(attributePaths = {"evaluation", "player", "sourceEvent"})
    Optional<EvaluationResult> findByEvaluationAndPlayer(Evaluation evaluation, Player player);
}
