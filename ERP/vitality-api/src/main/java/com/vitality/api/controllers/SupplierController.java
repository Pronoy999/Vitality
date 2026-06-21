package com.vitality.api.controllers;

import com.vitality.api.service.SupplierService;
import com.vitality.common.dtos.CreateSupplierRequest;
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
@RestController("supplierController")
@RequestMapping(Constants.SUPPLIER_PATH)
public class SupplierController {

    private final SupplierService supplierService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<?> getSuppliers(@RequestHeader Map<String, String> httpHeaders) {
        log.info("Received Supplier GET Request.");
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (validationResult.valid()) {
            return supplierService.getSuppliers();
        }
        return validationResult.errorResponse();
    }

    @PostMapping
    public ResponseEntity<?> createSupplier(@RequestBody CreateSupplierRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Received Supplier Creation Request");
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (validationResult.valid()) {
            return supplierService.createSupplier(request);
        }
        return validationResult.errorResponse();
    }
}
