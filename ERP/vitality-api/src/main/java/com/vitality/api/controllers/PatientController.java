package com.vitality.api.controllers;

import com.vitality.api.service.PatientService;
import com.vitality.common.dtos.CreatePatientRequest;
import com.vitality.common.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController("patientController")
@RequestMapping(Constants.PATIENT_PATH)
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody CreatePatientRequest request) {
        log.info("Received request to create/update patient with phone number: {}", request.phoneNumber());
        return patientService.createPatient(request);
    }
}
