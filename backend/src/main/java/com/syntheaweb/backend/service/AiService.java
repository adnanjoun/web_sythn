package com.syntheaweb.backend.service;

import com.syntheaweb.backend.database.entity.AiPatientSummary;
import com.syntheaweb.backend.database.entity.Patient;
import com.syntheaweb.backend.database.entity.SummaryStatus;
import com.syntheaweb.backend.database.repository.AiPatientSummaryRepository;
import com.syntheaweb.backend.database.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final RestClient restClient;
    private final AiPatientSummaryRepository summaryRepository;
    private final PatientRepository patientRepository;
    
    @Value("${ai.model.name:MEDGEMMA}")
    private String modelName;

    public AiService(@Value("${ai.service.url}") String aiServiceUrl,
                     AiPatientSummaryRepository summaryRepository,
                     PatientRepository patientRepository) {
        this.restClient = RestClient.builder().baseUrl(aiServiceUrl).build();
        this.summaryRepository = summaryRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public AiPatientSummary createPendingSummary(String patientId, String runId) {
        log.info("Creating pending AI summary for patient: {}", patientId);
        
        Patient patient = patientRepository.findByPatientIdAndRun_RunId(patientId, runId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));
        AiPatientSummary summary = new AiPatientSummary(patient, modelName);
        return summaryRepository.save(summary);
    }

    @Async
    public void generateSummaryAsync(Long summaryId, String patientData, String modelName) {
        log.info("Starting async LLM generation for Summary ID: {}", summaryId);
        
        try {
            String summaryText = callPythonService(patientData, modelName);
            
            summaryRepository.findById(summaryId).ifPresent(summary -> {
                summary.setSummaryText(summaryText);
                summary.setStatus(SummaryStatus.COMPLETED);
                summaryRepository.save(summary);
                log.info("Successfully saved completed summary for ID: {}", summaryId);
            });
            
        } catch (Exception e) {
            log.error("Failed to generate summary via Python Service for ID {}. Reason: {}", summaryId, e.getMessage(), e);
            
            summaryRepository.findById(summaryId).ifPresent(summary -> {
                summary.setStatus(SummaryStatus.FAILED);
                summaryRepository.save(summary);
            });
        }
    }

    public List<AiPatientSummary> getSummariesForPatient(String patientId) {
        return summaryRepository.findByPatient_PatientIdOrderByCreatedAtDesc(patientId);
    }

    private String callPythonService(String patientData, String modelName) {
        log.debug("Sending payload to Python backend for model: {}", modelName);
        
        SummarizeResponse response = restClient.post()
                .uri("/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SummarizeRequest(patientData, modelName))
                .retrieve()
                .body(SummarizeResponse.class);
                
        return response != null ? response.summary() : "No summary available";
    }

    private record SummarizeRequest(String patient_data, String model) {}

    private record SummarizeResponse(String summary) {}
}