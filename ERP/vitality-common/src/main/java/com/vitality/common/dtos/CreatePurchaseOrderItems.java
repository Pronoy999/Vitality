package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePurchaseOrderItems {
    private String itemDesc;
    private BigDecimal itemQty;
    private BigDecimal estimatedPrice;
}
