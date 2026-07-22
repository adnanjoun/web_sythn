package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.VisitOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitOccurrenceRepository extends JpaRepository<VisitOccurrence, Long> {

    /**For writing/creating the data */
    List<VisitOccurrence> findByRun(Run run);
    /**For reading/exporting the data*/
    List<VisitOccurrence> findByRun_RunId(String runId);
}
