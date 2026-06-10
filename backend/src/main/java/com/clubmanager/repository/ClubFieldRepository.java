package com.clubmanager.repository;

import com.clubmanager.domain.ClubField;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubFieldRepository extends JpaRepository<ClubField, Long> {

    Optional<ClubField> findByUuid(UUID uuid);

    List<ClubField> findByActiveTrueOrderByNameAsc();
}
