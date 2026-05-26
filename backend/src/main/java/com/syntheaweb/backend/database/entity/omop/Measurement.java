package com.syntheaweb.backend.database.entity.omop;

import com.syntheaweb.backend.database.entity.Run;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurement")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long measurementId;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Run run;

    //TODO: implement correctly after concept entity exists
    // @ManyToOne
    // @JoinColumn(name = "measurement_concept_id", nullable = false)
    @Column(name = "measurement_concept_id")
    private Integer measurementConceptId;

    @Column(name = "measurement_date")
    private LocalDate measurementDate;

    @Column(name = "value_as_number")
    private Double valueAsNumber;

    @Column(name = "measurement_source_value")
    private String measurementSourceValue;

    @Column(name = "measurement_datetime")
    private LocalDateTime measurementDatetime;

    @Column(name = "unit_concept_id")
    private Integer unitConceptId;

    public Measurement(){

    }

    public Measurement(Long measurementId,
                       Person person,
                       Integer measurementConceptId,
                       LocalDate measurementDate,
                       Double valueAsNumber,
                       String measurementSourceValue,
                       Run run,
                       LocalDateTime measurementDatetime,
                       Integer unitConceptId){
        this.measurementId = measurementId;
        this.person = person;
        this.measurementConceptId = measurementConceptId;
        this.measurementDate = measurementDate;
        this.valueAsNumber = valueAsNumber;
        this.measurementSourceValue = measurementSourceValue;
        this.run = run;
        this.measurementSourceValue = measurementSourceValue;
        this.unitConceptId = unitConceptId;
    }

    public Long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(Long measurementId) {
        this.measurementId = measurementId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Integer getMeasurementConceptId() {
        return measurementConceptId;
    }

    public void setMeasurementConceptId(Integer measurementConceptId) {
        this.measurementConceptId = measurementConceptId;
    }

    public LocalDate getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(LocalDate measurementDate) {
        this.measurementDate = measurementDate;
    }

    public Double getValueAsNumber() {
        return valueAsNumber;
    }

    public void setValueAsNumber(Double valueAsNumber) {
        this.valueAsNumber = valueAsNumber;
    }

    public String getMeasurementSourceValue() {
        return measurementSourceValue;
    }

    public void setMeasurementSourceValue(String measurementSourceValue) {
        this.measurementSourceValue = measurementSourceValue;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public LocalDateTime getMeasurementDatetime() {
        return measurementDatetime;
    }

    public void setMeasurementDatetime(LocalDateTime measurementDatetime) {
        this.measurementDatetime = measurementDatetime;
    }

    public Integer getUnitConceptId() {
        return unitConceptId;
    }

    public void setUnitConceptId(Integer unitConceptId) {
        this.unitConceptId = unitConceptId;
    }
}
