package com.vitality.common.dtos;

import org.springframework.http.ResponseEntity;

public record JwtValidationResult(boolean valid, ResponseEntity<?> errorResponse, JwtData decodedJwt) {
}
