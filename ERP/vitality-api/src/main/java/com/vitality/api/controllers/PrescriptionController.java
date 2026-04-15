package com.vitality.api.controllers;

import com.vitality.api.service.GeminiApiService;
import com.vitality.api.service.JwtAuthenticationService;
import com.vitality.api.service.PrescriptionService;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.dtos.ParsedPrescriptionData;
import com.vitality.common.dtos.PrescriptionDataRequest;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Slf4j
@RestController("prescriptionController")
public class PrescriptionController {
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "rx_uploads");

    private final PrescriptionService prescriptionService;
    private final GeminiApiService geminiApiService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final Map<String, PrescriptionJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping(Constants.PRESCRIPTION_PATH)
    public ResponseEntity<?> createPrescription(@RequestBody CreatePrescriptionRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Received request to create prescription for patient: ");
        ResponseEntity<?> authFailure = jwtAuthenticationService.validateRequest(httpHeaders);
        if (authFailure != null) {
            return authFailure;
        }
        return prescriptionService.createPrescription(request);
    }

    @PostMapping({Constants.PRESCRIPTION_UPLOAD_PATH})
    public ResponseEntity<?> upload(@RequestParam("files") List<MultipartFile> files, @RequestHeader Map<String, String> httpHeaders) {
        ResponseEntity<?> authFailure = jwtAuthenticationService.validateRequest(httpHeaders);
        if (authFailure != null) {
            return authFailure;
        }
        if (files == null || files.isEmpty()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "At least one prescription image is required.");
        }
        if (!geminiApiService.isConfigured()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API key is not configured.");
        }

        String jobId = UUID.randomUUID().toString();
        try {
            Files.createDirectories(UPLOAD_DIR);
            Path jobDir = UPLOAD_DIR.resolve(jobId);
            Files.createDirectories(jobDir);

            List<Path> imagePaths = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String filename = safeFilename(file.getOriginalFilename());
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
        } catch (IOException e) {
            log.error("Failed to store prescription upload: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to upload prescription images.");
        }
    }

    @GetMapping({Constants.PRESCRIPTION_STATUS_PATH})
    public ResponseEntity<?> status(@PathVariable("jobId") String jobId, @RequestHeader Map<String, String> httpHeaders) {
        ResponseEntity<?> authFailure = jwtAuthenticationService.validateRequest(httpHeaders);
        if (authFailure != null) {
            return authFailure;
        }
        PrescriptionJob job = jobs.get(jobId);
        if (job == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.NOT_FOUND, "Job not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", job.status);
        response.put("step", job.step);
        response.put("data", job.data);
        response.put("error", job.error);
        return ResponseEntity.ok(response);
    }

    @PostMapping({Constants.PRESCRIPTION_CONFIRM_PATH})
    public ResponseEntity<?> confirm(@PathVariable("jobId") String jobId, @RequestBody PrescriptionDataRequest request, @RequestHeader Map<String, String> httpHeaders) {
        ResponseEntity<?> authFailure = jwtAuthenticationService.validateRequest(httpHeaders);
        if (authFailure != null) {
            return authFailure;
        }
        if (!jobs.containsKey(jobId)) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.NOT_FOUND, "Job not found");
        }
        if (request == null || request.getData() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Prescription data is required.");
        }
        return prescriptionService.createPrescriptionFromParsedData(request.getData());
    }

    @PostMapping({Constants.PRESCRIPTION_MANUAL_PATH})
    public ResponseEntity<?> saveManualPrescription(@RequestBody PrescriptionDataRequest request, @RequestHeader Map<String, String> httpHeaders) {
        ResponseEntity<?> authFailure = jwtAuthenticationService.validateRequest(httpHeaders);
        if (authFailure != null) {
            return authFailure;
        }
        if (request == null || request.getData() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Prescription data is required.");
        }
        return prescriptionService.createPrescriptionFromParsedData(request.getData());
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
            job.status = "parsing";
            job.step = "Parsing prescription with Gemini...";
            ParsedPrescriptionData data = geminiApiService.parsePrescription(job.imagePaths);
            job.data = data;
            job.status = "ready";
            job.step = "Done";
        } catch (Exception e) {
            log.error("Failed to parse prescription job {}: {}", jobId, e.getMessage());
            job.status = "error";
            job.step = "Error";
            job.error = e.getMessage();
        }
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return UUID.randomUUID() + ".jpg";
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    private static class PrescriptionJob {
        private final String id;
        private volatile String status;
        private volatile String step;
        private volatile ParsedPrescriptionData data;
        private volatile String error;
        private final List<Path> imagePaths;

        private PrescriptionJob(String id, String status, String step, List<Path> imagePaths) {
            this.id = id;
            this.status = status;
            this.step = step;
            this.imagePaths = imagePaths;
        }
    }
}
