package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TeamCreateRequest;
import com.clubmanager.dto.TeamUpdateRequest;
import com.clubmanager.repository.AdminRepository;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private AdminRepository adminRepository;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, trainerRepository, adminRepository);
    }

    @Test
    void createTeam_WithValidRequest_ReturnsActiveTeam() {
        Trainer trainer = trainer();
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Team team = teamService.createTeam(new TeamCreateRequest(
                "Under 13 A", TeamAgeCategory.U13, TeamCategory.MASCULINE, trainer.getUuid(), null, null));

        assertThat(team.getAgeGroup()).isEqualTo("Under 13 A");
        assertThat(team.getAgeCategory()).isEqualTo(TeamAgeCategory.U13);
        assertThat(team.getTeamCategory()).isEqualTo(TeamCategory.MASCULINE);
        assertThat(team.getTrainer()).isEqualTo(trainer);
        assertThat(team.isActive()).isTrue();
    }

    @Test
    void createTeam_WithBlankAgeGroup_ThrowsValidationException() {
        Trainer trainer = trainer();
        TeamCreateRequest request = new TeamCreateRequest(" ", TeamAgeCategory.U13, TeamCategory.MASCULINE, trainer.getUuid(), null, null);

        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identification");
    }

    @Test
    void createTeam_WithUnknownTrainer_ThrowsNotFoundException() {
        Trainer trainer = trainer();
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest(
                "Under 13 A", TeamAgeCategory.U13, TeamCategory.MASCULINE, trainer.getUuid(), null, null)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found");
    }

    @Test
    void createTeam_WithInactiveTrainer_ThrowsValidationException() {
        Trainer trainer = trainer();
        trainer.setActive(false);
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest(
                "Under 13 A", TeamAgeCategory.U13, TeamCategory.MASCULINE, trainer.getUuid(), null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer must be active");
    }

    @Test
    void createTeam_WithInactiveAssistantAdmin_ThrowsValidationException() {
        Trainer trainer = trainer();
        Admin admin = new Admin();
        admin.setActive(false);
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(adminRepository.findByUuid(admin.getUuid())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> teamService.createTeam(new TeamCreateRequest(
                "Under 13 A", TeamAgeCategory.U13, TeamCategory.MASCULINE, trainer.getUuid(), null, admin.getUuid())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Administrative assistant must be active");
    }

    @Test
    void updateTeam_WithValidRequest_UpdatesFields() {
        Team team = team();
        Trainer newTrainer = trainer();
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(trainerRepository.findByUuid(newTrainer.getUuid())).thenReturn(Optional.of(newTrainer));
        when(teamRepository.save(team)).thenReturn(team);

        Team updated = teamService.updateTeam(
                team.getUuid(),
                new TeamUpdateRequest("Under 15 A", TeamAgeCategory.U15, TeamCategory.FEMININE, newTrainer.getUuid(), null, null));

        assertThat(updated.getAgeGroup()).isEqualTo("Under 15 A");
        assertThat(updated.getAgeCategory()).isEqualTo(TeamAgeCategory.U15);
        assertThat(updated.getTeamCategory()).isEqualTo(TeamCategory.FEMININE);
        assertThat(updated.getTrainer()).isEqualTo(newTrainer);
    }

    @Test
    void updateTeam_WithInactiveSubTrainer_ThrowsValidationException() {
        Team team = team();
        Trainer subTrainer = trainer();
        subTrainer.setActive(false);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(trainerRepository.findByUuid(subTrainer.getUuid())).thenReturn(Optional.of(subTrainer));

        assertThatThrownBy(() -> teamService.updateTeam(
                team.getUuid(),
                new TeamUpdateRequest(null, null, null, null, subTrainer.getUuid(), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Trainer must be active");
    }

    @Test
    void updateTeam_WithNullFields_KeepsExistingValues() {
        Team team = team();
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);

        Team updated = teamService.updateTeam(team.getUuid(), new TeamUpdateRequest(null, null, null, null, null, null));

        assertThat(updated.getAgeGroup()).isEqualTo("Under 13");
        assertThat(updated.getTeamCategory()).isEqualTo(TeamCategory.MASCULINE);
        assertThat(updated.getTrainer().getName()).isEqualTo("Carlos Mendes");
    }

    @Test
    void deactivateTeam_WhenActive_SetsActiveFalse() {
        Team team = team();
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);

        Team updated = teamService.deactivateTeam(team.getUuid());

        assertThat(updated.isActive()).isFalse();
        verify(teamRepository).save(team);
    }

    @Test
    void reactivateTeam_WhenInactive_SetsActiveTrue() {
        Team team = team();
        team.setActive(false);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);

        Team updated = teamService.reactivateTeam(team.getUuid());

        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void searchTeams_WithNoFilters_ReturnsAllTeams() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Team> page = new PageImpl<>(List.of(team()), pageable, 1);
        when(teamRepository.findAll(pageable)).thenReturn(page);

        Page<Team> result = teamService.searchTeams(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getAgeGroup()).isEqualTo("Under 13");
    }

    private Team team() {
        return Team.builder()
                .ageGroup("Under 13")
                .ageCategory(TeamAgeCategory.U13)
                .teamCategory(TeamCategory.MASCULINE)
                .trainer(trainer())
                .build();
    }

    private Trainer trainer() {
        return Trainer.builder()
                .name("Carlos Mendes")
                .birthdate(LocalDate.now().minusYears(35))
                .registerDate(LocalDate.now().minusDays(10))
                .memberSince(LocalDate.now().minusYears(5))
                .build();
    }
}
