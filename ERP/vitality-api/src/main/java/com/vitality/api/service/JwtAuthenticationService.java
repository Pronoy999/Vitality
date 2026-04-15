package com.vitality.api.service;

import com.vitality.common.exceptions.InvalidTokenException;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationService {
    private final SecurityUtils securityUtils;

    public ResponseEntity<?> validateRequest(Map<String, String> httpHeaders) {
        String jwtToken = getJwtToken(httpHeaders);
        if (jwtToken == null || jwtToken.isBlank()) {
            log.error("JWT token is missing in the request headers.");
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "JWT header token is missing");
        }
        try {
            securityUtils.decodeJwt(jwtToken);
            return null;
        } catch (InvalidTokenException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }
    }

    public String getJwtToken(Map<String, String> httpHeaders) {
        if (httpHeaders == null || httpHeaders.isEmpty()) {
            return null;
        }
        String jwtToken = httpHeaders.get(Constants.JWT_HEADER_KEY);
        if (jwtToken != null) {
            return jwtToken;
        }
        for (Map.Entry<String, String> header : httpHeaders.entrySet()) {
            if (Constants.JWT_HEADER_KEY.equalsIgnoreCase(header.getKey())) {
                return header.getValue();
            }
        }
        return null;
    }
}
