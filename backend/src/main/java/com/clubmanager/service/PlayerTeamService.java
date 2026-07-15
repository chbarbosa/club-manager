package com.clubmanager.service;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.dto.PlayerTeamAssignRequest;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerTeamService {

    private final PlayerTeamRepository playerTeamRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;



    @Transactional(readOnly = true)
    public List<PlayerTeam> getActiveRoster(UUID teamUuid) {
        Team team = getTeam(teamUuid);
        return playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team);
    }

    @Transactional(readOnly = true)
    public List<Player> getAvailablePlayers(UUID teamUuid) {
        Team team = getTeam(teamUuid);
        return playerRepository.findAllByOrderByNameAsc().stream()
                .filter(Player::isActive)
                .filter(player -> player.getTeamCategory() == team.getTeamCategory())
                .filter(player -> isWithinAgeLimit(team, player))
                .filter(player -> playerTeamRepository.findByPlayerAndActiveTrue(player).isEmpty())
                .toList();
    }

    @Transactional
    public PlayerTeam assignPlayer(UUID teamUuid, PlayerTeamAssignRequest request) {
        Team team = getTeam(teamUuid);
        Player player = getPlayer(request.playerUuid());

        validateAssignment(team, player, request.jerseyNumber());

        PlayerTeam assignment = PlayerTeam.builder()
                .team(team)
                .player(player)
                .jerseyNumber(request.jerseyNumber())
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

    private void validateAssignment(Team team, Player player, Integer jerseyNumber) {
        if (!team.isActive()) {
            throw new IllegalArgumentException("Cannot assign players to an inactive team");
        }
        if (!player.isActive()) {
            throw new IllegalArgumentException("Cannot assign an inactive player");
        }
        if (team.getTeamCategory() != player.getTeamCategory()) {
            throw new IllegalArgumentException("Player team category must match the team category");
        }
        validateAgeLimit(team, player);
        if (playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)) {
            throw new IllegalArgumentException("Player is already assigned to this team");
        }
        if (playerTeamRepository.existsByTeamAndJerseyNumberAndActiveTrue(team, jerseyNumber)) {
            throw new IllegalArgumentException("Jersey number is already assigned in this team");
        }
        playerTeamRepository.findByPlayerAndActiveTrue(player).ifPresent(existing -> {
            throw new IllegalArgumentException("Player is already assigned to an active team");
        });
    }

    private void validateAgeLimit(Team team, Player player) {
        Integer maxAge = maxAge(team.getAgeCategory());
        if (maxAge != null && playerAge(player) > maxAge) {
            throw new IllegalArgumentException("Player age must be " + maxAge + " or younger for this team");
        }
    }

    private boolean isWithinAgeLimit(Team team, Player player) {
        Integer maxAge = maxAge(team.getAgeCategory());
        return maxAge == null || playerAge(player) <= maxAge;
    }

    private int playerAge(Player player) {
        return Period.between(player.getBirthdate(), LocalDate.now()).getYears();
    }

    private Integer maxAge(TeamAgeCategory ageCategory) {
        return switch (ageCategory) {
            case U7 -> 7;
            case U8 -> 8;
            case U9 -> 9;
            case U10 -> 10;
            case U11 -> 11;
            case U12 -> 12;
            case U13 -> 13;
            case U14 -> 14;
            case U15 -> 15;
            case U16 -> 16;
            case U17_18 -> 18;
            case U19_PLUS -> null;
        };
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
