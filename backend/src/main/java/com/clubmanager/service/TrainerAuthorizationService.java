package com.clubmanager.service;

import com.clubmanager.domain.Team;
import com.clubmanager.domain.Trainer;
import com.clubmanager.domain.ScheduleType;
import com.clubmanager.repository.ChampionshipRepository;
import com.clubmanager.repository.ScheduleRepository;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("trainerAuthorizationService")
@RequiredArgsConstructor
public class TrainerAuthorizationService {

    private final TrainerRepository trainerRepository;
    private final TeamRepository teamRepository;
    private final ScheduleRepository scheduleRepository;
    private final ChampionshipRepository championshipRepository;

    @Transactional(readOnly = true)
    public Trainer requireCurrentTrainer() {
        return getCurrentTrainer()
                .orElseThrow(() -> new AccessDeniedException("Trainer access required"));
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getCurrentTrainer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !hasRole(authentication, "ROLE_TRAINER")) {
            return Optional.empty();
        }
        return trainerRepository.findByEmailIgnoreCase(authentication.getName())
                .filter(Trainer::isActive);
    }

    @Transactional(readOnly = true)
    public boolean canAccessTeam(UUID teamUuid) {
        return getCurrentTrainer()
                .map(trainer -> teamRepository.findByUuid(teamUuid)
                        .map(team -> isAssignedToTeam(trainer, team))
                        .orElse(false))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessSchedule(UUID scheduleUuid) {
        return getCurrentTrainer()
                .map(trainer -> scheduleRepository.findByUuid(scheduleUuid)
                        .map(schedule -> schedule.getType() == ScheduleType.TRAINING
                                && isAssignedToTeam(trainer, schedule.getTeam()))
                        .orElse(false))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessChampionship(UUID championshipUuid) {
        return getCurrentTrainer()
                .map(trainer -> championshipRepository.findByUuid(championshipUuid)
                        .map(championship -> isAssignedToTeam(trainer, championship.getTeam()))
                        .orElse(false))
                .orElse(false);
    }

    public boolean isAssignedToTeam(Trainer trainer, Team team) {
        return sameTrainer(trainer, team.getTrainer()) || sameTrainer(trainer, team.getSubTrainer());
    }

    public void ensureAssignedToTeam(Trainer trainer, Team team) {
        if (!isAssignedToTeam(trainer, team)) {
            throw new AccessDeniedException("Trainer is not assigned to this team");
        }
    }

    public Team requireTeam(UUID teamUuid) {
        return teamRepository.findByUuid(teamUuid)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamUuid));
    }

    private boolean sameTrainer(Trainer currentTrainer, Trainer teamTrainer) {
        return teamTrainer != null && teamTrainer.getUuid().equals(currentTrainer.getUuid());
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
