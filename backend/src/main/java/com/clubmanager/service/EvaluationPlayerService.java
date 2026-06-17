package com.clubmanager.service;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationPlayer;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.Player;
import com.clubmanager.dto.EvaluationPlayerAssignRequest;
import com.clubmanager.repository.EvaluationPlayerRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EvaluationPlayerService {

    private final EvaluationRepository evaluationRepository;
    private final PlayerRepository playerRepository;
    private final EvaluationPlayerRepository evaluationPlayerRepository;



    @Transactional(readOnly = true)
    public List<EvaluationPlayer> getActivePlayers(UUID evaluationUuid) {
        return evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(getEvaluation(evaluationUuid));
    }

    @Transactional
    public EvaluationPlayer assignPlayer(UUID evaluationUuid, EvaluationPlayerAssignRequest request) {
        Evaluation evaluation = getEvaluation(evaluationUuid);
        ensureEvaluationEditable(evaluation);
        Player player = getPlayer(request.playerUuid());
        if (!player.isActive()) {
            throw new IllegalArgumentException("Evaluation player must be active");
        }
        if (player.getTeamCategory() != evaluation.getTeamCategory()) {
            throw new IllegalArgumentException("Player team category must match the evaluation team category");
        }
        if (evaluationPlayerRepository.existsByEvaluationAndPlayerAndActiveTrue(evaluation, player)) {
            throw new IllegalArgumentException("Player is already assigned to this evaluation");
        }

        EvaluationPlayer evaluationPlayer = EvaluationPlayer.builder()
                .evaluation(evaluation)
                .player(player)
                .assignedDate(LocalDate.now())
                .build();
        return evaluationPlayerRepository.save(evaluationPlayer);
    }

    @Transactional
    public EvaluationPlayer removePlayer(UUID evaluationUuid, UUID assignmentUuid) {
        Evaluation evaluation = getEvaluation(evaluationUuid);
        ensureEvaluationEditable(evaluation);
        EvaluationPlayer evaluationPlayer = evaluationPlayerRepository.findByUuid(assignmentUuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation player assignment not found: " + assignmentUuid));
        if (!evaluationPlayer.getEvaluation().getUuid().equals(evaluationUuid)) {
            throw new IllegalArgumentException("Player assignment does not belong to this evaluation");
        }
        evaluationPlayer.setActive(false);
        return evaluationPlayerRepository.save(evaluationPlayer);
    }

    private Evaluation getEvaluation(UUID uuid) {
        return evaluationRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + uuid));
    }

    private Player getPlayer(UUID uuid) {
        return playerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + uuid));
    }

    private void ensureEvaluationEditable(Evaluation evaluation) {
        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new IllegalArgumentException("Finalized evaluations cannot be changed");
        }
    }
}
