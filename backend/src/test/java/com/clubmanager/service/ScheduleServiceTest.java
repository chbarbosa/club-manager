package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.ClubField;
import com.clubmanager.domain.Schedule;
import com.clubmanager.domain.ScheduleStatus;
import com.clubmanager.domain.ScheduleType;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.ScheduleCreateRequest;
import com.clubmanager.dto.ScheduleUpdateRequest;
import com.clubmanager.repository.ClubFieldRepository;
import com.clubmanager.repository.ScheduleRepository;
import com.clubmanager.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ClubFieldRepository clubFieldRepository;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(scheduleRepository, teamRepository, clubFieldRepository);
    }

    @Test
    void createSchedule_WithValidRequest_CreatesScheduledEntry() {
        Team team = team(true);
        ClubField field = field(true);
        LocalDateTime dateTime = LocalDateTime.now().plusDays(3);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(clubFieldRepository.findByUuid(field.getUuid())).thenReturn(Optional.of(field));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule schedule = scheduleService.createSchedule(new ScheduleCreateRequest(
                team.getUuid(), field.getUuid(), dateTime, 90, ScheduleType.TRAINING, " Evening session "));

        assertThat(schedule.getTeam()).isEqualTo(team);
        assertThat(schedule.getField()).isEqualTo(field);
        assertThat(schedule.getDateTime()).isEqualTo(dateTime);
        assertThat(schedule.getDurationMinutes()).isEqualTo(90);
        assertThat(schedule.getType()).isEqualTo(ScheduleType.TRAINING);
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);
        assertThat(schedule.getNotes()).isEqualTo("Evening session");
    }

    @Test
    void createSchedule_WithInactiveTeam_ThrowsValidationException() {
        Team team = team(false);
        ClubField field = field(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> scheduleService.createSchedule(new ScheduleCreateRequest(
                team.getUuid(), field.getUuid(), LocalDateTime.now().plusDays(1), 60, ScheduleType.TRAINING, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule team must be active");
    }

    @Test
    void createSchedule_WithPastDateTime_ThrowsValidationException() {
        Team team = team(true);
        ClubField field = field(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(clubFieldRepository.findByUuid(field.getUuid())).thenReturn(Optional.of(field));

        assertThatThrownBy(() -> scheduleService.createSchedule(new ScheduleCreateRequest(
                team.getUuid(), field.getUuid(), LocalDateTime.now().minusMinutes(1), 60, ScheduleType.TRAINING, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule date and time must be in the future");
    }

    @Test
    void createSchedule_WithInvalidDuration_ThrowsValidationException() {
        Team team = team(true);
        ClubField field = field(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(clubFieldRepository.findByUuid(field.getUuid())).thenReturn(Optional.of(field));

        assertThatThrownBy(() -> scheduleService.createSchedule(new ScheduleCreateRequest(
                team.getUuid(), field.getUuid(), LocalDateTime.now().plusDays(1), 45, ScheduleType.TRAINING, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule duration must be 60, 90, or 120 minutes");
    }

    @Test
    void updateSchedule_WhenCanceled_ThrowsValidationException() {
        Schedule schedule = Schedule.builder()
                .team(team(true))
                .field(field(true))
                .dateTime(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .type(ScheduleType.TRAINING)
                .status(ScheduleStatus.CANCELED)
                .build();
        when(scheduleRepository.findByUuid(schedule.getUuid())).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> scheduleService.updateSchedule(
                schedule.getUuid(),
                new ScheduleUpdateRequest(null, null, LocalDateTime.now().plusDays(2), null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Canceled schedules cannot be changed");
    }

    private Team team(boolean active) {
        Team team = Team.builder()
                .ageGroup("Under 13 A")
                .ageCategory(TeamAgeCategory.U13)
                .teamCategory(TeamCategory.MASCULINE)
                .trainer(Trainer.builder()
                        .name("Carlos Mendes")
                        .registerDate(LocalDate.now())
                        .memberSince(LocalDate.now())
                        .build())
                .build();
        team.setActive(active);
        return team;
    }

    private ClubField field(boolean active) {
        return ClubField.builder()
                .name("Main Field")
                .location("Club")
                .active(active)
                .build();
    }
}
