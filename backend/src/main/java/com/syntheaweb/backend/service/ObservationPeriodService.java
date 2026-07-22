package com.syntheaweb.backend.service;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.ObservationPeriod;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.database.repository.omop.ObservationPeriodRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ObservationPeriodService {

    private static final int OBSERVATION_PERIOD_TYPE = 44814724;
    private final ObservationPeriodRepository observationPeriodRepository;

    public ObservationPeriodService(ObservationPeriodRepository observationPeriodRepository) {
        this.observationPeriodRepository = observationPeriodRepository;
    }

    public LocalDate extractDateFromResource(Resource resource) {

        if (resource instanceof Condition condition) {

            if (condition.hasOnsetDateTimeType()) {
                return condition.getOnsetDateTimeType()
                        .getValue()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
        }

        if (resource instanceof Observation observation) {

            if (observation.hasEffectiveDateTimeType()) {
                return observation.getEffectiveDateTimeType()
                        .getValue()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
        }

        if (resource instanceof Encounter encounter) {

            if (encounter.hasPeriod()) {
                return encounter.getPeriod()
                        .getStart()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
        }

        if (resource instanceof MedicationRequest medicationRequest) {

            if (medicationRequest.hasAuthoredOn()) {
                return medicationRequest.getAuthoredOn()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
        }

        return null;
    }

    public ObservationPeriod createObservationPeriod(
            Person person,
            Run run,
            LocalDate startDate,
            LocalDate endDate) {

        ObservationPeriod observationPeriod = new ObservationPeriod();

        observationPeriod.setPerson(person);
        observationPeriod.setRun(run);

        observationPeriod.setObservationPeriodStartDate(startDate);
        observationPeriod.setObservationPeriodEndDate(endDate);

        observationPeriod.setPeriodTypeConceptId(OBSERVATION_PERIOD_TYPE);

        return observationPeriodRepository.save(observationPeriod);
    }

}
