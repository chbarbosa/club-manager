package com.clubmanager.repository;

import com.clubmanager.domain.Admin;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUuid(UUID uuid);

    List<Admin> findByActiveTrue();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByActiveTrue();
}
