package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.ObservationPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservationPeriodRepository extends JpaRepository<ObservationPeriod, Long> {

    /**For writing/creating the data */
    List<ObservationPeriod> findByRun(Run run);
    /**For reading/exporting the data*/
    List<ObservationPeriod> findByRun_RunId(String runId);
}
