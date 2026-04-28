package com.syntheaweb.backend.service;

import org.springframework.stereotype.Service;

@Service
public class ConceptService {

    //hardcoded mappings
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

    //hardcoded mappings
    public int mapGender(String gender) {
        return switch (gender) {
            case "male" -> 8507;
            case "female" -> 8532;
            case "other" -> 8551;
            default -> 0;
        };
    }
}
