package com.clubmanager.repository;

import com.clubmanager.domain.SupportAccess;
import com.clubmanager.domain.SupportAccessViewEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportAccessViewEventRepository extends JpaRepository<SupportAccessViewEvent, Long> {

    Page<SupportAccessViewEvent> findAllBySupportAccessOrderByOccurredAtDesc(SupportAccess supportAccess, Pageable pageable);
}
