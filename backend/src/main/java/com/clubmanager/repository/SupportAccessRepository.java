package com.clubmanager.repository;

import com.clubmanager.domain.SupportAccess;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportAccessRepository extends JpaRepository<SupportAccess, Long> {

    @EntityGraph(attributePaths = "createdByAdmin")
    Optional<SupportAccess> findByUuid(UUID uuid);

    Optional<SupportAccess> findFirstByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = "createdByAdmin")
    Page<SupportAccess> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
