package com.vitality.api.controllers;

import com.vitality.api.service.PrescriptionService;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.dtos.PrescriptionDataRequest;
import com.vitality.common.dtos.PrescriptionJob;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController("prescriptionController")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final SecurityUtils securityUtils;

    @PostMapping(Constants.PRESCRIPTION_PATH)
    public ResponseEntity<?> createPrescription(@RequestBody CreatePrescriptionRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Received request to create prescription for patient: ");
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        return prescriptionService.createPrescription(request);
    }

    @PostMapping({Constants.PRESCRIPTION_UPLOAD_PATH})
    public ResponseEntity<?> upload(@RequestParam("files") List<MultipartFile> files, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        return prescriptionService.parsePrescription(files);
    }

    @GetMapping({Constants.PRESCRIPTION_STATUS_PATH})
    public ResponseEntity<?> status(@PathVariable("jobId") String jobId, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        return prescriptionService.getPrescriptionParseStatus(jobId);
    }


    @PostMapping({Constants.PRESCRIPTION_CONFIRM_PATH})
    public ResponseEntity<?> confirm(@PathVariable("jobId") String jobId, @RequestBody PrescriptionDataRequest request, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        if (request == null || request.getData() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Prescription data is required.");
        }
        return prescriptionService.createPrescriptionFromParsedData(request.getData(), jobId, false);
    }

    @PostMapping({Constants.PRESCRIPTION_MANUAL_PATH})
    public ResponseEntity<?> saveManualPrescription(@RequestBody PrescriptionDataRequest request, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        if (request == null || request.getData() == null) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, "Prescription data is required.");
        }
        return prescriptionService.createPrescriptionFromParsedData(request.getData(), null, true);
    }
}
