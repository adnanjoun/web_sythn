package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.service.ConceptService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Mapping Patient to Person
 * */
@Component
public class PersonMapper {

    private final ConceptService conceptService;

    public PersonMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
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
            person.setGenderConceptId(conceptService.mapGender(patient.getGender().toCode()));
        }

        return person;
    }
}
