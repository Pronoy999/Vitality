package com.vitality.api.service;

import com.vitality.api.entities.PurchaseOrder;
import com.vitality.api.entities.PurchaseOrderItem;
import com.vitality.api.entities.PurchaseOrderStatus;
import com.vitality.api.entities.Supplier;
import com.vitality.api.repositories.PurchaseOrderRepository;
import com.vitality.common.dtos.CreatePurchaseOrderItems;
import com.vitality.common.dtos.CreatePurchaseOrderRequest;
import com.vitality.common.dtos.CreatePurchaseOrderResponse;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.CommonUtils;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierService supplierService;

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

    public void updatePurchaseOrderStatus(Long purchaseOrderId, PurchaseOrderStatus status) {
        int rowsAffected = purchaseOrderRepository.updatePurchaseOrderStatus(purchaseOrderId, status);
        log.info("Updated purchase order status for purchaseOrderId: {} to status: {}. Rows affected: {}", purchaseOrderId, status, rowsAffected);
    }

    /**
     * Method to create the Purchase Order.
     *
     * @param request The request to create the purchase order. It contains the supplier id, purchase order delivery date and the list of items to be added in the purchase order.
     * @return the response entity containing the created purchase order and the status code. If the request is invalid, a bad request response is returned.
     */
    public ResponseEntity<?> createPurchaseOrder(CreatePurchaseOrderRequest request) {
        try {
            Validators.validatePurchaseOrderRequest(request);
            Long supplierId = request.getSupplierId();
            Supplier supplier = supplierService.getSupplierById(supplierId);
            String poNumber = CommonUtils.generatePurchaseOrderNumber();
            LocalDate currentDate = LocalDate.now();
            PurchaseOrder purchaseOrder = new PurchaseOrder();
            purchaseOrder.setSupplier(supplier);
            purchaseOrder.setPoNumber(poNumber);
            purchaseOrder.setStatus(PurchaseOrderStatus.PO_GENERATED);
            purchaseOrder.setPoGenerationDate(currentDate);
            purchaseOrder.setPoDeliveryDate(request.getPurchaseOrderDeliveryDate());
            List<PurchaseOrderItem> purchaseOrderItems = getPurchaseOrderItems(purchaseOrder, request.getItems());
            purchaseOrder.setPurchaseOrderItems(purchaseOrderItems);
            purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
            log.info("Purchase Order created successfully with id: {} and poNumber: {}", purchaseOrder.getId(), purchaseOrder.getPoNumber());
            CreatePurchaseOrderResponse response = new CreatePurchaseOrderResponse();
            response.setPoNumber(poNumber);
            response.setStatus(PurchaseOrderStatus.PO_GENERATED.name());
            return ResponseGenerator.generateSuccessResponse(response, HttpStatus.CREATED);
        } catch (InvalidRequestException e) {
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create purchase order. Error: {}", e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create purchase order. Please try again later.");
        }
    }

    private List<PurchaseOrderItem> getPurchaseOrderItems(final PurchaseOrder order, @NotNull List<CreatePurchaseOrderItems> purchaseOrderItems) {
        return purchaseOrderItems.stream().map(item -> {
            PurchaseOrderItem purchaseOrderItem = new PurchaseOrderItem();
            purchaseOrderItem.setItemDesc(item.getItemDesc());
            purchaseOrderItem.setItemQty(item.getItemQty());
            purchaseOrderItem.setEstimatedPrice(item.getEstimatedPrice());
            purchaseOrderItem.setPurchaseOrder(order);
            return purchaseOrderItem;
        }).toList();
    }
}
