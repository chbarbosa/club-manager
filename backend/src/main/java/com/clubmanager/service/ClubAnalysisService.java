package com.clubmanager.service;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ClubAnalysis;
import com.clubmanager.domain.ClubAnalysisItem;
import com.clubmanager.domain.ClubAnalysisSeverity;
import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.ScheduleStatus;
import com.clubmanager.domain.ScheduleType;
import com.clubmanager.domain.Team;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ClubAnalysisRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.ScheduleRepository;
import com.clubmanager.repository.TeamMatchRepository;
import com.clubmanager.repository.TeamRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClubAnalysisService {

    private static final String PLAYER = "PLAYER";
    private static final String TEAM = "TEAM";
    private static final String CHAMPIONSHIP = "CHAMPIONSHIP";
    private static final String EVALUATION = "EVALUATION";

    private final ClubAnalysisRepository clubAnalysisRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final ScheduleRepository scheduleRepository;
    private final ChampionshipRepository championshipRepository;
    private final TeamMatchRepository teamMatchRepository;
    private final EvaluationRepository evaluationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ClubAnalysis getCurrentAnalysis() {
        LocalDate today = LocalDate.now();
        return clubAnalysisRepository.findByAnalysisDate(today)
                .orElseGet(() -> generate(today));
    }

    @Transactional(readOnly = true)
    public Page<ClubAnalysis> getAnalysisHistory(Pageable pageable) {
        return clubAnalysisRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ClubAnalysis getAnalysisByUuid(UUID uuid) {
        return clubAnalysisRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Club analysis not found: " + uuid));
    }

    private ClubAnalysis generate(LocalDate analysisDate) {
        ClubAnalysis analysis = ClubAnalysis.builder()
                .analysisDate(analysisDate)
                .generatedAt(LocalDateTime.now())
                .build();

        analyzePlayers(analysis);
        analyzeTeams(analysis);
        analyzeChampionships(analysis, analysisDate);
        analyzeEvaluations(analysis);

        return clubAnalysisRepository.save(analysis);
    }

    private void analyzePlayers(ClubAnalysis analysis) {
        List<Player> activePlayers = playerRepository.findAllByOrderByNameAsc().stream()
                .filter(Player::isActive)
                .toList();
        addItem(
                analysis,
                "PLAYER_TOTAL",
                ClubAnalysisSeverity.INFO,
                "Active players",
                "There are " + activePlayers.size() + " active players registered.",
                List.of());

        addItemWhenAffected(
                analysis,
                "PLAYERS_WITHOUT_SKILL_LEVEL",
                ClubAnalysisSeverity.WARNING,
                "Players without skill level",
                "Active players should have a current skill level defined.",
                activePlayers.stream()
                        .filter(player -> player.getCurrentSkillLevel() == null)
                        .map(player -> affected(PLAYER, player.getUuid(), player.getName()))
                        .toList());

        addItemWhenAffected(
                analysis,
                "PLAYERS_WITHOUT_POSITIONS",
                ClubAnalysisSeverity.CRITICAL,
                "Players without positions",
                "Active players should have at least one position.",
                activePlayers.stream()
                        .filter(player -> player.getPositions() == null || player.getPositions().isEmpty())
                        .map(player -> affected(PLAYER, player.getUuid(), player.getName()))
                        .toList());
    }

    private void analyzeTeams(ClubAnalysis analysis) {
        List<Team> activeTeams = teamRepository.findAll().stream()
                .filter(Team::isActive)
                .toList();

        addItemWhenAffected(
                analysis,
                "TEAM_TOO_FEW_PLAYERS",
                ClubAnalysisSeverity.WARNING,
                "Teams with fewer than 18 players",
                "Active teams should have at least 18 active players.",
                activeTeams.stream()
                        .filter(team -> activeRoster(team).size() < 18)
                        .map(team -> affected(TEAM, team.getUuid(), teamLabel(team)))
                        .toList());

        addItemWhenAffected(
                analysis,
                "TEAM_NO_GOALKEEPER",
                ClubAnalysisSeverity.CRITICAL,
                "Teams without goalkeepers",
                "Active teams should have at least one active goalkeeper.",
                activeTeams.stream()
                        .filter(team -> activeRoster(team).stream()
                                .noneMatch(assignment -> assignment.getPlayer().getPositions().contains(PlayerPosition.GOALKEEPER)))
                        .map(team -> affected(TEAM, team.getUuid(), teamLabel(team)))
                        .toList());

        addItemWhenAffected(
                analysis,
                "TEAM_MISSING_ASSISTANTS",
                ClubAnalysisSeverity.WARNING,
                "Teams missing assistants",
                "Active teams should have both an assistant trainer and an administrative assistant.",
                activeTeams.stream()
                        .filter(team -> team.getSubTrainer() == null || team.getAssistantAdmin() == null)
                        .map(team -> affected(TEAM, team.getUuid(), teamLabel(team)))
                        .toList());

        addItemWhenAffected(
                analysis,
                "TEAM_NO_TRAINING_SCHEDULE",
                ClubAnalysisSeverity.WARNING,
                "Teams without training schedule",
                "Active teams should have at least one non-canceled training schedule.",
                activeTeams.stream()
                        .filter(team -> !scheduleRepository.existsByTeamAndTypeAndStatus(
                                team, ScheduleType.TRAINING, ScheduleStatus.SCHEDULED))
                        .map(team -> affected(TEAM, team.getUuid(), teamLabel(team)))
                        .toList());
    }

    private void analyzeChampionships(ClubAnalysis analysis, LocalDate analysisDate) {
        YearMonth currentMonth = YearMonth.from(analysisDate);
        List<Championship> championships = championshipRepository.findAll();

        addItemWhenAffected(
                analysis,
                "CHAMPIONSHIP_NO_MATCH_AFTER_ONE_MONTH",
                ClubAnalysisSeverity.WARNING,
                "Championships without matches after one month",
                "Championships started more than one month ago should have registered matches.",
                championships.stream()
                        .filter(Championship::isActive)
                        .filter(championship -> YearMonth.of(championship.getStartYear(), championship.getStartMonth())
                                .plusMonths(1)
                                .isBefore(currentMonth))
                        .filter(championship -> teamMatchRepository.countByChampionship(championship) == 0)
                        .map(championship -> affected(CHAMPIONSHIP, championship.getUuid(), championship.getName()))
                        .toList());

        addItemWhenAffected(
                analysis,
                "CHAMPIONSHIP_EXPECTED_MATCHES_NOT_REACHED",
                ClubAnalysisSeverity.CRITICAL,
                "Ended championships below expected matches",
                "Ended championships should have at least the expected number of registered matches.",
                championships.stream()
                        .filter(championship -> YearMonth.of(championship.getEndYear(), championship.getEndMonth())
                                .isBefore(currentMonth))
                        .filter(championship -> teamMatchRepository.countByChampionship(championship) < championship.getExpectedMatches())
                        .map(championship -> affected(CHAMPIONSHIP, championship.getUuid(), championship.getName()))
                        .toList());
    }

    private void analyzeEvaluations(ClubAnalysis analysis) {
        List<Evaluation> notFinalized = evaluationRepository.findAll().stream()
                .filter(evaluation -> evaluation.getStatus() != EvaluationStatus.FINALIZED)
                .toList();
        addItemWhenAffected(
                analysis,
                "EVALUATIONS_NOT_FINALIZED",
                ClubAnalysisSeverity.WARNING,
                "Evaluations not finalized",
                notFinalized.size() + " evaluation" + (notFinalized.size() == 1 ? " is" : "s are") + " not finalized.",
                notFinalized.stream()
                        .map(evaluation -> affected(EVALUATION, evaluation.getUuid(), evaluation.getTitle()))
                        .toList());
    }

    private List<PlayerTeam> activeRoster(Team team) {
        return playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team);
    }

    private void addItemWhenAffected(
            ClubAnalysis analysis,
            String code,
            ClubAnalysisSeverity severity,
            String title,
            String message,
            List<AffectedRecord> affectedRecords) {
        if (!affectedRecords.isEmpty()) {
            addItem(analysis, code, severity, title, message, affectedRecords);
        }
    }

    private void addItem(
            ClubAnalysis analysis,
            String code,
            ClubAnalysisSeverity severity,
            String title,
            String message,
            List<AffectedRecord> affectedRecords) {
        analysis.addItem(ClubAnalysisItem.builder()
                .code(code)
                .severity(severity)
                .title(title)
                .message(message)
                .affectedRecords(toJson(affectedRecords))
                .build());
    }

    private AffectedRecord affected(String entityType, UUID uuid, String label) {
        return new AffectedRecord(entityType, uuid, label);
    }

    private String toJson(List<AffectedRecord> affectedRecords) {
        try {
            return objectMapper.writeValueAsString(affectedRecords);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize club analysis affected records", exception);
        }
    }

    private String teamLabel(Team team) {
        return team.getAgeGroup() + " " + team.getTeamCategory();
    }

    private record AffectedRecord(String entityType, UUID uuid, String label) {
    }
}
