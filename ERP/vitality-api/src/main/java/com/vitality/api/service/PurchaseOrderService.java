package com.vitality.api.service;

import com.vitality.api.entities.PurchaseOrder;
import com.vitality.api.entities.PurchaseOrderStatus;
import com.vitality.api.repositories.PurchaseOrderRepository;
import com.vitality.common.utils.ResponseGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Method to get the pending purchase orders, which are in PO_GENERATED status.
     *
     * @return the list of pending purchase orders. If there are no pending purchase orders, an empty list is returned.
     */
    public ResponseEntity<?> getPendingPurchaseOrders() {
        List<PurchaseOrder> pendingPurchaseOrders = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.PO_GENERATED);
        if (pendingPurchaseOrders == null || pendingPurchaseOrders.isEmpty()) {
            log.info("No pending purchase orders found.");
            return ResponseGenerator.generateSuccessResponse(Collections.EMPTY_LIST, HttpStatus.OK);
        }
        log.info("Found {} pending purchase orders.", pendingPurchaseOrders.size());
        return ResponseGenerator.generateSuccessResponse(pendingPurchaseOrders, HttpStatus.OK);
    }
}
