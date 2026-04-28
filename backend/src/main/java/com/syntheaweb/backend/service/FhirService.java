package com.syntheaweb.backend.service;

import ca.uhn.fhir.context.FhirContext;
import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import com.syntheaweb.backend.database.entity.omop.Measurement;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.database.repository.omop.ConditionOccurrenceRepository;
import com.syntheaweb.backend.database.repository.omop.MeasurementRepository;
import com.syntheaweb.backend.database.repository.omop.PersonRepository;
import com.syntheaweb.backend.mapperFhir.ConditionOccurrenceMapper;
import com.syntheaweb.backend.mapperFhir.MeasurementMapper;
import com.syntheaweb.backend.mapperFhir.PersonMapper;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FhirService {

    private final PersonMapper personMapper;
    private final ConditionOccurrenceMapper conditionOccurrenceMapper;
    private final MeasurementMapper measurementMapper;

    @Autowired
    private final PersonRepository personRepository;
    @Autowired
    private final ConditionOccurrenceRepository conditionOccurrenceRepository;
    @Autowired
    private final MeasurementRepository measurementRepository;

    private final FhirContext fhirContext = FhirContext.forR4();

    public FhirService(PersonMapper personMapper,
                       PersonRepository personRepository,
                       ConditionOccurrenceMapper conditionOccurrenceMapper,
                       ConditionOccurrenceRepository conditionOccurrenceRepository,
                       MeasurementMapper measurementMapper,
                       MeasurementRepository measurementRepository) {
        this.personMapper = personMapper;
        this.personRepository = personRepository;
        this.conditionOccurrenceMapper = conditionOccurrenceMapper;
        this.conditionOccurrenceRepository = conditionOccurrenceRepository;
        this.measurementMapper = measurementMapper;
        this.measurementRepository = measurementRepository;
    }

    public Bundle parseBundle(String json) {
        FhirContext ctx = FhirContext.forR4();
        return (Bundle) ctx.newJsonParser().parseResource(json);
    }

    public void processBundle(String json) {
        Bundle bundle = parseBundle(json);

        Map<String, Person> patientMap = new HashMap<>();

        //Process Patients
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Patient patient) {
                Person person = personMapper.toPerson(patient);
                person = personRepository.save(person);

                patientMap.put(patient.getIdElement().getIdPart(), person);
                System.out.println("Saved patient with ID: " + patient.getIdElement().getIdPart());
            }
        }

        //Process Conditions
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Condition condition) {

                // Extract patient reference
                String patientId = condition.getSubject()
                        .getReferenceElement()
                        .getIdPart();

                Person person = patientMap.get(patientId);

                if (person == null) {
                    throw new RuntimeException("Person not found for reference: " + patientId);
                }
                System.out.println("Condition subject reference: " + condition.getSubject().getReference());
                System.out.println("Extracted patientId: " + patientId);

                ConditionOccurrence conditionOccurrence =
                        conditionOccurrenceMapper.toConditionOccurrence(condition, person);
                conditionOccurrenceRepository.save(conditionOccurrence);
            }
        }

        //Process Measurement
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

            if (entry.getResource() instanceof Observation observation) {

                // Extract patient reference
                String patientId = observation.getSubject()
                        .getReferenceElement()
                        .getIdPart();

                Person person = patientMap.get(patientId);

                if (person == null) {
                    throw new RuntimeException("Person not found for reference: " + patientId);
                }
                System.out.println("Observation subject reference: " + observation.getSubject().getReference());
                System.out.println("Extracted patientId: " + patientId);

                Measurement measurement = measurementMapper.toMeasurement(observation, person);
                measurementRepository.save(measurement);
            }
        }
    }

    /**
     * Parsing and mapping the person
     * */
    public Person parseAndMapPatient(String json) {
        Patient patient = fhirContext.newJsonParser()
                .parseResource(Patient.class, json);

        Person person = personMapper.toPerson(patient);

        return personRepository.save(person); //SAVING to DB!!!!
    }

    public Person findPersonById(Long id){
        return personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
    }

}