package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;

import com.clubmanager.domain.Trainer;
import com.clubmanager.dto.TrainerCreateRequest;
import com.clubmanager.dto.TrainerUpdateRequest;
import com.clubmanager.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
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
public class TrainerService {

    private final TrainerRepository trainerRepository;



    @Transactional
    public Trainer createTrainer(TrainerCreateRequest request) {
        validateBirthdate(request.birthdate());
        validateMemberSince(request.memberSince());

        Trainer trainer = Trainer.builder()
                .name(request.name())
                .birthCountry(cleanOptionalText(request.birthCountry()))
                .livingCountry(cleanOptionalText(request.livingCountry()))
                .birthdate(request.birthdate())
                .email(cleanOptionalText(request.email()))
                .phone(cleanOptionalText(request.phone()))
                .registerDate(LocalDate.now())
                .memberSince(request.memberSince())
                .build();
        return trainerRepository.save(trainer);
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUuid(UUID uuid) {
        return trainerRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + uuid));
    }

    @Transactional(readOnly = true)
    public Page<Trainer> getAllTrainers(Pageable pageable) {
        return trainerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Trainer> searchTrainers(String name, Pageable pageable) {
        return searchTrainers(name, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Trainer> searchTrainers(String name, Boolean active, Pageable pageable) {
        boolean activeOnly = Boolean.TRUE.equals(active);
        if (!StringUtils.hasText(name)) {
            return activeOnly ? trainerRepository.findAllByActiveTrue(pageable) : getAllTrainers(pageable);
        }
        if (activeOnly) {
            return trainerRepository.findByNameContainingIgnoreCaseAndActiveTrue(name.trim(), pageable);
        }
        return trainerRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    @Transactional
    public Trainer updateTrainer(UUID uuid, TrainerUpdateRequest request) {
        Trainer trainer = getTrainerByUuid(uuid);

        applyTextUpdate(request.name(), "name", trainer::setName);
        applyOptionalTextUpdate(request.birthCountry(), trainer::setBirthCountry);
        applyOptionalTextUpdate(request.livingCountry(), trainer::setLivingCountry);
        applyBirthdateUpdate(request.birthdate(), trainer::setBirthdate);
        applyOptionalTextUpdate(request.email(), trainer::setEmail);
        applyOptionalTextUpdate(request.phone(), trainer::setPhone);
        applyMemberSinceUpdate(request.memberSince(), trainer::setMemberSince);

        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer deactivateTrainer(UUID uuid) {
        Trainer trainer = getTrainerByUuid(uuid);
        trainer.setActive(false);
        return trainerRepository.save(trainer);
    }

    @Transactional
    public Trainer reactivateTrainer(UUID uuid) {
        Trainer trainer = getTrainerByUuid(uuid);
        trainer.setActive(true);
        return trainerRepository.save(trainer);
    }

    private void validateBirthdate(LocalDate birthdate) {
        if (birthdate != null && !birthdate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Birthdate must be in the past");
        }
    }

    private void validateMemberSince(LocalDate memberSince) {
        if (memberSince == null || memberSince.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Member since must not be in the future");
        }
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

    private void applyOptionalTextUpdate(String value, Consumer<String> setter) {
        if (value != null) {
            setter.accept(cleanOptionalText(value));
        }
    }

    private String cleanOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
