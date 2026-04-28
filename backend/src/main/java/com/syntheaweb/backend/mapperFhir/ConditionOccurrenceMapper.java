package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Mapping Condition to ConditionOccurrence
 * */
@Component
public class ConditionOccurrenceMapper {

    private final ConceptService conceptService;

    public ConditionOccurrenceMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public ConditionOccurrence toConditionOccurrence(org.hl7.fhir.r4.model.Condition condition, Person person) {
        ConditionOccurrence conditionOccurrence = new ConditionOccurrence();

        conditionOccurrence.setPerson(person);

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

            conditionOccurrence.setConditionConceptId(conceptService.mapCondition(code, system));
        }

        return conditionOccurrence;
    }
}
