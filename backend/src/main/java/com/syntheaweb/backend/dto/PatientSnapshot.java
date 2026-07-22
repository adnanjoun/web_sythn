package com.syntheaweb.backend.dto;

import java.util.List;

public record PatientSnapshot(
        String demographics,
        List<String> activeConditions,
        List<String> currentMedications,
        List<String> recentEncountersAndProcedures,
        List<String> activeAllergies,
        List<String> recentObservations,
        List<String> immunizations
) {
    public String toLlmPromptString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("--- PATIENT CLINICAL SNAPSHOT ---\n\n");
        
        sb.append("[1. Demographics]\n");
        sb.append(demographics != null && !demographics.isBlank() ? demographics : "Keine demografischen Daten vorhanden.");
        sb.append("\n\n");

        sb.append("[2. Active Diagnoses and Conditions]\n");
        appendList(sb, activeConditions);

        sb.append("\n[3. Current Medications]\n");
        appendList(sb, currentMedications);

        sb.append("\n[4. Recent Encounters and Procedures]\n");
        appendList(sb, recentEncountersAndProcedures);

        sb.append("\n[5. Relevant Allergies]\n");
        appendList(sb, activeAllergies);

        sb.append("\n[6. Recent Laboratory Observations and Vitals]\n");
        appendList(sb, recentObservations);

        sb.append("\n[7. Immunization History]\n");
        appendList(sb, immunizations);
        
        sb.append("\n--- END OF RECORD ---");

        return sb.toString();
    }

    private void appendList(StringBuilder sb, List<String> items) {
        if (items == null || items.isEmpty()) {
            sb.append("- None documented in the provided record.\n");
        } else {
            for (String item : items) {
                sb.append("- ").append(item).append("\n");
            }
        }
    }
}