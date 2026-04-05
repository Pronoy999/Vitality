package com.vitality.api.repositories;

import com.vitality.api.entities.Patients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatientRepository extends JpaRepository<Patients, Long> {

    @Query("SELECT p from Patients p where p.phoneNumber=?1")
    Patients findByPhoneNumber(String phoneNumber);
}
