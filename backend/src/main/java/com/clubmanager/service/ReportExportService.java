package com.clubmanager.service;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.MatchPlayerAnalysis;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Schedule;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamMatch;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.EvaluationPlayerRepository;
import com.clubmanager.repository.EvaluationRepository;
import com.clubmanager.repository.EvaluationResultRepository;
import com.clubmanager.repository.MatchPlayerAnalysisRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.ScheduleRepository;
import com.clubmanager.repository.TeamMatchRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final TeamService teamService;
    private final ScheduleRepository scheduleRepository;
    private final ChampionshipRepository championshipRepository;
    private final EvaluationRepository evaluationRepository;
    private final EvaluationPlayerRepository evaluationPlayerRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final TeamMatchRepository teamMatchRepository;
    private final MatchPlayerAnalysisRepository matchPlayerAnalysisRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public String exportPlayersCsv() {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of(
                "Name",
                "Age",
                "Team Category",
                "Skill Level",
                "Positions",
                "Registration Number",
                "Member Since",
                "Status"));

        playerRepository.findAllByOrderByNameAsc().forEach(player -> appendRow(csv, List.of(
                player.getName(),
                String.valueOf(calculateAge(player.getBirthdate())),
                display(player.getTeamCategory()),
                display(player.getCurrentSkillLevel()),
                joinPositions(player.getPositions()),
                valueOrBlank(player.getRegistrationNumber()),
                formatDate(player.getMemberSince()),
                player.isActive() ? "Active" : "Inactive")));
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportTeamRosterCsv(UUID teamUuid) {
        Team team = teamService.getTeamByUuid(teamUuid);
        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of(
                "Team",
                "Number",
                "Player",
                "Age",
                "Team Category",
                "Skill Level",
                "Positions",
                "Assigned Date"));

        playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team).forEach(assignment -> {
            Player player = assignment.getPlayer();
            appendRow(csv, List.of(
                    teamLabel(team),
                    assignment.getJerseyNumber() == null ? "" : String.valueOf(assignment.getJerseyNumber()),
                    player.getName(),
                    String.valueOf(calculateAge(player.getBirthdate())),
                    display(player.getTeamCategory()),
                    display(player.getCurrentSkillLevel()),
                    joinPositions(player.getPositions()),
                    formatDate(assignment.getAssignedDate())));
        });
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportSchedulesCsv() {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of(
                "Team",
                "Field",
                "Date Time",
                "Duration Minutes",
                "Type",
                "Status",
                "Notes",
                "Cancel Reason"));

        scheduleRepository.findAll().stream()
                .sorted(Comparator.comparing(Schedule::getDateTime))
                .forEach(schedule -> appendRow(csv, List.of(
                        teamLabel(schedule.getTeam()),
                        schedule.getField().getName(),
                        formatDateTime(schedule.getDateTime()),
                        String.valueOf(schedule.getDurationMinutes()),
                        display(schedule.getType()),
                        display(schedule.getStatus()),
                        valueOrBlank(schedule.getNotes()),
                        valueOrBlank(schedule.getCancelReason()))));
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportChampionshipsCsv() {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of(
                "Name",
                "Team",
                "Period",
                "Expected Matches",
                "Status",
                "Description"));

        championshipRepository.findAll().stream()
                .sorted(Comparator.comparing(Championship::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(championship -> appendRow(csv, List.of(
                        championship.getName(),
                        teamLabel(championship.getTeam()),
                        championship.getStartMonth() + "/" + championship.getStartYear()
                                + " - " + championship.getEndMonth() + "/" + championship.getEndYear(),
                        String.valueOf(championship.getExpectedMatches()),
                        championship.isActive() ? "Active" : "Inactive",
                        valueOrBlank(championship.getDescription()))));
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportEvaluationResultsCsv(UUID evaluationUuid) {
        Evaluation evaluation = evaluationRepository.findByUuid(evaluationUuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + evaluationUuid));
        var resultsByPlayer = evaluationResultRepository.findByEvaluationOrderByPlayerNameAsc(evaluation).stream()
                .collect(Collectors.toMap(
                        result -> result.getPlayer().getUuid(),
                        result -> result,
                        (first, second) -> first,
                        LinkedHashMap::new));

        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of(
                "Evaluation",
                "Group",
                "Status",
                "Player",
                "Participation",
                "Final Skill Level",
                "Source Event",
                "Finalized At"));

        evaluationPlayerRepository.findByEvaluationAndActiveTrueOrderByPlayerNameAsc(evaluation)
                .forEach(assignment -> {
                    var result = resultsByPlayer.get(assignment.getPlayer().getUuid());
                    appendRow(csv, List.of(
                            evaluation.getTitle(),
                            evaluation.getAgeGroup() + " " + display(evaluation.getTeamCategory()),
                            display(evaluation.getStatus()),
                            assignment.getPlayer().getName(),
                            result == null ? "" : display(result.getAttendanceStatus()),
                            result == null ? "" : display(result.getLevelResult()),
                            result == null || result.getSourceEvent() == null
                                    ? ""
                                    : result.getSourceEvent().getPlace() + " " + formatDate(result.getSourceEvent().getEventDate()),
                            result == null ? "" : formatDateTime(result.getFinalizedAt())));
                });
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportMatchAnalysisCsv(UUID teamUuid, UUID matchUuid) {
        Team team = teamService.getTeamByUuid(teamUuid);
        TeamMatch match = teamMatchRepository.findByUuid(matchUuid)
                .orElseThrow(() -> new EntityNotFoundException("Match not found: " + matchUuid));
        if (!match.getTeam().getUuid().equals(team.getUuid())) {
            throw new IllegalArgumentException("Match does not belong to this team");
        }
        Map<UUID, MatchPlayerAnalysis> analysesByPlayer = matchPlayerAnalysisRepository.findByMatch(match).stream()
                .collect(Collectors.toMap(
                        analysis -> analysis.getPlayer().getUuid(),
                        analysis -> analysis,
                        (first, second) -> first,
                        LinkedHashMap::new));

        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of(
                "Team",
                "Opponent",
                "Place",
                "Match Date Time",
                "Score",
                "Championship",
                "Player",
                "Positions",
                "Improvement Opportunities",
                "Highlights",
                "Notes"));

        playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team)
                .forEach(assignment -> appendMatchAnalysisRow(csv, match, assignment, analysesByPlayer));
        return csv.toString();
    }

    private void appendRow(StringBuilder csv, List<String> values) {
        csv.append(values.stream()
                .map(this::escape)
                .reduce((left, right) -> left + "," + right)
                .orElse(""))
                .append("\r\n");
    }

    private String escape(String value) {
        String safeValue = valueOrBlank(value);
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n") || safeValue.contains("\r")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private String joinPositions(Collection<PlayerPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            return "";
        }
        return positions.stream()
                .map(this::display)
                .reduce((left, right) -> left + "; " + right)
                .orElse("");
    }

    private void appendMatchAnalysisRow(
            StringBuilder csv,
            TeamMatch match,
            PlayerTeam assignment,
            Map<UUID, MatchPlayerAnalysis> analysesByPlayer) {
        Player player = assignment.getPlayer();
        MatchPlayerAnalysis analysis = analysesByPlayer.get(player.getUuid());
        appendRow(csv, List.of(
                teamLabel(match.getTeam()),
                match.getOpponent(),
                match.getPlace(),
                formatDateTime(match.getMatchDateTime()),
                scoreLabel(match),
                match.getChampionship() == null ? "" : match.getChampionship().getName(),
                player.getName(),
                joinPositions(player.getPositions()),
                analysis == null ? "" : joinTags(analysis.getImprovementTags()),
                analysis == null ? "" : joinTags(analysis.getHighlightTags()),
                analysis == null ? "" : valueOrBlank(analysis.getNotes())));
    }

    private String joinTags(String jsonData) {
        try {
            return objectMapper.readValue(jsonData, STRING_LIST).stream()
                    .reduce((left, right) -> left + "; " + right)
                    .orElse("");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored match tags must be valid JSON arrays", exception);
        }
    }

    private String scoreLabel(TeamMatch match) {
        if (match.getTeamScore() == null || match.getOpponentScore() == null) {
            return "";
        }
        return match.getTeamScore() + " - " + match.getOpponentScore();
    }

    private String teamLabel(Team team) {
        return team.getAgeGroup() + " " + display(team.getTeamCategory());
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private String display(Enum<?> value) {
        return value == null ? "" : value.name().replace('_', ' ');
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value;
    }
}
