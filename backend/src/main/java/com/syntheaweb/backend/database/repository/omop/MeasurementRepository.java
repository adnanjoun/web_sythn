package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    /**For writing/creating the data */
    List<Measurement> findByRun(Run run);
    /**For reading/exporting the data*/
    List<Measurement> findByRun_RunId(String runId);
}
