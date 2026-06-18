package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Trainer;
import com.clubmanager.domain.Team;
import com.clubmanager.dto.TrainerCreateRequest;
import com.clubmanager.dto.TrainerUpdateRequest;
import com.clubmanager.repository.TeamRepository;
import com.clubmanager.repository.TrainerRepository;
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
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TeamRepository teamRepository;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(trainerRepository, teamRepository);
    }

    @Test
    void createTrainer_WithValidRequest_SetsRegisterDateToToday() {
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer trainer = trainerService.createTrainer(createRequest(LocalDate.now().minusYears(35)));

        assertThat(trainer.getName()).isEqualTo("Carlos Mendes");
        assertThat(trainer.getRegisterDate()).isEqualTo(LocalDate.now());
        assertThat(trainer.isActive()).isTrue();
    }

    @Test
    void createTrainer_WithFutureMemberSince_ThrowsValidationException() {
        TrainerCreateRequest request = new TrainerCreateRequest(
                "Carlos Mendes",
                null,
                null,
                null,
                "carlos@club.com",
                null,
                LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Member since");
    }

    @Test
    void createTrainer_WithFutureBirthdate_ThrowsValidationException() {
        TrainerCreateRequest request = createRequest(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> trainerService.createTrainer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Birthdate");
    }

    @Test
    void createTrainer_WithNullBirthdate_CreatesSuccessfully() {
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer trainer = trainerService.createTrainer(createRequest(null));

        assertThat(trainer.getBirthdate()).isNull();
        assertThat(trainer.getName()).isEqualTo("Carlos Mendes");
    }

    @Test
    void updateTrainer_WithValidRequest_UpdatesFields() {
        Trainer trainer = trainer();
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                "Ana Mendes",
                "Portugal",
                "Canada",
                LocalDate.now().minusYears(30),
                "ana@club.com",
                "555-1000",
                LocalDate.now().minusYears(2));
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerService.updateTrainer(trainer.getUuid(), request);

        assertThat(updated.getName()).isEqualTo("Ana Mendes");
        assertThat(updated.getBirthCountry()).isEqualTo("Portugal");
        assertThat(updated.getEmail()).isEqualTo("ana@club.com");
        assertThat(updated.getPhone()).isEqualTo("555-1000");
    }

    @Test
    void updateTrainer_RegisterDateIsNeverChanged() {
        Trainer trainer = trainer();
        LocalDate originalRegisterDate = trainer.getRegisterDate();
        TrainerUpdateRequest request = new TrainerUpdateRequest("Ana Mendes", null, null, null, null, null, null);
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerService.updateTrainer(trainer.getUuid(), request);

        assertThat(updated.getRegisterDate()).isEqualTo(originalRegisterDate);
    }

    @Test
    void deactivateTrainer_WhenActive_SetsActiveFalse() {
        Trainer trainer = trainer();
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerService.deactivateTrainer(trainer.getUuid());

        assertThat(updated.isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void reactivateTrainer_WhenInactive_SetsActiveTrue() {
        Trainer trainer = trainer();
        trainer.setActive(false);
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(trainer)).thenReturn(trainer);

        Trainer updated = trainerService.reactivateTrainer(trainer.getUuid());

        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void getAllTrainers_ReturnsPaginatedSummary() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Trainer> page = new PageImpl<>(List.of(trainer()), pageable, 1);
        when(trainerRepository.findAll(pageable)).thenReturn(page);

        Page<Trainer> result = trainerService.getAllTrainers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Carlos Mendes");
    }

    @Test
    void searchTrainers_WithActiveFilter_ReturnsOnlyActiveTrainers() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Trainer> page = new PageImpl<>(List.of(trainer()), pageable, 1);
        when(trainerRepository.findByNameContainingIgnoreCaseAndActiveTrue("carlos", pageable)).thenReturn(page);

        Page<Trainer> result = trainerService.searchTrainers(" carlos ", true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(trainerRepository).findByNameContainingIgnoreCaseAndActiveTrue("carlos", pageable);
    }

    @Test
    void searchTrainers_WithInactiveFilter_ReturnsOnlyInactiveTrainers() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<Trainer> page = new PageImpl<>(List.of(trainer()), pageable, 1);
        when(trainerRepository.findAllByActiveFalse(pageable)).thenReturn(page);

        Page<Trainer> result = trainerService.searchTrainers(null, false, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(trainerRepository).findAllByActiveFalse(pageable);
    }

    @Test
    void getTeamHistory_ReturnsTeamsWhereTrainerIsMainOrAssistant() {
        Trainer trainer = trainer();
        Team team = Team.builder()
                .ageGroup("Under 13 A")
                .trainer(trainer)
                .build();
        when(trainerRepository.findByUuid(trainer.getUuid())).thenReturn(Optional.of(trainer));
        when(teamRepository.findByTrainerOrSubTrainerOrderByAgeGroupAsc(trainer, trainer)).thenReturn(List.of(team));

        List<Team> result = trainerService.getTeamHistory(trainer.getUuid());

        assertThat(result).containsExactly(team);
    }

    private TrainerCreateRequest createRequest(LocalDate birthdate) {
        return new TrainerCreateRequest(
                "Carlos Mendes",
                "Brazil",
                "Brazil",
                birthdate,
                "carlos@club.com",
                "555-0100",
                LocalDate.now().minusYears(5));
    }

    private Trainer trainer() {
        return Trainer.builder()
                .name("Carlos Mendes")
                .birthCountry("Brazil")
                .livingCountry("Brazil")
                .birthdate(LocalDate.now().minusYears(35))
                .email("carlos@club.com")
                .phone("555-0100")
                .registerDate(LocalDate.now().minusDays(10))
                .memberSince(LocalDate.now().minusYears(5))
                .build();
    }
}
