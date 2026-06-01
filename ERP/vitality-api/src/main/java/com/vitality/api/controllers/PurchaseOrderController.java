package com.vitality.api.controllers;

import com.vitality.api.service.PurchaseOrderService;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController("purchaseOrderController")
@RequestMapping(Constants.PURCHASE_ORDER_PATH)
public class PurchaseOrderController {

    private final SecurityUtils securityUtils;
    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<?> getPendingPurchaseOrders(@RequestHeader Map<String, String> httpHeaders) {
        log.info("Received Purchase Order GET Request.");
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (validationResult.valid()) {
            return purchaseOrderService.getPendingPurchaseOrders();
        }
        return validationResult.errorResponse();
    }
}
