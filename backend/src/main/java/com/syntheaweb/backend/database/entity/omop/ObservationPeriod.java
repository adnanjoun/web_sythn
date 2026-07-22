package com.syntheaweb.backend.database.entity.omop;

import com.syntheaweb.backend.database.entity.Run;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "observation_period")
public class ObservationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "observation_period_id")
    private Long observationPeriodId;

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
    @Column(name = "period_type_concept_id")
    private Integer periodTypeConceptId;

    @Column(name = "observation_period_start_date")
    private LocalDate observationPeriodStartDate;

    @Column(name = "observation_period_end_date")
    private LocalDate observationPeriodEndDate;


    public ObservationPeriod(){}
    public ObservationPeriod(Long observationPeriodId,
                             Person person,
                             Run run,
                             LocalDate observationPeriodStartDate,
                             LocalDate observationPeriodEndDate,
                             Integer periodTypeConceptId){
        this.observationPeriodId = observationPeriodId;
        this.person = person;
        this.run = run;
        this.observationPeriodStartDate = observationPeriodStartDate;
        this.observationPeriodEndDate = observationPeriodEndDate;
        this.periodTypeConceptId = periodTypeConceptId;
    }

    public Long getObservationPeriodId() {
        return observationPeriodId;
    }

    public void setObservationPeriodId(Long observationPeriodId) {
        this.observationPeriodId = observationPeriodId;
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

    public Integer getPeriodTypeConceptId() {
        return periodTypeConceptId;
    }

    public void setPeriodTypeConceptId(Integer periodTypeConceptId) {
        this.periodTypeConceptId = periodTypeConceptId;
    }

    public LocalDate getObservationPeriodStartDate() {
        return observationPeriodStartDate;
    }

    public void setObservationPeriodStartDate(LocalDate observationPeriodStartDate) {
        this.observationPeriodStartDate = observationPeriodStartDate;
    }

    public LocalDate getObservationPeriodEndDate() {
        return observationPeriodEndDate;
    }

    public void setObservationPeriodEndDate(LocalDate observationPeriodEndDate) {
        this.observationPeriodEndDate = observationPeriodEndDate;
    }
}
