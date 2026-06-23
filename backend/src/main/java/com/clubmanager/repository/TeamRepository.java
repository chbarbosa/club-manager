package com.clubmanager.repository;

import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    Optional<Team> findByUuid(UUID uuid);

    @Override
    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    Page<Team> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    Page<Team> findByAgeGroupContainingIgnoreCase(String ageGroup, Pageable pageable);

    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    Page<Team> findByTeamCategory(TeamCategory teamCategory, Pageable pageable);

    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    Page<Team> findByAgeGroupContainingIgnoreCaseAndTeamCategory(String ageGroup, TeamCategory teamCategory, Pageable pageable);

    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    List<Team> findByTrainerOrSubTrainerOrderByAgeGroupAsc(Trainer trainer, Trainer subTrainer);

    @EntityGraph(attributePaths = {"trainer", "subTrainer", "assistantAdmin"})
    @Query("""
            select t from Team t
            where (t.trainer = :trainer or t.subTrainer = :trainer)
              and (:ageGroup is null or lower(t.ageGroup) like lower(concat('%', :ageGroup, '%')))
              and (:teamCategory is null or t.teamCategory = :teamCategory)
            """)
    Page<Team> findAssignedToTrainer(
            @Param("trainer") Trainer trainer,
            @Param("ageGroup") String ageGroup,
            @Param("teamCategory") TeamCategory teamCategory,
            Pageable pageable);
}
