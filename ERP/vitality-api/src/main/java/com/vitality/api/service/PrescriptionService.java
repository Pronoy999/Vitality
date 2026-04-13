package com.vitality.api.service;

import com.vitality.api.entities.Patient;
import com.vitality.api.entities.Prescription;
import com.vitality.api.entities.PrescriptionDiagnosis;
import com.vitality.api.repositories.PrescriptionDiagnosisRepository;
import com.vitality.api.repositories.PrescriptionRepository;
import com.vitality.common.dtos.CreatePrescriptionDiagnosisRequest;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.dtos.CreatePrescriptionResponse;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.exceptions.InvalidTokenException;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import com.vitality.common.utils.Validators;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDiagnosisRepository prescriptionDiagnosisRepository;
    private final PatientService patientService;
    private final SecurityUtils securityUtils;

    public ResponseEntity<?> createPrescription(@NotNull CreatePrescriptionRequest request, @NotNull String jwtToken) {
        try {
            securityUtils.decodeJwt(jwtToken);
        } catch (InvalidTokenException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }
        if (ObjectUtils.isEmpty(request.getFirstName()) && ObjectUtils.isEmpty(request.getLastName())) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "First name and last name are required to create a prescription.");
        }
        try {
            Patient patient = patientService.searchPatient(request.getFirstName(), request.getLastName(), request.getPhoneNumber(), request.getEmail());
            if (patient == null) {
                patient = patientService.doCreatePatient(request);
            }
            Validators.validatePrescriptionDiagnosis(request);
            Prescription prescription = new Prescription();
            prescription.setPatient(patient);
            prescription.setPrescriptionDate(request.getPrescriptionDate());
            prescription.setReferredByDoctor(request.getReferredByDoctor());
            prescription.setPrescriptionImageUrl(request.getPrescriptionImageUrl());
            prescription.setDiagnosis(request.getDiagnosis());
            prescription = prescriptionRepository.save(prescription);
            log.info("Prescription created with ID: {}", prescription.getId());
            createPrescriptionDiagnosis(request, prescription);
            return ResponseGenerator.generateSuccessResponse(new CreatePrescriptionResponse(prescription.getId()), HttpStatus.CREATED);
        } catch (InvalidRequestException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error while creating prescription: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the prescription.");
        }
    }

    private void createPrescriptionDiagnosis(CreatePrescriptionRequest prescriptionRequest, Prescription prescription) {
        List<CreatePrescriptionDiagnosisRequest> requests = prescriptionRequest.getPrescriptionDiagnoses();
        List<PrescriptionDiagnosis> diagnoses = new ArrayList<>();
        for (CreatePrescriptionDiagnosisRequest request : requests) {
            if (request.getMedicineName() != null) {
                PrescriptionDiagnosis diagnosis = getPrescriptionDiagnosis(prescription, request);
                diagnoses.add(diagnosis);
            }
        }
        prescriptionDiagnosisRepository.saveAll(diagnoses);
    }

    private PrescriptionDiagnosis getPrescriptionDiagnosis(Prescription prescription, CreatePrescriptionDiagnosisRequest request) {
        PrescriptionDiagnosis diagnosis = new PrescriptionDiagnosis();
        diagnosis.setPrescription(prescription);
        diagnosis.setDiagnosis(request.getDiagnosis());
        diagnosis.setMedicineName(request.getMedicineName());
        diagnosis.setDosage(request.getDosage());
        diagnosis.setUnit(request.getUnit());
        diagnosis.setUnitMeasure(request.getUnitMeasure());
        diagnosis.setStartDate(request.getStartDate());
        diagnosis.setEndDate(request.getEndDate());
        diagnosis.setFrequency(request.getFrequency());
        diagnosis.setIsActive(true);
        return diagnosis;
    }
}
