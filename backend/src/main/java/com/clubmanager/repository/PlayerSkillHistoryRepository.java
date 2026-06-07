package com.clubmanager.repository;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerSkillHistory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerSkillHistoryRepository extends JpaRepository<PlayerSkillHistory, Long> {

    @EntityGraph(attributePaths = {"player", "changedByAdmin", "evaluationEvent"})
    List<PlayerSkillHistory> findByPlayerOrderByChangedAtDesc(Player player);
}
