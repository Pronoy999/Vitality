package com.vitality.common.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSupplierRequest {
    public CreateSupplierRequest(String supplierName) {
        this.supplierName = supplierName;
    }
    @Valid
    @NotEmpty
    private String supplierName;
    private String supplierAddress;
    private String pocName;
    private String pocPhone;
    private BigDecimal estimateDeliveryInDays;
}
