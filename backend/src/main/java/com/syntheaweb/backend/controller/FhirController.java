package com.syntheaweb.backend.controller;

import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.FhirService;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/omop")
public class FhirController {

    @Autowired
    private final FhirService fhirService;


    public FhirController(FhirService fhirService) {
        this.fhirService = fhirService;
    }

    /*@PostMapping("/test/bundle")
    public String test(@RequestBody String json) {
        Bundle bundle = fhirService.parseBundle(json);
        return "Entries: " + bundle.getEntry().size();
    }*/
    @PostMapping("/bundle")
    public String processBundle(@RequestBody String json) {
        fhirService.processBundle(json);
        return "Bundle processed successfully";
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