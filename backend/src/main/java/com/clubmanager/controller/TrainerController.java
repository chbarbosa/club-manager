package com.clubmanager.controller;

import com.clubmanager.dto.PageResponse;
import com.clubmanager.dto.TrainerCreateRequest;
import com.clubmanager.dto.TrainerResponse;
import com.clubmanager.dto.TrainerSummaryResponse;
import com.clubmanager.dto.TrainerUpdateRequest;
import com.clubmanager.mapper.TrainerMapper;
import com.clubmanager.service.TrainerService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainers")
@PreAuthorize("hasRole('ADMIN')")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainerMapper trainerMapper;

    public TrainerController(TrainerService trainerService, TrainerMapper trainerMapper) {
        this.trainerService = trainerService;
        this.trainerMapper = trainerMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerResponse createTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        return trainerMapper.toResponse(trainerService.createTrainer(request));
    }

    @GetMapping
    public PageResponse<TrainerSummaryResponse> getAllTrainers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        return PageResponse.from(trainerService.searchTrainers(name, active, pageable)
                .map(trainerMapper::toSummaryResponse));
    }

    @GetMapping("/{uuid}")
    public TrainerResponse getTrainerByUuid(@PathVariable UUID uuid) {
        return trainerMapper.toResponse(trainerService.getTrainerByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public TrainerResponse updateTrainer(@PathVariable UUID uuid, @Valid @RequestBody TrainerUpdateRequest request) {
        return trainerMapper.toResponse(trainerService.updateTrainer(uuid, request));
    }

    @PatchMapping("/{uuid}/deactivate")
    public TrainerResponse deactivateTrainer(@PathVariable UUID uuid) {
        return trainerMapper.toResponse(trainerService.deactivateTrainer(uuid));
    }

    @PatchMapping("/{uuid}/reactivate")
    public TrainerResponse reactivateTrainer(@PathVariable UUID uuid) {
        return trainerMapper.toResponse(trainerService.reactivateTrainer(uuid));
    }
}
