package com.clubmanager.service;

import static com.clubmanager.service.ServiceDataHelper.applyTextUpdate;
import static com.clubmanager.service.ServiceDataHelper.requireText;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.EvaluationCreateRequest;
import com.clubmanager.dto.EvaluationUpdateRequest;
import com.clubmanager.repository.EvaluationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public EvaluationService(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public Evaluation createEvaluation(EvaluationCreateRequest request) {
        requireText(request.title(), "title");
        requireText(request.ageGroup(), "ageGroup");

        Evaluation evaluation = Evaluation.builder()
                .title(request.title().trim())
                .ageGroup(request.ageGroup().trim())
                .teamCategory(request.teamCategory())
                .createdDate(LocalDate.now())
                .build();
        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public Evaluation getEvaluationByUuid(UUID uuid) {
        return evaluationRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Evaluation not found: " + uuid));
    }

    @Transactional(readOnly = true)
    public Page<Evaluation> searchEvaluations(
            String ageGroup, TeamCategory teamCategory, EvaluationStatus status, Pageable pageable) {
        boolean hasAgeGroup = ageGroup != null && !ageGroup.isBlank();
        if (hasAgeGroup && teamCategory != null && status != null) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCaseAndTeamCategoryAndStatus(
                    ageGroup.trim(), teamCategory, status, pageable);
        }
        if (hasAgeGroup && teamCategory != null) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCaseAndTeamCategory(
                    ageGroup.trim(), teamCategory, pageable);
        }
        if (hasAgeGroup && status != null) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCaseAndStatus(ageGroup.trim(), status, pageable);
        }
        if (teamCategory != null && status != null) {
            return evaluationRepository.findByTeamCategoryAndStatus(teamCategory, status, pageable);
        }
        if (hasAgeGroup) {
            return evaluationRepository.findByAgeGroupContainingIgnoreCase(ageGroup.trim(), pageable);
        }
        if (teamCategory != null) {
            return evaluationRepository.findByTeamCategory(teamCategory, pageable);
        }
        if (status != null) {
            return evaluationRepository.findByStatus(status, pageable);
        }
        return evaluationRepository.findAll(pageable);
    }

    @Transactional
    public Evaluation updateEvaluation(UUID uuid, EvaluationUpdateRequest request) {
        Evaluation evaluation = getEvaluationByUuid(uuid);
        ensureNotFinalized(evaluation);

        applyTextUpdate(request.title(), "title", evaluation::setTitle);
        applyTextUpdate(request.ageGroup(), "ageGroup", evaluation::setAgeGroup);
        if (request.teamCategory() != null) {
            evaluation.setTeamCategory(request.teamCategory());
        }

        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public Evaluation startEvaluation(UUID uuid) {
        Evaluation evaluation = getEvaluationByUuid(uuid);
        if (evaluation.getStatus() != EvaluationStatus.OPEN) {
            throw new IllegalArgumentException("Only open evaluations can be started");
        }
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        return evaluationRepository.save(evaluation);
    }

    @Transactional
    public Evaluation finalizeEvaluation(UUID uuid) {
        Evaluation evaluation = getEvaluationByUuid(uuid);
        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new IllegalArgumentException("Evaluation is already finalized");
        }
        evaluation.setStatus(EvaluationStatus.FINALIZED);
        return evaluationRepository.save(evaluation);
    }

    private void ensureNotFinalized(Evaluation evaluation) {
        if (evaluation.getStatus() == EvaluationStatus.FINALIZED) {
            throw new IllegalArgumentException("Finalized evaluations cannot be updated");
        }
    }
}
