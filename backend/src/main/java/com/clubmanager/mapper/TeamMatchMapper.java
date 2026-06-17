package com.clubmanager.mapper;

import com.clubmanager.domain.MatchPlayerAnalysis;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.TeamMatch;
import com.clubmanager.dto.MatchPlayerAnalysisResponse;
import com.clubmanager.dto.TeamMatchResponse;
import com.clubmanager.service.TeamMatchService;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamMatchMapper {

    private final TeamMatchService teamMatchService;



    public TeamMatchResponse toSummaryResponse(TeamMatch match) {
        return toResponse(match, List.of());
    }

    public TeamMatchResponse toDetailResponse(
            TeamMatch match,
            List<PlayerTeam> roster,
            Map<UUID, MatchPlayerAnalysis> analysesByPlayer) {
        return toResponse(match, roster.stream()
                .map(assignment -> toPlayerAnalysisResponse(
                        assignment.getPlayer(),
                        analysesByPlayer.get(assignment.getPlayer().getUuid())))
                .toList());
    }

    public MatchPlayerAnalysisResponse toPlayerAnalysisResponse(MatchPlayerAnalysis analysis) {
        return toPlayerAnalysisResponse(analysis.getPlayer(), analysis);
    }

    private TeamMatchResponse toResponse(TeamMatch match, List<MatchPlayerAnalysisResponse> playerAnalyses) {
        return new TeamMatchResponse(
                match.getUuid(),
                match.getTeam().getUuid(),
                match.getTeam().getAgeGroup(),
                match.getChampionship() == null ? null : match.getChampionship().getUuid(),
                match.getChampionship() == null ? null : match.getChampionship().getName(),
                match.getOpponent(),
                match.getPlace(),
                match.getMatchDateTime(),
                match.getTeamScore(),
                match.getOpponentScore(),
                match.getNotes(),
                playerAnalyses);
    }

    private MatchPlayerAnalysisResponse toPlayerAnalysisResponse(Player player, MatchPlayerAnalysis analysis) {
        return new MatchPlayerAnalysisResponse(
                analysis == null ? null : analysis.getUuid(),
                player.getUuid(),
                player.getName(),
                calculateAge(player.getBirthdate()),
                player.getTeamCategory(),
                player.getPositions(),
                analysis == null ? List.of() : teamMatchService.parseTags(analysis.getImprovementTags()),
                analysis == null ? List.of() : teamMatchService.parseTags(analysis.getHighlightTags()),
                analysis == null ? null : analysis.getNotes());
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
