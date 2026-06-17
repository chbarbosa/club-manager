package com.clubmanager.controller;

import com.clubmanager.service.ReportExportService;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportExportController {

    private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private final ReportExportService reportExportService;



    @GetMapping("/players.csv")
    public ResponseEntity<String> exportPlayers() {
        return csvResponse(reportExportService.exportPlayersCsv(), "players.csv");
    }

    @GetMapping("/teams/{teamUuid}/roster.csv")
    public ResponseEntity<String> exportTeamRoster(@PathVariable UUID teamUuid) {
        return csvResponse(reportExportService.exportTeamRosterCsv(teamUuid), "team-roster.csv");
    }

    @GetMapping("/schedules.csv")
    public ResponseEntity<String> exportSchedules() {
        return csvResponse(reportExportService.exportSchedulesCsv(), "schedules.csv");
    }

    @GetMapping("/championships.csv")
    public ResponseEntity<String> exportChampionships() {
        return csvResponse(reportExportService.exportChampionshipsCsv(), "championships.csv");
    }

    @GetMapping("/evaluations/{evaluationUuid}/results.csv")
    public ResponseEntity<String> exportEvaluationResults(@PathVariable UUID evaluationUuid) {
        return csvResponse(reportExportService.exportEvaluationResultsCsv(evaluationUuid), "evaluation-results.csv");
    }

    @GetMapping("/teams/{teamUuid}/matches/{matchUuid}/analysis.csv")
    public ResponseEntity<String> exportMatchAnalysis(
            @PathVariable UUID teamUuid,
            @PathVariable UUID matchUuid) {
        return csvResponse(reportExportService.exportMatchAnalysisCsv(teamUuid, matchUuid), "match-analysis.csv");
    }

    private ResponseEntity<String> csvResponse(String csv, String filename) {
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename)
                        .build()
                        .toString())
                .body(csv);
    }
}
