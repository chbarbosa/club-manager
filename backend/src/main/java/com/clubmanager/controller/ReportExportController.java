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

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportExportController {

    private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private final ReportExportService reportExportService;

    public ReportExportController(ReportExportService reportExportService) {
        this.reportExportService = reportExportService;
    }

    @GetMapping("/players.csv")
    public ResponseEntity<String> exportPlayers() {
        return csvResponse(reportExportService.exportPlayersCsv(), "players.csv");
    }

    @GetMapping("/teams/{teamUuid}/roster.csv")
    public ResponseEntity<String> exportTeamRoster(@PathVariable UUID teamUuid) {
        return csvResponse(reportExportService.exportTeamRosterCsv(teamUuid), "team-roster.csv");
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
