package com.clubmanager.service;

import com.clubmanager.domain.ClubField;
import com.clubmanager.domain.Schedule;
import com.clubmanager.domain.ScheduleStatus;
import com.clubmanager.domain.ScheduleType;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.ScheduleCancelRequest;
import com.clubmanager.dto.ScheduleCreateRequest;
import com.clubmanager.dto.ScheduleUpdateRequest;
import com.clubmanager.repository.ClubFieldRepository;
import com.clubmanager.repository.ScheduleRepository;
import com.clubmanager.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final Set<Integer> ALLOWED_DURATIONS = Set.of(60, 90, 120);

    private final ScheduleRepository scheduleRepository;
    private final TeamRepository teamRepository;
    private final ClubFieldRepository clubFieldRepository;



    @Transactional
    public Schedule createSchedule(ScheduleCreateRequest request) {
        Team team = getActiveTeam(request.teamUuid());
        ClubField field = getActiveField(request.fieldUuid());
        validateDateTime(request.dateTime());
        validateDuration(request.durationMinutes());

        Schedule schedule = Schedule.builder()
                .team(team)
                .field(field)
                .dateTime(request.dateTime())
                .durationMinutes(request.durationMinutes())
                .type(request.type())
                .notes(cleanOptionalText(request.notes()))
                .build();
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Page<Schedule> searchSchedules(UUID teamUuid, ScheduleStatus status, Pageable pageable) {
        if (teamUuid != null && status != null) {
            return scheduleRepository.findByTeamAndStatus(getTeam(teamUuid), status, pageable);
        }
        if (teamUuid != null) {
            return scheduleRepository.findByTeam(getTeam(teamUuid), pageable);
        }
        if (status != null) {
            return scheduleRepository.findByStatus(status, pageable);
        }
        return scheduleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Schedule> searchTrainingSchedulesForTrainer(
            Trainer trainer,
            UUID teamUuid,
            ScheduleStatus status,
            Pageable pageable) {
        if (teamUuid != null) {
            Team team = getTeam(teamUuid);
            if (!isTrainerAssignedToTeam(trainer, team)) {
                throw new org.springframework.security.access.AccessDeniedException("Trainer is not assigned to this team");
            }
            if (status != null) {
                return scheduleRepository.findByTeamAndTypeAndStatus(team, ScheduleType.TRAINING, status, pageable);
            }
            return scheduleRepository.findByTeamAndType(team, ScheduleType.TRAINING, pageable);
        }
        if (status != null) {
            return scheduleRepository.findTrainingForTrainerAndStatus(trainer, ScheduleType.TRAINING, status, pageable);
        }
        return scheduleRepository.findTrainingForTrainer(trainer, ScheduleType.TRAINING, pageable);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleByUuid(UUID uuid) {
        return scheduleRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + uuid));
    }

    @Transactional
    public Schedule updateSchedule(UUID uuid, ScheduleUpdateRequest request) {
        Schedule schedule = getScheduleByUuid(uuid);
        ensureScheduled(schedule);

        if (request.teamUuid() != null) {
            schedule.setTeam(getActiveTeam(request.teamUuid()));
        }
        if (request.fieldUuid() != null) {
            schedule.setField(getActiveField(request.fieldUuid()));
        }
        if (request.dateTime() != null) {
            validateDateTime(request.dateTime());
            schedule.setDateTime(request.dateTime());
        }
        if (request.durationMinutes() != null) {
            validateDuration(request.durationMinutes());
            schedule.setDurationMinutes(request.durationMinutes());
        }
        if (request.type() != null) {
            schedule.setType(request.type());
        }
        if (request.notes() != null) {
            schedule.setNotes(cleanOptionalText(request.notes()));
        }

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule cancelSchedule(UUID uuid, ScheduleCancelRequest request) {
        Schedule schedule = getScheduleByUuid(uuid);
        ensureScheduled(schedule);
        schedule.setStatus(ScheduleStatus.CANCELED);
        schedule.setCancelReason(cleanOptionalText(request.cancelReason()));
        return scheduleRepository.save(schedule);
    }

    private Team getTeam(UUID uuid) {
        return teamRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + uuid));
    }

    private Team getActiveTeam(UUID uuid) {
        Team team = getTeam(uuid);
        if (!team.isActive()) {
            throw new IllegalArgumentException("Schedule team must be active");
        }
        return team;
    }

    private ClubField getActiveField(UUID uuid) {
        ClubField field = clubFieldRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Field not found: " + uuid));
        if (!field.isActive()) {
            throw new IllegalArgumentException("Schedule field must be active");
        }
        return field;
    }

    private void validateDateTime(LocalDateTime dateTime) {
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Schedule date and time must be in the future");
        }
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes == null || !ALLOWED_DURATIONS.contains(durationMinutes)) {
            throw new IllegalArgumentException("Schedule duration must be 60, 90, or 120 minutes");
        }
    }

    private void ensureScheduled(Schedule schedule) {
        if (schedule.getStatus() != ScheduleStatus.SCHEDULED) {
            throw new IllegalArgumentException("Canceled schedules cannot be changed");
        }
    }

    private String cleanOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isTrainerAssignedToTeam(Trainer trainer, Team team) {
        return sameTrainer(trainer, team.getTrainer()) || sameTrainer(trainer, team.getSubTrainer());
    }

    private boolean sameTrainer(Trainer currentTrainer, Trainer teamTrainer) {
        return teamTrainer != null && teamTrainer.getUuid().equals(currentTrainer.getUuid());
    }
}
