package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.Measurement;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptMappingService;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapping Observation to Measurement
 * */
@Component
public class MeasurementMapper {
    private final ConceptMappingService conceptMappingService;

    public MeasurementMapper(ConceptMappingService conceptMappingService) {
        this.conceptMappingService = conceptMappingService;
    }

    public Measurement toMeasurement(Observation observation, Person person) {
        Measurement measurement = new Measurement();

        measurement.setPerson(person);

        // Datetime + Date
        if (observation.getEffectiveDateTimeType() != null) {

            LocalDateTime dateTime = observation.getEffectiveDateTimeType()
                    .getValue()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime();

            measurement.setMeasurementDatetime(dateTime);
            measurement.setMeasurementDate(dateTime.toLocalDate());
        }

        // Concept mapping
        if (observation.hasCode() && observation.getCode().hasCoding()) {

            var coding = observation.getCode().getCodingFirstRep();

            String code = coding.getCode();
            String system = coding.getSystem();

            measurement.setMeasurementConceptId(
                    conceptMappingService.resolve(system, code)
            );

            measurement.setMeasurementSourceValue(code);
        }

        // Value + Unit
        if (observation.hasValueQuantity()) {

            measurement.setValueAsNumber(
                    observation.getValueQuantity()
                            .getValue()
                            .doubleValue()
            );

            String unitCode = observation.getValueQuantity().getCode();

            measurement.setUnitConceptId(
                    conceptMappingService.mapUnit(unitCode)
            );
        }

        return measurement;
    }
}
