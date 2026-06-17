package com.clubmanager.service;

import com.clubmanager.domain.Club;
import com.clubmanager.domain.ClubSetup;
import com.clubmanager.dto.ClubSetupUpdateRequest;
import com.clubmanager.dto.ClubUpdateRequest;
import com.clubmanager.repository.ClubRepository;
import com.clubmanager.repository.ClubSetupRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubService {

    private static final Pattern HEX_COLOUR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private final ClubRepository clubRepository;
    private final ClubSetupRepository clubSetupRepository;
    private final ObjectMapper objectMapper;



    @Transactional(readOnly = true)
    public Club getClub() {
        return getSingleClub();
    }

    @Transactional
    public Club updateClub(ClubUpdateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Club name must not be blank");
        }
        if (!isValidColour(request.colour1()) || !isValidColour(request.colour2())) {
            throw new IllegalArgumentException("Club colours must use #RRGGBB format");
        }
        Club club = getSingleClub();
        club.setName(request.name());
        club.setDescription(request.description());
        club.setColour1(request.colour1());
        club.setColour2(request.colour2());
        return clubRepository.save(club);
    }

    private boolean isValidColour(String colour) {
        return colour != null && HEX_COLOUR.matcher(colour).matches();
    }

    @Transactional(readOnly = true)
    public List<ClubSetup> getAllSetup() {
        return clubSetupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ClubSetup getSetupByType(String type) {
        return clubSetupRepository.findByType(type)
                .orElseThrow(() -> new EntityNotFoundException("Club setup type not found: " + type));
    }

    @Transactional
    public ClubSetup updateSetup(UUID uuid, ClubSetupUpdateRequest request) {
        validateSetupJson(request.jsonData());
        ClubSetup setup = clubSetupRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Club setup not found: " + uuid));
        setup.setJsonData(request.jsonData());
        return clubSetupRepository.save(setup);
    }

    private Club getSingleClub() {
        List<Club> clubs = clubRepository.findAll();
        if (clubs.size() != 1) {
            throw new IllegalStateException("Expected exactly one club configuration row, found " + clubs.size());
        }
        return clubs.getFirst();
    }

    private void validateSetupJson(String jsonData) {
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            if (!root.isArray()) {
                throw new IllegalArgumentException("Setup values must be a JSON array");
            }
            Set<String> values = new HashSet<>();
            for (JsonNode value : root) {
                if (!value.isTextual() || value.textValue().isBlank()) {
                    throw new IllegalArgumentException("Setup values must be non-blank strings");
                }
                if (!values.add(value.textValue())) {
                    throw new IllegalArgumentException("Setup values must be unique");
                }
            }
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Setup values must be valid JSON", exception);
        }
    }
}
