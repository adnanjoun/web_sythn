package com.syntheaweb.backend.database.repository.omop;

import com.syntheaweb.backend.database.entity.omop.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByRunId(String runId);
}
