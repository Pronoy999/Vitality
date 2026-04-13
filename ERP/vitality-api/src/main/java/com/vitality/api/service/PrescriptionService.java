package com.vitality.api.service;

import com.vitality.api.entities.Patient;
import com.vitality.api.entities.Prescription;
import com.vitality.api.entities.PrescriptionDiagnosis;
import com.vitality.api.repositories.PrescriptionDiagnosisRepository;
import com.vitality.api.repositories.PrescriptionRepository;
import com.vitality.common.dtos.CreatePrescriptionDiagnosisRequest;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.dtos.CreatePrescriptionResponse;
import com.vitality.common.dtos.ParsedMedicineData;
import com.vitality.common.dtos.ParsedPrescriptionData;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        return persistPrescription(request, true, true, false);
    }

    public ResponseEntity<?> createPrescriptionFromParsedData(@NotNull ParsedPrescriptionData data) {
        CreatePrescriptionRequest request = toCreatePrescriptionRequest(data);
        return persistPrescription(request, false, false, true);
    }

    private ResponseEntity<?> persistPrescription(CreatePrescriptionRequest request, boolean requirePatientName, boolean requireDiagnosisRows, boolean pythonCompatibleResponse) {
        if (requirePatientName && ObjectUtils.isEmpty(request.getFirstName()) && ObjectUtils.isEmpty(request.getLastName())) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "First name and last name are required to create a prescription.");
        }
        try {
            Patient patient = null;
            if (!ObjectUtils.isEmpty(request.getFirstName()) || !ObjectUtils.isEmpty(request.getLastName())
                    || !ObjectUtils.isEmpty(request.getPhoneNumber()) || !ObjectUtils.isEmpty(request.getEmail())) {
                patient = patientService.searchPatient(request.getFirstName(), request.getLastName(), request.getPhoneNumber(), request.getEmail());
            }
            if (patient == null && !ObjectUtils.isEmpty(request.getFirstName()) && !ObjectUtils.isEmpty(request.getLastName())) {
                patient = patientService.doCreatePatient(request);
            }
            if (requireDiagnosisRows) {
                Validators.validatePrescriptionDiagnosis(request);
            }
            Prescription prescription = new Prescription();
            prescription.setPatient(patient);
            prescription.setPrescriptionDate(request.getPrescriptionDate() == null ? LocalDate.now() : request.getPrescriptionDate());
            prescription.setReferredByDoctor(request.getReferredByDoctor());
            prescription.setPrescriptionImageUrl(request.getPrescriptionImageUrl());
            prescription.setDiagnosis(request.getDiagnosis());
            prescription = prescriptionRepository.save(prescription);
            log.info("Prescription created with ID: {}", prescription.getId());
            createPrescriptionDiagnosis(request, prescription);
            Object response = pythonCompatibleResponse
                    ? Map.of("prescription_id", prescription.getId())
                    : new CreatePrescriptionResponse(prescription.getId());
            return ResponseGenerator.generateSuccessResponse(response, HttpStatus.CREATED);
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
        if (requests == null || requests.isEmpty()) {
            return;
        }
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

    private CreatePrescriptionRequest toCreatePrescriptionRequest(ParsedPrescriptionData data) {
        CreatePrescriptionRequest request = new CreatePrescriptionRequest();
        String[] nameParts = splitPatientName(data.getPatientName());
        request.setFirstName(nameParts[0]);
        request.setLastName(nameParts[1]);
        request.setAge(parseInteger(data.getPatientAge()));
        request.setPrescriptionDate(parseDate(data.getDate()));
        request.setReferredByDoctor(data.getDoctorName());
        request.setDiagnosis(data.getDiagnosis());
        request.setAdditionalDiagnosis(data.getPatientIssue());
        request.setHealthParameters(formatHealthMetrics(data.getHealthMetrics()));
        request.setPrescriptionDiagnoses(toDiagnosisRequests(data));
        return request;
    }

    private List<CreatePrescriptionDiagnosisRequest> toDiagnosisRequests(ParsedPrescriptionData data) {
        List<CreatePrescriptionDiagnosisRequest> requests = new ArrayList<>();
        if (data.getMedicines() == null) {
            return requests;
        }
        LocalDate startDate = parseDate(data.getDate());
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        for (ParsedMedicineData medicine : data.getMedicines()) {
            if (medicine == null || ObjectUtils.isEmpty(medicine.getName())) {
                continue;
            }
            CreatePrescriptionDiagnosisRequest request = new CreatePrescriptionDiagnosisRequest();
            request.setDiagnosis(data.getDiagnosis());
            request.setMedicineName(medicine.getName());
            request.setDosage(medicine.getDosage());
            request.setUnit(medicine.getQuantity() == null ? null : BigDecimal.valueOf(medicine.getQuantity()));
            request.setUnitMeasure(medicine.getQuantity() == null ? null : "unit");
            request.setStartDate(startDate);
            requests.add(request);
        }
        return requests;
    }

    private String[] splitPatientName(String patientName) {
        if (ObjectUtils.isEmpty(patientName)) {
            return new String[]{null, null};
        }
        String trimmed = patientName.trim();
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex < 0) {
            return new String[]{trimmed, null};
        }
        return new String[]{trimmed.substring(0, spaceIndex), trimmed.substring(spaceIndex + 1).trim()};
    }

    private Integer parseInteger(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatHealthMetrics(Map<String, String> healthMetrics) {
        if (healthMetrics == null || healthMetrics.isEmpty()) {
            return null;
        }
        List<String> entries = new ArrayList<>();
        healthMetrics.forEach((key, value) -> entries.add(key + ": " + value));
        return String.join(", ", entries);
    }
}
