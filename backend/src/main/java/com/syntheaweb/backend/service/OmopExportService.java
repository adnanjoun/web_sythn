package com.syntheaweb.backend.service;

import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import com.syntheaweb.backend.database.entity.omop.Measurement;
import com.syntheaweb.backend.database.entity.omop.Person;
import com.syntheaweb.backend.database.repository.omop.ConditionOccurrenceRepository;
import com.syntheaweb.backend.database.repository.omop.MeasurementRepository;
import com.syntheaweb.backend.database.repository.omop.PersonRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class OmopExportService {

    private final PersonRepository personRepository;
    private final ConditionOccurrenceRepository conditionRepository;
    private final MeasurementRepository measurementRepository;

    public OmopExportService(PersonRepository personRepository,
                             ConditionOccurrenceRepository conditionRepository,
                             MeasurementRepository measurementRepository) {
        this.personRepository = personRepository;
        this.conditionRepository = conditionRepository;
        this.measurementRepository = measurementRepository;
    }

    public void exportRun(String runId, HttpServletResponse response) throws IOException {

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=omop_" + runId + ".zip");


        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            writePersonCsv(runId, zos);
            writeConditionCsv(runId, zos);
            writeMeasurementCsv(runId, zos);
        }
    }

    /** For safe CSV Handling,
     * e.g. commas inside values could break CSV structure
     * */
    private String safe(Object value) {
        if (value == null) return "";
        return "\"" + value.toString().replace("\"", "\"\"") + "\"";
    }

    private void writePersonCsv(String runId, ZipOutputStream zos) throws IOException {

        zos.putNextEntry(new ZipEntry("person.csv"));

        String header = "person_id,year_of_birth,month_of_birth,day_of_birth,gender_concept_id\n"; //TODO: fill the missing fields, when implemented
        zos.write(header.getBytes());

        List<Person> persons = personRepository.findByRun_RunId(runId);

        for (Person p : persons) {
            String line = safe(p.getId()) + "," +
                    safe(p.getYearOfBirth()) + "," +
                    safe(p.getMonthOfBirth()) + "," +
                    safe(p.getDayOfBirth()) + "," +
                    safe(p.getGenderConceptId()) + "\n";

            zos.write(line.getBytes(StandardCharsets.UTF_8));
        }

        zos.closeEntry();
    }

    private void writeConditionCsv(String runId, ZipOutputStream zos) throws IOException {

        zos.putNextEntry(new ZipEntry("condition_occurrence.csv"));

        String header = "condition_occurrence_id,person_id,condition_concept_id,start_date,end_date\n";
        zos.write(header.getBytes());

        List<ConditionOccurrence> list = conditionRepository.findByRun_RunId(runId);

        for (ConditionOccurrence c : list) {
            String line = safe(c.getConditionOccurrenceId()) + "," +
                    safe(c.getPerson().getId()) + "," +
                    safe(c.getConditionConceptId()) + "," +
                    safe(c.getConditionStartDate()) + "," +
                    safe(c.getConditionEndDate()) + "\n";

            zos.write(line.getBytes(StandardCharsets.UTF_8));
        }

        zos.closeEntry();
    }
    private void writeMeasurementCsv(String runId, ZipOutputStream zos) throws IOException {

        zos.putNextEntry(new ZipEntry("measurement.csv"));

        String header = "measurement_id,person_id,concept_id,date,value,source_value\n";
        zos.write(header.getBytes());

        List<Measurement> list = measurementRepository.findByRun_RunId(runId);

        for (Measurement m : list) {
            String line = safe(m.getMeasurementId()) + "," +
                    safe(m.getPerson().getId()) + "," +
                    safe(m.getMeasurementConceptId()) + "," +
                    safe(m.getMeasurementDate()) + "," +
                    safe(m.getValueAsNumber()) + "," +
                    safe(m.getMeasurementSourceValue()) + "\n";

            zos.write(line.getBytes(StandardCharsets.UTF_8));
        }

        zos.closeEntry();
    }
}
