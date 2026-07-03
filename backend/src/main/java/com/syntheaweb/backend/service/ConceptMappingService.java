package com.syntheaweb.backend.service;

import com.syntheaweb.backend.mapperFhir.ConceptMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConceptMappingService {

    private static final Logger log =
            LoggerFactory.getLogger(ConceptMappingService.class);

    public ConceptMappingService() {
        loadMappings();
    }

    // Type Concepts
    public static final int EHR_RECORD = 32817;
    public static final int PATIENT_REPORTED = 45905771;
    public static final int LAB_RESULT = 32856;

    //Drug Exposure
    public static final int PRESCRIPTION_WRITTEN = 38000177;
    public static final int PHYSICIAN_ADMINISTERED_DRUG = 38000180;


    private final Map<String, Integer> mappings = new HashMap<>();

    private void loadMappings() {

        try (InputStream input =
                     getClass().getClassLoader()
                             .getResourceAsStream("concept-mappings.csv")) {

            if (input == null) {
                throw new RuntimeException(
                        "Could not find resource concept-mappings.csv"
                );
            }

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(input));

            // skip header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                String[] values = line.split(",");

                if (values.length != 3) {
                    log.warn("Skipping invalid mapping: {}", line);
                    continue;
                }

                ConceptMapping mapping = new ConceptMapping(
                        values[0],
                        values[1],
                        Integer.parseInt(values[2])
                );

                mappings.put(
                        mapping.sourceSystem() + "|" + mapping.sourceCode(),
                        mapping.conceptId()
                );
            }

            log.info("Loaded {} concept mappings.", mappings.size());

        } catch (IOException e) {
            throw new RuntimeException("Failed to load concept mappings", e);
        }
    }

    public int resolve(String system, String code) {

        String key = system + "|" + code;

        if (!mappings.containsKey(key)) {
            log.warn("No OMOP concept mapping found for {}", key);
            return 0;
        }

        return mappings.get(key);
    }

    public int mapGender(String gender) {
        return switch (gender.toLowerCase()) {
            case "male" -> 8507;
            case "female" -> 8532;
            case "other" -> 8551;
            default -> 0;
        };
    }

    private final Map<String, Integer> raceMap = Map.of(
            "white", 8527,
            "black", 8516,
            "asian", 8515,
            "native american", 8657,
            "other", 8522
    );

    private final Map<String, Integer> ethnicityMap = Map.of(
            "hispanic", 38003563,
            "nonhispanic", 38003564
    );

    public Integer mapRace(String race) {
        if(race == null){
            return 0;
        }
        return raceMap.getOrDefault(
                race.toLowerCase(),
                0
        );
    }

    public Integer mapEthnicity(String ethnicity) {
        if(ethnicity == null){
            return 0;
        }
        return ethnicityMap.getOrDefault(
                ethnicity.toLowerCase(),
                0
        );
    }

    private final Map<String, Integer> unitMap = Map.ofEntries(

            Map.entry("kg", 9529),
            Map.entry("cm", 8582),
            Map.entry("mm[Hg]", 8876),
            Map.entry("Cel", 8555),
            Map.entry("degF", 8554),

            Map.entry("bpm", 4118323),
            Map.entry("/min", 8554),
            Map.entry("%", 8554),
            Map.entry("mg/dL", 8840),
            Map.entry("kg/m2", 9531)
    );

    public Integer mapUnit(String unit) {

        if (unit == null) {
            return 0;
        }

        return unitMap.getOrDefault(unit, 0);
    }
}
