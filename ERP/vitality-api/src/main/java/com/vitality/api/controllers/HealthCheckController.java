package com.vitality.api.controllers;

import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("healthCheckController")
@RequiredArgsConstructor
@RequestMapping(Constants.HEALTH_CHECK_PATH)
@Slf4j
public class HealthCheckController {
    @GetMapping
    public ResponseEntity<?> healthCheck() {
        log.info("Received health check request");
        return ResponseGenerator.generateSuccessResponse("Vitality API is up and running", HttpStatus.OK);
    }
}
