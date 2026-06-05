package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;
import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TeamCreateRequest;
import com.clubmanager.dto.TeamUpdateRequest;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TrainerRepository trainerRepository;

    public TeamService(TeamRepository teamRepository, TrainerRepository trainerRepository) {
        this.teamRepository = teamRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public Team createTeam(TeamCreateRequest request) {
        requireText(request.ageGroup(), "ageGroup");
        Trainer trainer = findTrainer(request.trainerUuid());
        Team team = Team.builder()
                .ageGroup(request.ageGroup().trim())
                .teamCategory(request.teamCategory())
                .trainer(trainer)
                .build();
        return teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public Team getTeamByUuid(UUID uuid) {
        return teamRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + uuid));
    }

    @Transactional(readOnly = true)
    public Page<Team> searchTeams(String ageGroup, TeamCategory teamCategory, Pageable pageable) {
        boolean hasAgeGroup = StringUtils.hasText(ageGroup);
        if (hasAgeGroup && teamCategory != null) {
            return teamRepository.findByAgeGroupContainingIgnoreCaseAndTeamCategory(ageGroup.trim(), teamCategory, pageable);
        }
        if (hasAgeGroup) {
            return teamRepository.findByAgeGroupContainingIgnoreCase(ageGroup.trim(), pageable);
        }
        if (teamCategory != null) {
            return teamRepository.findByTeamCategory(teamCategory, pageable);
        }
        return teamRepository.findAll(pageable);
    }

    @Transactional
    public Team updateTeam(UUID uuid, TeamUpdateRequest request) {
        Team team = getTeamByUuid(uuid);

        applyTextUpdate(request.ageGroup(), "ageGroup", team::setAgeGroup);
        applyTeamCategoryUpdate(request.teamCategory(), team::setTeamCategory);
        applyTrainerUpdate(request.trainerUuid(), team::setTrainer);

        return teamRepository.save(team);
    }

    @Transactional
    public Team deactivateTeam(UUID uuid) {
        Team team = getTeamByUuid(uuid);
        team.setActive(false);
        return teamRepository.save(team);
    }

    @Transactional
    public Team reactivateTeam(UUID uuid) {
        Team team = getTeamByUuid(uuid);
        team.setActive(true);
        return teamRepository.save(team);
    }

    private void applyTeamCategoryUpdate(TeamCategory teamCategory, Consumer<TeamCategory> setter) {
        if (teamCategory != null) {
            setter.accept(teamCategory);
        }
    }

    private void applyTrainerUpdate(UUID trainerUuid, Consumer<Trainer> setter) {
        if (trainerUuid != null) {
            setter.accept(findTrainer(trainerUuid));
        }
    }

    private Trainer findTrainer(UUID trainerUuid) {
        return trainerRepository.findByUuid(trainerUuid)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUuid));
    }
}
