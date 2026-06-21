package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ClubAnalysis;
import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ClubAnalysisRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.ScheduleRepository;
import com.clubmanager.repository.TeamMatchRepository;
import com.clubmanager.repository.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubAnalysisServiceTest {

    @Mock
    private ClubAnalysisRepository clubAnalysisRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PlayerTeamRepository playerTeamRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ChampionshipRepository championshipRepository;
    @Mock
    private TeamMatchRepository teamMatchRepository;
    @Mock
    private EvaluationRepository evaluationRepository;

    private ClubAnalysisService clubAnalysisService;

    @BeforeEach
    void setUp() {
        clubAnalysisService = new ClubAnalysisService(
                clubAnalysisRepository,
                playerRepository,
                teamRepository,
                playerTeamRepository,
                scheduleRepository,
                championshipRepository,
                teamMatchRepository,
                evaluationRepository,
                new ObjectMapper());
    }

    @Test
    void getCurrentAnalysis_WhenTodayExists_ReusesExistingAnalysis() {
        ClubAnalysis existing = ClubAnalysis.builder()
                .analysisDate(LocalDate.now())
                .generatedAt(LocalDateTime.now().minusHours(1))
                .build();
        when(clubAnalysisRepository.findByAnalysisDate(LocalDate.now())).thenReturn(Optional.of(existing));

        ClubAnalysis result = clubAnalysisService.getCurrentAnalysis();

        assertThat(result).isSameAs(existing);
        verify(clubAnalysisRepository, never()).save(any(ClubAnalysis.class));
    }

    @Test
    void getCurrentAnalysis_WhenMissing_GeneratesAllConfiguredFindings() {
        Player player = Player.builder()
                .name("No Skill Player")
                .build();
        Team team = Team.builder()
                .ageGroup("Under 13")
                .teamCategory(TeamCategory.MASCULINE)
                .build();
        Player goalkeeper = Player.builder()
                .name("Goalkeeper")
                .positions(Set.of(PlayerPosition.GOALKEEPER))
                .build();
        PlayerTeam assignment = PlayerTeam.builder()
                .team(team)
                .player(goalkeeper)
                .build();
        Championship championship = Championship.builder()
                .name("Old Cup")
                .team(team)
                .startMonth(1)
                .startYear(2020)
                .endMonth(2)
                .endYear(2020)
                .expectedMatches(3)
                .build();
        Evaluation evaluation = Evaluation.builder()
                .title("Open Evaluation")
                .status(EvaluationStatus.OPEN)
                .build();

        when(clubAnalysisRepository.findByAnalysisDate(LocalDate.now())).thenReturn(Optional.empty());
        when(playerRepository.findAllByOrderByNameAsc()).thenReturn(List.of(player));
        when(teamRepository.findAll()).thenReturn(List.of(team));
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team)).thenReturn(List.of(assignment));
        when(championshipRepository.findAll()).thenReturn(List.of(championship));
        when(teamMatchRepository.countByChampionship(championship)).thenReturn(0L);
        when(evaluationRepository.findAll()).thenReturn(List.of(evaluation));
        when(clubAnalysisRepository.save(any(ClubAnalysis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubAnalysis result = clubAnalysisService.getCurrentAnalysis();

        assertThat(result.getAnalysisDate()).isEqualTo(LocalDate.now());
        assertThat(result.getItems())
                .extracting("code")
                .contains(
                        "PLAYER_TOTAL",
                        "PLAYERS_WITHOUT_SKILL_LEVEL",
                        "PLAYERS_WITHOUT_POSITIONS",
                        "TEAM_TOO_FEW_PLAYERS",
                        "TEAM_MISSING_ASSISTANTS",
                        "TEAM_NO_TRAINING_SCHEDULE",
                        "CHAMPIONSHIP_NO_MATCH_AFTER_ONE_MONTH",
                        "CHAMPIONSHIP_EXPECTED_MATCHES_NOT_REACHED",
                        "EVALUATIONS_NOT_FINALIZED");
        assertThat(result.getItems())
                .filteredOn(item -> item.getCode().equals("PLAYERS_WITHOUT_SKILL_LEVEL"))
                .first()
                .extracting("affectedRecords")
                .asString()
                .contains("No Skill Player");
    }
}
