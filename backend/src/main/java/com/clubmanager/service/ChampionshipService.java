package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;
import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Championship;
import com.clubmanager.domain.ChampionshipRoster;
import com.clubmanager.domain.Player;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.ChampionshipCreateRequest;
import com.clubmanager.dto.ChampionshipRosterAssignRequest;
import com.clubmanager.dto.ChampionshipUpdateRequest;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ChampionshipRosterRepository;
import com.clubmanager.repository.PlayerRepository;
import com.clubmanager.repository.PlayerTeamRepository;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChampionshipService {

    private final ChampionshipRepository championshipRepository;
    private final ChampionshipRosterRepository championshipRosterRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TrainerRepository trainerRepository;
    private final PlayerTeamRepository playerTeamRepository;

    public ChampionshipService(
            ChampionshipRepository championshipRepository,
            ChampionshipRosterRepository championshipRosterRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            TrainerRepository trainerRepository,
            PlayerTeamRepository playerTeamRepository) {
        this.championshipRepository = championshipRepository;
        this.championshipRosterRepository = championshipRosterRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.trainerRepository = trainerRepository;
        this.playerTeamRepository = playerTeamRepository;
    }

    @Transactional
    public Championship createChampionship(ChampionshipCreateRequest request) {
        requireText(request.name(), "name");
        validatePeriod(request.startMonth(), request.startYear(), request.endMonth(), request.endYear());
        Championship championship = Championship.builder()
                .name(request.name().trim())
                .description(cleanOptionalText(request.description()))
                .team(getActiveTeam(request.teamUuid()))
                .startMonth(request.startMonth())
                .startYear(request.startYear())
                .endMonth(request.endMonth())
                .endYear(request.endYear())
                .build();
        return championshipRepository.save(championship);
    }

    @Transactional(readOnly = true)
    public Page<Championship> searchChampionships(String name, UUID teamUuid, Pageable pageable) {
        boolean hasName = name != null && !name.isBlank();
        Team team = teamUuid == null ? null : getTeam(teamUuid);
        if (hasName && team != null) {
            return championshipRepository.findByNameContainingIgnoreCaseAndTeam(name.trim(), team, pageable);
        }
        if (hasName) {
            return championshipRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        }
        if (team != null) {
            return championshipRepository.findByTeam(team, pageable);
        }
        return championshipRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Championship getChampionshipByUuid(UUID uuid) {
        return championshipRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Championship not found: " + uuid));
    }

    @Transactional
    public Championship updateChampionship(UUID uuid, ChampionshipUpdateRequest request) {
        Championship championship = getChampionshipByUuid(uuid);
        ensureActive(championship);

        applyTextUpdate(request.name(), "name", championship::setName);
        if (request.description() != null) {
            championship.setDescription(cleanOptionalText(request.description()));
        }
        if (request.teamUuid() != null) {
            championship.setTeam(getActiveTeam(request.teamUuid()));
        }

        int startMonth = request.startMonth() == null ? championship.getStartMonth() : request.startMonth();
        int startYear = request.startYear() == null ? championship.getStartYear() : request.startYear();
        int endMonth = request.endMonth() == null ? championship.getEndMonth() : request.endMonth();
        int endYear = request.endYear() == null ? championship.getEndYear() : request.endYear();
        validatePeriod(startMonth, startYear, endMonth, endYear);
        championship.setStartMonth(startMonth);
        championship.setStartYear(startYear);
        championship.setEndMonth(endMonth);
        championship.setEndYear(endYear);

        return championshipRepository.save(championship);
    }

    @Transactional
    public Championship deactivateChampionship(UUID uuid) {
        Championship championship = getChampionshipByUuid(uuid);
        championship.setActive(false);
        return championshipRepository.save(championship);
    }

    @Transactional
    public Championship reactivateChampionship(UUID uuid) {
        Championship championship = getChampionshipByUuid(uuid);
        championship.setActive(true);
        return championshipRepository.save(championship);
    }

    @Transactional(readOnly = true)
    public List<ChampionshipRoster> getActiveRoster(UUID championshipUuid) {
        return championshipRosterRepository.findByChampionshipAndActiveTrueOrderByPlayer_NameAsc(
                getChampionshipByUuid(championshipUuid));
    }

    @Transactional
    public ChampionshipRoster assignRosterPlayer(UUID championshipUuid, ChampionshipRosterAssignRequest request) {
        Championship championship = getChampionshipByUuid(championshipUuid);
        ensureActive(championship);
        Player player = getActivePlayer(request.playerUuid());
        Trainer trainer = getActiveTrainer(request.trainerUuid());
        if (!playerTeamRepository.existsByTeamAndPlayerAndActiveTrue(championship.getTeam(), player)) {
            throw new IllegalArgumentException("Player must be assigned to the championship team roster");
        }
        if (championshipRosterRepository.existsByChampionshipAndPlayerAndActiveTrue(championship, player)) {
            throw new IllegalArgumentException("Player is already assigned to this championship roster");
        }

        ChampionshipRoster roster = ChampionshipRoster.builder()
                .championship(championship)
                .player(player)
                .trainer(trainer)
                .assignedDate(LocalDate.now())
                .build();
        return championshipRosterRepository.save(roster);
    }

    @Transactional
    public ChampionshipRoster removeRosterPlayer(UUID championshipUuid, UUID rosterUuid) {
        Championship championship = getChampionshipByUuid(championshipUuid);
        ChampionshipRoster roster = championshipRosterRepository.findByUuid(rosterUuid)
                .orElseThrow(() -> new EntityNotFoundException("Championship roster entry not found: " + rosterUuid));
        if (!roster.getChampionship().getUuid().equals(championship.getUuid())) {
            throw new IllegalArgumentException("Roster entry does not belong to this championship");
        }
        if (!roster.isActive()) {
            throw new IllegalArgumentException("Roster entry is already inactive");
        }
        roster.setActive(false);
        roster.setRemovedDate(LocalDate.now());
        return championshipRosterRepository.save(roster);
    }

    private Team getTeam(UUID uuid) {
        return teamRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + uuid));
    }

    private Team getActiveTeam(UUID uuid) {
        Team team = getTeam(uuid);
        if (!team.isActive()) {
            throw new IllegalArgumentException("Championship team must be active");
        }
        return team;
    }

    private Player getActivePlayer(UUID uuid) {
        Player player = playerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + uuid));
        if (!player.isActive()) {
            throw new IllegalArgumentException("Championship player must be active");
        }
        return player;
    }

    private Trainer getActiveTrainer(UUID uuid) {
        Trainer trainer = trainerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + uuid));
        if (!trainer.isActive()) {
            throw new IllegalArgumentException("Championship trainer must be active");
        }
        return trainer;
    }

    private void ensureActive(Championship championship) {
        if (!championship.isActive()) {
            throw new IllegalArgumentException("Inactive championships cannot be changed");
        }
    }

    private void validatePeriod(Integer startMonth, Integer startYear, Integer endMonth, Integer endYear) {
        if (startMonth == null || endMonth == null || startYear == null || endYear == null) {
            throw new IllegalArgumentException("Championship period is required");
        }
        int startMonthValue = startMonth;
        int startYearValue = startYear;
        int endMonthValue = endMonth;
        int endYearValue = endYear;
        if (startMonthValue < 1 || startMonthValue > 12 || endMonthValue < 1 || endMonthValue > 12) {
            throw new IllegalArgumentException("Championship months must be between 1 and 12");
        }
        if (startYearValue < 2000 || endYearValue < 2000) {
            throw new IllegalArgumentException("Championship years must be 2000 or later");
        }
        if (endYearValue < startYearValue || (endYearValue == startYearValue && endMonthValue < startMonthValue)) {
            throw new IllegalArgumentException("Championship end period must be after the start period");
        }
    }

    private String cleanOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
