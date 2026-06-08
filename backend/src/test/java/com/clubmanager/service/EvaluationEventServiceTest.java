package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationAttendanceStatus;
import com.clubmanager.domain.EvaluationEvent;
import com.clubmanager.domain.EvaluationEventAttendance;
import com.clubmanager.domain.EvaluationPlayer;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.repository.EvaluationEventAttendanceRepository;
import com.clubmanager.repository.EvaluationEventRepository;
import com.clubmanager.repository.EvaluationPlayerRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.PlayerRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationEventServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private EvaluationEventRepository evaluationEventRepository;

    @Mock
    private EvaluationPlayerRepository evaluationPlayerRepository;

    @Mock
    private EvaluationEventAttendanceRepository attendanceRepository;

    @Mock
    private PlayerRepository playerRepository;

    private EvaluationEventService evaluationEventService;

    @BeforeEach
    void setUp() {
        evaluationEventService = new EvaluationEventService(
                evaluationRepository,
                evaluationEventRepository,
                evaluationPlayerRepository,
                attendanceRepository,
                playerRepository);
    }

    @Test
    void completeEvent_WhenAssignedPlayerHasParticipation_CompletesWithoutSkillUpdates() {
        Evaluation evaluation = Evaluation.builder()
                .title("Spring Tryouts")
                .ageGroup("Under 13")
                .teamCategory(TeamCategory.MASCULINE)
                .createdDate(LocalDate.now())
                .build();
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
                .eventDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(18, 0))
                .durationMinutes(90)
                .build();
        EvaluationPlayer evaluationPlayer = EvaluationPlayer.builder()
                .evaluation(evaluation)
                .player(player)
                .assignedDate(LocalDate.now())
                .build();
        EvaluationEventAttendance attendance = EvaluationEventAttendance.builder()
                .evaluationEvent(event)
                .player(player)
                .status(EvaluationAttendanceStatus.PRESENT)
                .build();

        when(evaluationEventRepository.findByUuid(event.getUuid())).thenReturn(Optional.of(event));
        when(evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation))
                .thenReturn(List.of(evaluationPlayer));
        when(attendanceRepository.findByEvaluationEventAndPlayer(event, player)).thenReturn(Optional.of(attendance));
        when(evaluationEventRepository.save(event)).thenReturn(event);

        verify(playerRepository, never()).save(player);
        evaluationEventService.completeEvent(event.getUuid());
        verify(evaluationEventRepository).save(event);
    }

    @Test
    void completeEvent_WhenAssignedPlayerHasNoParticipation_ThrowsValidationException() {
        Evaluation evaluation = Evaluation.builder()
                .title("Spring Tryouts")
                .ageGroup("Under 13")
                .teamCategory(TeamCategory.MASCULINE)
                .createdDate(LocalDate.now())
                .build();
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
                .eventDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(18, 0))
                .durationMinutes(90)
                .build();
        EvaluationPlayer evaluationPlayer = EvaluationPlayer.builder()
                .evaluation(evaluation)
                .player(player)
                .assignedDate(LocalDate.now())
                .build();

        when(evaluationEventRepository.findByUuid(event.getUuid())).thenReturn(Optional.of(event));
        when(evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation))
                .thenReturn(List.of(evaluationPlayer));
        when(attendanceRepository.findByEvaluationEventAndPlayer(event, player)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationEventService.completeEvent(event.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All assigned players must have participation before closing the event");

        verify(evaluationEventRepository, never()).save(event);
    }
}
