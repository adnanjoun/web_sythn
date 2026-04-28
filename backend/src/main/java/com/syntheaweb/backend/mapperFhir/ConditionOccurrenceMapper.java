package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class ConditionOccurrenceMapper {

    public ConditionOccurrence toConditionOccurrence(org.hl7.fhir.r4.model.Condition condition) {
        ConditionOccurrence conditionOccurrence = new ConditionOccurrence();

        if (condition.getRecordedDate() != null) {
            LocalDate date = condition.getRecordedDate().toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();

            conditionOccurrence.setConditionStartDate(date);
            conditionOccurrence.setConditionEndDate(date);
        }

        if (condition.hasCode() && condition.getCode().hasCoding()) {
            var coding = condition.getCode().getCodingFirstRep();

            String code = coding.getCode();
            String system = coding.getSystem();

            conditionOccurrence.setConditionConceptId(mapCondition(code, system));
        }

        return conditionOccurrence;
    }

    //hardcoded mappings
    private int mapCondition(String code, String system) {
        if ("http://snomed.info/sct".equals(system)) {
            return switch (code) {
                case "44054006" -> 201826; // Diabetes mellitus
                case "38341003" -> 319835; // Hypertension
                default -> 0;
            };
        }

        return 0;
    }
}
