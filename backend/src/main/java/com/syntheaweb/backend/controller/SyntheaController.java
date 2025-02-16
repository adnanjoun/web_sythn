package com.syntheaweb.backend.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.syntheaweb.backend.dto.SyntheaApiResponse;
import com.syntheaweb.backend.dto.SyntheaParameterBody;
import com.syntheaweb.backend.service.SyntheaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Controller for handling requests related to Synthea data generation and downloading
 */
@RestController 
@RequestMapping("/api/synthea")
public class SyntheaController {

    private final SyntheaService syntheaService;

    /**
     * Constructor for SyntheaController.
     *
     * @param syntheaService The service responsible for generating and managing synthetic data.
     */
    public SyntheaController(SyntheaService syntheaService) {
        this.syntheaService = syntheaService;
    }

    /**
     * Generates synthetic patient data based on the provided parameters
     *
     * @param requestBody The parameters for the synthetic data to be generated.
     * @return ResponseEntity containing a success message and the generated run ID, or an error message if the process fails.
     */
    @PostMapping("/generate")
    public ResponseEntity<SyntheaApiResponse> generateSyntheticData(@RequestBody SyntheaParameterBody requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SyntheaApiResponse("User not authenticated!", null));
        }

        // TODO need to save the run here first with runId for concurrency to work
              
        try {
            // Generate and return the generated run ID
            String runID = syntheaService.generateSyntheticData(
                    requestBody.getPopulationSize(),
                    requestBody.getGender(),
                    requestBody.getMinAge(),
                    requestBody.getMaxAge(),
                    requestBody.getState(),
                    requestBody.getCity()
            );

            return ResponseEntity.ok(new SyntheaApiResponse("Generating was successful!", runID));
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SyntheaApiResponse("Error while generating: " + e.getMessage(), null));
        }
    }

    /**
     *  provides download link for the generated synthetic data
     *
     * @param runID  The ID of the syntheaRun run.
     * @param format The format of the synthetic data (CSV or FHIR)
     * @return ResponseEntity containing the requested file or an error message if the process fails.
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadSyntheticData(@RequestParam(name = "runID") String runID,
                                                   @RequestParam(name = "format") String format) {
        try {
            // retrieve the zipped folder based on the requested format
            File zipFolder = getZippedFolder(runID, format);
            if (zipFolder == null) {
                return ResponseEntity.badRequest()
                        .body(new SyntheaApiResponse("Invalid type: " + format, runID));
            }

            // Prepare file download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFolder.getName() + "\"");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new FileSystemResource(zipFolder));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SyntheaApiResponse("RunID not found", runID));
        }
    }

    /**
     * get the zip folder for the requested synthetic data format.
     *
     * @param runID  The ID of the synthearun.
     * @param format The requested format (CSV or FHIR)
     * @return The zip folder containing the synthetic data, or null if the format is invalid.
     * @throws IOException if an error occurs
     */
    private File getZippedFolder(String runID, String format) throws IOException {
         return switch (format.toLowerCase()) {
            case "csv" -> syntheaService.zipCSVFolderIfNeeded(runID);
            case "fhir" -> syntheaService.zipFHIRFolderIfNeeded(runID);
            default -> null;
        };
    }
}
