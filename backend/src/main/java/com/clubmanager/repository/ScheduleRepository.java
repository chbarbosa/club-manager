package com.clubmanager.repository;

import com.clubmanager.domain.Schedule;
import com.clubmanager.domain.ScheduleStatus;
import com.clubmanager.domain.ScheduleType;
import com.clubmanager.domain.Team;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @EntityGraph(attributePaths = {"team", "field", "team.trainer"})
    Optional<Schedule> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer"})
    Page<Schedule> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer"})
    Page<Schedule> findByTeam(Team team, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer"})
    Page<Schedule> findByStatus(ScheduleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer"})
    Page<Schedule> findByTeamAndStatus(Team team, ScheduleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer", "team.subTrainer"})
    Page<Schedule> findByTeamAndType(Team team, ScheduleType type, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer", "team.subTrainer"})
    Page<Schedule> findByTeamAndTypeAndStatus(Team team, ScheduleType type, ScheduleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer", "team.subTrainer"})
    @Query("""
            select s from Schedule s
            where s.type = :type
              and (s.team.trainer = :trainer or s.team.subTrainer = :trainer)
            """)
    Page<Schedule> findTrainingForTrainer(
            @Param("trainer") com.clubmanager.domain.Trainer trainer,
            @Param("type") ScheduleType type,
            Pageable pageable);

    @EntityGraph(attributePaths = {"team", "field", "team.trainer", "team.subTrainer"})
    @Query("""
            select s from Schedule s
            where s.type = :type
              and s.status = :status
              and (s.team.trainer = :trainer or s.team.subTrainer = :trainer)
            """)
    Page<Schedule> findTrainingForTrainerAndStatus(
            @Param("trainer") com.clubmanager.domain.Trainer trainer,
            @Param("type") ScheduleType type,
            @Param("status") ScheduleStatus status,
            Pageable pageable);

    boolean existsByTeamAndTypeAndStatus(Team team, ScheduleType type, ScheduleStatus status);

    boolean existsByFieldAndStatusAndDateTimeLessThanAndDateTimeGreaterThan(
            com.clubmanager.domain.ClubField field,
            ScheduleStatus status,
            LocalDateTime endsAfterNewStart,
            LocalDateTime startsBeforeNewEnd);
}
