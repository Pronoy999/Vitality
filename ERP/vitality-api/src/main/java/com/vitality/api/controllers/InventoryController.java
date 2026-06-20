package com.vitality.api.controllers;

import com.vitality.api.service.InventoryService;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.INVENTORY_PATH)
public class InventoryController {

    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<?> getInventory(@RequestHeader Map<String, String> headers) {
        JwtValidationResult validationResult = securityUtils.validateRequest(headers);
        if (!validationResult.valid()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT Token or missing JWT Token");
        }
        return inventoryService.searchInventory(null, false);
    }

    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringInventory(@RequestHeader Map<String, String> headers) {
        JwtValidationResult validationResult = securityUtils.validateRequest(headers);
        if (!validationResult.valid()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT Token or missing JWT Token");
        }
        return inventoryService.searchInventory(null, true);
    }
}
