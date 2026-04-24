package com.vitality.api.controllers;

import com.vitality.api.service.InventoryService;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("inventoryController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = Constants.INVENTORY_PATH)
public class InventoryController {
    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<?> getInventory(@RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            log.error("Invalid Request Token");
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT Token or missing JWT Token");
        }
        return inventoryService.searchInventory(null);
    }
}
