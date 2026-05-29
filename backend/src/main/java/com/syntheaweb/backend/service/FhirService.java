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

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

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
    @Autowired
    private final ObservationPeriodService observationPeriodService;
    @Autowired
    private final StorageService storageService;

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
                       DrugExposureRepository drugExposureRepository,
                       ObservationPeriodService observationPeriodService,
                       StorageService storageService) {
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
        this.observationPeriodService = observationPeriodService;
        this.storageService = storageService;
    }

    public Bundle parseBundle(String json) {
        log.info("Starting parsing FHIR bundle.");
        FhirContext ctx = FhirContext.forR4();
        return (Bundle) ctx.newJsonParser().parseResource(json);
    }

    public void processRun(String runId) throws IOException {

        Run run = runRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Run not found"));

        log.info("Starting OMOP processing for run {}", runId);

        List<String> bundleJsonList =
                storageService.readAllFhirBundles(runId);

        if (bundleJsonList.isEmpty()) {
            throw new RuntimeException("No FHIR bundles found for run " + runId);
        }

        for (String json : bundleJsonList) {

            Bundle bundle = parseBundle(json);

            processBundle(bundle, run);
        }

        log.info("Finished OMOP processing for run {}", runId);
    }

    private void processBundle(Bundle bundle, Run run){

        Map<String, Person> patientMap = new HashMap<>();

        //to store dates for each patient, Observation Period
        Map<String, List<LocalDate>> patientDatesMap = new HashMap<>();

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

                log.debug("Assigning run {} to person entity", run.getRunId());

                patientMap.put(patient.getIdElement().getIdPart(), person);

                //to store dates for each patient, Observation Period
                patientDatesMap.put(patient.getIdElement().getIdPart(), new ArrayList<>());
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

                //store data for Observation Period
                LocalDate resourceDate =
                        observationPeriodService.extractDateFromResource(condition);

                List<LocalDate> dates = patientDatesMap.get(patientId);

                if (dates != null && resourceDate != null) {
                    dates.add(resourceDate);
                }

                log.debug("Resolved patient reference {}", patientId);

                ConditionOccurrence conditionOccurrence =
                        conditionOccurrenceMapper.toConditionOccurrence(condition, person);
                conditionOccurrence.setRun(run);

                log.debug("Assigning run {} to condition occurrence entity", run.getRunId());
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

                //store data for Observation Period
                LocalDate resourceDate =
                        observationPeriodService.extractDateFromResource(observation);

                List<LocalDate> dates = patientDatesMap.get(patientId);

                if (dates != null && resourceDate != null) {
                    dates.add(resourceDate);
                }

                log.debug("Resolved patient reference {}", patientId);

                Measurement measurement = measurementMapper.toMeasurement(observation, person);
                measurement.setRun(run);

                log.debug("Assigning run {} to measurement entity", run.getRunId());
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

                //store data for Observation Period
                LocalDate resourceDate =
                        observationPeriodService.extractDateFromResource(encounter);

                List<LocalDate> dates = patientDatesMap.get(patientId);

                if (dates != null && resourceDate != null) {
                    dates.add(resourceDate);
                }

                log.debug("Resolved patient reference {}", patientId);

                VisitOccurrence visitOccurrence =
                        visitOccurrenceMapper.toVisitOccurrence(encounter, person);
                visitOccurrence.setRun(run);

                log.debug("Assigning run {} to visit occurrence entity", run.getRunId());
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

                //store data for Observation Period
                LocalDate resourceDate =
                        observationPeriodService.extractDateFromResource(medicationRequest);

                List<LocalDate> dates = patientDatesMap.get(patientId);

                if (dates != null && resourceDate != null) {
                    dates.add(resourceDate);
                }

                log.debug("Resolved patient reference {}", patientId);

                DrugExposure drugExposure =
                        drugExposureMapper.toDrugExposure(medicationRequest, person);
                drugExposure.setRun(run);

                log.debug("Assigning run {} to drug exposure entity", run.getRunId());
                drugExposureRepository.save(drugExposure);
            }
        }

        //generate Observation Periods after all processing
        log.info("Generating observation periods");

        for (Map.Entry<String, List<LocalDate>> entry
                : patientDatesMap.entrySet()) {

            String patientId = entry.getKey();

            List<LocalDate> dates = entry.getValue();

            if (dates.isEmpty()) {
                log.warn("No dates found for patient {}", patientId);
                continue;
            }

            LocalDate startDate = Collections.min(dates);
            LocalDate endDate = Collections.max(dates);

            Person person = patientMap.get(patientId);

            observationPeriodService.createObservationPeriod(
                    person,
                    run,
                    startDate,
                    endDate
            );

            log.debug(
                    "Created observation period for patient {} from {} to {}",
                    patientId,
                    startDate,
                    endDate
            );
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