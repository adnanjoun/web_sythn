package com.syntheaweb.backend.database.entity.omop;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "measurement")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long measurementId;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

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

    public Measurement(){

    }

    public Measurement(Long measurementId, Person person, Integer measurementConceptId, LocalDate measurementDate, Double valueAsNumber, String measurementSourceValue){
        this.measurementId = measurementId;
        this.person = person;
        this.measurementConceptId = measurementConceptId;
        this.measurementDate = measurementDate;
        this.valueAsNumber = valueAsNumber;
        this.measurementSourceValue = measurementSourceValue;
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
}
