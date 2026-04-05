package com.vitality.api.service;

import com.vitality.api.entities.Patients;
import com.vitality.api.repositories.PatientRepository;
import com.vitality.common.dtos.CreatePatientRequest;
import com.vitality.common.dtos.CreatePatientResponse;
import com.vitality.common.utils.GuidUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;

    /**
     * Method to create or updated the data of a patient.
     *
     * @param request: The create patient request.
     * @return the ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> createPatient(@NotNull CreatePatientRequest request) {
        try {
            Patients patient = getPatient(request.phoneNumber());
            if (patient == null) {
                log.info("Creating new patient with phone number: {}", request.phoneNumber());
                Patients newPatient = new Patients();
                String guid = GuidUtils.generateGuid();
                newPatient.setGuid(guid);
                newPatient.setFirstName(request.name().split(" ")[0]);
                newPatient.setLastName(request.name().split(" ").length > 1 ? request.name().split(" ")[1] : "");
                newPatient.setPhoneNumber(request.phoneNumber());
                newPatient.setEmailId(request.email());
                newPatient.setAge(request.age());
                newPatient.setGender(request.gender());
                newPatient.setHeightInCms(request.height());
                newPatient.setWeightInKgs(request.weight());
                newPatient.setBloodPressure(request.bloodPressure());
                newPatient.setAilmentHistory(request.ailmentHistory());
                newPatient.setHasHealthInsurance(request.hasHealthInsurance());
                newPatient.setAdditionalDiagnosis(request.additionalDiagnosis());
                newPatient.setAbhaId(request.abhaId());
                newPatient.setMedicinesConsumed(request.medicinesConsumed());
                newPatient.setAdditionalServicesRequired(request.additionalServicesRequired());
                repository.save(newPatient);
                CreatePatientResponse response = new CreatePatientResponse(guid, request.phoneNumber());
                return ResponseEntity.ok(response);
            } else {
                log.info("Updating existing patient with phone number: {}", request.phoneNumber());
                patient.setFirstName(request.name().split(" ")[0]);
                patient.setLastName(request.name().split(" ").length > 1 ? request.name().split(" ")[1] : "");
                patient.setPhoneNumber(request.phoneNumber());
                patient.setEmailId(request.email());
                patient.setAge(request.age());
                patient.setGender(request.gender());
                patient.setHeightInCms(request.height());
                patient.setWeightInKgs(request.weight());
                patient.setBloodPressure(request.bloodPressure());
                patient.setAilmentHistory(request.ailmentHistory());
                patient.setHasHealthInsurance(request.hasHealthInsurance());
                patient.setAdditionalDiagnosis(request.additionalDiagnosis());
                patient.setAbhaId(request.abhaId());
                patient.setMedicinesConsumed(request.medicinesConsumed());
                patient.setAdditionalServicesRequired(request.additionalServicesRequired());
                repository.save(patient);
                log.info("Patient with phone number: {} processed successfully", request.phoneNumber());
                CreatePatientResponse response = new CreatePatientResponse(patient.getGuid(), request.phoneNumber());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error occurred while creating patient: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Something went wrong.");
        }
    }


    private Patients getPatient(@NotNull final String phoneNumber) {
        return repository.findByPhoneNumber(phoneNumber);
    }
}
