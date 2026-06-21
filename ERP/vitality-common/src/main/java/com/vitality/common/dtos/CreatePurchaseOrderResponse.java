package com.vitality.common.dtos;

import lombok.Data;

@Data
public class CreatePurchaseOrderResponse {
    private String poNumber;
    private String status;
}
