package com.clubmanager.service;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.Team;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportExportService {

    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final TeamService teamService;

    public ReportExportService(
            PlayerRepository playerRepository,
            PlayerTeamRepository playerTeamRepository,
            TeamService teamService) {
        this.playerRepository = playerRepository;
        this.playerTeamRepository = playerTeamRepository;
        this.teamService = teamService;
    }

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
                    player.getName(),
                    String.valueOf(calculateAge(player.getBirthdate())),
                    display(player.getTeamCategory()),
                    display(player.getCurrentSkillLevel()),
                    joinPositions(player.getPositions()),
                    formatDate(assignment.getAssignedDate())));
        });
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

    private String teamLabel(Team team) {
        return team.getAgeGroup() + " " + display(team.getTeamCategory());
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private String display(Enum<?> value) {
        return value == null ? "" : value.name().replace('_', ' ');
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value;
    }
}
