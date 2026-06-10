package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.ChampionshipCreateRequest;
import com.clubmanager.dto.ChampionshipUpdateRequest;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.TeamRepository;
import java.time.LocalDate;
import java.util.Optional;
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
    private TeamRepository teamRepository;

    private ChampionshipService championshipService;

    @BeforeEach
    void setUp() {
        championshipService = new ChampionshipService(
                championshipRepository,
                teamRepository);
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
    void updateChampionship_WithValidPeriod_UpdatesAllowedFields() {
        Championship championship = championship(team(true));
        when(championshipRepository.findByUuid(championship.getUuid())).thenReturn(Optional.of(championship));
        when(championshipRepository.save(any(Championship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Championship updated = championshipService.updateChampionship(
                championship.getUuid(),
                new ChampionshipUpdateRequest(" Autumn Cup ", " Fall season ", null, 8, 2026, 11, 2026));

        assertThat(updated.getName()).isEqualTo("Autumn Cup");
        assertThat(updated.getDescription()).isEqualTo("Fall season");
        assertThat(updated.getStartMonth()).isEqualTo(8);
        assertThat(updated.getEndMonth()).isEqualTo(11);
    }

    @Test
    void updateChampionship_WhenInactive_ThrowsValidationException() {
        Championship championship = championship(team(true));
        championship.setActive(false);
        when(championshipRepository.findByUuid(championship.getUuid())).thenReturn(Optional.of(championship));

        assertThatThrownBy(() -> championshipService.updateChampionship(
                championship.getUuid(),
                new ChampionshipUpdateRequest("Autumn Cup", null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inactive championships cannot be changed");
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

}
