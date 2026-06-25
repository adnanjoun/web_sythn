package com.syntheaweb.backend.controller;


import com.syntheaweb.backend.database.repository.RunRepository;
import com.syntheaweb.backend.service.FhirService;
import com.syntheaweb.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/omop")
public class OmopIngestionController {

    @Autowired
    private final FhirService fhirService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final RunRepository runRepository;


    public OmopIngestionController(FhirService fhirService,
                                   UserService userService,
                                   RunRepository runRepository) {
        this.fhirService = fhirService;
        this.userService = userService;
        this.runRepository = runRepository;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processRun(
            @RequestParam String runId
    ) {

        try {

            fhirService.processRunSafely(runId);

            return ResponseEntity.ok("OMOP conversion successful");

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("OMOP conversion failed: " + e.getMessage());
        }
    }
}