package com.clubmanager.repository;

import com.clubmanager.domain.EvaluationEvent;
import com.clubmanager.domain.EvaluationEventAttendance;
import com.clubmanager.domain.Player;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationEventAttendanceRepository extends JpaRepository<EvaluationEventAttendance, Long> {

    @EntityGraph(attributePaths = {"evaluationEvent", "player"})
    Optional<EvaluationEventAttendance> findByEvaluationEventAndPlayer(EvaluationEvent evaluationEvent, Player player);

    @EntityGraph(attributePaths = {"evaluationEvent", "player"})
    List<EvaluationEventAttendance> findByEvaluationEventOrderByPlayerNameAsc(EvaluationEvent evaluationEvent);
}
