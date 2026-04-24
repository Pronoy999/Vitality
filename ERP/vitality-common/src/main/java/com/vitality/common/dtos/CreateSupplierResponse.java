package com.vitality.common.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@Data
@RequiredArgsConstructor
public class CreateSupplierResponse {
    private BigInteger supplierId;
    private String supplierName;
}
