package com.clubmanager.service;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.dto.PlayerTeamAssignRequest;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerTeamService {

    private final PlayerTeamRepository playerTeamRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public PlayerTeamService(
            PlayerTeamRepository playerTeamRepository,
            PlayerRepository playerRepository,
            TeamRepository teamRepository) {
        this.playerTeamRepository = playerTeamRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerTeam> getActiveRoster(UUID teamUuid) {
        Team team = getTeam(teamUuid);
        return playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team);
    }

    @Transactional
    public PlayerTeam assignPlayer(UUID teamUuid, PlayerTeamAssignRequest request) {
        Team team = getTeam(teamUuid);
        Player player = getPlayer(request.playerUuid());

        validateAssignment(team, player);

        PlayerTeam assignment = PlayerTeam.builder()
                .team(team)
                .player(player)
                .assignedDate(LocalDate.now())
                .build();
        return playerTeamRepository.save(assignment);
    }

    @Transactional
    public PlayerTeam removePlayer(UUID teamUuid, UUID assignmentUuid) {
        Team team = getTeam(teamUuid);
        PlayerTeam assignment = playerTeamRepository.findByUuid(assignmentUuid)
                .orElseThrow(() -> new EntityNotFoundException("Player team assignment not found: " + assignmentUuid));

        if (!assignment.getTeam().getUuid().equals(team.getUuid())) {
            throw new IllegalArgumentException("Assignment does not belong to this team");
        }
        if (!assignment.isActive()) {
            throw new IllegalArgumentException("Assignment is already inactive");
        }

        assignment.setActive(false);
        assignment.setRemovedDate(LocalDate.now());
        return playerTeamRepository.save(assignment);
    }

    private void validateAssignment(Team team, Player player) {
        if (!team.isActive()) {
            throw new IllegalArgumentException("Cannot assign players to an inactive team");
        }
        if (!player.isActive()) {
            throw new IllegalArgumentException("Cannot assign an inactive player");
        }
        if (team.getTeamCategory() != player.getTeamCategory()) {
            throw new IllegalArgumentException("Player team category must match the team category");
        }
        if (playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)) {
            throw new IllegalArgumentException("Player is already assigned to this team");
        }
        playerTeamRepository.findByPlayerAndActiveTrue(player).ifPresent(existing -> {
            throw new IllegalArgumentException("Player is already assigned to an active team");
        });
    }

    private Team getTeam(UUID uuid) {
        return teamRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + uuid));
    }

    private Player getPlayer(UUID uuid) {
        return playerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + uuid));
    }
}
