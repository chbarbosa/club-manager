package com.clubmanager.repository;

import com.clubmanager.domain.Trainer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUuid(UUID uuid);

    Optional<Trainer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<Trainer> findAllByActiveTrue(Pageable pageable);

    Page<Trainer> findAllByActiveFalse(Pageable pageable);

    Page<Trainer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Trainer> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<Trainer> findByNameContainingIgnoreCaseAndActiveFalse(String name, Pageable pageable);
}
