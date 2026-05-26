package com.syntheaweb.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConceptMappingService {


    private final Map<String, Integer> mappings = Map.of(

            "http://snomed.info/sct|44054006",201826,
            "http://snomed.info/sct|38341003",319835,
            "http://loinc.org|8867-4",3027018,
            "http://loinc.org|8480-6", 3004249,
            "http://loinc.org|8462-4",3012888
    );

    public int resolve(String system, String code) {

        return mappings.getOrDefault(
                system + "|" + code,
                0
        );
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

    private final Map<String, Integer> unitMap = Map.of(
            "kg", 9529,
            "cm", 8582,
            "mm[Hg]", 8876,
            "Cel", 8555,
            "degF", 8554
    );

    public Integer mapUnit(String unit) {

        if (unit == null) {
            return 0;
        }

        return unitMap.getOrDefault(unit, 0);
    }
}
