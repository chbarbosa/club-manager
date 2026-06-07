package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerSkillHistory;
import com.clubmanager.domain.SkillLevel;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.PlayerCreateRequest;
import com.clubmanager.dto.PlayerUpdateRequest;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerSkillHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerSkillHistoryRepository playerSkillHistoryRepository;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository, playerSkillHistoryRepository);
    }

    @Test
    void createPlayer_WithValidRequest_SetsRegisterDateToToday() {
        PlayerCreateRequest request = createRequest("REG-1");
        when(playerRepository.existsByRegistrationNumber("REG-1")).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Player player = playerService.createPlayer(request);

        assertThat(player.getName()).isEqualTo("Joao Silva");
        assertThat(player.getRegisterDate()).isEqualTo(LocalDate.now());
        assertThat(player.isActive()).isTrue();
    }

    @Test
    void createPlayer_WithFutureMemberSince_ThrowsValidationException() {
        PlayerCreateRequest request = new PlayerCreateRequest(
                "Joao Silva",
                "Brazil",
                "Brazil",
                LocalDate.now().minusYears(15),
                TeamCategory.MASCULINE,
                null,
                LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> playerService.createPlayer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Member since");
    }

    @Test
    void createPlayer_WithFutureBirthdate_ThrowsValidationException() {
        PlayerCreateRequest request = new PlayerCreateRequest(
                "Joao Silva",
                "Brazil",
                "Brazil",
                LocalDate.now().plusDays(1),
                TeamCategory.MASCULINE,
                null,
                LocalDate.now());

        assertThatThrownBy(() -> playerService.createPlayer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Birthdate");
    }

    @Test
    void createPlayer_WithDuplicateRegistrationNumber_ThrowsException() {
        PlayerCreateRequest request = createRequest("REG-1");
        when(playerRepository.existsByRegistrationNumber("REG-1")).thenReturn(true);

        assertThatThrownBy(() -> playerService.createPlayer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration number");
    }

    @Test
    void updatePlayer_WithValidRequest_UpdatesFields() {
        Player player = player();
        PlayerUpdateRequest request = new PlayerUpdateRequest(
                "Maria Silva",
                "Portugal",
                "Canada",
                LocalDate.now().minusYears(14),
                TeamCategory.FEMININE,
                "REG-2",
                LocalDate.now().minusYears(1));
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerRepository.existsByRegistrationNumber("REG-2")).thenReturn(false);
        when(playerRepository.save(player)).thenReturn(player);

        Player updated = playerService.updatePlayer(player.getUuid(), request);

        assertThat(updated.getName()).isEqualTo("Maria Silva");
        assertThat(updated.getBirthCountry()).isEqualTo("Portugal");
        assertThat(updated.getTeamCategory()).isEqualTo(TeamCategory.FEMININE);
        assertThat(updated.getRegistrationNumber()).isEqualTo("REG-2");
    }

    @Test
    void updatePlayer_RegisterDateIsNeverChanged() {
        Player player = player();
        LocalDate originalRegisterDate = player.getRegisterDate();
        PlayerUpdateRequest request = new PlayerUpdateRequest(
                "Maria Silva",
                null,
                null,
                null,
                null,
                null,
                null);
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        Player updated = playerService.updatePlayer(player.getUuid(), request);

        assertThat(updated.getRegisterDate()).isEqualTo(originalRegisterDate);
    }

    @Test
    void deactivatePlayer_WhenActive_SetsActiveFalse() {
        Player player = player();
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        Player updated = playerService.deactivatePlayer(player.getUuid());

        assertThat(updated.isActive()).isFalse();
        verify(playerRepository).save(player);
    }

    @Test
    void reactivatePlayer_WhenInactive_SetsActiveTrue() {
        Player player = player();
        player.setActive(false);
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        Player updated = playerService.reactivatePlayer(player.getUuid());

        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void getAllPlayers_ReturnsPaginatedSummary() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Player> page = new PageImpl<>(List.of(player()), pageable, 1);
        when(playerRepository.findAll(pageable)).thenReturn(page);

        Page<Player> result = playerService.getAllPlayers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Joao Silva");
    }

    @Test
    void searchPlayers_ByName_ReturnsMatchingResults() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Player> page = new PageImpl<>(List.of(player()), pageable, 1);
        when(playerRepository.findByNameContainingIgnoreCase("joao", pageable)).thenReturn(page);

        Page<Player> result = playerService.searchPlayers(" joao ", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Joao Silva");
    }

    @Test
    void getSkillHistory_WithKnownPlayer_ReturnsHistory() {
        Player player = player();
        PlayerSkillHistory history = PlayerSkillHistory.builder()
                .player(player)
                .skillLevel(SkillLevel.SKILLED)
                .changedAt(java.time.LocalDateTime.now())
                .build();
        when(playerRepository.findByUuid(player.getUuid())).thenReturn(Optional.of(player));
        when(playerSkillHistoryRepository.findByPlayerOrderByChangedAtDesc(player)).thenReturn(List.of(history));

        List<PlayerSkillHistory> result = playerService.getSkillHistory(player.getUuid());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSkillLevel()).isEqualTo(SkillLevel.SKILLED);
    }

    private PlayerCreateRequest createRequest(String registrationNumber) {
        return new PlayerCreateRequest(
                "Joao Silva",
                "Brazil",
                "Brazil",
                LocalDate.now().minusYears(16),
                TeamCategory.MASCULINE,
                registrationNumber,
                LocalDate.now().minusYears(2));
    }

    private Player player() {
        Player player = new Player();
        player.setName("Joao Silva");
        player.setBirthCountry("Brazil");
        player.setLivingCountry("Brazil");
        player.setBirthdate(LocalDate.now().minusYears(16));
        player.setTeamCategory(TeamCategory.MASCULINE);
        player.setRegistrationNumber("REG-1");
        player.setRegisterDate(LocalDate.now().minusDays(10));
        player.setMemberSince(LocalDate.now().minusYears(2));
        player.setActive(true);
        return player;
    }
}
