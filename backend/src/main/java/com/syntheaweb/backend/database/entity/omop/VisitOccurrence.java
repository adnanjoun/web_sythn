package com.syntheaweb.backend.database.entity.omop;

import com.syntheaweb.backend.database.entity.Run;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_occurrence")
public class VisitOccurrence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_occurrence_id")
    private Long visitOccurrenceId;

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
    @Column(name = "visit_concept_id")
    private Integer visitConceptId;

    @Column(name = "visit_start_date")
    private LocalDate visitStartDate;

    @Column(name = "visit_start_datetime")
    private LocalDateTime visitStartDatetime;

    @Column(name = "visit_end_date")
    private LocalDate visitEndDate;

    @Column(name = "visit_end_datetime")
    private LocalDateTime visitEndDatetime;

    @Column(name = "visit_type_concept_id")
    private Integer visitTypeConceptId;

    @Column(name = "provider_id")
    private Long providerId;

    public VisitOccurrence(){

    }

    public VisitOccurrence(Long visitOccurrenceId,
                               Person person,
                               Run run,
                               Integer visitConceptId,
                               LocalDate visitStartDate,
                               LocalDate visitEndDate,
                               LocalDateTime visitStartDatetime,
                               LocalDateTime visitEndDatetime,
                               Integer visitTypeConceptId,
                               Long providerId){
        this.visitOccurrenceId = visitOccurrenceId;
        this.person = person;
        this.visitConceptId = visitConceptId;
        this.visitStartDate = visitStartDate;
        this.visitEndDate = visitEndDate;
        this.run = run;
        this.visitStartDatetime = visitStartDatetime;
        this.visitEndDatetime = visitEndDatetime;
        this.visitTypeConceptId = visitTypeConceptId;
        this.providerId = providerId;
    }

    public Long getVisitOccurrenceId() {
        return visitOccurrenceId;
    }

    public void setVisitOccurrenceId(Long visitOccurrenceId) {
        this.visitOccurrenceId = visitOccurrenceId;
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

    public Integer getVisitConceptId() {
        return visitConceptId;
    }

    public void setVisitConceptId(Integer visitConceptId) {
        this.visitConceptId = visitConceptId;
    }

    public LocalDate getVisitStartDate() {
        return visitStartDate;
    }

    public void setVisitStartDate(LocalDate visitStartDate) {
        this.visitStartDate = visitStartDate;
    }

    public LocalDateTime getVisitStartDatetime() {
        return visitStartDatetime;
    }

    public void setVisitStartDatetime(LocalDateTime visitStartDatetime) {
        this.visitStartDatetime = visitStartDatetime;
    }

    public LocalDate getVisitEndDate() {
        return visitEndDate;
    }

    public void setVisitEndDate(LocalDate visitEndDate) {
        this.visitEndDate = visitEndDate;
    }

    public LocalDateTime getVisitEndDatetime() {
        return visitEndDatetime;
    }

    public void setVisitEndDatetime(LocalDateTime visitEndDatetime) {
        this.visitEndDatetime = visitEndDatetime;
    }

    public Integer getVisitTypeConceptId() {
        return visitTypeConceptId;
    }

    public void setVisitTypeConceptId(Integer visitTypeConceptId) {
        this.visitTypeConceptId = visitTypeConceptId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }
}
