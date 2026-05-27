package com.syntheaweb.backend.database.entity.omop;

import com.syntheaweb.backend.database.entity.Run;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "drug_exposure")
public class DrugExposure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drug_exposure_id")
    private Long drugExposureId;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Run run;

    //TODO: implement correctly after concept entity exists
    // @ManyToOne
    // @JoinColumn(name = "condition_concept_id", nullable = false)
    @Column(name = "drug_concept_id")
    private Integer drugConceptId;

    @Column(name = "drug_exposure_start_date")
    private LocalDate drugExposureStartDate;

    @Column(name = "drug_exposure_start_datetime")
    private LocalDateTime drugExposureStartDatetime;

    @Column(name = "drug_exposure_end_date")
    private LocalDate drugExposureEndDate;

    @Column(name = "drug_exposure_end_datetime")
    private LocalDateTime drugExposureEndDatetime;

    @Column(name = "drug_exposure_type_concept_id")
    private Integer drugExposureTypeConceptId;

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "visit_occurrence_id")
    private Long visitOccurrenceId;

    public DrugExposure(){

    }
    public DrugExposure(Long drugExposureId,
                        Person person,
                        Run run,
                        Integer drugConceptId,
                        LocalDate drugExposureStartDate,
                        LocalDateTime drugExposureStartDatetime,
                        LocalDate drugExposureEndDate,
                        LocalDateTime drugExposureEndDatetime,
                        Integer drugExposureTypeConceptId,
                        Long providerId,
                        Long visitOccurrenceId) {
        this.drugExposureId = drugExposureId;
        this.person = person;
        this.run = run;
        this.drugConceptId = drugConceptId;
        this.drugExposureStartDate = drugExposureStartDate;
        this.drugExposureStartDatetime = drugExposureStartDatetime;
        this.drugExposureEndDate = drugExposureEndDate;
        this.drugExposureEndDatetime = drugExposureEndDatetime;
        this.drugExposureTypeConceptId = drugExposureTypeConceptId;
        this.providerId = providerId;
        this.visitOccurrenceId = visitOccurrenceId;
    }

    public Long getDrugExposureId() {
        return drugExposureId;
    }

    public void setDrugExposureId(Long drugExposureId) {
        this.drugExposureId = drugExposureId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public Integer getDrugConceptId() {
        return drugConceptId;
    }

    public void setDrugConceptId(Integer drugConceptId) {
        this.drugConceptId = drugConceptId;
    }

    public LocalDate getDrugExposureStartDate() {
        return drugExposureStartDate;
    }

    public void setDrugExposureStartDate(LocalDate drugExposureStartDate) {
        this.drugExposureStartDate = drugExposureStartDate;
    }

    public LocalDateTime getDrugExposureStartDatetime() {
        return drugExposureStartDatetime;
    }

    public void setDrugExposureStartDatetime(LocalDateTime drugExposureStartDatetime) {
        this.drugExposureStartDatetime = drugExposureStartDatetime;
    }

    public LocalDate getDrugExposureEndDate() {
        return drugExposureEndDate;
    }

    public void setDrugExposureEndDate(LocalDate drugExposureEndDate) {
        this.drugExposureEndDate = drugExposureEndDate;
    }

    public LocalDateTime getDrugExposureEndDatetime() {
        return drugExposureEndDatetime;
    }

    public void setDrugExposureEndDatetime(LocalDateTime drugExposureEndDatetime) {
        this.drugExposureEndDatetime = drugExposureEndDatetime;
    }

    public Integer getDrugExposureTypeConceptId() {
        return drugExposureTypeConceptId;
    }

    public void setDrugExposureTypeConceptId(Integer drugExposureTypeConceptId) {
        this.drugExposureTypeConceptId = drugExposureTypeConceptId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Long getVisitOccurrenceId() {
        return visitOccurrenceId;
    }

    public void setVisitOccurrenceId(Long visitOccurrenceId) {
        this.visitOccurrenceId = visitOccurrenceId;
    }
}
