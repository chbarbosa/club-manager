package com.clubmanager.repository;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationEvent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationEventRepository extends JpaRepository<EvaluationEvent, Long> {

    @EntityGraph(attributePaths = "evaluation")
    Optional<EvaluationEvent> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = "evaluation")
    List<EvaluationEvent> findByEvaluationOrderByEventDateAscStartTimeAsc(Evaluation evaluation);
}
