package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;
import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationAttendanceStatus;
import com.clubmanager.domain.EvaluationEvent;
import com.clubmanager.domain.EvaluationEventAttendance;
import com.clubmanager.domain.EvaluationEventStatus;
import com.clubmanager.domain.EvaluationPlayer;
import com.clubmanager.domain.EvaluationResult;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerSkillHistory;
import com.clubmanager.domain.SkillLevel;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.EvaluationCreateRequest;
import com.clubmanager.dto.EvaluationResultUpdateRequest;
import com.clubmanager.dto.EvaluationUpdateRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.EvaluationEventAttendanceRepository;
import com.clubmanager.repository.EvaluationEventRepository;
import com.clubmanager.repository.EvaluationPlayerRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.EvaluationResultRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerSkillHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationEventRepository evaluationEventRepository;
    private final EvaluationEventAttendanceRepository attendanceRepository;
    private final EvaluationPlayerRepository evaluationPlayerRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSkillHistoryRepository playerSkillHistoryRepository;
    private final AdminRepository adminRepository;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            EvaluationEventRepository evaluationEventRepository,
            EvaluationEventAttendanceRepository attendanceRepository,
            EvaluationPlayerRepository evaluationPlayerRepository,
            EvaluationResultRepository evaluationResultRepository,
            PlayerRepository playerRepository,
            PlayerSkillHistoryRepository playerSkillHistoryRepository,
            AdminRepository adminRepository) {
        this.evaluationRepository = evaluationRepository;
        this.evaluationEventRepository = evaluationEventRepository;
        this.attendanceRepository = attendanceRepository;
        this.evaluationPlayerRepository = evaluationPlayerRepository;
        this.evaluationResultRepository = evaluationResultRepository;
        this.playerRepository = playerRepository;
        this.playerSkillHistoryRepository = playerSkillHistoryRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public Evaluation createEvaluation(EvaluationCreateRequest request) {
        requireText(request.title(), "title");
        requireText(request.ageGroup(), "ageGroup");

        Evaluation evaluation = Evaluation.builder()
                .title(request.title().trim())
                .ageGroup(request.ageGroup().trim())
                .teamCategory(request.teamCategory())
                .createdDate(LocalDate.now())
                .build();
        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public Evaluation getEvaluationByUuid(UUID uuid) {
        return evaluationRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + uuid));
    }

    @Transactional(readOnly = true)
    public Page<Evaluation> searchEvaluations(
            String title, String ageGroup, TeamCategory teamCategory, EvaluationStatus status, Pageable pageable) {
        //TODO improve this
        boolean hasTitle = title != null && !title.isBlank();
        boolean hasAgeGroup = ageGroup != null && !ageGroup.isBlank();
        if (hasTitle && hasAgeGroup && teamCategory != null && status != null) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCaseAndTeamCategoryAndStatus(
                    title.trim(), ageGroup.trim(), teamCategory, status, pageable);
        }
        if (hasTitle && hasAgeGroup && teamCategory != null) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCaseAndTeamCategory(
                    title.trim(), ageGroup.trim(), teamCategory, pageable);
        }
        if (hasTitle && hasAgeGroup && status != null) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCaseAndStatus(
                    title.trim(), ageGroup.trim(), status, pageable);
        }
        if (hasTitle && teamCategory != null && status != null) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndTeamCategoryAndStatus(
                    title.trim(), teamCategory, status, pageable);
        }
        if (hasTitle && hasAgeGroup) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCase(
                    title.trim(), ageGroup.trim(), pageable);
        }
        if (hasTitle && teamCategory != null) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndTeamCategory(
                    title.trim(), teamCategory, pageable);
        }
        if (hasTitle && status != null) {
            return evaluationRepository.findByTitleContainingIgnoreCaseAndStatus(title.trim(), status, pageable);
        }
        if (hasTitle) {
            return evaluationRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
        }
        if (hasAgeGroup && teamCategory != null && status != null) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCaseAndTeamCategoryAndStatus(
                    ageGroup.trim(), teamCategory, status, pageable);
        }
        if (hasAgeGroup && teamCategory != null) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCaseAndTeamCategory(
                    ageGroup.trim(), teamCategory, pageable);
        }
        if (hasAgeGroup && status != null) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCaseAndStatus(ageGroup.trim(), status, pageable);
        }
        if (teamCategory != null && status != null) {
            return evaluationRepository.findByTeamCategoryAndStatus(teamCategory, status, pageable);
        }
        if (hasAgeGroup) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCase(ageGroup.trim(), pageable);
        }
        if (teamCategory != null) {
            return evaluationRepository.findByTeamCategory(teamCategory, pageable);
        }
        if (status != null) {
            return evaluationRepository.findByStatus(status, pageable);
        }
        return evaluationRepository.findAll(pageable);
    }

    @Transactional
    public Evaluation updateEvaluation(UUID uuid, EvaluationUpdateRequest request) {
        Evaluation evaluation = getEvaluationByUuid(uuid);
        ensureNotFinalized(evaluation);

        applyTextUpdate(request.title(), "title", evaluation::setTitle);
        applyTextUpdate(request.ageGroup(), "ageGroup", evaluation::setAgeGroup);
        if (request.teamCategory() != null) {
            evaluation.setTeamCategory(request.teamCategory());
        }

        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public Evaluation startEvaluation(UUID uuid) {
        Evaluation evaluation = getEvaluationByUuid(uuid);
        if (evaluation.getStatus() != EvaluationStatus.OPEN) {
            throw new IllegalArgumentException("Only open evaluations can be started");
        }
        ensureHasEvents(evaluation, "At least one event is required before starting an evaluation");
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public Evaluation finalizeEvaluation(UUID uuid) {
        Evaluation evaluation = getEvaluationByUuid(uuid);
        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new IllegalArgumentException("Evaluation is already finalized");
        }
        ensureHasEvents(evaluation, "At least one event is required before finalizing an evaluation");
        ensureAllEventsClosed(evaluation);
        ensureAllPlayersEvaluated(evaluation);
        evaluation.setStatus(EvaluationStatus.FINALIZED);
        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResult> getResults(UUID uuid) {
        return evaluationResultRepository.findByEvaluationOrderByPlayerNameAsc(getEvaluationByUuid(uuid));
    }

    @Transactional
    public EvaluationResult updateResult(UUID evaluationUuid, UUID playerUuid, EvaluationResultUpdateRequest request) {
        Evaluation evaluation = getEvaluationByUuid(evaluationUuid);
        ensureNotFinalized(evaluation);
        ensureHasEvents(evaluation, "At least one event is required before evaluating participants");
        ensureAllEventsClosed(evaluation);
        Player player = playerRepository.findByUuid(playerUuid)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + playerUuid));
        if (!evaluationPlayerRepository.existsByEvaluationAndPlayerAndActiveTrue(evaluation, player)) {
            throw new IllegalArgumentException("Player must be assigned to the evaluation before being evaluated");
        }
        EvaluationResult result = evaluationResultRepository.findByEvaluationAndPlayer(evaluation, player)
                .orElseGet(() -> EvaluationResult.builder()
                        .evaluation(evaluation)
                        .player(player)
                        .attendanceStatus(resolveParticipation(evaluation, player))
                        .build());
        result.setLevelResult(request.levelResult());
        result.setFinalizedAt(LocalDateTime.now());
        EvaluationResult saved = evaluationResultRepository.save(result);
        updatePlayerSkill(player, request.levelResult(), evaluation);
        return saved;
    }

    private void ensureNotFinalized(Evaluation evaluation) {
        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new IllegalArgumentException("Finalized evaluations cannot be updated");
        }
    }

    private void ensureHasEvents(Evaluation evaluation, String message) {
        if (!evaluationEventRepository.existsByEvaluation(evaluation)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void ensureAllEventsClosed(Evaluation evaluation) {
        if (evaluationEventRepository.existsByEvaluationAndStatus(evaluation, EvaluationEventStatus.SCHEDULED)) {
            throw new IllegalArgumentException("All evaluation events must be completed or canceled before finalizing");
        }
    }

    private void ensureAllPlayersEvaluated(Evaluation evaluation) {
        List<EvaluationPlayer> players = evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation);
        for (EvaluationPlayer evaluationPlayer : players) {
            Optional<EvaluationResult> result = evaluationResultRepository
                    .findByEvaluationAndPlayer(evaluation, evaluationPlayer.getPlayer());
            if (result.isEmpty() || result.get().getLevelResult() == null) {
                throw new IllegalArgumentException("All assigned players must be evaluated before finalizing");
            }
        }
    }

    private EvaluationAttendanceStatus resolveParticipation(Evaluation evaluation, Player player) {
        List<EvaluationEvent> events = evaluationEventRepository.findByEvaluationOrderByEventDateAscStartTimeAsc(evaluation);
        boolean participated = events.stream()
                .map(event -> attendanceRepository.findByEvaluationEventAndPlayer(event, player))
                .flatMap(Optional::stream)
                .anyMatch(attendance -> attendance.getStatus() == EvaluationAttendanceStatus.PRESENT);
        return participated ? EvaluationAttendanceStatus.PRESENT : EvaluationAttendanceStatus.ABSENT;
    }

    private void updatePlayerSkill(Player player, SkillLevel skillLevel, Evaluation evaluation) {
        Admin currentAdmin = getCurrentAdmin()
                .orElseThrow(() -> new IllegalStateException("Authenticated admin is required to evaluate participants"));
        player.setCurrentSkillLevel(skillLevel);
        playerRepository.save(player);
        playerSkillHistoryRepository.save(PlayerSkillHistory.builder()
                .player(player)
                .skillLevel(skillLevel)
                .changedAt(LocalDateTime.now())
                .changedByAdmin(currentAdmin)
                .description("Evaluation finalized: " + evaluation.getTitle())
                .build());
    }

    private Optional<Admin> getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }
        return adminRepository.findByUsername(authentication.getName());
    }
}
