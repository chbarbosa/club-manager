package com.clubmanager.controller;

import com.clubmanager.dto.ClubAnalysisResponse;
import com.clubmanager.dto.PageResponse;
import com.clubmanager.mapper.ClubAnalysisMapper;
import com.clubmanager.service.ClubAnalysisService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/club-analysis")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ClubAnalysisController {

    private final ClubAnalysisService clubAnalysisService;
    private final ClubAnalysisMapper clubAnalysisMapper;

    @GetMapping("/current")
    public ClubAnalysisResponse getCurrentAnalysis() {
        return clubAnalysisMapper.toResponse(clubAnalysisService.getCurrentAnalysis());
    }

    @GetMapping
    public PageResponse<ClubAnalysisResponse> getAnalysisHistory(Pageable pageable) {
        return PageResponse.from(clubAnalysisService.getAnalysisHistory(pageable)
                .map(clubAnalysisMapper::toResponse));
    }

    @GetMapping("/{uuid}")
    public ClubAnalysisResponse getAnalysisByUuid(@PathVariable UUID uuid) {
        return clubAnalysisMapper.toResponse(clubAnalysisService.getAnalysisByUuid(uuid));
    }
}
