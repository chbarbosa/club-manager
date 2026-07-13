package com.clubmanager.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrainerReadOnlyAccessTest {

    private static final String TRAINER_EMAIL = "trainer-readonly@club.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerTeamRepository playerTeamRepository;

    private Trainer trainer;
    private Team ownTeam;
    private Team otherTeam;

    @BeforeEach
    void setUp() {
        trainer = trainerRepository.save(trainer("Trainer Readonly", TRAINER_EMAIL));
        Trainer otherTrainer = trainerRepository.save(trainer("Other Trainer", "other-readonly@club.com"));
        ownTeam = teamRepository.save(team("Under 13 A", trainer));
        otherTeam = teamRepository.save(team("Under 14 B", otherTrainer));
    }

    @Test
    void trainerCanReadPlayersButCannotCreatePlayers() throws Exception {
        mockMvc.perform(get("/api/v1/players")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/players")
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanListOnlyAssignedTeamsAndViewOwnTeamOnly() throws Exception {
        mockMvc.perform(get("/api/v1/teams")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].uuid").value(ownTeam.getUuid().toString()));

        mockMvc.perform(get("/api/v1/teams/{uuid}", ownTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/teams/{uuid}", otherTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanReadOwnRosterAndTrainingSchedulesOnly() throws Exception {
        mockMvc.perform(get("/api/v1/teams/{teamUuid}/players", ownTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/teams/{teamUuid}/players", otherTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/schedules")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/schedules")
                        .param("teamUuid", otherTeam.getUuid().toString())
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanAssignAndRemovePlayersFromOwnTeamOnly() throws Exception {
        Player player = playerRepository.save(player("Trainer Roster Player", "TRAINER-ROSTER-1"));

        String response = mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", ownTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s", "jerseyNumber": 7}
                                """.formatted(player.getUuid())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerUuid").value(player.getUuid().toString()))
                .andExpect(jsonPath("$.jerseyNumber").value(7))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String assignmentUuid = JsonPath.read(response, "$.uuid");

        mockMvc.perform(delete("/api/v1/teams/{teamUuid}/players/{assignmentUuid}", ownTeam.getUuid(), assignmentUuid)
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        Player otherPlayer = playerRepository.save(player("Other Roster Player", "TRAINER-ROSTER-2"));
        mockMvc.perform(post("/api/v1/teams/{teamUuid}/players", otherTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerUuid": "%s", "jerseyNumber": 9}
                                """.formatted(otherPlayer.getUuid())))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanCreateUpdateAndAnalyzeOwnTeamMatchesOnly() throws Exception {
        Player player = playerRepository.save(player("Trainer Match Player", "TRAINER-MATCH-1"));
        playerTeamRepository.save(PlayerTeam.builder()
                .team(ownTeam)
                .player(player)
                .jerseyNumber(11)
                .assignedDate(LocalDate.now())
                .build());

        String response = mockMvc.perform(post("/api/v1/teams/{teamUuid}/matches", ownTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "Trainer Rivals",
                                  "place": "Main Field",
                                  "matchDateTime": "2026-08-12T18:00:00",
                                  "teamScore": 2,
                                  "opponentScore": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.opponent").value("Trainer Rivals"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String matchUuid = JsonPath.read(response, "$.uuid");

        mockMvc.perform(put("/api/v1/teams/{teamUuid}/matches/{matchUuid}", ownTeam.getUuid(), matchUuid)
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "Updated Trainer Rivals",
                                  "place": "Secondary Field",
                                  "matchDateTime": "2026-08-12T19:00:00",
                                  "teamScore": 3,
                                  "opponentScore": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponent").value("Updated Trainer Rivals"));

        mockMvc.perform(put("/api/v1/teams/{teamUuid}/matches/{matchUuid}/players/{playerUuid}",
                        ownTeam.getUuid(), matchUuid, player.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "improvementTags": ["Improve pass"],
                                  "highlightTags": ["Good passes"],
                                  "notes": "Trainer analysis"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Trainer analysis"));

        mockMvc.perform(post("/api/v1/teams/{teamUuid}/matches", otherTeam.getUuid())
                        .with(user(TRAINER_EMAIL).roles("TRAINER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opponent": "Forbidden Rivals",
                                  "place": "Main Field",
                                  "matchDateTime": "2026-08-12T18:00:00"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanReadMatchTagSetupOnly() throws Exception {
        mockMvc.perform(get("/api/v1/club/setup/MATCH_IMPROVEMENT_OPPORTUNITY")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MATCH_IMPROVEMENT_OPPORTUNITY"));

        mockMvc.perform(get("/api/v1/club/setup/EVALUATION_LEVEL")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void trainerCanReadOwnProfile() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/me")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainer.uuid").value(trainer.getUuid().toString()))
                .andExpect(jsonPath("$.teams[0].teamUuid").value(ownTeam.getUuid().toString()));
    }

    @Test
    void trainerCannotReadAdminOnlyAreas() throws Exception {
        mockMvc.perform(get("/api/v1/admins")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/club-analysis/current")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/support-access")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/club/setup")
                        .with(user(TRAINER_EMAIL).roles("TRAINER")))
                .andExpect(status().isForbidden());
    }

    private Trainer trainer(String name, String email) {
        return Trainer.builder()
                .name(name)
                .email(email)
                .birthdate(LocalDate.now().minusYears(35))
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now().minusYears(2))
                .active(true)
                .build();
    }

    private Team team(String identification, Trainer trainer) {
        return Team.builder()
                .ageGroup(identification)
                .ageCategory(TeamAgeCategory.U13)
                .teamCategory(TeamCategory.MASCULINE)
                .trainer(trainer)
                .active(true)
                .build();
    }

    private Player player(String name, String registrationNumber) {
        return Player.builder()
                .name(name)
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(12))
                .teamCategory(TeamCategory.MASCULINE)
                .positions(Set.of(PlayerPosition.MIDFIELD))
                .registrationNumber(registrationNumber)
                .registerDate(LocalDate.now())
                .memberSince(LocalDate.now().minusYears(2))
                .active(true)
                .build();
    }
}
