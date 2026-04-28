package com.syntheaweb.backend.service;

import org.springframework.stereotype.Service;

@Service
public class ConceptService {

    //hardcoded mappings gender concept id in Person
    public int mapGender(String gender) {
        return switch (gender) {
            case "male" -> 8507;
            case "female" -> 8532;
            case "other" -> 8551;
            default -> 0;
        };
    }

    //hardcoded mappings for condition concept id in Condition Occurrence
    public int mapCondition(String code, String system) {
        if ("http://snomed.info/sct".equals(system)) {
            return switch (code) {
                case "44054006" -> 201826; // Diabetes mellitus
                case "38341003" -> 319835; // Hypertension
                default -> 0;
            };
        } else{
            return 0;
        }
    }

    //hardcoded mappings for measurement Concept id in Measurement
    public int mapObservation(String code, String system) {
        if ("http://loinc.org".equals(system)) {
            return switch (code) {
                case "8867-4" -> 3027018; // Heart rate
                case "8480-6" -> 3004249; // Systolic BP
                case "8462-4" -> 3012888; // Diastolic BP
                default -> 0;
            };
        }
        return 0;
    }
}
