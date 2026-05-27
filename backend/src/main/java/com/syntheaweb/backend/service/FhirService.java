package com.syntheaweb.backend.service;

import ca.uhn.fhir.context.FhirContext;
import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.*;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.database.repository.RunRepository;
import com.syntheaweb.backend.database.repository.omop.*;
import com.syntheaweb.backend.mapperFhir.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FhirService {

    private static final Logger log =
            LoggerFactory.getLogger(FhirService.class);

    private final PersonMapper personMapper;
    private final ConditionOccurrenceMapper conditionOccurrenceMapper;
    private final MeasurementMapper measurementMapper;
    private final VisitOccurrenceMapper visitOccurrenceMapper;
    private final DrugExposureMapper drugExposureMapper;

    @Autowired
    private final PersonRepository personRepository;
    @Autowired
    private final ConditionOccurrenceRepository conditionOccurrenceRepository;
    @Autowired
    private final MeasurementRepository measurementRepository;
    @Autowired
    private final RunRepository runRepository;
    @Autowired
    private final VisitOccurrenceRepository visitOccurrenceRepository;
    @Autowired
    private final DrugExposureRepository drugExposureRepository;

    @Autowired
    private final ValidationService validationService;

    private final FhirContext fhirContext = FhirContext.forR4();

    public FhirService(PersonMapper personMapper,
                       PersonRepository personRepository,
                       ConditionOccurrenceMapper conditionOccurrenceMapper,
                       ConditionOccurrenceRepository conditionOccurrenceRepository,
                       MeasurementMapper measurementMapper,
                       MeasurementRepository measurementRepository,
                       RunRepository runRepository,
                       ValidationService validationService,
                       VisitOccurrenceRepository visitOccurrenceRepository,
                       VisitOccurrenceMapper visitOccurrenceMapper,
                       DrugExposureMapper drugExposureMapper,
                       DrugExposureRepository drugExposureRepository) {
        this.personMapper = personMapper;
        this.personRepository = personRepository;
        this.conditionOccurrenceMapper = conditionOccurrenceMapper;
        this.conditionOccurrenceRepository = conditionOccurrenceRepository;
        this.measurementMapper = measurementMapper;
        this.measurementRepository = measurementRepository;
        this.runRepository = runRepository;
        this.validationService = validationService;
        this.visitOccurrenceRepository = visitOccurrenceRepository;
        this.visitOccurrenceMapper = visitOccurrenceMapper;
        this.drugExposureMapper = drugExposureMapper;
        this.drugExposureRepository = drugExposureRepository;
    }

    public Bundle parseBundle(String json) {
        log.info("Starting parsing FHIR bundle.");
        FhirContext ctx = FhirContext.forR4();
        return (Bundle) ctx.newJsonParser().parseResource(json);
    }

    public void processBundle(String json, String runId) {
        Bundle bundle = parseBundle(json);

        Map<String, Person> patientMap = new HashMap<>();

        Run run = runRepository.findById(runId)
                .orElseThrow();

        log.info("Starting processing bundle for run {}", run.getRunId());

        //Process Patients
        log.info("Processing Patient resources");
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Patient patient) {

                //validation
                if (!validationService.validatePatient(patient)) {
                    log.warn("Skipping invalid patient {}", patient.getId());
                    continue;
                }

                Person person = personMapper.toPerson(patient);
                person.setRun(run);

                person = personRepository.save(person);

                log.debug("Assigning run {} to person entity", runId);

                patientMap.put(patient.getIdElement().getIdPart(), person);
                log.debug("Saved patient {}", patient.getIdElement().getIdPart());
            }
        }

        //Process Conditions
        log.info("Processing Condition resources");
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Condition condition) {

                //validation
                if (!validationService.validateCondition(condition)) {
                    log.warn("Skipping invalid condition {}", condition.getId());
                    continue;
                }

                // Extract patient reference
                String patientId = condition.getSubject()
                        .getReferenceElement()
                        .getIdPart();

                Person person = patientMap.get(patientId);

                if (person == null) {
                    log.error(
                            "Referenced person {} not found for condition {}",
                            patientId,
                            condition.getId()
                    );
                    continue;
                }

                log.debug("Resolved patient reference {}", patientId);

                ConditionOccurrence conditionOccurrence =
                        conditionOccurrenceMapper.toConditionOccurrence(condition, person);
                conditionOccurrence.setRun(run);

                log.debug("Assigning run {} to condition occurrence entity", runId);
                conditionOccurrenceRepository.save(conditionOccurrence);
            }
        }

        //Process Observation
        log.info("Processing Observation resources");
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {

            if (entry.getResource() instanceof Observation observation) {

                //validation
                if (!validationService.validateObservation(observation)) {
                    log.warn("Skipping invalid observation {}", observation.getId());
                    continue;
                }

                // Extract patient reference
                String patientId = observation.getSubject()
                        .getReferenceElement()
                        .getIdPart();

                Person person = patientMap.get(patientId);

                if (person == null) {
                    log.error(
                            "Referenced person {} not found for observation {}",
                            patientId,
                            observation.getId()
                    );
                    throw new RuntimeException("Person not found for reference: " + patientId);
                }

                log.debug("Resolved patient reference {}", patientId);

                Measurement measurement = measurementMapper.toMeasurement(observation, person);
                measurement.setRun(run);

                log.debug("Assigning run {} to measurement entity", runId);
                measurementRepository.save(measurement);
            }
        }

        //Process Encounters
        log.info("Processing Encounter resources");
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof Encounter encounter) {

                //validation
                if (!validationService.validateEncounter(encounter)) {
                    log.warn("Skipping invalid encounter {}", encounter.getId());
                    continue;
                }

                // Extract patient reference
                String patientId = encounter.getSubject()
                        .getReferenceElement()
                        .getIdPart();

                Person person = patientMap.get(patientId);

                if (person == null) {
                    log.error(
                            "Referenced person {} not found for encounter {}",
                            patientId,
                            encounter.getId()
                    );
                    continue;
                }

                log.debug("Resolved patient reference {}", patientId);

                VisitOccurrence visitOccurrence =
                        visitOccurrenceMapper.toVisitOccurrence(encounter, person);
                visitOccurrence.setRun(run);

                log.debug("Assigning run {} to visit occurrence entity", runId);
                visitOccurrenceRepository.save(visitOccurrence);
            }
        }

        //Process Medication Request
        log.info("Processing Medication Request resources");
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource() instanceof MedicationRequest medicationRequest) {

                //validation
                if (!validationService.validateMedicationRequest(medicationRequest)) {
                    log.warn("Skipping invalid medication request {}", medicationRequest.getId());
                    continue;
                }

                // Extract patient reference
                String patientId = medicationRequest.getSubject()
                        .getReferenceElement()
                        .getIdPart();

                Person person = patientMap.get(patientId);

                if (person == null) {
                    log.error(
                            "Referenced person {} not found for medication request {}",
                            patientId,
                            medicationRequest.getId()
                    );
                    continue;
                }

                log.debug("Resolved patient reference {}", patientId);

                DrugExposure drugExposure =
                        drugExposureMapper.toDrugExposure(medicationRequest, person);
                drugExposure.setRun(run);

                log.debug("Assigning run {} to drug exposure entity", runId);
                drugExposureRepository.save(drugExposure);
            }
        }
        log.info("Finished processing bundle for run {}", run.getRunId());
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