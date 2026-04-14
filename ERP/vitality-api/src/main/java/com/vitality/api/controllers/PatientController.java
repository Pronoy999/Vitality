package com.vitality.api.controllers;

import com.vitality.api.service.JwtAuthenticationService;
import com.vitality.api.service.PatientService;
import com.vitality.common.dtos.CreatePatientRequest;
import com.vitality.common.utils.Constants;
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
    private final JwtAuthenticationService jwtAuthenticationService;

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody CreatePatientRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Received request to create/update patient with phone number: ");
        ResponseEntity<?> authFailure = jwtAuthenticationService.validateRequest(httpHeaders);
        if (authFailure != null) {
            return authFailure;
        }
        return patientService.createPatient(request);
    }
}
