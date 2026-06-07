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
import com.clubmanager.domain.Player;
import com.clubmanager.domain.SkillLevel;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.EvaluationEventAttendanceRepository;
import com.clubmanager.repository.EvaluationEventRepository;
import com.clubmanager.repository.EvaluationPlayerRepository;
import com.clubmanager.repository.EvaluationRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock
    private PlayerSkillHistoryRepository playerSkillHistoryRepository;

    @Mock
    private AdminRepository adminRepository;

    private EvaluationEventService evaluationEventService;

    @BeforeEach
    void setUp() {
        evaluationEventService = new EvaluationEventService(
                evaluationRepository,
                evaluationEventRepository,
                evaluationPlayerRepository,
                attendanceRepository,
                playerRepository,
                playerSkillHistoryRepository,
                adminRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeEvent_WhenAuthenticatedAdminRecordIsMissing_ThrowsBeforeSkillUpdates() {
        Evaluation evaluation = Evaluation.builder()
                .title("Spring Tryouts")
                .ageGroup("Under 13")
                .teamCategory(TeamCategory.MASCULINE)
                .createdDate(LocalDate.now())
                .build();
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
                .skillLevel(SkillLevel.SKILLED)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing-admin", "password"));
        when(evaluationEventRepository.findByUuid(event.getUuid())).thenReturn(Optional.of(event));
        when(evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation))
                .thenReturn(List.of(evaluationPlayer));
        when(attendanceRepository.findByEvaluationEventAndPlayer(event, player)).thenReturn(Optional.of(attendance));
        when(adminRepository.findByUsername("missing-admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationEventService.completeEvent(event.getUuid()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated admin is required to complete an evaluation event");

        verify(playerRepository, never()).save(player);
        verify(playerSkillHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(evaluationEventRepository, never()).save(event);
    }
}
