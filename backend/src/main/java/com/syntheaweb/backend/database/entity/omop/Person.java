package com.syntheaweb.backend.database.entity.omop;

import jakarta.persistence.*;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long personId;

    @Column(name = "year_of_birth")
    private Integer yearOfBirth;

    @Column(name = "month_of_birth")
    private Integer monthOfBirth;

    @Column(name = "day_of_birth")
    private Integer dayOfBirth;

    @Column(name = "gender_concept_id")
    private Integer genderConceptId;

    public Person(){

    }

    public Person(Long personId, Integer yearOfBirth, Integer monthOfBirth, Integer dayOfBirth, Integer genderConceptId){
        this.personId = personId;
        this.yearOfBirth = yearOfBirth;
        this.monthOfBirth = monthOfBirth;
        this.dayOfBirth = dayOfBirth;
        this.genderConceptId = genderConceptId;
    }

    public Long getId() {
        return personId;
    }

    public void setId(Long personId) {
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
}
