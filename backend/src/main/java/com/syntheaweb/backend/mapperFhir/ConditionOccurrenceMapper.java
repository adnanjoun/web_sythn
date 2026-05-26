package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptMappingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapping Condition to ConditionOccurrence
 * */
@Component
public class ConditionOccurrenceMapper {

    private final ConceptMappingService conceptMappingService;

    public ConditionOccurrenceMapper(ConceptMappingService conceptMappingService) {
        this.conceptMappingService = conceptMappingService;
    }

    public ConditionOccurrence toConditionOccurrence(org.hl7.fhir.r4.model.Condition condition, Person person) {
        ConditionOccurrence conditionOccurrence = new ConditionOccurrence();

        conditionOccurrence.setPerson(person);

        if (condition.getOnsetDateTimeType() != null) {

            LocalDateTime dateTime = condition.getOnsetDateTimeType()
                    .getValue()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime();

            conditionOccurrence.setConditionStartDatetime(dateTime);
            conditionOccurrence.setConditionStartDate(dateTime.toLocalDate());
        } else if (condition.getRecordedDate() != null) {
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

            conditionOccurrence.setConditionConceptId(
                    conceptMappingService.resolve(system, code));
        }

        conditionOccurrence.setConditionTypeConceptId(32817);
        //TODO map provide_id, Visit_occurrence_id

        return conditionOccurrence;
    }
}
