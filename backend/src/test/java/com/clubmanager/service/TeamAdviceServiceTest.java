package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.repository.PlayerTeamRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamAdviceServiceTest {

    @Mock
    private PlayerTeamRepository playerTeamRepository;

    @Test
    void analyze_WithFewerThanMinimumPlayers_ReturnsCountsWithoutAdvice() {
        Team team = team();
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team))
                .thenReturn(List.of(
                        assignment(team, PlayerPosition.GOALKEEPER),
                        assignment(team, PlayerPosition.DEFENSE)));

        var advice = new TeamAdviceService(playerTeamRepository).analyze(team);

        assertThat(advice.totalPlayers()).isEqualTo(2);
        assertThat(advice.minimumPlayersForAdvice()).isEqualTo(12);
        assertThat(advice.goalkeepers()).isEqualTo(1);
        assertThat(advice.defenders()).isEqualTo(1);
        assertThat(advice.items()).isEmpty();
    }

    @Test
    void analyze_WithNoGoalkeeperAfterMinimumPlayers_ReturnsNoGoalkeeperAdvice() {
        Team team = team();
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team))
                .thenReturn(roster(team, 12, Set.of(PlayerPosition.DEFENSE)));

        var advice = new TeamAdviceService(playerTeamRepository).analyze(team);

        assertThat(advice.items()).extracting("code").contains("NO_GOALKEEPER");
    }

    @Test
    void analyze_WithOneGoalkeeperAfterMinimumPlayers_ReturnsOnlyOneGoalkeeperAdvice() {
        Team team = team();
        List<PlayerTeam> roster = roster(team, 11, Set.of(PlayerPosition.DEFENSE));
        roster.add(assignment(team, PlayerPosition.GOALKEEPER));
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team)).thenReturn(roster);

        var advice = new TeamAdviceService(playerTeamRepository).analyze(team);

        assertThat(advice.items()).extracting("code").contains("ONLY_ONE_GOALKEEPER");
    }

    @Test
    void analyze_WithFewFieldPositions_ReturnsPositionAdvice() {
        Team team = team();
        List<PlayerTeam> roster = roster(team, 10, Set.of(PlayerPosition.DEFENSE));
        roster.add(assignment(team, PlayerPosition.GOALKEEPER));
        roster.add(assignment(team, PlayerPosition.GOALKEEPER));
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team)).thenReturn(roster);

        var advice = new TeamAdviceService(playerTeamRepository).analyze(team);

        assertThat(advice.items()).extracting("code")
                .contains("FEW_MIDFIELDERS", "FEW_ATTACKERS")
                .doesNotContain("FEW_DEFENDERS");
    }

    @Test
    void analyze_WithMultiPositionPlayers_CountsEachPosition() {
        Team team = team();
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team))
                .thenReturn(roster(team, 12, Set.of(PlayerPosition.DEFENSE, PlayerPosition.MIDFIELD)));

        var advice = new TeamAdviceService(playerTeamRepository).analyze(team);

        assertThat(advice.defenders()).isEqualTo(12);
        assertThat(advice.midfielders()).isEqualTo(12);
    }

    @Test
    void analyze_WithMoreThanRecommendedPlayers_ReturnsTooManyPlayersAdvice() {
        Team team = team();
        when(playerTeamRepository.findByTeamAndActiveTrueOrderByPlayer_NameAsc(team))
                .thenReturn(roster(team, 23, Set.of(PlayerPosition.DEFENSE)));

        var advice = new TeamAdviceService(playerTeamRepository).analyze(team);

        assertThat(advice.items()).extracting("code").contains("TOO_MANY_PLAYERS");
    }

    private List<PlayerTeam> roster(Team team, int total, Set<PlayerPosition> positions) {
        java.util.ArrayList<PlayerTeam> roster = new java.util.ArrayList<>();
        for (int index = 0; index < total; index++) {
            roster.add(assignment(team, positions));
        }
        return roster;
    }

    private PlayerTeam assignment(Team team, PlayerPosition position) {
        return assignment(team, Set.of(position));
    }

    private PlayerTeam assignment(Team team, Set<PlayerPosition> positions) {
        return PlayerTeam.builder()
                .team(team)
                .player(Player.builder()
                        .name("Player")
                        .birthCountry("Brazil")
                        .livingCountry("Brazil")
                        .birthdate(LocalDate.now().minusYears(13))
                        .teamCategory(TeamCategory.MASCULINE)
                        .positions(positions)
                        .registerDate(LocalDate.now())
                        .memberSince(LocalDate.now())
                        .build())
                .assignedDate(LocalDate.now())
                .build();
    }

    private Team team() {
        return Team.builder()
                .ageGroup("Under 13 A")
                .ageCategory(TeamAgeCategory.U13)
                .teamCategory(TeamCategory.MASCULINE)
                .trainer(Trainer.builder()
                        .name("Carlos Mendes")
                        .registerDate(LocalDate.now())
                        .memberSince(LocalDate.now())
                        .build())
                .build();
    }
}
