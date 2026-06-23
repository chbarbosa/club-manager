package com.clubmanager.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import java.time.LocalDate;
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
}
