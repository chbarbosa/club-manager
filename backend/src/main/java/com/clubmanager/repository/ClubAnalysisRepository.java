package com.clubmanager.repository;

import com.clubmanager.domain.ClubAnalysis;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubAnalysisRepository extends JpaRepository<ClubAnalysis, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<ClubAnalysis> findByAnalysisDate(LocalDate analysisDate);

    @EntityGraph(attributePaths = "items")
    Optional<ClubAnalysis> findByUuid(UUID uuid);

    @Override
    @EntityGraph(attributePaths = "items")
    Page<ClubAnalysis> findAll(Pageable pageable);
}
