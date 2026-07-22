package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.database.entity.omop.VisitOccurrence;
import com.syntheaweb.backend.service.ConceptMappingService;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapping Encounter to VisitOccurrence
 * */
@Component
public class VisitOccurrenceMapper {

    private final ConceptMappingService conceptMappingService;

    public VisitOccurrenceMapper(ConceptMappingService conceptMappingService) {
        this.conceptMappingService = conceptMappingService;
    }

    public VisitOccurrence toVisitOccurrence(
            Encounter encounter,
            Person person) {

        VisitOccurrence visitOccurrence = new VisitOccurrence();

        visitOccurrence.setPerson(person);

        if (encounter.hasPeriod()) {

            if (encounter.getPeriod().hasStart()) {

                LocalDateTime start = encounter.getPeriod().getStart()
                        .toInstant()
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDateTime();

                visitOccurrence.setVisitStartDatetime(start);
                visitOccurrence.setVisitStartDate(start.toLocalDate());
            }

            if (encounter.getPeriod().hasEnd()) {

                LocalDateTime end = encounter.getPeriod().getEnd()
                        .toInstant()
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDateTime();

                visitOccurrence.setVisitEndDatetime(end);
                visitOccurrence.setVisitEndDate(end.toLocalDate());
            }
        }

        if (encounter.hasClass_()) {

            String code = encounter.getClass_().getCode();
            String system = encounter.getClass_().getSystem();

            visitOccurrence.setVisitConceptId(
                    conceptMappingService.resolve(system, code));
        }

        visitOccurrence.setVisitTypeConceptId(
                ConceptMappingService.EHR_RECORD
        );

        return visitOccurrence;
    }
}
