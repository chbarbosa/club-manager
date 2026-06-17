package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;
import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Admin;
import com.clubmanager.domain.Team;
import com.clubmanager.domain.TeamAgeCategory;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TeamCreateRequest;
import com.clubmanager.dto.TeamUpdateRequest;
import com.clubmanager.repository.AdminRepository;
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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TrainerRepository trainerRepository;
    private final AdminRepository adminRepository;



    @Transactional
    public Team createTeam(TeamCreateRequest request) {
        requireText(request.identification(), "identification");
        Trainer trainer = findTrainer(request.trainerUuid());
        Team team = Team.builder()
                .ageGroup(request.identification().trim())
                .ageCategory(request.ageCategory())
                .teamCategory(request.teamCategory())
                .trainer(trainer)
                .subTrainer(findOptionalTrainer(request.subTrainerUuid()))
                .assistantAdmin(findOptionalAdmin(request.assistantAdminUuid()))
                .build();
        return teamRepository.save(team);
    }

    @Transactional(readOnly = true)
    public Team getTeamByUuid(UUID uuid) {
        return teamRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + uuid));
    }

    @Transactional(readOnly = true)
    public Page<Team> searchTeams(String identification, TeamCategory teamCategory, Pageable pageable) {
        boolean hasIdentification = StringUtils.hasText(identification);
        if (hasIdentification && teamCategory != null) {
            return teamRepository.findByAgeGroupContainingIgnoreCaseAndTeamCategory(identification.trim(), teamCategory, pageable);
        }
        if (hasIdentification) {
            return teamRepository.findByAgeGroupContainingIgnoreCase(identification.trim(), pageable);
        }
        if (teamCategory != null) {
            return teamRepository.findByTeamCategory(teamCategory, pageable);
        }
        return teamRepository.findAll(pageable);
    }

    @Transactional
    public Team updateTeam(UUID uuid, TeamUpdateRequest request) {
        Team team = getTeamByUuid(uuid);

        applyTextUpdate(request.identification(), "identification", team::setAgeGroup);
        applyAgeCategoryUpdate(request.ageCategory(), team::setAgeCategory);
        applyTeamCategoryUpdate(request.teamCategory(), team::setTeamCategory);
        applyTrainerUpdate(request.trainerUuid(), team::setTrainer);
        if (request.subTrainerUuid() != null) {
            team.setSubTrainer(findOptionalTrainer(request.subTrainerUuid()));
        }
        if (request.assistantAdminUuid() != null) {
            team.setAssistantAdmin(findOptionalAdmin(request.assistantAdminUuid()));
        }

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

    private void applyAgeCategoryUpdate(TeamAgeCategory ageCategory, Consumer<TeamAgeCategory> setter) {
        if (ageCategory != null) {
            setter.accept(ageCategory);
        }
    }

    private void applyTrainerUpdate(UUID trainerUuid, Consumer<Trainer> setter) {
        if (trainerUuid != null) {
            setter.accept(findTrainer(trainerUuid));
        }
    }

    private Trainer findTrainer(UUID trainerUuid) {
        Trainer trainer = trainerRepository.findByUuid(trainerUuid)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUuid));
        if (!trainer.isActive()) {
            throw new IllegalArgumentException("Trainer must be active");
        }
        return trainer;
    }

    private Trainer findOptionalTrainer(UUID trainerUuid) {
        return trainerUuid == null ? null : findTrainer(trainerUuid);
    }

    private Admin findOptionalAdmin(UUID adminUuid) {
        if (adminUuid == null) {
            return null;
        }
        Admin admin = adminRepository.findByUuid(adminUuid)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found: " + adminUuid));
        if (!admin.isActive()) {
            throw new IllegalArgumentException("Administrative assistant must be active");
        }
        return admin;
    }
}
