package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ClubSetup;
import com.clubmanager.domain.MatchPlayerAnalysis;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.TeamMatch;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.MatchPlayerAnalysisUpdateRequest;
import com.clubmanager.dto.TeamMatchCreateRequest;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ClubSetupRepository;
import com.clubmanager.repository.MatchPlayerAnalysisRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamMatchRepository;
import com.clubmanager.repository.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamMatchServiceTest {

    @Mock
    private TeamMatchRepository teamMatchRepository;
    @Mock
    private MatchPlayerAnalysisRepository matchPlayerAnalysisRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ChampionshipRepository championshipRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerTeamRepository playerTeamRepository;
    @Mock
    private ClubSetupRepository clubSetupRepository;

    private TeamMatchService teamMatchService;

    @BeforeEach
    void setUp() {
        teamMatchService = new TeamMatchService(
                teamMatchRepository,
                matchPlayerAnalysisRepository,
                teamRepository,
                championshipRepository,
                playerRepository,
                playerTeamRepository,
                clubSetupRepository,
                new ObjectMapper());
    }

    @Test
    void createMatch_WithValidRequest_CreatesMatch() {
        Team team = team(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamMatchRepository.save(any(TeamMatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamMatch match = teamMatchService.createMatch(team.getUuid(), createRequest(null));

        assertThat(match.getTeam()).isEqualTo(team);
        assertThat(match.getOpponent()).isEqualTo("Rivals FC");
        assertThat(match.getPlace()).isEqualTo("Main Field");
        assertThat(match.getTeamScore()).isEqualTo(2);
        assertThat(match.getOpponentScore()).isEqualTo(1);
    }

    @Test
    void createMatch_WithInactiveTeam_ThrowsValidationException() {
        Team team = team(false);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));

        assertThatThrownBy(() -> teamMatchService.createMatch(team.getUuid(), createRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot create matches for an inactive team");
    }

    @Test
    void createMatch_WithChampionshipFromAnotherTeam_ThrowsValidationException() {
        Team team = team(true);
        Championship championship = championship(team(true));
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(championshipRepository.findByUuid(championship.getUuid())).thenReturn(Optional.of(championship));

        assertThatThrownBy(() -> teamMatchService.createMatch(team.getUuid(), createRequest(championship.getUuid())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Championship must belong to the match team");
    }

    @Test
    void createMatch_WithNegativeScore_ThrowsValidationException() {
        assertThatThrownBy(() -> teamMatchService.createMatch(UUID.randomUUID(), new TeamMatchCreateRequest(
                null, "Rivals FC", "Main Field", LocalDateTime.now(), -1, 1, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match scores cannot be negative");
    }

    @Test
    void savePlayerAnalysis_WhenPlayerNotOnTeam_ThrowsValidationException() {
        Team team = team(true);
        TeamMatch match = match(team);
        Player player = player(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamMatchRepository.findByUuid(match.getUuid())).thenReturn(Optional.of(match));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)).thenReturn(false);

        assertThatThrownBy(() -> teamMatchService.savePlayerAnalysis(
                team.getUuid(), match.getUuid(), player.getUuid(), analysisRequest("Improve pass")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player must be assigned to the match team");
    }

    @Test
    void savePlayerAnalysis_WithUnknownTag_ThrowsValidationException() {
        Team team = team(true);
        TeamMatch match = match(team);
        Player player = player(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamMatchRepository.findByUuid(match.getUuid())).thenReturn(Optional.of(match));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)).thenReturn(true);
        when(clubSetupRepository.findByType(TeamMatchService.IMPROVEMENT_SETUP_TYPE))
                .thenReturn(Optional.of(setup(TeamMatchService.IMPROVEMENT_SETUP_TYPE, "[\"Improve pass\"]")));

        assertThatThrownBy(() -> teamMatchService.savePlayerAnalysis(
                team.getUuid(), match.getUuid(), player.getUuid(), analysisRequest("Unknown tag")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Improvement opportunity tag is not configured: Unknown tag");
    }

    @Test
    void savePlayerAnalysis_WithValidTags_SavesAnalysis() {
        Team team = team(true);
        TeamMatch match = match(team);
        Player player = player(true);
        when(teamRepository.findByUuid(team.getUuid())).thenReturn(Optional.of(team));
        when(teamMatchRepository.findByUuid(match.getUuid())).thenReturn(Optional.of(match));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)).thenReturn(true);
        when(matchPlayerAnalysisRepository.findByMatchAndPlayer(match, player)).thenReturn(Optional.empty());
        when(clubSetupRepository.findByType(TeamMatchService.IMPROVEMENT_SETUP_TYPE))
                .thenReturn(Optional.of(setup(TeamMatchService.IMPROVEMENT_SETUP_TYPE, "[\"Improve pass\"]")));
        when(clubSetupRepository.findByType(TeamMatchService.HIGHLIGHT_SETUP_TYPE))
                .thenReturn(Optional.of(setup(TeamMatchService.HIGHLIGHT_SETUP_TYPE, "[\"Good passes\"]")));
        when(matchPlayerAnalysisRepository.save(any(MatchPlayerAnalysis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MatchPlayerAnalysis analysis = teamMatchService.savePlayerAnalysis(
                team.getUuid(),
                match.getUuid(),
                player.getUuid(),
                new MatchPlayerAnalysisUpdateRequest(List.of("Improve pass"), List.of("Good passes"), "Solid game"));

        assertThat(analysis.getImprovementTags()).isEqualTo("[\"Improve pass\"]");
        assertThat(analysis.getHighlightTags()).isEqualTo("[\"Good passes\"]");
        assertThat(analysis.getNotes()).isEqualTo("Solid game");
    }

    private TeamMatchCreateRequest createRequest(java.util.UUID championshipUuid) {
        return new TeamMatchCreateRequest(
                championshipUuid,
                " Rivals FC ",
                " Main Field ",
                LocalDateTime.now(),
                2,
                1,
                " Good rhythm ");
    }

    private MatchPlayerAnalysisUpdateRequest analysisRequest(String tag) {
        return new MatchPlayerAnalysisUpdateRequest(List.of(tag), List.of(), null);
    }

    private TeamMatch match(Team team) {
        return TeamMatch.builder()
                .team(team)
                .opponent("Rivals FC")
                .place("Main Field")
                .matchDateTime(LocalDateTime.now())
                .build();
    }

    private Championship championship(Team team) {
        return Championship.builder()
                .name("Spring Cup")
                .team(team)
                .startMonth(3)
                .startYear(2026)
                .endMonth(6)
                .endYear(2026)
                .build();
    }

    private ClubSetup setup(String type, String jsonData) {
        ClubSetup setup = new ClubSetup();
        setup.setType(type);
        setup.setJsonData(jsonData);
        return setup;
    }

    private Team team(boolean active) {
        Team team = Team.builder()
                .ageGroup("Under 13 A")
                .ageCategory(TeamAgeCategory.U13)
                .teamCategory(TeamCategory.MASCULINE)
                .trainer(Trainer.builder()
                        .name("Carlos Mendes")
                        .registerDate(LocalDate.now())
                        .memberSince(LocalDate.now())
                        .build())
                .build();
        team.setActive(active);
        return team;
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
