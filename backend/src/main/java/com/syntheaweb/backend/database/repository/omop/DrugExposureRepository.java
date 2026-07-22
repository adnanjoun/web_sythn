package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.Run;
import com.syntheaweb.backend.database.entity.omop.DrugExposure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugExposureRepository extends JpaRepository<DrugExposure, Long> {

    /**For writing/creating the data */
    List<DrugExposure> findByRun(Run run);
    /**For reading/exporting the data*/
    List<DrugExposure> findByRun_RunId(String runId);
}
