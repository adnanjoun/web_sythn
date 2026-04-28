package com.syntheaweb.backend.database.entity.omop;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "condition_occurrence")
public class ConditionOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_occurrence_id")
    private Long conditionOccurrenceId;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    //TODO: implement correctly after concept entity exists
    // @ManyToOne
    // @JoinColumn(name = "condition_concept_id", nullable = false)
    @Column(name = "condition_concept_id")
    private Integer conditionConceptId;

    @Column(name = "condition_start_date")
    private LocalDate conditionStartDate;

    @Column(name = "condition_end_date")
    private LocalDate conditionEndDate;

    public ConditionOccurrence(){

    }

    public ConditionOccurrence(Long conditionOccurrenceId, Person person, Integer conditionConceptId, LocalDate conditionStartDate, LocalDate conditionEndDate){
        this.conditionOccurrenceId = conditionOccurrenceId;
        this.person = person;
        this.conditionConceptId = conditionConceptId;
        this.conditionStartDate = conditionStartDate;
        this.conditionEndDate = conditionEndDate;
    }

    public Long getConditionOccurrenceId() {
        return conditionOccurrenceId;
    }

    public void setConditionOccurrenceId(Long conditionOccurrenceId) {
        this.conditionOccurrenceId = conditionOccurrenceId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Integer getConditionConceptId() {
        return conditionConceptId;
    }

    public void setConditionConceptId(Integer conditionConceptId) {
        this.conditionConceptId = conditionConceptId;
    }

    public LocalDate getConditionStartDate() {
        return conditionStartDate;
    }

    public void setConditionStartDate(LocalDate conditionStartDate) {
        this.conditionStartDate = conditionStartDate;
    }

    public LocalDate getConditionEndDate() {
        return conditionEndDate;
    }

    public void setConditionEndDate(LocalDate conditionEndDate) {
        this.conditionEndDate = conditionEndDate;
    }
}
