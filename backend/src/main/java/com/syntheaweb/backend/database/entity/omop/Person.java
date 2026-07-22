package com.syntheaweb.backend.database.entity.omop;

import com.syntheaweb.backend.database.entity.Run;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long personId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Run run;

    @Column(name = "year_of_birth")
    private Integer yearOfBirth;

    @Column(name = "month_of_birth")
    private Integer monthOfBirth;

    @Column(name = "day_of_birth")
    private Integer dayOfBirth;

    @Column(name = "gender_concept_id")
    private Integer genderConceptId;

    @Column(name = "race_concept_id")
    private Integer raceConceptId;

    @Column(name = "ethnicity_concept_id")
    private Integer ethnicityConceptId;


    public Person(){

    }

    public Person(Long personId,
                  Integer yearOfBirth,
                  Integer monthOfBirth,
                  Integer dayOfBirth,
                  Integer genderConceptId,
                  Run run,
                  Integer raceConceptId,
                  Integer ethnicityConceptId){
        this.personId = personId;
        this.yearOfBirth = yearOfBirth;
        this.monthOfBirth = monthOfBirth;
        this.dayOfBirth = dayOfBirth;
        this.genderConceptId = genderConceptId;
        this.run = run;
        this.raceConceptId = raceConceptId;
        this.ethnicityConceptId = ethnicityConceptId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Integer getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Integer getMonthOfBirth() {
        return monthOfBirth;
    }

    public void setMonthOfBirth(Integer monthOfBirth) {
        this.monthOfBirth = monthOfBirth;
    }

    public Integer getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(Integer dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public Integer getGenderConceptId() {
        return genderConceptId;
    }

    public void setGenderConceptId(Integer genderConceptId) {
        this.genderConceptId = genderConceptId;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public Integer getRaceConceptId() {
        return raceConceptId;
    }

    public void setRaceConceptId(Integer raceConceptId) {
        this.raceConceptId = raceConceptId;
    }

    public Integer getEthnicityConceptId() {
        return ethnicityConceptId;
    }

    public void setEthnicityConceptId(Integer ethnicityConceptId) {
        this.ethnicityConceptId = ethnicityConceptId;
    }
}
