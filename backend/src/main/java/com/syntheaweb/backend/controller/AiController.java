package com.syntheaweb.backend.controller;

import com.syntheaweb.backend.database.entity.AiPatientSummary;
import com.syntheaweb.backend.dto.AiPatientSummaryDto;
import com.syntheaweb.backend.service.AiService;
import com.syntheaweb.backend.service.FhirExtractionService;
import com.syntheaweb.backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private FhirExtractionService fhirExtractionService;

    public record BulkGenerateRequest(String runId, String patientId) {}

    @PostMapping("/summaries/generate")
    public ResponseEntity<AiPatientSummaryDto> generateSummary(
            @RequestParam String runId,
            @RequestParam String patientId) {
        try {
            final String rawFhir = storageService.readPatientFile(runId, patientId);
            if (rawFhir == null)
                return ResponseEntity.notFound().build();
                
            final AiPatientSummary summary = aiService.createPendingSummary(patientId, runId);
            final String patientData = fhirExtractionService.extractSnapshot(rawFhir, summary.getPatient()).toLlmPromptString();
            aiService.generateSummaryAsync(summary.getId(), patientData, summary.getModelName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(summary));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/summaries/generate/bulk")
    public ResponseEntity<List<AiPatientSummaryDto>> generateSummariesBulk(
            @RequestBody List<BulkGenerateRequest> requests) {
        List<AiPatientSummaryDto> created = new ArrayList<>();
        for (BulkGenerateRequest req : requests) {
            try {
                String rawFhir = storageService.readPatientFile(req.runId(), req.patientId());
                if (rawFhir == null) continue;
                
                AiPatientSummary summary = aiService.createPendingSummary(req.patientId(), req.runId());
                String patientData = fhirExtractionService.extractSnapshot(rawFhir, summary.getPatient()).toLlmPromptString();
                aiService.generateSummaryAsync(summary.getId(), patientData, summary.getModelName());
                
                created.add(toDto(summary));
            } catch (IOException e) {
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<AiPatientSummaryDto>> getSummaries(@RequestParam String patientId) {
        final List<AiPatientSummaryDto> summaries = aiService.getSummariesForPatient(patientId)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(summaries);
    }

    private AiPatientSummaryDto toDto(AiPatientSummary s) {
        return new AiPatientSummaryDto(s.getId(), s.getStatus(), s.getModelName(), s.getSummaryText(),
                s.getCreatedAt());
    }
}