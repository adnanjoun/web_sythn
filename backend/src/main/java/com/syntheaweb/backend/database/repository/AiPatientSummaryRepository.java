package com.syntheaweb.backend.database.repository;

import com.syntheaweb.backend.database.entity.AiPatientSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiPatientSummaryRepository extends JpaRepository<AiPatientSummary, Long> {

    List<AiPatientSummary> findByPatient_PatientIdOrderByCreatedAtDesc(String patientId);
}
