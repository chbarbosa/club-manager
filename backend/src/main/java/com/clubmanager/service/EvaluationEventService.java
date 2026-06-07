package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationEvent;
import com.clubmanager.domain.EvaluationEventAttendance;
import com.clubmanager.domain.EvaluationEventStatus;
import com.clubmanager.domain.EvaluationPlayer;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerSkillHistory;
import com.clubmanager.domain.Admin;
import com.clubmanager.dto.EvaluationEventAttendanceUpdateRequest;
import com.clubmanager.dto.EvaluationEventCancelRequest;
import com.clubmanager.dto.EvaluationEventCreateRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.EvaluationEventAttendanceRepository;
import com.clubmanager.repository.EvaluationEventRepository;
import com.clubmanager.repository.EvaluationPlayerRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerSkillHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EvaluationEventService {

    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(60, 90, 120);

    private final EvaluationRepository evaluationRepository;
    private final EvaluationEventRepository evaluationEventRepository;
    private final EvaluationPlayerRepository evaluationPlayerRepository;
    private final EvaluationEventAttendanceRepository attendanceRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSkillHistoryRepository playerSkillHistoryRepository;
    private final AdminRepository adminRepository;

    public EvaluationEventService(
            EvaluationRepository evaluationRepository,
            EvaluationEventRepository evaluationEventRepository,
            EvaluationPlayerRepository evaluationPlayerRepository,
            EvaluationEventAttendanceRepository attendanceRepository,
            PlayerRepository playerRepository,
            PlayerSkillHistoryRepository playerSkillHistoryRepository,
            AdminRepository adminRepository) {
        this.evaluationRepository = evaluationRepository;
        this.evaluationEventRepository = evaluationEventRepository;
        this.evaluationPlayerRepository = evaluationPlayerRepository;
        this.attendanceRepository = attendanceRepository;
        this.playerRepository = playerRepository;
        this.playerSkillHistoryRepository = playerSkillHistoryRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional(readOnly = true)
    public List<EvaluationEvent> getEvents(UUID evaluationUuid) {
        return evaluationEventRepository.findByEvaluationOrderByEventDateAscStartTimeAsc(getEvaluation(evaluationUuid));
    }

    @Transactional
    public EvaluationEvent createEvent(UUID evaluationUuid, EvaluationEventCreateRequest request) {
        Evaluation evaluation = getEvaluation(evaluationUuid);
        ensureEvaluationEditable(evaluation);
        requireText(request.place(), "place");
        validateDuration(request.durationMinutes());

        EvaluationEvent event = EvaluationEvent.builder()
                .evaluation(evaluation)
                .place(request.place().trim())
                .eventDate(request.eventDate())
                .startTime(request.startTime())
                .durationMinutes(request.durationMinutes())
                .build();
        return evaluationEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<EvaluationEventAttendance> getAttendance(UUID eventUuid) {
        return attendanceRepository.findByEvaluationEventOrderByPlayerNameAsc(getEvent(eventUuid));
    }

    @Transactional
    public EvaluationEventAttendance updateAttendance(
            UUID eventUuid, UUID playerUuid, EvaluationEventAttendanceUpdateRequest request) {
        EvaluationEvent event = getEvent(eventUuid);
        ensureEventScheduled(event);
        Player player = getPlayer(playerUuid);
        ensurePlayerAssignedToEvaluation(event.getEvaluation(), player);

        EvaluationEventAttendance attendance = attendanceRepository.findByEvaluationEventAndPlayer(event, player)
                .orElseGet(() -> EvaluationEventAttendance.builder()
                        .evaluationEvent(event)
                        .player(player)
                        .build());
        attendance.setStatus(request.status());
        attendance.setSkillLevel(request.skillLevel());
        attendance.setReason(StringUtils.hasText(request.reason()) ? request.reason().trim() : null);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public EvaluationEvent completeEvent(UUID eventUuid) {
        EvaluationEvent event = getEvent(eventUuid);
        ensureEventScheduled(event);
        List<EvaluationPlayer> players = evaluationPlayerRepository
                .findByEvaluationAndActiveTrueOrderByPlayerNameAsc(event.getEvaluation());
        if (players.isEmpty()) {
            throw new IllegalArgumentException("At least one player must be assigned before completing an event");
        }
        List<EvaluationEventAttendance> completedAttendance = new java.util.ArrayList<>();
        for (EvaluationPlayer evaluationPlayer : players) {
            Optional<EvaluationEventAttendance> attendance = attendanceRepository
                    .findByEvaluationEventAndPlayer(event, evaluationPlayer.getPlayer());
            if (attendance.isEmpty()
                    || attendance.get().getStatus() == null
                    || attendance.get().getSkillLevel() == null) {
                throw new IllegalArgumentException(
                        "All assigned players must have participation and skill level before closing the event");
            }
            completedAttendance.add(attendance.get());
        }
        applySkillUpdates(event, completedAttendance);
        event.setStatus(EvaluationEventStatus.COMPLETED);
        return evaluationEventRepository.save(event);
    }

    @Transactional
    public EvaluationEvent cancelEvent(UUID eventUuid, EvaluationEventCancelRequest request) {
        EvaluationEvent event = getEvent(eventUuid);
        ensureEventScheduled(event);
        event.setStatus(EvaluationEventStatus.CANCELED);
        event.setCancelReason(request == null || !StringUtils.hasText(request.cancelReason())
                ? null
                : request.cancelReason().trim());
        return evaluationEventRepository.save(event);
    }

    private Evaluation getEvaluation(UUID uuid) {
        return evaluationRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + uuid));
    }

    private EvaluationEvent getEvent(UUID uuid) {
        return evaluationEventRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation event not found: " + uuid));
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

    private void ensureEventScheduled(EvaluationEvent event) {
        if (event.getStatus() != EvaluationEventStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only scheduled events can be changed");
        }
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || !ALLOWED_DURATIONS.contains(durationMinutes)) {
            throw new IllegalArgumentException("durationMinutes must be one of 60, 90, or 120");
        }
    }

    private void ensurePlayerAssignedToEvaluation(Evaluation evaluation, Player player) {
        if (!evaluationPlayerRepository.existsByEvaluationAndPlayerAndActiveTrue(evaluation, player)) {
            throw new IllegalArgumentException("Player must be assigned to the evaluation before attendance can be recorded");
        }
    }

    private void applySkillUpdates(EvaluationEvent event, List<EvaluationEventAttendance> completedAttendance) {
        LocalDateTime changedAt = LocalDateTime.now();
        Admin currentAdmin = getCurrentAdmin()
                .orElseThrow(() -> new IllegalStateException("Authenticated admin is required to complete an evaluation event"));
        for (EvaluationEventAttendance attendance : completedAttendance) {
            Player player = attendance.getPlayer();
            player.setCurrentSkillLevel(attendance.getSkillLevel());
            playerRepository.save(player);
            playerSkillHistoryRepository.save(PlayerSkillHistory.builder()
                    .player(player)
                    .skillLevel(attendance.getSkillLevel())
                    .changedAt(changedAt)
                    .changedByAdmin(currentAdmin)
                    .evaluationEvent(event)
                    .description("Evaluation event completed: " + event.getPlace())
                    .build());
        }
    }

    private Optional<Admin> getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }
        return adminRepository.findByUsername(authentication.getName());
    }
}
