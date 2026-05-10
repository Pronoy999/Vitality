package com.vitality.api.controllers;

import com.vitality.api.service.GeminiApiService;
import com.vitality.api.service.InvoiceService;
import com.vitality.common.dtos.CreateInvoiceRequest;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.dtos.ParsedInvoiceData;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
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

@RestController("invoiceController")
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Constants.INVOICE_PATH)
public class InvoiceController {
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "invoice_uploads");

    private final SecurityUtils securityUtils;
    private final InvoiceService invoiceService;
    private final GeminiApiService geminiApiService;
    private final Map<String, InvoiceJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody CreateInvoiceRequest request, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult result = securityUtils.validateRequest(httpHeaders);
        if (!result.valid()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Unauthorized access. Please provide a valid token.");
        }
        try {
            return invoiceService.createInvoice(request);
        } catch (InvalidRequestException e) {
            log.error("Invalid request for creating invoice: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating invoice: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create invoice. Please try again later.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getInvoice(@RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult result = securityUtils.validateRequest(httpHeaders);
        if (!result.valid()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Unauthorized access. Please provide a valid token.");
        }
        return invoiceService.getAllInvoices();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("files") List<MultipartFile> files, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        if (files == null || files.isEmpty()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "At least one invoice image is required.");
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
                return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "At least one non-empty invoice image is required.");
            }

            InvoiceJob job = new InvoiceJob(jobId, "uploading", "Uploading images...", imagePaths);
            jobs.put(jobId, job);
            executorService.submit(() -> parseInvoice(jobId));
            return ResponseGenerator.generateSuccessResponse(Map.of("job_id", jobId), HttpStatus.OK);
        } catch (IOException e) {
            log.error("Failed to store invoice upload: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to upload invoice images.");
        }
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> status(@PathVariable("jobId") String jobId, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        InvoiceJob job = jobs.get(jobId);
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

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }

    private void parseInvoice(String jobId) {
        InvoiceJob job = jobs.get(jobId);
        if (job == null) {
            return;
        }
        try {
            job.status = "parsing";
            job.step = "Parsing invoice with Gemini...";
            ParsedInvoiceData data = geminiApiService.parseInvoice(job.imagePaths);
            job.data = data;
            job.status = "ready";
            job.step = "Done";
        } catch (Exception e) {
            log.error("Failed to parse invoice job {}: {}", jobId, e.getMessage());
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

    private static class InvoiceJob {
        private final String id;
        private volatile String status;
        private volatile String step;
        private volatile ParsedInvoiceData data;
        private volatile String error;
        private final List<Path> imagePaths;

        private InvoiceJob(String id, String status, String step, List<Path> imagePaths) {
            this.id = id;
            this.status = status;
            this.step = step;
            this.imagePaths = imagePaths;
        }
    }
}
