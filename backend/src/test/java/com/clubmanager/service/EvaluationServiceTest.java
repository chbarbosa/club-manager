package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private EvaluationEventRepository evaluationEventRepository;

    @Mock
    private EvaluationEventAttendanceRepository attendanceRepository;

    @Mock
    private EvaluationPlayerRepository evaluationPlayerRepository;

    @Mock
    private EvaluationResultRepository evaluationResultRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerSkillHistoryRepository playerSkillHistoryRepository;

    @Mock
    private AdminRepository adminRepository;

    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationService(
                evaluationRepository,
                evaluationEventRepository,
                attendanceRepository,
                evaluationPlayerRepository,
                evaluationResultRepository,
                playerRepository,
                playerSkillHistoryRepository,
                adminRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createEvaluation_WithValidRequest_ReturnsOpenEvaluation() {
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Evaluation evaluation = evaluationService.createEvaluation(
                new EvaluationCreateRequest("Spring Tryouts", "Under 13", TeamCategory.MASCULINE));

        assertThat(evaluation.getTitle()).isEqualTo("Spring Tryouts");
        assertThat(evaluation.getAgeGroup()).isEqualTo("Under 13");
        assertThat(evaluation.getTeamCategory()).isEqualTo(TeamCategory.MASCULINE);
        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.OPEN);
        assertThat(evaluation.getCreatedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createEvaluation_WithBlankTitle_ThrowsValidationException() {
        assertThatThrownBy(() -> evaluationService.createEvaluation(
                new EvaluationCreateRequest(" ", "Under 13", TeamCategory.MASCULINE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    @Test
    void createEvaluation_WithBlankAgeGroup_ThrowsValidationException() {
        assertThatThrownBy(() -> evaluationService.createEvaluation(
                new EvaluationCreateRequest("Spring Tryouts", " ", TeamCategory.MASCULINE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ageGroup");
    }

    @Test
    void updateEvaluation_WithOpenEvaluation_UpdatesFields() {
        Evaluation evaluation = evaluation();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation updated = evaluationService.updateEvaluation(
                evaluation.getUuid(),
                new EvaluationUpdateRequest("Summer Tryouts", "Under 15", TeamCategory.FEMININE));

        assertThat(updated.getTitle()).isEqualTo("Summer Tryouts");
        assertThat(updated.getAgeGroup()).isEqualTo("Under 15");
        assertThat(updated.getTeamCategory()).isEqualTo(TeamCategory.FEMININE);
        assertThat(updated.getCreatedDate()).isEqualTo(evaluation.getCreatedDate());
    }

    @Test
    void updateEvaluation_WithFinalizedEvaluation_ThrowsValidationException() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.FINALIZED);
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));

        assertThatThrownBy(() -> evaluationService.updateEvaluation(
                evaluation.getUuid(),
                new EvaluationUpdateRequest("Summer Tryouts", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Finalized");
    }

    @Test
    void startEvaluation_WhenOpen_SetsInProgress() {
        Evaluation evaluation = evaluation();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(true);
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation started = evaluationService.startEvaluation(evaluation.getUuid());

        assertThat(started.getStatus()).isEqualTo(EvaluationStatus.IN_PROGRESS);
    }

    @Test
    void startEvaluation_WithoutEvents_ThrowsValidationException() {
        Evaluation evaluation = evaluation();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(false);

        assertThatThrownBy(() -> evaluationService.startEvaluation(evaluation.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one event is required before starting an evaluation");
    }

    @Test
    void finalizeEvaluation_WhenInProgress_SetsFinalized() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(true);
        when(evaluationEventRepository.existsByEvaluationAndStatus(evaluation, EvaluationEventStatus.SCHEDULED))
                .thenReturn(false);
        when(evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation))
                .thenReturn(List.of());
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation finalized = evaluationService.finalizeEvaluation(evaluation.getUuid());

        assertThat(finalized.getStatus()).isEqualTo(EvaluationStatus.FINALIZED);
    }

    @Test
    void finalizeEvaluation_WithScheduledEvent_ThrowsValidationException() {
        Evaluation evaluation = evaluation();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(true);
        when(evaluationEventRepository.existsByEvaluationAndStatus(evaluation, EvaluationEventStatus.SCHEDULED))
                .thenReturn(true);

        assertThatThrownBy(() -> evaluationService.finalizeEvaluation(evaluation.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All evaluation events must be completed or canceled before finalizing");
    }

    @Test
    void finalizeEvaluation_WithAssignedPlayerBeforeParticipantEvaluation_ThrowsValidationException() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        Player player = Player.builder()
                .name("Player One")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(12))
                .teamCategory(TeamCategory.MASCULINE)
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
        EvaluationPlayer evaluationPlayer = EvaluationPlayer.builder()
                .evaluation(evaluation)
                .player(player)
                .assignedDate(LocalDate.now())
                .build();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(true);
        when(evaluationEventRepository.existsByEvaluationAndStatus(evaluation, EvaluationEventStatus.SCHEDULED))
                .thenReturn(false);
        when(evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation))
                .thenReturn(List.of(evaluationPlayer));

        assertThatThrownBy(() -> evaluationService.finalizeEvaluation(evaluation.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All assigned players must be evaluated before finalizing");
    }

    @Test
    void updateResult_WithClosedEvents_UpdatesPlayerSkillAndHistory() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        Player player = Player.builder()
                .name("Player One")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(12))
                .teamCategory(TeamCategory.MASCULINE)
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
        EvaluationEvent event = EvaluationEvent.builder()
                .evaluation(evaluation)
                .place("Main Field")
                .eventDate(LocalDate.now())
                .startTime(LocalTime.of(18, 0))
                .durationMinutes(90)
                .status(EvaluationEventStatus.COMPLETED)
                .build();
        EvaluationEventAttendance attendance = EvaluationEventAttendance.builder()
                .evaluationEvent(event)
                .player(player)
                .status(EvaluationAttendanceStatus.PRESENT)
                .build();
        Admin admin = new Admin();
        admin.setName("Admin");
        admin.setUsername("admin");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "password"));
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(true);
        when(evaluationEventRepository.existsByEvaluationAndStatus(evaluation, EvaluationEventStatus.SCHEDULED))
                .thenReturn(false);
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(evaluationPlayerRepository.existsByEvaluationAndPlayerAndActiveTrue(evaluation, player)).thenReturn(true);
        when(evaluationResultRepository.findByEvaluationAndPlayer(evaluation, player)).thenReturn(Optional.empty());
        when(evaluationEventRepository.findByEvaluationOrderByEventDateAscStartTimeAsc(evaluation)).thenReturn(List.of(event));
        when(attendanceRepository.findByEvaluationEventAndPlayer(event, player)).thenReturn(Optional.of(attendance));
        when(evaluationResultRepository.save(any(EvaluationResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        EvaluationResult result = evaluationService.updateResult(
                evaluation.getUuid(), player.getUuid(), new EvaluationResultUpdateRequest(SkillLevel.SKILLED));

        assertThat(result.getLevelResult()).isEqualTo(SkillLevel.SKILLED);
        assertThat(result.getAttendanceStatus()).isEqualTo(EvaluationAttendanceStatus.PRESENT);
        assertThat(result.getSourceEvent()).isNull();
        assertThat(player.getCurrentSkillLevel()).isEqualTo(SkillLevel.SKILLED);
        ArgumentCaptor<com.clubmanager.domain.PlayerSkillHistory> historyCaptor =
                ArgumentCaptor.forClass(com.clubmanager.domain.PlayerSkillHistory.class);
        verify(playerRepository).save(player);
        verify(playerSkillHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getChangedByAdmin()).isEqualTo(admin);
        assertThat(historyCaptor.getValue().getDescription()).isEqualTo("Evaluation finalized: Spring Tryouts");
    }

    @Test
    void finalizeEvaluation_WithAssignedPlayerAlreadyEvaluated_SetsFinalized() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        Player player = Player.builder()
                .name("Player One")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(12))
                .teamCategory(TeamCategory.MASCULINE)
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
        EvaluationPlayer evaluationPlayer = EvaluationPlayer.builder()
                .evaluation(evaluation)
                .player(player)
                .assignedDate(LocalDate.now())
                .build();
        EvaluationResult result = EvaluationResult.builder()
                .evaluation(evaluation)
                .player(player)
                .levelResult(SkillLevel.SKILLED)
                .attendanceStatus(EvaluationAttendanceStatus.PRESENT)
                .finalizedAt(java.time.LocalDateTime.now())
                .build();

        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationEventRepository.existsByEvaluation(evaluation)).thenReturn(true);
        when(evaluationEventRepository.existsByEvaluationAndStatus(evaluation, EvaluationEventStatus.SCHEDULED))
                .thenReturn(false);
        when(evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation))
                .thenReturn(List.of(evaluationPlayer));
        when(evaluationResultRepository.findByEvaluationAndPlayer(evaluation, player)).thenReturn(Optional.of(result));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation finalized = evaluationService.finalizeEvaluation(evaluation.getUuid());

        assertThat(finalized.getStatus()).isEqualTo(EvaluationStatus.FINALIZED);
    }

    private Evaluation evaluation() {
        return Evaluation.builder()
                .title("Spring Tryouts")
                .ageGroup("Under 13")
                .teamCategory(TeamCategory.MASCULINE)
                .createdDate(LocalDate.now().minusDays(1))
                .build();
    }
}
