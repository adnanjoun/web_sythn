package com.syntheaweb.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntheaweb.backend.database.entity.Patient;
import com.syntheaweb.backend.dto.PatientSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class FhirExtractionService {

    private static final Logger log = LoggerFactory.getLogger(FhirExtractionService.class);
    private final ObjectMapper mapper = new ObjectMapper();

    public PatientSnapshot extractSnapshot(String rawFhirJson, Patient dbPatient) {
        try {
            JsonNode root = mapper.readTree(rawFhirJson);
            JsonNode entries = root.path("entry");

            String demographics = String.format("Name: %s, Age: %d, Gender: %s, Location: %s",
                    dbPatient.getName(), dbPatient.getAge(), dbPatient.getGender(), dbPatient.getLocation());

            Set<String> conditions = new LinkedHashSet<>();
            Set<String> medications = new LinkedHashSet<>();
            List<String> encountersAndProcedures = new ArrayList<>();
            Set<String> allergies = new LinkedHashSet<>();
            List<String> observations = new ArrayList<>();
            Set<String> immunizations = new LinkedHashSet<>();

            for (JsonNode entry : entries) {
                JsonNode resource = entry.path("resource");
                String resourceType = resource.path("resourceType").asText("");

                switch (resourceType) {
                    case "Condition":
                        String condStr = parseCondition(resource);
                        if (condStr != null) conditions.add(condStr);
                        break;
                    case "MedicationRequest":
                        String medStr = parseMedicationRequest(resource);
                        if (medStr != null) medications.add(medStr);
                        break;
                    case "Encounter":
                    case "Procedure":
                        String encProcStr = parseEncounterOrProcedure(resource);
                        if (encProcStr != null) encountersAndProcedures.add(encProcStr);
                        break;
                    case "AllergyIntolerance":
                        String allergyStr = parseAllergy(resource);
                        if (allergyStr != null) allergies.add(allergyStr);
                        break;
                    case "Observation":
                        String obsStr = parseObservation(resource);
                        if (obsStr != null) observations.add(obsStr);
                        break;
                    case "Immunization":
                        String immStr = parseImmunization(resource);
                        if (immStr != null) immunizations.add(immStr);
                        break;
                }
            }

            //for now limited to 20 entries per category
            return new PatientSnapshot(
                    demographics,
                    limitList(new ArrayList<>(conditions), 20),
                    limitList(new ArrayList<>(medications), 20),
                    limitList(encountersAndProcedures, 20),
                    limitList(new ArrayList<>(allergies), 20),
                    limitList(observations, 20),
                    limitList(new ArrayList<>(immunizations), 20)
            );

        } catch (Exception e) {
            log.error("Fehler beim Parsen der FHIR-Daten: {}", e.getMessage(), e);
            return new PatientSnapshot("Parsing Error", List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }
    }

    private List<String> limitList(List<String> list, int max) {
        if (list.size() > max) {
            return new ArrayList<>(list.subList(list.size() - max, list.size()));
        }
        return new ArrayList<>(list);
    }
    private String parseCondition(JsonNode resource) {
        String status = resource.path("clinicalStatus").path("coding").path(0).path("code").asText("");
        if (!"active".equals(status)) return null;

        String display = extractDisplay(resource.path("code"));
        String date = resource.path("onsetDateTime").asText("unknown date");
        if (date.length() > 4) date = date.substring(0, 4);
        return String.format("%s (Onset: %s)", display, date);
    }

    private String parseMedicationRequest(JsonNode resource) {
        String status = resource.path("status").asText("");
        if (!"active".equals(status)) return null;
        return extractDisplay(resource.path("medicationCodeableConcept"));
    }

    private String parseEncounterOrProcedure(JsonNode resource) {
        String type = resource.path("resourceType").asText();
        String status = resource.path("status").asText("");
        
        if ("cancelled".equals(status) || "entered-in-error".equals(status) || "not-done".equals(status)) {
            return null;
        }

        String display = extractDisplay(resource.path(type.equals("Encounter") ? "type" : "code"));
        String date = type.equals("Encounter") 
                ? resource.path("period").path("start").asText("unknown date")
                : resource.path("performedDateTime").asText("unknown date");
        
        if (date.length() > 10) date = date.substring(0, 10);
        return String.format("[%s] %s (Date: %s)", type, display, date);
    }

    private String parseAllergy(JsonNode resource) {
        String status = resource.path("clinicalStatus").path("coding").path(0).path("code").asText("");
        if (!"active".equals(status)) return null;

        String display = extractDisplay(resource.path("code"));
        String criticality = resource.path("criticality").asText("unknown");
        return String.format("%s (Criticality: %s)", display, criticality);
    }

    private String parseObservation(JsonNode resource) {
        String status = resource.path("status").asText("");
        if ("entered-in-error".equals(status) || "cancelled".equals(status)) return null;

        String display = extractDisplay(resource.path("code"));
        String date = resource.path("effectiveDateTime").asText("unknown date");
        if (date.length() > 10) date = date.substring(0, 10);
        
        String valueStr = "No value";
        JsonNode qty = resource.path("valueQuantity");
        if (!qty.isMissingNode()) {
            valueStr = qty.path("value").asText("") + " " + qty.path("unit").asText("");
        } else if (resource.has("valueString")) {
            valueStr = resource.path("valueString").asText("");
        } else if (resource.has("valueCodeableConcept")) {
            valueStr = extractDisplay(resource.path("valueCodeableConcept"));
        }

        return String.format("%s: %s (%s)", display, valueStr, date);
    }

    private String parseImmunization(JsonNode resource) {
        String status = resource.path("status").asText("");
        if ("not-done".equals(status) || "entered-in-error".equals(status)) return null;
        return extractDisplay(resource.path("vaccineCode"));
    }

    private String extractDisplay(JsonNode codeableConcept) {
        if (codeableConcept.isArray()) codeableConcept = codeableConcept.path(0);
        String text = codeableConcept.path("text").asText("");
        if (!text.isEmpty()) return text;
        return codeableConcept.path("coding").path(0).path("display").asText("Unknown Entity");
    }
}