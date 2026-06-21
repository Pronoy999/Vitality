package com.vitality.common.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetSuppliersResponse {
    private Long supplierId;
    private String supplierName;
    private String supplierAddress;
    private String pocName;
    private String pocContact;
    private BigDecimal estimateDeliveryInDays;
}
