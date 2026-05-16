package com.vitality.api.service;

import com.vitality.api.entities.Patient;
import com.vitality.api.entities.Prescription;
import com.vitality.api.entities.PrescriptionDiagnosis;
import com.vitality.api.entities.User;
import com.vitality.api.repositories.PrescriptionDiagnosisRepository;
import com.vitality.api.repositories.PrescriptionRepository;
import com.vitality.common.dtos.*;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.CommonUtils;
import com.vitality.common.utils.GuidUtils;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.Validators;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.vitality.api.mappers.ResponseMappers.mapToCreatePrescriptionRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDiagnosisRepository prescriptionDiagnosisRepository;
    private final PatientService patientService;
    private final UserService userService;
    private final GeminiApiService geminiApiService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<String, PrescriptionJob> jobs = new ConcurrentHashMap<>();
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "rx_uploads");

    /**
     * Method to create a Prescription
     *
     * @param request: the request to create a Prescription.
     * @return the ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> createPrescription(@NotNull CreatePrescriptionRequest request) {
        if (ObjectUtils.isEmpty(request.getFirstName()) && ObjectUtils.isEmpty(request.getLastName())) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "First name and last name are required to create a prescription.");
        }
        return persistPrescription(request);
    }

    public ResponseEntity<?> createPrescriptionFromParsedData(@NotNull ParsedPrescriptionData data, String jobId, boolean isManual) {
        if (!isManual && jobs.containsKey(jobId)) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.NOT_FOUND, "Job not found");
        }
        CreatePrescriptionRequest request = mapToCreatePrescriptionRequest(data);
        return persistPrescription(request);
    }

    /**
     * Method to store the prescription data in the database.
     *
     * @param request: the request to create a Prescription.
     * @return the ResponseEntity indicating the result of the operation.
     */
    private ResponseEntity<?> persistPrescription(CreatePrescriptionRequest request) {
        try {
            Validators.validatePrescriptionCreateRequest(request);
            Patient patient = null;
            User customer = null;
            if (StringUtils.hasLength(request.getCustomerPhoneNumber())) {
                customer = userService.searchOrCreateUser(request.getCustomerFirstName(), request.getCustomerLastName(), null, request.getCustomerPhoneNumber());
            }
            if (StringUtils.hasLength(request.getPatientPhoneNumber())) {
                patient = patientService.searchAndCreatePatient(request.getFirstName(), request.getLastName(), request.getPatientPhoneNumber(), null);
                if (ObjectUtils.isEmpty(patient)) {
                    return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Patient details are invalid.");
                }
                patient.setCustomer(customer);
                log.info("Patient Created or Existed with patient id: {}", patient.getId());
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

    public ResponseEntity<?> parsePrescription(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "At least one prescription image is required.");
        }
        if (!geminiApiService.isConfigured()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API key is not configured.");
        }

        String jobId = GuidUtils.generateGuid();
        try {
            Files.createDirectories(UPLOAD_DIR);
            Path jobDir = UPLOAD_DIR.resolve(jobId);
            Files.createDirectories(jobDir);

            List<Path> imagePaths = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String filename = CommonUtils.safeFilename(file.getOriginalFilename());
                Path destination = jobDir.resolve(filename);
                file.transferTo(destination);
                imagePaths.add(destination);
            }
            if (imagePaths.isEmpty()) {
                return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "At least one non-empty prescription image is required.");
            }

            PrescriptionJob job = new PrescriptionJob(jobId, "uploading", "Uploading images...", imagePaths);
            jobs.put(jobId, job);
            executorService.submit(() -> parsePrescription(jobId));
            return ResponseEntity.ok(Map.of("job_id", jobId));
        } catch (Exception e) {
            log.error("Error while uploading prescription images for job {}: {}", jobId, e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while uploading the prescription images.");
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }

    private void parsePrescription(String jobId) {
        PrescriptionJob job = jobs.get(jobId);
        if (job == null) {
            return;
        }
        try {
            job.setStatus("parsing");
            job.setStep("Parsing prescription with Gemini...");
            ParsedPrescriptionData data = geminiApiService.parsePrescription(job.getImagePaths());
            job.setData(data);
            job.setStatus("ready");
            job.setStep("Done");
        } catch (Exception e) {
            log.error("Failed to parse prescription job {}: {}", jobId, e.getMessage());
            job.setStatus("error");
            job.setStep("Error");
            job.setError(e.getMessage());
        }
    }


    public ResponseEntity<?> getPrescriptionParseStatus(String jobId) {
        PrescriptionJob job = jobs.get(jobId);
        if (job == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.NOT_FOUND, "Job not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", job.getStatus());
        response.put("step", job.getStep());
        response.put("data", job.getData());
        response.put("error", job.getError());
        return ResponseEntity.ok(response);
    }
}
