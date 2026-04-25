package com.vitality.api.repositories;

import com.vitality.api.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p from Patient p where p.phoneNumber=?1 and p.isActive=true")
    Patient findByPhoneNumber(String phoneNumber);

    @Query("SELECT p from Patient p where p.user.guid=?1 and p.isActive=true")
    Patient findByUserGuid(String userGuid);

    @Query("SELECT p from Patient p where p.firstName=?1 and p.lastName=?2 and p.isActive=true")
    List<Patient> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT p from Patient p where p.emailId=?1 and p.isActive=true")
    Patient findByEmailId(String emailId);
}
