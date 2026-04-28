package com.syntheaweb.backend.mapperFhir;

import com.syntheaweb.backend.database.entity.omop.Person;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class PatientMapper {

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
            person.setGenderConceptId(mapGender(patient.getGender().toCode()));
        }

        return person;
    }

    //hardcoded mappings
    private int mapGender(String gender) {
        return switch (gender) {
            case "male" -> 8507;
            case "female" -> 8532;
            case "other" -> 8551;
            default -> 0;
        };
    }
}
