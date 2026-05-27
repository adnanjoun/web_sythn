package com.syntheaweb.backend.service;

import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ValidationService {

    private static final Logger log =
            LoggerFactory.getLogger(ValidationService.class);

    public boolean validatePatient(Patient patient) {

        if (patient.getGender() == null) {
            log.warn("Patient {} has no gender", patient.getId());
            return false;
        }

        if (!patient.hasBirthDate()) {
            log.warn("Patient {} missing birth date", patient.getId());
            return false;
        }

        return true;
    }

    public boolean validateObservation(Observation observation) {

        if (!observation.hasCode()) {
            log.warn("Observation {} missing code", observation.getId());
            return false;
        }

        return true;
    }

    public boolean validateCondition(Condition condition){

        if(!condition.hasCode()){
            log.warn("Condition {} missing code", condition.getId());
            return false;
        }

        return true;
    }
}
