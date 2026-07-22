package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.DrugExposure;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptMappingService;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Mapping MedicationRequest to Drug Exposure
 * other FHIR resources like MedicationAdministration, MedicationStatement or MedicationDispense are not mapped here.
 * */
@Component
public class DrugExposureMapper {
    private final ConceptMappingService conceptMappingService;


    public DrugExposureMapper(ConceptMappingService conceptMappingService) {
        this.conceptMappingService = conceptMappingService;
    }

    public DrugExposure toDrugExposure(
            MedicationRequest medicationRequest,
            Person person) {

        DrugExposure drugExposure = new DrugExposure();

        drugExposure.setPerson(person);

        //start date
        if (medicationRequest.hasAuthoredOn()) {

            LocalDateTime start = medicationRequest.getAuthoredOn()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime();

            drugExposure.setDrugExposureStartDatetime(start);
            drugExposure.setDrugExposureStartDate(start.toLocalDate());
        }

        //end date
        if (medicationRequest.hasDispenseRequest()
                && medicationRequest.getDispenseRequest().hasValidityPeriod()
                && medicationRequest.getDispenseRequest()
                .getValidityPeriod()
                .hasEnd()) {

            LocalDateTime end = medicationRequest.getDispenseRequest()
                    .getValidityPeriod()
                    .getEnd()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime();

            drugExposure.setDrugExposureEndDatetime(end);
            drugExposure.setDrugExposureEndDate(end.toLocalDate());

        } else if (medicationRequest.hasAuthoredOn()) {
            // fallback if no end date exists
            LocalDate date = medicationRequest.getAuthoredOn()
                    .toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();

            drugExposure.setDrugExposureEndDate(date);
        }


        if (medicationRequest.hasMedicationCodeableConcept()
                && medicationRequest.getMedicationCodeableConcept().hasCoding()) {

            var coding = medicationRequest.getMedicationCodeableConcept()
                    .getCodingFirstRep();

            String code = coding.getCode();
            String system = coding.getSystem();

            drugExposure.setDrugConceptId(
                    conceptMappingService.resolve(system, code));
        }

        drugExposure.setDrugExposureTypeConceptId(
                ConceptMappingService.PRESCRIPTION_WRITTEN
        );

        // TODO:
        // map provider_id
        // map visit_occurrence_id
        return drugExposure;
    }
}
