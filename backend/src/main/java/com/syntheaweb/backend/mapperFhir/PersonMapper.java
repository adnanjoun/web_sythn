package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptMappingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Mapping Patient to Person
 * */
@Component
public class PersonMapper {

    private final ConceptMappingService conceptMappingService;

    public PersonMapper(ConceptMappingService conceptMappingService) {
        this.conceptMappingService = conceptMappingService;
    }

    public Person toPerson(org.hl7.fhir.r4.model.Patient patient) {
        Person person = new Person();

        if (patient.getBirthDate() != null) {
            LocalDate date = patient.getBirthDate().toInstant()
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();

            person.setYearOfBirth(date.getYear());
            person.setMonthOfBirth(date.getMonthValue());
            person.setDayOfBirth(date.getDayOfMonth());
        }

        if (patient.hasGender()) {
            person.setGenderConceptId(conceptMappingService.mapGender(patient.getGender().toCode()));
        }

        return person;
    }
}
