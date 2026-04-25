package com.vitality.api.controllers;

import com.vitality.api.service.OrderService;
import com.vitality.common.dtos.CreateOrderRequest;
import com.vitality.common.dtos.JwtValidationResult;
import com.vitality.common.utils.Constants;
import com.vitality.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController("orderController")
@RequestMapping(path = Constants.ORDER_PATH)
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request, @RequestHeader Map<String, String> httpHeaders) {
        log.info("Request Received for Order creation");
        JwtValidationResult validationResult = securityUtils.validateRequest(httpHeaders);
        if (!validationResult.valid()) {
            return validationResult.errorResponse();
        }
        return orderService.createOrder(request);
    }
}
