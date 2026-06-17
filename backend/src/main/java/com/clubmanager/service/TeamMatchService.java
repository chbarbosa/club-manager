package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;
import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ClubSetup;
import com.clubmanager.domain.MatchPlayerAnalysis;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamMatch;
import com.clubmanager.dto.MatchPlayerAnalysisUpdateRequest;
import com.clubmanager.dto.TeamMatchCreateRequest;
import com.clubmanager.dto.TeamMatchUpdateRequest;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ClubSetupRepository;
import com.clubmanager.repository.MatchPlayerAnalysisRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamMatchRepository;
import com.clubmanager.repository.TeamRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMatchService {

    static final String IMPROVEMENT_SETUP_TYPE = "MATCH_IMPROVEMENT_OPPORTUNITY";
    static final String HIGHLIGHT_SETUP_TYPE = "MATCH_HIGHLIGHT";

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final TeamMatchRepository teamMatchRepository;
    private final MatchPlayerAnalysisRepository matchPlayerAnalysisRepository;
    private final TeamRepository teamRepository;
    private final ChampionshipRepository championshipRepository;
    private final PlayerRepository playerRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final ClubSetupRepository clubSetupRepository;
    private final ObjectMapper objectMapper;



    @Transactional
    public TeamMatch createMatch(UUID teamUuid, TeamMatchCreateRequest request) {
        requireText(request.opponent(), "opponent");
        requireText(request.place(), "place");
        validateMatchDateTime(request.matchDateTime());
        validateScores(request.teamScore(), request.opponentScore());

        Team team = getActiveTeam(teamUuid);
        TeamMatch match = TeamMatch.builder()
                .team(team)
                .championship(getOptionalChampionship(request.championshipUuid(), team))
                .opponent(request.opponent().trim())
                .place(request.place().trim())
                .matchDateTime(request.matchDateTime())
                .teamScore(request.teamScore())
                .opponentScore(request.opponentScore())
                .notes(cleanOptionalText(request.notes()))
                .build();
        return teamMatchRepository.save(match);
    }

    @Transactional(readOnly = true)
    public List<TeamMatch> getTeamMatches(UUID teamUuid) {
        return teamMatchRepository.findByTeamOrderByMatchDateTimeDesc(getTeam(teamUuid));
    }

    @Transactional(readOnly = true)
    public TeamMatch getTeamMatch(UUID teamUuid, UUID matchUuid) {
        Team team = getTeam(teamUuid);
        TeamMatch match = getMatch(matchUuid);
        ensureMatchBelongsToTeam(match, team);
        return match;
    }

    @Transactional
    public TeamMatch updateMatch(UUID teamUuid, UUID matchUuid, TeamMatchUpdateRequest request) {
        Team team = getTeam(teamUuid);
        TeamMatch match = getMatch(matchUuid);
        ensureMatchBelongsToTeam(match, team);

        applyTextUpdate(request.opponent(), "opponent", match::setOpponent);
        applyTextUpdate(request.place(), "place", match::setPlace);
        if (request.matchDateTime() != null) {
            validateMatchDateTime(request.matchDateTime());
            match.setMatchDateTime(request.matchDateTime());
        }
        if (request.championshipUuid() != null) {
            match.setChampionship(getOptionalChampionship(request.championshipUuid(), team));
        }
        if (request.teamScore() != null || request.opponentScore() != null) {
            Integer teamScore = request.teamScore() == null ? match.getTeamScore() : request.teamScore();
            Integer opponentScore = request.opponentScore() == null ? match.getOpponentScore() : request.opponentScore();
            validateScores(teamScore, opponentScore);
            match.setTeamScore(teamScore);
            match.setOpponentScore(opponentScore);
        }
        if (request.notes() != null) {
            match.setNotes(cleanOptionalText(request.notes()));
        }

        return teamMatchRepository.save(match);
    }

    @Transactional
    public MatchPlayerAnalysis savePlayerAnalysis(
            UUID teamUuid,
            UUID matchUuid,
            UUID playerUuid,
            MatchPlayerAnalysisUpdateRequest request) {
        Team team = getTeam(teamUuid);
        TeamMatch match = getMatch(matchUuid);
        ensureMatchBelongsToTeam(match, team);
        Player player = getPlayer(playerUuid);
        ensurePlayerIsActiveTeamMember(team, player);

        List<String> improvementTags = normalizeTags(request.improvementTags());
        List<String> highlightTags = normalizeTags(request.highlightTags());
        validateConfiguredTags(improvementTags, IMPROVEMENT_SETUP_TYPE, "Improvement opportunity");
        validateConfiguredTags(highlightTags, HIGHLIGHT_SETUP_TYPE, "Highlight");

        MatchPlayerAnalysis analysis = matchPlayerAnalysisRepository.findByMatchAndPlayer(match, player)
                .orElseGet(() -> MatchPlayerAnalysis.builder()
                        .match(match)
                        .player(player)
                        .build());
        analysis.setImprovementTags(toJson(improvementTags));
        analysis.setHighlightTags(toJson(highlightTags));
        analysis.setNotes(cleanOptionalText(request.notes()));
        return matchPlayerAnalysisRepository.save(analysis);
    }

    @Transactional(readOnly = true)
    public List<PlayerTeam> getCurrentRoster(UUID teamUuid) {
        return playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(getTeam(teamUuid));
    }

    @Transactional(readOnly = true)
    public Map<UUID, MatchPlayerAnalysis> getAnalysesByPlayer(TeamMatch match) {
        return matchPlayerAnalysisRepository.findByMatch(match).stream()
                .collect(Collectors.toMap(
                        analysis -> analysis.getPlayer().getUuid(),
                        analysis -> analysis,
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    public List<String> parseTags(String jsonData) {
        try {
            return objectMapper.readValue(jsonData, STRING_LIST);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored match tags must be valid JSON arrays", exception);
        }
    }

    private Team getTeam(UUID uuid) {
        return teamRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + uuid));
    }

    private Team getActiveTeam(UUID uuid) {
        Team team = getTeam(uuid);
        if (!team.isActive()) {
            throw new IllegalArgumentException("Cannot create matches for an inactive team");
        }
        return team;
    }

    private TeamMatch getMatch(UUID uuid) {
        return teamMatchRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Match not found: " + uuid));
    }

    private Player getPlayer(UUID uuid) {
        return playerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + uuid));
    }

    private Championship getOptionalChampionship(UUID uuid, Team team) {
        if (uuid == null) {
            return null;
        }
        Championship championship = championshipRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Championship not found: " + uuid));
        if (!championship.getTeam().getUuid().equals(team.getUuid())) {
            throw new IllegalArgumentException("Championship must belong to the match team");
        }
        return championship;
    }

    private void ensureMatchBelongsToTeam(TeamMatch match, Team team) {
        if (!match.getTeam().getUuid().equals(team.getUuid())) {
            throw new IllegalArgumentException("Match does not belong to this team");
        }
    }

    private void ensurePlayerIsActiveTeamMember(Team team, Player player) {
        if (!player.isActive()) {
            throw new IllegalArgumentException("Cannot analyze an inactive player");
        }
        if (!playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(team, player)) {
            throw new IllegalArgumentException("Player must be assigned to the match team");
        }
    }

    private void validateMatchDateTime(LocalDateTime matchDateTime) {
        if (matchDateTime == null) {
            throw new IllegalArgumentException("matchDateTime is required");
        }
    }

    private void validateScores(Integer teamScore, Integer opponentScore) {
        if ((teamScore != null && teamScore < 0) || (opponentScore != null && opponentScore < 0)) {
            throw new IllegalArgumentException("Match scores cannot be negative");
        }
    }

    private List<String> normalizeTags(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private void validateConfiguredTags(List<String> selectedTags, String setupType, String label) {
        Set<String> allowedTags = Set.copyOf(parseTags(getSetup(setupType).getJsonData()));
        for (String tag : selectedTags) {
            if (!allowedTags.contains(tag)) {
                throw new IllegalArgumentException(label + " tag is not configured: " + tag);
            }
        }
    }

    private ClubSetup getSetup(String type) {
        return clubSetupRepository.findByType(type)
                .orElseThrow(() -> new EntityNotFoundException("Club setup type not found: " + type));
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize match tags", exception);
        }
    }

    private String cleanOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
