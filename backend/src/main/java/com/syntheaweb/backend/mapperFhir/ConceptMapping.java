package com.syntheaweb.backend.mapperFhir;

/**
 * Mapping Concept
 * */
public record ConceptMapping(
        String sourceSystem,
        String sourceCode,
        int conceptId
) {}
