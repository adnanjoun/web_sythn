package com.syntheaweb.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConceptMappingService {

    private static final Logger log =
            LoggerFactory.getLogger(ConceptMappingService.class);

    // Type Concepts
    public static final int EHR_RECORD = 32817;
    public static final int PATIENT_REPORTED = 45905771;
    public static final int LAB_RESULT = 32856;

    //Drug Exposure
    public static final int PRESCRIPTION_WRITTEN = 38000177;
    public static final int PHYSICIAN_ADMINISTERED_DRUG = 38000180;


    private final Map<String, Integer> mappings = Map.ofEntries(

            // Conditions
            Map.entry("http://snomed.info/sct|44054006", 201826), // Diabetes mellitus type 2
            Map.entry("http://snomed.info/sct|38341003", 319835), // Hypertension
            Map.entry("http://snomed.info/sct|195967001", 255848), // Asthma
            Map.entry("http://snomed.info/sct|233604007", 433736), // Pneumonia
            Map.entry("http://snomed.info/sct|6142004", 441840), // Influenza

            // Measurements
            Map.entry("http://loinc.org|8867-4", 3027018), // Heart rate
            Map.entry("http://loinc.org|8480-6", 3004249), // Systolic BP
            Map.entry("http://loinc.org|8462-4", 3012888), // Diastolic BP
            Map.entry("http://loinc.org|8310-5", 3020891), // Body temperature
            Map.entry("http://loinc.org|29463-7", 3036277), // Body weight
            Map.entry("http://loinc.org|8302-2", 3038553), // Body height

            // Encounter / VisitOccurrence
            Map.entry("http://terminology.hl7.org/CodeSystem/v3-ActCode|IMP", 9201), // Inpatient
            Map.entry("http://terminology.hl7.org/CodeSystem/v3-ActCode|AMB", 9202), // Outpatient
            Map.entry("http://terminology.hl7.org/CodeSystem/v3-ActCode|EMER", 9203), // Emergency Room
            Map.entry("http://terminology.hl7.org/CodeSystem/v3-ActCode|VR", 9204), // Virtual visit

            // Medications
            Map.entry("http://www.nlm.nih.gov/research/umls/rxnorm|860975", 860975), // Metformin
            Map.entry("http://www.nlm.nih.gov/research/umls/rxnorm|617314", 617314), // Simvastatin
            Map.entry("http://www.nlm.nih.gov/research/umls/rxnorm|1049630", 1049630), // Lisinopril
            Map.entry("http://www.nlm.nih.gov/research/umls/rxnorm|197361", 197361), // Amlodipine
            Map.entry("http://www.nlm.nih.gov/research/umls/rxnorm|83367", 83367) // Ibuprofen
    );

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
