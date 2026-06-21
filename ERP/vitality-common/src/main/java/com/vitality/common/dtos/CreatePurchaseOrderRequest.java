package com.vitality.common.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseOrderRequest {
    private Long supplierId;
    private LocalDate purchaseOrderDeliveryDate;
    private List<CreatePurchaseOrderItems> items;
}
