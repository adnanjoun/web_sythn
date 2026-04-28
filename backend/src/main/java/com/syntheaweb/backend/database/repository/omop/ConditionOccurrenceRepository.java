package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConditionOccurrenceRepository extends JpaRepository<ConditionOccurrence, Long> {
}
