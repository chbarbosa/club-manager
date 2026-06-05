package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.PlayerTeamAssignRequest;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlayerTeamServiceTest {

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    private PlayerTeamService playerTeamService;

    @BeforeEach
    void setUp() {
        playerTeamService = new PlayerTeamService(playerTeamRepository, playerRepository, teamRepository);
    }

    @Test
    void assignPlayer_WithValidRequest_CreatesActiveAssignment() {
        Team team = team(TeamCategory.MASCULINE);
        Player player = player(TeamCategory.MASCULINE);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)).thenReturn(false);
        when(playerTeamRepository.findByPlayerAndActiveTrue(player)).thenReturn(Optional.empty());
        when(playerTeamRepository.save(any(PlayerTeam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlayerTeam assignment = playerTeamService.assignPlayer(team.getUuid(), new PlayerTeamAssignRequest(player.getUuid()));

        assertThat(assignment.getTeam()).isEqualTo(team);
        assertThat(assignment.getPlayer()).isEqualTo(player);
        assertThat(assignment.getAssignedDate()).isEqualTo(LocalDate.now());
        assertThat(assignment.isActive()).isTrue();
    }

    @Test
    void assignPlayer_WithMismatchedTeamCategory_ThrowsValidationException() {
        Team team = team(TeamCategory.FEMININE);
        Player player = player(TeamCategory.MASCULINE);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> playerTeamService.assignPlayer(team.getUuid(), new PlayerTeamAssignRequest(player.getUuid())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("category");
    }

    @Test
    void removePlayer_WithActiveAssignment_DeactivatesAssignment() {
        Team team = team(TeamCategory.MASCULINE);
        Player player = player(TeamCategory.MASCULINE);
        PlayerTeam assignment = PlayerTeam.builder()
                .team(team)
                .player(player)
                .assignedDate(LocalDate.now().minusDays(1))
                .build();
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(playerTeamRepository.findByUuid(assignment.getUuid())).thenReturn(Optional.of(assignment));
        when(playerTeamRepository.save(assignment)).thenReturn(assignment);

        PlayerTeam removed = playerTeamService.removePlayer(team.getUuid(), assignment.getUuid());

        assertThat(removed.isActive()).isFalse();
        assertThat(removed.getRemovedDate()).isEqualTo(LocalDate.now());
        verify(playerTeamRepository).save(assignment);
    }

    private Team team(TeamCategory teamCategory) {
        return Team.builder()
                .ageGroup("Under 13")
                .teamCategory(teamCategory)
                .trainer(Trainer.builder()
                        .name("Carlos Mendes")
                        .registerDate(LocalDate.now())
                        .memberSince(LocalDate.now())
                        .build())
                .build();
    }

    private Player player(TeamCategory teamCategory) {
        return Player.builder()
                .name("Joao Silva")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(12))
                .teamCategory(teamCategory)
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
    }
}
