package com.clubmanager.service;

import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAdvice;
import com.clubmanager.domain.TeamAdviceItem;
import com.clubmanager.repository.PlayerTeamRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamAdviceService {

    public static final int MINIMUM_PLAYERS_FOR_ADVICE = 12;
    public static final int MAXIMUM_RECOMMENDED_PLAYERS = 22;
    private static final int MINIMUM_GOALKEEPERS = 2;
    private static final int MINIMUM_FIELD_POSITION_PLAYERS = 3;
    private static final String WARNING = "WARNING";

    private final PlayerTeamRepository playerTeamRepository;



    @Transactional(readOnly = true)
    public TeamAdvice analyze(Team team) {
        List<PlayerTeam> roster = playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team);
        int goalkeepers = countPlayersWithPosition(roster, PlayerPosition.GOALKEEPER);
        int defenders = countPlayersWithPosition(roster, PlayerPosition.DEFENSE);
        int midfielders = countPlayersWithPosition(roster, PlayerPosition.MIDFIELD);
        int attackers = countPlayersWithPosition(roster, PlayerPosition.ATTACK);

        List<TeamAdviceItem> items = roster.size() < MINIMUM_PLAYERS_FOR_ADVICE
                ? List.of()
                : buildAdviceItems(roster.size(), goalkeepers, defenders, midfielders, attackers);

        return new TeamAdvice(
                roster.size(),
                MINIMUM_PLAYERS_FOR_ADVICE,
                goalkeepers,
                defenders,
                midfielders,
                attackers,
                items);
    }

    private List<TeamAdviceItem> buildAdviceItems(
            int totalPlayers,
            int goalkeepers,
            int defenders,
            int midfielders,
            int attackers) {
        List<TeamAdviceItem> items = new ArrayList<>();
        if (totalPlayers > MAXIMUM_RECOMMENDED_PLAYERS) {
            items.add(new TeamAdviceItem("TOO_MANY_PLAYERS", WARNING, "Too many players assigned."));
        }
        if (goalkeepers == 0) {
            items.add(new TeamAdviceItem("NO_GOALKEEPER", WARNING, "No goalkeepers assigned."));
        } else if (goalkeepers < MINIMUM_GOALKEEPERS) {
            items.add(new TeamAdviceItem("ONLY_ONE_GOALKEEPER", WARNING, "Only one goalkeeper assigned."));
        }
        if (defenders < MINIMUM_FIELD_POSITION_PLAYERS) {
            items.add(new TeamAdviceItem("FEW_DEFENDERS", WARNING, "Few defenders assigned."));
        }
        if (midfielders < MINIMUM_FIELD_POSITION_PLAYERS) {
            items.add(new TeamAdviceItem("FEW_MIDFIELDERS", WARNING, "Few midfielders assigned."));
        }
        if (attackers < MINIMUM_FIELD_POSITION_PLAYERS) {
            items.add(new TeamAdviceItem("FEW_ATTACKERS", WARNING, "Few attackers assigned."));
        }
        return items;
    }

    private int countPlayersWithPosition(List<PlayerTeam> roster, PlayerPosition position) {
        return (int) roster.stream()
                .filter(assignment -> assignment.getPlayer().getPositions().contains(position))
                .count();
    }
}
