package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ChampionshipRoster;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.ChampionshipCreateRequest;
import com.clubmanager.dto.ChampionshipRosterAssignRequest;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ChampionshipRosterRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChampionshipServiceTest {

    @Mock
    private ChampionshipRepository championshipRepository;

    @Mock
    private ChampionshipRosterRepository championshipRosterRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    private ChampionshipService championshipService;

    @BeforeEach
    void setUp() {
        championshipService = new ChampionshipService(
                championshipRepository,
                championshipRosterRepository,
                teamRepository,
                playerRepository,
                trainerRepository,
                playerTeamRepository);
    }

    @Test
    void createChampionship_WithValidRequest_CreatesChampionship() {
        Team team = team(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(championshipRepository.save(any(Championship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Championship championship = championshipService.createChampionship(new ChampionshipCreateRequest(
                " City Cup ", " Spring tournament ", team.getUuid(), 4, 2026, 6, 2026));

        assertThat(championship.getName()).isEqualTo("City Cup");
        assertThat(championship.getDescription()).isEqualTo("Spring tournament");
        assertThat(championship.getTeam()).isEqualTo(team);
        assertThat(championship.getStartMonth()).isEqualTo(4);
        assertThat(championship.getEndMonth()).isEqualTo(6);
        assertThat(championship.isActive()).isTrue();
    }

    @Test
    void createChampionship_WithInactiveTeam_ThrowsValidationException() {
        Team team = team(false);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> championshipService.createChampionship(new ChampionshipCreateRequest(
                "City Cup", null, team.getUuid(), 4, 2026, 6, 2026)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Championship team must be active");
    }

    @Test
    void createChampionship_WithEndBeforeStart_ThrowsValidationException() {
        Team team = team(true);

        assertThatThrownBy(() -> championshipService.createChampionship(new ChampionshipCreateRequest(
                "City Cup", null, team.getUuid(), 9, 2026, 6, 2026)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Championship end period must be after the start period");
    }

    @Test
    void assignRosterPlayer_WithEligiblePlayer_CreatesRosterEntry() {
        Championship championship = championship(team(true));
        Player player = player(true);
        Trainer trainer = trainer(true);
        when(championshipRepository.findByUuid(championship.getUuid())).thenReturn(Optional.of(championship));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(championship.getTeam(), player)).thenReturn(true);
        when(championshipRosterRepository.existsByChampionshipAndPlayerAndActiveTrue(championship, player)).thenReturn(false);
        when(championshipRosterRepository.save(any(ChampionshipRoster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChampionshipRoster roster = championshipService.assignRosterPlayer(
                championship.getUuid(),
                new ChampionshipRosterAssignRequest(player.getUuid(), trainer.getUuid()));

        assertThat(roster.getChampionship()).isEqualTo(championship);
        assertThat(roster.getPlayer()).isEqualTo(player);
        assertThat(roster.getTrainer()).isEqualTo(trainer);
        assertThat(roster.getAssignedDate()).isEqualTo(LocalDate.now());
        assertThat(roster.isActive()).isTrue();
    }

    @Test
    void assignRosterPlayer_WhenPlayerNotOnTeamRoster_ThrowsValidationException() {
        Championship championship = championship(team(true));
        Player player = player(true);
        Trainer trainer = trainer(true);
        when(championshipRepository.findByUuid(championship.getUuid())).thenReturn(Optional.of(championship));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(championship.getTeam(), player)).thenReturn(false);

        assertThatThrownBy(() -> championshipService.assignRosterPlayer(
                championship.getUuid(),
                new ChampionshipRosterAssignRequest(player.getUuid(), trainer.getUuid())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player must be assigned to the championship team roster");
    }

    private Championship championship(Team team) {
        return Championship.builder()
                .name("City Cup")
                .team(team)
                .startMonth(4)
                .startYear(2026)
                .endMonth(6)
                .endYear(2026)
                .build();
    }

    private Team team(boolean active) {
        Team team = Team.builder()
                .ageGroup("Under 13 A")
                .ageCategory(TeamAgeCategory.U13)
                .teamCategory(TeamCategory.MASCULINE)
                .trainer(trainer(true))
                .build();
        team.setActive(active);
        return team;
    }

    private Trainer trainer(boolean active) {
        Trainer trainer = Trainer.builder()
                .name("Carlos Mendes")
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
        trainer.setActive(active);
        return trainer;
    }

    private Player player(boolean active) {
        Player player = Player.builder()
                .name("Joao Silva")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(13))
                .teamCategory(TeamCategory.MASCULINE)
                .positions(Set.of(PlayerPosition.MIDFIELD))
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now())
                .build();
        player.setActive(active);
        return player;
    }
}
