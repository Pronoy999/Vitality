package com.vitality.api.controllers;

import com.vitality.api.service.PrescriptionService;
import com.vitality.common.dtos.CreatePrescriptionRequest;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController("prescriptionController")
@RequestMapping(Constants.PRESCRIPTION_PATH)
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<?> createPrescription(@RequestBody CreatePrescriptionRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Received request to create prescription for patient: ");
        String jwtToken = httpHeaders.get(Constants.JWT_HEADER_KEY);
        if (jwtToken == null || jwtToken.isEmpty()) {
            log.error("JWT token is missing in the request headers.");
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "JWT header token is missing");
        }
        return prescriptionService.createPrescription(request, jwtToken);
    }
}
