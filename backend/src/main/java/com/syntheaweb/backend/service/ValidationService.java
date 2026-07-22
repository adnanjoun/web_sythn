package com.syntheaweb.backend.service;

import org.hl7.fhir.r4.model.*;
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

    public boolean validateEncounter(Encounter encounter){

        if (!encounter.hasSubject()) {
            log.warn("Encounter {} missing subject reference", encounter.getId());
            return false;
        }

        if (!encounter.hasClass_()) {
            log.warn("Encounter {} missing encounter class", encounter.getId());
            return false;
        }

        if (!encounter.hasPeriod()
                || !encounter.getPeriod().hasStart()) {

            log.warn("Encounter {} missing period start", encounter.getId());
            return false;
        }

        return true;
    }

    public boolean validateMedicationRequest(MedicationRequest medicationRequest) {

        if (!medicationRequest.hasSubject()) {
            log.warn(
                    "MedicationRequest {} missing subject reference",
                    medicationRequest.getId()
            );
            return false;
        }

        if (!medicationRequest.hasMedicationCodeableConcept()) {
            log.warn(
                    "MedicationRequest {} missing medicationCodeableConcept",
                    medicationRequest.getId()
            );
            return false;
        }

        if (!medicationRequest.getMedicationCodeableConcept().hasCoding()) {
            log.warn(
                    "MedicationRequest {} missing medication coding",
                    medicationRequest.getId()
            );
            return false;
        }

        return true;
    }
}
