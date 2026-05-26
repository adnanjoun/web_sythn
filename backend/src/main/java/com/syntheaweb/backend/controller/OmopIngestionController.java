package com.syntheaweb.backend.controller;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.User;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.database.repository.RunRepository;
import com.syntheaweb.backend.service.FhirService;
import com.syntheaweb.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@RequestMapping("/omop")
public class OmopIngestionController {

    @Autowired
    private final FhirService fhirService;

    /**For the Run Id generation*/
    //TODO change later, test with frontend
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

    /*@PostMapping("/test/bundle")
    public String test(@RequestBody String json) {
        Bundle bundle = fhirService.parseBundle(json);
        return "Entries: " + bundle.getEntry().size();
    }*/
    /*@PostMapping("/bundle")
    public String processBundle(@RequestBody String json) {
        fhirService.processBundle(json);
        return "Bundle processed successfully";
    }*/
    @PostMapping("/bundle")
    public String processBundle(@RequestBody String json, Principal principal) {

        String runId = UUID.randomUUID().toString();

        User user = userService.getUserByPrincipal(principal);

        Run run = new Run(
                runId,
                user,
                LocalDateTime.now(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        runRepository.save(run);

        //TODO: change later to Run run = runService.createRun(user); fhirService.processBundle(json, run);
        fhirService.processBundle(json, runId);

        return runId;
    }
    /**
     * only for testing
     */
    @PostMapping("/test/patient")
    public Person testPatient(@RequestBody String json) {
        return fhirService.parseAndMapPatient(json);
    }

    @GetMapping("/persons/{id}")
    public Person getPersonById(@PathVariable Long id) {
        return fhirService.findPersonById(id);
    }
}