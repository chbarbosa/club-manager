package com.clubmanager.service;

import com.clubmanager.domain.ClubField;
import com.clubmanager.repository.ClubFieldRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubFieldService {

    private final ClubFieldRepository clubFieldRepository;

    public ClubFieldService(ClubFieldRepository clubFieldRepository) {
        this.clubFieldRepository = clubFieldRepository;
    }

    @Transactional(readOnly = true)
    public List<ClubField> getActiveFields() {
        return clubFieldRepository.findByActiveTrueOrderByNameAsc();
    }
}
