package com.vitality.api.controllers;

import com.vitality.api.service.InvoiceService;
import com.vitality.common.dtos.CreateInvoiceRequest;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("invoiceController")
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Constants.INVOICE_PATH)
public class InvoiceController {
    private final SecurityUtils securityUtils;
    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<?> createInvoice(@RequestBody CreateInvoiceRequest request, @RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult result = securityUtils.validateRequest(httpHeaders);
        if (!result.valid()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Unauthorized access. Please provide a valid token.");
        }
        try {
            return invoiceService.createInvoice(request);
        } catch (InvalidRequestException e) {
            log.error("Invalid request for creating invoice: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error creating invoice: ", e);
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create invoice. Please try again later.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getInvoice(@RequestHeader Map<String, String> httpHeaders) {
        JwtValidationResult result = securityUtils.validateRequest(httpHeaders);
        if (!result.valid()) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.UNAUTHORIZED, "Unauthorized access. Please provide a valid token.");
        }
        return invoiceService.getAllInvoices();
    }
}
