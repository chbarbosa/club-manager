package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;

import com.clubmanager.domain.Player;
import com.clubmanager.domain.PlayerPosition;
import com.clubmanager.domain.PlayerSkillHistory;
import com.clubmanager.domain.PlayerTeam;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.PlayerCreateRequest;
import com.clubmanager.dto.PlayerUpdateRequest;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerSkillHistoryRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerSkillHistoryRepository playerSkillHistoryRepository;
    private final PlayerTeamRepository playerTeamRepository;
    private final ChampionshipRepository championshipRepository;

    @Transactional
    public Player createPlayer(PlayerCreateRequest request) {
        validateBirthdate(request.birthdate());
        validateMemberSince(request.memberSince());
        validatePositions(request.positions());
        validateRegistrationNumberUniqueness(request.registrationNumber(), null);

        Player player = Player.builder()
                .name(request.name())
                .birthCountry(request.birthCountry())
                .livingCountry(request.livingCountry())
                .birthdate(request.birthdate())
                .teamCategory(request.teamCategory())
                .positions(new LinkedHashSet<>(request.positions()))
                .registrationNumber(cleanRegistrationNumber(request.registrationNumber()))
                .registerDate(LocalDate.now())
                .memberSince(request.memberSince())
                .build();
        return playerRepository.save(player);
    }

    @Transactional(readOnly = true)
    public Player getPlayerByUuid(UUID uuid) {
        return playerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + uuid));
    }

    @Transactional(readOnly = true)
    public Page<Player> getAllPlayers(Pageable pageable) {
        return playerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Player> searchPlayers(String name, Pageable pageable) {
        return searchPlayers(name, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Player> searchPlayers(String name, Boolean active, Pageable pageable) {
        boolean hasName = StringUtils.hasText(name);
        if (!hasName && active == null) {
            return getAllPlayers(pageable);
        }
        if (!hasName) {
            return active ? playerRepository.findAllByActiveTrue(pageable) : playerRepository.findAllByActiveFalse(pageable);
        }
        String cleanName = name.trim();
        if (active == null) {
            return playerRepository.findByNameContainingIgnoreCase(cleanName, pageable);
        }
        return active
                ? playerRepository.findByNameContainingIgnoreCaseAndActiveTrue(cleanName, pageable)
                : playerRepository.findByNameContainingIgnoreCaseAndActiveFalse(cleanName, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.List<PlayerSkillHistory> getSkillHistory(UUID uuid) {
        return playerSkillHistoryRepository.findByPlayerOrderByChangedAtDesc(getPlayerByUuid(uuid));
    }

    @Transactional(readOnly = true)
    public java.util.List<PlayerTeam> getTeamHistory(UUID uuid) {
        return playerTeamRepository.findByPlayerOrderByAssignedDateDesc(getPlayerByUuid(uuid));
    }

    @Transactional(readOnly = true)
    public long countChampionships(UUID uuid) {
        getPlayerByUuid(uuid);
        return championshipRepository.countDistinctByPlayerUuid(uuid);
    }

    @Transactional
    public Player updatePlayer(UUID uuid, PlayerUpdateRequest request) {
        Player player = getPlayerByUuid(uuid);

        applyTextUpdate(request.name(), "name", player::setName);
        applyTextUpdate(request.birthCountry(), "birthCountry", player::setBirthCountry);
        applyTextUpdate(request.livingCountry(), "livingCountry", player::setLivingCountry);
        applyBirthdateUpdate(request.birthdate(), player::setBirthdate);
        applyTeamCategoryUpdate(request.teamCategory(), player::setTeamCategory);
        applyPositionsUpdate(request.positions(), player::setPositions);
        applyRegistrationNumberUpdate(request.registrationNumber(), player);
        applyMemberSinceUpdate(request.memberSince(), player::setMemberSince);

        return playerRepository.save(player);
    }

    @Transactional
    public Player deactivatePlayer(UUID uuid) {
        Player player = getPlayerByUuid(uuid);
        player.setActive(false);
        return playerRepository.save(player);
    }

    @Transactional
    public Player reactivatePlayer(UUID uuid) {
        Player player = getPlayerByUuid(uuid);
        player.setActive(true);
        return playerRepository.save(player);
    }

    private void validateBirthdate(LocalDate birthdate) {
        if (birthdate == null || !birthdate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Birthdate must be in the past");
        }
    }

    private void validateMemberSince(LocalDate memberSince) {
        if (memberSince == null || memberSince.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Member since must not be in the future");
        }
    }

    private void validatePositions(Set<PlayerPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("At least one player position is required");
        }
    }

    private void validateRegistrationNumberUniqueness(String registrationNumber, Player currentPlayer) {
        String cleaned = cleanRegistrationNumber(registrationNumber);
        if (cleaned == null) {
            return;
        }
        boolean sameAsCurrent = currentPlayer != null && cleaned.equals(currentPlayer.getRegistrationNumber());
        if (!sameAsCurrent && playerRepository.existsByRegistrationNumber(cleaned)) {
            throw new IllegalArgumentException("Registration number already exists");
        }
    }

    private String cleanRegistrationNumber(String registrationNumber) {
        return StringUtils.hasText(registrationNumber) ? registrationNumber.trim() : null;
    }

    private void applyBirthdateUpdate(LocalDate birthdate, Consumer<LocalDate> setter) {
        if (birthdate == null) {
            return;
        }
        validateBirthdate(birthdate);
        setter.accept(birthdate);
    }

    private void applyMemberSinceUpdate(LocalDate memberSince, Consumer<LocalDate> setter) {
        if (memberSince == null) {
            return;
        }
        validateMemberSince(memberSince);
        setter.accept(memberSince);
    }

    private void applyTeamCategoryUpdate(TeamCategory teamCategory, Consumer<TeamCategory> setter) {
        if (teamCategory != null) {
            setter.accept(teamCategory);
        }
    }

    private void applyPositionsUpdate(Set<PlayerPosition> positions, Consumer<Set<PlayerPosition>> setter) {
        if (positions == null) {
            return;
        }
        validatePositions(positions);
        setter.accept(new LinkedHashSet<>(positions));
    }

    private void applyRegistrationNumberUpdate(String registrationNumber, Player player) {
        if (registrationNumber == null) {
            return;
        }
        validateRegistrationNumberUniqueness(registrationNumber, player);
        player.setRegistrationNumber(cleanRegistrationNumber(registrationNumber));
    }

}
