package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.ConditionOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConditionOccurrenceRepository extends JpaRepository<ConditionOccurrence, Long> {
    /**For writing/creating the data */
    List<ConditionOccurrence> findByRun(Run run);
    /**For reading/exporting the data*/
    List<ConditionOccurrence> findByRun_RunId(String runId);
}
