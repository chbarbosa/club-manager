package com.clubmanager.repository;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.TeamCategory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    Optional<Evaluation> findByUuid(UUID uuid);

    Page<Evaluation> findByStatus(EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Evaluation> findByAgeGroupContainingIgnoreCase(String ageGroup, Pageable pageable);

    Page<Evaluation> findByTeamCategory(TeamCategory teamCategory, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCase(
            String title, String ageGroup, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndTeamCategory(
            String title, TeamCategory teamCategory, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndStatus(
            String title, EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByAgeGroupContainingIgnoreCaseAndTeamCategory(String ageGroup, TeamCategory teamCategory, Pageable pageable);

    Page<Evaluation> findByAgeGroupContainingIgnoreCaseAndStatus(String ageGroup, EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByTeamCategoryAndStatus(TeamCategory teamCategory, EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCaseAndTeamCategory(
            String title, String ageGroup, TeamCategory teamCategory, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCaseAndStatus(
            String title, String ageGroup, EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndTeamCategoryAndStatus(
            String title, TeamCategory teamCategory, EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByAgeGroupContainingIgnoreCaseAndTeamCategoryAndStatus(
            String ageGroup, TeamCategory teamCategory, EvaluationStatus status, Pageable pageable);

    Page<Evaluation> findByTitleContainingIgnoreCaseAndAgeGroupContainingIgnoreCaseAndTeamCategoryAndStatus(
            String title, String ageGroup, TeamCategory teamCategory, EvaluationStatus status, Pageable pageable);
}
