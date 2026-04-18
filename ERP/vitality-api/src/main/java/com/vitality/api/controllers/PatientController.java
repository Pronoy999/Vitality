package com.vitality.api.controllers;

import com.vitality.api.service.PatientService;
import com.vitality.common.dtos.CreatePatientRequest;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController("patientController")
@RequestMapping(Constants.PATIENT_PATH)
public class PatientController {
    private final PatientService patientService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody CreatePatientRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Received request to create/update patient with phone number: ");
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        return patientService.createPatient(request);
    }
}
