package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Evaluation;
import com.clubmanager.domain.EvaluationStatus;
import com.clubmanager.domain.TeamCategory;
import com.clubmanager.dto.EvaluationCreateRequest;
import com.clubmanager.dto.EvaluationUpdateRequest;
import com.clubmanager.repository.EvaluationRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationService(evaluationRepository);
    }

    @Test
    void createEvaluation_WithValidRequest_ReturnsOpenEvaluation() {
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Evaluation evaluation = evaluationService.createEvaluation(
                new EvaluationCreateRequest("Spring Tryouts", "Under 13", TeamCategory.MASCULINE));

        assertThat(evaluation.getTitle()).isEqualTo("Spring Tryouts");
        assertThat(evaluation.getAgeGroup()).isEqualTo("Under 13");
        assertThat(evaluation.getTeamCategory()).isEqualTo(TeamCategory.MASCULINE);
        assertThat(evaluation.getStatus()).isEqualTo(EvaluationStatus.OPEN);
        assertThat(evaluation.getCreatedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createEvaluation_WithBlankTitle_ThrowsValidationException() {
        assertThatThrownBy(() -> evaluationService.createEvaluation(
                new EvaluationCreateRequest(" ", "Under 13", TeamCategory.MASCULINE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title");
    }

    @Test
    void createEvaluation_WithBlankAgeGroup_ThrowsValidationException() {
        assertThatThrownBy(() -> evaluationService.createEvaluation(
                new EvaluationCreateRequest("Spring Tryouts", " ", TeamCategory.MASCULINE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ageGroup");
    }

    @Test
    void updateEvaluation_WithOpenEvaluation_UpdatesFields() {
        Evaluation evaluation = evaluation();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation updated = evaluationService.updateEvaluation(
                evaluation.getUuid(),
                new EvaluationUpdateRequest("Summer Tryouts", "Under 15", TeamCategory.FEMININE));

        assertThat(updated.getTitle()).isEqualTo("Summer Tryouts");
        assertThat(updated.getAgeGroup()).isEqualTo("Under 15");
        assertThat(updated.getTeamCategory()).isEqualTo(TeamCategory.FEMININE);
        assertThat(updated.getCreatedDate()).isEqualTo(evaluation.getCreatedDate());
    }

    @Test
    void updateEvaluation_WithFinalizedEvaluation_ThrowsValidationException() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.FINALIZED);
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));

        assertThatThrownBy(() -> evaluationService.updateEvaluation(
                evaluation.getUuid(),
                new EvaluationUpdateRequest("Summer Tryouts", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Finalized");
    }

    @Test
    void startEvaluation_WhenOpen_SetsInProgress() {
        Evaluation evaluation = evaluation();
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation started = evaluationService.startEvaluation(evaluation.getUuid());

        assertThat(started.getStatus()).isEqualTo(EvaluationStatus.IN_PROGRESS);
    }

    @Test
    void finalizeEvaluation_WhenInProgress_SetsFinalized() {
        Evaluation evaluation = evaluation();
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        when(evaluationRepository.findByUuid(evaluation.getUuid())).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        Evaluation finalized = evaluationService.finalizeEvaluation(evaluation.getUuid());

        assertThat(finalized.getStatus()).isEqualTo(EvaluationStatus.FINALIZED);
    }

    private Evaluation evaluation() {
        return Evaluation.builder()
                .title("Spring Tryouts")
                .ageGroup("Under 13")
                .teamCategory(TeamCategory.MASCULINE)
                .createdDate(LocalDate.now().minusDays(1))
                .build();
    }
}
