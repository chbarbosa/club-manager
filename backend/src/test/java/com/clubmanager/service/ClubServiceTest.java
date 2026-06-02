package com.clubmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clubmanager.domain.Club;
import com.clubmanager.domain.ClubSetup;
import com.clubmanager.dto.ClubSetupUpdateRequest;
import com.clubmanager.dto.ClubUpdateRequest;
import com.clubmanager.repository.ClubRepository;
import com.clubmanager.repository.ClubSetupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubSetupRepository clubSetupRepository;

    private ClubService clubService;

    @BeforeEach
    void setUp() {
        clubService = new ClubService(
                clubRepository,
                clubSetupRepository,
                new ObjectMapper()
        );
    }

    @Test
    void getClub_WhenClubExists_ReturnsClub() {
        Club club = club();
        when(clubRepository.findAll()).thenReturn(List.of(club));

        assertThat(clubService.getClub()).isEqualTo(club);
    }

    @Test
    void getClub_WhenClubIsMissing_ThrowsException() {
        when(clubRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> clubService.getClub())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly one");
    }

    @Test
    void updateClub_WithValidRequest_UpdatesAndReturnsClub() {
        Club club = club();
        ClubUpdateRequest request = new ClubUpdateRequest("City FC", "Youth club", "#112233", "#AABBCC");
        when(clubRepository.findAll()).thenReturn(List.of(club));
        when(clubRepository.save(club)).thenReturn(club);

        assertThat(clubService.updateClub(request)).isEqualTo(club);
        assertThat(club.getName()).isEqualTo("City FC");
        verify(clubRepository).save(club);
    }

    @Test
    void updateClub_WithInvalidColour_ThrowsValidationException() {
        ClubUpdateRequest request = new ClubUpdateRequest("City FC", null, "blue", "#AABBCC");

        assertThatThrownBy(() -> clubService.updateClub(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("#RRGGBB");
    }

    @Test
    void getSetupByType_WhenExists_ReturnsSetup() {
        ClubSetup setup = setup();
        when(clubSetupRepository.findByType("EVALUATION_LEVEL")).thenReturn(Optional.of(setup));

        assertThat(clubService.getSetupByType("EVALUATION_LEVEL")).isEqualTo(setup);
    }

    @Test
    void getSetupByType_WhenMissing_ThrowsNotFound() {
        when(clubSetupRepository.findByType("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.getSetupByType("UNKNOWN"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateSetup_WithValidJson_UpdatesSuccessfully() {
        ClubSetup setup = setup();
        when(clubSetupRepository.findByUuid(setup.getUuid())).thenReturn(Optional.of(setup));
        when(clubSetupRepository.save(setup)).thenReturn(setup);

        assertThat(clubService.updateSetup(
                setup.getUuid(),
                new ClubSetupUpdateRequest("[\"Beginner\"]")
        )).isEqualTo(setup);
        assertThat(setup.getJsonData()).isEqualTo("[\"Beginner\"]");
        assertThat(setup.getType()).isEqualTo("EVALUATION_LEVEL");
    }

    @Test
    void updateSetup_WithMalformedJson_ThrowsValidationException() {
        assertInvalidSetup("[");
    }

    @Test
    void updateSetup_WithDuplicateValue_ThrowsValidationException() {
        assertInvalidSetup("[\"Advanced\",\"Advanced\"]");
    }

    @Test
    void updateSetup_WithBlankValue_ThrowsValidationException() {
        assertInvalidSetup("[\"Advanced\", \" \"]");
    }

    @Test
    void updateSetup_WithNonStringValue_ThrowsValidationException() {
        assertInvalidSetup("[\"Advanced\", 2]");
    }

    private void assertInvalidSetup(String jsonData) {
        assertThatThrownBy(() -> clubService.updateSetup(
                UUID.randomUUID(),
                new ClubSetupUpdateRequest(jsonData)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private Club club() {
        Club club = new Club();
        club.setName("My Club");
        club.setDescription("Configure your club.");
        club.setColour1("#2d2d2d");
        club.setColour2("#f0f0f0");
        return club;
    }

    private ClubSetup setup() {
        ClubSetup setup = new ClubSetup();
        setup.setType("EVALUATION_LEVEL");
        setup.setJsonData("[\"Debutant\", \"Advanced\", \"Skilled\"]");
        return setup;
    }

}
