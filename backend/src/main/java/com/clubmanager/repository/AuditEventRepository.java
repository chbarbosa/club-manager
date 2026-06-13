package com.clubmanager.repository;

import com.clubmanager.domain.AuditEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {

    @Override
    @EntityGraph(attributePaths = "actorAdmin")
    org.springframework.data.domain.Page<AuditEvent> findAll(
            org.springframework.data.jpa.domain.Specification<AuditEvent> specification,
            org.springframework.data.domain.Pageable pageable);
}
