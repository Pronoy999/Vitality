package com.vitality.api.service;

import com.vitality.api.entities.Patient;
import com.vitality.api.entities.User;
import com.vitality.api.repositories.PatientRepository;
import com.vitality.common.dtos.CreatePatientRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserService userService;

    /**
     * Method to create or updated the data of a patient.
     *
     * @param request: The create patient request.
     * @return the ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> createPatient(@NotNull CreatePatientRequest request) {
        return null;
    }

    public Patient getPatientByEmailId(String emailId) {
        User user = userService.getUserByEmail(emailId);
        if (user != null) {
            return patientRepository.findByUserGuid(user.getGuid());
        }
        return null;
    }

    protected Patient doCreatePatient(@NotNull CreatePatientRequest request) {
        User user = userService.searchOrCreatePatientUser(request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getPhoneNumber());
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setAbhaId(request.getAbhaId());
        patient.setEmailId(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender());
        patient.setAdditionalDiagnosis(request.getAdditionalDiagnosis());
        patient.setHeightInCms(request.getHeight());
        patient.setWeightInKgs(request.getWeight());
        patient.setBloodPressure(request.getBloodPressure());
        patient.setAilmentHistory(request.getAilmentHistory());
        patient.setHealthParameters(request.getHealthParameters());
        patient.setHasHealthInsurance(request.getHasHealthInsurance());
        patient.setMedicinesConsumed(request.getMedicinesConsumed());
        patient.setAdditionalServicesRequired(request.getAdditionalServicesRequired());
        patient.setIsActive(true);
        return patientRepository.save(patient);
    }

    /**
     * Method to search for a Patient by Either Name, phone Number or Email id
     *
     * @param firstName:   the first name of the patient.
     * @param lastName:    the last name of the patient.
     * @param phoneNumber: the phone number of the patient.
     * @param emailId:     the email id of the patient.
     * @return the {@link Patient} matching the search criteria, or null if no match is found.
     */
    public Patient searchPatient(String firstName, String lastName, String phoneNumber, String emailId) {
        if (!ObjectUtils.isEmpty(phoneNumber)) {
            return patientRepository.findByPhoneNumber(phoneNumber);
        }
        if (!ObjectUtils.isEmpty(emailId)) {
            return patientRepository.findByEmailId(emailId);
        }
        if (!ObjectUtils.isEmpty(firstName) && !ObjectUtils.isEmpty(lastName)) {
            patientRepository.findByFirstNameAndLastName(firstName, lastName);
        }
        return null;
    }
}
