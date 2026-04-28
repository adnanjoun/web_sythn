package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.Measurement;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptService;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Mapping Observation to Measurement
 * */
@Component
public class MeasurementMapper {
    private final ConceptService conceptService;

    public MeasurementMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public Measurement toMeasurement(Observation observation, Person person) {
        Measurement measurement = new Measurement();

        measurement.setPerson(person);

        // Date
        if (observation.getEffectiveDateTimeType() != null) {
            LocalDate date = observation.getEffectiveDateTimeType()
                    .getValue()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();

            measurement.setMeasurementDate(date);
        }

        // Concept mapping
        if (observation.hasCode() && observation.getCode().hasCoding()) {
            var coding = observation.getCode().getCodingFirstRep();

            String code = coding.getCode();
            String system = coding.getSystem();

            measurement.setMeasurementConceptId(
                    conceptService.mapObservation(code, system)
            );

            measurement.setMeasurementSourceValue(code);
        }

        // Value
        if (observation.hasValueQuantity()) {
            measurement.setValueAsNumber(
                    observation.getValueQuantity().getValue().doubleValue()
            );
        }

        return measurement;
    }
}
